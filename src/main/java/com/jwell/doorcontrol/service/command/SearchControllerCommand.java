package com.jwell.doorcontrol.service.command;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * 搜索控制器 指令
 * @author ljy
 */
@Component(value = "searchControllerCommand")
@Log4j2
public class SearchControllerCommand extends AbstractSendCommand{

    @Override
    public AtomicInteger commandExecute(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setModule((byte) 7);
        this.startSearch();
        return new AtomicInteger(1);
    }

    /**
     *  将带符号的bt转换为不带符号的int类型数据
     *  bt 转换为无符号的int
     *  @param bt
     */
    public static int getIntByByte(byte bt) {
        if (bt < 0) {
            return (bt + 256);
        } else {
            return bt;
        }
    }


    /**
     * 从字节转换为 long型数据, 最大长度为8字节 低位在前, 高位在后...
     * 	bytlen (1--8), 不在此范围则返回 -1
     * @param data
     * @param startIndex
     * @param bytlen
     * @return
     */
    public static long getLongByByte(byte[] data, int startIndex, int bytlen) {
        long ret = -1;
        if ((bytlen >= 1) && (bytlen <= 8)) {
            ret = getIntByByte(data[startIndex + bytlen - 1]);
            for (int i = 1; i < bytlen; i++) {
                ret <<= 8;
                ret += getIntByByte(data[startIndex + bytlen - 1 - i]);
            }
        }
        return ret;
    }

    /**
     *  开始搜索网段中的所有控制器
     */
    public void startSearch() {
        log.info("### 开始搜索控制器.................. ");
         AtomicInteger port = new AtomicInteger(0);
         MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(port.get());
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        byte[] buf = new byte[] { (byte) 0x17, (byte) 0x94, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("255.255.255.255"), 60000);
            socket.send(packet);
        } catch (UnknownHostException e2) {
            e2.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            socket.setSoTimeout(2000);
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        while (true) {
            byte data[] = new byte[64];
            packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
                long controllerSN = 0;
                controllerSN = getLongByByte(data, 4, 4);
                log.info(String.format("控制器SN = %d", controllerSN));
            } catch (IOException e) {
                // e.printStackTrace();
                break;
            }
        }
        log.info("### 搜索控制器 完成   结束.................. ");
    }
}
