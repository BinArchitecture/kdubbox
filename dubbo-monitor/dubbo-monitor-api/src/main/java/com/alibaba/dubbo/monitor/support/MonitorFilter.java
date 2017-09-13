/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.monitor.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorFactory;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.support.DubboInvokeDetail;
import com.alibaba.dubbo.rpc.support.KafkaDubboUtil;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.alibaba.fastjson.JSON;

/**
 * MonitorFilter. (SPI, Singleton, ThreadSafe)
 * 
 * @author william.liangf
 */
@Activate(group = { Constants.PROVIDER, Constants.CONSUMER })
public class MonitorFilter implements Filter {
	private static final Logger logger = LoggerFactory
			.getLogger(MonitorFilter.class);
	private final ConcurrentMap<String, AtomicInteger> concurrents = new ConcurrentHashMap<String, AtomicInteger>();
	private MonitorFactory monitorFactory;

	public void setMonitorFactory(MonitorFactory monitorFactory) {
		this.monitorFactory = monitorFactory;
	}

	// 调用过程拦截
	public Result invoke(Invoker<?> invoker, Invocation invocation)
			throws RpcException {
		boolean boo = false;
		if (invocation instanceof RpcInvocation) {
			RpcInvocation rpc = (RpcInvocation) invocation;
			Method method = rpc.getMethod();
			if (method == null) {
				try {
					method = invoker.getInterface().getDeclaredMethod(
							invocation.getMethodName(),
							invocation.getParameterTypes());
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
			boo = KafkaDubboUtil.checkNeedHttpLogging(method);
		}
		if (boo && invoker.getUrl().hasParameter(Constants.MONITOR_KEY)) {
			RpcContext context = RpcContext.getContext(); // 提供方必须在invoke()之前获取context信息
			long start = System.currentTimeMillis(); // 记录起始时间戮
			getConcurrent(invoker, invocation).incrementAndGet(); // 并发计数
			try {
				Result result = invoker.invoke(invocation); // 让调用链往下执行
				collect(invoker, invocation, result, context, start,
						result.hasException());
				return result;
			} catch (RpcException e) {
				collect(invoker, invocation, null, context, start, true);
				throw e;
			} finally {
				getConcurrent(invoker, invocation).decrementAndGet(); // 并发计数
			}
		} else {
			return invoker.invoke(invocation);
		}
	}

	// 信息采集
	private void collect(Invoker<?> invoker, Invocation invocation,
			Result result, RpcContext context, long start, boolean error) {
		URL statistics = null;
		try {
			// ---- 服务信息获取 ----
			long elapsed = System.currentTimeMillis() - start; // 计算调用耗时
			int concurrent = getConcurrent(invoker, invocation).get(); // 当前并发数
			String application = invoker.getUrl().getParameter(
					Constants.APPLICATION_KEY);
			String service = invoker.getInterface().getName(); // 获取服务名称
			String method = RpcUtils.getMethodName(invocation); // 获取方法名
			URL url = invoker.getUrl().getUrlParameter(Constants.MONITOR_KEY);
			Monitor monitor = monitorFactory.getMonitor(url);
			int localPort;
			String remoteKey;
			String remoteValue;
			if (Constants.CONSUMER_SIDE.equals(invoker.getUrl().getParameter(
					Constants.SIDE_KEY))) {
				// ---- 服务消费方监控 ----
				context = RpcContext.getContext(); // 消费方必须在invoke()之后获取context信息
				localPort = 0;
				remoteKey = MonitorService.PROVIDER;
				remoteValue = invoker.getUrl().getAddress();
			} else {
				// ---- 服务提供方监控 ----
				localPort = invoker.getUrl().getPort();
				remoteKey = MonitorService.CONSUMER;
				remoteValue = context.getRemoteHost();
			}
			String input = "", output = "";
			if (invocation.getAttachment(Constants.INPUT_KEY) != null) {
				input = invocation.getAttachment(Constants.INPUT_KEY);
			}
			if (result != null
					&& result.getAttachment(Constants.OUTPUT_KEY) != null) {
				output = result.getAttachment(Constants.OUTPUT_KEY);
			}
			statistics = new URL(Constants.COUNT_PROTOCOL,
					NetUtils.getLocalHost(), localPort, service + "/" + method,
					MonitorService.APPLICATION, application,
					MonitorService.INTERFACE, service, MonitorService.METHOD,
					method, remoteKey, remoteValue,
					error ? MonitorService.FAILURE : MonitorService.SUCCESS,
					"1", MonitorService.ELAPSED, String.valueOf(elapsed),
					MonitorService.CONCURRENT, String.valueOf(concurrent),
					Constants.INPUT_KEY, input, Constants.OUTPUT_KEY, output);
			monitor.collect(statistics);
		} catch (Throwable t) {
			logger.error("Failed to monitor count service " + invoker.getUrl()
					+ ", cause: " + t.getMessage(), t);
		} finally {
			if (KafkaDubboUtil.getLogSender() != null) {
				DubboInvokeDetail di = buildDi(statistics, invocation, result,
						invoker);
				if (!checkLog(di))
					try {
						KafkaDubboUtil.getLogSender().sendMsg(di);
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
					}
			}
		}
	}

	private DubboInvokeDetail buildDi(URL statistics, Invocation invocation,
			Result result, Invoker<?> invoker) {
		DubboInvokeDetail dubboInvoke = new DubboInvokeDetail();
		if (statistics.hasParameter(MonitorService.PROVIDER)) {
			dubboInvoke.setType(MonitorService.CONSUMER);
			dubboInvoke.setConsumer(statistics.getHost());
			dubboInvoke.setProvider(statistics
					.getParameter(MonitorService.PROVIDER));
			int i = dubboInvoke.getProvider().indexOf(':');
			if (i > 0) {
				dubboInvoke.setProvider(dubboInvoke.getProvider().substring(0,
						i));
			}
		} else {
			dubboInvoke.setType(MonitorService.PROVIDER);
			dubboInvoke.setConsumer(statistics
					.getParameter(MonitorService.CONSUMER));
			int i = dubboInvoke.getConsumer().indexOf(':');
			if (i > 0) {
				dubboInvoke.setConsumer(dubboInvoke.getConsumer().substring(0,
						i));
			}
			dubboInvoke.setProvider(statistics.getHost());
		}
		dubboInvoke.setProtocol(invoker.getUrl().getProtocol());
		if (checkLog(dubboInvoke))
			return dubboInvoke;
		dubboInvoke.setService(statistics.getServiceInterface());
		dubboInvoke.setMethod(statistics.getParameter(MonitorService.METHOD));
		if (result != null) {
			if (result.hasException()) {
				dubboInvoke.setException(result.getException().getMessage());
				dubboInvoke.setExceptionClazz(result.getException().getClass()
						.getName());
			}
			dubboInvoke.setResponseBody(JSON.toJSONString(result.getResult()));
		}
		dubboInvoke.setHostUri(invoker.getUrl().toServiceString());
		dubboInvoke
				.setRequestBody(JSON.toJSONString(invocation.getArguments()));

		return dubboInvoke;
	}

	private boolean checkLog(DubboInvokeDetail dubboInvoke) {
		return ("rest".equals(dubboInvoke.getProtocol()) && "provider"
				.equals(dubboInvoke.getType()))
				|| "webservice".equals(dubboInvoke.getProtocol());
	}

	// 获取并发计数器
	private AtomicInteger getConcurrent(Invoker<?> invoker,
			Invocation invocation) {
		String key = invoker.getInterface().getName() + "."
				+ invocation.getMethodName();
		AtomicInteger concurrent = concurrents.get(key);
		if (concurrent == null) {
			concurrents.putIfAbsent(key, new AtomicInteger());
			concurrent = concurrents.get(key);
		}
		return concurrent;
	}
}