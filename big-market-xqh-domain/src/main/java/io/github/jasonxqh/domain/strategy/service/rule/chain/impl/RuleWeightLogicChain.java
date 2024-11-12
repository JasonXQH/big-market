package io.github.jasonxqh.domain.strategy.service.rule.chain.impl;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.RuleActionEntity;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyDispatch;
import io.github.jasonxqh.domain.strategy.service.rule.chain.AbstractLogicChain;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/12, 星期二
 * @Description :
 **/
@Slf4j
@Component("rule_weight")
public class RuleWeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository strategyRepository;
    @Resource
    protected IStrategyDispatch strategyDispatch;

    private Long userScore = 0L;

    @Override
    public Integer logic(String userId, Long strategyId) {

        log.info("抽奖责任链-权重 userId:{} strategyId:{} ruleModel:{} ",userId, strategyId,ruleModel());
        String ruleValue = strategyRepository.queryStrategyRuleValue( strategyId,ruleModel());

        //4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109

        //["4000:102,103,104,105" "5000:102,103,104,105,106,107" "6000:102,103,104,105,106,107,108,109"]
        // Stream to find the maximum weight that does not exceed the score

        // 将权重和原始字符串映射存储在 Map 中
        Map<Long, String> analyticalValueGroup = Arrays.stream(ruleValue.split(Constants.SPACE))
                .map(group -> {
                    String[] groupValue = group.split(Constants.COLON); // 使用冒号分割权重和ID列表
                    if (groupValue.length != 2) {
                        throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + group);
                    }
                    Long weight = Long.parseLong(groupValue[0]); // 解析权重值为整数
                    return new AbstractMap.SimpleEntry<>(weight, group);
                })
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        // 使用stream找出最大的不超过score的权重值
        Long nextValue = analyticalValueGroup.keySet().stream()
                .filter(weight -> weight <= userScore) // 过滤出小于等于score的权重
                .max(Comparator.naturalOrder())
                .orElse(null);


        if(null != nextValue) {
            Integer randomAwardId = strategyDispatch.getRandomAwardId(strategyId, analyticalValueGroup.get(nextValue));
            log.info("抽奖责任链-权重接管 userId:{} strategyId:{} ruleModel:{} ",userId, strategyId,ruleModel());
            return randomAwardId;
        }
        log.info("抽奖责任链-权重放行 userId:{} strategyId:{} ruleModel:{} ", userId, strategyId,ruleModel());
        return next().logic(userId, strategyId);
    }

    @Override
    protected String ruleModel() {
        return "rule_weight";
    }
}
