package com.vending.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.BaseTest;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.common.exception.BusinessException;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.mapper.CabinetMapper;
import com.vending.module.cabinet.service.CabinetService;
import com.vending.module.inventory.entity.Inventory;
import com.vending.module.inventory.mapper.InventoryMapper;
import com.vending.module.inventory.service.InventoryService;
import com.vending.module.order.dto.CreateOrderRequest;
import com.vending.module.order.dto.OrderVO;
import com.vending.module.order.entity.Order;
import com.vending.module.order.entity.OrderItem;
import com.vending.module.order.mapper.OrderItemMapper;
import com.vending.module.order.mapper.OrderMapper;
import com.vending.module.order.service.OrderService;
import com.vending.module.payment.entity.PaymentRecord;
import com.vending.module.payment.mapper.PaymentRecordMapper;
import com.vending.module.pickup.entity.PickupCode;
import com.vending.module.pickup.mapper.PickupCodeMapper;
import com.vending.module.product.entity.Product;
import com.vending.module.product.mapper.ProductMapper;
import com.vending.module.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("数据库事务一致性测试")
class TransactionConsistencyTest extends BaseTest {

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
    private RedisCacheUtil redisCacheUtil;

    private Long testCabinetId;
    private Long testProductId;
    private Long testUserId = 10001L;
    private Integer initialStock = 100;

    @BeforeEach
    void setUp() {
        orderItemMapper.delete(new LambdaQueryWrapper<>());
        orderMapper.delete(new LambdaQueryWrapper<>());
        inventoryMapper.delete(new LambdaQueryWrapper<>());
        productMapper.delete(new LambdaQueryWrapper<>());
        cabinetMapper.delete(new LambdaQueryWrapper<>());
        paymentRecordMapper.delete(new LambdaQueryWrapper<>());
        pickupCodeMapper.delete(new LambdaQueryWrapper<>());

        Product product = new Product();
        product.setName("测试商品");
        product.setCategory("饮料");
        product.setPrice(new BigDecimal("5.00"));
        product.setStatus(1);
        productService.save(product);
        testProductId = product.getProductId();

        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-TRANS-001");
        cabinet.setName("事务测试货柜");
        cabinet.setCity("上海");
        cabinet.setCapacity(100);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);
        testCabinetId = cabinet.getCabinetId();

        Inventory inventory = new Inventory();
        inventory.setCabinetId(testCabinetId);
        inventory.setProductId(testProductId);
        inventory.setQuantity(initialStock);
        inventoryMapper.insert(inventory);
    }

    @Test
    @DisplayName("测试1: 事务原子性 - 部分失败时全部回滚")
    @Transactional
    @Rollback
    void testTransactionAtomicity() {
        log.info("=== 开始测试事务原子性 ===");

        long orderCountBefore = orderMapper.selectCount(null);
        long orderItemCountBefore = orderItemMapper.selectCount(null);
        long paymentCountBefore = paymentRecordMapper.selectCount(null);
        long pickupCodeCountBefore = pickupCodeMapper.selectCount(null);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCabinetId(testCabinetId);
        CreateOrderRequest.ItemDTO item = new CreateOrderRequest.ItemDTO();
        item.setProductId(testProductId);
        item.setQuantity(5);
        request.setItems(Collections.singletonList(item));

        OrderVO orderVO = orderService.createOrder(testUserId, request);

        Inventory inventoryBefore = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getCabinetId, testCabinetId)
                        .eq(Inventory::getProductId, testProductId));
        Integer stockBeforePay = inventoryBefore.getQuantity();

        assertThrows(BusinessException.class, () -> {
            orderService.payOrder(orderVO.getOrderId(), "INVALID_CHANNEL");
        });

        Inventory inventoryAfter = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getCabinetId, testCabinetId)
                        .eq(Inventory::getProductId, testProductId));
        assertEquals(stockBeforePay, inventoryAfter.getQuantity());

        Order orderAfter = orderMapper.selectById(orderVO.getOrderId());
        assertNotNull(orderAfter);
        assertEquals(0, orderAfter.getStatus());

        long paymentCountAfter = paymentRecordMapper.selectCount(null);
        assertEquals(paymentCountBefore, paymentCountAfter);

        long pickupCodeCountAfter = pickupCodeMapper.selectCount(null);
        assertEquals(pickupCodeCountBefore, pickupCodeCountAfter);

        log.info("✅ 事务原子性测试通过");
    }

    @Test
    @DisplayName("测试2: 库存并发扣减 - 验证超卖问题")
    @Transactional
    @Rollback
    void testConcurrentStockDeduction() throws InterruptedException {
        log.info("=== 开始测试库存并发扣减 ===");

        int threadCount = 20;
        int quantityPerThread = 5;
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
                    CreateOrderRequest.ItemDTO item = new CreateOrderRequest.ItemDTO();
                    item.setProductId(testProductId);
                    item.setQuantity(quantityPerThread);
                    request.setItems(Collections.singletonList(item));

                    OrderVO orderVO = orderService.createOrder(testUserId + threadId, request);
                    orderService.payOrder(orderVO.getOrderId(), "WECHAT");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.warn("线程 {} 库存扣减失败: {}", threadId, e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        log.info("成功: {}, 失败: {}", successCount.get(), failCount.get());

        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getCabinetId, testCabinetId)
                        .eq(Inventory::getProductId, testProductId));

        int remainingStock = inventory.getQuantity();
        int totalSold = initialStock - remainingStock;
        int expectedMaxSold = successCount.get() * quantityPerThread;

        assertTrue(remainingStock >= 0, "库存不能为负数");
        assertTrue(totalSold <= initialStock, "总销量不能超过初始库存");

        log.info("✅ 库存并发扣减测试通过，剩余库存: {}", remainingStock);
    }

    @Test
    @DisplayName("测试3: 数据一致性 - 订单与订单项数据匹配")
    @Transactional
    @Rollback
    void testDataConsistency() {
        log.info("=== 开始测试数据一致性 ===");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCabinetId(testCabinetId);

        CreateOrderRequest.ItemDTO item1 = new CreateOrderRequest.ItemDTO();
        item1.setProductId(testProductId);
        item1.setQuantity(3);

        CreateOrderRequest.ItemDTO item2 = new CreateOrderRequest.ItemDTO();
        item2.setProductId(testProductId);
        item2.setQuantity(2);

        request.setItems(java.util.Arrays.asList(item1, item2));

        OrderVO orderVO = orderService.createOrder(testUserId, request);
        orderService.payOrder(orderVO.getOrderId(), "WECHAT");

        Order order = orderMapper.selectById(orderVO.getOrderId());
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderVO.getOrderId()));

        BigDecimal calculatedTotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(calculatedTotal, order.getTotalAmount());

        int totalQuantity = items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        assertEquals(5, totalQuantity);

        log.info("✅ 数据一致性测试通过");
    }
}
