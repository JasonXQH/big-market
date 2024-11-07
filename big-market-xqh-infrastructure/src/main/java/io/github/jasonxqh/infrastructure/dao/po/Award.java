package io.github.jasonxqh.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description:  奖品表
 **/
@Data
public class Award {
    /*自增ID*/
    private Long id;
    /*抽奖奖品ID - 内部流转使用*/
    private Long awardId;
    /*品对接标识 - 每一个都是一个对应的发奖策略*/
    private String awardKey;
    /*奖品配置信息*/
    private String awardConfig;
    /*奖品内容描述*/
    private String awardDesc;
    /*创建时间*/
    private Date createTime;
    /*更新时间*/
    private Date updateTime;
}

