package com.vending.module.cabinet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.product.entity.Product;

import java.util.List;
import java.util.Map;

public interface CabinetService extends IService<Cabinet> {
    List<Map<String, Object>> getCabinetProducts(Long cabinetId);
}
