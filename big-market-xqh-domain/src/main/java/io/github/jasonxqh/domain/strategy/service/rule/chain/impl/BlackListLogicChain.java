package io.github.jasonxqh.domain.strategy.service.rule.chain.impl;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.service.rule.chain.AbstractLogicChain;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/12, 星期二
 * @Description :
 **/
@Slf4j
@Component("rule_blacklist")
public class BlackListLogicChain extends AbstractLogicChain {
    @Resource

    private IStrategyRepository strategyRepository;


    @Override
    public Integer logic(String userId, Long strategyId) {
        log.info("抽奖责任链-黑名单开始 userId:{} strategyId:{} ruleModel:{} ", userId, strategyId,ruleModel());
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, ruleModel());

        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.valueOf(splitRuleValue[0]);
        String[] userNames = splitRuleValue[1].split(Constants.SPLIT);

        // 100:user001,user002,user003 判断ruleMatterEntity.getUserId()是否在黑名单中
        boolean isBlackListed = Arrays.asList(userNames).contains(userId);
        if(isBlackListed){
            log.info("抽奖责任链-黑名单接管 userId:{} strategyId:{} ruleModel:{} ", userId, strategyId,ruleModel());
            return awardId;
        }
        log.info("抽奖责任链-黑名单放行 userId:{} strategyId:{} ruleModel:{} ", userId, strategyId,ruleModel());
        return next().logic(userId, strategyId);
    }

    @Override
    protected String ruleModel() {
        return "rule_blacklist";
    }
}
