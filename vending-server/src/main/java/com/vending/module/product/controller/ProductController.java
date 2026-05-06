package com.vending.module.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.common.exception.BusinessException;
import com.vending.common.result.Result;
import com.vending.common.result.ResultCode;
import com.vending.module.product.entity.Product;
import com.vending.module.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final RedisCacheUtil redisCacheUtil;

    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String category) {
        String cacheKey = RedisCacheUtil.KEY_PRODUCT_LIST + page + ":" + size + ":" + (category == null ? "all" : category);
        
        @SuppressWarnings("unchecked")
        Page<Product> cached = redisCacheUtil.get(cacheKey, Page.class);
        if (cached != null) {
            return Result.success(cached);
        }

        Page<Product> productPage = new Page<>(page, size);
        if (category != null && !category.isEmpty()) {
            productPage = productService.lambdaQuery()
                    .eq(Product::getCategory, category)
                    .eq(Product::getStatus, 1)
                    .page(productPage);
        } else {
            productPage = productService.lambdaQuery()
                    .eq(Product::getStatus, 1)
                    .page(productPage);
        }

        redisCacheUtil.set(cacheKey, productPage, 30, TimeUnit.MINUTES);
        return Result.success(productPage);
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Void> save(@Valid @RequestBody Product product) {
        productService.save(product);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_PRODUCT_LIST);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Void> update(@Valid @RequestBody Product product) {
        productService.updateById(product);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_PRODUCT_LIST);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        redisCacheUtil.deleteByPrefix(RedisCacheUtil.KEY_PRODUCT_LIST);
        return Result.success();
    }
}
