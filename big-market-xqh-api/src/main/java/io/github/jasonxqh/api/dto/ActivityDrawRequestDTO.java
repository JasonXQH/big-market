package io.github.jasonxqh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDrawRequestDTO {
    /**
     * 活动ID
     * */
    private Long activityId;

    /**
     * 用户ID
     */
    private String userId;
}
