
package com.alibaba.dubbo.rpc.support;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DubboInvokeDetail extends BaseDubboInvoke {
	private static final long serialVersionUID = 7665585438437728912L;
    private String exception;
    private String exceptionClazz;
	private String hostUri;
    private String requestBody;
    private String responseBody;
	private String httpMethodType;
//	private String relatedId;
    
    public String getHttpMethodType() {
		return httpMethodType;
	}
	public void setHttpMethodType(String httpMethodType) {
		this.httpMethodType = httpMethodType;
	}
	public DubboInvokeDetail(){
    		this.invokeDateTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		.format(new Date());
    }
    public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	public String getHostUri() {
		return hostUri;
	}
	public void setHostUri(String hostUri) {
		this.hostUri = hostUri;
	}
	public String getRequestBody() {
		return requestBody;
	}
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}
	public String getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	public String getExceptionClazz() {
		return exceptionClazz;
	}
	public void setExceptionClazz(String exceptionClazz) {
		this.exceptionClazz = exceptionClazz;
	}
//	public String getRelatedId() {
//		return relatedId;
//	}
//	public void setRelatedId(String relatedId) {
//		this.relatedId = relatedId;
//	}
}
