package com.oversea.task.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yhb
 * @version V1.0
 * @Title: DateUtils.java
 * @Package com.taofen8.www.util
 * @Description: 时间工具类
 * @date 2013-1-22 下午4:36:20
 */
public class DateUtils {

    /**
     * 获取现在时间
     *
     * @return 返回格式化后的时间
     */
    public static Date getCurrentDate(String format) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(currentTime);
        ParsePosition pos = new ParsePosition(0);
        Date currentTime_2 = formatter.parse(dateString, pos);
        return currentTime_2;
    }
    

    /**
     * 获取现在时间
     *
     * @return 返回格式化后的时间
     */
    public static String getCurrentDateStr(String format) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 格式化时间
     *
     * @param date
     * @param format
     * @return
     */
    public static String formatDate(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 将字符串 格式化成 时间格式
     *
     * @param format  时间格式
     * @param dateStr 时间文本
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String format, String dateStr) throws ParseException {
        if (StringUtils.isEmpty(dateStr) || StringUtils.isEmpty(format)) {
            return null;
        }
        SimpleDateFormat bartDateFormat = new SimpleDateFormat(format);
        return bartDateFormat.parse(dateStr);
    }

    /**
     * 判断当前日期，在一年中是奇数天还是偶数天
     *
     * @return
     */
    public static boolean isOdd() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        return day % 2 == 1;
    }

    /**
     * 时间计算方法
     *
     * @param time       基准时间
     * @param minDate    需要做运算的时间
     * @param type       时间单位  如 : Calendar.MINUTE , Calendar.DAY_OF_MONTH
     * @param dateFormat 返回的时间格式
     * @return
     */
    public static String dateCalcu(Date time, int minDate, int type, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(type, minDate);
        String s_date = sdf.format(calendar.getTime());
        return s_date;
    }

    /**
     * 时间计算方法
     *
     * @param time    基准时间
     * @param minDate 需要做运算的时间
     * @param type    时间单位  如 : Calendar.MINUTE , Calendar.DAY_OF_MONTH
     * @return
     */
    public static Date dateCalcu(Date time, int minDate, int type) {
        Date date = time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(type, minDate);
        return calendar.getTime();
    }

    /**
     * 7天的毫秒数
     */
    public static final long MILLS_7DAYS = 7 * 24 * 60 * 60 * 1000;

    private static final long MillihourPerDay = 60 * 60 * 1000;
    private static final long Milli24HourPerDay = 24 * 60 * 60 * 1000;
    private static final long MillisecondPerMinute = 60 * 1000;

    /**
     * 字符串转换成日期
     * 格式为yyyyMMddHHmmss
     *
     * @param stringDate
     * @return
     * @throws ParseException
     */
    public static final Date string2DateTime2(String stringDate) throws ParseException {
        if (stringDate == null || stringDate.length() == 0) {
            return null;
        }
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return ymdhmsFormat.parse(stringDate);
    }

    /**
     * 日期转换成字符串时分秒
     * 格式为HH:mm:ss
     *
     * @param date
     * @return
     */
    public static final String hmsFormat(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat hmsFormat = new SimpleDateFormat("HH:mm:ss");
        return hmsFormat.format(date);
    }

    /**
     * 日期转换成字符串时分
     * 格式为HH:mm
     *
     * @param date
     * @return
     */
    public static final String hmFormat(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat hmFormat = new SimpleDateFormat("HH:mm");
        return hmFormat.format(date);
    }

    /**
     * 日期转换成字符串
     * 格式为年月日星期
     *
     * @param date
     * @return
     */
    public static final String ymdwFormat(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat ymdwFormat = DateFormat.getDateInstance(DateFormat.FULL);
        return ymdwFormat.format(date);
    }

    /**
     * 日期转换成字符串
     * 格式为yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String ymdhmsFormat(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return ymdhmsFormat.format(date);
    }

    /**
     * 日期转成字符串
     * 格式为yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public static String ymdFormat(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd");
        return ymdFormat.format(date);
    }

    /**
     * 转成"xxxx年xx月xx日"
     *
     * @param date
     * @return
     */
    public static String ymdFormat2(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat ymdFormat = new SimpleDateFormat("yyyy年MM月dd日");
        return ymdFormat.format(date);
    }

    /**
     * 日期转成字符串
     * 格式为yyyy-MM
     *
     * @param date
     * @return
     */
    public static String ymFormat(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat ymdFormat = new SimpleDateFormat("yyyy-MM");
        return ymdFormat.format(date);
    }

    /**
     * 日期转成字符串
     * 格式为yyyyMM
     *
     * @param date
     * @return
     */
    public static String yearMonthFormat(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat ymdFormat = new SimpleDateFormat("yyyyMM");
        return ymdFormat.format(date);
    }

    /**
     * 字符串转成日期
     * 格式为yyyyMM
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static Date yearMonthString2Date(String date) throws ParseException {
        if (date == null) {
            return null;
        }
        DateFormat ymdFormat = new SimpleDateFormat("yyyyMM");
        return ymdFormat.parse(date);
    }

    /**
     * 格式化日期为"年-月-日 时:分"
     *
     * @param date
     * @return
     */
    public static String ymdhmFormat(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat ymdhmFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return ymdhmFormat.format(date);
    }

    /**
     * 字符串转换成日期
     * 格式为yyyy-MM-dd
     *
     * @param ymdStringDate
     * @return
     * @throws ParseException
     */
    public static Date ymdString2Date(String ymdStringDate) throws ParseException {
        if (ymdStringDate == null) {
            return null;
        }
        DateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd");
        return ymdFormat.parse(ymdStringDate);
    }


    /**
     * @param ymdStringDate
     * @param format
     * @return
     * @throws ParseException
     */
    public static Date parse(String ymdStringDate, String format) throws ParseException {
        if (ymdStringDate == null) {
            return null;
        }
        DateFormat ymdFormat = new SimpleDateFormat(format);
        return ymdFormat.parse(ymdStringDate);
    }

    /**
     * @param date
     * @param format
     * @return
     */
    public static String format(Date date, String format) {
        if (date == null) {
            return "";
        }
        DateFormat ymdhmFormat = new SimpleDateFormat(format);
        return ymdhmFormat.format(date);
    }

    /**
     * 字符串转换成日期年月
     * 格式为yyyy-MM
     *
     * @param ymStringDate
     * @return
     * @throws ParseException
     */
    public static Date ymString2Date(String ymStringDate) throws ParseException {
        if (ymStringDate == null) {
            return null;
        }

        DateFormat ymFormat = new SimpleDateFormat("yyyy-MM");
        return ymFormat.parse(ymStringDate);
    }

    /**
     * 字符串转换成日期时分
     * 格式为HH:mm
     *
     * @param hmStringDate
     * @return
     * @throws ParseException
     */
    public static Date hmString2DateTime(String hmStringDate) throws ParseException {
        if (hmStringDate == null) {
            return null;
        }

        DateFormat hmFormat = new SimpleDateFormat("HH:mm");
        return hmFormat.parse(hmStringDate);
    }

    /**
     * alahan add 20050825
     * 获取传入时间相差的日期
     *
     * @param dt   传入日期，可以为空
     * @param diff 需要获取相隔diff天的日期 如果为正则取以后的日期，否则时间往前推
     * @return
     */
    public static String getDiffStringDate(Date dt, int diff) {
        Calendar ca = Calendar.getInstance();

        if (dt == null) {
            ca.setTime(new Date());
        } else {
            ca.setTime(dt);
        }

        ca.add(Calendar.DATE, diff);

        return ymdFormat(ca.getTime());
    }

    /**
     * 校验输入的时间格式是否合法，但不需要校验时间一定要是8位的
     *
     * @param statTime
     * @return alahan add 20050901
     */
    public static boolean checkTime(String statTime) {
        if (statTime.length() > 8) {
            return false;
        }

        String[] timeArray = statTime.split(":");

        if (timeArray.length != 3) {
            return false;
        }

        for (int i = 0; i < timeArray.length; i++) {
            String tmpStr = timeArray[i];

            try {
                Integer tmpInt = new Integer(tmpStr);

                if (i == 0) {
                    if ((tmpInt.intValue() > 23) || (tmpInt.intValue() < 0)) {
                        return false;
                    } else {
                        continue;
                    }
                }

                if ((tmpInt.intValue() > 59) || (tmpInt.intValue() < 0)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    /**
     * 返回日期时间（Add by Sunzy）
     * 格式为yyyy-MM-dd HH:mm:ss
     *
     * @return @throws
     * ParseException
     */
    public static Date ymdhmsString2DateTime(String ymdhmsStringDate) {
        if (ymdhmsStringDate == null) {
            return null;
        }
        Date date = null;
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = ymdhmsFormat.parse(ymdhmsStringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 字符串日期转换成毫秒表示的日期
     *
     * @param stringDate
     * @return
     * @throws ParseException
     */
    public static Long ymdhmsString2DateLong(String stringDate) throws ParseException {
        Date d = ymdhmsString2DateTime(stringDate);

        if (d == null) {
            return null;
        }

        return new Long(d.getTime());
    }

    /**
     * 比较两个日期
     *
     * @param planTime
     * @param reallyTime
     * @return
     * @throws ParseException
     */
    public static boolean compareStringDateTime(String planTime, String reallyTime) throws ParseException {
        Date dt1 = ymdhmsString2DateTime(planTime);
        Date dt2 = ymdhmsString2DateTime(reallyTime);

        return dt1.after(dt2);
    }

    private static long MillisecondsPerDay = 24 * 60 * 60 * 1000;

    /**
     * 减去天数
     *
     * @param d
     * @param intev
     * @return
     */
    public static Date dayMinus(Date d, int intev) {
        long dl = d.getTime();

        return new Date(dl - (intev * MillisecondsPerDay));
    }

    /**
     * 加减天数
     *
     * @return Date
     */
    public static Date increaseWeek(Date aDate, int weeks) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(aDate);
        cal.add(Calendar.WEEK_OF_YEAR, weeks);
        return cal.getTime();
    }

    /**
     * 加减天数
     *
     * @param aDate
     * @param days
     * @return Date
     */
    public static Date increaseDate(Date aDate, int days) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(aDate);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    public static Date increaseDay(Date aDate, int days) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(aDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    /**
     * 加减小时数
     *
     * @param d
     * @param intev
     * @return
     */
    public static Date increaseHourDate(Date d, int intev) {
        if (d == null) {
            d = new Date();
        }

        long dl = d.getTime();

        return new Date(dl + (intev * MillihourPerDay));
    }

    /**
     * 加减分钟数
     *
     * @param d
     * @param intev
     * @return
     */
    public static Date increaseMinuteDate(Date d, int intev) {
        if (d == null) {
            d = new Date();
        }

        long dl = d.getTime();

        return new Date(dl + (intev * MillisecondPerMinute));
    }

    /**
     * 取得本季度的第一天
     *
     * @return
     */
    public static Date getQuarterFirst() {
        Calendar cal = Calendar.getInstance();

        int m = cal.get(Calendar.MONTH);
        int n = 0 - (m % 3);

        cal.add(Calendar.MONTH, n);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Date first = cal.getTime();

        return first;
    }

    /**
     * 获取年份
     *
     * @param date
     * @return
     */
    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static int getYear(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 获取月份
     *
     * @param date
     * @return
     */
    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getMonth(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取日期
     *
     * @param date
     * @return
     */
    public static int getDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DATE);
    }

    public static int getDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.DATE);
    }

    /**
     * 获取小时
     *
     * @param date
     * @return
     */
    public static int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getHour(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 把日期后的时间归0 变成(yyyy-MM-dd 00:00:00:000)
     *
     * @return Date
     */
    public static Date zerolizedTime(Date fullDate) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(fullDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 当月最后一天日期
     *
     * @return
     */
    public static String getCurrentMonthEndDate() {
        Calendar ca = Calendar.getInstance();

        ca.setTime(new Date());
        ca.set(Calendar.HOUR_OF_DAY, 23);
        ca.set(Calendar.MINUTE, 59);
        ca.set(Calendar.SECOND, 59);
        ca.set(Calendar.DAY_OF_MONTH, 1);
        ca.add(Calendar.MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);

        Date lastDate = new Date(ca.getTime().getTime());

        return ymdFormat(lastDate);
    }

    /**
     * 当月第一天日期
     *
     * @return
     */
    public static String getCurrentMonthStartDate() {
        Calendar ca = Calendar.getInstance();

        ca.setTime(new Date());
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.DAY_OF_MONTH, 1);

        Date firstDate = ca.getTime();

        return ymdFormat(firstDate);
    }

    /**
     * 当月最后一天日期
     *
     * @return
     */
    public static Date getCurrentMonthEndDate(Date date) {
        Calendar ca = Calendar.getInstance();

        ca.setTime(date);
        ca.set(Calendar.HOUR_OF_DAY, 23);
        ca.set(Calendar.MINUTE, 59);
        ca.set(Calendar.SECOND, 59);
        ca.set(Calendar.DAY_OF_MONTH, 1);
        ca.add(Calendar.MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);

        Date lastDate = new Date(ca.getTime().getTime());

        return lastDate;
    }

    /**
     * 当月第一天日期
     *
     * @return
     */
    public static Date getCurrentMonthStartDate(Date date) {
        Calendar ca = Calendar.getInstance();

        ca.setTime(date);
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.DAY_OF_MONTH, 1);

        Date firstDate = ca.getTime();

        return firstDate;
    }

    /**
     * 上个月最后一天日期
     *
     * @return
     */
    public static Date getLastMonthEndDate() {
        return getLastMonthEndDate(new Date());
//		Date lastDate = new Date(ca.getTime().getTime());
//		return ymdFormat(lastDate);
    }

    /**
     * 上个月第一天日期
     *
     * @return
     */
    public static Date getLastMonthStartDate() {
        return getLastMonthStartDate(new Date());
//		Date firstDate = ca.getTime();
//		return ymdFormat(firstDate);
    }

    /**
     * 上个月最后一天日期
     *
     * @return
     */
    public static Date getLastMonthEndDate(Date date) {
        Calendar ca = Calendar.getInstance();

        ca.setTime(date);
        ca.set(Calendar.HOUR_OF_DAY, 23);
        ca.set(Calendar.MINUTE, 59);
        ca.set(Calendar.SECOND, 59);
        ca.set(Calendar.DAY_OF_MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);

        return ca.getTime();
//		Date lastDate = new Date(ca.getTime().getTime());
//		return ymdFormat(lastDate);
    }

    /**
     * 上个月第一天日期
     *
     * @return
     */
    public static Date getLastMonthStartDate(Date date) {
        Calendar ca = Calendar.getInstance();

        ca.setTime(date);
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.DAY_OF_MONTH, 1);
        ca.add(Calendar.MONTH, -1);

        return ca.getTime();
//		Date firstDate = ca.getTime();
//		return ymdFormat(firstDate);
    }

    /**
     * 取两个日期相差的年份
     * 如果算年龄的话,这个是周岁ziliu modified
     *
     * @param d1
     * @param d2
     * @return
     */
    public static int yearIntev(Date d1, Date d2) {
        if ((d1 == null) || (d2 == null)) {
            return -1;
        }

        int intev = 0;

        try {
            DateFormat yFormat = new SimpleDateFormat("yyyy");
            int s1 = Integer.parseInt(yFormat.format(d1));
            int s2 = Integer.parseInt(yFormat.format(d2));
            intev = s2 - s1;
        } catch (Exception e) {
        }

        return intev;
    }

    /**
     * 这个时间段是不是没有超出当前日期
     *
     * @param start
     * @param end
     * @return
     */
    public static boolean isPeriod(Date start, Date end) {
        return start.getTime() <= System.currentTimeMillis() && end.getTime() >= System.currentTimeMillis();
    }

    /**
     * 转换成GMT时间
     *
     * @param d
     * @return
     */
    public static Date toGMT(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c = toGMT(c);
        return c.getTime();
    }

    /**
     * 转换成GMT时间
     *
     * @param cal
     * @return
     */
    public static Calendar toGMT(Calendar cal) {
        // 也可以用 Calendar cal1 = Calendar.getInstance();
        Calendar cal1 = (Calendar) cal.clone();
        // 先保存原来的时区
        TimeZone tzSave = cal.getTimeZone();
        // 将时区转换到GMT下
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        // 将GMT下的年月日等信息再填回到当前时区下的Calendar
        cal1.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        cal1.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND));
        // 恢复原来的时区
        cal.setTimeZone(tzSave);
        // 锁定MilliSecond
        cal.getTime();
        return cal1;
    }

    public static long getTimeIntervalSec(Date first, Date second) {
        if (first == null || second == null) {
            return 0;
        }
        return (second.getTime() - first.getTime()) / 1000;
    }

    /**
     * second-first相差几分钟
     *
     * @param first
     * @param second
     * @return
     */
    public static long getTimeIntervalMinute(Date first, Date second) {
        if (first == null || second == null) {
            return 0;
        }
        return (second.getTime() - first.getTime()) / (1000 * 60);
    }

    public static Date getAfterYearDate() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, 1);
        Date otherDate = cal.getTime();
        return otherDate;
    }

    public static long getTimeIntervalDay(Date first, Date second) {
        if (first == null || second == null) {
            return 0;
        }
        return (second.getTime() - first.getTime()) / (1000 * 60 * 60 * 24);
    }

    /**
     * dtime距今几天(返回天数)
     *
     * @param dtime
     * @return
     */
    public static float getDaysFromNow(Date dtime) {
        Date currentTime = new Date();
        return (float) (currentTime.getTime() - dtime.getTime()) / (1000 * 60 * 60 * 24); //一天1000*60*60*24
    }

    public static String getCountdown(Date date) {
        long nowTime = new Date().getTime();
        long countdownTime = date.getTime();
        long _d_time = countdownTime - nowTime;
        //取得天
        long day = _d_time / (1000 * 60 * 60 * 24);
        long _h_time = _d_time % (1000 * 60 * 60 * 24);
        //取得小时
        long hours = _h_time / (1000 * 60 * 60);
        long _s_time = _h_time % (1000 * 60 * 60);
        long s = _s_time / (1000 * 60);
        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (s > 0) {
            sb.append(s).append("分");
        }
        return sb.toString();
    }

    public static String getCountdownHour(Date date) {
        long nowTime = new Date().getTime();
        long countdownTime = date.getTime();
        long _d_time = countdownTime - nowTime;
        long h = _d_time / (1000 * 60 * 60);
        if (h < 0) {
            h = 0;
        } else if (h == 0) {
            h = 1;
        }
        return String.valueOf(h);
    }


    /**
     * 是否是闰年
     */
    private static boolean isLeapYear(int year) {
        return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
    }

    /**
     * 校验日期格式是否合法
     */
    public static boolean validate(String dateString) {
        //使用正则表达式 测试 字符 符合 dddd-dd-dd 的格式(d表示数字)
        Pattern p = Pattern.compile("\\d{4}+[-]\\d{1,2}+[-]\\d{1,2}+");
        Matcher m = p.matcher(dateString);
        if (!m.matches()) {
            return false;
        }
        //得到年月日
        String[] array = dateString.split("-");
        int year = Integer.valueOf(array[0]);
        int month = Integer.valueOf(array[1]);
        int day = Integer.valueOf(array[2]);

        if (month < 1 || month > 12) {
            return false;
        }
        int[] monthLengths = new int[]{0, 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (isLeapYear(year)) {
            monthLengths[2] = 29;
        } else {
            monthLengths[2] = 28;
        }
        int monthLength = monthLengths[month];
        if (day < 1 || day > monthLength) {
            return false;
        }
        return true;
    }

    public static String getStarSign(String dateStr) {//传入格式"yyyy-MM-dd"
        int month = Integer.parseInt(dateStr.substring(5, 7));
        int day = Integer.parseInt(dateStr.substring(8, dateStr.length()));

        String[] starArr = {"摩羯座", "水瓶座", "双鱼座", "牡羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座"};
        int[] DayArr = {22, 20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22};  // 两个星座分割日
        int index = month;
        // 所查询日期在分割日之前，索引-1，否则不变
        if (day < DayArr[month - 1]) {
            index = index - 1;
        }
        if (index == 12) {
            index = 0;
        }
        // 返回索引指向的星座string
        return starArr[index];
    }

    /**
     * 根据生日计算年龄
     *
     * @param birthday
     * @return
     */
    public static int getAge(Date birthday) {
        if (birthday == null) {
            return 0;
        }
        long age = System.currentTimeMillis() - birthday.getTime();
        return (int) ((age / (24 * 60 * 60 * 1000)) / 365) + 1;
    }

    /**
     * 判断给定的日期是否就是当天
     *
     * @param date
     * @return
     */
    public static boolean isCurrentDay(Date date) {
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        if (System.currentTimeMillis() - date.getTime() < Milli24HourPerDay) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当天凌晨的毫秒数
     *
     * @return
     */
    public static long getCurrentDayTimeMillis() {
        Long currentTimeMillis = System.currentTimeMillis() / 1000 * 1000;
        Date date = new Date(currentTimeMillis);

        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);

        return date.getTime();
    }

    /**
     * 获取指定日期凌晨的毫秒数
     *
     * @param day
     * @return
     */
    public static long getDayTimeMillis(long day) {
        day = day / 1000 * 1000;
        Date date = new Date(day);

        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);

        return date.getTime();
    }

    /**
     * 获取指定日期午夜的毫秒数
     *
     * @param day
     * @return
     */
    public static long getMidnightTimeMillis(long day) {
        day = day / 1000 * 1000;
        Date date = new Date(day);
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);

        return date.getTime() + 999;
    }

    /**
     * 获取指定时间，当月第一天的毫秒数
     *
     * @param month
     * @return
     */
    public static long getMonthTimeMillis(long month) {
        month = month / 1000 * 1000;
        Date date = new Date(month);

        date.setDate(1);
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);

        return date.getTime();
    }

    /**
     * 日期转换成字符串
     * 格式为yyyyMMddHHmmss
     *
     * @param date
     * @return
     */
    public static String DateFormatStr(Date date, String format) {
        if (date == null) {
            return "";
        }

        DateFormat ymdhmsFormat = new SimpleDateFormat(format);
        return ymdhmsFormat.format(date);
    }

    public static Date increaseDayLastTime(Date aDate, int days) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(aDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    public static String getClockTime(Long timestamp) {
        Long millisecond = System.currentTimeMillis() - timestamp;
        Long minute = millisecond / 1000 / 60;
        if (minute > 60) {
            Long hour = minute / 60;
            if (hour > 24) {
                Long day = hour / 24;
                return day + "天前";
            }
            return hour + "小时前";
        }
        if (minute < 1) {
            return "刚刚";
        }
        return minute + "分钟前";
    }
}
