package com.jwell.doorcontrol.service.command;


import com.jwell.boot.utilscommon.utils.ThreadPoolExecutorFactory;
import com.jwell.doorcontrol.utils.SM4;
import lombok.extern.log4j.Log4j2;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;

/***
 * 链接微耕控制器udp 协议
 * @author ljy
 */
@Log4j2
@Component(value = "wgUdpClient")
public class WgUdpClient {

    private static ThreadPoolExecutor threadPoolExecutor = ThreadPoolExecutorFactory.getThreadPoolExecutor();
    @Resource(name = "scanCodeOpenDoorCommand")
    private  ScanCodeOpenDoorCommand scanCodeOpenDoorCommand;

    /**
     * 链接并运行监控服务器
     *
     * @param controllerInfo
     * @return
     */
    public  void chainingWatchingServer(WgControllerInfo controllerInfo) {
        //获得本机IP
        try {
            String localAddress = InetAddress.getLocalHost().getHostAddress();
            if (localAddress != null) {
                controllerInfo.setWatchServerIp(localAddress);
            }
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        int ret = 0;

        log.info("先运行云服务器...");
        log.info(String.format(" 云服务器IP=%s,  Port=%d...", controllerInfo.getWatchServerIp(), controllerInfo.getWatchServerPort()));
        //服务器运行....
        ret = watchingServerRuning(controllerInfo);
        if (ret == 0) {
            log.info("云服务器监控程序启动 失败...[请检查IP和端口 是否被占用]");
        } else {
            log.info("云服务器监控程序启动 成功.... ");
        }
    }

    static Queue<byte[]> queue = new LinkedList<>();

    /**
     *  进入服务器监控状态 执行指令
     * @param controllerInfo
     * @return
     */
    private  int watchingServerRuning(WgControllerInfo controllerInfo) {
        // 创建UDP数据包NIO
        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
        // NIO设置底层IOHandler
        acceptor.setHandler(new WatchingShortHandler(queue));

        // 设置是否重用地址？ 也就是每个发过来的udp信息都是一个地址？
        DatagramSessionConfig dcfg = acceptor.getSessionConfig();
        dcfg.setReuseAddress(true);
        // 绑定端口地址
        try {
            acceptor.bind(new InetSocketAddress(controllerInfo.getWatchServerIp(), controllerInfo.getWatchServerPort()));
            log.info("################# 开始监控控制器数据 ############################");
          //  startMonitoring(controllerInfo);
            //开启线程运行数据监控
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    startMonitoring(controllerInfo);
                  //  acceptor.unbind();
                  //  acceptor.dispose();
                }
            });
        } catch (IOException e) {
            log.info("绑定接收服务器失败....");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    /**
     * 开始监控控制器数据
     * @param controllerInfo
     */
    private  void startMonitoring(WgControllerInfo controllerInfo) {
       /* new Thread() {
            @Override
            public void run() {*/
                //记录索引号
                long recordIndex = 0;
                while(true) {
                    if (!queue.isEmpty()) {
                        byte[] recvBuff;
                        synchronized (queue) {
                            recvBuff= queue.poll();
                        }
                        if ((recvBuff[1] == 0x20)) {
                            // 控制器SN
                            long sn = WgUdpCommShort.getLongByByte(recvBuff, 4, 4);
                            int controlerSn = Integer.parseInt(String.valueOf(sn));
                            long recordIndexGet = WgUdpCommShort.getLongByByte(recvBuff, 8, 4);
                            log.info(String.format("接收到来自控制器SN = %d 的数据包..##########", controlerSn));
                            int iget = WatchingShortHandler.arrSNReceived.indexOf(controlerSn);
                            if (iget >= 0) {
                                if (WatchingShortHandler.arrControllerInfo.size() >= iget) {
                                    WgControllerInfo  info = WatchingShortHandler.arrControllerInfo.get(iget);
                                    if (info != null) {
                                        recordIndex = info.getRecordIndex4WatchingRemoteOpen();
                                        info.setRecordIndex4WatchingRemoteOpen(recordIndexGet);
                                        if (recordIndex < recordIndexGet) {
                                            recordIndex = recordIndexGet;
                                            // 显示日志记录
                                            displayRecordInformation(recvBuff);
                                        }
                                    }
                                }
                            }
                        } else if ((recvBuff[1] == 0x22)) {
                            long sn = WgUdpCommShort.getLongByByte(recvBuff, 4, 4);
                            long recordIndexGet = WgUdpCommShort.getLongByByte(recvBuff, 8, 4);
                            log.info(String.format("接收到来自控制器SN = %d 的二维码数据包....", sn));
                            int i = 0;
                            int j = 0;
                            StringBuffer resultDataBuffer = new StringBuffer();
                            StringBuffer quickMarkDataBuffer = new StringBuffer();
                            byte[] quickMarkDataArrays = new byte[32];
                            for (byte v : recvBuff) {
                                String newValue = String.format("%02x",v).toUpperCase();
                                if (i > 63 && i < 96) {
                                    quickMarkDataArrays[j] = v;
                                    j++;
                                    quickMarkDataBuffer.append(newValue).append(" ");
                                }
                                i++;
                                resultDataBuffer.append(newValue).append(" ");
                            }
                            log.info(resultDataBuffer.toString());
                            log.info("二维码原始数据：" + quickMarkDataBuffer.toString());

                            /*byte[] pwdData=
                                    {
                                            (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37,
                                            (byte)0x38, (byte)0x39, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x45, (byte)0x46
                                    };
                            log.info("二维码原始数据(转换为字符串): " + SM4.decodeSM4toString(quickMarkDataArrays, pwdData));*/
                            WgControllerInfo wgControllerInfo = new WgControllerInfo();
                            // 控制器sn
                            wgControllerInfo.setControllerSN(sn);
                            // 卡号
                            wgControllerInfo.setCardNo(2482799616L);
                            // 门号
                            //14	门号(1,2,3,4)	1
                            int recordDoorNO = WgUdpCommShort.getIntByByte(recvBuff[14]);
                            wgControllerInfo.setDoorNo(recordDoorNO);
                            scanCodeOpenDoorCommand.openDoor(wgControllerInfo);

                        }
                    } else {
                        long times = 100;
                        try {
                            // 增加延时
                            Thread.sleep(times);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
      //      }
       // }.start();
    }


    /**
     *  显示记录信息
     * @param recvBuff
     */
    public static void displayRecordInformation(byte[] recvBuff) {
        //8-11	最后一条记录的索引号
        //(=0表示没有记录)	4	0x00000000
        long recordIndex = WgUdpCommShort.getLongByByte(recvBuff, 8, 4);
        //12	记录类型
        //0=无记录
        //1=刷卡记录
        //2=门磁,按钮, 设备启动, 远程开门记录
        //3=报警记录	1
        int recordType = WgUdpCommShort.getIntByByte(recvBuff[12]);

        //13	有效性(0 表示不通过, 1表示通过)	1
        int recordValid = WgUdpCommShort.getIntByByte(recvBuff[13]);

        //14	门号(1,2,3,4)	1
        int recordDoorNO = WgUdpCommShort.getIntByByte(recvBuff[14]);

        //15	进门/出门(1表示进门, 2表示出门)	1	0x01
        int recordInOrOut = WgUdpCommShort.getIntByByte(recvBuff[15]);

        //16-19	卡号(类型是刷卡记录时)
        //或编号(其他类型记录)	4
        long  recordCardNO = WgUdpCommShort.getLongByByte(recvBuff, 16, 4);


        //20-26	刷卡时间:
        //年月日时分秒 (采用BCD码)见设置时间部分的说明
        String recordTime=  String.format("%02X%02X-%02X-%02X %02X:%02X:%02X",
                WgUdpCommShort.getIntByByte(recvBuff[20]),
                WgUdpCommShort.getIntByByte(recvBuff[21]),
                WgUdpCommShort.getIntByByte(recvBuff[22]),
                WgUdpCommShort.getIntByByte(recvBuff[23]),
                WgUdpCommShort.getIntByByte(recvBuff[24]),
                WgUdpCommShort.getIntByByte(recvBuff[25]),
                WgUdpCommShort.getIntByByte(recvBuff[26]));

        //2012.12.11 10:49:59	7
        //27	记录原因代码(可以查 "刷卡记录说明.xls"文件的ReasonNO)
        //处理复杂信息才用	1
        int reason = WgUdpCommShort.getIntByByte(recvBuff[27]);

        //0=无记录
        //1=刷卡记录
        //2=门磁,按钮, 设备启动, 远程开门记录
        //3=报警记录	1
        //0xFF=表示指定索引位的记录已被覆盖掉了.  请使用索引0, 取回最早一条记录的索引值
        if (recordType == 0) {
            log.info("\n" + String.format("索引位 = %d  无记录", recordIndex));
        } else if (recordType == 0xff) {
            log.info(" 指定索引位的记录已被覆盖掉了,请使用索引0, 取回最早一条记录的索引值");
        } else if (recordType == 1) {
            //显示记录类型为卡号的数据
            //卡号
            log.info("\n" + (String.format("索引位 = %d  ", recordIndex))+"\r\n" +
                    (String.format("  卡号 = %d", recordCardNO))+"\r\n" +
                    (String.format("  门号 = %d", recordDoorNO))+"\r\n" +
                    (String.format("  进出 = %s", recordInOrOut == 1 ? "进门" : "出门"))+"\r\n" +
                    (String.format("  有效 = %s", recordValid == 1 ? "通过" : "禁止"))+"\r\n" +
                    (String.format("  时间 = %s", recordTime))+"\r\n" +
                    (String.format("  描述 = %s", getReasonDetailChinese(reason))));
        } else if (recordType == 2) {
            //其他处理
            //门磁,按钮, 设备启动, 远程开门记录
            log.info("\n" + (String.format("索引位=%d  非刷卡记录", recordIndex))+"\r\n" +
                    (String.format("  编号 = %d", recordCardNO))+"\r\n" +
                    (String.format("  门号 = %d", recordDoorNO))+"\r\n" +
                    (String.format("  时间 = %s", recordTime))+"\r\n" +
                    (String.format("  描述 = %s", getReasonDetailChinese(reason))));
        } else if (recordType == 3) {
            //其他处理
            //报警记录
            log.info("\n" + (String.format("索引位=%d  报警记录", recordIndex))+"\r\n" +
                    (String.format("  编号 = %d", recordCardNO))+"\r\n" +
                    (String.format("  门号 = %d", recordDoorNO))+"\r\n" +
                    (String.format("  时间 = %s", recordTime))+"\r\n" +
                    (String.format("  描述 = %s", getReasonDetailChinese(reason))));
        }
    }


    public static  String recordDetails[] =
            {
//记录原因 (类型中 SwipePass 表示通过; SwipeNOPass表示禁止通过; ValidEvent 有效事件(如按钮 门磁 超级密码开门); Warn 报警事件)
//代码  类型   英文描述  中文描述
                    "1","SwipePass","Swipe","刷卡开门",
                    "2","SwipePass","Swipe Close","刷卡关",
                    "3","SwipePass","Swipe Open","刷卡开",
                    "4","SwipePass","Swipe Limited Times","刷卡开门(带限次)",
                    "5","SwipeNOPass","Denied Access: PC Control","刷卡禁止通过: 电脑控制",
                    "6","SwipeNOPass","Denied Access: No PRIVILEGE","刷卡禁止通过: 没有权限",
                    "7","SwipeNOPass","Denied Access: Wrong PASSWORD","刷卡禁止通过: 密码不对",
                    "8","SwipeNOPass","Denied Access: AntiBack","刷卡禁止通过: 反潜回",
                    "9","SwipeNOPass","Denied Access: More Cards","刷卡禁止通过: 多卡",
                    "10","SwipeNOPass","Denied Access: First Card Open","刷卡禁止通过: 首卡",
                    "11","SwipeNOPass","Denied Access: Door Set NC","刷卡禁止通过: 门为常闭",
                    "12","SwipeNOPass","Denied Access: InterLock","刷卡禁止通过: 互锁",
                    "13","SwipeNOPass","Denied Access: Limited Times","刷卡禁止通过: 受刷卡次数限制",
                    "14","SwipeNOPass","Denied Access: Limited Person Indoor","刷卡禁止通过: 门内人数限制",
                    "15","SwipeNOPass","Denied Access: Invalid Timezone","刷卡禁止通过: 卡过期或不在有效时段",
                    "16","SwipeNOPass","Denied Access: In Order","刷卡禁止通过: 按顺序进出限制",
                    "17","SwipeNOPass","Denied Access: SWIPE GAP LIMIT","刷卡禁止通过: 刷卡间隔约束",
                    "18","SwipeNOPass","Denied Access","刷卡禁止通过: 原因不明",
                    "19","SwipeNOPass","Denied Access: Limited Times","刷卡禁止通过: 刷卡次数限制",
                    "20","ValidEvent","Push Button","按钮开门",
                    "21","ValidEvent","Push Button Open","按钮开",
                    "22","ValidEvent","Push Button Close","按钮关",
                    "23","ValidEvent","Door Open","门打开[门磁信号]",
                    "24","ValidEvent","Door Closed","门关闭[门磁信号]",
                    "25","ValidEvent","Super Password Open Door","超级密码开门",
                    "26","ValidEvent","Super Password Open","超级密码开",
                    "27","ValidEvent","Super Password Close","超级密码关",
                    "28","Warn","Controller Power On","控制器上电",
                    "29","Warn","Controller Reset","控制器复位",
                    "30","Warn","Push Button Invalid: Disable","按钮不开门: 按钮禁用",
                    "31","Warn","Push Button Invalid: Forced Lock","按钮不开门: 强制关门",
                    "32","Warn","Push Button Invalid: Not On Line","按钮不开门: 门不在线",
                    "33","Warn","Push Button Invalid: InterLock","按钮不开门: 互锁",
                    "34","Warn","Threat","胁迫报警",
                    "35","Warn","Threat Open","胁迫报警开",
                    "36","Warn","Threat Close","胁迫报警关",
                    "37","Warn","Open too long","门长时间未关报警[合法开门后]",
                    "38","Warn","Forced Open","强行闯入报警",
                    "39","Warn","Fire","火警",
                    "40","Warn","Forced Close","强制关门",
                    "41","Warn","Guard Against Theft","防盗报警",
                    "42","Warn","7*24Hour Zone","烟雾煤气温度报警",
                    "43","Warn","Emergency Call","紧急呼救报警",
                    "44","RemoteOpen","Remote Open Door","操作员远程开门",
                    "45","RemoteOpen","Remote Open Door By USB Reader","发卡器确定发出的远程开门"
            };

    /**
     * 中文信息
     * @param reason
     * @return 中文信息
     */
    private static   String getReasonDetailChinese(int reason) {
        if (reason > 45) {
            return "";
        }
        if (reason <= 0) {
            return "";
        }
        // 中文信息
        return recordDetails[(reason - 1) * 4 + 3];
    }

}

