package com.jwell.doorcontrol.controller;

import com.jwell.boot.utilscommon.annotation.ApiVersion;
import com.jwell.boot.utilscommon.controller.BaseController;
import com.jwell.doorcontrol.service.qrode.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/***
 * 生成二维码 Controller
 * @author ljy
 */
@RestController
public class QRCodeController extends BaseController {

    @Autowired
    private QRCodeService codeService;

    /**
     * 生成二维码图片
     * @param content 二维码内容
     * @param width  图片宽度
     * @param height 图片高度
     * @return
     */
    @RequestMapping(value = "code/image")
    @ApiVersion(1)
    public ResponseEntity<byte[]> generateImage(String content, Integer width, Integer height ) {
       return codeService.generateImage(content, width, height);
    }

    /**
     * 生成闸机二维码图片
     * @param cardNo   卡号
     * @param startTime  二维码有效时间起
     * @param endTime  二维码有效时间止
     * @param width  图片宽度
     * @param height 图片高度
     * @return
     */
    @RequestMapping(value = "code/gate/image")
    @ApiVersion(1)
    public ResponseEntity<byte[]> gateCodeImage(long cardNo, String startTime, String endTime, Integer width, Integer height ) {
        return codeService.gateCodeImage(cardNo, startTime, endTime, width, height);
    }

}
