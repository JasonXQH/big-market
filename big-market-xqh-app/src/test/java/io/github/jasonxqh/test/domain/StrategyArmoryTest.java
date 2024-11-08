package io.github.jasonxqh.test.domain;

import io.github.jasonxqh.domain.strategy.service.armory.IStrategyArmory;
import io.github.jasonxqh.domain.strategy.service.armory.StrategyArmory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description :
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest

public class StrategyArmoryTest {

    @Resource
    private IStrategyArmory strategyArmory;

    @Test
    public void test_strategyArmory() {
        strategyArmory.assembleLotteryStrategy(100002L);

    }

    @Test
    public void test_strategyRandomVal() {
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
        System.out.println(strategyArmory.getRandomAwardId(100002L));
//        log.info("测试结果：｛｝ - 奖品ID值",strategyArmory.getRandomAwardId(100002L));
//        log.info("测试结果：｛｝- 奖品ID值",strategyArmory.getRandomAwardId( 100002L));
//        log.info("测试结果：｛｝ - 奖品ID值",strategyArmory.getRandomAwardId(100002L));
    }
}
