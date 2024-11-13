package io.github.jasonxqh.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description :
 **/
@Data
public class RuleTreeNode {
    private Long id;
    private String treeId;
    private String ruleKey;
    private String ruleDesc;
    private String ruleValue;
    private Date createTime;
    private Date updateTime;
}
