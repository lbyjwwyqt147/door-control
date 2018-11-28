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

    @Resource(name = "accessAuthorizationCommand")
    private AbstractSendCommand accessAuthorizationCommand;

    /**
     *  远程开门
     *  controllerSN  控制器设备序列号
     *  doorNO  门号
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
     * 提取控制器上的纪录
     *  controllerSN  控制器设备序列号
     *  bForceGetAllSwipe  >=1  强制提取所有记录(包括之前已提取的), == 0 表示 提取新的记录(不含 已提取过的)
     * @param wgControllerInfo
     * @return
     */
    @RequestMapping(value = "command/extractRecords", method = RequestMethod.GET)
    @ApiVersion(1)
    public ResultInfo extractRecords(WgControllerInfo wgControllerInfo) {
        boolean success = accessToRecordsCommand.commandExecute(wgControllerInfo).get() > 0 ? true : false;
        return ResultUtil.restfulInfo(success);
    }

    /**
     * 上传权限到控制器上
     * 1.11	权限添加或修改[功能号: 0x50]
     * controllerSN  控制器设备序列号
     * cardNo 卡号
     * startTime 有效时间起
     * endTime  有效时间止
     * @param wgControllerInfo
     * @return
     */
    @RequestMapping(value = "command/auth/upload", method = RequestMethod.POST)
    @ApiVersion(1)
    public ResultInfo uploadAuthorization(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setMark((byte) 0);
        boolean success = accessAuthorizationCommand.commandExecute(wgControllerInfo).get() > 0 ? true : false;
        return ResultUtil.restfulInfo(success);
    }


    /**
     * 单个删除控制器上权限
     * 1.12	权限删除(单个删除)[功能号: 0x52]
     * controllerSN  控制器设备序列号
     * cardNo 卡号
     * @param wgControllerInfo
     * @return
     */
    @RequestMapping(value = "command/auth/delete", method = RequestMethod.POST)
    @ApiVersion(1)
    public ResultInfo deleteAuthorization(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setMark((byte) 2);
        boolean success = accessAuthorizationCommand.commandExecute(wgControllerInfo).get() > 0 ? true : false;
        return ResultUtil.restfulInfo(success);
    }

    /**
     * 权限清空(全部清掉)控制板上权限
     * 11.13	权限清空(全部清掉)[功能号: 0x54]
     * controllerSN  控制器设备序列号
     * @param wgControllerInfo
     * @return
     */
    @RequestMapping(value = "command/auth/clear", method = RequestMethod.POST)
    @ApiVersion(1)
    public ResultInfo clearAuthorization(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setMark((byte) 5);
        boolean success = accessAuthorizationCommand.commandExecute(wgControllerInfo).get() > 0 ? true : false;
        return ResultUtil.restfulInfo(success);
    }

    /**
     * 权限查询
     * 1.15	权限查询[功能号: 0x5A]
     * controllerSN  控制器SN
     * cardNo 卡号
     * @param wgControllerInfo
     * @param
     * @return
     */
    @RequestMapping(value = "command/auth/query", method = RequestMethod.GET)
    @ApiVersion(1)
    public ResultInfo queryAuthorization(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setMark((byte) 4);
        boolean success = accessAuthorizationCommand.commandExecute(wgControllerInfo).get() > 0 ? true : false;
        return ResultUtil.restfulInfo(success);
    }

    /**
     * 批量上传权限到控制器
     * 1.15	权限查询[功能号: 0x5A]
     * controllerSN  控制器SN
     * cardNoList  -组卡号
     * startTime 有效时间起
     * endTime  有效时间止
     * @param wgControllerInfo
     * @param
     * @return
     */
    @RequestMapping(value = "command/auth/batchUpload", method = RequestMethod.POST)
    @ApiVersion(1)
    public ResultInfo batchUploadAuthorization(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setMark((byte) 6);
        boolean success = accessAuthorizationCommand.commandExecute(wgControllerInfo).get() > 0 ? true : false;
        return ResultUtil.restfulInfo(success);
    }
}
