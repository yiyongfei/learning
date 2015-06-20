package com.ea.core.integration.bridge;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.ea.core.integration.IntegrationConstant;
import com.ea.core.integration.IntegrationContext;

public abstract class SoapIntegration {
	@Autowired
	private IntegrationContext integrationContext;
	
	public void connect(String host, String method, Object... obj) throws Exception{
		Map<String, String> conf = new HashMap<String, String>();
		if(host.endsWith("?wsdl")){
			conf.put(IntegrationConstant.CONF.HOST.getCode(), host);
		} else {
			conf.put(IntegrationConstant.CONF.HOST.getCode(), host + "?wsdl");
		}
		integrationContext.connect(IntegrationConstant.CONNECTOR_MODE.SOAP.getCode(), conf, method, null, obj);
	}
}
