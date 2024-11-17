package io.github.jasonxqh.domain.strategy.service.raffle;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.model.vo.RuleTreeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import io.github.jasonxqh.domain.strategy.service.AbstractRaffleStrategy;
import io.github.jasonxqh.domain.strategy.service.IRaffleAward;
import io.github.jasonxqh.domain.strategy.service.IRaffleStock;
import io.github.jasonxqh.domain.strategy.service.IRaffleStrategy;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyDispatch;
import io.github.jasonxqh.domain.strategy.service.rule.chain.ILogicChain;
import io.github.jasonxqh.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description :
 **/
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy implements IRaffleAward {


    public DefaultRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch, DefaultChainFactory chainFactory, DefaultTreeFactory treeFactory) {
        super(strategyRepository, strategyDispatch, chainFactory, treeFactory);
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
        if(!strategyAwardRuleModelVO.getRuleModels().equals("tree_lock")){
            log.info("被拦截，返回");
            return DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).build();
        }
        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        if(null == ruleTreeVO) {
            throw new RuntimeException("存在抽奖策略配置的规则模型，但是未在库表中配置对应的规则树信息");
        }
        IDecisionTreeEngine decisionTreeEngine = treeFactory.openLogicTree(ruleTreeVO);
        DefaultTreeFactory.StrategyAwardVO strategyAwardVO = decisionTreeEngine.process(userId, strategyId, awardId);
        return strategyAwardVO;
    }


    @Override
    public StrategyAwardStockKeyVO takeQueueValue() throws InterruptedException {
        return strategyRepository.takeQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        strategyRepository.updateStrategyAwardStock( strategyId,  awardId);
    }

    @Override
    public  List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId){
        List<StrategyAwardEntity> strategyAwardEntities = strategyRepository.queryStrategyAwardList(strategyId);
        return strategyAwardEntities;
    }


}
