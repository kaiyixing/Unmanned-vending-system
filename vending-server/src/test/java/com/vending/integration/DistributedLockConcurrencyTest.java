package com.vending.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.BaseTest;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.mapper.CabinetMapper;
import com.vending.module.cabinet.service.CabinetService;
import com.vending.module.inventory.entity.Inventory;
import com.vending.module.inventory.mapper.InventoryMapper;
import com.vending.module.inventory.service.InventoryService;
import com.vending.module.order.dto.CreateOrderRequest;
import com.vending.module.order.dto.OrderVO;
import com.vending.module.order.mapper.OrderItemMapper;
import com.vending.module.order.mapper.OrderMapper;
import com.vending.module.order.service.OrderService;
import com.vending.module.payment.mapper.PaymentRecordMapper;
import com.vending.module.pickup.mapper.PickupCodeMapper;
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
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("分布式锁并发安全测试")
class DistributedLockConcurrencyTest extends BaseTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CabinetMapper cabinetMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private PickupCodeMapper pickupCodeMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Long testCabinetId;
    private Long testProductId;
    private Long testUserId = 20000L;

    @BeforeEach
    void setUp() {
        orderItemMapper.delete(new LambdaQueryWrapper<>());
        orderMapper.delete(new LambdaQueryWrapper<>());
        inventoryMapper.delete(new LambdaQueryWrapper<>());
        productMapper.delete(new LambdaQueryWrapper<>());
        cabinetMapper.delete(new LambdaQueryWrapper<>());
        paymentRecordMapper.delete(new LambdaQueryWrapper<>());
        pickupCodeMapper.delete(new LambdaQueryWrapper<>());

        clearAllCache();

        Product product = new Product();
        product.setName("分布式锁测试商品");
        product.setCategory("饮料");
        product.setPrice(new BigDecimal("10.00"));
        product.setStatus(1);
        productService.save(product);
        testProductId = product.getProductId();

        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-LOCK-001");
        cabinet.setName("分布式锁测试货柜");
        cabinet.setCity("深圳");
        cabinet.setCapacity(200);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);
        testCabinetId = cabinet.getCabinetId();
    }

    private void clearAllCache() {
        var keys = stringRedisTemplate.keys("*");
        if (keys != null) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("测试1: 分布式锁基本功能 - 获取和释放")
    void testDistributedLockBasic() throws InterruptedException {
        log.info("=== 开始测试分布式锁基本功能 ===");

        String lockKey = "inventory:lock:" + testCabinetId + ":" + testProductId;

        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        assertTrue(acquired, "第一次获取锁应该成功");

        Boolean acquiredAgain = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        assertFalse(acquiredAgain, "锁被占用时应该获取失败");

        stringRedisTemplate.delete(lockKey);

        Boolean acquiredAfterRelease = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        assertTrue(acquiredAfterRelease, "释放后应该能重新获取锁");

        stringRedisTemplate.delete(lockKey);

        log.info("✅ 分布式锁基本功能测试通过");
    }

    @Test
    @DisplayName("测试2: 分布式锁过期时间 - 自动过期释放")
    void testDistributedLockExpiration() throws InterruptedException {
        log.info("=== 开始测试分布式锁过期时间 ===");

        String lockKey = "inventory:lock:expire:test";

        stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 2, TimeUnit.SECONDS);

        Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= 2, "过期时间应该在0-2秒之间");

        Thread.sleep(2500);

        String value = stringRedisTemplate.opsForValue().get(lockKey);
        assertNull(value, "2.5秒后锁应该过期自动释放");

        log.info("✅ 分布式锁过期时间测试通过");
    }

    @Test
    @DisplayName("测试3: 高并发库存扣减 - 验证分布式锁防超卖")
    @Transactional
    @Rollback
    void testHighConcurrencyStockDeduction() throws InterruptedException {
        log.info("=== 开始测试高并发库存扣减 ===");

        int initialStock = 50;
        Inventory inventory = new Inventory();
        inventory.setCabinetId(testCabinetId);
        inventory.setProductId(testProductId);
        inventory.setQuantity(initialStock);
        inventoryMapper.insert(inventory);

        int threadCount = 30;
        int quantityPerThread = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    CreateOrderRequest request = new CreateOrderRequest();
        request.setCabinetId(testCabinetId);
        CreateOrderRequest.OrderItemDTO item = new CreateOrderRequest.OrderItemDTO();
                    item.setProductId(testProductId);
                    item.setQuantity(quantityPerThread);
                    request.setItems(Collections.singletonList(item));

                    OrderVO orderVO = orderService.createOrder(testUserId + threadId, request);
                    orderService.payOrder(orderVO.getOrderId(), "WECHAT");
                    successCount.incrementAndGet();
                    log.info("线程 {} 订单支付成功", threadId);
                } catch (Exception e) {
                    log.warn("线程 {} 订单失败: {}", threadId, e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await();
        long endTime = System.currentTimeMillis();

        executor.shutdown();

        log.info("测试完成! 成功: {}, 失败: {}, 耗时: {}ms",
                successCount.get(), failCount.get(), endTime - startTime);

        Inventory finalInventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getCabinetId, testCabinetId)
                        .eq(Inventory::getProductId, testProductId));

        int remainingStock = finalInventory.getQuantity();
        int totalSold = initialStock - remainingStock;
        int expectedMaxSold = successCount.get() * quantityPerThread;

        log.info("初始库存: {}, 剩余库存: {}, 已售: {}, 期望已售: {}",
                initialStock, remainingStock, totalSold, expectedMaxSold);

        assertTrue(remainingStock >= 0, "库存不能为负数 - 分布式锁防超卖成功");
        assertTrue(totalSold <= initialStock, "总销量不能超过初始库存");

        int maxPossible = initialStock / quantityPerThread * quantityPerThread;
        assertTrue(totalSold <= maxPossible, "实际销量应该不超过理论最大销量");

        log.info("✅ 高并发库存扣减测试通过，分布式锁防超卖验证成功");
    }

    @Test
    @DisplayName("测试4: 不同商品的锁独立 - 不互相干扰")
    @Transactional
    @Rollback
    void testLockIndependenceForDifferentProducts() throws InterruptedException {
        log.info("=== 开始测试不同商品锁独立性 ===");

        Product product2 = new Product();
        product2.setName("商品2");
        product2.setCategory("零食");
        product2.setPrice(new BigDecimal("5.00"));
        product2.setStatus(1);
        productService.save(product2);

        Inventory inventory1 = new Inventory();
        inventory1.setCabinetId(testCabinetId);
        inventory1.setProductId(testProductId);
        inventory1.setQuantity(100);
        inventoryMapper.insert(inventory1);

        Inventory inventory2 = new Inventory();
        inventory2.setCabinetId(testCabinetId);
        inventory2.setProductId(product2.getProductId());
        inventory2.setQuantity(100);
        inventoryMapper.insert(inventory2);

        String lockKey1 = "inventory:lock:" + testCabinetId + ":" + testProductId;
        String lockKey2 = "inventory:lock:" + testCabinetId + ":" + product2.getProductId();

        Boolean lock1Acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey1, "1", 10, TimeUnit.SECONDS);
        assertTrue(lock1Acquired);

        Boolean lock2Acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey2, "1", 10, TimeUnit.SECONDS);
        assertTrue(lock2Acquired, "不同商品的锁应该能同时获取");

        stringRedisTemplate.delete(lockKey1);
        stringRedisTemplate.delete(lockKey2);

        log.info("✅ 不同商品锁独立性测试通过");
    }

    @Test
    @DisplayName("测试5: 锁释放保证 - finally块中的释放")
    void testLockReleaseInFinally() throws InterruptedException {
        log.info("=== 开始测试锁释放保证 ===");

        String lockKey = "inventory:lock:finally:test";

        try {
            stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
            throw new RuntimeException("模拟异常");
        } catch (Exception e) {
            log.info("捕获到异常: {}", e.getMessage());
        } finally {
            stringRedisTemplate.delete(lockKey);
        }

        Boolean canAcquire = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        assertTrue(canAcquire, "finally块应该释放锁");

        stringRedisTemplate.delete(lockKey);

        log.info("✅ 锁释放保证测试通过");
    }
}
