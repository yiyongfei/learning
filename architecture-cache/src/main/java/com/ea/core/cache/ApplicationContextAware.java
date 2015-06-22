package com.ea.core.cache;

import org.springframework.context.ApplicationContext;

public interface ApplicationContextAware extends org.springframework.context.ApplicationContextAware {
	/**
	 * 得到上下文
	 * @return
	 */
	public ApplicationContext getApplicationContext();
	
}
