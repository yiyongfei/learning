/**
 * Copyright 2014 伊永飞
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ea.core.integration;

import java.net.URL;
import java.util.Calendar;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ea.core.bridge.AbstractConnector;
import com.ea.core.bridge.AbstractWSConnector;
import com.ea.core.bridge.IClient;
import com.ea.core.bridge.IConnector;
import com.ea.core.bridge.IWSClient;
import com.ea.core.bridge.async.mq.client.MqSendClient;
import com.ea.core.bridge.sync.storm.client.RemoteClient;
import com.ea.core.bridge.ws.rest.client.DeleteClient;
import com.ea.core.bridge.ws.rest.client.GetClient;
import com.ea.core.bridge.ws.rest.client.PostClient;
import com.ea.core.bridge.ws.rest.client.PutClient;
import com.ea.core.bridge.ws.soap.client.SoapClient;
import com.ea.core.cache.CacheContext;
import com.ea.core.integration.mq.MqConnector;
import com.ea.core.integration.rest.RestConnector;
import com.ea.core.integration.soap.SoapConnector;
import com.ea.core.integration.storm.StormConnector;

@Component
public class IntegrationContext {
	private Logger logger = LoggerFactory.getLogger(IntegrationContext.class);
	
	@Autowired
	private CacheContext cacheContext;
	
	public Object connect(String connectMode, Map<String, String> conf, String connectorId, String facadeId, Object... models) throws Exception {
		// TODO Auto-generated method stub
		long startTime = Calendar.getInstance().getTimeInMillis();
		IConnector connector = null;
		if(connectMode.equals(IntegrationConstant.CONNECTOR_MODE.MQ.getCode())){
			IClient client = new MqSendClient(conf.get(IntegrationConstant.CONF.HOST.getCode()), 
								conf.get(IntegrationConstant.CONF.USERNAME.getCode()),
								conf.get(IntegrationConstant.CONF.PASSWORD.getCode()));
			connector = new MqConnector();
			((AbstractConnector)connector).setClient(client);
		}
		else if(connectMode.equals(IntegrationConstant.CONNECTOR_MODE.STORM.getCode())){
			//基于Host、Port缓存Client
			//CacheKey:INTEGRATION_STORM_HOST:PORT
			//缓存时效3小时
			String cacheKey = INTEGRATION + "_" + STORM + "_"
					+ conf.get(IntegrationConstant.CONF.HOST.getCode()) + ":" + conf.get(IntegrationConstant.CONF.PORT.getCode());
			IClient client = (IClient) fromCache(cacheKey);
			if (client == null) {
				client = new RemoteClient(
						conf.get(IntegrationConstant.CONF.HOST.getCode()),
						new Integer(conf.get(IntegrationConstant.CONF.PORT.getCode())), 
						new Integer(conf.get(IntegrationConstant.CONF.TIMEOUT.getCode())));
				toCache(cacheKey, client, 60 * 60 * 3);
			}
			
			connector = new StormConnector();
			((AbstractConnector)connector).setClient(client);
		}
		else if(connectMode.equals(IntegrationConstant.CONNECTOR_MODE.SOAP.getCode())){
			//基于HostURL缓存Client
			//CacheKey:INTEGRATION_SOAP_HOST
			//缓存时效3小时
			String cacheKey = INTEGRATION + "_" + SOAP + "_" + conf.get(IntegrationConstant.CONF.HOST.getCode());
			IWSClient client = (IWSClient) fromCache(cacheKey);
			if (client == null) {
				client = new SoapClient(new URL(conf.get(IntegrationConstant.CONF.HOST.getCode())));
				toCache(cacheKey, client, 60 * 60 * 3);
			}
			connector = new SoapConnector();
			((AbstractWSConnector)connector).setClient(client);
		}
		else {
			////基于HttpMethod、HostURL缓存Client
			//CacheKey:INTEGRATION_REST_METHOD_HOST
			//缓存时效3小时
			String cacheKey = INTEGRATION + "_" + REST + "_"
					+ conf.get(IntegrationConstant.CONF.HTTP_METHOD.getCode()) + "_" + conf.get(IntegrationConstant.CONF.HOST.getCode());
			IWSClient client = (IWSClient) fromCache(cacheKey);
			
			if (client == null) {
				if(HttpMethod.GET.equals(conf.get(IntegrationConstant.CONF.HTTP_METHOD.getCode()))){
					client = new GetClient(new URL(conf.get(IntegrationConstant.CONF.HOST.getCode())));
				} else if(HttpMethod.POST.equals(conf.get(IntegrationConstant.CONF.HTTP_METHOD.getCode()))){
					client = new PostClient(new URL(conf.get(IntegrationConstant.CONF.HOST.getCode())));
				} else if(HttpMethod.PUT.equals(conf.get(IntegrationConstant.CONF.HTTP_METHOD.getCode()))){
					client = new PutClient(new URL(conf.get(IntegrationConstant.CONF.HOST.getCode())));
				} else if(HttpMethod.DELETE.equals(conf.get(IntegrationConstant.CONF.HTTP_METHOD.getCode()))){
					client = new DeleteClient(new URL(conf.get(IntegrationConstant.CONF.HOST.getCode())));
				} else {
					throw new RuntimeException("提供的HttpMethod方法有误，请检查！");
				}
				
				toCache(cacheKey, client, 60 * 60 * 3);
			}
			
			connector = new RestConnector();
			((AbstractWSConnector)connector).setClient(client);
		}
		long endTime1 = Calendar.getInstance().getTimeInMillis();
		logger.info("初始化连接共耗时" + (endTime1 - startTime) + "毫秒"); 
		Object result = connector.connect(connectorId, facadeId, models);
		long endTime2 = Calendar.getInstance().getTimeInMillis();
		logger.info("本次执行共耗时" + (endTime2 - startTime) + "毫秒，排除初始化连接后的耗时" + (endTime2 - endTime1) + "毫秒，连接方式：" + connectMode + "，调用Facade：" + facadeId); 
		return result;
	}

	private Object fromCache(String cacheKey) throws Exception{
		if(cacheContext != null){
			return cacheContext.get(cacheKey);
		} else {
			return null;
		}
	}
	
	private void toCache(String cacheKey, Object client, int times) throws Exception{
		if(cacheContext != null){
			cacheContext.set(cacheKey, client, times);
		}
	}
	
	private final String INTEGRATION = "INTEGRATION";
	private final String STORM = "STORM";
	private final String SOAP = "SOAP";
	private final String REST = "REST";
	
}
