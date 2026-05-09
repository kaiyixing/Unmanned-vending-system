package com.vending.module.product.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.BaseTest;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.module.product.entity.Product;
import com.vending.module.product.mapper.ProductMapper;
import com.vending.module.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@DisplayName("ProductController 商品控制器测试")
class ProductControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @BeforeEach
    void setUp() {
        productMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    @DisplayName("测试获取商品列表")
    void testGetProductList() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Product product = new Product();
            product.setName("列表商品" + i);
            product.setCategory(i % 2 == 0 ? "饮料" : "零食");
            product.setPrice(new BigDecimal(i + ".50"));
            product.setStatus(1);
            productService.save(product);
        }

        MvcResult result = mockMvc.perform(get("/api/product/list")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("商品列表响应: {}", content);
        assertNotNull(content);
        assertTrue(content.contains("列表商品1"));
    }

    @Test
    @DisplayName("测试根据分类获取商品列表")
    void testGetProductListByCategory() throws Exception {
        Product p1 = new Product();
        p1.setName("可口可乐");
        p1.setCategory("饮料");
        p1.setPrice(new BigDecimal("3.50"));
        p1.setStatus(1);
        productService.save(p1);

        Product p2 = new Product();
        p2.setName("薯片");
        p2.setCategory("零食");
        p2.setPrice(new BigDecimal("8.00"));
        p2.setStatus(1);
        productService.save(p2);

        MvcResult result = mockMvc.perform(get("/api/product/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("category", "饮料")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("按分类查询响应: {}", content);
        assertNotNull(content);
        assertTrue(content.contains("可口可乐"));
        assertFalse(content.contains("薯片"));
    }

    @Test
    @DisplayName("测试根据ID获取商品")
    void testGetProductById() throws Exception {
        Product product = new Product();
        product.setName("单个查询商品");
        product.setCategory("分类");
        product.setPrice(new BigDecimal("10.00"));
        product.setStatus(1);
        productService.save(product);

        MvcResult result = mockMvc.perform(get("/api/product/{id}", product.getProductId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("单个商品查询响应: {}", content);
        assertNotNull(content);
        assertTrue(content.contains("单个查询商品"));
    }

    @Test
    @DisplayName("测试添加商品")
    void testSaveProduct() throws Exception {
        Product product = new Product();
        product.setName("新添加商品");
        product.setCategory("饮料");
        product.setPrice(new BigDecimal("5.00"));
        product.setStatus(1);

        MvcResult result = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(product)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("添加商品响应: {}", content);
        assertNotNull(content);

        long count = productService.count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("测试更新商品")
    void testUpdateProduct() throws Exception {
        Product product = new Product();
        product.setName("旧商品");
        product.setCategory("旧分类");
        product.setPrice(new BigDecimal("5.00"));
        product.setStatus(1);
        productService.save(product);

        product.setName("新商品");
        product.setPrice(new BigDecimal("6.00"));

        MvcResult result = mockMvc.perform(put("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(product)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("更新商品响应: {}", content);
        assertNotNull(content);

        Product updated = productService.getById(product.getProductId());
        assertEquals("新商品", updated.getName());
        assertEquals(new BigDecimal("6.00"), updated.getPrice());
    }

    @Test
    @DisplayName("测试删除商品")
    void testDeleteProduct() throws Exception {
        Product product = new Product();
        product.setName("要删除的商品");
        product.setCategory("分类");
        product.setPrice(new BigDecimal("5.00"));
        product.setStatus(1);
        productService.save(product);

        MvcResult result = mockMvc.perform(delete("/api/product/{id}", product.getProductId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("删除商品响应: {}", content);
        assertNotNull(content);

        Product deleted = productService.getById(product.getProductId());
        assertNull(deleted);
    }
}
