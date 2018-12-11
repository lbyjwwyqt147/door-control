package com.jwell.doorcontrol.dto;

import lombok.Data;

/***
 *  访客基础信息 dto
 * @author
 */
@Data
public class VisitorInfoDto {

    /**  访客卡号 */
    private Long cardNo;
    /** 访客手机号 */
    private Long phoneNumber;
    /** 访客姓名 */
    private String name;
    /** 二维码数据 */
    private String qrCode;

}
