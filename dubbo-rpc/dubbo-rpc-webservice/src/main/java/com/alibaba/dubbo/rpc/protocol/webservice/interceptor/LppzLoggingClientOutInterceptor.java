package com.alibaba.dubbo.rpc.protocol.webservice.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;

import sun.net.www.MessageHeader;

import com.alibaba.dubbo.rpc.protocol.webservice.util.BaseLoggingClientUtil;
import com.alibaba.dubbo.rpc.protocol.webservice.util.ReFelctionUtil;
import com.alibaba.dubbo.rpc.support.DubboInvokeDetail;
import com.alibaba.fastjson.JSON;

@SuppressWarnings("restriction")
public class LppzLoggingClientOutInterceptor extends LoggingOutInterceptor {
	@Override
	public void handleMessage(Message message) throws Fault {
		BaseLoggingClientUtil.getInstance().th.set(new DubboInvokeDetail());
		HttpURLConnection hc = (HttpURLConnection) message
				.getContextualProperty("http.connection");
		MessageContentsList mcl = null;
		try {
			mcl =BaseLoggingClientUtil.getInstance().getMCL(message);
			buildClientMsgDto(mcl, hc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildClientMsgDto(MessageContentsList mcl, HttpURLConnection hc)
			throws IOException {
		DubboInvokeDetail dto = BaseLoggingClientUtil.getInstance().th.get();
		if (dto == null)
			return;
		dto.setProtocol("webservice");
		dto.setProvider(hc.getURL().toString());
		dto.setType("consumer");
		dto.setHostUri(hc.getURL().toString() + "?wsdl");
		try {
			dto.setConsumer(InetAddress.getLocalHost().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		dto.setHttpMethodType(hc.getRequestMethod());
		dto.setRequestBody(JSON.toJSONString(mcl));
	}

	public class LppzLoggingClientInInterceptor extends LoggingInInterceptor {
		@Override
		public void handleMessage(Message message) throws Fault {
			DubboInvokeDetail dto = BaseLoggingClientUtil.getInstance().th
					.get();
			if (dto == null)
				return;
			InputStream is = (InputStream) message
					.getContent(InputStream.class);
			HttpURLConnection huc = null;
			MessageHeader reqheader = null;
			try {
				huc = ReFelctionUtil.getDynamicObj(is.getClass(), "this$0", is);
				reqheader = ReFelctionUtil.getDynamicObj(
						sun.net.www.protocol.http.HttpURLConnection.class,
						"requests", huc);
//				dto.setRequesthttpHeader(reqheader.getHeaders().toString());
//				dto.setResponsehttpHeader(message.get(Message.PROTOCOL_HEADERS)
//						.toString());
				dto.setResponseBody(BaseLoggingClientUtil.getInstance().cpAndhandleMessage(is, message));
				BaseLoggingClientUtil.getInstance().send(dto);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
