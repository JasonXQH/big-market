package io.github.jasonxqh.domain.credit.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TradeTypeVO {
    //forward-正向，用户收到积分、reverse-逆向，用户使用积分
    forward("forward", "正向"),
    reverse("reverse", "逆向"),
    ;

    private final String code;
    private final String desc;
}
