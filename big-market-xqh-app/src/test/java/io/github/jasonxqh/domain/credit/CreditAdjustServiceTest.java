package io.github.jasonxqh.domain.credit;

import io.github.jasonxqh.domain.activity.model.entity.SkuRechargeEntity;
import io.github.jasonxqh.domain.activity.model.valobj.OrderTradeTypeVO;
import io.github.jasonxqh.domain.activity.service.IRaffleActivityAccountQuotaService;
import io.github.jasonxqh.domain.credit.model.entity.TradeEntity;
import io.github.jasonxqh.domain.credit.model.vo.TradeNameVO;
import io.github.jasonxqh.domain.credit.model.vo.TradeTypeVO;
import io.github.jasonxqh.domain.credit.service.ICreditAdjustService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 积分额度增加服务测试
 * @create 2024-06-01 10:22
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CreditAdjustServiceTest {

    @Resource
    private ICreditAdjustService creditAdjustService;

    @Resource
    private IRaffleActivityAccountQuotaService accountQuotaService;

    @Test
    public void test_createOrder_forward() {
        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.setUserId("xiaofuge");
        tradeEntity.setTradeName(TradeNameVO.REBATE);
        tradeEntity.setTradeType(TradeTypeVO.forward);
        tradeEntity.setAmount(new BigDecimal("10.19"));
        tradeEntity.setOutBusinessNo("12406039900002");
        creditAdjustService.createOrder(tradeEntity);
    }

    @Test
    public void test_createOrder_reverse() {
        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.setUserId("xiaofuge");
        tradeEntity.setTradeName(TradeNameVO.REBATE);
        tradeEntity.setTradeType(TradeTypeVO.reverse);
        tradeEntity.setAmount(new BigDecimal("-10.19"));
        tradeEntity.setOutBusinessNo("20000990991");
        creditAdjustService.createOrder(tradeEntity);
    }

    @Test
    public void test_createOrder_pay() throws InterruptedException {
        //先创建raffle_activity_order订单，此时账单未付款
        SkuRechargeEntity skuRechargeEntity = new SkuRechargeEntity();
        skuRechargeEntity.setUserId("lzh");
        skuRechargeEntity.setSku(9011L);
        skuRechargeEntity.setOrderTradeType(OrderTradeTypeVO.credit_pay_trade);
        skuRechargeEntity.setOutBusinessNo("70009240609009");
        accountQuotaService.createOrder(skuRechargeEntity);
        // 然后创建积分变更订单，变更成功后，说明消费成功
        // 然后发送mq消息告诉customer，对raffle_activity_account进行更新，添加抽奖次数
        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.setUserId("lzh");
        tradeEntity.setTradeName(TradeNameVO.CONVERT_SKU);
        tradeEntity.setTradeType(TradeTypeVO.reverse);
        tradeEntity.setAmount(new BigDecimal("-10"));
        tradeEntity.setOutBusinessNo("70009240609009");
        creditAdjustService.createOrder(tradeEntity);
        new CountDownLatch(1).await();
    }


}
