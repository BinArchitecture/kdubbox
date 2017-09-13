/**
 * Copyright 2006-2015 handu.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.support;

import java.io.Serializable;

/**
 * Dubbo Invoke Base Entity
 *
 *         Created on 15/6/29.
 */
public class BaseDubboInvoke implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3665585438437728912L;
	
	protected String protocol;
	protected String id;
	protected String service;
	protected String method;
	protected String consumer;
	protected String provider;
	protected String type;
	protected String invokeDateTime;
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
    public String getInvokeDateTime() {
		return invokeDateTime;
	}

	public void setInvokeDateTime(String invokeDateTime) {
		this.invokeDateTime = invokeDateTime;
	}
	
	 public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public String getConsumer() {
			return consumer;
		}

		public void setConsumer(String consumer) {
			this.consumer = consumer;
		}

		public String getProvider() {
			return provider;
		}

		public void setProvider(String provider) {
			this.provider = provider;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
}
