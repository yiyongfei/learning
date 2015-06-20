package com.ea.core.integration.bridge;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.ea.core.integration.IntegrationConstant;
import com.ea.core.integration.IntegrationContext;

public abstract class RestIntegration {
	@Autowired
	private IntegrationContext integrationContext;
	
	public void connect(String host, String httpMethod, String method, Object... obj) throws Exception{
		Map<String, String> conf = new HashMap<String, String>();
		conf.put(IntegrationConstant.CONF.HOST.getCode(), host);
		conf.put(IntegrationConstant.CONF.HTTP_METHOD.getCode(), httpMethod);
		integrationContext.connect(IntegrationConstant.CONNECTOR_MODE.REST.getCode(), conf, method, null, obj);
	}
}
