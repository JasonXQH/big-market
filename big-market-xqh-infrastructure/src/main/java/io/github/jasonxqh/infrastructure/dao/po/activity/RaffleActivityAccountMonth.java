package io.github.jasonxqh.infrastructure.dao.po.activity;

import java.util.Date;
import lombok.Data;

/**
 * 抽奖活动账户表-月次数
 */
@Data
public class RaffleActivityAccountMonth {
    /**
    * 自增ID
    */
    private Integer id;

    /**
    * 用户ID
    */
    private String userId;

    /**
    * 活动ID
    */
    private Long activityId;

    /**
    * 月（yyyy-mm）
    */
    private String month;

    /**
    * 月次数
    */
    private Integer monthCount;

    /**
    * 月次数-剩余
    */
    private Integer monthCountSurplus;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;
}