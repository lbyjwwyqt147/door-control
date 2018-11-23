package com.jwell.doorcontrol.utils;


import lombok.extern.log4j.Log4j2;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

/***
 *
 */
@Log4j2
public class WgUdpClient {

    public static String sendCommand(WgControllerInfo controllerInfo) {
        //获得本机IP
        try {
            String localAddress = InetAddress.getLocalHost().getHostAddress();
            if (localAddress != null) {
                controllerInfo.setControllerIp(localAddress);
            }
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        int ret =0;

        System.out.println("先运行云服务器...");
        log.info("先运行云服务器...");
        System.out.println(String.format(" 云服务器IP=%s,  Port=%d...", controllerInfo.getControllerIp(), controllerInfo.getWatchServerPort()));
        log.info(String.format(" 云服务器IP=%s,  Port=%d...", controllerInfo.getControllerIp(), controllerInfo.getWatchServerPort()));
        //服务器运行....
        ret = WatchingServerRuning(controllerInfo.getControllerIp(), controllerInfo.getWatchServerPort());
        if (ret == 0) {
            System.out.println("云服务器监控程序启动 失败...[请检查IP和端口 是否被占用]");
            log.info("云服务器监控程序启动 失败...[请检查IP和端口 是否被占用]");
            return null;
        }

    }

    static Queue<byte[]> queue = new LinkedList<byte[]>();

    /**
     *  执行指令
     * @param watchServerIp
     * @param watchServerPort
     * @return
     */
    public static int WatchingServerRuning(String watchServerIp,int watchServerPort) {

    }
}

