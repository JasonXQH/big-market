package io.github.jasonxqh.domain.strategy.service.rule.tree.factory.engine.impl;

import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.model.vo.RuleTreeNodeLineVO;
import io.github.jasonxqh.domain.strategy.model.vo.RuleTreeNodeVO;
import io.github.jasonxqh.domain.strategy.model.vo.RuleTreeVO;
import io.github.jasonxqh.domain.strategy.service.rule.tree.ILogicTreeNode;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description : 决策树引擎，不负责创建
 **/
@Slf4j
public class DecisionTreeEngine implements IDecisionTreeEngine {

    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;
    //根节点
    private final RuleTreeVO ruleTreeVO;

    public DecisionTreeEngine(Map<String, ILogicTreeNode> logicTreeNodeGroup, RuleTreeVO ruleTreeVO) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
        this.ruleTreeVO = ruleTreeVO;
    }


    @Override
    public DefaultTreeFactory.StrategyAwardVO process(String userId, Long strategyId, Integer awardId) {
        DefaultTreeFactory.StrategyAwardVO strategyAwardData = null;

        String nextNode = ruleTreeVO.getTreeRootRuleNode();
        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();

        RuleTreeNodeVO ruleTreeNodeVO = treeNodeMap.get(nextNode);
        while(null != nextNode) {
            ILogicTreeNode logicTreeNode = logicTreeNodeGroup.get(ruleTreeNodeVO.getRuleKey());
            DefaultTreeFactory.TreeActionEntity logicEntity = logicTreeNode.logic(userId, strategyId, awardId);

            RuleLogicCheckTypeVO ruleLogicCheckTypeVO = logicEntity.getRuleLogicCheckTypeVO();

            strategyAwardData = logicEntity.getStrategyAwardVO();
            log.info("决策树引擎【{}】treeId:{} node:{} code:{}", ruleTreeVO.getTreeName(), ruleTreeVO.getTreeId(), nextNode, ruleLogicCheckTypeVO.getCode());
            //判断下一个节点，matterValue是TAKE_OVER还是ALLOW
            nextNode = nextNode(ruleLogicCheckTypeVO.getCode(), ruleTreeNodeVO.getTreeNodeLineVOList());
            ruleTreeNodeVO = treeNodeMap.get(nextNode);
        }
        //返回最终结果
        return strategyAwardData;
    }


    private  String nextNode(String matterValue, List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList ) {
        if(null == ruleTreeNodeLineVOList|| ruleTreeNodeLineVOList.isEmpty()) return null;

        for(RuleTreeNodeLineVO ruleTreeNodeLineVO : ruleTreeNodeLineVOList) {
            if(decisionLogic(matterValue, ruleTreeNodeLineVO)) {
                return ruleTreeNodeLineVO.getRuleNodeTo();
            }
        }
        throw new RuntimeException("决策树引擎，nextNode 计算失败，未找到可执行节点！");
    }


    public boolean decisionLogic(String matterValue, RuleTreeNodeLineVO nodeLine) {
        switch (nodeLine.getRuleLimitType()) {
            case EQUAL:
                return matterValue.equals(nodeLine.getRuleLimitValue().getCode());
            // 以下规则暂时不需要实现
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }
}