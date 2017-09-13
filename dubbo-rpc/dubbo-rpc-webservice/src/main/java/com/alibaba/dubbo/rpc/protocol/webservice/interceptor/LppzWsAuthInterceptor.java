package com.alibaba.dubbo.rpc.protocol.webservice.interceptor;

import java.util.HashMap;
import java.util.Map;
import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.common.security.TokenType;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.JAASLoginInterceptor;
import org.apache.cxf.message.Message;

public class LppzWsAuthInterceptor extends JAASLoginInterceptor{
	private static Map<String,AuthModel> mapAuth=new HashMap<String,AuthModel>();
	public static Map<String, AuthModel> getMapAuth() {
		return mapAuth;
	}
	@Override
	public void handleMessage(Message message) throws Fault {
		String wsdl=message.getExchange().get("javax.xml.ws.wsdl.description").toString();
		String name = null;
		     String password = null;
		     AuthorizationPolicy policy = (AuthorizationPolicy)message.get(AuthorizationPolicy.class);
		     if (policy != null) {
		       name = policy.getUserName();
		       password = policy.getPassword();
		     }
		     else {
		       SecurityToken token = (SecurityToken)message.get(SecurityToken.class);
		       if ((token != null) && (token.getTokenType() == TokenType.UsernameToken)) {
		         UsernameToken ut = (UsernameToken)token;
		         name = ut.getName();
		         password = ut.getPassword();
		       }
		     }
	     if ((name == null) || (password == null)) {
	       throw new IllegalStateException("for "+wsdl+"username and passwd can not be null!");
	     }
		AuthModel auth=mapAuth.get(wsdl);
		if(name.equals(auth.getUsrname())&&password.equals(auth.getPasswd()))
			return;
		throw new IllegalStateException("for "+wsdl+" "+name+"/"+password+" has no access!");
	}
}
