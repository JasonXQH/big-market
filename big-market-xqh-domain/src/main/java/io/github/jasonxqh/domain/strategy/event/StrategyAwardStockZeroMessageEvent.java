package io.github.jasonxqh.domain.strategy.event;

import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import io.github.jasonxqh.types.event.BaseEvent;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
public class StrategyAwardStockZeroMessageEvent extends BaseEvent<StrategyAwardStockKeyVO> {
    @Value("${spring.rabbitmq.topic.strategy_award_stock_zero}")
    private String topic;

    @Override
    public EventMessage<StrategyAwardStockKeyVO> buildEventMessage(StrategyAwardStockKeyVO awardStockKeyVO) {
        return EventMessage.<StrategyAwardStockKeyVO>builder()
                .id(RandomStringUtils.randomAlphanumeric(11))
                .timestamp(new Date())
                .data(awardStockKeyVO)
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }

}
