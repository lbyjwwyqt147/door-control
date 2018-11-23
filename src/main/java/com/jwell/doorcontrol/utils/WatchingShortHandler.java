package com.jwell.doorcontrol.utils;

import com.jwell.doorcontrol.controller.WgUdpCommShort4Cloud;
import com.jwell.doorcontrol.controller.wgControllerInfo;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/***
 * 监控处理
 */
public class WatchingShortHandler extends IoHandlerAdapter {


    private Queue<byte[]> queue;
    public WatchingShortHandler(Queue<byte[]> queue) {
        super();
        this.queue = queue;
    }

    private  static ArrayList<Integer> arrSNReceived = new ArrayList<>();
    private  static ArrayList<wgControllerInfo> arrControllerInfo = new ArrayList<wgControllerInfo>();
    private  static Queue<byte[]> queueApp = new LinkedList<>();  // 应用队列

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
    public static boolean isConnected(Long controllerSN) {
        int iget = arrSNReceived.indexOf(controllerSN);
        if (iget >= 0) {
            if (arrControllerInfo.size() >= iget) {
                wgControllerInfo info = arrControllerInfo.get(iget);
                if (info != null) {
                    //在5分钟以内
                    if ((info.UpdateDateTime + 5*60*1000) > System.currentTimeMillis()) {
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
            if (((validBytes.length == WgUdpCommShort4Cloud.WGPacketSize)
                    || ((validBytes.length % WgUdpCommShort4Cloud.WGPacketSize) == 0)) //引入64的倍数, 用于二维码
                    && (validBytes.length > 0)
                    && (validBytes[0] == WgUdpCommShort4Cloud.Type))  //型号固定
            {
                synchronized (queue) {
                    queue.offer(validBytes);
                }
                long sn = WgUdpCommShort4Cloud.getLongByByte(validBytes, 4, 4);
                int iget = arrSNReceived.indexOf((int) sn);
                if (iget < 0) {
                    arrSNReceived.add((int) sn);
                    wgControllerInfo con = new wgControllerInfo();
                    con.update((int) sn, session, validBytes);
                    arrControllerInfo.add(con);
                } else {
                    // 更新操作
                    arrControllerInfo.get(iget).update((int) sn, session, validBytes);
                }
            }
        } else {
            System.out.println("收到无效数据包: ????\r\n");
        }
    }



    @Override
    public void sessionClosed(IoSession session) throws Exception {
            System.out.println("服务器端关闭session...");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
            System.out.println("服务器端成功创建一个session...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
          System.out.println("Session idle...");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        System.out.println("服务器端成功开启一个session...");
    }

    public static ArrayList<Integer> getArrSNReceived() {
        return arrSNReceived;
    }

    public static void setArrSNReceived(ArrayList<Integer> arrSNReceived) {
        WatchingShortHandler.arrSNReceived = arrSNReceived;
    }

    public static ArrayList<wgControllerInfo> getArrControllerInfo() {
        return arrControllerInfo;
    }

    public static void setArrControllerInfo(ArrayList<wgControllerInfo> arrControllerInfo) {
        WatchingShortHandler.arrControllerInfo = arrControllerInfo;
    }
}
