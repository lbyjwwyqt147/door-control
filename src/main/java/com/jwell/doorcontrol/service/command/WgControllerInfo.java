package com.jwell.doorcontrol.service.command;

import com.jwell.doorcontrol.dto.VisitorInfoDto;
import lombok.Data;
import org.apache.mina.core.session.IoSession;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.*;

/***
 * 微耕 控制器信息
 * @author ljy
 */
@Data
public class WgControllerInfo implements Serializable {


    private static final long serialVersionUID = -2077270202026646892L;
    /** 控制器设备序列号 */
    private long controllerSN;
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
    private byte[] receivedBytes;
    /** 应用队列 */
    private Queue<byte[]> queueOfReply = new LinkedList<>();
    /** 控制器与云服务器建立的IoSession */
    private IoSession ioSession;
    /** 用于监控时 远程开门测试时的记录 */
    private  Long recordIndex4WatchingRemoteOpen = 0L;
    /** 门号 */
    private Integer doorNo;
    /** 纪录类型  0 : 无记录  1: 刷卡记录 2: 门磁,按钮, 设备启动, 远程开门记录  3: 报警记录*/
    private Integer recordType;
    /** 业务标志   0:新增  1：修改  2：删除  3：获取纪录  4：查询 5: 清空全部 6: 批量操作 */
    private Byte mark;
    /**  模块  0：远程开门  1：上传权限  2：删除权限 3：清空权限 4：权限查询 5: 批量上传权限 6：提取纪录  7：搜索控制器  9：IP设置 10:读取IP */
    private Byte module;
    /** 开始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /** 结束时间  */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    /** 卡号  */
    private long cardNo;
    /** 一组 卡号 */
    private List<Integer> cardNoList = new ArrayList<>();
    /** bForceGetAllSwipe  >=1  强制提取所有记录(包括之前已提取的), ==0 表示 提取新的记录(不含 已提取过的) */
    private Integer bForceGetAllSwipe;
    /** 二维码数据内容 */
    private String quickMarkDataContent;
    /** 访客信息 */
    private VisitorInfoDto visitorInfo;


    /**
     * 更新信息
     * @param controllerSN
     * @param controllerIp
     * @param port
     * @param recv  最后一次接收到的数据包
     */
    public void update( Integer controllerSN, String controllerIp, Integer port,  byte[] recv) {
        this.controllerSN = controllerSN;
        this.controllerIp = controllerIp;
        this.port = port;
        this.updateDateTime = System.currentTimeMillis();
        this.receivedBytes = recv;
    }

    /**
     * 更新信息
     * @param controllerSN
     * @param recv 最后一次接收到的数据包
     */
    public void update(Integer controllerSN, IoSession ioSession,  byte[] recv) {
        this.controllerSN = controllerSN;
        this.ioSession = ioSession;
        this.updateDateTime = System.currentTimeMillis() ;
        // 最后一次接收到的数据包
        this.receivedBytes = recv;
        // 是回复指令 才加入
        if (WgUdpCommShort.isValidCommandReply(recv)) {
            if (queueOfReply == null) {
                queueOfReply = new LinkedList<>();
            }
            //10000个缓存  640K
            if (queueOfReply.size() > 10000) {
                synchronized(queueOfReply) {
                    queueOfReply.clear();
                }
            }
            synchronized (queueOfReply) {
                // 其他应用数据
                queueOfReply.offer(recv);
            }
        }
    }
}
