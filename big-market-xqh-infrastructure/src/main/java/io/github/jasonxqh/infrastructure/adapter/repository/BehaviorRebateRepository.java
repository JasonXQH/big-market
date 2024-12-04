package io.github.jasonxqh.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.domain.rebate.adapter.repository.IBehaviorRebateRepository;
import io.github.jasonxqh.domain.rebate.model.aggregate.BehaviorRebateOrderAggregate;
import io.github.jasonxqh.domain.rebate.model.entity.TaskEntity;
import io.github.jasonxqh.domain.rebate.model.entity.UserBehaviorRebateOrderEntity;
import io.github.jasonxqh.domain.rebate.model.vo.DailyBehaviorRebateVO;
import io.github.jasonxqh.domain.rebate.model.vo.RebateTypeVO;
import io.github.jasonxqh.infrastructure.dao.IDailyBehaviorRebateDao;
import io.github.jasonxqh.infrastructure.dao.ITaskDao;
import io.github.jasonxqh.infrastructure.dao.IUserBehaviorRebateOrderDao;
import io.github.jasonxqh.infrastructure.dao.po.Task;
import io.github.jasonxqh.infrastructure.dao.po.rebate.DailyBehaviorRebate;
import io.github.jasonxqh.infrastructure.dao.po.rebate.UserBehaviorRebateOrder;
import io.github.jasonxqh.infrastructure.event.EventPublisher;
import io.github.jasonxqh.types.common.Constants;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class BehaviorRebateRepository implements IBehaviorRebateRepository {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @Resource
    private IDailyBehaviorRebateDao dailyBehaviorRebateDao;
    @Resource
    private IUserBehaviorRebateOrderDao userBehaviorRebateOrderDao;
    @Resource
    private ITaskDao taskDao;

    @Resource
    private IDBRouterStrategy routerStrategy;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private EventPublisher eventPublisher;

    @Override
    public List<DailyBehaviorRebateVO> queryDailyBehaviorRebateByBehaviorType(String behaviorType) {
        List<DailyBehaviorRebate> dailyBehaviorRebates = dailyBehaviorRebateDao.queryDailyBehaviorRebateByBehaviorType(behaviorType);

        return  dailyBehaviorRebates.stream()
                .map(dailyBehaviorRebate -> DailyBehaviorRebateVO.builder()
                        .rebateConfig(dailyBehaviorRebate.getRebateConfig())
                        .rebateDesc(dailyBehaviorRebate.getRebateDesc())
                        .rebateType(dailyBehaviorRebate.getRebateType())
                        .behaviorType(dailyBehaviorRebate.getBehaviorType())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public void doSaveRebateOrderAggregate(String userId, List<BehaviorRebateOrderAggregate> behaviorRebateOrderAggregates) {
         try{
             routerStrategy.doRouter(userId);
             transactionTemplate.execute(status -> {
                 try{
                     //遍历aggregate存入：
                     for(BehaviorRebateOrderAggregate behaviorRebateOrderAggregate : behaviorRebateOrderAggregates){
                         UserBehaviorRebateOrder userBehaviorRebateOrder = getUserBehaviorRebateOrder(userId, behaviorRebateOrderAggregate);
                         //写入记录
                         userBehaviorRebateOrderDao.saveBehaviorRebateOrder(userBehaviorRebateOrder);
                         TaskEntity taskEntity = behaviorRebateOrderAggregate.getTaskEntity();
                         Task task = Task.builder()
                                 .userId(taskEntity.getUserId())
                                 .topic(taskEntity.getTopic())
                                 .messageId(taskEntity.getMessageId())
                                 .message(JSON.toJSONString( taskEntity.getMessage()))
                                 .state(taskEntity.getState().getCode())
                                 .build();
                         //写入任务
                         taskDao.saveTask(task);
                     }
                    return 1;
                 }catch (DuplicateKeyException e){
                     status.setRollbackOnly();
                     log.error("写入Task和用户行为返利记录时唯一bizId冲突,userId:{},behaviorType:{},rebateType:{},biz_id:{}",userId,e);
                     throw new AppException(ResponseCode.INDEX_DUP.getCode(),ResponseCode.INDEX_DUP.getInfo());
                 }
             });
         }finally{
             routerStrategy.clear();
         }

        // 同步发送MQ消息
        for (BehaviorRebateOrderAggregate behaviorRebateAggregate : behaviorRebateOrderAggregates ) {
            TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
            Task task = new Task();
            task.setUserId(taskEntity.getUserId());
            task.setMessageId(taskEntity.getMessageId());
            try {
                // 发送消息【在事务外执行，如果失败还有任务补偿】
                eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                // 更新数据库记录，task 任务表
                taskDao.updateTaskSendMessageCompleted(task);
            } catch (Exception e) {
                log.error("写入返利记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
                taskDao.updateTaskSendMessageFail(task);
            }
        }
    }

    @Override
    public Integer queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
        UserBehaviorRebateOrder userBehaviorRebateOrderReq = new UserBehaviorRebateOrder();
        userBehaviorRebateOrderReq.setUserId(userId);
        userBehaviorRebateOrderReq.setOutBusinessNo(outBusinessNo);
        return  userBehaviorRebateOrderDao.queryOrderNumberByBusinessNo(userBehaviorRebateOrderReq);
    }

    private static UserBehaviorRebateOrder getUserBehaviorRebateOrder(String userId, BehaviorRebateOrderAggregate behaviorRebateOrderAggregate) {
        UserBehaviorRebateOrderEntity userBehaviorRebateOrderEntity = behaviorRebateOrderAggregate.getUserBehaviorRebateOrder();
        UserBehaviorRebateOrder userBehaviorRebateOrder = new UserBehaviorRebateOrder();
        userBehaviorRebateOrder.setUserId(userId);
        userBehaviorRebateOrder.setOrderId(userBehaviorRebateOrderEntity.getOrderId());
        userBehaviorRebateOrder.setBehaviorType(userBehaviorRebateOrderEntity.getBehaviorType());
        userBehaviorRebateOrder.setRebateType(userBehaviorRebateOrderEntity.getRebateType());
        userBehaviorRebateOrder.setRebateDesc(userBehaviorRebateOrderEntity.getRebateDesc());
        userBehaviorRebateOrder.setRebateConfig(userBehaviorRebateOrderEntity.getRebateConfig());
        userBehaviorRebateOrder.setBizId(userBehaviorRebateOrderEntity.getBizId());
        userBehaviorRebateOrder.setOutBusinessNo(userBehaviorRebateOrderEntity.getOutBusinessNo());
        return userBehaviorRebateOrder;
    }
}
