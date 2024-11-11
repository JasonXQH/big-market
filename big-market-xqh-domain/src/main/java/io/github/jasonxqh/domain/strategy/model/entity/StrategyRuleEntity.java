package io.github.jasonxqh.domain.strategy.model.entity;

import io.github.jasonxqh.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/8, 星期五
 * @Description :
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRuleEntity {
    /*抽奖策略ID*/
    private Long strategyId;
    /*抽奖奖品ID【规则类型为策略，则不需要奖品ID】*/
    private Integer awardId;
    /*抽象规则类型；1-策略规则、2-奖品规则*/
    private Integer ruleType;
    /*抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】*/
    private String ruleModel;
    /*抽奖规则比值*/
    private String ruleValue;
    /*抽奖规则描述*/
    private String ruleDesc;

    public Map<String , List<Integer>> getRuleWeights(){
        if(!ruleModel.equals("rule_weight")){return null;}
        return Arrays.stream(ruleValue.split(Constants.SPACE))
                .filter(ruleValueGroup -> ruleValueGroup != null && !ruleValueGroup.isEmpty())
                .map(ruleValueGroup ->{
                    String[] parts = ruleValueGroup.split(Constants.COLON);
                    if(parts.length != 2){
                        throw new IllegalArgumentException("rule weight rule_value invalid");
                    }
                    List<Integer> values = Arrays.stream(parts[1].split(Constants.SPLIT))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                    return  new AbstractMap.SimpleEntry<>(ruleValueGroup,values);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

}
