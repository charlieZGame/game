package com.beimi.wlogin;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;

/**
 * Created by zhengchenglei on 2018/3/18.
 */
public class Test {


    public static void main(String[] args) throws IOException {

         byte[] str = new BASE64Decoder().decodeBuffer("5O32BhoiLjdBSElTWQ==");

        System.out.println(str);


    }


}
