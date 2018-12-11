package com.jwell.doorcontrol.dto;

/***
 *  redis key
 * @author ljy
 */
public class RedisKeyDto {
    /** 微耕闸机控制器 sn */
    public static final String WEI_GENG_CONTROL_KEY = "door:control";
    /** 微耕闸机二维码 信息 */
    public static final String GATE_QR_CODE_KEY = "door:control:qrcode";
    /** 微耕闸机二维码使用次数 信息 */
    public static final String GATE_QR_CODE_FREQUENCY_KEY = "door:control:qrcode:frequency";
}
