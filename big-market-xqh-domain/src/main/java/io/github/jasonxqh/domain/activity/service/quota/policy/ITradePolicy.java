package io.github.jasonxqh.domain.activity.service.quota.policy;

import io.github.jasonxqh.domain.activity.model.aggregate.CreateSkuQuotaOrderAggregate;

/**
 * @author jasonxu
 * @description 交易策略接口，包括；返利兑换（不用支付），积分订单（需要支付）
 * @create 2024-06-08 18:04
 */
public interface ITradePolicy {

    void trade(CreateSkuQuotaOrderAggregate createSkuQuotaOrderAggregate);

}
