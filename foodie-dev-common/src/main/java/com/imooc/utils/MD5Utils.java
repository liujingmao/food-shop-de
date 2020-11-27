package com.imooc.utils;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;

/**
 * Created by liujingmao on 2019-11-10.
 */
public class MD5Utils {
    public static String getMD5Str(String str) throws Exception{
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String newstring= Base64.encodeBase64String(md5.digest(str.getBytes()));
        return newstring;
    }

}


