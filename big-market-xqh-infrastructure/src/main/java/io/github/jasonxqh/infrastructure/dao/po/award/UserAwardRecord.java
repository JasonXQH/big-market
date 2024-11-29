package io.github.jasonxqh.infrastructure.dao.po.award;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 用户中奖记录表
 */
@Data
@Builder
public class UserAwardRecord {
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
    * 抽奖策略ID
    */
    private Long strategyId;

    /**
    * 抽奖订单ID【作为幂等使用】
    */
    private String orderId;

    /**
    * 奖品ID
    */
    private Integer awardId;

    /**
    * 奖品标题（名称）
    */
    private String awardTitle;

    /**
    * 中奖时间
    */
    private Date awardTime;

    /**
    * 奖品状态；create-创建、completed-发奖完成
    */
    private String awardState;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;
}