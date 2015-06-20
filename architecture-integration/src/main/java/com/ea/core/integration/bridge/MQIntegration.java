package com.ea.core.integration.bridge;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.ea.core.integration.IntegrationConstant;
import com.ea.core.integration.IntegrationContext;

public abstract class MQIntegration {
	@Autowired
	private IntegrationContext integrationContext;
	
	public void connect(String host, String username, String password, String queueName, String facadeId, Object... obj) throws Exception{
		Map<String, String> conf = new HashMap<String, String>();
		conf.put(IntegrationConstant.CONF.HOST.getCode(), host);
		conf.put(IntegrationConstant.CONF.USERNAME.getCode(), username);
		conf.put(IntegrationConstant.CONF.PASSWORD.getCode(), password);
		integrationContext.connect(IntegrationConstant.CONNECTOR_MODE.MQ.getCode(), conf, queueName, facadeId, obj);
	}
}
