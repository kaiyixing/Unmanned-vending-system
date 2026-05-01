package com.vending.module.inventory.service.impl;

import com.vending.module.inventory.entity.Inventory;
import com.vending.module.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class InventoryAlertTask {

    private final InventoryService inventoryService;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(cron = "0 */5 * * * ?")
    public void checkInventoryAlert() {
        List<Inventory> alerts = inventoryService.lambdaQuery()
                .apply("quantity <= threshold")
                .list();

        if (!alerts.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(
                        "inventory:alerts",
                        alerts.toString(),
                        10, TimeUnit.MINUTES
                );
            } catch (Exception e) {
                // Redis操作失败不影响主流程
            }
        }
    }
}
