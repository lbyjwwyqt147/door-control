package com.jwell.doorcontrol.service.sms;

import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 *  短信发送模板
 * @author ljy
 */
public class AbstractSmsSend {
    /** 短信发送地址 */
    @Value("#{jwell.sms.send-url}")
    protected String smsSendUrl;
    /** 短信系统码  */
    @Value("#{jwell.sms.system-code}")
    protected String systemCode;
    /** 短信模板编号 */
    @Value("#{jwell.sms.template.invitation-code}")
    protected String templateCode;
    /** 短信参数 内容  */
    protected Map<String, Object> paramsMap = new ConcurrentHashMap<>();





}
