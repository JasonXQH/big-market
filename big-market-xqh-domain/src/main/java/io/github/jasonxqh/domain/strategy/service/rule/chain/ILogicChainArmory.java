package io.github.jasonxqh.domain.strategy.service.rule.chain;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/12, 星期二
 * @Description :
 **/
public interface ILogicChainArmory {

    ILogicChain appendNext(ILogicChain next);

    ILogicChain next();
}
