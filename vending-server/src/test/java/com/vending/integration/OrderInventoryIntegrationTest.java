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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("订单-库存集成测试")
class OrderInventoryIntegrationTest extends BaseTest {

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
    private Integer initialStock = 10;

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
        product.setName("可乐");
        product.setCategory("饮料");
        product.setPrice(new BigDecimal("3.50"));
        product.setCostPrice(new BigDecimal("1.50"));
        product.setStatus(1);
        productService.save(product);
        testProductId = product.getProductId();

        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-TEST-001");
        cabinet.setName("测试货柜");
        cabinet.setCity("北京");
        cabinet.setCapacity(50);
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
    @DisplayName("测试1: 完整的订单创建流程 - 模块间接口调用")
    @Transactional
    @Rollback
    void testCompleteOrderCreationFlow() {
        log.info("=== 开始测试完整订单创建流程 ===");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCabinetId(testCabinetId);
        CreateOrderRequest.OrderItemDTO item = new CreateOrderRequest.OrderItemDTO();
        item.setProductId(testProductId);
        item.setQuantity(2);
        request.setItems(Collections.singletonList(item));

        OrderVO orderVO = orderService.createOrder(testUserId, request);

        assertNotNull(orderVO);
        assertNotNull(orderVO.getOrderId());
        assertNotNull(orderVO.getOrderNo());
        assertEquals(0, orderVO.getStatus());
        assertEquals(new BigDecimal("7.00"), orderVO.getTotalAmount());
        assertEquals(1, orderVO.getItems().size());
        assertEquals("可乐", orderVO.getItems().get(0).getProductName());
        assertEquals(2, orderVO.getItems().get(0).getQuantity());

        Order savedOrder = orderMapper.selectById(orderVO.getOrderId());
        assertNotNull(savedOrder);
        assertEquals(testUserId, savedOrder.getUserId());
        assertEquals(testCabinetId, savedOrder.getCabinetId());

        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderVO.getOrderId()));
        assertEquals(1, orderItems.size());
        assertEquals(testProductId, orderItems.get(0).getProductId());

        log.info("✅ 完整订单创建流程测试通过");
    }

    @Test
    @DisplayName("测试2: 订单支付流程 - 库存扣减+支付记录+取货码生成")
    @Transactional
    @Rollback
    void testOrderPaymentFlow() {
        log.info("=== 开始测试订单支付流程 ===");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCabinetId(testCabinetId);
        CreateOrderRequest.OrderItemDTO item = new CreateOrderRequest.OrderItemDTO();
        item.setProductId(testProductId);
        item.setQuantity(3);
        request.setItems(Collections.singletonList(item));

        OrderVO orderVO = orderService.createOrder(testUserId, request);

        orderService.payOrder(orderVO.getOrderId(), "WECHAT");

        Order updatedOrder = orderMapper.selectById(orderVO.getOrderId());
        assertEquals(1, updatedOrder.getStatus());
        assertNotNull(updatedOrder.getPayTime());
        assertEquals("WECHAT", updatedOrder.getPayChannel());

        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getCabinetId, testCabinetId)
                        .eq(Inventory::getProductId, testProductId));
        assertNotNull(inventory);
        assertEquals(initialStock - 3, inventory.getQuantity());

        List<PaymentRecord> payments = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getOrderId, orderVO.getOrderId()));
        assertEquals(1, payments.size());

        List<PickupCode> pickupCodes = pickupCodeMapper.selectList(
                new LambdaQueryWrapper<PickupCode>().eq(PickupCode::getOrderId, orderVO.getOrderId()));
        assertEquals(1, pickupCodes.size());
        assertNotNull(pickupCodes.get(0).getCodeValue());

        log.info("✅ 订单支付流程测试通过");
    }

    @Test
    @DisplayName("测试3: 库存不足时的异常处理")
    @Transactional
    @Rollback
    void testInsufficientStockHandling() {
        log.info("=== 开始测试库存不足异常处理 ===");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCabinetId(testCabinetId);
        CreateOrderRequest.OrderItemDTO item = new CreateOrderRequest.OrderItemDTO();
        item.setProductId(testProductId);
        item.setQuantity(initialStock + 1);
        request.setItems(Collections.singletonList(item));

        OrderVO orderVO = orderService.createOrder(testUserId, request);

        assertThrows(Exception.class, () -> {
            orderService.payOrder(orderVO.getOrderId(), "WECHAT");
        });

        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getCabinetId, testCabinetId)
                        .eq(Inventory::getProductId, testProductId));
        assertNotNull(inventory);
        assertEquals(initialStock, inventory.getQuantity());

        log.info("✅ 库存不足异常处理测试通过");
    }

    @Test
    @DisplayName("测试4: 商品下架时的异常处理")
    @Transactional
    @Rollback
    void testProductOffShelfHandling() {
        log.info("=== 开始测试商品下架异常处理 ===");

        Product product = productService.getById(testProductId);
        product.setStatus(0);
        productService.updateById(product);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCabinetId(testCabinetId);
        CreateOrderRequest.OrderItemDTO item = new CreateOrderRequest.OrderItemDTO();
        item.setProductId(testProductId);
        item.setQuantity(1);
        request.setItems(Collections.singletonList(item));

        assertThrows(Exception.class, () -> {
            orderService.createOrder(testUserId, request);
        });

        log.info("✅ 商品下架异常处理测试通过");
    }
}
