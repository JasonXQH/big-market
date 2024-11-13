package io.github.jasonxqh.domain.strategy.service.rule.tree.factory.engine;

import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description : 规则树组合接口，执行引擎接口
 **/
public interface IDecisionTreeEngine {

    DefaultTreeFactory.StrategyAwardData process(String userId, Long strategyId, Integer awardId);
}
