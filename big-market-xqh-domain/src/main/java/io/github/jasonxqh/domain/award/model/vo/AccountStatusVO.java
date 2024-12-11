package io.github.jasonxqh.domain.award.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountStatusVO {
     //【open - 可用，close - 冻结】
     open("open", "可用"),
    close("close", "冻结"),
    ;

    private final String code;
    private final String desc;
}
