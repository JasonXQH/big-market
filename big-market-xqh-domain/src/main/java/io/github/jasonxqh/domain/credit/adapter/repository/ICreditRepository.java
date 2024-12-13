package io.github.jasonxqh.domain.credit.adapter.repository;

import io.github.jasonxqh.domain.credit.model.aggregate.TradeAggregate;

public interface ICreditRepository {

    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);

}
