package io.github.jasonxqh.domain.activity.service.partake;

import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityAccountDayEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityAccountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityAccountMonthEntity;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class RaffleActivityPartakeService extends AbstractRaffleActivityPartake{
    private final SimpleDateFormat dateFormatMonth = new SimpleDateFormat("yyyy-MM");
    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");

    protected RaffleActivityPartakeService(IActivityRepository activityRepository) {
        super(activityRepository);
    }



    @Override
    protected CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate) {
        //1. 查询账户总额度
        RaffleActivityAccountEntity raffleActivityAccountEntity = activityRepository.queryActivityAccountByUserIdAndActivityId(userId,activityId);
        //2. 判断总额度是否足够
        if(raffleActivityAccountEntity == null||raffleActivityAccountEntity.getTotalCountSurplus() <= 0){
            throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(),ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
        }
        String month = dateFormatMonth.format(currentDate);
        //3.查询月额度
        RaffleActivityAccountMonthEntity raffleActivityAccountMonthEntity = activityRepository.queryActivityAccountMonthByUserId(userId,activityId,month);
        if(raffleActivityAccountMonthEntity!= null && raffleActivityAccountMonthEntity.getMonthCountSurplus() <=0){
            throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(),ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
        }
        boolean isExistAccountMonth = null != raffleActivityAccountMonthEntity;
        if(raffleActivityAccountMonthEntity == null){
            raffleActivityAccountMonthEntity = RaffleActivityAccountMonthEntity.builder()
                    .activityId(activityId)
                    .month(month)
                    .userId(userId)
                    .monthCount(raffleActivityAccountEntity.getMonthCount())
                    .monthCountSurplus(raffleActivityAccountEntity.getMonthCountSurplus())
                    .build();
        }

        //4.查询日额度
        String day = dateFormatDay.format(currentDate);
        RaffleActivityAccountDayEntity raffleActivityAccountDayEntity = activityRepository.queryActivityAccountDayByUserId(userId,activityId,day);

        if(raffleActivityAccountDayEntity != null && raffleActivityAccountDayEntity.getDayCountSurplus() <=0){
            throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(),ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
        }
        boolean isExistDay = null != raffleActivityAccountDayEntity;
        if(raffleActivityAccountDayEntity == null){
            raffleActivityAccountDayEntity = raffleActivityAccountDayEntity.builder()
                    .activityId(activityId)
                    .day(day)
                    .userId(userId)
                    .dayCount(raffleActivityAccountEntity.getDayCount())
                    .dayCountSurplus(raffleActivityAccountEntity.getDayCountSurplus())
                    .build();
        }

        CreatePartakeOrderAggregate createPartakeOrderAggregate = CreatePartakeOrderAggregate.builder()
                .activityId(activityId)
                .userId(userId)
                .isExistAccountMonth(isExistAccountMonth)
                .isExistAccountDay(isExistDay)
                .raffleActivityAccountDayEntity(raffleActivityAccountDayEntity)
                .raffleActivityAccountMonthEntity(raffleActivityAccountMonthEntity)
                .raffleActivityAccountEntity(raffleActivityAccountEntity)
                .build();

        return createPartakeOrderAggregate;
    }

//    @Override
//    protected void doSavePartakeOrder(CreatePartakeOrderAggregate createPartakeOrderAggregate) {
//        activityRepository.doSavePartakeOrder();
//    }
}
