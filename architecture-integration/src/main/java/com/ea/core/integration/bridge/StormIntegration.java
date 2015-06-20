package com.ea.core.integration.bridge;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.ea.core.integration.IntegrationConstant;
import com.ea.core.integration.IntegrationContext;

public abstract class StormIntegration {
	@Autowired
	private IntegrationContext integrationContext;
	
	public void connect(String host, String port, String timeout, String topologyName, String facadeId, Object... obj) throws Exception{
		Map<String, String> conf = new HashMap<String, String>();
		conf.put(IntegrationConstant.CONF.HOST.getCode(), host);
		conf.put(IntegrationConstant.CONF.PORT.getCode(), port);
		conf.put(IntegrationConstant.CONF.TIMEOUT.getCode(), timeout);
		integrationContext.connect(IntegrationConstant.CONNECTOR_MODE.STORM.getCode(), conf, topologyName, facadeId, obj);
	}
}
