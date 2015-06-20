package com.ea.core.cache.memcached.commands;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.rubyeye.xmemcached.KeyIterator;
import net.rubyeye.xmemcached.MemcachedClient;

import com.ea.core.base.utils.RegexUtil;
import com.ea.core.cache.ICacheCommands;

/**
 * Memcached命令封装
 * Memcached的缓存失效策略是LRU（最近最少使用）加上到期失效策略。
 * 当向Memcached存储数据时，可能会设置一个过期时间，可以是永久也可以是一段时间。
 * 但是如果一旦给Memcached分配的内存使用完毕，则首先会替换掉已失效的数据，其次是最近最少使用的数据。
 * Memcached可以作为一级缓存，存放使用频率较高的数据，但如果数据项过大，超过1M，放Memcached就会失败。
 * 缓存时需要预先定好缓存策略，哪些数据放L1，哪些数据放L2，另外L2同时作为L1的备份缓存。
 * 
 * @author yiyongfei
 *
 */
public class MemcachedCommands implements ICacheCommands {
	private MemcachedClient commands;
	private List<InetSocketAddress> socketAddress;
	
	public MemcachedCommands(MemcachedClient commands, List<InetSocketAddress> socketAddress){
		this.commands = commands;
		this.socketAddress = socketAddress;
	}
	@Override
	public Boolean set(String key, String value, int seconds) throws Exception {
		// TODO Auto-generated method stub
		return commands.set(key, seconds, value);
	}

	@Override
	public Boolean add(String key, String value, int seconds) throws Exception {
		return commands.add(key, seconds, value);
	}
	
	@Override
	public Boolean replace(String key, String value, int seconds) throws Exception {
		return commands.replace(key, seconds, value);
	}
	
	@Override
	public Boolean expire(String key, int seconds) throws Exception {
		// TODO Auto-generated method stub
		return commands.touch(key, seconds);
	}

	/**
	 * 用正则表达式模拟了Redis的Keys方法的模式匹配
	 */
	@Override
	public Set<String> keys(String pattern) throws Exception {
		// TODO Auto-generated method stub
		Set<String> set = new HashSet<String>();
		for(InetSocketAddress address : socketAddress) {
			KeyIterator iterator = commands.getKeyIterator(address);
			while(iterator.hasNext()){
				set.add(iterator.next());
			}
		}
		if("*".equals(pattern)){
			return set;
		} else {
			String tmpPattern = pattern;
			tmpPattern = tmpPattern.replaceAll("\\?", "\\\\S\\?");
			tmpPattern = tmpPattern.replaceAll("\\*", "\\\\S\\*");
			return RegexUtil.matcher(tmpPattern, set);
		}
	}

	@Override
	public String get(String key) throws Exception {
		// TODO Auto-generated method stub
		return commands.get(key);
	}

	@Override
	public Boolean exists(String key) throws Exception {
		// TODO Auto-generated method stub
		String str = commands.get(key);
		return str != null ? true : false;
	}

	@Override
	public Boolean delete(String key) throws Exception {
		// TODO Auto-generated method stub
		return commands.delete(key);
	}

	@Override
	public Boolean append(String key, String value) throws Exception {
		// TODO Auto-generated method stub
		return commands.append(key, value);
	}

	@Override
	public void shutdown() throws Exception {
		// TODO Auto-generated method stub
		commands.shutdown();
	}
	@Override
	public Object getCommands() {
		// TODO Auto-generated method stub
		return this.commands;
	}

}
