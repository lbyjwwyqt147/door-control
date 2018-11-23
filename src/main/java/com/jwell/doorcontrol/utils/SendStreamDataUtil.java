package com.jwell.doorcontrol.utils;


import com.jwell.boot.utilscommon.utils.HttpClientUtils;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/***
 *  发送报文数据给控制器端
 * @author  ljy
 */
@Log4j2
public class SendStreamDataUtil {

    /**
     *  POST 方式 发送报文数据
     * @param servletUrl  目标地址url
     * @param dataValus  发送的数据值
     * @return
     */
    public static String transmitDataPost(String servletUrl,  String dataValus) {
        HttpURLConnection httpUrlConnection = null;
        InputStream inStream = null;
        ObjectInputStream objInStream = null;
        OutputStream outStrm = null;
        try {
            URL url = new URL(servletUrl);
            log.info("开始与【" + servletUrl + "】服务建立链接.......... ");
            System.out.println("开始与【" + servletUrl + "】服务建立链接.......... ");
            //获取连接对象
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            // http正文内容，因此需要设为true, 默认情况下是false;
            httpUrlConnection.setDoOutput(true);
            // 设置是否从httpUrlConnection读入，默认情况下是true;
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setAllowUserInteraction(true);
            //设置connection timeout为10秒
            httpUrlConnection.setConnectTimeout(10 * 1000);
            // 设置read timeout为15秒
            httpUrlConnection.setReadTimeout(15 * 1000);
            // Post 请求不能使用缓存
            httpUrlConnection.setUseCaches(false);
            //设置字符编码类型
            httpUrlConnection.addRequestProperty("Charset", "UTF-8");
            // 设定请求的方法为"POST"，默认是GET
            httpUrlConnection.setRequestMethod("POST");

            //开启流 写入数据
            outStrm = httpUrlConnection.getOutputStream();
            // 现在通过输出流对象构建对象输出流对象，以实现输出可序列化的对象。
            ObjectOutputStream objOutputStrm = new ObjectOutputStream(outStrm);
            // 向对象输出流写出数据，这些数据将存到内存缓冲区中
            objOutputStrm.writeObject(dataValus.getBytes());
            System.out.println("开始给目标服务器上发送报文数据:" + dataValus);
            log.info("开始给目标服务器上发送报文数据:" + dataValus);
            // 刷新对象输出流，将任何字节都写入潜在的流中（些处为ObjectOutputStream）
            objOutputStrm.flush();
            // 关闭流对象。此时，不能再向对象输出流写入任何数据，先前写入的数据存在于内存缓冲区中,
            // 在调用下边的getInputStream()函数时才把准备好的http请求正式发送到服务器
            objOutputStrm.close();
            outStrm.close();
            //  OutputStream buffered = new BufferedOutputStream(raw);
            //  OutputStreamWriter out = new OutputStreamWriter(buffered, "UTF-8");
           // System.out.println("发送报文:" + dataValus);
          //  output.write(data.getBytes());


            //获取到返回数据的输入流
            // 调用HttpURLConnection连接对象的getInputStream()函数,
            // 将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
            inStream = httpUrlConnection.getInputStream(); // <===注意，实际发送请求的代码段就在这里
            objInStream = new ObjectInputStream(inStream);
            Object obj = objInStream.readObject();
            System.out.println(obj);
            /*StringBuffer resultData = new StringBuffer();
            System.out.println("获取返回数据:");
            while ((obj = objInStream.readLine()) != null) {
                //读取返回数据，分行读取
                System.out.println(obj.trim());
                resultData.append(obj.trim());
            }*/

         /*   InputStream is = httpUrlConnection.getInputStream();
            BufferedReader connectionBuffer = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = null;
            StringBuffer resultData = new StringBuffer();
            System.out.println("获取返回数据:");
            while ((line = connectionBuffer.readLine()) != null) {
                //读取返回数据，分行读取
                System.out.println(line.trim());
                resultData.append(line.trim());
            }
            System.out.println(resultData.toString());
            connectionBuffer.close();*/
        } catch (SocketTimeoutException timeoutException) {
            timeoutException.printStackTrace();
            String errorMessage = timeoutException.getMessage();
            if (errorMessage.equals("connect timed out")) {
                System.out.println("与目标服务器建立链接超时.......... ");
                log.info("与目标服务器建立链接超时.......... ");
            } else if (errorMessage.equals("Read timed out")) {
                System.out.println("读取目标服务器上返回的报文数据超时.......... ");
                log.info("读取目标服务器上返回的报文数据超时.......... ");
            }
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outStrm != null) {
                    outStrm.close();
                }
                if (objInStream != null) {
                    objInStream.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
                if (httpUrlConnection != null) {
                    httpUrlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("关闭与【" + servletUrl + "】服务建立的链接..........");
            log.info("关闭与【" + servletUrl + "】服务建立的链接..........");
        }
        return null;
    }

    public static void main(String args[]) {
        //目标服务器地址
        String url1 = "http://10.8.3.139:60000";
        //发送的报文
       // String data = "17 20 00 00 0E 06 5E 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        String data = "17 40 00 00 0E 06 E5 0D 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

        try {
            SendStreamDataUtil.transmitDataPost(url1,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
