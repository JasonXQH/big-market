package io.github.jasonxqh.infrastructure.adapter.repository;

import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.infrastructure.dao.*;
import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivity;
import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivityCount;
import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivitySku;
import io.github.jasonxqh.infrastructure.redis.IRedisService;
import io.github.jasonxqh.types.common.Constants;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class ActivityRepository implements IActivityRepository {
    @Resource
    IRedisService redisService;

    @Resource
    IRaffleActivityDao activityDao;

    @Resource
    IRaffleActivityCountDao countDao;

    @Resource
    IRaffleActivitySkuDao skuDao;

    @Resource
    IRaffleActivityAccountDao accountDao;

    @Resource
    IRaffleActivityOrderDao orderDao;


    @Override
    public RaffleActivitySkuEntity queryActivitySku(Long sku) {
        RaffleActivitySku raffleActivitySku = skuDao.queryActivitySku(sku);
         RaffleActivitySkuEntity skuEntity = RaffleActivitySkuEntity.builder()
                  .sku(raffleActivitySku.getSku())
                  .activityId(raffleActivitySku.getActivityId())
                  .activityCountId(raffleActivitySku.getActivityCountId())
                  .stockCount(raffleActivitySku.getStockCount())
                  .stockCountSurplus(raffleActivitySku.getStockCountSurplus())
                  .build();
         return skuEntity;
    }

    @Override
    public RaffleActivityEntity queryActivityByActivityId(Long activityId) {
        String cacheKey = Constants.RedisKey.ACTIVITY_KEY + activityId;
        RaffleActivityEntity activityEntity  = redisService.getValue(cacheKey);
        if(activityEntity != null ) {
            return activityEntity;
        }
        RaffleActivity raffleActivity = activityDao.queryRaffleActivityByActivityId(activityId);
         activityEntity = RaffleActivityEntity.builder()
                  .activityId(raffleActivity.getActivityId())
                  .activityName(raffleActivity.getActivityName())
                  .activityDesc(raffleActivity.getActivityDesc())
                  .beginDateTime(raffleActivity.getBeginDateTime())
                  .endDateTime(raffleActivity.getEndDateTime())
                  .strategyId(raffleActivity.getStrategyId())
                  .state(raffleActivity.getState())
                  .build();
         redisService.setValue(cacheKey, activityEntity);
         return activityEntity;
    }

    @Override
    public RaffleActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        String cacheKey = Constants.RedisKey.ACTIVITY_COUNT_KEY + activityCountId;
        RaffleActivityCountEntity countEntity  = redisService.getValue(cacheKey);
        if(countEntity != null ) {
            return countEntity;
        }
        RaffleActivityCount raffleActivityCount = countDao.queryRaffleActivityCountByActivityCountId(activityCountId);
        countEntity = RaffleActivityCountEntity.builder()
                  .activityCountId(raffleActivityCount.getActivityCountId())
                  .totalCount(raffleActivityCount.getTotalCount())
                  .dayCount(raffleActivityCount.getDayCount())
                  .monthCount(raffleActivityCount.getMonthCount())
                  .build();
        redisService.setValue(cacheKey, countEntity);
         return countEntity;
    }
}
