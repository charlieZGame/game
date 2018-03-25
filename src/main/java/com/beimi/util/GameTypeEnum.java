package com.beimi.util;

/**
 * Created by zhengchenglei on 2018/3/23.
 */
public enum GameTypeEnum {


    LAIYUAN_HUN("laiYuanHun","涞源混子打法"),
    LAIYUAN_KOU("laiYuanKou","涞源混子打法"),
    SICHUAN("sichuanmajiang","四川麻将");

    private String key;

    private String value;

    GameTypeEnum(String key,String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
