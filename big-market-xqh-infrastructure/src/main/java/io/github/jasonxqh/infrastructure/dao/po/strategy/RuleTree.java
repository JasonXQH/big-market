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
public class RuleTree {
    private Long    id;
    private String  treeId;
    private String  treeName;
    private String  treeDesc;
    private String  treeNodeRuleKey;
    private Date    createTime;
    private Date    updateTime;
}
