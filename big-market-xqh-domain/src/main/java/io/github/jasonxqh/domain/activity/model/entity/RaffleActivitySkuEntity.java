package io.github.jasonxqh.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleActivitySkuEntity {

    /**
    * 商品sku - 把每一个组合当做一个商品
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
    * 商品库存
    */
    private Integer stockCount;

    /**
    * 剩余库存
    */
    private Integer stockCountSurplus;

}