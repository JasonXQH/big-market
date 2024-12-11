package io.github.jasonxqh.domain.award.service.delivery.impl;

import io.github.jasonxqh.domain.award.model.entity.DeliverAwardEntity;
import io.github.jasonxqh.domain.award.service.delivery.IDeliverAward;
import org.springframework.stereotype.Component;


@Component("openai_model")
public class DeliverOpenAIModel implements IDeliverAward {
    @Override
    public void giveOutPrizes(DeliverAwardEntity deliverAwardEntity) {

    }
}
