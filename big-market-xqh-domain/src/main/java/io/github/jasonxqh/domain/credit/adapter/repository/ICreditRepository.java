package io.github.jasonxqh.domain.credit.adapter.repository;

import io.github.jasonxqh.domain.credit.model.aggregate.TradeAggregate;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditAccountEntity;

import java.math.BigDecimal;

public interface ICreditRepository {

    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);

    UserCreditAccountEntity queryUserCreditAccountByUserId(String userId);

    BigDecimal queryUserCreditAvailableAmountByUserId(String userId);
}
