package io.github.jasonxqh.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.domain.award.model.vo.AccountStatusVO;
import io.github.jasonxqh.domain.credit.adapter.repository.ICreditRepository;
import io.github.jasonxqh.domain.credit.event.CreditAdjustSuccessMessageEvent;
import io.github.jasonxqh.domain.credit.model.aggregate.TradeAggregate;
import io.github.jasonxqh.domain.credit.model.entity.TaskEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditAccountEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditOrderEntity;
import io.github.jasonxqh.infrastructure.dao.IRaffleActivitySkuDao;
import io.github.jasonxqh.infrastructure.dao.ITaskDao;
import io.github.jasonxqh.infrastructure.dao.IUserCreditAccountDao;
import io.github.jasonxqh.infrastructure.dao.IUserCreditOrderDao;
import io.github.jasonxqh.infrastructure.dao.po.Task;
import io.github.jasonxqh.infrastructure.dao.po.award.UserCreditAccount;
import io.github.jasonxqh.infrastructure.dao.po.credit.UserCreditOrder;
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
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class CreditRepository implements ICreditRepository {
    @Resource
    private IUserCreditAccountDao userCreditAccountDao;
    @Resource
    private IUserCreditOrderDao userCreditOrderDao;
    @Resource
    private IDBRouterStrategy routerStrategy;
    @Resource
    private EventPublisher eventPublisher;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private IRaffleActivitySkuDao activitySkuDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private CreditAdjustSuccessMessageEvent creditAdjustSuccessMessageEvent;
    @Resource
    private ITaskDao taskDao;
    @Override
    public void saveUserCreditTradeOrder(TradeAggregate tradeAggregate) {
        UserCreditOrderEntity userCreditOrderEntity = tradeAggregate.getUserCreditOrder();
        UserCreditAccountEntity userCreditAccountEntity = tradeAggregate.getUserCreditAccount();
        String userId = tradeAggregate.getUserId();
        TaskEntity taskEntity = tradeAggregate.getTask();
         UserCreditAccount userCreditAccountReq = UserCreditAccount.builder()
                  .userId(userCreditAccountEntity.getUserId())
                  .totalAmount(userCreditAccountEntity.getAdjustAmount())
                  .availableAmount(userCreditAccountEntity.getAdjustAmount())
                  .accountStatus(AccountStatusVO.open.getCode())
                  .build();

         UserCreditOrder userCreditOrderReq = UserCreditOrder.builder()
                  .userId(userCreditOrderEntity.getUserId())
                  .orderId(userCreditOrderEntity.getOrderId())
                  .tradeName(userCreditOrderEntity.getTradeName().getName())
                  .tradeType(userCreditOrderEntity.getTradeType().getCode())
                  .tradeAmount(userCreditOrderEntity.getTradeAmount())
                  .outBusinessNo(userCreditOrderEntity.getOutBusinessNo())
                  .build();
        Task task = Task.builder()
                .userId(userId)
                .topic(taskEntity.getTopic())
                .messageId(taskEntity.getMessageId())
                .message(JSON.toJSONString(taskEntity.getMessage()))
                .state(taskEntity.getState().getCode())
                .build();
        RLock lock = redisService.getLock(Constants.RedisKey.USER_CREDIT_ACCOUNT_LOCK + userId + Constants.UNDERLINE + userCreditOrderEntity.getOutBusinessNo());
        try{
            lock.lock(3, TimeUnit.SECONDS);
            routerStrategy.doRouter(userId);
            //编程式事务
            transactionTemplate.execute(status -> {
                try{
                    //写入任务,存在就更新，不存在就插入
                    UserCreditAccount userCreditAccount = userCreditAccountDao.queryUserCreditAccountByUserId(userCreditAccountReq);
                    if (null == userCreditAccount) {
                        userCreditAccountDao.insert(userCreditAccountReq);
                    } else {
                        BigDecimal availableAmount = userCreditAccountReq.getAvailableAmount();
                        if (availableAmount.compareTo(BigDecimal.ZERO) >= 0) {
                            userCreditAccountDao.updateAddAmount(userCreditAccountReq);
                        } else {
                            int subtractionCount = userCreditAccountDao.updateSubtractionAmount(userCreditAccountReq);
                            if (1 != subtractionCount) {
                                status.setRollbackOnly();
                                throw new AppException(ResponseCode.USER_CREDIT_ACCOUNT_NO_AVAILABLE_AMOUNT.getCode(), ResponseCode.USER_CREDIT_ACCOUNT_NO_AVAILABLE_AMOUNT.getInfo());
                            }
                        }
                    }
                    //写入任务
                    taskDao.saveTask(task);
                    //写入user_credit_order
                    userCreditOrderDao.saveUserCreditOrder(userCreditOrderReq);
                }catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("调整账户积分额度异常，唯一索引冲突 userId:{} orderId:{}", userId, userCreditOrderEntity.getOrderId(), e);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("调整账户积分额度失败 userId:{} orderId:{}", userId, userCreditOrderEntity.getOrderId(), e);
                }
                return 1;
            });
        }finally {
            routerStrategy.clear();
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
        //用线程池的方式实现：发成功了就成功，失败的话，就重新发送
        try {
            // 发送消息【在事务外执行，如果失败还有任务补偿】
            eventPublisher.publish(task.getTopic(), task.getMessage());
            // 更新数据库记录，task 任务表
            taskDao.updateTaskSendMessageCompleted(task);
            log.info("写入用户消费积分记录，发送MQ消息完成 userId: {} orderId:{} topic: {}", userId, userCreditOrderReq.getOrderId(), task.getTopic());
        } catch (Exception e) {
            log.error("写入用户消费积分记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
            taskDao.updateTaskSendMessageFail(task);
        }
    }

    @Override
    public UserCreditAccountEntity queryUserCreditAccountByUserId(String userId) {
        UserCreditAccount userCreditAccountReq = new UserCreditAccount();
        userCreditAccountReq.setUserId(userId);
        try {
            routerStrategy.doRouter(userId);
            UserCreditAccount userCreditAccountRes = userCreditAccountDao.queryUserCreditAccountByUserId(userCreditAccountReq);
            BigDecimal availableAmount = BigDecimal.ZERO;
            if (null != userCreditAccountRes) {
                availableAmount = userCreditAccountRes.getAvailableAmount();
            }
            return UserCreditAccountEntity.builder()
                    .userId(userId)
                    .adjustAmount(availableAmount)
                    .build();
        }finally {
            routerStrategy.clear();
        }
    }

    @Override
    public BigDecimal queryUserCreditAvailableAmountByUserId(String userId) {
        try {
            routerStrategy.doRouter(userId);
            UserCreditAccount userCreditAccountReq = new UserCreditAccount();
            userCreditAccountReq.setUserId(userId);
            UserCreditAccount userCreditAccount = userCreditAccountDao.queryUserCreditAccountByUserId(userCreditAccountReq);
            if (null == userCreditAccount) return BigDecimal.ZERO;
            return userCreditAccount.getAvailableAmount();
        } finally {
            routerStrategy.clear();
        }
    }

}
