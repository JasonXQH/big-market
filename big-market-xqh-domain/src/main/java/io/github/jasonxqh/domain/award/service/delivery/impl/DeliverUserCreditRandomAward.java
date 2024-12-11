package io.github.jasonxqh.domain.award.service.delivery.impl;

import com.mysql.cj.util.StringUtils;
import io.github.jasonxqh.domain.award.adapter.repository.IAwardRepository;
import io.github.jasonxqh.domain.award.model.aggregate.GiveOutPrizesAggregate;
import io.github.jasonxqh.domain.award.model.entity.DeliverAwardEntity;
import io.github.jasonxqh.domain.award.model.entity.UserAwardRecordEntity;
import io.github.jasonxqh.domain.award.model.entity.UserCreditRandomAwardEntity;
import io.github.jasonxqh.domain.award.model.vo.AwardStateVO;
import io.github.jasonxqh.domain.award.service.IAwardService;
import io.github.jasonxqh.domain.award.service.delivery.IDeliverAward;
import io.github.jasonxqh.types.common.Constants;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.scanner.Constant;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author jasonxu
 */
@Component("user_credit_random")
public class DeliverUserCreditRandomAward implements IDeliverAward {
    @Resource
    private IAwardRepository awardRepository;
    @Override
    public void giveOutPrizes(DeliverAwardEntity deliverAwardEntity) {
        String userId = deliverAwardEntity.getUserId();
        String orderId = deliverAwardEntity.getOrderId();
        Integer awardId = deliverAwardEntity.getAwardId();
        String awardConfig = deliverAwardEntity.getAwardConfig();
        if(StringUtils.isNullOrEmpty(userId) || StringUtils.isNullOrEmpty(orderId) || awardId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(),ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        if(StringUtils.isNullOrEmpty(awardConfig)) {
            //0.01,1,1,100
            awardConfig = awardRepository.queryAwardConfigByAwardId(awardId);
        }
        //根据awardConfig去生成一个随机的积分值
        String[] creditRange = awardConfig.split(Constants.SPLIT);
        if(creditRange.length != 2) {
            throw new AppException("awardConfig 未正常配置");
        }
        BigDecimal creditAmount = generateRandom(new BigDecimal(creditRange[0]),new BigDecimal(creditRange[1]));

        UserAwardRecordEntity userAwardRecordEntity = GiveOutPrizesAggregate.buildDeliverUserAwardRecordEntity(userId,orderId,awardId, AwardStateVO.complete);
        UserCreditRandomAwardEntity userCreditRandomAwardEntity = GiveOutPrizesAggregate.buildUserCreditRandomAwardEntity(userId, creditAmount);
        GiveOutPrizesAggregate giveOutPrizesAggregate = GiveOutPrizesAggregate.builder()
                .userCreditRandomAwardEntity(userCreditRandomAwardEntity)
                .userAwardRecordEntity(userAwardRecordEntity)
                .userId(userId)
                .build();
        //存储发奖对象
        awardRepository.saveGiveOutPrizesAggregate(giveOutPrizesAggregate);

    }

    private BigDecimal generateRandom(BigDecimal min,BigDecimal max) {
        if(min.equals(max)) return min;
        BigDecimal randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));
        return randomBigDecimal.round(new MathContext(3));
    }
}
