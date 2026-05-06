package com.vending.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.result.Result;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.service.CabinetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/cabinet")
@RequiredArgsConstructor
public class AdminCabinetController {

    private final CabinetService cabinetService;

    @GetMapping("/list")
    public Result<Page<Cabinet>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String city) {
        Page<Cabinet> cabinetPage = new Page<>(page, size);
        cabinetPage = cabinetService.lambdaQuery()
                .eq(city != null && !city.isEmpty(), Cabinet::getCity, city)
                .page(cabinetPage);
        return Result.success(cabinetPage);
    }

    @GetMapping("/cities")
    public Result<List<String>> getCities() {
        List<Cabinet> cabinets = cabinetService.list();
        List<String> cities = cabinets.stream()
                .map(Cabinet::getCity)
                .distinct()
                .filter(city -> city != null && !city.isEmpty())
                .sorted()
                .collect(Collectors.toList());
        return Result.success(cities);
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
