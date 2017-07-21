package com.oversea.task.utils;

import java.math.BigDecimal;

/**
 * @author fengjian
 * @version V1.0
 * @title: sea-online
 * @Package com.oversea.task.utils
 * @Description: 重量转化
 * @date 16/1/13 17:48
 */
public class WeightUtil {

    private WeightUtil() {
    }

    private static final double LB_2_KG_RATE = 0.4535924d;

    public static Integer lb2kg(String lb) {
        return new BigDecimal(lb).multiply(new BigDecimal(LB_2_KG_RATE)).intValue();
    }

    public static Integer lb2kg(int lb) {
        return new BigDecimal(lb).multiply(new BigDecimal(LB_2_KG_RATE)).intValue();
    }

    public static Integer lb2kg(double lb) {
        return new BigDecimal(lb).multiply(new BigDecimal(LB_2_KG_RATE)).intValue();
    }

    public static Integer lb2g(String lb) {
        return new BigDecimal(lb).multiply(new BigDecimal(LB_2_KG_RATE)).multiply(new BigDecimal(1000)).intValue();
    }

    public static Integer lb2g(int lb) {
        return new BigDecimal(lb).multiply(new BigDecimal(LB_2_KG_RATE)).multiply(new BigDecimal(1000)).intValue();
    }

    public static Integer lb2g(double lb) {
        return new BigDecimal(lb).multiply(new BigDecimal(LB_2_KG_RATE)).multiply(new BigDecimal(1000)).intValue();
    }

    public static void main(String[] args) {

        System.out.println(lb2g(2));
    }
}
