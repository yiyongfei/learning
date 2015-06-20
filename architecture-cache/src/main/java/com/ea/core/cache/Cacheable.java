package com.ea.core.cache;

public interface Cacheable {

	/**
	 * 用于生成应用缓存的Key
	 */
	public String generatorCacheKey();
}
