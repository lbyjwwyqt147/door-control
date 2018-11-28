package com.jwell.doorcontrol.service.command;

import com.jwell.boot.utilscommon.exception.DescribeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/***
 *  读取控制器的IP和接收服务器IP和端口信息 指令
 * @author ljy
 */
@Component(value = "readControllerIpPortCommand")
@Log4j2
public class ReadControllerIpPortCommand extends AbstractSendCommand {

    /**
     * 读取控制器IP和接收服务器的IP和端口
     * controllerSN  要读取的控制器设备序列号
     * @param wgControllerInfo
     * @return
     */
    @Override
    public AtomicInteger commandExecute(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setModule((byte) 10);
        this.moduleDescription = " 读取控制器IP和接收服务器的IP和端口 ";
        this.successPromptMessage = "  读取控制器IP和接收服务器的IP和端口 成功.";
        this.failPromptMessage = "  读取控制器IP和接收服务器的IP和端口 失败.";
        try {
            this.wgUdpCommShort = new WgUdpCommShort();
            wgUdpCommShort.resetData();
            // 读取接收服务器的IP和端口 [功能号: 0x92]
            wgUdpCommShort.setFunctionId((byte) 0x92);
            return super.commandExecute(wgControllerInfo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
    }
}
