package io.github.jasonxqh.domain.strategy.model.entity;

import io.github.jasonxqh.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;

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
public class StrategyEntity {
    //抽奖策略ID
    private Long strategyId;
    //抽奖策略描述
    private String strategyDesc;
    //规则模型
    private String ruleModels;

    public String[] ruleModels(){
        if(StringUtils.isBlank(ruleModels)){return null;}
        return ruleModels.split(Constants.SPLIT);
    }

    public String getRuleWeight(){
        String[] ruleModels = ruleModels();
        if(ruleModels == null || ruleModels.length == 0){return null;}
        return Arrays.stream(ruleModels).filter(ruleModel -> ruleModel.equals("rule_weight"))
                                 .findAny()
                                 .orElse(null);
    }
}
