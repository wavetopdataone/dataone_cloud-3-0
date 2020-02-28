package com.cn.wavetop.dataone.util;

import com.cn.wavetop.dataone.entity.SysUser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {
    //日期倒叙排列
    public static Set<String> getOrderByDate(Set<String> set){
        Set<String> sortSet = new TreeSet<String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date d1 = df.parse(o1);
                    Date d2 = df.parse(o2);
                    if(d1.getTime()<d2.getTime()){
                        return 1;
                    }else if(d1.getTime()>d2.getTime()){
                        return -1;
                    }else{
                        return 0;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        sortSet.addAll(set);

        return sortSet;
    }

    public static String dateAdd(String dateStr, int num) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dateMaxDate = format.parse(dateStr);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(dateMaxDate);
            calendar.add(calendar.DATE, num);//把日期往后增加一天.整数往后推,负数往前移动
            dateStr = format.format(calendar.getTime());   //这个时间就是日期往后推一天的结果
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }
    public static String todate(String datetime) {
        String str;
        String[] time=datetime.split("-");
        Integer year_rual=Integer.parseInt(time[0]);
        Integer month_rual=Integer.parseInt(time[1]);
        Integer day_rual=Integer.parseInt(time[2]);
        if (judge(year_rual,month_rual,day_rual))
        {
             str=weekByDate(year_rual,month_rual,day_rual);
        }
        else
        {
            str="检测到您的输入不合法，请输入合法日期！";
        }
        return str;
    }

    /**
     * 判断输入的年月日是否为数字，或为空
     *
     * @param number //输入内容
     * @return boolean//返回值为true符合规则，返回值为false不符合规则
     */
    public static boolean isNumber(String number) {
        if (number == null || "".equals(number.trim())) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");

        Matcher isNum = pattern.matcher(number.trim());

        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 判断输入的年月日是否符合规则
     *
     * @param year  //年份
     * @param month // 月份
     * @param day   //天
     * @return boolean//返回值为true符合规则，返回值为false不符合规则
     */
    public static boolean judge(int year, int month, int day) {
        //当输入的数字小于零时，返回false
        if (year <= 0) {
            return false;
        }
        if (month <= 0 || month > 12) {
            return false;
        }
        if (day <= 0 || day > 31) {

            return false;
        }
        //年份能被4整除并且不能被100整除，或者能被400整除，则为闰年
        if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
            if (month == 2||month == 02) {
                //闰年的2月
                if (day > 29) {
                    return false;
                }
            }
        } else {
            //不是闰年的2月
            if (month == 2||month == 02) {
                if (day > 28) {
                    return false;
                }
            }
        }
        //1、3、5、7、8、10、12月份为31天
        int[] m1 = {1, 3, 5, 7, 8, 10, 12};

        for (int i = 0; i < m1.length; i++) {
            if (month == m1[i]) {
                if (day > 31) {
                    return false;
                }
            }
        }
        //4、6、9、12月份为30天
        int[] m2 = {4, 6, 9, 11};

        for (int j = 0; j < m2.length; j++) {
            if (month == m2[j]) {
                if (day > 30) {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * 根据年月日返回星期几
     *
     * @param year  //年份
     * @param month //月份
     * @param day   //天
     * @return String //返回值直接返回星期几
     */
    public static String weekByDate(int year, int month, int day) {
        String str = "";
        SimpleDateFormat fmt = new SimpleDateFormat("dd MM yyyy");

        Date d = null;
        try {
            d = fmt.parse(day + " " + month + " " + year);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();

        cal.setTime(d);

        int weekDay = cal.get(Calendar.DAY_OF_WEEK);

        switch (weekDay) {
            case 1:
                str = "星期日";
                break;
            case 2:
                str = "星期一";
                break;
            case 3:
                str = "星期二";
                break;
            case 4:
                str = "星期三";
                break;
            case 5:
                str = "星期四";
                break;
            case 6:
                str = "星期五";
                break;
            case 7:
                str = "星期六";
                break;
            default:
                break;
        }

        return str;
    }
    public static  Date StringToDate(String dateTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //         ParsePosition pos = new ParsePosition(0);
        Date strtodate = null;
        try {
            strtodate = formatter.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
       return strtodate;
    }

}
