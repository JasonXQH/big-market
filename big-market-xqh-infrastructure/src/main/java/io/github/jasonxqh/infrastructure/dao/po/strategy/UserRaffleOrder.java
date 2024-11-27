package io.github.jasonxqh.infrastructure.dao.po.strategy;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户抽奖订单表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRaffleOrder {
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
    * 活动名称
    */
    private String activityName;

    /**
    * 抽奖策略ID
    */
    private Long strategyId;

    /**
    * 订单ID
    */
    private String orderId;

    /**
    * 下单时间
    */
    private Date orderTime;

    /**
    * 订单状态；create-创建、used-已使用、cancel-已作废
    */
    private String orderState;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;
}