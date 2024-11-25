package io.github.jasonxqh.domain.activity.service.armory;

import lombok.Data;

import java.util.Date;

public interface IActivityDispatch {
    Boolean subtractionSkuStock(Long sku, Date endDateTime);
}
