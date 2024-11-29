package io.github.jasonxqh.domain.award.model.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AwardStateVO {
    //create-创建、completed-发奖完成
    completed("completed", "发奖完成"),
    create("create","创建")
    ;

    private final String code;
    private final String desc;
}
