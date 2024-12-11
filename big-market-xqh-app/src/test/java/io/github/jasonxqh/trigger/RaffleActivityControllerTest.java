package io.github.jasonxqh.trigger;

import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.api.IRaffleActivityService;
import io.github.jasonxqh.api.dto.ActivityDrawRequestDTO;
import io.github.jasonxqh.api.dto.ActivityDrawResponseDTO;
import io.github.jasonxqh.api.dto.UserActivityAccountRequestDTO;
import io.github.jasonxqh.api.dto.UserActivityAccountResponseDTO;
import io.github.jasonxqh.api.response.Response;
import io.github.jasonxqh.domain.activity.service.armory.IActivitySkuArmory;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyArmory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动服务测试
 * @create 2024-04-20 11:02
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityControllerTest {
    @Resource
    private IActivitySkuArmory activityArmory;
    @Resource
    private IStrategyArmory strategyArmory;
    @Resource
    private IRaffleActivityService raffleActivityService;
    @Before
    public void setUp() {
        log.info("装配活动：{}", activityArmory.assembleActivitySkuByActivityId(100301L));
        log.info("装配活动：{}",  strategyArmory.assembleLotteryStrategyByActivityId(100301L));
    }
    @Test
    public void test_armory() {
        Response<Boolean> response = raffleActivityService.activityArmory(100301L);
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    @Test
    public void test_draw() {
        for (int i = 0; i < 10; i++) {
            ActivityDrawRequestDTO request = new ActivityDrawRequestDTO();
            request.setActivityId(100301L);
            request.setUserId("xiaofuge");
            Response<ActivityDrawResponseDTO> response = raffleActivityService.draw(request);

            log.info("请求参数：{}", JSON.toJSONString(request));
            log.info("测试结果：{}", JSON.toJSONString(response));
        }
    }

    @Test
    public void test_blacklist_draw() throws InterruptedException {

        for (int i = 0; i < 10; i++) {
            ActivityDrawRequestDTO request = new ActivityDrawRequestDTO();
            request.setActivityId(100301L);
            request.setUserId("user003");
            Response<ActivityDrawResponseDTO> response = raffleActivityService.draw(request);

            log.info("请求参数：{}", JSON.toJSONString(request));
            log.info("测试结果：{}", JSON.toJSONString(response));
        }
        // 让程序挺住方便测试，也可以去掉
        new CountDownLatch(1).await();
    }

    @Test
    public void test_calendarSignRebate() throws InterruptedException {
        Response<Boolean> response = raffleActivityService.calendarSignRebate("xiaofuge");
        log.info("测试结果：{}", JSON.toJSONString(response));
        // 让程序挺住方便测试，也可以去掉
        new CountDownLatch(1).await();
    }

    @Test
    public void test_isCalendarSignRebate() {
        Response<Boolean> response = raffleActivityService.isCalendarSignRebate("xiaofuge");
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    @Test
    public void test_queryUserActivityAccount() {
        UserActivityAccountRequestDTO request = new UserActivityAccountRequestDTO();
        request.setActivityId(100301L);
        request.setUserId("xiaofuge");

        // 查询数据
        Response<UserActivityAccountResponseDTO> response = raffleActivityService.queryUserActivityAccount(request);

        log.info("请求参数：{}", JSON.toJSONString(request));
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

}
