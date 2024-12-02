package io.github.jasonxqh.infrastructure.dao;
import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivity;
import org.apache.ibatis.annotations.Mapper;

import java.sql.CallableStatement;
import java.sql.Date;
import java.util.List;

@Mapper
public interface IRaffleActivityDao {
    RaffleActivity queryRaffleActivityByActivityId(Long activityId);

    Long queryStrategyIdByActivityId(Long activityId);

    Long queryActivityIdByStrategyId(Long strategyId);


    Date queryActivityExpiredTimeByActivityId(Long activityId);

    Date queryActivityExpiredTimeByStrategyId(Long activityId);
}