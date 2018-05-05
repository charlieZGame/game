package com.beimi.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;

/**
 * Created by zhengchenglei on 2018/4/20.
 */
public class Base64Util {


    public static String baseEncode(String str)  {

        try {
            return new BASE64Encoder().encode(str.getBytes("UTF-8"));
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }


    public static String baseDencode(String str) {

        try {
            return new String(new BASE64Decoder().decodeBuffer(str),"UTF-8");
        } catch (IOException e) {
           throw new RuntimeException(e);
        }

    }


    public static void main(String[] args) throws Exception {
        System.out.println(baseEncode("ZCL"));
    //    System.out.println(baseDencode("R3Vlc3RfMUFkdGM0"));
     //   System.out.println(baseDencode(baseEncode("NXAybzVwK3o1TDZkNUw2ZA==,15#UjNWbGMzUmZNV3hGVVVsRw==,-5#UjNWbGMzUmZNR05qZEUwMQ==,-5#UjNWbGMzUmZNR2gwV1Zvdw==,-5")));
    }


}
