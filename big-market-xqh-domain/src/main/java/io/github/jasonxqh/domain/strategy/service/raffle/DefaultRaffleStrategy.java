package io.github.jasonxqh.domain.strategy.service.raffle;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleFactorEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RuleActionEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RuleMatterEntity;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.model.vo.RuleTreeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import io.github.jasonxqh.domain.strategy.service.AbstractRaffleStrategy;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyDispatch;
import io.github.jasonxqh.domain.strategy.service.rule.ILogicFilter;
import io.github.jasonxqh.domain.strategy.service.rule.chain.ILogicChain;
import io.github.jasonxqh.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import io.github.jasonxqh.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description :
 **/
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {
    @Resource
    private DefaultLogicFactory logicFactory;

    public DefaultRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch, DefaultChainFactory chainFactory, DefaultTreeFactory treeFactory) {
        super(strategyRepository, strategyDispatch, chainFactory, treeFactory);
    }


    @Override
    protected DefaultChainFactory.StrategyAwardVO raffleChain(String userId, Long strategyId) {
        ILogicChain logicChain = chainFactory.openLogicChain(strategyId);
        return logicChain.logic(userId, strategyId);
    }

    @Override
    protected DefaultTreeFactory.StrategyAwardVO raffleTree(String userId, Long strategyId, Integer awardId) {
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = strategyRepository.queryStrategyAwardRuleModelVO(strategyId, awardId);
        if(!Arrays.asList(strategyAwardRuleModelVO.getCenterRuleModels()).contains("tree_lock")) {
            //直接返回，因为抽到的不是限定奖品
            return DefaultTreeFactory.StrategyAwardVO.builder()
                    .awardId(awardId)
                    .build();
        }
        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVO(strategyAwardRuleModelVO.getRuleModels());
        IDecisionTreeEngine decisionTreeEngine = treeFactory.openLogicTree(ruleTreeVO);
        return decisionTreeEngine.process(userId, strategyId, awardId);
    }


//    @Override
//    protected RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String[] logics) {
//        if (logics == null || 0 == logics.length)
//            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
//                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
//                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
//                .build();
//
//        Map<String,ILogicFilter<RuleActionEntity.RaffleCenterEntity>> logicFilterGroup = logicFactory.openLogicFilter();
//        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionEntity = null;
//        for( String ruleModel : logics) {
//            ILogicFilter<RuleActionEntity.RaffleCenterEntity> raffleCenterEntityILogicFilter = logicFilterGroup.get(DefaultLogicFactory.LogicModel.RULE_LOCK.getCode());
//            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
//            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
//            ruleMatterEntity.setRuleModel(ruleModel);
//            ruleMatterEntity.setAwardId(raffleFactorEntity.getAwardId());
//            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
//            ruleActionEntity = raffleCenterEntityILogicFilter.filter(ruleMatterEntity);
//            // 非放行结果则顺序过滤
//            log.info("抽奖中规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());
//            if(RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleMatterEntity.getStrategyId())) {
//                return ruleActionEntity;
//            }
//        }
//        return  ruleActionEntity;
//    }
//
//
//    @Override
//    protected RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String[] logics) {
//        if (logics == null || 0 == logics.length) return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
//                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
//                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
//                .build();
//
//        Map<String, ILogicFilter<RuleActionEntity.RaffleBeforeEntity>> logicFilterGroup = logicFactory.openLogicFilter();
//
//        //黑名单规则优先过滤
//
//        String ruleBlackList = Arrays.stream(logics)
//                .filter(str -> str.contains(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
//                .findFirst()
//                .orElse(null);
//
//        if(StringUtils.isNoneBlank(ruleBlackList)) {
//            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(ruleBlackList);
//            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
//            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
//            ruleMatterEntity.setRuleModel(ruleBlackList);
//            ruleMatterEntity.setAwardId(ruleMatterEntity.getAwardId());
//            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
//            RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = logicFilter.filter(ruleMatterEntity);
//            if(!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())){
//                return  ruleActionEntity;
//            }
//        }
//        //顺序过滤剩余规则
//        List<String> ruleList = Arrays.stream(logics)
//                .filter(s -> !s.equals(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
//                .collect(Collectors.toList());
//
//        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = null;
//        for( String ruleModel : ruleList) {
//            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(ruleModel);
//            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
//            ruleMatterEntity.setRuleModel(ruleModel);
//            ruleMatterEntity.setAwardId(ruleMatterEntity.getAwardId());
//            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
//            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
//            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
//            if(!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())){
//                return  ruleActionEntity;
//            }
//        }
//
//        return ruleActionEntity;
//    }
}
