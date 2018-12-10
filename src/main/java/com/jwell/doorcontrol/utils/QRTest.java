package com.jwell.doorcontrol.utils;


import com.jwell.doorcontrol.service.command.WgUdpCommShort;

public class QRTest {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
       	byte[] pwdData=
       	{
	           	(byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37, 
	            (byte)0x38, (byte)0x39, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x45, (byte)0x46
	    }; //测试密码0123456789ABCDEF
		//String infoA =	createQR(12345,pwdData);
		// infoA =	createQR(153139810,pwdData);
		String infoA =	createQR(2482799616L,pwdData);
		log("加密后的二维码数据:" + infoA);

    /*    byte[] data =
                {
                        (byte) 0x37,(byte) 0x44, (byte)0x34, (byte)0x37, (byte)0x46, (byte)0x37, (byte)0x44, (byte)0x37, (byte)0x46, (byte)0x33,
                        (byte)0x46, (byte)0x42, (byte)0x45, (byte)0x38, (byte)0x33, (byte)0x39, (byte)0x33, (byte)0x38, (byte)0x35, (byte)0x36,
                        (byte)0x43, (byte)0x42, (byte)0x38, (byte)0x39, (byte)0x39, (byte)0x32, (byte)0x34, (byte)0x37, (byte)0x38, (byte)0x46,
                        (byte)0x43, (byte)0x41
                };*/
        byte[] data =
                {
                        55, 68, 52, 55, 70, 55, 68, 55, 70, 51, 70, 66, 69, 56, 51, 57, 51, 56,53, 54, 67, 66, 56, 57, 57, 50, 52, 55, 56, 70, 67, 65
                };

        byte [] s = "2482799616".getBytes();
        StringBuffer d = new StringBuffer();
        for (byte b : s) {
           // log(String.format("0x%02x",b));
           d.append(b).append(" ");
        }
        log(d.toString());
        log("解密后的二维码数据:" +  SM4.decodeSM4toString(data, pwdData));
        log(new String(pwdData));
        log(String.format("%02x",23));
        int a  = Integer.parseInt("17",16);

        log(a + "");

        log(SM4.decodeSM4toString(SM4.encodeSM4("7D47F7D7F3FBE8393856CB8992478FCA", pwdData), pwdData));
        log(DenaryConvertUtil.toHexString(data));
    }

	public static void log(String info) 
	{
		System.out.println(info);
	}

	
	static long getYMD(int Year,int Month, int Day)
     {
         long ymd;
         ymd = (Year % 100) << 9;
         ymd += (Month << 5);
         ymd += (Day);
         return (long)ymd;
     }
       
	static long getHMS(int Hour,int Minute, int Second)
     {
         long hms = 0;
         hms += ((Second >> 1));
         hms += Minute << (5);
         hms += Hour << (11);
         return hms;
     }

	private static String createQR(long cardNO,byte[] pwdData ) //生成QR
	{
   	    byte[] sendData=
      	{
	       	(byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
	        (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x00, (byte)0x00
        };
		
   	 sendData[15] = 2; //固定
     //起始日期时间 2010年1月1日
     long ymd = getYMD(2010,1,1);
     long hms = getHMS(0,0,0);
     sendData[8] = (byte)(ymd & 0xff);
     sendData[9] = (byte)((ymd >> 8) & 0xff);
     sendData[10] = (byte)((hms >> 8) & 0xff); //高位
     sendData[11] = (byte)(hms & 0xE0); //低位

     //截止日期时间 2029年12月31日 23:59:59
     ymd = getYMD(2029,12,31);
     hms = getHMS(23,59,59);
     sendData[11] = (byte)(sendData[11] + ((hms & 0xE0) >> 4)); 
     sendData[12] = (byte)(ymd & 0xff);
     sendData[13] = (byte)((ymd >> 8) & 0xff);
     sendData[14] = (byte)((hms >> 8) & 0xff); 
     if ((hms & 0xE0) == 0xE0) //2018-09-20 19:17:39 修改部分
     {
         sendData[14] = (byte)(sendData[14] + 1);
     }


		if (cardNO >0)
	   	{
	   		sendData[0] = (byte)(cardNO & 0xff);
	   		sendData[1] = (byte)((cardNO>>8) & 0xff);
	   		sendData[2] = (byte)((cardNO>>16) & 0xff);
	   		sendData[3] = (byte)((cardNO>>24) & 0xff);
	   		sendData[4] = (byte)((cardNO>>32) & 0xff);
	   		sendData[5] = (byte)((cardNO>>40) & 0xff);
	   		sendData[6] = (byte)((cardNO>>48) & 0xff);
	   		sendData[7] = (byte)((cardNO>>56) & 0xff);
            sendData[7] = crc8(sendData, 7); //2017-12-18 22:03:47 校验和
  	   	}
			byte[] enOut = SM4.encode16(sendData, pwdData);
			String info ="";
	      for(int i=0; i<enOut.length; i++)
	      {
	    	  info=info+  String.format("%02X",enOut[i]);
	      }
	      return info;
	}
	public static byte GetHex(int val) //
    {
	    return (byte)((val % 10) + (((val -(val % 10)) / 10)%10) *16);
    }
	
	public static  byte crc8(byte[] buf, int len)
     {
         byte i, j, crc;

         crc = 0;
         for (j = 0; j < len; j++)
         {
             crc = (byte)(crc ^ (buf[j]));
             for (i = 8; i > 0; i--)
             {
                 if ((crc & 0x80) > 0)
                 {
                     crc = (byte)((crc << 1) ^ 0x31);  //CRC=X8+X5+X4+1
                 }
                 else
                 {
                     crc = (byte)(crc << 1);
                 }
             }
             //buf++;
         }
         return crc;
     }


    

}
    


    

