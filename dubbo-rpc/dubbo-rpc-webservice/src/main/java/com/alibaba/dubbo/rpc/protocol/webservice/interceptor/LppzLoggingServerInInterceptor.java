package com.alibaba.dubbo.rpc.protocol.webservice.interceptor;

import java.io.InputStream;

import org.apache.catalina.connector.ResponseFacade;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;

import com.alibaba.dubbo.rpc.protocol.webservice.util.BaseLoggingClientUtil;
import com.alibaba.dubbo.rpc.support.DubboInvokeDetail;
import com.alibaba.fastjson.JSON;
public class LppzLoggingServerInInterceptor extends LoggingInInterceptor{
	@Override
	public void handleMessage(Message message) throws Fault {
		BaseLoggingClientUtil.getInstance().th.set(new DubboInvokeDetail());
		buildReqByMsg(message);
	}
	
	private void buildReqByMsg(Message message) {
		DubboInvokeDetail dto=BaseLoggingClientUtil.getInstance().th.get();
		if(dto==null)
			return;
//		String reqheader=message.getContextualProperty("org.apache.cxf.message.Message.PROTOCOL_HEADERS").toString();
//		dto.setRequesthttpHeader(reqheader);
		String hostUri=message.getContextualProperty("org.apache.cxf.request.url").toString()+"?wsdl";
		dto.setHostUri(hostUri);
		dto.setProtocol("webservice");
		dto.setProvider(message.getContextualProperty("org.apache.cxf.request.url").toString());
		dto.setType("provider");
		org.apache.catalina.connector.RequestFacade reqfacade=(org.apache.catalina.connector.RequestFacade)message.getContextualProperty("HTTP.REQUEST");
		dto.setConsumer(reqfacade.getRemoteAddr());
		dto.setHttpMethodType(reqfacade.getMethod());
		InputStream is = (InputStream)message.getContent(InputStream.class);
		try {
			dto.setRequestBody(BaseLoggingClientUtil.getInstance().cpAndhandleMessage(is, message));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class LppzLoggingServerOutInterceptor extends LoggingOutInterceptor{
		@Override
		public void handleMessage(Message message) throws Fault {
			DubboInvokeDetail dto=BaseLoggingClientUtil.getInstance().th.get();
			if(dto==null)
				return;
			MessageContentsList mcl=BaseLoggingClientUtil.getInstance().getMCL(message);
			dto.setResponseBody(JSON.toJSONString(mcl));
//			dto.setResponsehttpHeader(buildRespHeader(message));
			BaseLoggingClientUtil.getInstance().send(dto);
		}

		private String buildRespHeader(Message message) {
//			HTTP.RESPONSE
			ResponseFacade resf=(ResponseFacade) message.getContextualProperty("HTTP.RESPONSE");
			String sheader="";
			for(String hname:resf.getHeaderNames()){
				sheader+=resf.getHeader(hname);
				sheader+="\n";
			}
			return sheader;
		}
	}
}
