package io.github.jasonxqh.domain.activity.model.entity;

import io.github.jasonxqh.domain.activity.model.valobj.OrderStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 抽奖活动单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleActivityOrderEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商品sku
     */
    private Long sku;

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
     * 总次数
     */
    private Integer totalCount;

    /**
     * 日次数
     */
    private Integer dayCount;

    /**
     * 月次数
     */
    private Integer monthCount;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 外部透传字段
     */
    private String outBusinessNo;

    /**
     * 订单状态（complete完成,wait_pay等待付款）
     */
    private OrderStateVO state;
}