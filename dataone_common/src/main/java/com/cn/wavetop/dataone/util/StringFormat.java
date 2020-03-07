package com.cn.wavetop.dataone.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class StringFormat {

//	public static void main(String[] args) {
//		char c = '��';
//		System.err.println((int)(c));
//	}

    public static String format(String str){
        if(str == null)
            return "";
        else
            return str.trim();
    }

    public static String format(Object object)
    {
        if(object==null)
            return "";
        else
            return(object.toString());
    }

    public static String stringToHtml(String plainstring)
    {
        StringBuffer temp=new StringBuffer();
        if(plainstring==null)return "";
        else{

            for(int i=0;i<plainstring.length();i++)
            {
                char c=plainstring.charAt(i);
                if(c=='\n')temp.append("<br>");
                else if(c==' ')temp.append("&nbsp");
                else temp.append(c);
            }
        }
        return temp.toString();
    }

    public static boolean isNullOrBlank(String s){
        if(s == null || "".equals(s.trim()))
            return true;
        return false;
    }

    public static String zero2blank(float input){
        String result="";
        DecimalFormat df = new DecimalFormat("#0.00");
        if(input!=0)result=df.format(input);
        return result;
    }

    public static String zero2blank(double input){
        String result="";
        DecimalFormat df = new DecimalFormat("#0.00");
        if(input!=0)result=df.format(input);
        return result;
    }

    public static String zero2blank2(float input){
        String result="&nbsp&nbsp";
        DecimalFormat df = new DecimalFormat("#0.00");
        if(input!=0)result=df.format(input);
        return result;
    }

    public static String zero2blank2(double input){
        String result="&nbsp&nbsp";
        DecimalFormat df = new DecimalFormat("#0.00");
        if(input!=0)result=df.format(input);
        return result;
    }

    public static String numberFormat(float input)
    {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(input);
    }

    public static String numberFormat(double input)
    {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(input);
    }

    public static String formatDate(String _date){
        if(_date==null)return "";
        else
            return _date.substring(0,16);
    }

    public static String[] formatTitle(String[] strArr,int length,int height){
        List list = new ArrayList();
        int hei = 0;
        int i = 0;
        for(i=0;i<strArr.length;i++){
            hei += getHeight(strArr[i],length);
            System.err.println(hei);
            if(hei > height)
                break;
            list.add(strArr[i]);
        }

        String[] arr = new String[list.size()];
        list.toArray(arr);
        return arr;
    }

    private static int getHeight(String str,int length){
        int len = 0;
        for(int i=0;i<str.length();i++){
            char c = str.charAt(i);
            int p = c & 0xFF00;
            if(p > 0)
                len += 2;
            else
                len += 1;
        }
        int height = (len + length - 1)/length;
        return height;
    }

    public static String getString(String str,int length){
        int len = 0;
        for(int i=0;i<str.length();i++){
            char c = str.charAt(i);
            int p = c & 0xFF00;
            if(p > 0)
                len += 2;
            else
                len += 1;

            if(len > length){
                str = str.substring(0,i);
            }
        }

        return str;
    }

    public static String formatWeekplan(String str,int baseLen){
        String[] names = str.split(" ");
        StringBuffer sb = new StringBuffer();
        int len = 0;

        for(int i=0;i<names.length;i++){
            int nameLen = getLength(names[i] + " ");
            System.err.println("len = " + len);
            System.err.println("nameLen = " + nameLen);
            if(len + nameLen > baseLen && i > 0){
                sb.append("<br>");
                len = nameLen % baseLen;
            }else{
                len = (len + nameLen)%baseLen;
            }
            sb.append(names[i] + " ");
        }
        return new String(sb);
    }

    public static String formatWeekplan2(String str,int len){
        if(str == null)
            return "";
        StringBuffer sb = new StringBuffer();
        int length = 0;

        for(int i=0;i<str.length();i++){
            char c = str.charAt(i);
            int p = c & 0xFF00;
            if(p > 0)
                length += 2;
            else
                length += 1;
            if(length > len){
                sb.append("<br>");
                length = length%len;
            }
            sb.append(c);
        }
        return new String(sb);
    }

    public static String formatMeeting(String str,int baseLen){
        String[] names = str.split(" ");
        StringBuffer sb = new StringBuffer();
        int len = 0;

        for(int i=0;i<names.length;i++){
            int nameLen = getLength(names[i] + " ");
            //System.err.println("len = " + len);
            //System.err.println("nameLen = " + nameLen);
            if(len + nameLen > baseLen && i > 0){
                sb.append("\r\n");
                len = nameLen % baseLen;
            }else{
                len = (len + nameLen)%baseLen;
            }
            sb.append(names[i] + " ");
        }
        return new String(sb);
    }

    private static int getLength(String str){
        int len = 0;
        for(int i=0;i<str.length();i++){
            char c = str.charAt(i);
            int p = c & 0xFF00;
            if(p > 0)
                len += 2;
            else
                len += 1;
        }

        return len;
    }

    public static String formatNumber(Double f){
        if(f == null){
            return "0.00";
        }

        NumberFormat nf = new DecimalFormat("0.00");
        return nf.format(f.doubleValue());
    }

    public static String fillChar(String str,int len,char c){
        StringBuffer ret = new StringBuffer();
        for(int i=0;i<len - str.length();i++){
            ret.append(c);
        }
        ret.append(str);
        return ret.toString();
    }

}
