package com.vending.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.BaseTest;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.module.cabinet.controller.CabinetController;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.mapper.CabinetMapper;
import com.vending.module.cabinet.service.CabinetService;
import com.vending.module.product.controller.ProductController;
import com.vending.module.product.entity.Product;
import com.vending.module.product.mapper.ProductMapper;
import com.vending.module.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("Redis缓存一致性测试")
class RedisCacheConsistencyTest extends BaseTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CabinetMapper cabinetMapper;

    @Autowired
    private ProductController productController;

    @Autowired
    private CabinetController cabinetController;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        productMapper.delete(new LambdaQueryWrapper<>());
        cabinetMapper.delete(new LambdaQueryWrapper<>());
        clearAllCache();
    }

    private void clearAllCache() {
        Set<String> keys = stringRedisTemplate.keys("*");
        if (keys != null) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("测试1: 商品列表缓存一致性 - 添加商品后缓存失效")
    @Transactional
    @Rollback
    void testProductListCacheConsistencyOnAdd() {
        log.info("=== 开始测试商品列表缓存一致性 ===");

        Product product1 = new Product();
        product1.setName("商品1");
        product1.setCategory("饮料");
        product1.setPrice(new BigDecimal("3.50"));
        product1.setStatus(1);
        productService.save(product1);

        var result1 = productController.list(1, 10, null);
        assertTrue(result1.getData().getRecords().size() >= 1);

        String cacheKey1 = RedisCacheUtil.KEY_PRODUCT_LIST + "1:10:all";
        String cached1 = stringRedisTemplate.opsForValue().get(cacheKey1);
        assertNotNull(cached1, "第一次查询后缓存应该存在");

        Product product2 = new Product();
        product2.setName("商品2");
        product2.setCategory("饮料");
        product2.setPrice(new BigDecimal("5.00"));
        product2.setStatus(1);
        productController.save(product2);

        String cached2 = stringRedisTemplate.opsForValue().get(cacheKey1);
        assertNull(cached2, "添加商品后缓存应该失效");

        var result2 = productController.list(1, 10, null);
        assertTrue(result2.getData().getRecords().size() >= 2);

        String cached3 = stringRedisTemplate.opsForValue().get(cacheKey1);
        assertNotNull(cached3, "再次查询后缓存应该重新生成");

        log.info("✅ 商品列表缓存一致性测试通过");
    }

    @Test
    @DisplayName("测试2: 商品列表缓存一致性 - 更新商品后缓存失效")
    @Transactional
    @Rollback
    void testProductListCacheConsistencyOnUpdate() {
        log.info("=== 开始测试更新商品缓存失效 ===");

        Product product = new Product();
        product.setName("原商品");
        product.setCategory("饮料");
        product.setPrice(new BigDecimal("3.50"));
        product.setStatus(1);
        productService.save(product);

        productController.list(1, 10, null);

        String cacheKey = RedisCacheUtil.KEY_PRODUCT_LIST + "1:10:all";
        String cached1 = stringRedisTemplate.opsForValue().get(cacheKey);
        assertNotNull(cached1);

        product.setName("更新后的商品");
        productController.save(product);

        String cached2 = stringRedisTemplate.opsForValue().get(cacheKey);
        assertNull(cached2, "更新商品后缓存应该失效");

        log.info("✅ 更新商品缓存一致性测试通过");
    }

    @Test
    @DisplayName("测试3: 商品列表缓存一致性 - 删除商品后缓存失效")
    @Transactional
    @Rollback
    void testProductListCacheConsistencyOnDelete() {
        log.info("=== 开始测试删除商品缓存失效 ===");

        Product product = new Product();
        product.setName("待删除商品");
        product.setCategory("饮料");
        product.setPrice(new BigDecimal("3.50"));
        product.setStatus(1);
        productService.save(product);

        productController.list(1, 10, null);

        String cacheKey = RedisCacheUtil.KEY_PRODUCT_LIST + "1:10:all";
        String cached1 = stringRedisTemplate.opsForValue().get(cacheKey);
        assertNotNull(cached1);

        productController.delete(product.getProductId());

        String cached2 = stringRedisTemplate.opsForValue().get(cacheKey);
        assertNull(cached2, "删除商品后缓存应该失效");

        log.info("✅ 删除商品缓存一致性测试通过");
    }

    @Test
    @DisplayName("测试4: 货柜列表缓存一致性 - 操作后缓存失效")
    @Transactional
    @Rollback
    void testCabinetListCacheConsistency() {
        log.info("=== 开始测试货柜列表缓存一致性 ===");

        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-CACHE-001");
        cabinet.setName("测试货柜");
        cabinet.setCity("北京");
        cabinet.setCapacity(50);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);

        cabinetController.list(1, 10, null);

        String cacheKey = RedisCacheUtil.KEY_CABINET_LIST + "1:10:all";
        String cached1 = stringRedisTemplate.opsForValue().get(cacheKey);
        assertNotNull(cached1);

        cabinet.setName("更新后的货柜");
        cabinetController.save(cabinet);

        String cached2 = stringRedisTemplate.opsForValue().get(cacheKey);
        assertNull(cached2, "更新货柜后缓存应该失效");

        log.info("✅ 货柜列表缓存一致性测试通过");
    }

    @Test
    @DisplayName("测试5: 缓存过期时间设置")
    @Transactional
    @Rollback
    void testCacheExpiration() throws InterruptedException {
        log.info("=== 开始测试缓存过期时间 ===");

        Product product = new Product();
        product.setName("测试过期商品");
        product.setCategory("饮料");
        product.setPrice(new BigDecimal("3.50"));
        product.setStatus(1);
        productService.save(product);

        productController.list(1, 10, null);

        String cacheKey = RedisCacheUtil.KEY_PRODUCT_LIST + "1:10:all";
        Long ttl = stringRedisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
        
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= 1800, "缓存过期时间应该在0-1800秒之间");

        log.info("✅ 缓存过期时间测试通过，TTL: {}秒", ttl);
    }

    @Test
    @DisplayName("测试6: 分类查询的缓存独立")
    @Transactional
    @Rollback
    void testCategoryCacheSeparation() {
        log.info("=== 开始测试分类查询缓存独立性 ===");

        Product drink = new Product();
        drink.setName("可乐");
        drink.setCategory("饮料");
        drink.setPrice(new BigDecimal("3.50"));
        drink.setStatus(1);
        productService.save(drink);

        Product snack = new Product();
        snack.setName("薯片");
        snack.setCategory("零食");
        snack.setPrice(new BigDecimal("8.00"));
        snack.setStatus(1);
        productService.save(snack);

        productController.list(1, 10, "饮料");
        productController.list(1, 10, "零食");
        productController.list(1, 10, null);

        String drinkCache = RedisCacheUtil.KEY_PRODUCT_LIST + "1:10:饮料";
        String snackCache = RedisCacheUtil.KEY_PRODUCT_LIST + "1:10:零食";
        String allCache = RedisCacheUtil.KEY_PRODUCT_LIST + "1:10:all";

        assertNotNull(stringRedisTemplate.opsForValue().get(drinkCache));
        assertNotNull(stringRedisTemplate.opsForValue().get(snackCache));
        assertNotNull(stringRedisTemplate.opsForValue().get(allCache));

        Product newDrink = new Product();
        newDrink.setName("雪碧");
        newDrink.setCategory("饮料");
        newDrink.setPrice(new BigDecimal("3.00"));
        newDrink.setStatus(1);
        productController.save(newDrink);

        assertNull(stringRedisTemplate.opsForValue().get(drinkCache));
        assertNull(stringRedisTemplate.opsForValue().get(snackCache));
        assertNull(stringRedisTemplate.opsForValue().get(allCache));

        log.info("✅ 分类查询缓存独立性测试通过");
    }
}
