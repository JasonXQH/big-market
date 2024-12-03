package io.github.jasonxqh.domain.rebate.service;

import io.github.jasonxqh.domain.rebate.model.entity.BehaviorEntity;

import java.util.List;

public interface IBehaviorRebateService {

    /**
     * 创建行为动作的入账订单
     *
     * @param behaviorEntity 行为实体对象
     * @return 订单ID
     */
    List<String> createOrder(BehaviorEntity behaviorEntity);
}
