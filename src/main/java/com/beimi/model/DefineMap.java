package com.beimi.model;

import java.util.HashMap;

/**
 * Created by zhengchenglei on 2018/3/15.
 */
public class DefineMap<K,V>  extends HashMap{


    public DefineMap<K,V> put(K k,V v){
        super.put(k,v);
        return  this;
    }

}
