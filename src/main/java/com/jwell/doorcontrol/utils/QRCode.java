package com.jwell.doorcontrol.utils;

import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/***
 *  根据微耕二维码规则生成二维码内容数据
 * @author ljy
 */
@Log4j2
public class QRCode {

    /**
     * @param args
     */
    public static void main(String[] args) {
        byte[] pwdData=
                {
                        (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37,
                        (byte)0x38, (byte)0x39, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x45, (byte)0x46
                }; //测试密码0123456789ABCDEF
        //String infoA =	createQR(12345,pwdData);
        // infoA =	createQR(153139810,pwdData);
        String infoA =	createQR(2482799616L,pwdData,"2018-11-22 13:20:00", "2018-11-22 14:20:00");
        log("加密后的二维码数据:" + infoA);
    }


    /**
     * 生成二维码数据内容
     * @param cardNO
     * @param startTime
     * @param endTime
     * @return
     */
    public static String getCdoeDataContent(long cardNO, String startTime, String endTime) {
        //测试密码0123456789ABCDEF
        byte[] pwdData=
                {
                        (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37,
                        (byte)0x38, (byte)0x39, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x45, (byte)0x46
                };
        String code =	createQR(cardNO, pwdData, startTime, endTime);
        log("加密后的二维码数据:" + code);
        return code;
    }

    public static void log(String info) {
        System.out.println(info);
        log.info(info);
    }


    private static  long getYMD(int Year,int Month, int Day) {
        long ymd;
        ymd = (Year % 100) << 9;
        ymd += (Month << 5);
        ymd += (Day);
        return (long)ymd;
    }

    private static long getHMS(int Hour,int Minute, int Second) {
        long hms = 0;
        hms += ((Second >> 1));
        hms += Minute << (5);
        hms += Hour << (11);
        return hms;
    }


    /**
     * 生成二维码数据
     * @param cardNO   卡号
     * @param pwdData  二维码规则密码
     * @param startTime 二维码有效时间起
     * @param endTime  二维码有效时间止
     * @return
     */
    private static String createQR(long cardNO, byte[] pwdData, String startTime, String endTime) {
        byte[] sendData=
                {
                        (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                        (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x00, (byte)0x00
                };
        sendData[15] = 2; //固定
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            now.setTime(sdf.parse(startTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //起始日期时间 2010年1月1日
        long ymd = getYMD(now.get(Calendar.YEAR),now.get(Calendar.MONTH) + 1,now.get(Calendar.DAY_OF_MONTH));
        long hms = getHMS(now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),now.get(Calendar.SECOND));
        sendData[8] = (byte)(ymd & 0xff);
        sendData[9] = (byte)((ymd >> 8) & 0xff);
        sendData[10] = (byte)((hms >> 8) & 0xff); //高位
        sendData[11] = (byte)(hms & 0xE0); //低位

        try {
            now.clear();
            now.setTime(sdf.parse(endTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //截止日期时间 2029年12月31日 23:59:59
        ymd =  getYMD(now.get(Calendar.YEAR),now.get(Calendar.MONTH) + 1,now.get(Calendar.DAY_OF_MONTH));
        hms = getHMS(now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),now.get(Calendar.SECOND));
        sendData[11] = (byte)(sendData[11] + ((hms & 0xE0) >> 4));
        sendData[12] = (byte)(ymd & 0xff);
        sendData[13] = (byte)((ymd >> 8) & 0xff);
        sendData[14] = (byte)((hms >> 8) & 0xff);
        if ((hms & 0xE0) == 0xE0) //2018-09-20 19:17:39 修改部分
        {
            sendData[14] = (byte)(sendData[14] + 1);
        }


        if (cardNO > 0) {
            sendData[0] = (byte)(cardNO & 0xff);
            sendData[1] = (byte)((cardNO>>8) & 0xff);
            sendData[2] = (byte)((cardNO>>16) & 0xff);
            sendData[3] = (byte)((cardNO>>24) & 0xff);
            sendData[4] = (byte)((cardNO>>32) & 0xff);
            sendData[5] = (byte)((cardNO>>40) & 0xff);
            sendData[6] = (byte)((cardNO>>48) & 0xff);
            sendData[7] = (byte)((cardNO>>56) & 0xff);
            sendData[7] = crc8(sendData, 7); //2017-12-18 22:03:47 校验和
        }
        byte[] enOut = SM4.encode16(sendData, pwdData);
        String info = "";
        for(int i=0; i<enOut.length; i++)
        {
            info=info+  String.format("%02X",enOut[i]);
        }
        return info;
    }
    public static byte GetHex(int val) {
        return (byte)((val % 10) + (((val -(val % 10)) / 10)%10) *16);
    }

    public static  byte crc8(byte[] buf, int len) {
        byte i, j, crc;
        crc = 0;
        for (j = 0; j < len; j++) {
            crc = (byte)(crc ^ (buf[j]));
            for (i = 8; i > 0; i--) {
                if ((crc & 0x80) > 0) {
                    crc = (byte)((crc << 1) ^ 0x31);  //CRC=X8+X5+X4+1
                } else {
                    crc = (byte)(crc << 1);
                }
            }
            //buf++;
        }
        return crc;
    }


}
