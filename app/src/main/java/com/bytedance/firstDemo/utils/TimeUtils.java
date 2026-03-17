package com.bytedance.firstDemo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 全局时间工具类：
 *  - formatTime(String)  用于消息列表（刚刚 / xx分钟前 / x天前 / 昨天）
 *  - formatChatTime(long) 用于聊天界面（今天 HH:mm / 昨天 HH:mm / 前天 HH:mm / MM-dd HH:mm）
 *  - parse()             解析数据库时间
 *  - now()               当前时间字符串
 */
public class TimeUtils {

    private static final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);


    // =========================================================
    //   消息列表格式（如 抖音消息页）：刚刚 / xx分钟前 / x天前 / MM-dd
    // =========================================================

    public static String formatTime(String timeStr) {
        try {
            Date time = sdf.parse(timeStr);
            Date now = new Date();

            long diffMs = now.getTime() - time.getTime();
            long diffSec = diffMs / 1000;
            long diffMin = diffSec / 60;
            long diffHour = diffMin / 60;
            long diffDay = diffHour / 24;

            Calendar calMsg = Calendar.getInstance();
            calMsg.setTime(time);

            Calendar calNow = Calendar.getInstance();

            // 1 分钟内
            if (diffSec < 60) return "刚刚";

            // 1 小时内
            if (diffMin < 60) return diffMin + " 分钟前";

            // 同一天
            if (isSameDay(calNow, calMsg)) {
                return new SimpleDateFormat("HH:mm", Locale.CHINA).format(time);
            }

            // 昨天
            if (isYesterday(calNow, calMsg)) {
                return "昨天 " + new SimpleDateFormat("HH:mm", Locale.CHINA).format(time);
            }

            // 7 天内
            if (diffDay < 7) return diffDay + " 天前";

            // 其他：MM-dd
            return new SimpleDateFormat("MM-dd", Locale.CHINA).format(time);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timeStr;
    }



    // =========================================================
    //      聊天界面时间戳（微信 / 抖音逻辑）
    // =========================================================

    public static String formatChatTime(long ts) {

        Calendar msg = Calendar.getInstance();
        msg.setTimeInMillis(ts);

        Calendar now = Calendar.getInstance();

        SimpleDateFormat hm = new SimpleDateFormat("HH:mm", Locale.CHINA);
        SimpleDateFormat mdhm = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
        SimpleDateFormat ymdhm = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

        int msgYear = msg.get(Calendar.YEAR);
        int msgDay = msg.get(Calendar.DAY_OF_YEAR);

        int nowYear = now.get(Calendar.YEAR);
        int nowDay = now.get(Calendar.DAY_OF_YEAR);

        // 跨年 → yyyy-MM-dd HH:mm
        if (msgYear != nowYear) {
            return ymdhm.format(msg.getTime());
        }

        int diff = nowDay - msgDay;

        if (diff == 0) return hm.format(msg.getTime());                 // 今天
        if (diff == 1) return "昨天 " + hm.format(msg.getTime());       // 昨天
        if (diff == 2) return "前天 " + hm.format(msg.getTime());       // 前天

        // 同一年内 → MM-dd HH:mm
        return mdhm.format(msg.getTime());
    }



    // =========================================================
    //                        通用方法
    // =========================================================

    private static boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isYesterday(Calendar now, Calendar msg) {
        Calendar temp = (Calendar) now.clone();
        temp.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(temp, msg);
    }

    /** 当前时间（写入数据库） */
    public static String now() {
        return sdf.format(new Date());
    }

    /** 将 "yyyy-MM-dd HH:mm:ss" 转为毫秒 */
    public static long parse(String timeStr) {
        if (timeStr == null) return 0;
        try {
            return sdf.parse(timeStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
