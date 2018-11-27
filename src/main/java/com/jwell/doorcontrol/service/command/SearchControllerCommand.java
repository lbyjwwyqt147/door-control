package com.jwell.doorcontrol.service.command;

import com.jwell.boot.utilscommon.exception.DescribeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/***
 * 搜索控制器 指令
 * @author ljy
 */
@Component(value = "searchControllerCommand")
@Log4j2
public class SearchControllerCommand extends AbstractSendCommand {

    @Override
    public AtomicInteger commandExecute(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setModule((byte) 8);
        this.moduleDescription = "搜索控制器";
        this.successPromptMessage = " 搜索控制器成功.";
        this.failPromptMessage = " 搜索控制器失败.";
        try {
            this.wgUdpCommShort = new WgUdpCommShort();
            wgUdpCommShort.resetData();
            // 搜索控制器 [功能号: 0x94]
            wgUdpCommShort.setFunctionId((byte) 0x94);
            wgUdpCommShort.run();
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
        return null;

    }
}
