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
}
