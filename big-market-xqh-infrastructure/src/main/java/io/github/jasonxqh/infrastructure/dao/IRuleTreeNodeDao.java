package io.github.jasonxqh.infrastructure.dao;

import io.github.jasonxqh.infrastructure.dao.po.strategy.RuleTreeNode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/13, 星期三
 * @Description :
 **/

@Mapper
public interface IRuleTreeNodeDao {
    List<RuleTreeNode> queryRuleTreeNodeListByTreeId(String treeId);


    List<RuleTreeNode> queryRuleLocks(String[] treeIds);
}
