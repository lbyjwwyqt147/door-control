package com.jwell.doorcontrol.service.qrode;

import org.springframework.http.ResponseEntity;

/***
 * 闸机生成二维码 service
 * @author ljy
 */
public interface QRCodeService {

    /**
     * 获取闸机二维码数据内容
     * @param cardNO   卡号
     * @param startTime  二维码有效时间起
     * @param endTime  二维码有效时间止
     * @return
     */
    String getCodeDateContent(long cardNO, String startTime, String endTime );

    /**
     * 生成二维码图片
     * @param content 二维码内容
     * @param width  图片宽度
     * @param height 图片高度
     * @return
     */
    ResponseEntity<byte[]> generateImage(String content, Integer width, Integer height );

    /**
     * 生成闸机二维码图片
     * @param cardNo   卡号
     * @param startTime  二维码有效时间起
     * @param endTime  二维码有效时间止
     * @param width  图片宽度
     * @param height 图片高度
     * @return
     */
    ResponseEntity<byte[]> gateCodeImage(long cardNo, String startTime, String endTime, Integer width, Integer height );
}
