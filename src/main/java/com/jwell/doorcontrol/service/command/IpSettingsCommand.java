package com.jwell.doorcontrol.service.command;

import com.jwell.boot.utilscommon.exception.DescribeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * IP 设置命令
 * @author ljy
 */
@Log4j2
@Component(value = "ipSettingsCommand")
public class IpSettingsCommand extends AbstractSendCommand {

    /**
     * 设置控制器IP和接收服务器的IP和端口
     *  controllerSN  要设置的控制器设备序列号
     *  controllerIp  要设置的控制器设备IP 地址
     *  watchServerIp 要设置的接收服务器的IP
     *  watchServerPort 要设置的接收服务器端口
     * @param wgControllerInfo
     * @return
     */
    @Override
    public AtomicInteger commandExecute(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setModule((byte) 9);
        this.moduleDescription = " 设置控制器IP和接收服务器的IP和端口 ";
        this.successPromptMessage = "  设置控制器IP和接收服务器的IP和端口 成功.";
        this.failPromptMessage = "  设置控制器IP和接收服务器的IP和端口 失败.";
        try {
            this.wgUdpCommShort = new WgUdpCommShort();
            wgUdpCommShort.resetData();
            // 1.18	设置接收服务器的IP和端口 [功能号: 0x90]
            wgUdpCommShort.setFunctionId((byte) 0x90);
            byte[] datas = new byte[56];
            String[] ip = wgControllerInfo.getWatchServerIp().split("\\.");
            if (ip.length == 4) {
                datas[0] =  (byte)Integer.parseInt(ip[0]);
                datas[1] =  (byte)Integer.parseInt(ip[1]);
                datas[2] =  (byte)Integer.parseInt(ip[2]);
                datas[3] =  (byte)Integer.parseInt(ip[3]);
            }
            //接收服务器的端口: 61005
            datas[4] =(byte)(wgControllerInfo.getWatchServerPort() & 0xff);
            datas[5] =(byte)((wgControllerInfo.getWatchServerPort() >>8) & 0xff);
            //每隔5秒发送一次: 05 (定时上传信息的周期为5秒 [正常运行时每隔5秒发送一次  有刷卡时立即发送])
            datas[6] = 5;
            wgUdpCommShort.setData(datas);
            return super.commandExecute(wgControllerInfo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
    }
}
