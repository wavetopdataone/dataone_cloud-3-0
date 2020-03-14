package com.cn.wavetop.dataone.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

    static final Log log = LogFactory.getLog(DateUtil.class);

    /**
     * ��ʽ�����ڣ�ת�ɲ���ʱ������ַ���
     */
    public static String format(Date date) {
        if (date == null) {

            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    /**
     * ��ʽ��ʱ�䣬ת�ɴ�ʱ������ַ���
     *
     * @param date
     *            ���ڶ���
     * @return
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return formatter.format(date);
    }

    public static String formatTime2(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
    public static String formatTime4(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return formatter.format(date);
    }

    public static String formatTime3(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(date);
    }

    public static String formatMsgTime(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("M��d��H��m��");
        return formatter.format(date);
    }

    /**
     * �����ַ�����ת����java.util.Date����
     *
     * @param strDate
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String strDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = formatter.parse(strDate);
        return date;
    }

    /**
     * �����ַ�����ת����java.util.Date����
     *
     * @param strDate
     * @return
     * @throws ParseException
     */
    public static Date parseTime(String strDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = formatter.parse(strDate);
        return date;
    }

    /**
     * ��ǰʱ�䣬��ʱ���֡���
     *
     * @return ��ǰʱ�䣬�ַ��� 2005-11-30 13:20:34
     * @throws Exception
     */
//    public static String current() throws Exception {
//        String[][] arr = DBUtil.query("select sysdate from dual",2);
//
//        return arr[0][0];
//    }
//
    /**
     * ��������ڣ�����ʱ���֡��� 2005-11-30
     *
     * @return ���������
     * @throws Exception
     */
//    public static String today() throws Exception {
//        String[][] arr = DBUtil.query("select sysdate from dual",0);
//        return arr[0][0];
//    }

    /**
     * ��ǰʱ�䣬��ʱ����
     *
     * @return ��ǰʱ�䣬��java.util.Date����
     * @throws Exception
     */
//    public static Date currentObj() throws Exception {
//        String[][] arr = DBUtil.query("select sysdate from dual",2);
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date = formatter.parse(arr[0][0]);
//
//        return date;
//    }

    public static String to_date(String sDate) {
        if (sDate == null || sDate.trim().equals(""))
            return null;
        else
            return "to_date('" + sDate + "','yyyy-mm-dd hh24:mi:ss')";
    }

    public static boolean isDiffer(int differ, Calendar date1, Calendar date2) {
        boolean result = false;
        date1.add(Calendar.DATE, differ);
        if (date1.before(date2))
            result = true;
        return result;
    }

    public static String[] workplanDate(String strDate) throws Exception {
        String[] dates = new String[4];
        dates[0] = strDate.substring(0, 10);
        String time = strDate.substring(11);
        dates[3]=time;
        if ("13:00".compareTo(time)>0) {
            dates[2] = "����";
        } else {
            dates[2] = "����";
        }
        Date d = parseDate(dates[0]);
        int day = d.getDay();
        switch (day) {
            case 0:
                dates[1] = "������";
                break;
            case 1:
                dates[1] = "����һ";
                break;
            case 2:
                dates[1] = "���ڶ�";
                break;
            case 3:
                dates[1] = "������";
                break;
            case 4:
                dates[1] = "������";
                break;
            case 5:
                dates[1] = "������";
                break;
            case 6:
                dates[1] = "������";
                break;
        }
        return dates;
    }

//    public static void main(String[] args) throws Exception{
//
//        System.out.println(today());
//
//    }

    static String[] wks = {"����һ","���ڶ�","������","������","������","������","������"};

    public static String[][] getWeeks(Date d){
        String[][] weeks = new String[7][2];
        int day = d.getDay();
        if(day == 0) day = 7;
        for(int i=1-day,k = 0;i<=7-day;i++,k++){
            Date d2 = new Date(d.getTime());
            d2.setDate(d.getDate() + i);
            weeks[k][0] = format(d2);
            weeks[k][1] = wks[k];
        }
        return weeks;
    }

    public static String[][] getLastWeeks(Date d){
        d.setDate(d.getDate() - 7);
        String[][] weeks = new String[7][2];
        int day = d.getDay();
        if(day == 0) day = 7;
        for(int i=1-day,k=0;i<=7-day;i++,k++){
            Date d2 = new Date(d.getTime());
            d2.setDate(d.getDate() + i);
            weeks[k][0] = format(d2);
            weeks[k][1] = wks[k];
        }
        return weeks;
    }

    public static String[][] getNextWeeks(Date d){
        d.setDate(d.getDate() + 7);
        String[][] weeks = new String[7][2];
        int day = d.getDay();
        if(day == 0) day = 7;
        for(int i=1-day,k=0;i<=7-day;i++,k++){
            Date d2 = new Date(d.getTime());
            d2.setDate(d.getDate() + i);
            weeks[k][0] = format(d2);
            weeks[k][1] = wks[k];
        }
        return weeks;
    }

    //private static Date curDate =new Date();
    public static Date nextDay(Date curDate,int i) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(curDate);
        cal.add(GregorianCalendar.DATE, 1*i);// �����ϼ�1
        return cal.getTime();
    }
    public static Date getDay(Date curDate,int i) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(curDate);
        cal.add(GregorianCalendar.DATE, -1*i);// �����ϼ�1
        return cal.getTime();
    }
    // ��ȡ��һ�ܵ�����
    public static Date nextWeek(Date curDate,int i) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(curDate);
        cal.add(GregorianCalendar.DATE, 7*i);// �������ϼ�7��
        return cal.getTime();
    }
    public static Date getWeek(Date curDate,int i) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(curDate);
        cal.add(GregorianCalendar.DATE, -7*i);// �������ϼ�7��
        return cal.getTime();
    }

    // ��ȡ��һ�µ�����
    public static Date nextMonth(Date curDate, int i) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(curDate);
        cal.add(GregorianCalendar.MONTH, 1*i);// ���·��ϼ�1
        return cal.getTime();
    }
    public static Date getMonth(Date curDate, int i) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(curDate);
        cal.add(GregorianCalendar.MONTH, -1*i);// ���·��ϼ�1
        return cal.getTime();
    }
    // ��ȡ��һ�������
    public static Date nextYear(Date curDate,int i) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(curDate);
        cal.add(GregorianCalendar.YEAR, 1*i);// �����ϼ�1
        return cal.getTime();
    }


}
