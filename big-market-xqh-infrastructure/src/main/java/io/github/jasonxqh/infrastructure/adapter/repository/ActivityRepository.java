package io.github.jasonxqh.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.aggregate.CreateOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.*;
import io.github.jasonxqh.infrastructure.dao.*;
import io.github.jasonxqh.infrastructure.dao.po.activity.*;
import io.github.jasonxqh.infrastructure.redis.IRedisService;
import io.github.jasonxqh.types.common.Constants;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
@Slf4j
@Repository
public class ActivityRepository implements IActivityRepository {
    @Resource
    private IRedisService redisService;

    @Resource
    private IRaffleActivityDao activityDao;

    @Resource
   private IRaffleActivityCountDao countDao;

    @Resource
   private IRaffleActivitySkuDao skuDao;

    @Resource
   private IRaffleActivityAccountDao accountDao;

    @Resource
   private IRaffleActivityOrderDao orderDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy routerStrategy;
    @Autowired
    private IDBRouterStrategy dbRouterStrategy;


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

    @Override
    public void doSaveOrder(CreateOrderAggregate createOrderAggregate) {
        try{
            RaffleActivityOrderEntity raffleActivityOrderEntity = createOrderAggregate.getRaffleActivityOrderEntity();
            RaffleActivityAccountEntity raffleActivityAccountEntity = createOrderAggregate.getRaffleActivityAccountEntity();
             RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
             raffleActivityOrder.setUserId(raffleActivityOrderEntity.getUserId());
             raffleActivityOrder.setSku(raffleActivityOrderEntity.getSku());
             raffleActivityOrder.setActivityId(raffleActivityOrderEntity.getActivityId());
             raffleActivityOrder.setActivityName(raffleActivityOrderEntity.getActivityName());
             raffleActivityOrder.setStrategyId(raffleActivityOrderEntity.getStrategyId());
             raffleActivityOrder.setOrderId(raffleActivityOrderEntity.getOrderId());
             raffleActivityOrder.setOrderTime(raffleActivityOrderEntity.getOrderTime());
             raffleActivityOrder.setTotalCount(raffleActivityOrderEntity.getTotalCount());
             raffleActivityOrder.setDayCount(raffleActivityOrderEntity.getDayCount());
             raffleActivityOrder.setMonthCount(raffleActivityOrderEntity.getMonthCount());
             raffleActivityOrder.setState(raffleActivityOrderEntity.getState().getCode());
             raffleActivityOrder.setOutBusinessNo(raffleActivityOrderEntity.getOutBusinessNo());

            RaffleActivityAccount raffleActivityAccount = new RaffleActivityAccount();
             raffleActivityAccount.setUserId(raffleActivityAccountEntity.getUserId());
             raffleActivityAccount.setActivityId(raffleActivityAccountEntity.getActivityId());
             raffleActivityAccount.setTotalCount(raffleActivityAccountEntity.getTotalCount());
             raffleActivityAccount.setTotalCountSurplus(raffleActivityAccountEntity.getTotalCount());
             raffleActivityAccount.setDayCount(raffleActivityAccountEntity.getDayCount());
             raffleActivityAccount.setDayCountSurplus(raffleActivityAccountEntity.getDayCount());
             raffleActivityAccount.setMonthCount(raffleActivityAccountEntity.getMonthCount());
             raffleActivityAccount.setMonthCountSurplus(raffleActivityAccountEntity.getMonthCount());
            log.info("account json: {}" , JSON.toJSON(raffleActivityAccount));
             dbRouterStrategy.doRouter(raffleActivityAccount.getUserId());
                 //编程式事务
                 transactionTemplate.execute(status -> {
                        try{
                            //1.写入订单
                            orderDao.insert(raffleActivityOrder);
                            //2.更新账户
                            int count = accountDao.updateAccountQuota(raffleActivityAccount);
                            //3.创建账户-更新为0。说明更新失败了
                            if( 0 == count){
                                accountDao.insert(raffleActivityAccount);
                            }
                            return 1;

                        }catch (DuplicateKeyException e){
                            status.setRollbackOnly();
                            log.error("写入订单记录，唯一索引冲突 userId:{},activityId:{} sku:{}",raffleActivityOrder.getUserId(),raffleActivityOrder.getActivityId(),raffleActivityOrder.getSku());
                            throw new AppException(ResponseCode.INDEX_DUP.getCode());
                        }
                 });
        }finally {
            dbRouterStrategy.clear();
        }

    }
}
