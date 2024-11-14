package io.github.jasonxqh.domain.strategy.service;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleFactorEntity;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyDispatch;
import io.github.jasonxqh.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description : 抽奖策略抽象类
 **/
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {
    protected IStrategyRepository strategyRepository;

    protected IStrategyDispatch strategyDispatch;

    protected DefaultChainFactory chainFactory;

    protected DefaultTreeFactory treeFactory;

    public AbstractRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch, DefaultChainFactory chainFactory, DefaultTreeFactory treeFactory) {
        this.strategyRepository = strategyRepository;
        this.strategyDispatch = strategyDispatch;
        this.chainFactory = chainFactory;
        this.treeFactory = treeFactory;
    }


    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity){
        //1. 参数校验
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if(null == strategyId|| StringUtils.isBlank(userId)){
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(),ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        //2. 责任链抽奖
//        ILogicChain logicChain = chainFactory.openLogicChain(strategyId);
//        Integer randomAwardId = logicChain.logic(userId, strategyId);
        DefaultChainFactory.StrategyAwardVO raffleChainResult = raffleChain(userId, strategyId);
        if(raffleChainResult.getLogicModel().equals(DefaultChainFactory.LogicModel.RULE_DEFAULT.getCode())){
            return RaffleAwardEntity.builder()
                .awardId(raffleChainResult.getAwardId())
                .build();
        }

        DefaultTreeFactory.StrategyAwardVO raffleTreeResult  = raffleTree(userId, strategyId, raffleChainResult.getAwardId());

        if(raffleTreeResult == null){
            throw new RuntimeException("抽奖错误");
        }
        return RaffleAwardEntity.builder()
                .awardId(raffleTreeResult.getAwardId())
                .strategyId(strategyId)
                .build();

//        StrategyAwardRuleModelVO strategyAwardRuleModelVO = strategyRepository.queryStrategyAwardRuleModelVO(strategyId, randomAwardId);
//
//        //抽奖中，次数过滤 ,传入awardId
//        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> centerRuleActionEntity = this.doCheckRaffleCenterLogic(RaffleFactorEntity.builder()
//                .userId(userId)
//                .strategyId(strategyId)
//                .awardId(randomAwardId)
//                .build(), strategyAwardRuleModelVO.getCenterRuleModels());
//        if(RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(centerRuleActionEntity.getCode())){
//            //返回保底
//            log.info("【临时日志】中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。");
//            return RaffleAwardEntity.builder()
//                    .awardDesc("中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。")
//                    .build();
//        }

//        return RaffleAwardEntity.builder()
//                .awardId(randomAwardId)
//                .build();



    }

//    protected abstract RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity build,String[] strings);
//
//    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity build, String[] strings);
    protected abstract DefaultChainFactory.StrategyAwardVO raffleChain (String userId, Long strategyId);

    protected abstract DefaultTreeFactory.StrategyAwardVO raffleTree(String userId,Long strategyId,Integer awardId);
}
