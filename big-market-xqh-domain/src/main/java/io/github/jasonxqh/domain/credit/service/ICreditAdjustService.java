package io.github.jasonxqh.domain.credit.service;

import io.github.jasonxqh.domain.credit.model.entity.TradeEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditAccountEntity;

public interface ICreditAdjustService {
    String createOrder(TradeEntity tradeEntity);

    UserCreditAccountEntity queryUserCreditAccount(String userId);
}
