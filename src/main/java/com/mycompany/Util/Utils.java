package com.mycompany.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Utils {

    //传入文本内容，返回 SHA-256 串
    public static String SHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    //传入文本内容，返回 SHA-512 串
    public static String SHA512(final String strText) {
        return SHA(strText, "SHA-512");
    }

    //md5加密
    public static String SHAMD5(String strText) {
        return SHA(strText, "MD5");
    }

    //字符串 SHA 加密
    private static String SHA(final String strText, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(strText.getBytes());
                // 得到 byte 類型结果
                byte[] byteBuffer = messageDigest.digest();

                // 將 byte 轉換爲 string
                StringBuilder strHexString = new StringBuilder();
                // 遍歷 byte buffer
                for (byte b : byteBuffer) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }

    //把list字符串转成字符串数组
    public static String[] stringListToStringArray(List<String> list){
        if(list == null) return null;
        int length = list.size();
        String[] array = new String[length];
        for(int i=0;i<length;i++) {
            String[] items = list.get(i).split(" ");
            StringBuilder buffer = new StringBuilder();
            for(String string:items) buffer.append(string);
            array[i] = buffer.toString();
        }
        return array;
    }

    //判断字符串中是否包含中文字符
    public static boolean isContainZh(String selectedString){
        return selectedString.matches(".*[\u4E00-\u9FA5].*");
    }
}
