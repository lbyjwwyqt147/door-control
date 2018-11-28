package com.jwell.doorcontrol.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;

import java.util.Queue;

/***
 *  微耕 udp 短报文协议
 * @author  ljy
 */
@Data
@AllArgsConstructor
@ToString(callSuper = true)
@Log4j2
public final class WgUdpCommShort {
    /**  报文长度 */
    public static final Integer WG_PACKET_SIZE = 64;
    /** 类型  */
    public static final Byte TYPE = 0x17;
    /** 控制器端口 */
    public static final Integer CONTROLLER_PORT = 60000;
    /**  特殊标识 防止误操作 */
    public static final long SPECIAL_FLAG = 0x55AAAA55;

    /** 功能指令代码 */
    private Byte functionId;
    /** 控制器设备序列号 */
    private long controllerSN;
    /**  56字节的数据 [含流水号] */
    private byte[] data = new byte[56];

    private static long globalXid = 0L;
    protected long xid = 0L;

    IoConnector connector;

    public WgUdpCommShort() {
        resetData();
    }

    /**
     *  long 数字转为 byte
     * @param number
     * @return
     */
    public static byte[] longToByte(long number) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }

    /**
     *  将带符号的bt转换为不带符号的int类型数据
     * @param bt
     * @return
     */
    public static int getIntByByte(byte bt) {
        if (bt < 0) {
            return (bt + 256);
        } else {
            return bt;
        }
    }

    /**
     *  从字节转换为 long型数据, 最大长度为8字节 低位在前, 高位在后...
     *  bytlen (1--8), 不在此范围则返回 -1
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
     * 获取新的Xid
     */
    void getNewXid() {
        globalXid++;
        if ((globalXid >= 0x4fffffff) || (globalXid < 0x40000001)) {
            globalXid = 0x40000001;
        }
        // 新的值
        xid = globalXid;
    }

    /**
     * 获取指令中的xid
     * @param cmd 指令
     * @return
     */
    public static long getXidOfCommand(byte[] cmd) {
        long ret = -1;
        if (cmd.length >= WG_PACKET_SIZE) {
            ret = getLongByByte(cmd, 40, 4);
        }
        return ret;
    }

    /**
     * 校验指令是否答复
     * @param cmd
     * @return
     */
    public static boolean isValidCommandReply(byte[] cmd) {
        long xd = getXidOfCommand(cmd);
        if ((xd >= 0x4fffffff) || (xd < 0x40000001)) {
            return false;
        }
        return true;
    }

    /**
     * 数据复位
     */
    public  void resetData() {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    /**
     * 生成64字节指令包
     * @return
     */
    public byte[] toByte() {
        byte[] buff = new byte[WG_PACKET_SIZE];
        for (int i = 0; i < data.length; i++) {
            buff[i] = 0;
        }
        buff[0] = TYPE;
        buff[1] = functionId;
        System.arraycopy(longToByte(controllerSN), 0, buff, 4, 4);
        System.arraycopy(data, 0, buff, 8, data.length);
        getNewXid();
        System.arraycopy(longToByte(xid), 0, buff, 40, 4);
        return buff;
    }

    /**
     *  运行 64字节指令 获取通信数据
     *  失败时, 返回 null, 否则为64字节数据
     * @return
     */
    public byte[] run() {
        return getInfo(controllerSN, toByte());
    }

    /**
     * 运行 1024字节指令
     *  失败时, 返回 null, 否则为1024字节数据
     * @param command1024
     * @return
     */
    public byte[] run(byte[] command1024) {
        return getInfo(controllerSN, command1024);
    }

    /**
     *  通过指定sn和command 获取数据
     * @param controllerSN
     * @param command
     * @return
     */
    public byte[] getInfo(long controllerSN, byte[] command) {
        byte[] bytCommand = command;
        IoBuffer b;

        int iget = WatchingShortHandler.arrSNReceived.indexOf((int)controllerSN);
        if (iget < 0) {
            return null;
        }
        //connFuture.getSession();
        IoSession session = WatchingShortHandler.arrControllerInfo.get(iget).getIoSession();
        Queue<byte[]> queueApplication = WatchingShortHandler.arrControllerInfo.get(iget).getQueueOfReply();
        // 先清空接收缓冲区
        if (queueApplication != null) {
            synchronized (queueApplication) {
                queueApplication.clear();
            }
        }

        Boolean bSent = false;
        if (session != null) {
            if (session.isConnected()) {
                b = IoBuffer.allocate(bytCommand.length);
                b.put(bytCommand);
                b.flip();
                session.write(b);
                bSent = true;
            }
        }

        int bSuccess = 0;
        int tries = 3;
        xid = getXidOfCommand(bytCommand);
        byte[] bytget = null;
        while ((tries--) > 0) {
            long startTicks = java.util.Calendar.getInstance()
                    .getTimeInMillis(); // DateTime.Now.Ticks;
            long commTimeoutMsMin = 300;
            long endTicks = startTicks + commTimeoutMsMin;
            if (startTicks > endTicks) {
                log.info("链接超时..........");
                try {
                    java.lang.Thread.sleep(30L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            long startIndex = 0;
            while (endTicks > java.util.Calendar.getInstance().getTimeInMillis()) {
                // 没有发送过....
                if (!bSent) {
                    if (session != null) {
                        if (session.isConnected()) {
                            b = IoBuffer.allocate(bytCommand.length);
                            b.put(bytCommand);
                            b.flip();
                            session.write(b);
                            bSent = true;
                        }
                    }
                }
                if (!queueApplication.isEmpty()) {
                    synchronized (queueApplication) {
                        bytget = queueApplication.poll();
                    }
                    // 类型一致  功能号一致  序列号对应
                    if ((bytget[0] == bytCommand[0]) && (bytget[1] == bytCommand[1]) && (xid == getXidOfCommand(bytget))) {
                        bSuccess = 1;
                        break;
                    } else {
                        log.info("无效数据包 ..........");
                    }
                } else {
                    if ((startTicks + 1) < java.util.Calendar.getInstance().getTimeInMillis()) {

                    } else if (startIndex > 10) {
                        try {
                            java.lang.Thread.sleep(30L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        startIndex++;
                        try {
                            java.lang.Thread.sleep(1L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            if (bSuccess > 0) {
                break;
            } else {
                if (session != null) {
                    if (session.isConnected()) {
                        b = IoBuffer.allocate(bytCommand.length);
                        b.put(bytCommand);
                        b.flip();
                        session.write(b);
                    }
                }
            }
        }

        if (bSuccess > 0) {
            log.info("与微耕控制板通信 成功。");
            return bytget;
        } else {
            log.info("与微耕控制板通信 失败....");
        }
        return null;
    }
}
