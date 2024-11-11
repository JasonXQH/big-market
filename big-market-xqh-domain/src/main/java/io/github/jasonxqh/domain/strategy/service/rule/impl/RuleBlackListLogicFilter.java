package io.github.jasonxqh.domain.strategy.service.rule.impl;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.RuleActionEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RuleMatterEntity;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.service.annotation.LogicStrategy;
import io.github.jasonxqh.domain.strategy.service.rule.ILogicFilter;
import io.github.jasonxqh.domain.strategy.service.rule.factory.DefaultLogicFactory;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description :
 **/
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_BLACKLIST)
public class RuleBlackListLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity>{
    @Resource
    private IStrategyRepository strategyRepository;


    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        log.info("规则过滤-黑名单 userId:{} strategyId:{} ruleModel:{} ",ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());

        String ruleValue = strategyRepository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(),ruleMatterEntity.getRuleModel());

        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.valueOf(splitRuleValue[0]);
        String[] userNames = splitRuleValue[1].split(Constants.SPLIT);

        // 100:user001,user002,user003 判断ruleMatterEntity.getUserId()是否在黑名单中
        boolean isBlackListed = Arrays.asList(userNames).contains(ruleMatterEntity.getUserId());
        if(isBlackListed){
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .ruleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())
                    .data(RuleActionEntity.RaffleBeforeEntity.builder()
                            .strategyId(ruleMatterEntity.getStrategyId())
                            .awardId(awardId)
                            .build())
                    .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                    .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                    .build();

        }

        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .ruleModel(ruleMatterEntity.getRuleModel())
                .data(RuleActionEntity.RaffleBeforeEntity.builder()
                        .strategyId(ruleMatterEntity.getStrategyId())
                        .awardId(awardId)
                        .build())
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();
    }
}
