package com.jwell.doorcontrol.utils.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TestSearch implements Runnable {
	private int port;
	private MulticastSocket socket = null;

	public TestSearch() {
		run();
	}

	public static void log(String info) // 日志信息
	{
		System.out.println(info);
	}

	// 将带符号的bt转换为不带符号的int类型数据
	public static int getIntByByte(byte bt) // bt 转换为无符号的int
	{
		if (bt < 0) {
			return (bt + 256);
		} else {
			return bt;
		}
	}

	// 从字节转换为 long型数据, 最大长度为8字节 低位在前, 高位在后...
	// bytlen (1--8), 不在此范围则返回 -1
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

	@Override
	public void run() {
		// TODO Auto-generated method stub

		log("开始搜索");
		port = 0;
		try {
			socket = new MulticastSocket(port);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
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
			packet = new DatagramPacket(buf, buf.length,
					InetAddress.getByName("255.255.255.255"), 60000);
			socket.send(packet);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			socket.setSoTimeout(2000);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true) {
			byte data[] = new byte[64];
			packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
				long controllerSN = 0;
				controllerSN = getLongByByte(data, 4, 4); // data[4]+(data[5]<<8)+(data[5]<<16)+(data[5]<<24);
				log(String.format("控制器SN = %d", controllerSN));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// log("e..");
				break;
			}
		}
		log("完成搜索");
	}

	public static void main(String[] agrs) {
		new TestSearch();
	}

}