package com.rh.utilslib.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author      yang yingwei
 * @description 排序工具，实现SortInterface接口，根据getSortField()返回的字段时行排序
 *              支持数字、英文、中文排序
 * @date        2018/1/18
 * @modify
 */

public class SortUtil {

    public static void sort(List<? extends SortInterface> list){
        try {
            Collections.sort(list, new Comparator<SortInterface>() {
                @Override
                public int compare(SortInterface o1, SortInterface o2) {
                    String s2 = getPingYin(o2.getSortField());
                    String s1 = getPingYin(o1.getSortField());

                    int s1Length = s1.length();
                    int s2Length = s2.length();

                    int min = s1Length > s2Length ? s2Length : s1Length;

                    for(int i = 0; i < min;i++){
                        if (s1.charAt(i) > s2.charAt(i)) {
                            return 1;
                        } else if (s1.charAt(i) < s2.charAt(i)) {
                            return -1;
                        }else {
                            continue;
                        }
                    }

                    if(s1Length == min && s2Length == min){
                        return 0;
                    }

                    if(s1Length > s2Length){
                        return 1;
                    }else if(s1Length < s2Length){
                        return -1;
                    }else {
                        return 0;
                    }
                }
            });
            Collections.sort(list, new Comparator<SortInterface>() {
                @Override
                public int compare(SortInterface o1, SortInterface o2) {
                    String s2 = o2.getSortField();//getPingYin(o2.getSortField());
                    String s1 = o1.getSortField();//getPingYin(o1.getSortField());
                    if (s1.length() == s2.length())
                        return 0;
                    else if (s1.length() > s2.length())
                        return 1;
                    return -1;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.d("SortUtil","sort(List<? extends SortInterface> list) 排序发生异常");
        }
    }

    public static void sort(ArrayList<String> list){
        try {
            Collections.sort(list, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    String s2 = getPingYin(o2);
                    String s1 = getPingYin(o1);

                    int s1Length = s1.length();
                    int s2Length = s2.length();

                    int min = s1Length > s2Length ? s2Length : s1Length;

                    for(int i = 0; i < min;i++){
                        if (s1.charAt(i) > s2.charAt(i)) {
                            return 1;
                        } else if (s1.charAt(i) < s2.charAt(i)) {
                            return -1;
                        }else {
                            continue;
                        }
                    }

                    if(s1Length == min && s2Length == min){
                        return 0;
                    }

                    if(s1Length > s2Length){
                        return 1;
                    }else if(s1Length < s2Length){
                        return -1;
                    }else {
                        return 0;
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.d("SortUtil","sort(ArrayList<String> list) 排序发生异常");
        }
    }


    public static String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        String output = "";
        if (inputString != null && inputString.length() > 0
                && !"null".equals(inputString)) {
            char[] input = inputString.trim().toCharArray();
            try {
                for (int i = 0; i < input.length; i++) {
                    if (java.lang.Character.toString(input[i]).matches(
                            "[\\u4E00-\\u9FA5]+")) {
                        String[] temp = PinyinHelper.toHanyuPinyinStringArray(
                                input[i], format);
                        output += temp[0];
                    } else
                        output += java.lang.Character.toString(input[i]);
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        } else {
            return "*";
        }
        return output;
    }

    public interface SortInterface{
        String getSortField();
    }


}
