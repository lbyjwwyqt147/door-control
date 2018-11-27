package com.jwell.doorcontrol.service.command;

import com.jwell.boot.utilscommon.exception.DescribeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/***
 * 远程开门
 * @author  ljy
 */
@Component(value = "remoteOpenDoorCommand")
@Log4j2
public class RemoteOpenDoorCommand extends AbstractSendCommand {

    /**
     * 执行远程开门指令
     *  1.10	远程开门[功能号: 0x40]
     * @param wgControllerInfo
     * @return
     */
    @Override
    public AtomicInteger commandExecute(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setModule((byte) 0);
        this.moduleDescription = "远程开门";
        this.successPromptMessage = " 远程开门成功.";
        this.failPromptMessage = " 远程开门失败.";
        try {
            this.wgUdpCommShort = new WgUdpCommShort();
            wgUdpCommShort.resetData();
            // 1.10	远程开门[功能号: 0x40]
            wgUdpCommShort.setFunctionId((byte) 0x40);
            byte[] datas = new byte[56];
            datas[0] =(byte) (wgControllerInfo.getDoorNo() & 0xff);
            wgUdpCommShort.setData(datas);
            return super.commandExecute(wgControllerInfo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
    }
}
