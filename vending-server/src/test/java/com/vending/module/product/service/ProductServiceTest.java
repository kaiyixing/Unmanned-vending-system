package com.vending.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.BaseTest;
import com.vending.module.product.entity.Product;
import com.vending.module.product.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductService 商品服务测试")
class ProductServiceTest extends BaseTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    @DisplayName("测试新增商品")
    void testSaveProduct() {
        Product product = new Product();
        product.setName("测试商品");
        product.setCategory("饮料");
        product.setPrice(new BigDecimal("3.50"));
        product.setCostPrice(new BigDecimal("2.00"));
        product.setStatus(1);

        boolean result = productService.save(product);
        assertTrue(result);
        assertNotNull(product.getProductId());
    }

    @Test
    @DisplayName("测试查询商品列表")
    void testListProducts() {
        for (int i = 1; i <= 5; i++) {
            Product product = new Product();
            product.setName("商品" + i);
            product.setCategory("测试分类");
            product.setPrice(new BigDecimal(i + ".00"));
            product.setStatus(1);
            productService.save(product);
        }

        List<Product> list = productService.list();
        assertEquals(5, list.size());
    }

    @Test
    @DisplayName("测试根据ID查询商品")
    void testGetProductById() {
        Product product = new Product();
        product.setName("单个查询测试商品");
        product.setCategory("分类");
        product.setPrice(new BigDecimal("10.00"));
        product.setStatus(1);
        productService.save(product);

        Product found = productService.getById(product.getProductId());
        assertNotNull(found);
        assertEquals("单个查询测试商品", found.getName());
    }

    @Test
    @DisplayName("测试更新商品")
    void testUpdateProduct() {
        Product product = new Product();
        product.setName("旧名称");
        product.setCategory("分类");
        product.setPrice(new BigDecimal("5.00"));
        product.setStatus(1);
        productService.save(product);

        product.setName("新名称");
        product.setPrice(new BigDecimal("6.00"));
        boolean result = productService.updateById(product);
        assertTrue(result);

        Product updated = productService.getById(product.getProductId());
        assertEquals("新名称", updated.getName());
        assertEquals(new BigDecimal("6.00"), updated.getPrice());
    }

    @Test
    @DisplayName("测试删除商品")
    void testDeleteProduct() {
        Product product = new Product();
        product.setName("要删除的商品");
        product.setCategory("分类");
        product.setPrice(new BigDecimal("5.00"));
        product.setStatus(1);
        productService.save(product);

        boolean result = productService.removeById(product.getProductId());
        assertTrue(result);

        Product deleted = productService.getById(product.getProductId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试根据分类查询商品")
    void testGetProductsByCategory() {
        Product p1 = new Product();
        p1.setName("饮料1");
        p1.setCategory("饮料");
        p1.setPrice(new BigDecimal("3.00"));
        p1.setStatus(1);
        productService.save(p1);

        Product p2 = new Product();
        p2.setName("零食1");
        p2.setCategory("零食");
        p2.setPrice(new BigDecimal("5.00"));
        p2.setStatus(1);
        productService.save(p2);

        Product p3 = new Product();
        p3.setName("饮料2");
        p3.setCategory("饮料");
        p3.setPrice(new BigDecimal("4.00"));
        p3.setStatus(1);
        productService.save(p3);

        List<Product> drinks = productService.lambdaQuery()
                .eq(Product::getCategory, "饮料")
                .list();
        
        assertEquals(2, drinks.size());
    }

    @Test
    @DisplayName("测试商品状态查询")
    void testGetProductsByStatus() {
        Product p1 = new Product();
        p1.setName("上架商品");
        p1.setCategory("分类");
        p1.setPrice(new BigDecimal("5.00"));
        p1.setStatus(1);
        productService.save(p1);

        Product p2 = new Product();
        p2.setName("下架商品");
        p2.setCategory("分类");
        p2.setPrice(new BigDecimal("5.00"));
        p2.setStatus(0);
        productService.save(p2);

        List<Product> activeProducts = productService.lambdaQuery()
                .eq(Product::getStatus, 1)
                .list();
        
        assertEquals(1, activeProducts.size());
        assertEquals("上架商品", activeProducts.get(0).getName());
    }

    @Test
    @DisplayName("测试批量删除商品")
    void testBatchDeleteProducts() {
        for (int i = 0; i < 3; i++) {
            Product product = new Product();
            product.setName("商品" + i);
            product.setCategory("分类");
            product.setPrice(new BigDecimal("5.00"));
            product.setStatus(1);
            productService.save(product);
        }

        List<Product> list = productService.list();
        List<Long> ids = list.stream().map(Product::getProductId).toList();
        
        boolean result = productService.removeByIds(ids);
        assertTrue(result);

        List<Product> remaining = productService.list();
        assertEquals(0, remaining.size());
    }
}
