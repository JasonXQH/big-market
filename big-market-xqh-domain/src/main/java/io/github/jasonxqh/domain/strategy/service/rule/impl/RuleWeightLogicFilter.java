package io.github.jasonxqh.domain.strategy.service.rule.impl;

import com.mysql.cj.x.protobuf.MysqlxDatatypes;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description :
 **/

@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_WIGHT)
public class RuleWeightLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {
    @Resource
    private IStrategyRepository strategyRepository;

    private Long userScore = 4500L;
    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        log.info("规则过滤-权重 userId:{} strategyId:{} ruleModel:{} ",ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());
        String ruleValue = strategyRepository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());
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

        if (null == analyticalValueGroup || analyticalValueGroup.isEmpty()) {
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }

        // 使用stream找出最大的不超过score的权重值
        Optional<Long> optimalWeight = analyticalValueGroup.keySet().stream()
                .filter(weight -> weight <= userScore) // 过滤出小于等于score的权重
                .max(Comparator.naturalOrder()); // 从符合条件的权重中选择最大的

        if (optimalWeight.isPresent()) {
            log.info("Selected maximum weight: {}", optimalWeight.get());
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .data(RuleActionEntity.RaffleBeforeEntity.builder()
                            .strategyId(ruleMatterEntity.getStrategyId())
                            .awardId(ruleMatterEntity.getAwardId())
                            .ruleWeightValueKey(analyticalValueGroup.get(optimalWeight.get()))
                            .build())
                    .ruleModel(DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode())
                    .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                    .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                    .build();
        } else {
            log.info("No suitable weight found");
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }
    }
}
