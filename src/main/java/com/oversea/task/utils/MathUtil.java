package com.oversea.task.utils;

import java.math.BigDecimal;

/**
 * 数学计算类
 *
 * @author fengjian
 */
public class MathUtil {

    public static void main(String args[]) {
//		System.out.println(MathUtil.mul("18.0234", "2.9881"));
//		
//		System.out.println(MathUtil.mul(18.0234d, 2.9881d));
        System.out.println(MathUtil.divCelling("62201", "10000"));
        System.out.println(MathUtil.div("62201", "10000", 2));
    }


    private MathUtil() {
    }

    private static final int DEF_DIV_SCALE = 2;

    /**
     * 提供精确的加法运算。
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static String add(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return String.valueOf(b1.add(b2));
    }

    /**
     * 提供精确的减法运算。
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static String sub(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return String.valueOf(b1.subtract(b2));
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static String mul(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        int scale = 2;
        return round(b1.multiply(b2), scale);
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static String mul(int v1, int v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        int scale = 2;
        return round(b1.multiply(b2), scale);
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static Double mul(Double v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        int scale = 2;
        return round(b1.multiply(b2).doubleValue(), scale);
    }


    public static String divCelling(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        int scale = 2;
        return String.valueOf(b1.divide(b2, scale, BigDecimal.ROUND_CEILING));
    }

    public static Double divCelling(Double v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        int scale = 2;
        return b1.divide(b2, scale, BigDecimal.ROUND_CEILING).doubleValue();
    }

    public static Integer divCelling(Integer v1, Integer v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        int scale = 2;
        return b1.divide(b2, scale, BigDecimal.ROUND_CEILING).intValue();
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static String div(String v1, String v2) {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    public static int div(Double v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, DEF_DIV_SCALE, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static String div(String v1, String v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return String.valueOf(b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP));
    }

    public static Double div(Double v1, Double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static Integer div(Integer v1, Integer v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static Long div(Long v1, Long v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).longValue();
    }


    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static String round(String v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(v);
        BigDecimal one = new BigDecimal("1");
        return String.valueOf(b.divide(one, scale, BigDecimal.ROUND_HALF_UP));
    }

    public static String round(BigDecimal b, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal one = new BigDecimal("1");
        return String.valueOf(b.divide(one, scale, BigDecimal.ROUND_HALF_UP));
    }

    public static Double round(Double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(v);
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 乘100
     *
     * @param num
     * @return
     */
    public static int mul_100(Object num) {
        if (num instanceof Integer) {
            return MathUtil.mul(((Integer) num).doubleValue(), 100d).intValue();
        } else if (num instanceof Long) {
            return MathUtil.mul(((Long) num).doubleValue(), 100d).intValue();
        } else if (num instanceof Double) {
            return MathUtil.mul(((Double) num), 100d).intValue();
        } else if (num instanceof String) {
            return MathUtil.mul(Double.parseDouble((String) num), 100d).intValue();
        } else {
            throw new IllegalArgumentException("mul type is erorr!");
        }
    }

    /**
     * 乘100
     *
     * @param num
     * @return
     */
    public static int toInt(Object num) {
        if (num instanceof Integer) {
            return MathUtil.mul(((Integer) num).doubleValue(), 1d).intValue();
        } else if (num instanceof Long) {
            return MathUtil.mul(((Long) num).doubleValue(), 1d).intValue();
        } else if (num instanceof Double) {
            return MathUtil.mul(((Double) num).doubleValue(), 1d).intValue();
        } else {
            throw new IllegalArgumentException("mul type is erorr!");
        }
    }
}
