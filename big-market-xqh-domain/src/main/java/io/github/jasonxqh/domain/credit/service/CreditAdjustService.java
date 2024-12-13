package io.github.jasonxqh.domain.credit.service;

import io.github.jasonxqh.domain.credit.adapter.repository.ICreditRepository;
import io.github.jasonxqh.domain.credit.event.CreditAdjustSuccessMessageEvent;
import io.github.jasonxqh.domain.credit.model.aggregate.TradeAggregate;
import io.github.jasonxqh.domain.credit.model.entity.TaskEntity;
import io.github.jasonxqh.domain.credit.model.entity.TradeEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditAccountEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditOrderEntity;
import io.github.jasonxqh.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Slf4j
@Service
public class CreditAdjustService implements ICreditAdjustService {
    @Resource
    private ICreditRepository creditRepository;


    @Resource
    private CreditAdjustSuccessMessageEvent creditAdjustSuccessMessageEvent;
    @Override
    public  String createOrder(TradeEntity tradeEntity) {
        UserCreditAccountEntity userCreditAccountEntity = TradeAggregate.buildUserCreditAccountEntity(
                tradeEntity.getUserId(),
                tradeEntity.getAmount()
        );

        UserCreditOrderEntity userCreditOrderEntity = TradeAggregate.buildUserCreditOrderEntity(
                tradeEntity.getUserId(),
                tradeEntity.getTradeName(),
                tradeEntity.getTradeType(),
                tradeEntity.getAmount(),
                tradeEntity.getOutBusinessNo()
        );

        CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage creditAdjustSuccessMessage = CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage.builder()
                .userId(tradeEntity.getUserId())
                .orderId(userCreditOrderEntity.getOrderId())
                .amount(tradeEntity.getAmount())
                .outBusinessNo(tradeEntity.getOutBusinessNo())
                .build();

        BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> creditAdjustSuccessMessageEventMessage = creditAdjustSuccessMessageEvent.buildEventMessage(creditAdjustSuccessMessage);


        TaskEntity taskEntity = TradeAggregate.buildTaskEntity(
                tradeEntity.getUserId(),
                creditAdjustSuccessMessageEventMessage.getId(),
                creditAdjustSuccessMessageEvent.topic(),
                creditAdjustSuccessMessageEventMessage
        );

        TradeAggregate tradeAggregate = TradeAggregate.builder()
                .userCreditOrder(userCreditOrderEntity)
                .userCreditAccount(userCreditAccountEntity)
                .task(taskEntity)
                .userId(userCreditAccountEntity.getUserId())
                .build();

        creditRepository.saveUserCreditTradeOrder(tradeAggregate);
        log.info("账户积分额度完成 userId:{} orderId:{}", tradeEntity.getUserId(), userCreditOrderEntity.getOrderId());

        return userCreditOrderEntity.getOrderId();
    }

}
