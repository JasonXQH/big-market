package io.github.jasonxqh.domain.strategy.service.rule;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.vo.RuleWeightVO;
import io.github.jasonxqh.domain.strategy.service.IRaffleRule;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class RaffleRule implements IRaffleRule {

    @Resource
    IStrategyRepository strategyRepository;

    /**
     * 根据规则树ID集合查询奖品中加锁数量的配置「部分奖品需要抽奖N次解锁」
     *
     * @param treeIds 规则树ID值
     * @return key 规则树，value rule_lock 加锁值
     */
    @Override
    public Map<String, Integer> queryAwardRuleLockCount(String[] treeIds) {
        return strategyRepository.queryAwardRuleLockCount(treeIds);
    }

    @Override
    public List<RuleWeightVO> queryAwardRuleWeight(Long strategyId) {
        return strategyRepository.queryAwardRuleWeight(strategyId);
    }


    @Override
    public List<RuleWeightVO> queryAwardRuleWeightByActivityId(Long activityId) {
        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        return queryAwardRuleWeight(strategyId);
    }
}
