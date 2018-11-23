package com.jwell.doorcontrol.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.jwell.doorcontrol.controller.WgUdpCommShort4Cloud;
import com.jwell.doorcontrol.controller.wgControllerInfo;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;


    
    /**
     * Class the extends IoHandlerAdapter in order to properly handle
     * connections and the data the connections send
     *
     * @author <a href="http://mina.apache.org" mce_href="http://mina.apache.org">Apache MINA Project</a>
     */
    public class WatchingShortHandler extends IoHandlerAdapter {

    	private Queue<byte[]> queue;
        public WatchingShortHandler(Queue<byte[]> queue) {
    		super();
    		this.queue = queue;
    	}
        /**
         * 异常来关闭session
         */
        @Override
        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {
            cause.printStackTrace();
            session.close(true);
        }

   public  static   ArrayList<Integer> arrSNReceived = new ArrayList<Integer>();
   public  static   ArrayList<wgControllerInfo> arrControllerInfo = new ArrayList<wgControllerInfo>(); //2017-12-24 14:58:44
   public  static   Queue<byte[]> queueApp= new LinkedList<byte[]>(); //2018-07-11 16:05:33 应用队列
   
       public static boolean isConnected(long controllerSN)  //检查控制器是否连接
       {
    	   int iget = arrSNReceived.indexOf((int) controllerSN);
	   		if (iget >= 0) {
	   			if (arrControllerInfo.size()>=iget)
	   			{
	   			wgControllerInfo info = arrControllerInfo.get(iget);
	   			if (info != null)
	   			{
	   				if ((info.UpdateDateTime + 5*60*1000) > System.currentTimeMillis()) //在5分钟以内
	   				{
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
        public void messageReceived(IoSession session, Object message)
                throws Exception {

        	IoBuffer io = (IoBuffer) message;
    		if (io.hasRemaining())
    		{
    			byte[] validBytes = new byte[io.remaining()];
    			io.get(validBytes,0,io.remaining());
        		if (((validBytes.length == WgUdpCommShort4Cloud.WGPacketSize)
            			|| ((validBytes.length % WgUdpCommShort4Cloud.WGPacketSize) ==0)) //2018-07-10 14:30:38 引入64的倍数, 用于二维码
                        &&(validBytes.length>0)
            			&& (validBytes[0] == WgUdpCommShort4Cloud.Type))  //型号固定
    			{
    				 synchronized (queue)
    		         {
      				   queue.offer(validBytes);
    		         }
    				 
    				 long sn = WgUdpCommShort4Cloud.getLongByByte(validBytes, 4, 4);
    				 int iget = arrSNReceived.indexOf((int)sn);
		            if (iget < 0)
		            {
		                arrSNReceived.add((int) sn);
		                wgControllerInfo con = new wgControllerInfo();	
		                con.update((int)sn, session,  validBytes);
		                arrControllerInfo.add(con); //2017-12-24 15:01:29
  		            }
		            else
		            {
                //2015-06-13 15:00:41 更新操作
	    		        arrControllerInfo.get(iget).update((int)sn, session, validBytes);
		            }
		            
//		            if ((validBytes[1]== 0x20) )
//					{
//		            	if (WgUdpCommShort4Cloud.isValidCommandReply(validBytes))
//		            	{
//		            		synchronized (queueApp)
//		            		{
//		            		queueApp.offer(validBytes); //2018-07-11 16:09:39 其他应用数据
//		            		}
//		            	}
//					}
//	                else  if ((validBytes[1]== 0x22))  //2018-07-10 10:39:43 增加0x22
//				    {
//					
//				    }
//	               else
//	               {
//	            	   synchronized (queueApp)
//	            		{
//	            		queueApp.offer(validBytes); //2018-07-11 16:09:39 其他应用数据
//	            		}	              
//	               }
    			}
    			else
    			{
    				//System.out.print("收到无效数据包: ????\r\n");
    			}
    			//System.out.println("");
    		}
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
//            System.out.println("服务器端关闭session...");
        }

        @Override
        public void sessionCreated(IoSession session) throws Exception {
//            System.out.println("服务器端成功创建一个session...");
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status)
                throws Exception {
           //  System.out.println("Session idle...");
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
//            System.out.println("服务器端成功开启一个session...");
        }
    }
