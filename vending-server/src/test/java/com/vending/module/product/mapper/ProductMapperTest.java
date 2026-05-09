package com.vending.module.product.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.BaseTest;
import com.vending.module.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductMapper 商品数据访问层测试")
class ProductMapperTest extends BaseTest {

    @Autowired
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    @DisplayName("测试插入商品")
    void testInsertProduct() {
        Product product = new Product();
        product.setName("Mapper测试商品");
        product.setCategory("饮料");
        product.setPrice(new BigDecimal("5.50"));
        product.setCostPrice(new BigDecimal("3.00"));
        product.setStatus(1);

        int result = productMapper.insert(product);
        assertEquals(1, result);
        assertNotNull(product.getProductId());
    }

    @Test
    @DisplayName("测试根据ID查询商品")
    void testSelectById() {
        Product product = new Product();
        product.setName("Mapper查询商品");
        product.setCategory("零食");
        product.setPrice(new BigDecimal("10.00"));
        product.setStatus(1);
        productMapper.insert(product);

        Product found = productMapper.selectById(product.getProductId());
        assertNotNull(found);
        assertEquals("Mapper查询商品", found.getName());
    }

    @Test
    @DisplayName("测试根据条件查询商品列表")
    void testSelectList() {
        for (int i = 1; i <= 3; i++) {
            Product product = new Product();
            product.setName("商品" + i);
            product.setCategory("食品");
            product.setPrice(new BigDecimal(i + ".00"));
            product.setStatus(i % 2 == 0 ? 1 : 0);
            productMapper.insert(product);
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        List<Product> list = productMapper.selectList(wrapper);
        
        assertEquals(1, list.size());
        assertEquals("商品2", list.get(0).getName());
    }

    @Test
    @DisplayName("测试根据分类查询")
    void testSelectByCategory() {
        Product p1 = new Product();
        p1.setName("可口可乐");
        p1.setCategory("饮料");
        p1.setPrice(new BigDecimal("3.50"));
        p1.setStatus(1);
        productMapper.insert(p1);

        Product p2 = new Product();
        p2.setName("薯片");
        p2.setCategory("零食");
        p2.setPrice(new BigDecimal("8.00"));
        p2.setStatus(1);
        productMapper.insert(p2);

        Product p3 = new Product();
        p3.setName("百事可乐");
        p3.setCategory("饮料");
        p3.setPrice(new BigDecimal("3.00"));
        p3.setStatus(1);
        productMapper.insert(p3);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategory, "饮料");
        List<Product> drinks = productMapper.selectList(wrapper);
        
        assertEquals(2, drinks.size());
    }

    @Test
    @DisplayName("测试更新商品")
    void testUpdateProduct() {
        Product product = new Product();
        product.setName("旧名称");
        product.setCategory("分类");
        product.setPrice(new BigDecimal("5.00"));
        product.setStatus(1);
        productMapper.insert(product);

        product.setName("新名称");
        product.setPrice(new BigDecimal("6.50"));
        int result = productMapper.updateById(product);
        assertEquals(1, result);

        Product updated = productMapper.selectById(product.getProductId());
        assertEquals("新名称", updated.getName());
        assertEquals(new BigDecimal("6.50"), updated.getPrice());
    }

    @Test
    @DisplayName("测试删除商品")
    void testDeleteProduct() {
        Product product = new Product();
        product.setName("要删除的商品");
        product.setCategory("分类");
        product.setPrice(new BigDecimal("5.00"));
        product.setStatus(1);
        productMapper.insert(product);

        int result = productMapper.deleteById(product.getProductId());
        assertEquals(1, result);

        Product deleted = productMapper.selectById(product.getProductId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试批量删除")
    void testBatchDelete() {
        Long[] ids = new Long[3];
        for (int i = 0; i < 3; i++) {
            Product product = new Product();
            product.setName("商品" + i);
            product.setCategory("分类");
            product.setPrice(new BigDecimal("5.00"));
            product.setStatus(1);
            productMapper.insert(product);
            ids[i] = product.getProductId();
        }

        int result = productMapper.deleteBatchIds(List.of(ids));
        assertEquals(3, result);

        List<Product> remaining = productMapper.selectList(null);
        assertEquals(0, remaining.size());
    }

    @Test
    @DisplayName("测试分页查询")
    void testSelectPage() {
        for (int i = 1; i <= 10; i++) {
            Product product = new Product();
            product.setName("分页商品" + i);
            product.setCategory("分类");
            product.setPrice(new BigDecimal(i + ".00"));
            product.setStatus(1);
            productMapper.insert(product);
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 5);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> result = 
            productMapper.selectPage(page, null);
        
        assertEquals(10, result.getTotal());
        assertEquals(5, result.getRecords().size());
    }
}
