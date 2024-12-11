package io.github.jasonxqh.domain.award.service.delivery;

import io.github.jasonxqh.domain.award.model.entity.DeliverAwardEntity;

public interface IDeliverAward {
    void giveOutPrizes(DeliverAwardEntity deliverAwardEntity);
}
