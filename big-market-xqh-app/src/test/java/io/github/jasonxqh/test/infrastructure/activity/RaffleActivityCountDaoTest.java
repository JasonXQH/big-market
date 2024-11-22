package io.github.jasonxqh.test.infrastructure.activity;

import io.github.jasonxqh.infrastructure.dao.IRaffleActivityCountDao;
import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivityCount;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityCountDaoTest {
    @Resource
    private IRaffleActivityCountDao mapper;


    @Test
    public void testQueryRaffleActivityCountByActivityCountId() {
        RaffleActivityCount raffleActivityCount = mapper.queryRaffleActivityCountByActivityCountId(11101L);
        System.out.println(raffleActivityCount);
    }
}
