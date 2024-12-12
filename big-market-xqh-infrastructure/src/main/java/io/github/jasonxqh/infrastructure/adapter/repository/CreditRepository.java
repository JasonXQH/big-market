package io.github.jasonxqh.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.domain.award.model.vo.AccountStatusVO;
import io.github.jasonxqh.domain.credit.adapter.repository.ICreditRepository;
import io.github.jasonxqh.domain.credit.model.aggregate.TradeAggregate;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditAccountEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditOrderEntity;
import io.github.jasonxqh.infrastructure.dao.IUserCreditAccountDao;
import io.github.jasonxqh.infrastructure.dao.IUserCreditOrderDao;
import io.github.jasonxqh.infrastructure.dao.po.award.UserCreditAccount;
import io.github.jasonxqh.infrastructure.dao.po.credit.UserCreditOrder;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

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
    private TransactionTemplate transactionTemplate;

    @Override
    public void saveUserCreditTradeOrder(TradeAggregate tradeAggregate) {
        UserCreditOrderEntity userCreditOrderEntity = tradeAggregate.getUserCreditOrder();
        UserCreditAccountEntity userCreditAccountEntity = tradeAggregate.getUserCreditAccount();
        String userId = tradeAggregate.getUserId();

         UserCreditAccount userCreditAccountReq = UserCreditAccount.builder()
                  .userId(userCreditAccountEntity.getUserId())
                  .totalAmount(userCreditAccountEntity.getTotalAmount())
                  .availableAmount(userCreditAccountEntity.getTotalAmount())
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

        try{
            routerStrategy.doRouter(userId);
            //编程式事务
            transactionTemplate.execute(status -> {
                try{
                    //写入任务,存在就更新，不存在就插入
                    int updateAccountCount = userCreditAccountDao.updateAddAmount(userCreditAccountReq);
                    if (0 == updateAccountCount) {
                        userCreditAccountDao.insert(userCreditAccountReq);
                    }
                    //写入user_credit_order
                    userCreditOrderDao.saveUserCreditOrder(userCreditOrderReq);
                    return 1;
                }catch (DuplicateKeyException e){
                    status.setRollbackOnly();
                    log.error("写入积分账单，唯一索引冲突 userId: {} ", userId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(),ResponseCode.INDEX_DUP.getInfo());
                }
            });
        }finally {
            routerStrategy.clear();
        }
    }
}
