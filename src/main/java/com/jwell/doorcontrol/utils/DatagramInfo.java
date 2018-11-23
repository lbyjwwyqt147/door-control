package com.jwell.doorcontrol.utils;

import lombok.*;

import java.io.Serializable;

/***
 *  报文数据
 * @author ljy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class DatagramInfo implements Serializable {
    private static final long serialVersionUID = -4917323540194881339L;
    /** 功能指令代码 */
    private Byte functionId;
    /** 控制器设备序列号 */
    private Long controllerSN;
    /** 最后一条记录的索引号 */
    private Long lastIndexNumber;
    /** 纪录方式   01: 表示刷卡纪录 */
    private String recordMode = "00";
    /** 是否通过  00:表示不通过  */
    private String isPassed = "00";
    /** 通道门  01：一号门  */
    private String aisleNumber = "00";
    /** 进出类型 01: 进门  */
    private String inAndOut = "00";
    /** 卡号  */
    private Long cardNo;
    /** 最后刷卡时间 */
    private String lastUseOfCard = "00";
    /** 纪录原因代码 */
    private String reason = "00";
    private String  otherParam = " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    public DatagramInfo (Byte functionId, Long controllerSN, Long cardNo ) {

    }

}
