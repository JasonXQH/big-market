package io.github.jasonxqh.domain.credit.model.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TaskStateVO {
   //任务状态；create-创建、completed-完成、fail-失败
    completed("completed", "完成"),
    create("create","创建"),
    fail("fail","失败")
    ;
    private final String code;
    private final String desc;
}
