package io.github.jasonxqh.api.dto;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/17, 星期日
 * @Description :
 **/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardListRequestDTO {
    // 用户ID
    private String userId;
    // 抽奖活动ID
    private Long activityId;
}
