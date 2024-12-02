package io.github.jasonxqh.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import io.github.jasonxqh.domain.activity.service.IRaffleActivitySkuStockService;
import io.github.jasonxqh.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动sku库存耗尽
 * @create 2024-03-30 12:31
 */
@Slf4j
@Component
public class ActivitySkuStockZeroCustomer {

    @Value("${spring.rabbitmq.topic.activity_sku_stock_zero}")
    private String topic;

    @Resource
    private IRaffleActivitySkuStockService skuStock;

    @RabbitListener(queuesToDeclare = @Queue(value = "activity_sku_stock_zero"))
    public void listener(String message) {
        try {
            log.info("监听活动sku库存消耗为0消息 topic: {} message: {}", topic, message);
            // 转换对象
            BaseEvent.EventMessage<ActivitySkuStockKeyVO> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<ActivitySkuStockKeyVO>>() {
            }.getType());
            ActivitySkuStockKeyVO skuStockKeyVO = eventMessage.getData();
            // 更新库存
            skuStock.clearActivitySkuStock(skuStockKeyVO);
            // 清空队列 「此时就不需要延迟更新数据库记录了」
            skuStock.clearQueueValue(skuStockKeyVO);
        } catch (Exception e) {
            log.error("监听活动sku库存消耗为0消息，消费失败 topic: {} message: {}", topic, message);
            throw e;
        }
    }
}

