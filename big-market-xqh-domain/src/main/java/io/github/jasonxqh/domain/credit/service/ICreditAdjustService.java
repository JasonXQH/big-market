package io.github.jasonxqh.domain.credit.service;

import io.github.jasonxqh.domain.credit.model.entity.TradeEntity;

public interface ICreditAdjustService {
    String createOrder(TradeEntity tradeEntity);
}
