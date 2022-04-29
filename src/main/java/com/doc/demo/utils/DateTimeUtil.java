package com.doc.demo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: zym
 * @Date: 2019/6/14 15:44
 */
public class DateTimeUtil {

    public final static String YMD = "yyyy-MM-dd";

    private final static SimpleDateFormat sdHms = new SimpleDateFormat("HH:mm:ss");

    private final static SimpleDateFormat sdYm = new SimpleDateFormat("yyyy-MM");

    private final static SimpleDateFormat sdYmd = new SimpleDateFormat(YMD);

    private final static SimpleDateFormat sdYmdHms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final static String ymdReg = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$";

    /**
     * 传入时间获取，当前时间是星期几
     *
     * @return 返回星期几
     */
    public static int getWeek(Date date) {
        int[] weekDays = {7, 1, 2, 3, 4, 5, 6};
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 检查格式是否有效 yyyy-MM-dd
     *
     * @param ymdStr yyyy-MM-dd
     * @return true表示有效
     */
    public static boolean ymdCheck(String ymdStr) {
        Pattern compile = Pattern.compile(ymdReg);
        Matcher matcher = compile.matcher(ymdStr);
        return matcher.find();
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    /**
     * 得到指定时间的毫秒数，得到一个时分秒的毫秒
     *
     * @param time 格式例如 18:30:20
     * @return {@link Long}毫秒数
     */
    public static Long getHmsLocalTime(String time) {
        String[] times = time.split(":");
        return timeCount(times);
    }

    /**
     * 获取时间毫秒数
     *
     * @param time 格式例如 2021-04-11
     * @return {@link Long}毫秒数
     */
    public static Long getYmdHmsLocalTime(String time) {
        try {
            Date parse = sdYmd.parse(time);
            return parse.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }

    /**
     * 传入一个date类型得到一个时分秒的毫秒
     */
    public static Long getHmsLocalTime(Date time) {
        String hmsTime = sdHms.format(time);
        String[] times = hmsTime.split(":");
        return timeCount(times);
    }

    /**
     * 获取一个时间的年月日的毫秒数
     *
     * @param date 传入的时间
     * @return 移除掉时分秒后的时间
     */
    public static Long getYmdTime(Date date) {
        String format = sdYmd.format(date);
        try {
            return sdYmd.parse(format).getTime();
        } catch (ParseException e) {
            return System.currentTimeMillis() - 1000 * 60;
        }
    }

    /**
     * 获取一个字符串的时间格式
     * <p>
     * yyyy-MM-dd HH:mm:ss
     */
    public static String getYmdHmsStr(Date date) {
        return sdYmdHms.format(date);
    }

    /**
     * 返回一个date类
     *
     * @param time yyyy-MM-dd HH:mm:ss
     */
    public static Date getYmdHmsDate(String time) {
        try {
            return sdYmdHms.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取一个字符串的时间格式
     * <p>
     * yyyy-MM
     */
    public static String getYm(Date date) {
        return sdYm.format(date);
    }

    /**
     * 获取一个字符串的时间格式
     * <p>
     * yyyy-MM-dd
     */
    public static String getYmdStr(Date date) {
        return sdYmd.format(date);
    }

    /**
     * 获取减少后的日期
     * <p>
     * yyyy-MM-dd
     *
     * @param day 减少的天数
     * @return 返回的时间 格式为yyyy-MM-dd
     */
    public static String getMinusDaysYmdStr(long day) {
        String yesterdayStr = LocalDate.now().minusDays(day).atStartOfDay().atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern(YMD));
        return yesterdayStr;
    }

    private static Long timeCount(String[] times) {
        Long timeCount = 0L;
        for (int i = 0; i < times.length; i++) {
            //i=0代表小时,i=1代表分钟。如果开始字符为0则截取
            if (i == 0) {
                timeCount += TimeUnit.HOURS.toMillis(Integer.valueOf(times[i]));
            }
            if (i == 1) {
                timeCount += TimeUnit.MINUTES.toMillis(Integer.valueOf(times[i]));
            }
        }
        return timeCount;
    }
}
