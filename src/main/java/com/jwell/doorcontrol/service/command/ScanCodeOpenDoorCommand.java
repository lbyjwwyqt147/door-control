package com.jwell.doorcontrol.service.command;

import com.jwell.boot.utilscommon.redis.RedisUtil;
import com.jwell.doorcontrol.dto.RedisKeyDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/***
 *  扫码开门
 * @author ljy
 */
@Component(value = "scanCodeOpenDoorCommand")
public class ScanCodeOpenDoorCommand {
    /** 访问频率限制 */
    @Value("${jwell.access-frequency-restrict}")
    private  Byte accessFrequencyRestrict;
    @Resource(name = "remoteOpenDoorCommand")
    private AbstractSendCommand remoteOpenDoorCommand;
    @Resource(name = "accessAuthorizationCommand")
    private AbstractSendCommand accessAuthorizationCommand;
    @Autowired
    private RedisUtil redisUtil;

    public void openDoor(WgControllerInfo wgControllerInfo) {
        wgControllerInfo.setMark((byte) 4);
        // 从redis 中获取二维码已经使用的次数
        Object redisResult = redisUtil.hget(RedisKeyDto.GATE_QR_CODE_FREQUENCY_KEY, wgControllerInfo.getQuickMarkDataContent());
        int number = redisResult != null ? Integer.parseInt(redisResult.toString()) : 0;
        // number > accessFrequencyRestrict  表示超过了使用次数，无法在使用当前二维码
        // 大于 0 表示有开门权限  等于 0 表示没有开门权限
        if (number <= accessFrequencyRestrict && accessAuthorizationCommand.commandExecute(wgControllerInfo).get() == 2 ) {
            // 执行远程开门
            remoteOpenDoorCommand.commandExecute(wgControllerInfo);
            // 开门后 二维码使用次数累计+1
            int lastNumber = number + 1;
            redisUtil.hset(RedisKeyDto.GATE_QR_CODE_FREQUENCY_KEY, wgControllerInfo.getQuickMarkDataContent(), String.valueOf(lastNumber));
        }
    }

}
