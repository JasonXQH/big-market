package io.github.jasonxqh.domain.rebate.adapter.repository;

import io.github.jasonxqh.domain.rebate.model.aggregate.BehaviorRebateOrderAggregate;
import io.github.jasonxqh.domain.rebate.model.vo.DailyBehaviorRebateVO;

import java.util.List;

public interface  IBehaviorRebateRepository {

    List<DailyBehaviorRebateVO> queryDailyBehaviorRebateByBehaviorType(String code);

    void doSaveRebateOrderAggregate(String userId, List<BehaviorRebateOrderAggregate> behaviorRebateOrderAggregates);
}
