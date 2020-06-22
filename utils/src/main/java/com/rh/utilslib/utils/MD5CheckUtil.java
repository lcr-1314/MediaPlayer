package com.rh.utilslib.utils;


import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *@author  xu
 *@date 2017/12/2
 *@note md5文件校验
 **/

public class MD5CheckUtil {

    /**
     * 将字节数组转换为16进制字符串
     * @param byteArr
     * @return 16进制字符串
     */
    private static String byteArrToHex(byte[] byteArr) {
        // Initialize the character array, used to store each hexadecimal string
        char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        // Initialize a char Array, used to form the result string
        char[] resultCharArr = new char[byteArr.length*2];
        // Traverse the byte array, converted into characters in a character array
        int index = 0;
        for (byte b : byteArr) {
            resultCharArr[index++] = hexDigits[b>>> 4 & 0xf];
            resultCharArr[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArr);
    }

    /**
     * 获取字符串的MD5
     * @param input
     * @return
     */
    public static String getStringMD5(String input){
        try {
            // get MD5 digest
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            // The input String to Byte Array
            byte[] inputArr = input.getBytes();
            // Updates the digest using the specified byte.
            mDigest.update(inputArr);
            // Completes the hash computation by performing final operations such as padding.
            // The digest is reset after this call is made.
            byte[] resultArr = mDigest.digest();
            //
            return byteArrToHex(resultArr);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取文件的MD5，可以替换为SHA1
     */
    @SuppressWarnings("resource")
    public static String getFileMD5(String fileUrl) throws IOException {
        int bufferSize = 1024*1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        try {
            // 可以替换为"SHA1"
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(fileUrl);
            // Creates a digest input stream, using the specified input stream and message digest.
            digestInputStream = new DigestInputStream(fileInputStream, mDigest);
            byte[] buffer = new byte[bufferSize];
            while(digestInputStream.read(buffer)>0);
            mDigest = digestInputStream.getMessageDigest();
            byte[] resultArr = mDigest.digest();
            return byteArrToHex(resultArr);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }finally{
            fileInputStream.close();
            digestInputStream.close();
        }
        return null;
    }

    /**
     * 获取String的SHA1
     * @param input
     * @return
     */
    public static String getStringSHA1(String input){
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] inputArr = input.getBytes();
            mDigest.update(inputArr);
            byte[] resultArr = mDigest.digest();
            return byteArrToHex(resultArr);

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


}
