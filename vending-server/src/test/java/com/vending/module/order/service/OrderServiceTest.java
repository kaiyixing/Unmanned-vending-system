package com.vending.module.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.common.exception.BusinessException;
import com.vending.module.inventory.entity.Inventory;
import com.vending.module.inventory.service.InventoryService;
import com.vending.module.order.dto.CreateOrderRequest;
import com.vending.module.order.dto.CreateOrderRequest.OrderItemDTO;
import com.vending.module.order.dto.OrderVO;
import com.vending.module.order.entity.Order;
import com.vending.module.order.entity.OrderItem;
import com.vending.module.order.mapper.OrderItemMapper;
import com.vending.module.order.service.impl.OrderServiceImpl;
import com.vending.module.product.entity.Product;
import com.vending.module.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderServiceTest {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    private Product testProduct;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("10.00"));
        testProduct.setStatus(1);
        productService.save(testProduct);

        testInventory = new Inventory();
        testInventory.setCabinetId(1L);
        testInventory.setProductId(testProduct.getProductId());
        testInventory.setQuantity(10);
        testInventory.setThreshold(2);
        inventoryService.save(testInventory);
    }

    @Test
    @Transactional
    @DisplayName("测试创建订单时库存扣减的原子性 - 防止超卖")
    void testCreateOrderWithStockDeductionAtomicity() {
        int initialStock = testInventory.getQuantity();
        Long cabinetId = testInventory.getCabinetId();
        Long productId = testProduct.getProductId();

        CreateOrderRequest request1 = createOrderRequest(cabinetId, productId, 5);
        CreateOrderRequest request2 = createOrderRequest(cabinetId, productId, 5);
        CreateOrderRequest request3 = createOrderRequest(cabinetId, productId, 1);

        OrderVO order1 = orderService.createOrder(1L, request1);
        OrderVO order2 = orderService.createOrder(2L, request2);

        assertEquals(0, inventoryService.getById(testInventory.getInventoryId()).getQuantity());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(3L, request3);
        });
        assertEquals("库存不足", exception.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("测试订单取消时库存回滚")
    void testOrderCancellationRollsBackStock() {
        CreateOrderRequest request = createOrderRequest(testInventory.getCabinetId(), testProduct.getProductId(), 3);
        OrderVO order = orderService.createOrder(1L, request);

        int stockAfterOrder = inventoryService.getById(testInventory.getInventoryId()).getQuantity();
        assertEquals(7, stockAfterOrder);

        Order orderEntity = orderService.getById(order.getOrderId());
        orderService.lambdaUpdate()
                .eq(Order::getOrderId, order.getOrderId())
                .set(Order::getStatus, 3)
                .update();

        inventoryService.rollbackStock(testInventory.getCabinetId(), order.getOrderId());

        int stockAfterRollback = inventoryService.getById(testInventory.getInventoryId()).getQuantity();
        assertEquals(10, stockAfterRollback);
    }

    @Test
    @Transactional
    @DisplayName("测试库存扣减后订单创建失败时的回滚")
    void testStockRollbackOnOrderCreationFailure() {
        CreateOrderRequest request = createOrderRequest(testInventory.getCabinetId(), testProduct.getProductId(), 5);

        OrderVO order = orderService.createOrder(1L, request);
        int stockAfterOrder = inventoryService.getById(testInventory.getInventoryId()).getQuantity();
        assertEquals(5, stockAfterOrder);

        orderService.removeById(order.getOrderId());
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getOrderId()));
        items.forEach(item -> orderItemMapper.deleteById(item.getOrderItemId()));

        inventoryService.rollbackStock(testInventory.getCabinetId(), order.getOrderId());

        int stockAfterRollback = inventoryService.getById(testInventory.getInventoryId()).getQuantity();
        assertEquals(10, stockAfterRollback);
    }

    private CreateOrderRequest createOrderRequest(Long cabinetId, Long productId, Integer quantity) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCabinetId(cabinetId);

        List<OrderItemDTO> items = new ArrayList<>();
        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);
        items.add(item);

        request.setItems(items);
        return request;
    }
}