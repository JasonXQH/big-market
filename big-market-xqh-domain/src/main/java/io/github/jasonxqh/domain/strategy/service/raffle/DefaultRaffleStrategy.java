package io.github.jasonxqh.domain.strategy.service.raffle;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleFactorEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RuleActionEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RuleMatterEntity;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyDispatch;
import io.github.jasonxqh.domain.strategy.service.rule.ILogicFilter;
import io.github.jasonxqh.domain.strategy.service.rule.factory.DefaultLogicFactory;
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

@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {
    @Resource
    private DefaultLogicFactory logicFactory;

    public DefaultRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch) {
        super(strategyRepository, strategyDispatch);
    }



    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforLogic(RaffleFactorEntity raffleFactorEntity, String[] logics) {
        Map<String, ILogicFilter<RuleActionEntity.RaffleBeforeEntity>> logicFilterGroup = logicFactory.openLogicFilter();

        //黑名单规则优先过滤

        String ruleBlackList = Arrays.stream(logics)
                .filter(str -> str.contains(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
                .findFirst()
                .orElse(null);

        if(StringUtils.isNoneBlank(ruleBlackList)) {
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(ruleBlackList);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setRuleModel(ruleBlackList);
            ruleMatterEntity.setAwardId(ruleMatterEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            if(!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())){
                return  ruleActionEntity;
            }
        }
        //顺序过滤剩余规则
        List<String> ruleList = Arrays.stream(logics)
                .filter(s -> !s.equals(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
                .collect(Collectors.toList());

        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = null;
        for( String ruleModel : ruleList) {
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setRuleModel(ruleModel);
            ruleMatterEntity.setAwardId(ruleMatterEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            if(!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())){
                return  ruleActionEntity;
            }
        }

        return ruleActionEntity;
    }
}
