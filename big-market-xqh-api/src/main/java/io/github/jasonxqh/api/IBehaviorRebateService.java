package io.github.jasonxqh.api;

import io.github.jasonxqh.api.dto.BehaviorRebateRequestDTO;
import io.github.jasonxqh.api.dto.BehaviorRebateResponseDTO;
import io.github.jasonxqh.api.response.Response;

public interface IBehaviorRebateService {
    Response<BehaviorRebateResponseDTO> userBehaviorRebate(BehaviorRebateRequestDTO requestDTO);
}
