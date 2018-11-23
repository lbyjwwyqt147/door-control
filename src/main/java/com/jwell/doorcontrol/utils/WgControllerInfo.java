package com.jwell.doorcontrol.utils;

import lombok.Data;
import org.apache.mina.core.session.IoSession;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

/***
 * 微耕 控制器信息
 */
@Data
public class WgControllerInfo implements Serializable {


    private static final long serialVersionUID = -2077270202026646892L;
    /** 控制器设备序列号 */
    private Long controllerSN;
    /** 接收服务器的IP */
    private String watchServerIp;
    /** 接收服务器端口 (61005) */
    private Integer watchServerPort = 61005;
    /** 控制器设备IP 地址 */
    private String controllerIp;
    /** 控制器 通信端口 默认为60000 */
    private Integer port = 60000;
    /** 更新时间 */
    private Long updateDateTime = System.currentTimeMillis();
    /** 最后收到的数据 */
    private Byte[] receivedBytes;
    /** 应用队列 */
    private Queue<Byte[]> queueOfReply = new LinkedList<>();
    /** 控制器与云服务器建立的IoSession */
    private IoSession ioSession;
    /** 用于监控时 远程开门测试时的记录 */
    private  Long recordIndex4WatchingRemoteOpen = 0L;
}
