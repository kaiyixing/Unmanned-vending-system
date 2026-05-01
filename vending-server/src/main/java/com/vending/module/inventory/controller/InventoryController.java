package com.vending.module.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.result.Result;
import com.vending.module.inventory.entity.Inventory;
import com.vending.module.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/list")
    public Result<Page<Inventory>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long cabinetId) {
        Page<Inventory> inventoryPage = new Page<>(page, size);
        if (cabinetId != null) {
            inventoryPage = inventoryService.lambdaQuery()
                    .eq(Inventory::getCabinetId, cabinetId)
                    .page(inventoryPage);
        } else {
            inventoryPage = inventoryService.page(inventoryPage);
        }
        return Result.success(inventoryPage);
    }

    @GetMapping("/alerts")
    public Result<List<Inventory>> getAlerts() {
        List<Inventory> alerts = inventoryService.lambdaQuery()
                .apply("quantity <= threshold")
                .list();
        return Result.success(alerts);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Inventory inventory) {
        inventoryService.updateById(inventory);
        return Result.success();
    }
}
