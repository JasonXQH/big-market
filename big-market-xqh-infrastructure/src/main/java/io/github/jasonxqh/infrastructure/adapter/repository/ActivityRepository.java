package io.github.jasonxqh.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.event.ActivitySkuStockZeroMessageEvent;
import io.github.jasonxqh.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import io.github.jasonxqh.domain.activity.model.aggregate.CreateSkuQuotaOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.*;
import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import io.github.jasonxqh.domain.activity.model.valobj.ActivityStateVO;
import io.github.jasonxqh.domain.activity.model.valobj.UserRaffleOrderStateVO;
import io.github.jasonxqh.infrastructure.adapter.support.QueueManager;
import io.github.jasonxqh.infrastructure.dao.*;
import io.github.jasonxqh.infrastructure.dao.po.activity.*;
import io.github.jasonxqh.infrastructure.dao.po.strategy.UserRaffleOrder;
import io.github.jasonxqh.infrastructure.event.EventPublisher;
import io.github.jasonxqh.infrastructure.redis.IRedisService;
import io.github.jasonxqh.types.common.Constants;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private IRaffleActivityAccountMonthDao accountMonthDao;

    @Resource
    private IRaffleActivityAccountDayDao accountDayDao;

    @Resource
   private IRaffleActivityOrderDao raffleActivityOrderDao;

    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy routerStrategy;

    @Resource
    private IDBRouterStrategy dbRouter;
    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private ActivitySkuStockZeroMessageEvent activitySkuStockZeroMessageEvent;
    @Autowired
    private QueueManager queueManager;


    @Override
    public RaffleActivitySkuEntity queryActivitySku(Long sku) {
        RaffleActivitySku raffleActivitySku = skuDao.queryActivitySku(sku);
         RaffleActivitySkuEntity skuEntity = RaffleActivitySkuEntity.builder()
                  .sku(raffleActivitySku.getSku())
                  .activityId(raffleActivitySku.getActivityId())
                  .activityCountId(raffleActivitySku.getActivityCountId())
                  .stockCount(raffleActivitySku.getStockCount())
                  .productAmount(raffleActivitySku.getProductAmount())
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
                  .state(ActivityStateVO.valueOf(raffleActivity.getState()))
                  .build();
        // 获取 endDateTime 的时间戳（单位：毫秒）
        long endDateTime = raffleActivity.getEndDateTime().getTime();
        long expireTimeinMillis = (endDateTime - System.currentTimeMillis());
        // 增加过期时间
         redisService.setValue(cacheKey, activityEntity,expireTimeinMillis);
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
        log.info("raffleActivityCount : {}", JSON.toJSON(raffleActivityCount));
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
    public void doSaveNoPayOrder(CreateSkuQuotaOrderAggregate createSkuQuotaOrderAggregate) {
        String userId = createSkuQuotaOrderAggregate.getRaffleActivityOrderEntity().getUserId();
        Long activityId = createSkuQuotaOrderAggregate.getRaffleActivityOrderEntity().getActivityId();
        RLock lock = redisService.getLock(Constants.RedisKey.ACTIVITY_ACCOUNT_LOCK + userId  + Constants.UNDERLINE + activityId);
        try{
            lock.lock(3, TimeUnit.SECONDS);
            RaffleActivityOrderEntity raffleActivityOrderEntity = createSkuQuotaOrderAggregate.getRaffleActivityOrderEntity();
            RaffleActivityAccountEntity raffleActivityAccountEntity = createSkuQuotaOrderAggregate.getRaffleActivityAccountEntity();
            RaffleActivityOrder raffleActivityOrder = getRaffleActivityOrder(raffleActivityOrderEntity);

            RaffleActivityAccount raffleActivityAccountReq = new RaffleActivityAccount();
            raffleActivityAccountReq.setUserId(raffleActivityAccountEntity.getUserId());
            raffleActivityAccountReq.setActivityId(raffleActivityAccountEntity.getActivityId());
            raffleActivityAccountReq.setTotalCount(raffleActivityAccountEntity.getTotalCount());
            raffleActivityAccountReq.setTotalCountSurplus(raffleActivityAccountEntity.getTotalCount());
            raffleActivityAccountReq.setDayCount(raffleActivityAccountEntity.getDayCount());
            raffleActivityAccountReq.setDayCountSurplus(raffleActivityAccountEntity.getDayCount());
            raffleActivityAccountReq.setMonthCount(raffleActivityAccountEntity.getMonthCount());
            raffleActivityAccountReq.setMonthCountSurplus(raffleActivityAccountEntity.getMonthCount());

            // 账户对象 - 月
            RaffleActivityAccountMonth raffleActivityAccountMonth = new RaffleActivityAccountMonth();
            raffleActivityAccountMonth.setUserId(raffleActivityAccountEntity.getUserId());
            raffleActivityAccountMonth.setActivityId(raffleActivityAccountEntity.getActivityId());
            raffleActivityAccountMonth.setMonth(RaffleActivityAccountMonth.currentMonth());
            raffleActivityAccountMonth.setMonthCount(raffleActivityAccountEntity.getMonthCount());
            raffleActivityAccountMonth.setMonthCountSurplus(raffleActivityAccountEntity.getMonthCount());

            // 账户对象 - 日
            RaffleActivityAccountDay raffleActivityAccountDay = new RaffleActivityAccountDay();
            raffleActivityAccountDay.setUserId(raffleActivityAccountEntity.getUserId());
            raffleActivityAccountDay.setActivityId(raffleActivityAccountEntity.getActivityId());
            raffleActivityAccountDay.setDay(RaffleActivityAccountDay.currentDay());
            raffleActivityAccountDay.setDayCount(raffleActivityAccountEntity.getDayCount());
            raffleActivityAccountDay.setDayCountSurplus(raffleActivityAccountEntity.getDayCount());
            routerStrategy.doRouter(raffleActivityAccountReq.getUserId());
            //编程式事务
            transactionTemplate.execute(status -> {
                try{
                    //1.写入订单
                    int success = raffleActivityOrderDao.insert(raffleActivityOrder);
                    if(success != 1) {
                        log.error("写入 raffleActivityOrder 错误， userId:{},activityId:{} sku:{}",raffleActivityOrder.getUserId(),raffleActivityOrder.getActivityId(),raffleActivityOrder.getSku());
                    }
                    //2.更新账户
                    RaffleActivityAccount raffleActivityAccountRes = accountDao.queryActivityAccountByUserIdAndActivityId(raffleActivityAccountReq);
                    if (null == raffleActivityAccountRes) {
                        accountDao.insert(raffleActivityAccountReq);
                    } else {
                        accountDao.updateAccountQuota(raffleActivityAccountReq);
                    }
                    //4.同时要更新月账户和日账户
                    accountDayDao.updateActivityAccountDayAddQuota(raffleActivityAccountDay);
                    accountMonthDao.updateActivityAccountMonthAddQuota(raffleActivityAccountMonth);
                    return 1;
                }catch (DuplicateKeyException e){
                    status.setRollbackOnly();
                    log.error("写入订单记录，唯一索引冲突 userId:{},activityId:{} sku:{}",raffleActivityOrder.getUserId(),raffleActivityOrder.getActivityId(),raffleActivityOrder.getSku());
                    throw new AppException(ResponseCode.INDEX_DUP.getCode());
                }
            });
        }finally {
            routerStrategy.clear();
            lock.unlock();
        }
    }

    private static RaffleActivityOrder getRaffleActivityOrder(RaffleActivityOrderEntity raffleActivityOrderEntity) {
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
        return raffleActivityOrder;
    }

    @Override
    public void storeActivitySkuStockCount(String key, Integer count) {
        if (redisService.isExists(key)) return;
        redisService.setAtomicLong(key,count);
    }



    @Override
    public void awardSkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        RDelayedQueue<ActivitySkuStockKeyVO> activitySkuStockKeyVORDelayedQueue = queueManager.getOrCreateActivitySkuStockKeyVORDelayedQueue(activitySkuStockKeyVO);
        log.info("向sku专用延迟队列传入Sku VO: {}", JSON.toJSONString(activitySkuStockKeyVO));
        activitySkuStockKeyVORDelayedQueue.offer(activitySkuStockKeyVO,3, TimeUnit.SECONDS);
    }

    @Override
    public List<ActivitySkuStockKeyVO> takeQueueValue() {
        List<ActivitySkuStockKeyVO> skuStockKeyVOList = new ArrayList<>();
        Map<String,RBlockingQueue<ActivitySkuStockKeyVO>> queueMap = queueManager.getAllActivitySkuStockKeyVORBlockingQueues();
        for(Map.Entry<String,RBlockingQueue<ActivitySkuStockKeyVO>>entry:queueMap.entrySet()){
            RBlockingQueue<ActivitySkuStockKeyVO> queue = entry.getValue();
            ActivitySkuStockKeyVO activitySkuStockKeyVO =queue.poll();
            if(null != activitySkuStockKeyVO){
                skuStockKeyVOList.add(activitySkuStockKeyVO);
            }
        }
        return skuStockKeyVOList;
    }

    @Override
    public void updateSkuStock(Long sku, Long activityId) {
        RaffleActivitySku skuReq = new RaffleActivitySku();
        skuReq.setSku(sku);
        skuReq.setActivityId(activityId);
        skuDao.updateSkuStock(skuReq);
    }

    @Override
    public Boolean substractionSkuStock(RaffleActivitySkuEntity raffleActivitySku, String cacheKey, Date endDateTime) {
        Long sku = raffleActivitySku.getSku();
        Long activityId = raffleActivitySku.getActivityId();
        long surplus = redisService.decr(cacheKey);
            if(surplus == 0){
                eventPublisher.publish(activitySkuStockZeroMessageEvent.getTopic(), activitySkuStockZeroMessageEvent.buildEventMessage(ActivitySkuStockKeyVO.builder()
                        .activityId(activityId)
                        .sku(sku)
                        .build()
                ));
                return false;
            } else if (surplus < 0) {
                redisService.setAtomicLong(cacheKey, 0);
                return false;
            }
            // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
            // 2. 加锁为了兜底，如果后续有恢复库存，手动处理等，也不会超卖。因为所有的可用库存key，都被加锁了
            String lockKey = cacheKey + Constants.UNDERLINE + surplus;
            Long expireMillis = endDateTime.getTime() - System.currentTimeMillis()+TimeUnit.DAYS.toMillis(1);
            Boolean lock = redisService.setNx(lockKey,expireMillis,TimeUnit.MILLISECONDS);
            if(!lock) log.info("Sku库存扣减失败 {}",lockKey);
            return lock;
    }

    @Override
    public void clearQueueValue(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        String queueKey = activitySkuStockKeyVO.getActivityId() + "_" + activitySkuStockKeyVO.getSku();
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY + queueKey;
        RBlockingQueue<Object> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<Object> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        blockingQueue.clear();
        delayedQueue.clear();
    }

    @Override
    public void clearActivitySkuStock(ActivitySkuStockKeyVO skuStockKeyVOReq){
        RaffleActivitySku skuReq = new RaffleActivitySku();
        skuReq.setSku(skuStockKeyVOReq.getSku());
        skuReq.setActivityId(skuStockKeyVOReq.getActivityId());
        skuDao.clearActivitySkuStock(skuReq);
    }

    @Override
    public UserRaffleOrderEntity queryUnusedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        // 查询数据
        UserRaffleOrder userRaffleOrderReq = new UserRaffleOrder();
        userRaffleOrderReq.setUserId(partakeRaffleActivityEntity.getUserId());
        userRaffleOrderReq.setActivityId(partakeRaffleActivityEntity.getActivityId());

        UserRaffleOrder userRaffleOrderRes = userRaffleOrderDao.queryUnusedUserRaffleOrder(userRaffleOrderReq);
        if (null == userRaffleOrderRes) return null;
        return UserRaffleOrderEntity.builder()
                  .userId(userRaffleOrderRes.getUserId())
                  .activityId(userRaffleOrderRes.getActivityId())
                  .activityName(userRaffleOrderRes.getActivityName())
                  .strategyId(userRaffleOrderRes.getStrategyId())
                  .orderId(userRaffleOrderRes.getOrderId())
                  .orderTime(userRaffleOrderRes.getOrderTime())
                  .orderState(UserRaffleOrderStateVO.valueOf(userRaffleOrderRes.getOrderState()))
                  .build();
    }

    @Override
    public RaffleActivityAccountEntity queryActivityAccountByUserIdAndActivityId(String userId, Long activityId) {
        RaffleActivityAccount accountReq = new RaffleActivityAccount();
        accountReq.setUserId(userId);
        accountReq.setActivityId(activityId);
        RaffleActivityAccount raffleActivityAccountRes = accountDao.queryActivityAccountByUserIdAndActivityId(accountReq);
        if(null == raffleActivityAccountRes) return null;
        return RaffleActivityAccountEntity.builder()
                  .userId(raffleActivityAccountRes.getUserId())
                  .activityId(raffleActivityAccountRes.getActivityId())
                  .totalCount(raffleActivityAccountRes.getTotalCount())
                  .totalCountSurplus(raffleActivityAccountRes.getTotalCountSurplus())
                  .dayCount(raffleActivityAccountRes.getDayCount())
                  .dayCountSurplus(raffleActivityAccountRes.getDayCountSurplus())
                  .monthCount(raffleActivityAccountRes.getMonthCount())
                  .monthCountSurplus(raffleActivityAccountRes.getMonthCountSurplus())
                  .build();
    }

    @Override
    public RaffleActivityAccountEntity queryActivityAccountByUserIdAndActivityIdFromFront(String userId, Long activityId) {
        RaffleActivityAccount accountReq = new RaffleActivityAccount();
        accountReq.setUserId(userId);
        accountReq.setActivityId(activityId);
        RaffleActivityAccount raffleActivityAccountRes = accountDao.queryActivityAccountByUserIdAndActivityId(accountReq);

        if(null == raffleActivityAccountRes) return RaffleActivityAccountEntity.builder()
                .userId(userId)
                .activityId(activityId)
                .totalCount(0)
                .totalCountSurplus(0)
                .dayCount(0)
                .dayCountSurplus(0)
                .monthCount(0)
                .monthCountSurplus(0)
                .build();

        //查询月、日账户的额度。
        RaffleActivityAccountMonth raffleActivityAccountMonth = accountMonthDao.queryActivityAccountMonthByUserId(RaffleActivityAccountMonth.builder()
                .activityId(activityId)
                .userId(userId)
                .build()
        );

        RaffleActivityAccountDay raffleActivityAccountDay = accountDayDao.queryActivityAccountDayByUserId(RaffleActivityAccountDay.builder()
                .activityId(activityId)
                .userId(userId)
                .build()
        );
        //组装AccountEntity对象
        RaffleActivityAccountEntity raffleActivityAccountEntity = new RaffleActivityAccountEntity();
        raffleActivityAccountEntity.setUserId(userId);
        raffleActivityAccountEntity.setActivityId(activityId);
        raffleActivityAccountEntity.setTotalCount(raffleActivityAccountRes.getTotalCount());
        raffleActivityAccountEntity.setTotalCountSurplus(raffleActivityAccountRes.getTotalCountSurplus());

        //当前还没进行抽奖
        if(null == raffleActivityAccountDay){
            raffleActivityAccountEntity.setDayCount(raffleActivityAccountRes.getDayCount());
            raffleActivityAccountEntity.setDayCountSurplus(raffleActivityAccountRes.getDayCountSurplus());
        }else {
            raffleActivityAccountRes.setDayCount(raffleActivityAccountDay.getDayCount());
            raffleActivityAccountDay.setDayCountSurplus(raffleActivityAccountDay.getDayCountSurplus());
        }

        if(null == raffleActivityAccountMonth){
            raffleActivityAccountEntity.setMonthCount(raffleActivityAccountRes.getMonthCount());
            raffleActivityAccountEntity.setMonthCountSurplus(raffleActivityAccountRes.getMonthCountSurplus());
        }else{
            raffleActivityAccountRes.setMonthCount(raffleActivityAccountMonth.getMonthCount());
            raffleActivityAccountRes.setMonthCountSurplus(raffleActivityAccountMonth.getMonthCountSurplus());
        }

        return raffleActivityAccountEntity;
    }


    @Override
    public RaffleActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month) {
        RaffleActivityAccountMonth raffleActivityAccountMonthReq  = new RaffleActivityAccountMonth();
        raffleActivityAccountMonthReq.setUserId(userId);
        raffleActivityAccountMonthReq.setActivityId(activityId);
        raffleActivityAccountMonthReq.setMonth(month);

        RaffleActivityAccountMonth raffleActivityAccountMonthRes = accountMonthDao.queryActivityAccountMonthByUserId(raffleActivityAccountMonthReq);
        if(null == raffleActivityAccountMonthRes) return null;
        return RaffleActivityAccountMonthEntity.builder()
                  .userId(raffleActivityAccountMonthRes.getUserId())
                  .activityId(raffleActivityAccountMonthRes.getActivityId())
                  .month(raffleActivityAccountMonthRes.getMonth())
                  .monthCount(raffleActivityAccountMonthRes.getMonthCount())
                  .monthCountSurplus(raffleActivityAccountMonthRes.getMonthCountSurplus())
                  .build();

    }

    @Override
    public RaffleActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day) {
        RaffleActivityAccountDay raffleActivityAccountDayReq  = new RaffleActivityAccountDay();
        raffleActivityAccountDayReq.setUserId(userId);
        raffleActivityAccountDayReq.setActivityId(activityId);
        raffleActivityAccountDayReq.setDay(day);

        RaffleActivityAccountDay raffleActivityAccountDayRes = accountDayDao.queryActivityAccountDayByUserId(raffleActivityAccountDayReq);

        if(null == raffleActivityAccountDayRes) return null;
        return RaffleActivityAccountDayEntity.builder()
                .userId(raffleActivityAccountDayRes.getUserId())
                .activityId(raffleActivityAccountDayRes.getActivityId())
                .day(raffleActivityAccountDayRes.getDay())
                .dayCount(raffleActivityAccountDayRes.getDayCount())
                .dayCountSurplus(raffleActivityAccountDayRes.getDayCountSurplus())
                .build();

    }

    @Override
    public void doSavePartakeOrder(CreatePartakeOrderAggregate partakeOrderAggregate) {
        try{
            String userId = partakeOrderAggregate.getUserId();
            Long activityId = partakeOrderAggregate.getActivityId();
            RaffleActivityAccountEntity raffleActivityAccountEntity = partakeOrderAggregate.getRaffleActivityAccountEntity();
            RaffleActivityAccountMonthEntity raffleActivityAccountMonthEntity = partakeOrderAggregate.getRaffleActivityAccountMonthEntity();
            RaffleActivityAccountDayEntity raffleActivityAccountDayEntity = partakeOrderAggregate.getRaffleActivityAccountDayEntity();
            UserRaffleOrder userRaffleOrder = getUserRaffleOrder(partakeOrderAggregate);
            routerStrategy.doRouter(partakeOrderAggregate.getUserId());
            //编程式事务
            transactionTemplate.execute(status -> {
                try{
                    //1.写入user raffle order
                    userRaffleOrderDao.insertUserRaffleOrder(userRaffleOrder);

                    //2.更新总账户
                    int totalCount = accountDao.updateActivityAccountSubstractionQuota(
                            RaffleActivityAccount.builder()
                                    .userId(userId)
                                    .activityId(activityId)
                                    .build()
                    );
                    //3.更新为0。说明更新失败了
                    if( totalCount != 1){
                       status.setRollbackOnly();
                       log.warn("写入创建参与活动记录，更新总账户额度不足，异常 userId:{},activityId:{}",userId,activityId);
                       throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(),ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
                    }
                    //4.更新月额度
                    if(partakeOrderAggregate.isExistAccountMonth()){
                        //存在，就更新
                        int monthCount = accountMonthDao.updateActivityAccountMonthSubstractionQuota(RaffleActivityAccountMonth
                                .builder()
                                .activityId(activityId)
                                .userId(userId)
                                .month(raffleActivityAccountMonthEntity.getMonth())
                                .build()
                        );
                        if( 1 != monthCount){
                            status.setRollbackOnly();
                            throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(),ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
                        }
                    }else{
                        //不存在，就插入
                        accountMonthDao.insertActivityAccountMonth(RaffleActivityAccountMonth
                                .builder()
                                .activityId(activityId)
                                .userId(userId)
                                .month(raffleActivityAccountMonthEntity.getMonth())
                                .monthCount(raffleActivityAccountMonthEntity.getMonthCount())
                                .monthCountSurplus(raffleActivityAccountMonthEntity.getMonthCountSurplus()-1)
                                .build());
                        // 新创建月账户，则更新总账表中月镜像额度
                        accountDao.updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccount.builder()
                                .userId(userId)
                                .activityId(activityId)
                                .monthCountSurplus(raffleActivityAccountEntity.getMonthCountSurplus())
                                .build());
                    }
                    //5.更新日额度
                    if(partakeOrderAggregate.isExistAccountDay()){
                        //存在，就更新
                        int dayCount = accountDayDao.updateActivityAccountDaySubstractionQuota(
                                RaffleActivityAccountDay.builder()
                                        .activityId(activityId)
                                        .userId(userId)
                                        .day(raffleActivityAccountDayEntity.getDay())
                                        .build()
                        );
                        if( 1 != dayCount){
                            status.setRollbackOnly();
                            throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(),ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
                        }

                    }else{
                        //不存在，就插入
                        accountDayDao.insertActivityAccountDay(RaffleActivityAccountDay.builder()
                                .activityId(activityId)
                                .userId(userId)
                                .day(raffleActivityAccountDayEntity.getDay())
                                .dayCount(raffleActivityAccountDayEntity.getDayCount())
                                .dayCountSurplus(raffleActivityAccountDayEntity.getDayCountSurplus()-1)
                                .build()
                        );
                        accountDao.updateActivityAccountDaySurplusImageQuota(
                                RaffleActivityAccount.builder()
                                        .activityId(activityId)
                                        .userId(userId)
                                        .dayCountSurplus(raffleActivityAccountDayEntity.getDayCountSurplus())
                                        .build()
                        );
                    }
                    return 1;

                }catch (DuplicateKeyException e){
                    status.setRollbackOnly();
                    log.error("写入创建参与活动记录，唯一索引冲突 userId:{},activityId:{}",partakeOrderAggregate.getUserId(),partakeOrderAggregate.getActivityId(),e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(),e.getMessage());
                }
            });
        }finally {
            routerStrategy.clear();
        }
    }

    @Override
    public void doSaveCreditPayOrder(CreateSkuQuotaOrderAggregate createSkuQuotaOrderAggregate) {
        try {
            String userId = createSkuQuotaOrderAggregate.getRaffleActivityOrderEntity().getUserId();
            Long activityId = createSkuQuotaOrderAggregate.getRaffleActivityOrderEntity().getActivityId();
            // 创建交易订单
            RaffleActivityOrderEntity raffleActivityOrderEntity = createSkuQuotaOrderAggregate.getRaffleActivityOrderEntity();
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
            raffleActivityOrder.setTotalCount(raffleActivityOrderEntity.getTotalCount());
            raffleActivityOrder.setDayCount(raffleActivityOrderEntity.getDayCount());
            raffleActivityOrder.setMonthCount(raffleActivityOrderEntity.getMonthCount());
            raffleActivityOrder.setPayAmount(raffleActivityOrderEntity.getPayAmount());
            raffleActivityOrder.setState(raffleActivityOrderEntity.getState().getCode());
            raffleActivityOrder.setOutBusinessNo(raffleActivityOrderEntity.getOutBusinessNo());

            // 以用户ID作为切分键，通过 doRouter 设定路由【这样就保证了下面的操作，都是同一个链接下，也就保证了事务的特性】
            routerStrategy.doRouter(userId);
            // 编程式事务
            transactionTemplate.execute(status -> {
                try {
                    raffleActivityOrderDao.insert(raffleActivityOrder);
                    log.info("写入积分消费订单记录成功 userId: {} activityId: {} sku: {}", userId,activityId, raffleActivityOrderEntity.getSku());
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入积分消费订单记录，唯一索引冲突 userId: {} activityId: {} sku: {}", userId,activityId, raffleActivityOrderEntity.getSku(), e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        } finally {
            routerStrategy.clear();
        }
    }

    @Override
    public void updateOrder(DeliveryOrderEntity deliveryOrderEntity) {
        RLock lock = redisService.getLock(Constants.RedisKey.ACTIVITY_ACCOUNT_UPDATE_LOCK + deliveryOrderEntity.getUserId());
        try {
            lock.lock(3, TimeUnit.SECONDS);

            // 查询订单
            RaffleActivityOrder raffleActivityOrderReq = new RaffleActivityOrder();
            raffleActivityOrderReq.setUserId(deliveryOrderEntity.getUserId());
            raffleActivityOrderReq.setOutBusinessNo(deliveryOrderEntity.getOutBusinessNo());
            //null
            RaffleActivityOrder raffleActivityOrderRes = raffleActivityOrderDao.queryRaffleActivityOrderByUserIdAndOutBN(raffleActivityOrderReq);
            if(raffleActivityOrderRes == null) {
                log.info("无法从raffle_activity_order中找到对应订单，可能是no_pay类型的订单,userId:{},outBusnessNo:{}",deliveryOrderEntity.getUserId(),deliveryOrderEntity.getOutBusinessNo());
                return;
            }
            // 账户对象 - 总
            RaffleActivityAccount raffleActivityAccount = new RaffleActivityAccount();
            raffleActivityAccount.setUserId(raffleActivityOrderRes.getUserId());
            raffleActivityAccount.setActivityId(raffleActivityOrderRes.getActivityId());
            raffleActivityAccount.setTotalCount(raffleActivityOrderRes.getTotalCount());
            raffleActivityAccount.setTotalCountSurplus(raffleActivityOrderRes.getTotalCount());
            raffleActivityAccount.setDayCount(raffleActivityOrderRes.getDayCount());
            raffleActivityAccount.setDayCountSurplus(raffleActivityOrderRes.getDayCount());
            raffleActivityAccount.setMonthCount(raffleActivityOrderRes.getMonthCount());
            raffleActivityAccount.setMonthCountSurplus(raffleActivityOrderRes.getMonthCount());

            // 账户对象 - 月
            RaffleActivityAccountMonth raffleActivityAccountMonth = new RaffleActivityAccountMonth();
            raffleActivityAccountMonth.setUserId(raffleActivityOrderRes.getUserId());
            raffleActivityAccountMonth.setActivityId(raffleActivityOrderRes.getActivityId());
            raffleActivityAccountMonth.setMonth(RaffleActivityAccountMonth.currentMonth());
            raffleActivityAccountMonth.setMonthCount(raffleActivityOrderRes.getMonthCount());
            raffleActivityAccountMonth.setMonthCountSurplus(raffleActivityOrderRes.getMonthCount());

            // 账户对象 - 日
            RaffleActivityAccountDay raffleActivityAccountDay = new RaffleActivityAccountDay();
            raffleActivityAccountDay.setUserId(raffleActivityOrderRes.getUserId());
            raffleActivityAccountDay.setActivityId(raffleActivityOrderRes.getActivityId());
            raffleActivityAccountDay.setDay(RaffleActivityAccountDay.currentDay());
            raffleActivityAccountDay.setDayCount(raffleActivityOrderRes.getDayCount());
            raffleActivityAccountDay.setDayCountSurplus(raffleActivityOrderRes.getDayCount());


            dbRouter.doRouter(deliveryOrderEntity.getUserId());
            // 编程式事务
            transactionTemplate.execute(status -> {
                try {
                    // 1. 更新订单
                    int updateCount = raffleActivityOrderDao.updateOrderCompleted(raffleActivityOrderReq);
                    if (1 != updateCount) {
                        status.setRollbackOnly();
                        return 1;
                    }
                    // 2. 更新账户 - 总
                    RaffleActivityAccount raffleActivityAccountRes = accountDao.queryActivityAccountByUserIdAndActivityId(raffleActivityAccount);
                    if (null == raffleActivityAccountRes) {
                        accountDao.insert(raffleActivityAccount);
                    } else {
                        accountDao.updateAccountQuota(raffleActivityAccount);
                    }
                    // 4. 更新账户 - 月
                    accountMonthDao.updateActivityAccountMonthAddQuota(raffleActivityAccountMonth);
                    // 5. 更新账户 - 日
                    accountDayDao.updateActivityAccountDayAddQuota(raffleActivityAccountDay);
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("更新订单记录，完成态，唯一索引冲突 userId: {} outBusinessNo: {}", deliveryOrderEntity.getUserId(), deliveryOrderEntity.getOutBusinessNo(), e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("更新订单记录失败 userId: {} outBusinessNo: {}", deliveryOrderEntity.getUserId(), deliveryOrderEntity.getOutBusinessNo(), e);
                    throw new AppException(ResponseCode.UN_ERROR.getCode(), e);
                }
            });
        } finally {
            dbRouter.clear();
            lock.unlock();
        }
    }

    @Override
    public List<RaffleActivitySkuEntity> queryActivitySkuByActivityId(Long activityId) {
        List<RaffleActivitySku> raffleActivitySkus = skuDao.queryActivitySkuByActivityId(activityId);
        return raffleActivitySkus.stream()
                .map(raffleActivitySku -> RaffleActivitySkuEntity.builder()
                                            .activityId(activityId)
                                            .activityCountId(raffleActivitySku.getActivityCountId())
                                            .sku(raffleActivitySku.getSku())
                                            .stockCount(raffleActivitySku.getStockCount())
                                            .productAmount(raffleActivitySku.getProductAmount())
                                            .stockCountSurplus(raffleActivitySku.getStockCountSurplus())
                                            .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public void cacheActivitySkuStockCount(String cacheKey, Integer stockCount) {
        if (redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, stockCount);
    }

    @Override
    public Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId) {
        RaffleActivityAccountDay raffleActivityAccountDayReq = new RaffleActivityAccountDay();
        raffleActivityAccountDayReq.setActivityId(activityId);
        raffleActivityAccountDayReq.setUserId(userId);
        raffleActivityAccountDayReq.setDay(RaffleActivityAccountDay.currentDay());
        RaffleActivityAccountDay raffleActivityAccountDay = accountDayDao.queryActivityAccountDayByUserId(raffleActivityAccountDayReq);
        return raffleActivityAccountDay == null? 0:raffleActivityAccountDay.getDayCount() - raffleActivityAccountDay.getDayCountSurplus();
    }

    @Override
    public Integer queryActivityAccountPartakeCount(String userId, Long activityId) {
        RaffleActivityAccount raffleActivityAccount = accountDao.queryActivityAccountByUserIdAndActivityId(RaffleActivityAccount.builder()
                .activityId(activityId)
                .userId(userId)
                .build());
        return raffleActivityAccount.getTotalCount()-raffleActivityAccount.getTotalCountSurplus();
    }



    private static UserRaffleOrder getUserRaffleOrder(CreatePartakeOrderAggregate partakeOrderAggregate) {
        UserRaffleOrderEntity userRaffleOrderEntity = partakeOrderAggregate.getUserRaffleOrderEntity();
        return  UserRaffleOrder.builder()
                  .userId(userRaffleOrderEntity.getUserId())
                  .activityId(userRaffleOrderEntity.getActivityId())
                  .activityName(userRaffleOrderEntity.getActivityName())
                  .strategyId(userRaffleOrderEntity.getStrategyId())
                  .orderId(userRaffleOrderEntity.getOrderId())
                  .orderTime(userRaffleOrderEntity.getOrderTime())
                  .orderState(userRaffleOrderEntity.getOrderState().getCode())
                  .build();
    }

}
