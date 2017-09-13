package com.alibaba.dubbo.rpc.protocol.webservice.util;
import java.io.IOException;
import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.message.MessageImpl;

import com.alibaba.dubbo.rpc.support.DubboInvokeDetail;
import com.alibaba.dubbo.rpc.support.KafkaDubboUtil;

public class BaseLoggingClientUtil {
	private static BaseLoggingClientUtil instance=new BaseLoggingClientUtil();
	private BaseLoggingClientUtil(){}
	public static BaseLoggingClientUtil getInstance(){
		return instance;
	}
	public ThreadLocal<DubboInvokeDetail> th=new ThreadLocal<DubboInvokeDetail>();
	public void send(DubboInvokeDetail dto) {
		if(KafkaDubboUtil.getLogSender()!=null)
			try {
				KafkaDubboUtil.getLogSender().sendMsg(dto);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public String cpAndhandleMessage(InputStream is, Message message) {
		if (is != null) {
			CachedOutputStream bos = new CachedOutputStream();
			try {
				IOUtils.copy(is, bos);
				bos.flush();
				is.close();
				message.setContent(InputStream.class, bos.getInputStream());
				return bos.getInputStream().toString();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public MessageContentsList getMCL(Message message) {
		Object[] contents=null;
		try {
			contents = ReFelctionUtil.getDynamicObj(MessageImpl.class,
					"contents", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		MessageContentsList mcl;
		for (Object content : contents) {
			if (content instanceof MessageContentsList) {
				mcl = (MessageContentsList) content;
				return mcl;
			}
		}
		return null;
	}
}

 