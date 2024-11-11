package io.github.jasonxqh.domain.strategy.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description :
 **/

@Getter
@AllArgsConstructor
public enum RuleLogicCheckTypeVO {
    ALLOW("0000","放行; 执行后续的流程，不受规则引擎影响"),
    TAKE_OVER("0001","接管； 后续流程，受规则引擎执行结果影响"),
    ;
    private final String code;
    private final String info;
}
