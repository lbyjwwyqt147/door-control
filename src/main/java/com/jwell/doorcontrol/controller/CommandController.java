package com.jwell.doorcontrol.controller;

import com.jwell.boot.utilscommon.annotation.ApiVersion;
import com.jwell.boot.utilscommon.controller.BaseController;
import com.jwell.boot.utilscommon.restful.ResultInfo;
import com.jwell.boot.utilscommon.restful.ResultUtil;
import com.jwell.doorcontrol.service.command.AbstractSendCommand;
import com.jwell.doorcontrol.service.command.WgControllerInfo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/***
 *  下发指令给控制器 controler
 * @author ljy
 */
@RestController
public class CommandController extends BaseController {

    @Resource(name = "remoteOpenDoorCommand")
    private AbstractSendCommand remoteOpenDoorCommand;

    @Resource(name = "accessToRecordsCommand")
    private AbstractSendCommand accessToRecordsCommand;
    @Resource(name = "searchControllerCommand")
    private AbstractSendCommand searchControllerCommand;

    /**
     * 远程开门
     * @param wgControllerInfo
     * @return
     */
    @RequestMapping(value = "command/remoteOpenDoor", method = RequestMethod.POST)
    @ApiVersion(1)
    public ResultInfo remoteOpenDoor(WgControllerInfo wgControllerInfo) {
        boolean success = remoteOpenDoorCommand.commandExecute(wgControllerInfo).get() > 0 ? true : false;
        return ResultUtil.restfulInfo(success);
    }

    /**
     * 搜索控制器
     * @param wgControllerInfo
     * @return
     */
    @RequestMapping(value = "command/searchController", method = RequestMethod.GET)
    @ApiVersion(1)
    public ResultInfo searchController(WgControllerInfo wgControllerInfo) {
        boolean success = searchControllerCommand.commandExecute(wgControllerInfo).get() > 0 ? true : false;
        return ResultUtil.restfulInfo(success);
    }

    /**
     * 提取控制器上纪录
     * @param wgControllerInfo
     * @return
     */
    @RequestMapping(value = "command/extractRecords", method = RequestMethod.GET)
    @ApiVersion(1)
    public ResultInfo extractRecords(WgControllerInfo wgControllerInfo) {
        boolean success = accessToRecordsCommand.commandExecute(wgControllerInfo).get() > 0 ? true : false;
        return ResultUtil.restfulInfo(success);
    }


}
