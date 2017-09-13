/**
 * Copyright 1999-2014 dangdang.com.
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
package com.alibaba.dubbo.rpc.protocol.rest;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.remoting.http.servlet.BootstrapListener;
import com.alibaba.dubbo.remoting.http.servlet.ServletManager;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.support.DubboInvokeDetail;
import com.alibaba.dubbo.rpc.support.KafkaDubboUtil;

/**
 * @author lishen
 */
public class DubboHttpServer extends BaseRestServer {

    private final HttpServletDispatcher dispatcher = new HttpServletDispatcher();
    private final ResteasyDeployment deployment = new ResteasyDeployment();
    private HttpBinder httpBinder;
    private HttpServer httpServer;
	private static ThreadLocal<DubboInvokeDetail> th=new ThreadLocal<DubboInvokeDetail>();

//    private boolean isExternalServer;

    public static ThreadLocal<DubboInvokeDetail> getTh() {
		return th;
	}

	public DubboHttpServer(HttpBinder httpBinder) {
        this.httpBinder = httpBinder;
    }

    protected void doStart(URL url) {
        // TODO jetty will by default enable keepAlive so the xml config has no effect now
        httpServer = httpBinder.bind(url, new RestHandler());

        ServletContext servletContext = ServletManager.getInstance().getServletContext(url.getPort());
        if (servletContext == null) {
            servletContext = ServletManager.getInstance().getServletContext(ServletManager.EXTERNAL_SERVER_PORT);
        }
        if (servletContext == null) {
            throw new RpcException("No servlet context found. If you are using server='servlet', " +
                    "make sure that you've configured " + BootstrapListener.class.getName() + " in web.xml");
        }

        servletContext.setAttribute(ResteasyDeployment.class.getName(), deployment);

        try {
            dispatcher.init(new SimpleServletConfig(servletContext));
        } catch (ServletException e) {
            throw new RpcException(e);
        }
    }

    public void stop() {
        httpServer.close();
    }

    protected ResteasyDeployment getDeployment() {
        return deployment;
    }

    private class RestHandler implements HttpHandler {

        public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
            try {
				dispatcher.service(request, response);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
            DubboInvokeDetail did=DubboHttpServer.getTh().get();
            if(did!=null){
            		try {
						KafkaDubboUtil.getLogSender().sendMsg(did);
					} catch (Exception e) {
						e.printStackTrace();
					}
                DubboHttpServer.getTh().remove();
            }
        }
    }

    private static class SimpleServletConfig implements ServletConfig {

        private final ServletContext servletContext;

        public SimpleServletConfig(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        public String getServletName() {
            return "DispatcherServlet";
        }

        public ServletContext getServletContext() {
            return servletContext;
        }

        public String getInitParameter(String s) {
            return null;
        }

        public Enumeration getInitParameterNames() {
            return new Enumeration() {
                public boolean hasMoreElements() {
                    return false;
                }

                public Object nextElement() {
                    return null;
                }
            };
        }
    }
}