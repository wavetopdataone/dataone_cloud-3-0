package com.cn.wavetop.dataone.db;

/**
 * 字段类型判断
 */
public class DataStructure {

    /**
     * DM不能携带长度的类型
     */
    public static String[]Types={
        "DATE"
    };
    /**
     * DM需要携带精度的类型
     */
    public static String[]AccuracyTypes={
            "NUMBER"
    };

    /**
     * 判断是否包含不能携带长度的类型
     * 包含返回true，否则false
     * @param type
     * @return
     */
    public static boolean equalsType(String type){
        for (int i = 0; i <Types.length ; i++) {
            if(Types[i].equals(type)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * 判断是否包含能携带精度的类型
     * 包含返回true，否则false
     * @param type
     * @return
     */
    public static boolean equalsAccuracyTypes(String type){
        for (int i = 0; i <AccuracyTypes.length ; i++) {
            if(AccuracyTypes[i].equals(type)){
                return true;
            }
        }
        return false;
    }


}
