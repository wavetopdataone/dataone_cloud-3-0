package com.cn.wavetop.dataone.util;

public class MappingFieldUtil {

    /**
     * oracle到dm需要加上长度的类型 《《长度一样的放成一个数组》》
     */
    public static String[] Types = {
            "VARCHAR2"
    };
    public static String[] NumberTypes = {
            "NUMBER"
    };
    /**
     * 判断是否可以变长类型 长度上限的是4000
     * 包含返回true，否则false
     *
     * @param type
     * @return
     */
    public static boolean equalsVarcharType(String type,Integer length) {
        for (int i = 0; i < Types.length; i++) {
            if (Types[i].equals(type)&&length+10<=4000) {
                return true;
            }
        }
        return false;
    }
    /**
     * 判断是否可以变长类型 长度上限的是38
     * 包含返回true，否则false
     *
     * @param type
     * @return
     */
    public static boolean equalsNumberType(String type,Integer length) {
        for (int i = 0; i < NumberTypes.length; i++) {
            if (NumberTypes[i].equals(type)&&length+10<=38) {
                return true;
            }
        }
        return false;
    }

    public static boolean VarcharOrNumber(String type,Integer length){
        if(equalsVarcharType(type,length)||equalsNumberType(type,length)){
            return true;
        }
        return false;
    }

}
