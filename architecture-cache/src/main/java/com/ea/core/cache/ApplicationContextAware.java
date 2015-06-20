package com.ea.core.cache;

import org.springframework.context.ApplicationContext;

public interface ApplicationContextAware extends org.springframework.context.ApplicationContextAware {
	public ApplicationContext getApplicationContext();
}
