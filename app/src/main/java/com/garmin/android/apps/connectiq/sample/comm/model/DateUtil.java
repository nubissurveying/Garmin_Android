package com.garmin.android.apps.connectiq.sample.comm.model;

/**
 * Created by cal on 4/24/18.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
    public static String stringifyAll(Calendar calendar){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return (calendar == null) ? "null" : format.format(calendar.getTime());
    }
    public static String stringifyTime(Calendar calendar){
        SimpleDateFormat format = new SimpleDateFormat("kk:mm");
        return (calendar == null) ? "null" : format.format(calendar.getTime());
    }
    public static String stringifyTimeHuman(Calendar calendar){
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, h:mma");
        return (calendar == null) ? "null" : format.format(calendar.getTime());
    }
    public static String stringifyDate(Calendar calendar){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return (calendar == null) ? "null" : format.format(calendar.getTime());
    }
    public static int intDate(Calendar calendar){
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmm");
        return (calendar == null) ? -1 : Integer.parseInt(format.format(calendar.getTime()));
    }
    public static Calendar stringToCalendarAll(String input){
        if (input.isEmpty()) return null;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cal.setTime(format.parse(input));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }


}