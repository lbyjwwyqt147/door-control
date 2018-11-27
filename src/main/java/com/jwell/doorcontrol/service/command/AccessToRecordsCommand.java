package com.jwell.doorcontrol.service.command;

import com.jwell.boot.utilscommon.exception.DescribeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/***
 * 获取纪录信息 指令
 * @author ljy
 */
@Component(value = "accessToRecordsCommand")
@Log4j2
public class AccessToRecordsCommand extends AbstractSendCommand {
    @Override
    public AtomicInteger commandExecute(WgControllerInfo wgControllerInfo) {
        AtomicInteger success = new AtomicInteger(0);
        wgControllerInfo.setModule((byte) 6);
        this.moduleDescription = "提取控制器上的纪录";
        this.successPromptMessage = " 提取控制器上纪录成功.";
        this.failPromptMessage = " 提取控制器上纪录失败.";
        // 最后收到的数据包
        byte[] recvBuff;
        try {
            this.getLogMessage(wgControllerInfo);
            //1.9	提取记录操作
            //1. 通过 0xB0指令 获取最早一条记录索引
            //2. 通过 0xB0指令 获取最后一条记录索引
            //3. 通过 0xB4指令 获取已读取过的记录索引号 recordIndex
            //4. 通过 0xB0指令 获取指定索引号的记录  从recordIndex + 1开始提取记录， 直到记录为空为止
            //5. 通过 0xB2指令 设置已读取过的记录索引号  设置的值为最后读取到的刷卡记录索引号
            //经过上面步骤， 整个提取记录的操作完成
            //第一条记录索引号
            long firstRecordIndex = 0;
            //最后一条记录索引号
            long lastRecordIndex = 0;
            long recordIndexGotToRead = 0x0;
            long recordIndexToGet = 0;
            log.info(" 提取记录操作	 开始...[1024字节指令]");
            this.wgUdpCommShort = new WgUdpCommShort();

            // ################ 获取最早一条记录索引
            wgUdpCommShort.resetData();
            wgUdpCommShort.setFunctionId((byte) 0xB0);
            wgUdpCommShort.setControllerSN(wgControllerInfo.getControllerSN());
            long recordIndexGot4GetSwipe = 0x0;
            recvBuff = wgUdpCommShort.run();
            if (recvBuff != null) {
                firstRecordIndex = WgUdpCommShort.getLongByByte(recvBuff, 8, 4);
                log.info(" 获取最早一条记录索引 = " + String.valueOf(firstRecordIndex));
            }

            // ################ 获取最后一条记录索引
            wgUdpCommShort.resetData();
            wgUdpCommShort.setFunctionId((byte) 0xB0);
            wgUdpCommShort.setControllerSN(wgControllerInfo.getControllerSN());
            byte[] datas = new byte[56];
            //取最后的一条记录索引
            datas[0] =(byte) 0xff;
            //取最后的一条记录索引
            datas[1] =(byte) 0xff;
            //取最后的一条记录索引
            datas[2] =(byte) 0xff;
            //取最后的一条记录索引
            datas[3] =(byte) 0xff;
            wgUdpCommShort.setData(datas);
            recvBuff = wgUdpCommShort.run();
            if (recvBuff != null) {
                lastRecordIndex = WgUdpCommShort.getLongByByte(recvBuff, 8, 4);
                log.info(" 获取最后一条记录索引	 =" + String.valueOf( lastRecordIndex));
            }

            // ############# 获取已读取过的记录索引号
            wgUdpCommShort.resetData();
            wgUdpCommShort.setFunctionId((byte) 0xB4);
            wgUdpCommShort.setControllerSN(wgControllerInfo.getControllerSN());
            recvBuff = wgUdpCommShort.run();
            if (recvBuff != null) {
                recordIndexGotToRead = WgUdpCommShort.getLongByByte(recvBuff, 8, 4);
                log.info(" 获取已读取过的记录索引号	 =" + String.valueOf( recordIndexGotToRead));
            }

            long validRecordsCount = 0;
            if (wgControllerInfo.getBForceGetAllSwipe() > 0) {
                //强制取所有记录
                recordIndexGotToRead = 0;
            }
            if (recvBuff != null) {
                long recordIndexValidGet = 0;
                //准备要提取的记录索引位
                long recordIndexToGetStart = recordIndexGotToRead + 1;
                //超过范围 取第一个记录的索引号
                if (recordIndexGotToRead > lastRecordIndex || recordIndexGotToRead < firstRecordIndex) {
                    recordIndexToGetStart = firstRecordIndex;
                }
                long recordIndexCurrent;
                int cnt = 0;
                recordIndexGot4GetSwipe = recordIndexGotToRead;
                wgUdpCommShort.resetData();
                wgUdpCommShort.setFunctionId((byte) 0xB0);
                wgUdpCommShort.setControllerSN(wgControllerInfo.getControllerSN());
                byte[] command1024 = new byte[1024];
            //    do {
                    for (int j = 0; j < 1024; j++) {
                        //复位
                        command1024[j] = 0;
                    }
                    recordIndexCurrent = recordIndexToGetStart;
                    for (int j = 0; j < 1024; j = j + 64) {
                        wgUdpCommShort.resetData();
                        wgUdpCommShort.setFunctionId((byte) 0xB0);
                        wgUdpCommShort.setControllerSN(wgControllerInfo.getControllerSN());
                        System.arraycopy(WgUdpCommShort.longToByte(recordIndexToGetStart) , 0, wgUdpCommShort.getData(), 0, 4);
                        System.arraycopy(wgUdpCommShort.toByte(), 0, command1024, j, 64);
                        recordIndexToGetStart++;
                        cnt++;
                    }
                    recvBuff = wgUdpCommShort.run(command1024);
                    if (recvBuff != null) {
                        for (int j = 0; j < 1024; j = j + 64) {
                            //12	记录类型
                            //0=无记录
                            //1=刷卡记录
                            //2=门磁,按钮, 设备启动, 远程开门记录
                            //3=报警记录	1
                            //0xFF=表示指定索引位的记录已被覆盖掉了.  请使用索引0, 取回最早一条记录的索引值
                            byte[] recvNew = new byte[64];
                            System.arraycopy(recvBuff, j, recvNew, 0, 64);
                            int recordType = recvNew[12];
                            if (recordType == 0) {
                                //没有更多记录
                                log.info("没有更多的纪录.");
                                break;
                            }
                            if (recordType == 0xff) {
                                //此索引号无效
                                break;
                            }
                            success.set(1);
                            recordIndexValidGet = recordIndexCurrent;
                            recordIndexCurrent++;
                            validRecordsCount++;
                            // 显示前100个, 太多显示处理速度慢 不作分析了...
                            if (validRecordsCount < 100) {
                                super.displayRecordInfo(recvNew);
                                if (validRecordsCount == 99) {
                                    log.info(" 为加快提取速度, 超过100个的  不再显示记录信息.......");
                                }
                            }
                            // ########### 对收到的记录作存储处理

                        }
                    }
               // } while (cnt < 2);

                if (success.get() > 0) {
                    //通过 0xB2指令 设置已读取过的记录索引号  设置的值为最后读取到的刷卡记录索引号
                    wgUdpCommShort.resetData();
                    wgUdpCommShort.setFunctionId((byte) 0xB2);
                    wgUdpCommShort.setControllerSN(wgControllerInfo.getControllerSN());
                    System.arraycopy(WgUdpCommShort.longToByte(recordIndexValidGet) , 0, wgUdpCommShort.getData(), 0, 4);

                    //12	标识(防止误设置)	1	0x55 [固定]
                    System.arraycopy(WgUdpCommShort.longToByte(WgUdpCommShort.SPECIAL_FLAG) , 0, wgUdpCommShort.getData(), 4, 4);

                    recvBuff = wgUdpCommShort.run();
                    if (recvBuff != null) {
                        if (WgUdpCommShort.getIntByByte(recvBuff[8]) == 1) {
                            //显示纪录信息
                            super.displayRecordInfo(recvBuff);
                            //完全提取成功....
                            log.info(this.logMessage.append(" 数据纪录完全提取成功	 成功...(1024字节指令").toString());
                            success.set(1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(this.logMessage.append(this.failPromptMessage).toString());
            throw  new DescribeException(this.failPromptMessage);
        }
        return success;
    }
}
