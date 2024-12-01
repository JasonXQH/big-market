package io.github.jasonxqh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/17, 星期日
 * @Description : 
**/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleStrategyRequestDTO {
    private Long strategyId;
}
