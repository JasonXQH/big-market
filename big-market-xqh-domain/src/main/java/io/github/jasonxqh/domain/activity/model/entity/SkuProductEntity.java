package io.github.jasonxqh.domain.activity.model.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuProductEntity {
    /**
     * 商品sku
     */
    private Long sku;
    /**
     * 活动ID
     */
    private Long activityId;
    /**
     * 活动个人参与次数ID
     */
    private Long activityCountId;
    /**
     * 库存总量
     */
    private Integer stockCount;
    /**
     * 剩余库存
     */
    private Integer stockCountSurplus;
    /**
     * 商品金额【积分】
     */
    private BigDecimal productAmount;

    /**
     * 活动配置的次数 - 购买商品后可以获得的次数
     */
    private RaffleActivityCountEntity activityCount;
}
