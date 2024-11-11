package io.github.jasonxqh.test.infrastructure;

import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.infrastructure.dao.IAwardDao;
import io.github.jasonxqh.infrastructure.dao.po.Award;
import io.github.jasonxqh.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description : 奖品持久化单元测试
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AwardDaoTest {
    @Resource
    private IRedisService redisService;

    @Test
    public void test() {
        RMap<Object, Object> map = redisService.getMap("strategy_id_100001");
        map.put(1,101);
        map.put(2,101);
        map.put(3,101);
        map.put(4,102);
        map.put(5,102);
        map.put(6,102);
        map.put(7,103);
        map.put(8,103);
        map.put(9,104);
        map.put(10,105);
        log.info("测试结果: {}",redisService.getFromMap("strategy_id_100001", 1).toString());
    }

}