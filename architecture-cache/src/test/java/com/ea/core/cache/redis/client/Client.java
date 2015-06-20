package com.ea.core.cache.redis.client;

import org.junit.Test;

import com.ea.core.cache.ICachePool;
import com.ea.core.cache.client.CacheClient;
import com.ea.core.cache.memcached.pool.MemcachedPool;
import com.ea.core.cache.redis.pool.RedisGeneralPool;

public class Client {

	@Test
	public void test(){ 
		ICachePool cachePool = null;
		cachePool = new MemcachedPool();
		cachePool.addServer("192.168.222.135:10240");
		CacheClient client = new CacheClient(cachePool);
		
		try {
			client.set("key1", "value4345", 1000);
			client.set("key2", "value4345", 1000);
			client.set("key10", "value4345", 1000);
			client.set("key", "value4345", 1000);
			client.set("kay", "value4345", 1000);
			
			System.out.println(client.get("key"));
			System.out.println(client.keys("*","key"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
