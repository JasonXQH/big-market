package io.github.jasonxqh.domain.strategy.service.rule.tree.impl;

import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.service.rule.tree.ILogicTreeNode;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description : 兜底奖励节点
 **/

@Slf4j
@Component("rule_luck_award")
public class RuleLuckAwardLogicTreeNode implements ILogicTreeNode {
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId,String ruleValue) {
        String[] ruleParts = ruleValue.split(Constants.COLON);
        if (ruleParts.length == 0) {
            log.error("规则过滤-兜底奖品，兜底奖品未配置告警 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
            throw new RuntimeException("兜底奖品未配置 " + ruleValue);
        }
        // 兜底奖励配置
        Integer luckAwardId = Integer.valueOf(ruleParts[0]);
        String awardRuleValue = ruleParts.length > 1 ? ruleParts[1] : "";
        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.TAKE_OVER)
                .strategyAwardVO(DefaultTreeFactory.StrategyAwardVO.builder()
                        .awardId(luckAwardId)
                        .awardRuleValue(awardRuleValue)
                        .build())
                .build();
    }
}
