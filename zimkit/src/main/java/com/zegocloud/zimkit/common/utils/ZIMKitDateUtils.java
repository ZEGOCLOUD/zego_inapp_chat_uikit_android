package com.zegocloud.zimkit.common.utils;

import android.app.Application;
import android.text.TextUtils;
import com.zegocloud.zimkit.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.zegocloud.zimkit.R;

public class ZIMKitDateUtils {

    private static Application sApplication;
    public static SimpleDateFormat sdfParse = new SimpleDateFormat("yyyy-M-dd", Locale.CHINA);
    public static SimpleDateFormat sdfYMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    public static SimpleDateFormat sdfYMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    public static SimpleDateFormat sdfYMDHMS2 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    public static SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    public static SimpleDateFormat sdfMD = new SimpleDateFormat("MM-dd", Locale.CHINA);
    public static SimpleDateFormat sdfHM = new SimpleDateFormat("HH:mm", Locale.CHINA);
    public static SimpleDateFormat sdfHMS = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
    public static SimpleDateFormat sdfMDHM = new SimpleDateFormat("MM/dd HH:mm", Locale.CHINA);
    public static SimpleDateFormat sdfMDAndHM = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
    public static SimpleDateFormat sdfYM = new SimpleDateFormat("yyyy-MM", Locale.CHINA);
    public static SimpleDateFormat sdfMD2 = new SimpleDateFormat("MM-dd", Locale.CHINA);
    private static String[] weekOfDays = null;
    public static SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA);
    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.CHINA);

    public static void setContext(Application application) {
        sApplication = application;
    }

    public static String getUct(String data) {
        Date result;
        try {
            result = sdfUTC.parse(data);
            sdfYMDHMS.setTimeZone(TimeZone.getDefault());
            return sdfYMDHMS.format(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Direct output of "yyyy-MM-dd" format
     */
    public static String getYMDDate(long date) {
        return sdfYMD.format(date);
    }

    /**
     * Direct output of "HH:mm:ss" format
     */
    public static String getHMSDate(long date) {
        return sdfHMS.format(date);
    }

    /**
     * Return to "a certain month and a certain date"
     *
     * @param date
     * @return
     */
    public static String getMDDate2(long date) {
        return sdfMD2.format(date);
    }

    /**
     * Time to get the message
     */
    public static String getMessageDate(long date, boolean showDetailTime) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(new Date(date));
        Calendar nowCal1 = Calendar.getInstance();
        nowCal1.setTime(new Date());
        if (isSameDay(cal1, nowCal1)) {
            return sdfHM.format(date);
        } else if (isYesterday(cal1, nowCal1)) {
            return sApplication.getString(R.string.zimkit_yesterday) + (showDetailTime ? " " + sdfHM.format(date) : "");
        } else if (isWithinWeek(cal1, nowCal1)) {
            return getWeekOfDate(date) + (showDetailTime ? " " + sdfHM.format(date) : "");
        } else if (cal1.get(Calendar.YEAR) != nowCal1.get(Calendar.YEAR)) {
            if (showDetailTime) {
                return sdfYMDHM.format(date);
            } else {
                return sdfYMD.format(date);
            }
        } else {
            if (showDetailTime) {
                return sdfMDAndHM.format(date);
            } else {
                return sdfMD.format(date);
            }
        }
    }


    // 日期判断工具方法
    private static boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
            && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isYesterday(Calendar target, Calendar now) {
        Calendar yesterday = (Calendar) now.clone();
        yesterday.add(Calendar.DATE, -1);
        return isSameDay(target, yesterday);
    }

    private static boolean isWithinWeek(Calendar target, Calendar now) {
        long diff = now.getTimeInMillis() - target.getTimeInMillis();
        return diff < 7 * 24 * 60 * 60 * 1000L;
    }

    private static boolean sameYear(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
    }


    /**
     * Direct output of "yyyy-MM-dd HH:mm" format
     */
    public static String getYMDHMDate(long date) {
        return sdfYMDHM.format(date);
    }

    /**
     * Direct output of "yyyy-MM" format
     */
    public static String getYMDate(long date) {
        return sdfYM.format(date);
    }

    /**
     * Direct output of "HH:mm" format
     */
    public static String getHMDate(long date) {
        return sdfHM.format(date);
    }

    /**
     * Direct output of "yyyy-MM-dd HH:mm:ss" format
     */
    public static String getYMDHMSDate(long date) {
        return sdfYMDHMS.format(date);
    }

    /**
     * Directly output "yyyyMMddHHmmss" format
     */
    public static String getYMDHMSDate2(long date) {
        return sdfYMDHMS2.format(date);
    }

    /**
     * The format is yyyy-M-dd to work, yyyy-MM-dd or it will fail
     */
    public static long parseLong(String date) {
        try {
            return sdfParse.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * Formatted as yyyy-MM-dd to work
     */
    public static long parseLong2(String date) {
        try {
            return sdfYMD.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * The format is yyyy-MM-dd HH:mm:ss to work
     */
    public static long parseLong3(String date) {
        try {
            // FIXME: 2021/10/8 The data of the api may be null, which will crash
            if (TextUtils.isEmpty(date)) {
                return 0;
            }
            return sdfYMDHMS.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * The format is yyyy-MM-dd HH:mm to work
     */
    public static long parseLong4(String date) {
        try {
            // FIXME: 2021/10/8 The data of the api may be null, which will crash
            if (TextUtils.isEmpty(date)) {
                return 0;
            }
            return sdfYMDHM.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * Customized date resolution format
     */
    public static long parseLong(String date, SimpleDateFormat format) {
        try {
            return format.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * Get the day of the week of the specified date When the parameter is null, it means get the day of the week of the
     * current date
     *
     * @param milliseconds
     * @return
     */
    public static String getWeekOfDate(Long milliseconds) {
        if (weekOfDays == null) {
            weekOfDays = sApplication.getResources().getStringArray(R.array.zimkit_weeks);
        }
        return getWeekOfDate(milliseconds, weekOfDays);
    }

    public static String getWeekOfDate(Long milliseconds, String[] weekOfDays) {
        Calendar calendar = Calendar.getInstance();
        if (milliseconds != null) {
            calendar.setTimeInMillis(milliseconds);
        }
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekOfDays[w];
    }

    /**
     * Calculate the date n days after date
     */
    public static Date getDateAfter(Date date, int n) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + n);
        return now.getTime();
    }

    /**
     * Calculate the date n days before date
     */
    public static Date getDateBefore(Date date, int n) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - n);
        return now.getTime();
    }

    /**
     * Compare the number of days difference between two dates with positive and negative
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int calcIntervalDays(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        calendar2.set(Calendar.HOUR_OF_DAY, 0);
        calendar2.set(Calendar.MINUTE, 0);
        calendar2.set(Calendar.SECOND, 0);
        calendar2.set(Calendar.MILLISECOND, 0);
        int days = (int) ((calendar2.getTime().getTime() - calendar1.getTime().getTime()) / (1000 * 3600 * 24));
        return days;
    }

    public static long parseUTCTime(String utc) {
        sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdfUTC.parse(utc).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * Seconds to 00：00：00
     *
     * @param seconds
     * @return
     */
    public static String formatTime(long seconds) {
        int temp;
        StringBuffer sb = new StringBuffer();
        if (seconds > 3600) {
            temp = (int) (seconds / 3600);
            sb.append((seconds / 3600) < 10 ? "0" + temp + ":" : temp + ":");
            temp = (int) (seconds % 3600 / 60);
            changeSeconds(seconds, temp, sb);
        } else {
            temp = (int) (seconds % 3600 / 60);
            changeSeconds(seconds, temp, sb);
        }
        return sb.toString();
    }

    private static void changeSeconds(long seconds, int temp, StringBuffer sb) {
        sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");
        temp = (int) (seconds % 3600 % 60);
        sb.append((temp < 10) ? "0" + temp : "" + temp);
    }

}
