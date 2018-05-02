package com.beimi.rule;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public enum  HuType {

    SL("SL","少龙"),
    LL("LL","老龙"),
    LLL("LLL","老老龙"),
    KZ("KZ","坎子"),
    LZ("LZ","篓子");

    private String key;

    private String value;

    HuType(String key,String value){
        this.key = key;
        this.value = value;
    }
}
