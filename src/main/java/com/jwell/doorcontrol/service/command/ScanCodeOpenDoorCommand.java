package com.jwell.doorcontrol.service.command;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/***
 *  扫码开门
 * @author ljy
 */
@Component(value = "scanCodeOpenDoorCommand")
public class ScanCodeOpenDoorCommand {

    @Resource(name = "remoteOpenDoorCommand")
    private AbstractSendCommand remoteOpenDoorCommand;
    @Resource(name = "accessAuthorizationCommand")
    private AbstractSendCommand accessAuthorizationCommand;

    public void openDoor(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setMark((byte) 4);
        // 大于 0 表示有开门权限  等于 0 表示没有开门权限
        if (accessAuthorizationCommand.commandExecute(wgControllerInfo).get() == 2 ) {
            // 执行远程开门
            remoteOpenDoorCommand.commandExecute(wgControllerInfo);
        }
    }

}
