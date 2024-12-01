package io.github.jasonxqh.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import io.github.jasonxqh.domain.strategy.service.IRaffleStock;
import io.github.jasonxqh.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class StrategyAwardStockZeroCustomer {
    @Value("${spring.rabbitmq.topic.strategy_award_stock_zero}")
    private String topic;

    @Resource
    private IRaffleStock raffleStock;

    @RabbitListener(queuesToDeclare = @Queue(value = "strategy_award_stock_zero"))
    public void listener(String message) {
        try {
            log.info("监听奖品库存消耗为0消息 topic: {} message: {}", topic, message);
            // 转换对象
            BaseEvent.EventMessage<StrategyAwardStockKeyVO> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<StrategyAwardStockKeyVO>>() {
            }.getType());
            StrategyAwardStockKeyVO strategyAwardStockKeyVO = eventMessage.getData();
            // 更新库存
            raffleStock.clearStrategyAwardStock(strategyAwardStockKeyVO);
            // 清空队列 「此时就不需要延迟更新数据库记录了」
            raffleStock.clearQueueValue(strategyAwardStockKeyVO);
        } catch (Exception e) {
            log.error("监听奖品库存消耗为0消息，消费失败 topic: {} message: {}", topic, message);
            throw e;
        }
    }
}