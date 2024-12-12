package io.github.jasonxqh.domain.credit.service;

import io.github.jasonxqh.domain.credit.adapter.repository.ICreditRepository;
import io.github.jasonxqh.domain.credit.model.aggregate.TradeAggregate;
import io.github.jasonxqh.domain.credit.model.entity.TradeEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditAccountEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditOrderEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CreditAdjustService implements ICreditAdjustService {
    @Resource
    private ICreditRepository creditRepository;

    @Override
    public void createOrder(TradeEntity tradeEntity) {
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

        TradeAggregate tradeAggregate = TradeAggregate.builder()
                .userCreditOrder(userCreditOrderEntity)
                .userCreditAccount(userCreditAccountEntity)
                .userId(userCreditAccountEntity.getUserId())
                .build();

        creditRepository.saveUserCreditTradeOrder(tradeAggregate);
    }
}
