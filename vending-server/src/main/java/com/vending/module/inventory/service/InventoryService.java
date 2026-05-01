package com.vending.module.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vending.module.inventory.entity.Inventory;

public interface InventoryService extends IService<Inventory> {
    boolean checkStock(Long cabinetId, Long productId, Integer quantity);
    void deductStock(Long cabinetId, Long orderId);
    void rollbackStock(Long cabinetId, Long orderId);
}
