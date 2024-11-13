package io.github.jasonxqh.domain.strategy.service.raffle;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.vo.RuleTreeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import io.github.jasonxqh.domain.strategy.service.AbstractRaffleStrategy;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyDispatch;
import io.github.jasonxqh.domain.strategy.service.rule.chain.ILogicChain;
import io.github.jasonxqh.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.stereotype.Service;

import java.util.Arrays;


/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description :
 **/
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {


    private final DefaultTreeFactory defaultTreeFactory;

    public DefaultRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch, DefaultChainFactory chainFactory, DefaultTreeFactory treeFactory, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
        super(strategyRepository, strategyDispatch, chainFactory, treeFactory);
        this.defaultTreeFactory = defaultTreeFactory;
    }


    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {
        ILogicChain logicChain = chainFactory.openLogicChain(strategyId);
        return logicChain.logic(userId,strategyId);
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId,Integer awardId) {
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = strategyRepository.queryStrategyAwardRuleModelVO(strategyId, awardId);
        if(strategyAwardRuleModelVO == null) {
            return DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).build();

        }
        log.info("strategyAwardRuleModelVO.getRuleModels :{}", strategyAwardRuleModelVO.getRuleModels());
        if(!Arrays.asList( strategyAwardRuleModelVO.getRuleModels().split(Constants.SPLIT)).contains("rule_lock")){
            return DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).build();
        }
        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        if(null == ruleTreeVO) {
            throw new RuntimeException("存在抽奖策略配置的规则模型，但是未在库表中配置对应的规则树信息");
        }
        IDecisionTreeEngine decisionTreeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);
        DefaultTreeFactory.StrategyAwardVO strategyAwardVO = decisionTreeEngine.process(userId, strategyId, awardId);
        return strategyAwardVO;
    }


}
