package com.wehop.priest.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.text.StaticLayout;

public class DateUtil {
    private static SimpleDateFormat yyyMMFormat = new SimpleDateFormat("yyyy-MM");
    private static SimpleDateFormat yyyMMddFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // 获得当前月--开始日期
    public static String getMinMonthDate(String date, String dateFomate) {
        SimpleDateFormat yyyMMFormat = new SimpleDateFormat(dateFomate);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(yyyMMFormat.parse(date));
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            return yyyMMddFormat.format(calendar.getTime());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return null;

    }

    // 获得当前月--结束日期
    public static String getMaxMonthDate(String date, String dateFomate) {
        SimpleDateFormat yyyMMFormat = new SimpleDateFormat(dateFomate);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(yyyMMFormat.parse(date));
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return yyyMMddFormat.format(calendar.getTime());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getBrotherMonth(String date, String dateFomate, int step) {
        SimpleDateFormat yyyMMFormat = new SimpleDateFormat(dateFomate);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(yyyMMFormat.parse(date));
            calendar.add(Calendar.MONTH, step);
            return yyyMMFormat.format(calendar.getTime());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    
    public static String getCurrentMounth() {
        Calendar calendar = Calendar.getInstance();
        String mounth = yyyMMFormat.format(calendar.getTime());
        return mounth;
    }
    public static String getCurrentMounth(String dateFomate) {
        SimpleDateFormat yyyMMFormat = new SimpleDateFormat(dateFomate);
        Calendar calendar = Calendar.getInstance();
        String mounth = yyyMMFormat.format(calendar.getTime());
        return mounth;
    }
    
    
    public static void main(String[] args) {
        System.out.println(getMinMonthDate("2015-02", "yyyy-MM"));
        
        System.out.println(getMaxMonthDate("2015-02", "yyyy-MM"));
    }
}
