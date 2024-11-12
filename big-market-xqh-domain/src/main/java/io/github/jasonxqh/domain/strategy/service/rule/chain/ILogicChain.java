package io.github.jasonxqh.domain.strategy.service.rule.chain;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/12, 星期二
 * @Description : 责任链
 **/
public interface ILogicChain  extends ILogicChainArmory {
    /*
     * 责任链接口
     * @param userId        用户ID
     * @param strategyId    策略ID
     * @return 奖品ID
     * */
    Integer logic(String userId, Long Strategy);


}