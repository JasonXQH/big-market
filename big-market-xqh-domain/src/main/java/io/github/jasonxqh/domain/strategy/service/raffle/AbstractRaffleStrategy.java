package io.github.jasonxqh.domain.strategy.service.raffle;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleFactorEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RuleActionEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyEntity;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import io.github.jasonxqh.domain.strategy.service.IRaffleStrategy;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyDispatch;
import io.github.jasonxqh.domain.strategy.service.rule.factory.DefaultLogicFactory;
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

    public AbstractRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch) {
        this.strategyRepository = strategyRepository;
        this.strategyDispatch = strategyDispatch;
    }


    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity){
        //1. 参数校验
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if(null == strategyId|| StringUtils.isBlank(userId)){
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(),ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        //2. 策略查询
        StrategyEntity strategyEntity = strategyRepository.queryStrategyEntityByStrategyId(strategyId);
        //3. 抽奖前-规则过滤
        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> beforeRuleActionEntity = this.doCheckRaffleBeforeLogic(RaffleFactorEntity.builder()
                .userId(userId)
                .strategyId(strategyId)
                .build(), strategyEntity.ruleModels());

        if(RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(beforeRuleActionEntity.getCode())){
            if(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode().equals(beforeRuleActionEntity.getRuleModel())){
                return RaffleAwardEntity.builder()
                        .awardId(beforeRuleActionEntity.getData().getAwardId())
                        .build();
            } else if (DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode().equals(beforeRuleActionEntity.getRuleModel())) {
                //根据返回的信息进行抽奖

                Integer randomAwardId = strategyDispatch.getRandomAwardId(strategyId, beforeRuleActionEntity.getData().getRuleWeightValueKey());
                return RaffleAwardEntity.builder()
                        .awardId(randomAwardId)
                        .build();
            }
        }

        Integer randomAwardId = strategyDispatch.getRandomAwardId(strategyId);

        StrategyAwardRuleModelVO strategyAwardRuleModelVO = strategyRepository.queryStrategyAwardRuleModelVO(strategyId, randomAwardId);

        //抽奖中，次数过滤 ,传入awardId
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> centerRuleActionEntity = this.doCheckRaffleCenterLogic(RaffleFactorEntity.builder()
                .userId(userId)
                .strategyId(strategyId)
                .awardId(randomAwardId)
                .build(), strategyAwardRuleModelVO.getCenterRuleModels());
        if(RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(centerRuleActionEntity.getCode())){
            //返回保底
            log.info("【临时日志】中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。");
            return RaffleAwardEntity.builder()
                    .awardDesc("中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。")
                    .build();
        }
        return RaffleAwardEntity.builder()
                .awardId(randomAwardId)
                .build();

    }

    protected abstract RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity build,String[] strings);

    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity build, String[] strings);
}
