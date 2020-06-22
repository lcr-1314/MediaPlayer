package com.rh.utilslib.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Base64;


/**
 * @author xu
 * @date 2017/11/29
 * @note 使用Base64 将扫码密码简单加密
 **/
public class Base64Utils {


    // 加密
    public static String encryption(String str) {
            byte[] encode = Base64.encode(str.getBytes(), Base64.NO_WRAP);
        return new String(encode);
    }

    // 解密
    public static String decrypt(String s) {
        byte[] decode =Base64.decode(s, android.util.Base64.NO_WRAP);
        return new String(decode);
    }

    /**
     * 散列MD5加密
     *
     * @param input
     * @param isUpperCase 是否大写
     * @return
     */
    public static String MD5(String input, Boolean isUpperCase) {
        return MD5(input.getBytes(), isUpperCase);
    }

    /**
     * 散列MD5加密
     *
     * @param input
     * @param isUpperCase 是否大写
     * @return
     */
    public static String MD5(byte[] input, Boolean isUpperCase) {
        String output = "";
        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(input);
            // 获得密文
            byte[] md = mdInst.digest();
            output = parseByte2HexStr(md, isUpperCase);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * 将二进制转换成16进制字符串表示
     *
     * @param input       输入字节数组
     * @param isUpperCase 是否大写
     * @return
     */
    public static String parseByte2HexStr(byte[] input, Boolean isUpperCase) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length; i++) {
            String hex = Integer.toHexString(input[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(isUpperCase ? hex.toUpperCase() : hex);
        }
        return sb.toString();
    }
}
