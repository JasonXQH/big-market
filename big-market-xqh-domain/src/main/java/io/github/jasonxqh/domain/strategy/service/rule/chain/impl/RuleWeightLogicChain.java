package io.github.jasonxqh.domain.strategy.service.rule.chain.impl;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.RuleActionEntity;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyDispatch;
import io.github.jasonxqh.domain.strategy.service.rule.chain.AbstractLogicChain;
import io.github.jasonxqh.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
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


    @Override
    public DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId) {

        log.info("抽奖责任链-权重 userId:{} strategyId:{} ruleModel:{} ",userId, strategyId,ruleModel());
        String ruleValue = strategyRepository.queryStrategyRuleValue( strategyId,ruleModel());

        //10:102,103 70:106,107 1000:104,105
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

        Integer userScore = strategyRepository.queryActivityAccountTotalUseCount(userId, strategyId);
        // 使用stream找出最大的不超过score的权重值
        Long nextValue = analyticalValueGroup.keySet().stream()
                .filter(weight -> weight <= userScore)
                .max(Comparator.naturalOrder())
                .orElse(null);

        if(null != nextValue) {
            Integer randomAwardId = strategyDispatch.getRandomAwardId(strategyId, analyticalValueGroup.get(nextValue));
            log.info("抽奖责任链-权重接管 userId:{} strategyId:{} ruleModel:{} userScore:{} ,nextValue:{} ",userId, strategyId,ruleModel(),userScore,nextValue);
            //这里，不用库存扣减，因为达到权重抽奖，告诉无库存了。那么会有大量客诉。
            return DefaultChainFactory.StrategyAwardVO.builder()
                    .awardId(randomAwardId)
                    .logicModel(ruleModel())
                    .build();
        }
        log.info("抽奖责任链-权重放行 userId:{} strategyId:{} ruleModel:{} userScore:{} ", userId, strategyId,ruleModel(),userScore);
        return next().logic(userId, strategyId);
    }

    @Override
    protected String ruleModel() {
        return DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode();
    }
}
