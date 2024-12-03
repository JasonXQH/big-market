package io.github.jasonxqh.infrastructure.dao;

import io.github.jasonxqh.infrastructure.dao.po.rebate.DailyBehaviorRebate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IDailyBehaviorRebateDao {
    DailyBehaviorRebate queryDailyBehaviorRebateByBehaviorTypeAndRebateType(DailyBehaviorRebate dailyBehaviorRebateReq);

    List<DailyBehaviorRebate> queryDailyBehaviorRebateByBehaviorType(String behaviorType);
}