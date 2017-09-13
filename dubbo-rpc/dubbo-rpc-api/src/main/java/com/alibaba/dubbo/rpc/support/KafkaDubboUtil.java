package com.alibaba.dubbo.rpc.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaDubboUtil {
	private static KafkaProducer<DubboInvokeDetail> logSender;
	private static Map<Method, Boolean> cacheAuth = new ConcurrentHashMap<Method, Boolean>();

	public static KafkaProducer<DubboInvokeDetail> getLogSender() {
		return logSender;
	}

	public static void setLogSender(KafkaProducer<DubboInvokeDetail> logSender) {
		KafkaDubboUtil.logSender = logSender;
	}

	public static boolean checkNeedHttpLogging(Method m) {
		if (m == null)
			return false;
		if (cacheAuth.get(m) != null) {
			return cacheAuth.get(m);
		}
		DubboRpcLog rh = m.getAnnotation(DubboRpcLog.class);
		if (rh != null) {
			cacheAuth.put(m, rh.value());
			return rh.value();
		}
		Class<?> clazz = getDynamicObj(m.getClass(), "clazz", m);
		rh = clazz.getAnnotation(DubboRpcLog.class);
		if (rh == null) {
			cacheAuth.put(m, false);
			return false;
		}
		cacheAuth.put(m, rh.value());
		return rh.value();
	}

	@SuppressWarnings("unchecked")
	private static <T> T getDynamicObj(Class<?> clazz, String name,
			Object instance) {
		try {
			Field ff = clazz.getDeclaredField(name);
			ff.setAccessible(true);
			return (T) ff.get(instance);
		} catch (Exception e) {
		}
		return null;
	}
}
