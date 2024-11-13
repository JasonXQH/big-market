package io.github.jasonxqh.domain.strategy.service.rule.tree.impl;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import io.github.jasonxqh.domain.strategy.service.rule.tree.ILogicTreeNode;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static io.github.jasonxqh.domain.strategy.service.rule.filter.factory.DefaultLogicFactory.LogicModel.RULE_LOCK;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description : 次数锁节点
 **/

@Slf4j
@Component("rule_lock")
public class RuleLockLogicTreeNode implements ILogicTreeNode {
    @Resource
    private IStrategyRepository strategyRepository;

    private Long userRaffleCount = 2L;

    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId) {
        log.info("规则树过滤-次数锁节点 userId:{} strategyId:{} awardId:{}", userId, strategyId,awardId);

//        StrategyAwardRuleModelVO strategyAwardRuleModelVO = strategyRepository.queryStrategyAwardRuleModelVO(strategyId, awardId);
//
//        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, awardId,RULE_LOCK.getCode());

//        if(Long.parseLong(ruleValue) > userRaffleCount) {
//           //xxx
//        }

        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.ALLOW)
                .build();
    }
}
