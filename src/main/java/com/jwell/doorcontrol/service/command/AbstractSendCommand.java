package com.jwell.doorcontrol.service.command;


import com.jwell.boot.utilscommon.exception.DescribeException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

/***
 *  执行控制器命令 抽象模板方法
 *  @author  ljy
 */
@Log4j2
public abstract class AbstractSendCommand {

    protected WgUdpCommShort wgUdpCommShort;
    /** 成功提示信息  */
    protected String successPromptMessage;
    /** 失败提示信息 */
    protected String failPromptMessage;
    /** 功能模块描述 */
    protected String moduleDescription;
    protected  StringBuffer logMessage = new StringBuffer();

    /**
     *  执行命令的模板方法
     * @param wgControllerInfo
     * @return
     */
    public  AtomicInteger commandExecute(WgControllerInfo wgControllerInfo) {
        AtomicInteger success = new AtomicInteger(0);
        // 最后收到的数据包
        byte[] recvBuff;
        this.getLogMessage(wgControllerInfo);
        try {
            wgUdpCommShort.setControllerSN(wgControllerInfo.getControllerSN());
            recvBuff = wgUdpCommShort.run();
            if (recvBuff != null) {
                if (WgUdpCommShort.getIntByByte(recvBuff[8]) == 1) {
                    success.set(1);
                    log.info(logMessage.append(successPromptMessage).toString());
                    this.displayRecordInfo(recvBuff);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.info(logMessage.append(failPromptMessage).toString());
            throw  new DescribeException(failPromptMessage);
        }
        return success;
    }

    /**
     * 日志信息
     * @param wgControllerInfo
     * @return
     */
    protected void getLogMessage(WgControllerInfo wgControllerInfo) {
        logMessage.setLength(0);
        logMessage.append("功能描述：").append(moduleDescription);
        logMessage.append("，控制器SN = ").append(wgControllerInfo.getControllerSN());
        logMessage.append(", 卡号 = ").append(wgControllerInfo.getCardNo() == 0 ? "无" : wgControllerInfo.getCardNo());
        logMessage.append(", 门号 = ").append(wgControllerInfo.getDoorNo() == null || wgControllerInfo.getDoorNo() == 0 ? "无" : wgControllerInfo.getDoorNo());
        log.info(logMessage.toString());
    }

    /**
     * 显示控制器日志纪录
     * @param recvBuff 从控制器上收到的数据包
     */
    protected  void displayRecordInfo(byte[] recvBuff) {
        WgUdpClient.displayRecordInformation(recvBuff);
    }

}
