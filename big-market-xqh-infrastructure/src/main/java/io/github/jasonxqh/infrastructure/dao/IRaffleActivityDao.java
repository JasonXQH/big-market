package io.github.jasonxqh.infrastructure.dao;
import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRaffleActivityDao {
    RaffleActivity queryRaffleActivityByActivityId(Long activityId);

    Long queryStrategyIdByActivityId(Long activityId);

    Long queryActivityIdByStrategyId(Long strategyId);
}