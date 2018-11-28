package com.jwell.doorcontrol.utils;

import com.jwell.boot.utilscommon.utils.DateTimeUtils;

import java.util.Date;

/**
 * 10进制转 16进制
 * 16进制 转 10进制
 * @author ljy
 *
 */
public final class DenaryConvertUtil {

    /**
     * 10 进制数字 转为 16进制
     * @param number
     * @return  0x0037d70d
     */
    public static long getDecadeSixteen(int number) {
        return Long.valueOf(String.format("0x%08x",number));
    }

    /**
     * 10 进制数字 转为 16进制
     * @param number
     * @return  0x0037d70d
     */
    public static long getFourSixteen(int number) {
        return Long.valueOf(String.format("0x%04x",number));
    }

    /**
     * 16进制 转为10进制
     * @param number
     * @return
     */
    public static int getDecimalism(long number) {
        int valueHex = (int)number;
        return valueHex;
    }

    /**
     * 16进制 转为10进制
     * @param number
     * @return
     */
    public static int getDecimalism(String  number) {
        return Integer.parseInt(number,16);
    }

    /**
     * 获取日期的 年 月 日 数
     * @param dateTime
     * @param index
     * @return
     */
    public static int getYMDValue(Date dateTime, Integer index) {
        switch (index) {
            case 1:
                return getDecimalism(DateTimeUtils.getYmdhmsStringValue(dateTime, DateTimeUtils.YEAR).substring(0,2));
            case 2:
                return getDecimalism(DateTimeUtils.getYmdhmsStringValue(dateTime, DateTimeUtils.YEAR).substring(2,4));
            case 3:
                return getDecimalism(DateTimeUtils.getYmdhmsStringValue(dateTime, DateTimeUtils.MONTH));
            case 4:
                return getDecimalism(DateTimeUtils.getYmdhmsStringValue(dateTime, DateTimeUtils.DAY));
            default:
                return 0;
        }
    }

}
