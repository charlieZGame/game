package com.beimi.util.cache.hazelcast.impl;

import com.beimi.util.cache.CacheBean;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

@Service("ProxyGameRoomCache")
public class ProxyGameRoomCache implements CacheBean{
	


	private static Map<String,Object> hazelcastInstance = new HashMap<String,Object>();
	
	private String cacheName ; 
	
	public Map<String,Object> getInstance(){
		return hazelcastInstance ;
	}
	public CacheBean getCacheInstance(String cacheName){
		this.cacheName = cacheName ;
		return this ;
	}
	
	@Override
	public void put(String key, Object value, String orgi) {
		//getInstance().getMap(getName()).put(key, value) ;
		getInstance().put(key, value);
	}

	@Override
	public void clear(String orgi) {
		getInstance().clear();
	}

	@Override
	public Object delete(String key, String orgi) {
		return getInstance().remove(key) ;
	}

	@Override
	public void update(String key, String orgi, Object value) {
		getInstance().put(key, value);
	}

	@Override
	public Object getCacheObject(String key, String orgi) {
		return getInstance().get(key);
	}

	public String getName() {
		return cacheName ;
	}

//	@Override
	public void service() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<?> getAllCacheObject(String orgi) {
		return getInstance().keySet();
	}
	@Override
	public Object getCacheObject(String key, String orgi, Object defaultValue) {
		return getCacheObject(key, orgi);
	}
	@Override
	public Object getCache() {
		return getInstance();
	}
	
	@Override
	public Lock getLock(String lock , String orgi) {
		// TODO Auto-generated method stub
		//return getInstance().getLock(lock);
		return null;
	}
	@Override
	public long getSize() {
		return getInstance().size();
	}
	@Override
	public long getAtomicLong(String cacheName) {
		//return getInstance().incrementAndGet();
		return 1;
	}
	@Override
	public void setAtomicLong(String cacheName, long start) {
		//getInstance().getAtomicLong(getName()).set(start);
	}
}
