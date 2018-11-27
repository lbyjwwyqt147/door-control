package com.jwell.doorcontrol.utils.udp;

import org.apache.mina.core.session.IoSession;

import java.util.LinkedList;
import java.util.Queue;

public class wgControllerInfo {
	public int ControllerSN;
	/// <summary>
    /// 控制器 IP地址
    /// </summary>
    public String IP ="";
    /// <summary>
    /// 控制器 通信端口 默认为60000
    /// </summary>
    public int PORT = 60000;

    /// <summary>
    /// 更新时间
    /// </summary>
    public long UpdateDateTime = System.currentTimeMillis() ;
    
    /// <summary>
    /// 最后接收到的数据
    /// </summary>
    public byte[] ReceivedBytes = null;
    
    public  Queue<byte[]> queueOfReply= new LinkedList<byte[]>(); //2018-07-11 16:05:33 应用队列

    
    /// <summary>
    /// 控制器与云服务器建立的IoSession
    /// </summary>
    public IoSession  Session = null;
    
    /// <summary>
    /// 更新信息
    /// </summary>
    /// <param name="sn"></param>
    /// <param name="ip"></param>
    /// <param name="port"></param>
    /// <param name="dt"></param>
    /// <param name="recv"></param>
    public void update(int sn, String ip, int port,  byte[] recv)
    {
        ControllerSN =(int) sn;
        IP = ip;
        PORT = port;
        UpdateDateTime = System.currentTimeMillis() ; //dt;
        ReceivedBytes = recv;
    }
    
    public void update(int sn, IoSession sess,  byte[] recv)
    {
        ControllerSN =(int) sn;
        Session = sess;
        UpdateDateTime = System.currentTimeMillis() ; //dt;
        ReceivedBytes = recv;  //2018-07-15 08:43:35 最后一次接收到的数据包
        if (WgUdpCommShort4Cloud.isValidCommandReply(recv))  //2018-07-15 08:42:03 是回复指令 才加入
        {
        	if (queueOfReply == null)
        	{
        		queueOfReply = new LinkedList<byte[]>();
        	}
	        if (queueOfReply.size() >10000)  //10000个缓存  640K
	        {
	        	synchronized(queueOfReply)
	    		{
	        		queueOfReply.clear();
	    		}
	        }      
	        synchronized (queueOfReply)
			{
	        	queueOfReply.offer(recv); //2018-07-11 16:09:39 其他应用数据
			}
        }
    }
    
    
    public long recordIndex4WatchingRemoteOpen =0; //2018-07-16 15:15:56 用于监控时 远程开门测试时的记录
    

}
