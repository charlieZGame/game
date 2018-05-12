package com.beimi.util.cache.hazelcast.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beimi.util.cache.CacheBean;
import com.hazelcast.core.HazelcastInstance;

@Service("online_cache")
public class OnlineCache implements CacheBean{
	
	//@Autowired
	//public HazelcastInstance hazelcastInstance;
	public static Map<String,Object> hazelcastInstance = new HashMap<String,Object>();
	
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
		getInstance().put(key, value) ;
	}

	@Override
	public void clear(String orgi) {
		getInstance().clear();
	}

	@Override
	public Object delete(String key, String orgi) {
		System.out.println("开始清理用户映射缓存 key:"+key+" value"+getInstance().get(key));
	/*	for(StackTraceElement element : Thread.currentThread().getStackTrace()) {
			System.out.println(element.getClassName()+" "+element.getMethodName()+" "+element.getLineNumber());
		}*/
		return getInstance().remove(key) ;
		//return getInstance().get(key);
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
		//return getInstance();
		return null;
	}
	@Override
	public long getSize() {
		return getInstance().size();
	}
	@Override
	public long getAtomicLong(String cacheName) {
	//	return getInstance().incrementAndGet();
		return 1;
	}
	@Override
	public void setAtomicLong(String cacheName, long start) {
		//getInstance().getAtomicLong(getName()).set(start);
	}
}
