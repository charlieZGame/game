package com.beimi.backManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
/**
 * Created by zhengchenglei on 2018/4/7.
 */
public class SessionMapping extends HashMap{

    private static Map<String,Session> map = new ConcurrentHashMap<String,Session>();


    public static void put(String key,Session session){
        if(map == null){
            map = new ConcurrentHashMap<String,Session>();
        }
        map.put(key,session);
        map.put(key,map.get(key).setTimestemp(System.currentTimeMillis()));
    }


    public static Session get(String key){
        if(map == null){
            map = new ConcurrentHashMap<String,Session>();
        }
        Session session = map.get(key);
        if(session != null){
            session.setTimestemp(System.currentTimeMillis());
        }
        return map.get(key);
    }

    public static String getOpenId(String key){
        if(map == null){
            map = new ConcurrentHashMap<String,Session>();
        }
        Session session = map.get(key);
        if(session != null){
            session.setTimestemp(System.currentTimeMillis());
        }
        return map.get(key).getOpenId();
    }

    public static boolean containsKey(String key){
        if(map == null){
            map = new ConcurrentHashMap<String,Session>();
            return false;
        }
        return map.containsKey(key);
    }

    public static void main(String[] args) {
        Session session = new Session();
        put(null,session);
    }


    static{

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                List<String> temp = new ArrayList<String>();
                for(Entry<String,Session> entry : map.entrySet()){
                    if(startTime - entry.getValue().getTimestemp() > 300000){
                        temp.add(entry.getKey());
                    }
                }

                for(String key : temp){
                    map.remove(key);
                }
            }
        },0,300000);




    }



}
