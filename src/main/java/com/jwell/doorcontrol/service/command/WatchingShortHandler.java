package com.jwell.doorcontrol.service.command;


import lombok.extern.log4j.Log4j2;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/***
 * 监控处理
 * @author ljy
 */
@Log4j2
public class WatchingShortHandler extends IoHandlerAdapter {


    private Queue<byte[]> queue;
    public WatchingShortHandler(Queue<byte[]> queue) {
        super();
        this.queue = queue;
    }

    public  static ArrayList<Integer> arrSNReceived = new ArrayList<>();
    public  static ArrayList<WgControllerInfo> arrControllerInfo = new ArrayList<>();
    /**  应用队列 */
    public  static Queue<byte[]> queueApp = new LinkedList<>();

    /**
     * 异常来关闭session
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        session.closeNow();
    }

    /**
     *  检查控制器是否连接
     * @param controllerSN
     * @return
     */
    public static boolean isConnected(Integer controllerSN) {
        int iget = arrSNReceived.indexOf(controllerSN);
        if (iget >= 0) {
            if (arrControllerInfo.size() >= iget) {
                WgControllerInfo info = arrControllerInfo.get(iget);
                if (info != null) {
                    //在5分钟以内
                    if ((info.getUpdateDateTime() + 5*60*1000) > System.currentTimeMillis()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 服务器端收到一个消息
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        IoBuffer io = (IoBuffer) message;
        if (io.hasRemaining()) {
            byte[] validBytes = new byte[io.remaining()];
            io.get(validBytes, 0, io.remaining());
            if (((validBytes.length == WgUdpCommShort.WG_PACKET_SIZE)
                    || ((validBytes.length % WgUdpCommShort.WG_PACKET_SIZE) == 0)) //引入64的倍数, 用于二维码
                    && (validBytes.length > 0)
                    && (validBytes[0] == WgUdpCommShort.TYPE))  //型号固定
            {
                synchronized (queue) {
                    queue.offer(validBytes);
                }
                long sn = WgUdpCommShort.getLongByByte(validBytes, 4, 4);
                int controlerSn = Integer.parseInt(String.valueOf(sn));
                int iget = arrSNReceived.indexOf(controlerSn);
                if (iget < 0) {
                    arrSNReceived.add(controlerSn);
                    WgControllerInfo con = new WgControllerInfo();
                    con.update(controlerSn, session, validBytes);
                    arrControllerInfo.add(con);
                } else {
                    // 更新操作
                    arrControllerInfo.get(iget).update(controlerSn, session, validBytes);
                }
            }
        } else {
            log.info("收到无效数据包: ????");
        }
    }



    @Override
    public void sessionClosed(IoSession session) throws Exception {
        log.info("服务器端关闭IoSession...");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        log.info("服务器端成功创建一个IoSession...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        log.info("IoSession idle...");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("服务器端成功开启一个IoSession....");
    }

}
