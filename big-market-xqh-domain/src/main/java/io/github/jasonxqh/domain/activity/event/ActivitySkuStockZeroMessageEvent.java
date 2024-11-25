package io.github.jasonxqh.domain.activity.event;

import io.github.jasonxqh.types.event.BaseEvent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
@Getter
public class ActivitySkuStockZeroMessageEvent extends BaseEvent<Long> {
    @Value("${spring.rabbitmq.topic.activity_sku_stock_zero}")
    private String topic;


    @Override
    public EventMessage<Long> buildEventMessage(Long sku) {
        return EventMessage.<Long>builder()
                .id(RandomStringUtils.randomAlphanumeric(11))
                .timestamp(new Date())
                .data(sku)
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }

}
