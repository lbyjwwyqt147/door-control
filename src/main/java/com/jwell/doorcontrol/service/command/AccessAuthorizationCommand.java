package com.jwell.doorcontrol.service.command;

import com.jwell.boot.utilscommon.exception.DescribeException;
import com.jwell.doorcontrol.utils.DenaryConvertUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/***
 *  权限指令
 * @author ljy
 */
@Component(value = "accessAuthorizationCommand")
@Log4j2
public class AccessAuthorizationCommand extends AbstractSendCommand {

    @Override
    public AtomicInteger commandExecute(WgControllerInfo wgControllerInfo) {
        switch (wgControllerInfo.getMark()) {
            case 0:
                return uploadAuthorization(wgControllerInfo);
            case 1:
                return uploadAuthorization(wgControllerInfo);
            case 2:
                return deleteAuthorization(wgControllerInfo);
            case 5:
                return clearAuthorization(wgControllerInfo);
            case 4:
                return queryAuthorization(wgControllerInfo);
            case 6:
                return batchUploadAuthorization(wgControllerInfo);
            default:
                return new AtomicInteger(0);
        }
    }

    /**
     * 上传权限到控制板上
     * 1.11	权限添加或修改[功能号: 0x50]
     * controllerSN  控制器设备序列号
     * cardNo 卡号
     * @param wgControllerInfo
     * @return
     */
    private AtomicInteger uploadAuthorization(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setModule((byte) 1);
        this.moduleDescription = "上传权限";
        this.successPromptMessage = " 上传权限成功.";
        this.failPromptMessage = " 上传权限失败.";
        try {
            this.wgUdpCommShort = new WgUdpCommShort();
            wgUdpCommShort.resetData();
            // 1.11	权限添加或修改[功能号: 0x50]
            wgUdpCommShort.setFunctionId((byte) 0x50);
            byte[] datas = new byte[56];
            // 卡号
            long cardNOOfPrivilege = wgControllerInfo.getCardNo();;
            System.arraycopy(WgUdpCommShort.longToByte(cardNOOfPrivilege) , 0, datas, 0, 4);
            //20 10 01 01 起始日期:  2010年01月01日   (必须大于2001年)
            datas[4] = (byte)DenaryConvertUtil.getYMDValue(wgControllerInfo.getStartTime(), 1);
            datas[5] = (byte)DenaryConvertUtil.getYMDValue(wgControllerInfo.getStartTime(), 2);
            datas[6] = (byte)DenaryConvertUtil.getYMDValue(wgControllerInfo.getStartTime(), 3);
            datas[7] = (byte)DenaryConvertUtil.getYMDValue(wgControllerInfo.getStartTime(), 4);
            //20 29 12 31 截止日期:  2029年12月31日
            datas[8] = (byte)DenaryConvertUtil.getYMDValue(wgControllerInfo.getEndTime(), 1);
            datas[9] = (byte)DenaryConvertUtil.getYMDValue(wgControllerInfo.getEndTime(), 2);
            datas[10] = (byte)DenaryConvertUtil.getYMDValue(wgControllerInfo.getEndTime(), 3);
            datas[11] = (byte)DenaryConvertUtil.getYMDValue(wgControllerInfo.getEndTime(), 4);
            //01 允许通过 一号门 [对单门, 双门, 四门控制器有效]
            datas[12] = 0x01;
            //01 允许通过 二号门 [对双门, 四门控制器有效]  如果禁止2号门, 则只要设为 0x00
            datas[13] = 0x01;
            //01 允许通过 三号门 [对四门控制器有效]
            datas[14] = 0x01;
            //01 允许通过 四号门 [对四门控制器有效]
            datas[15] = 0x01;
            wgUdpCommShort.setData(datas);
            return super.commandExecute(wgControllerInfo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
    }

    /**
     * 单个删除控制板上权限
     * 1.12	权限删除(单个删除)[功能号: 0x52]
     * controllerSN  控制器设备序列号
     * cardNo 卡号
     * @param wgControllerInfo
     * @return
     */
    private AtomicInteger deleteAuthorization(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setModule((byte) 2);
        this.moduleDescription = "删除权限";
        this.successPromptMessage = " 删除权限成功.";
        this.failPromptMessage = " 删除权限失败.";
        try {
            this.wgUdpCommShort = new WgUdpCommShort();
            wgUdpCommShort.resetData();
            // 1.12	权限删除(单个删除)[功能号: 0x52]
            wgUdpCommShort.setFunctionId((byte) 0x52);
            byte[] datas = new byte[56];
            // 卡号
            long cardNOOfPrivilegeToDelete = wgControllerInfo.getCardNo();
            System.arraycopy(WgUdpCommShort.longToByte(cardNOOfPrivilegeToDelete) , 0, datas, 0, 4);
            wgUdpCommShort.setData(datas);
            return super.commandExecute(wgControllerInfo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
    }

    /**
     * 权限清空(全部清掉)控制板上权限
     * 11.13	权限清空(全部清掉)[功能号: 0x54]
     * controllerSN  控制器设备序列号
     * @param wgControllerInfo
     * @return
     */
    private AtomicInteger clearAuthorization(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setModule((byte) 3);
        this.moduleDescription = "清空权限";
        this.successPromptMessage = " 清空权限成功.";
        this.failPromptMessage = " 清空权限失败.";
        try {
            this.wgUdpCommShort = new WgUdpCommShort();
            wgUdpCommShort.resetData();
            // 11.13	权限清空(全部清掉)[功能号: 0x54]
            wgUdpCommShort.setFunctionId((byte) 0x54);
            byte[] datas = new byte[56];
            //12	标识(防止误设置)	1	0x55 [固定]
            System.arraycopy(WgUdpCommShort.longToByte(WgUdpCommShort.SPECIAL_FLAG) , 0, datas, 0, 4);
            wgUdpCommShort.setData(datas);
            return super.commandExecute(wgControllerInfo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
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
    private AtomicInteger queryAuthorization(WgControllerInfo wgControllerInfo) {
        AtomicInteger success = new AtomicInteger(0);
        wgControllerInfo.setModule((byte) 4);
        this.moduleDescription = "权限查询";
        this.successPromptMessage = " 权限查询成功.";
        this.failPromptMessage = " 权限查询失败.";
        // 最后收到的数据包
        byte[] recvBuff;
        try {
            this.getLogMessage(wgControllerInfo);
            this.wgUdpCommShort = new WgUdpCommShort();
            wgUdpCommShort.resetData();
            // 1.15	权限查询[功能号: 0x5A]
            wgUdpCommShort.setFunctionId((byte) 0x5A);
            byte[] datas = new byte[56];
            // 卡号
            long cardNOOfPrivilegeToQuery = wgControllerInfo.getCardNo();
            System.arraycopy(WgUdpCommShort.longToByte(cardNOOfPrivilegeToQuery) , 0, datas, 0, 4);
            wgUdpCommShort.setData(datas);
            wgUdpCommShort.setControllerSN(wgControllerInfo.getControllerSN());
            recvBuff = wgUdpCommShort.run();
            if (recvBuff != null) {
                long cardNOOfPrivilegeToGet = WgUdpCommShort.getLongByByte(recvBuff,8, 4);
                String msg = null;
                if (cardNOOfPrivilegeToGet == 0) {
                    //没有权限时: (卡号部分为0)
                     msg = " 没有权限信息: (卡号部分为0)";
                    log .info(msg);
                } else {
                    //具体权限信息...
                    msg = " 有权限信息 .....";
                    log .info(msg);
                    success.set(2);
                }
                log .info(this.logMessage.append(this.successPromptMessage + "\t " + msg).toString());
                if (success.get() != 2) {
                    success.set(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
        return success;
    }


    /**
     * 批量上传权限到控制器
     * 1.21	权限按从小到大顺序添加[功能号: 0x56]
     * 此功能实现 完全更新全部权限, 用户不用清空之前的权限. 只是将上传的权限顺序从第1个依次到最后一个上传完成. 如果中途中断的话, 仍以原权限为主
     * 建议权限数更新超过50个, 即可使用此指令
     * 如果权限数超过8万时, 中途中断的话, 权限会为空. 所以要上传完整
     *
     * controllerSN  控制器SN
     * cardNoList  -组卡号
     * @param wgControllerInfo
     * @return
     */
    private AtomicInteger batchUploadAuthorization(WgControllerInfo wgControllerInfo) {
        AtomicInteger success = new AtomicInteger(0);
        log.info("权限按从小到大顺序添加[功能号: 0x56]\t开始........ [采用1024字节指令, 每次上传16个权限]");
        wgControllerInfo.setModule((byte) 5);
        this.moduleDescription = "批量上传权限";
        this.successPromptMessage = " 批量上传权限成功.";
        this.failPromptMessage = " 批量上传权限失败.";
        // 最后收到的数据包
        byte[] recvBuff;
        try {
            this.getLogMessage(wgControllerInfo);
            List<Integer> cardNoList = wgControllerInfo.getCardNoList();
            if (!cardNoList.isEmpty()) {
                //按从小到大排序
                Collections.sort(cardNoList);
                // 总卡号数
                int cardCount = cardNoList.size();
                byte[] command1024 = new byte[1024];
                int i = 0;
                for (Integer cardNo : cardNoList) {
                    for (int j = 0; j < 1024; j++) {
                        //复位
                        command1024[j] = 0;
                    }
                    this.wgUdpCommShort = new WgUdpCommShort();
                    for (int j = 0; j < 1024; j = j + 64) {
                        if (i >= cardCount) {
                            break;
                        }
                        wgUdpCommShort.resetData();
                        // 1.11	权限添加或修改[功能号: 0x50]
                        wgUdpCommShort.setFunctionId((byte) 0x56);
                        byte[] datas = new byte[56];

                        // 卡号
                        long cardNOOfPrivilege = cardNo;
                        System.arraycopy(WgUdpCommShort.longToByte(cardNOOfPrivilege), 0, datas, 0, 4);

                        //20 10 01 01 起始日期:  2010年01月01日   (必须大于2001年)
                        datas[4] = (byte) DenaryConvertUtil.getYMDValue(wgControllerInfo.getStartTime(), 1);
                        datas[5] = (byte) DenaryConvertUtil.getYMDValue(wgControllerInfo.getStartTime(), 2);
                        datas[6] = (byte) DenaryConvertUtil.getYMDValue(wgControllerInfo.getStartTime(), 3);
                        datas[7] = (byte) DenaryConvertUtil.getYMDValue(wgControllerInfo.getStartTime(), 4);
                        //20 29 12 31 截止日期:  2029年12月31日
                        datas[8] = (byte) DenaryConvertUtil.getYMDValue(wgControllerInfo.getEndTime(), 1);
                        datas[9] = (byte) DenaryConvertUtil.getYMDValue(wgControllerInfo.getEndTime(), 2);
                        datas[10] = (byte) DenaryConvertUtil.getYMDValue(wgControllerInfo.getEndTime(), 3);
                        datas[11] = (byte) DenaryConvertUtil.getYMDValue(wgControllerInfo.getEndTime(), 4);
                        //01 允许通过 一号门 [对单门, 双门, 四门控制器有效]
                        datas[12] = 0x01;
                        //01 允许通过 二号门 [对双门, 四门控制器有效]  如果禁止2号门, 则只要设为 0x00
                        datas[13] = 0x01;
                        //01 允许通过 三号门 [对四门控制器有效]
                        datas[14] = 0x01;
                        //01 允许通过 四号门 [对四门控制器有效]
                        datas[15] = 0x01;
                        wgUdpCommShort.setData(datas);

                        // 总的权限数
                        System.arraycopy(WgUdpCommShort.longToByte(cardCount), 0, wgUdpCommShort.getData(), 32 - 8, 4);
                        int i2 = i + 1;
                        // 当前权限的索引位(从1开始)
                        System.arraycopy(WgUdpCommShort.longToByte(i2), 0, wgUdpCommShort.getData(), 35 - 8, 4);
                        System.arraycopy(wgUdpCommShort.toByte(), 0, command1024, j, 64);
                        i++;
                    }
                    recvBuff = wgUdpCommShort.run(command1024);
                    if (recvBuff != null) {
                        if (WgUdpCommShort.getIntByByte(recvBuff[8]) == 1) {
                            success.set(1);
                        } else	if (WgUdpCommShort.getIntByByte(recvBuff[8]) == 0xE1) {
                            log.info("权限按从小到大顺序添加[功能号: 0x56]	 =0xE1 表示卡号没有从小到大排序...???");
                            break;
                        } else {
                            log.info("权限按从小到大顺序添加[功能号: 0x56]	??? recvBuff[8]=" + recvBuff[8]);
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (success.get() == 1) {
                    log.info("权限按从小到大顺序添加[功能号: 0x56]	 成功...");
                } else {
                    log.info("权限按从小到大顺序添加[功能号: 0x56]	 失败....");
                }
            } else {
                log.info("缺少卡号..");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
        return new AtomicInteger(0);
    }

}
