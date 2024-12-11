package io.github.jasonxqh.domain.award.service.delivery.impl;

import io.github.jasonxqh.domain.award.model.entity.DeliverAwardEntity;
import io.github.jasonxqh.domain.award.service.delivery.IDeliverAward;
import org.springframework.stereotype.Component;

@Component("openai_use_count")
public class DeliverOpenaiUseCountAward implements IDeliverAward {
    @Override
    public void giveOutPrizes(DeliverAwardEntity deliverAwardEntity) {

    }
}
