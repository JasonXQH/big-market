package io.github.jasonxqh.domain.activity.service;

import io.github.jasonxqh.domain.activity.model.aggregate.CreateOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityOrderEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityShopCartEntity;

public interface IRaffleOrder {
    /**
     * 以sku创建抽奖活动订单，获得参与抽奖的资格
     *
     * @param shopCartEntity
     * @return {@link RaffleActivityOrderEntity }
     */
    RaffleActivityOrderEntity createRaffleActivityOrder(RaffleActivityShopCartEntity shopCartEntity);
}
