package io.github.jasonxqh.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.domain.award.adapter.repository.IAwardRepository;
import io.github.jasonxqh.domain.award.model.aggregate.UserAwardRecordAggregate;
import io.github.jasonxqh.domain.award.model.entity.TaskEntity;
import io.github.jasonxqh.domain.award.model.entity.UserAwardRecordEntity;
import io.github.jasonxqh.infrastructure.dao.ITaskDao;
import io.github.jasonxqh.infrastructure.dao.IUserAwardRecordDao;
import io.github.jasonxqh.infrastructure.dao.IUserRaffleOrderDao;
import io.github.jasonxqh.infrastructure.dao.po.Task;
import io.github.jasonxqh.infrastructure.dao.po.award.UserAwardRecord;
import io.github.jasonxqh.infrastructure.dao.po.strategy.UserRaffleOrder;
import io.github.jasonxqh.infrastructure.event.EventPublisher;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;


@Slf4j
@Repository
public class AwardRepository implements IAwardRepository {
    @Resource
    private IUserAwardRecordDao userAwardRecordDao;
    @Resource
    private ITaskDao taskDao;

    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;
    @Resource
    private IDBRouterStrategy routerStrategy;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private EventPublisher eventPublisher;

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
        //TODO 用线程池的方式实现：发成功了就成功，失败的话，就重新发送
        try {
            // 发送消息【在事务外执行，如果失败还有任务补偿】
            eventPublisher.publish(task.getTopic(), task.getMessage());
            // 更新数据库记录，task 任务表
            taskDao.updateTaskSendMessageCompleted(task);
        } catch (Exception e) {
            log.error("写入中奖记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
            taskDao.updateTaskSendMessageFail( task);
        }

    }


}
