package io.github.jasonxqh.infrastructure.dao.po.strategy;

import lombok.Data;

import java.util.Date;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description :
 **/
@Data
public class RuleTreeNodeLine {
    private Long    id;
    private String  treeId;
    private String  ruleNodeFrom;
    private String  ruleNodeTo;
    private String  ruleLimitType;
    private String  ruleLimitValue;
    private Date    createTime;
    private Date    updateTime;
}
