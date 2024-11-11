package io.github.jasonxqh.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 **/
@Data
public class Strategy {
   //自增ID
    private Long id;
   //抽奖策略ID
    private Long strategyId;
   //抽奖策略描述
    private String strategyDesc;
    //规则模型
    private String ruleModels;
   //创建时间
    private Date createTime;
   //更新时间
    private Date updateTime;
}
