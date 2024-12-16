package io.github.jasonxqh.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuProductResponseDTO {

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

    /**
     *  购买价格
     * */
    private BigDecimal productAmount;

    private ActivityCount activityCount;

    @Data
    public static class ActivityCount {
        private Integer totalCount;
        private Integer dayCount;
        private Integer monthCount;
    }
}
