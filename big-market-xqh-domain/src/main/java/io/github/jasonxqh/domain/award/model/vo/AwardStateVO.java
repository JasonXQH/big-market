package io.github.jasonxqh.domain.award.model.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AwardStateVO {
    //create-创建、completed-发奖完成
    create("create", "创建"),
    complete("complete", "发奖完成"),
    fail("fail", "发奖失败"),
    ;

    private final String code;
    private final String desc;
}
