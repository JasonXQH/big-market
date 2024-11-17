package io.github.jasonxqh.domain.strategy.service.rule.tree.impl;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyDispatch;
import io.github.jasonxqh.domain.strategy.service.rule.tree.ILogicTreeNode;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.rule.Rule;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description : 把扣减库存看做一个规则，作为节点
 * 如果放行，那么说明扣减库存成功
 * 如果接管，返回兜底奖品
 **/

@Slf4j
@Component("rule_stock")
public class RuleStockLogicTreeNode implements ILogicTreeNode {

    @Resource
    private IStrategyDispatch strategyDispatch;

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId,String ruleValue) {
        log.info("规则过滤-库存扣减 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
        Boolean status = strategyDispatch.subtractionAwardStock(strategyId, awardId);
        // status 为true，库存扣减成功，否则失败
        //成功，则发送队列消息
        if(status){
            //写入延迟队列，延迟消费更新数据库记录
            log.info("规则过滤-库存扣减成功，向队列发送消息 ");
            strategyRepository.awardStockConsumeSendQueue( StrategyAwardStockKeyVO.builder()
                            .strategyId(strategyId)
                            .awardId(awardId)
                            .build());

            return DefaultTreeFactory.TreeActionEntity.builder()
                    .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.TAKE_OVER)
                    .strategyAwardVO(DefaultTreeFactory.StrategyAwardVO.builder()
                            .awardId(awardId)
                            .awardRuleValue("")
                            .build())
                    .build();
        }
        log.info("规则过滤-库存扣减失败，走兜底");
        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.TAKE_OVER)
                .build();
    }
}
