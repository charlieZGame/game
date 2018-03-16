package com.beimi.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhengchenglei on 2018/3/15.
 */
public class PlayCache {

    private final static Map<String,CacheObject> map = new ConcurrentHashMap<String,CacheObject>();
    private final static PlayCache cache = new PlayCache();

    public static void put(String key,Object value){
        map.put(key,cache.new CacheObject().setValue(value));
    }

    public static <T> T get(String key,Class<T> clazz){
        return (T) map.get(key).getValue();
    }

    public static void clear(String key){
        map.remove(key);
    }



    class CacheObject{

        private Object value;

        private boolean isNeedCheck;

        public Object getValue() {
            return value;
        }

        public CacheObject setValue(Object value) {
            this.value = value;
            return this;
        }

        public boolean isNeedCheck() {
            return isNeedCheck;
        }

        public void setNeedCheck(boolean needCheck) {
            isNeedCheck = needCheck;
        }
    }


}
