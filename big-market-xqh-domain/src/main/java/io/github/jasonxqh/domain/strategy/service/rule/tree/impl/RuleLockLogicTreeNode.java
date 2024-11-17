package io.github.jasonxqh.domain.strategy.service.rule.tree.impl;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import io.github.jasonxqh.domain.strategy.service.rule.tree.ILogicTreeNode;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description : 次数锁节点
 **/

@Slf4j
@Component("rule_lock")
public class RuleLockLogicTreeNode implements ILogicTreeNode {

    private Long userRaffleCount = 2L;

    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId,String ruleValue) {
        log.info("规则树过滤-次数锁节点 userId:{} strategyId:{} awardId:{}", userId, strategyId,awardId);


        long raffleCount = 0L;
        try {
            raffleCount = Long.parseLong(ruleValue);
        }catch (NumberFormatException e){
            throw new RuntimeException("规则过滤-次数锁异常 ruleValue: "+ruleValue+"配置不正确");
        }
        //接管
        if(raffleCount > userRaffleCount) {
            return DefaultTreeFactory.TreeActionEntity.builder()
                    .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.TAKE_OVER)
                    .build();
        }

        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.ALLOW)
                .build();
    }
}
