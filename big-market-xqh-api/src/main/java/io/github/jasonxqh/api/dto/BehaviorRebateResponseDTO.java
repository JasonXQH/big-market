package io.github.jasonxqh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorRebateResponseDTO {
    /**
     *  用户Id
     * */
    private String userId;
 
    private String orderId;
}
