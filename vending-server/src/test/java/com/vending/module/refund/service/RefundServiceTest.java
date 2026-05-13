package com.vending.module.refund.service;

import com.vending.common.exception.BusinessException;
import com.vending.module.inventory.entity.Inventory;
import com.vending.module.inventory.service.InventoryService;
import com.vending.module.order.dto.CreateOrderRequest;
import com.vending.module.order.dto.CreateOrderRequest.OrderItemDTO;
import com.vending.module.order.entity.Order;
import com.vending.module.order.service.OrderService;
import com.vending.module.product.entity.Product;
import com.vending.module.product.service.ProductService;
import com.vending.module.refund.entity.Refund;
import com.vending.module.refund.service.impl.RefundServiceImpl;
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
class RefundServiceTest {

    @Autowired
    private RefundServiceImpl refundService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    private Product testProduct;
    private Inventory testInventory;
    private Order testOrder;

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

        CreateOrderRequest request = createOrderRequest(1L, testProduct.getProductId(), 2);
        var orderVO = orderService.createOrder(1L, request);
        testOrder = orderService.getById(orderVO.getOrderId());
        testOrder.setStatus(1);
        orderService.updateById(testOrder);
    }

    @Test
    @Transactional
    @DisplayName("测试重复审核退款申请时的状态校验")
    void testDuplicateRefundAuditShouldFail() {
        refundService.applyRefund(1L, testOrder.getOrderId(), "Test reason");

        Refund refund = refundService.lambdaQuery()
                .eq(Refund::getOrderId, testOrder.getOrderId())
                .one();
        assertNotNull(refund);
        assertEquals(0, refund.getStatus());

        refundService.auditRefund(refund.getRefundId(), true, "Approved");

        refund = refundService.getById(refund.getRefundId());
        assertEquals(3, refund.getStatus());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.auditRefund(refund.getRefundId(), false, "Trying to reject again");
        });
        assertEquals("退款申请状态异常", exception.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("测试审核不存在的退款申请")
    void testAuditNonExistentRefund() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.auditRefund(99999L, true, "Approved");
        });
        assertEquals("退款申请不存在", exception.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("测试申请退款时验证订单归属")
    void testRefundApplicationValidatesOwnership() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.applyRefund(999L, testOrder.getOrderId(), "Test reason");
        });
        assertEquals("无权操作该订单", exception.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("测试只有已支付订单才能申请退款")
    void testOnlyPaidOrderCanApplyRefund() {
        CreateOrderRequest request = createOrderRequest(1L, testProduct.getProductId(), 1);
        var orderVO = orderService.createOrder(2L, request);
        Order unpaidOrder = orderService.getById(orderVO.getOrderId());
        unpaidOrder.setStatus(0);
        orderService.updateById(unpaidOrder);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.applyRefund(2L, unpaidOrder.getOrderId(), "Test reason");
        });
        assertEquals("只有已支付订单才能申请退款", exception.getMessage());
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