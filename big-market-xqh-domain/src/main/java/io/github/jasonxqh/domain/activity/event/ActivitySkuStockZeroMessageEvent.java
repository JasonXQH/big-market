package io.github.jasonxqh.domain.activity.event;

import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import io.github.jasonxqh.types.event.BaseEvent;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
@Getter
public class ActivitySkuStockZeroMessageEvent extends BaseEvent<ActivitySkuStockKeyVO> {
    @Value("${spring.rabbitmq.topic.activity_sku_stock_zero}")
    private String topic;


    @Override
    public EventMessage<ActivitySkuStockKeyVO> buildEventMessage(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        return EventMessage.<ActivitySkuStockKeyVO>builder()
                .id(RandomStringUtils.randomAlphanumeric(11))
                .timestamp(new Date())
                .data(activitySkuStockKeyVO)
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }

}
