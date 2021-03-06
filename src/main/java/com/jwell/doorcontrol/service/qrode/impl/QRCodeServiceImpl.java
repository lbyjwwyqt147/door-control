package com.jwell.doorcontrol.service.qrode.impl;

import com.google.zxing.WriterException;
import com.jwell.boot.utilscommon.utils.QRcodeUtils;
import com.jwell.doorcontrol.service.command.AbstractSendCommand;
import com.jwell.doorcontrol.service.qrode.QRCodeService;
import com.jwell.doorcontrol.utils.QRCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;


/***
 *  闸机二维码生成 ServiceImpl
 * @author ljy
 *
 */
@Service
public class QRCodeServiceImpl implements QRCodeService {

    @Resource(name = "accessAuthorizationCommand")
    private AbstractSendCommand accessAuthorizationCommand;


    @Override
    public String getCodeDateContent(long cardNO, String startTime, String endTime) {
        return QRCode.getCdoeDataContent(cardNO, startTime, endTime);
    }

    @Override
    public ResponseEntity<byte[]> generateImage(String content, Integer width, Integer height) {
        try {
            return QRcodeUtils.getResponseEntity(content,width != null ? width : 300, height != null ? height : 300, "png" );
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ResponseEntity<byte[]> gateCodeImage(long cardNo, String startTime, String endTime, Integer width, Integer height) {
        try {

            // 二维码数据内容
            String content = QRCode.getCdoeDataContent(cardNo, startTime, endTime);
            return QRcodeUtils.getResponseEntity(content,width != null ? width : 300, height != null ? height : 300, "png" );
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
