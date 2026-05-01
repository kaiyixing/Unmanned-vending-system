package com.vending.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String category) {
        Page<Product> productPage = new Page<>(page, size);
        if (category != null && !category.isEmpty()) {
            productPage = productService.lambdaQuery()
                    .eq(Product::getCategory, category)
                    .page(productPage);
        } else {
            productPage = productService.page(productPage);
        }
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
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }
}
