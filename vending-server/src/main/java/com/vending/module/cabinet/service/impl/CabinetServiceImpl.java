package com.vending.module.cabinet.service.impl;

========== Bcrypt Hash for 123456 ==========
$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
============================================import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.mapper.CabinetMapper;
import com.vending.module.cabinet.service.CabinetService;
import com.vending.module.inventory.entity.Inventory;
import com.vending.module.inventory.mapper.InventoryMapper;
import com.vending.module.product.entity.Product;
import com.vending.module.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CabinetServiceImpl extends ServiceImpl<CabinetMapper, Cabinet> implements CabinetService {

    private final InventoryMapper inventoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<Map<String, Object>> getCabinetProducts(Long cabinetId) {
        List<Inventory> inventories = inventoryMapper.selectList(
                Wrappers.<Inventory>lambdaQuery().eq(Inventory::getCabinetId, cabinetId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Inventory inv : inventories) {
            Product product = productMapper.selectById(inv.getProductId());
            if (product != null && product.getStatus() == 1) {
                Map<String, Object> item = new HashMap<>();
                item.put("product", product);
                item.put("stock", inv.getQuantity());
                item.put("inventoryId", inv.getInventoryId());
                result.add(item);
            }
        }
        return result;
    }
}
