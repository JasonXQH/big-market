package io.github.jasonxqh.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.domain.award.adapter.repository.IAwardRepository;
import io.github.jasonxqh.domain.award.model.aggregate.GiveOutPrizesAggregate;
import io.github.jasonxqh.domain.award.model.aggregate.UserAwardRecordAggregate;
import io.github.jasonxqh.domain.award.model.entity.TaskEntity;
import io.github.jasonxqh.domain.award.model.entity.UserAwardRecordEntity;
import io.github.jasonxqh.domain.award.model.entity.UserCreditRandomAwardEntity;
import io.github.jasonxqh.domain.award.model.vo.AccountStatusVO;
import io.github.jasonxqh.infrastructure.dao.*;
import io.github.jasonxqh.infrastructure.dao.po.Task;
import io.github.jasonxqh.infrastructure.dao.po.award.UserAwardRecord;
import io.github.jasonxqh.infrastructure.dao.po.award.UserCreditAccount;
import io.github.jasonxqh.infrastructure.dao.po.strategy.UserRaffleOrder;
import io.github.jasonxqh.infrastructure.event.EventPublisher;
import io.github.jasonxqh.infrastructure.redis.IRedisService;
import io.github.jasonxqh.types.common.Constants;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


@Slf4j
@Repository
public class AwardRepository implements IAwardRepository {
    @Resource
    private IRedisService redisService;
    @Resource
    private IUserAwardRecordDao userAwardRecordDao;
    @Resource
    private ITaskDao taskDao;
    @Resource
    private IAwardDao awardDao;
    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;
    @Resource
    private IDBRouterStrategy routerStrategy;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private EventPublisher eventPublisher;
    @Resource
    private IUserCreditAccountDao userCreditAccountDao;
    @Override
    public void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate) {
        UserAwardRecordEntity userAwardRecordEntity = userAwardRecordAggregate.getUserAwardRecordEntity();
        TaskEntity taskEntity = userAwardRecordAggregate.getTaskEntity();

        //必要信息
        String userId = userAwardRecordEntity.getUserId();
        Long activityId = userAwardRecordEntity.getActivityId();
        Integer awardId = userAwardRecordEntity.getAwardId();

        UserAwardRecord record = UserAwardRecord.builder()
                .userId(userAwardRecordEntity.getUserId())
                .activityId(userAwardRecordEntity.getActivityId())
                .strategyId(userAwardRecordEntity.getStrategyId())
                .orderId(userAwardRecordEntity.getOrderId())
                .awardId(userAwardRecordEntity.getAwardId())
                .awardTitle(userAwardRecordEntity.getAwardTitle())
                .awardTime(userAwardRecordEntity.getAwardTime())
                .awardState(userAwardRecordEntity.getAwardState().getCode())
                .build();
        Task task = Task.builder()
                .userId(taskEntity.getUserId())
                .topic(taskEntity.getTopic())
                .messageId(taskEntity.getMessageId())
                .message(JSON.toJSONString(taskEntity.getMessage()))
                .state(taskEntity.getState().getCode())
                .build();

        UserRaffleOrder userRaffleOrderReq = UserRaffleOrder.builder()
                .userId(userId)
                .activityId(activityId)
                .orderId(userAwardRecordEntity.getOrderId())
                .build();
        try{
            routerStrategy.doRouter(userId);
            //编程式事务
            transactionTemplate.execute(status -> {
                try{
                    //写入记录
                    userAwardRecordDao.saveUserAwardRecord(record);
                    //写入任务
                    taskDao.saveTask(task);
                    //更新抽奖单
                    int count = userRaffleOrderDao.updateUserRaffleOrderStateUsed(userRaffleOrderReq);
                    if( 1 != count ) {
                        //回滚操作
                        status.setRollbackOnly();
                        log.error("写入中奖记录，用户抽奖单已使用过,userId:{},activityId:{},orderId:{}",userId,activityId,userRaffleOrderReq.getOrderId());
                        throw new AppException(ResponseCode.ACTIVITY_RAFFLE_ORDER_ERROR.getCode(),ResponseCode.ACTIVITY_RAFFLE_ORDER_ERROR.getCode());
                    }
                    return 1;
                }catch (DuplicateKeyException e){
                    status.setRollbackOnly();
                    log.error("写入Task和用户抽奖记录时唯一orderId冲突,userId:{},activityId:{},awardId:{},orderId:{}",userId,activityId,awardId, record.getOrderId(),e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(),ResponseCode.INDEX_DUP.getInfo());
                }
            });
        }finally {
            routerStrategy.clear();
        }
        //用线程池的方式实现：发成功了就成功，失败的话，就重新发送
        try {
            // 发送消息【在事务外执行，如果失败还有任务补偿】
            eventPublisher.publish(task.getTopic(), task.getMessage());
            // 更新数据库记录，task 任务表
            taskDao.updateTaskSendMessageCompleted(task);
            log.info("写入中奖记录，发送MQ消息完成 userId: {} orderId:{} topic: {}", userId, userAwardRecordEntity.getOrderId(), task.getTopic());
        } catch (Exception e) {
            log.error("写入中奖记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
            taskDao.updateTaskSendMessageFail( task);
        }

    }

    @Override
    public String queryAwardConfigByAwardId(Integer awardId) {
        String redisKey = Constants.RedisKey.AWARD_CONFIG_KEY+awardId;
        String awardConfig = redisService.getValue(redisKey);
        if(awardConfig != null) {
            return awardConfig;
        }
        awardConfig = awardDao.queryAwardConfigByAwardId(awardId);
        redisService.setValue(redisKey, awardConfig);
        return awardConfig;
    }

    @Override
    public String queryAwardKeyByAwardId(Integer awardId) {
        String redisKey = Constants.RedisKey.AWARD_KEY+awardId;
        String awardKey = redisService.getValue(redisKey);
        if(awardKey != null) {
            return awardKey;
        }
        awardKey = awardDao.queryAwardKeyByAwardId(awardId);
        redisService.setValue(redisKey, awardKey);
        return awardKey;
    }

    @Override
    public void saveGiveOutPrizesAggregate(GiveOutPrizesAggregate giveOutPrizesAggregate) {
        String userId = giveOutPrizesAggregate.getUserId();
        //随机积分奖励实体
        UserCreditRandomAwardEntity userCreditRandomAwardEntity = giveOutPrizesAggregate.getUserCreditRandomAwardEntity();
        //用户中奖记录单
        UserAwardRecordEntity userAwardRecordEntity = giveOutPrizesAggregate.getUserAwardRecordEntity();
        //更新发奖记录
        UserAwardRecord userAwardRecordReq = UserAwardRecord.builder()
                .userId(userId)
                .orderId(userAwardRecordEntity.getOrderId())
                .awardId(userAwardRecordEntity.getAwardId())
                .awardState(userAwardRecordEntity.getAwardState().getCode())
                .build();

        UserCreditAccount userCreditAccountReq = UserCreditAccount.builder()
                .userId(userId)
                .totalAmount(userCreditRandomAwardEntity.getCreditAmount())
                .availableAmount(userCreditRandomAwardEntity.getCreditAmount())
                .accountStatus(AccountStatusVO.open.getCode())
                .build();

        RLock lock = redisService.getLock(Constants.RedisKey.ACTIVITY_ACCOUNT_LOCK + userId);
        try{
            lock.lock(3, TimeUnit.SECONDS);
            routerStrategy.doRouter(userId);
            //编程式事务
            transactionTemplate.execute(status -> {
                try{
                    //写入任务,存在就更新，不存在就插入
                    UserCreditAccount userCreditAccountRes = userCreditAccountDao.queryUserCreditAccountByUserId(userCreditAccountReq);
                    if (null == userCreditAccountRes) {
                        userCreditAccountDao.insert(userCreditAccountReq);
                    } else {
                        userCreditAccountDao.updateAddAmount(userCreditAccountReq);
                    }
                    //更新中奖单中的发奖状态
                    int count = userAwardRecordDao.updateUserAwardRecord(userAwardRecordReq);
                    if( 1 != count ) {
                        //回滚操作
                        log.warn("更新中奖单记录，重复更新拦截 userId:{} giveOutPrizesAggregate:{}", userId, JSON.toJSONString(giveOutPrizesAggregate));
                        status.setRollbackOnly();
                    }
                    return 1;
                }catch (DuplicateKeyException e){
                    status.setRollbackOnly();
                    log.error("更新中奖记录，唯一索引冲突 userId: {} ", userId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(),ResponseCode.INDEX_DUP.getInfo());
                }
            });
        }finally {
            routerStrategy.clear();
            lock.unlock();
        }

    }


}
