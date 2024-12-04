package io.github.jasonxqh.api;

import io.github.jasonxqh.api.dto.*;
import io.github.jasonxqh.api.response.Response;

import java.util.List;

/**
 * @author jasonxu
 * @date 2024/12/01
 */
public interface  IRaffleActivityService {

    //策略装配接口
    Response<Boolean> activityArmory(Long activityId);

    Response<ActivityDrawResponseDTO> draw(ActivityDrawRequestDTO requestDTO);

    /**
     * 日历签到返利接口
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    Response<Boolean> calendarSignRebate(String userId);

    /**
     * 判断是否完成日历签到返利接口
     *
     * @param userId 用户ID
     * @return 签到结果 true 已签到，false 未签到
     */
    Response<Boolean> isCalendarSignRebate(String userId);

    /**
     * 查询用户活动账户
     *
     * @param request 请求对象「活动ID、用户ID」
     * @return 返回结果「总额度、月额度、日额度」
     */
    Response<UserActivityAccountResponseDTO> queryUserActivityAccount(UserActivityAccountRequestDTO request);
}
