package io.github.jasonxqh.domain.activity.service.armory;

import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;

import java.util.Date;

public interface IActivityDispatch {
    Boolean subtractionSkuStock(RaffleActivitySkuEntity raffleActivitySkuEntity, Date endDateTime);
}
