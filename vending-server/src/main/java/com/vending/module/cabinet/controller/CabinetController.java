package com.vending.module.cabinet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.common.result.Result;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.service.CabinetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/cabinet")
@RequiredArgsConstructor
public class CabinetController {

    private final CabinetService cabinetService;
    private final RedisCacheUtil redisCacheUtil;

    @GetMapping("/list")
    public Result<Page<Cabinet>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String city) {
        String cacheKey = RedisCacheUtil.KEY_CABINET_LIST + page + ":" + size + ":" + (city == null ? "all" : city);
        
        @SuppressWarnings("unchecked")
        Page<Cabinet> cached = redisCacheUtil.get(cacheKey, Page.class);
        if (cached != null) {
            return Result.success(cached);
        }

        Page<Cabinet> cabinetPage = new Page<>(page, size);
        if (city != null && !city.isEmpty()) {
            cabinetPage = cabinetService.lambdaQuery()
                    .eq(Cabinet::getCity, city)
                    .page(cabinetPage);
        } else {
            cabinetPage = cabinetService.page(cabinetPage);
        }

        redisCacheUtil.set(cacheKey, cabinetPage, 30, TimeUnit.MINUTES);
        return Result.success(cabinetPage);
    }

    @GetMapping("/{id}/products")
    public Result<List<Map<String, Object>>> getProducts(@PathVariable Long id) {
        String cacheKey = RedisCacheUtil.KEY_CABINET_PRODUCTS + id;
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cached = redisCacheUtil.get(cacheKey, List.class);
        if (cached != null) {
            return Result.success(cached);
        }

        List<Map<String, Object>> products = cabinetService.getCabinetProducts(id);
        redisCacheUtil.set(cacheKey, products, 30, TimeUnit.MINUTES);
        return Result.success(products);
    }

    @GetMapping("/{id}")
    public Result<Cabinet> getById(@PathVariable Long id) {
        Cabinet cabinet = cabinetService.getById(id);
        return Result.success(cabinet);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Cabinet cabinet) {
        cabinetService.save(cabinet);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_CABINET_LIST);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Cabinet cabinet) {
        cabinetService.updateById(cabinet);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_CABINET_LIST);
        redisCacheUtil.delete(RedisCacheUtil.KEY_CABINET_PRODUCTS + cabinet.getCabinetId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cabinetService.removeById(id);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_CABINET_LIST);
        redisCacheUtil.delete(RedisCacheUtil.KEY_CABINET_PRODUCTS + id);
        return Result.success();
    }
}
