package com.vending.module.cabinet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.result.Result;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.service.CabinetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cabinet")
@RequiredArgsConstructor
public class CabinetController {

    private final CabinetService cabinetService;

    @GetMapping("/list")
    public Result<Page<Cabinet>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String city) {
        Page<Cabinet> cabinetPage = new Page<>(page, size);
        if (city != null && !city.isEmpty()) {
            cabinetPage = cabinetService.lambdaQuery()
                    .eq(Cabinet::getCity, city)
                    .page(cabinetPage);
        } else {
            cabinetPage = cabinetService.page(cabinetPage);
        }
        return Result.success(cabinetPage);
    }

    @GetMapping("/{id}/products")
    public Result<List<Map<String, Object>>> getProducts(@PathVariable Long id) {
        List<Map<String, Object>> products = cabinetService.getCabinetProducts(id);
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
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Cabinet cabinet) {
        cabinetService.updateById(cabinet);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cabinetService.removeById(id);
        return Result.success();
    }
}
