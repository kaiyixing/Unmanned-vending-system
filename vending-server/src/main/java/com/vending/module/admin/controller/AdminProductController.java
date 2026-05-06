package com.vending.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.common.result.Result;
import com.vending.module.product.entity.Product;
import com.vending.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final RedisCacheUtil redisCacheUtil;

    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {
        Page<Product> productPage = new Page<>(page, size);
        productPage = productService.lambdaQuery()
                .eq(category != null && !category.isEmpty(), Product::getCategory, category)
                .like(name != null && !name.isEmpty(), Product::getName, name)
                .page(productPage);
        return Result.success(productPage);
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Product product) {
        productService.save(product);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_PRODUCT_LIST);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_CABINET_PRODUCTS);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.updateById(product);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_PRODUCT_LIST);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_CABINET_PRODUCTS);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_PRODUCT_LIST);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_CABINET_PRODUCTS);
        return Result.success();
    }
}
