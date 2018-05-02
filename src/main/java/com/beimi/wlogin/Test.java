package com.beimi.wlogin;

import com.beimi.util.UKTools;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;

/**
 * Created by zhengchenglei on 2018/3/18.
 */
public class Test {


    public static void main(String[] args) throws IOException {


        System.out.println(UKTools.md5One("212LYJM#888888&6666$3333@END").toUpperCase());


    }


}
