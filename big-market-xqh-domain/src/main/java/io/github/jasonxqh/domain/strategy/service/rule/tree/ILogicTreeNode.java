package io.github.jasonxqh.domain.strategy.service.rule.tree;

import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description : 规则树接口
 **/
public interface ILogicTreeNode {

    DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId);

}
