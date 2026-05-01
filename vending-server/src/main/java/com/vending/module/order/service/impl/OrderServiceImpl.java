package com.vending.module.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vending.common.exception.BusinessException;
import com.vending.common.result.ResultCode;
import com.vending.module.inventory.service.InventoryService;
import com.vending.module.order.dto.CreateOrderRequest;
import com.vending.module.order.dto.OrderVO;
import com.vending.module.order.entity.Order;
import com.vending.module.order.entity.OrderItem;
import com.vending.module.order.mapper.OrderItemMapper;
import com.vending.module.order.mapper.OrderMapper;
import com.vending.module.order.service.OrderService;
import com.vending.module.payment.service.PaymentService;
import com.vending.module.pickup.service.PickupCodeService;
import com.vending.module.product.entity.Product;
import com.vending.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final PickupCodeService pickupCodeService;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, CreateOrderRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = request.getItems().stream().map(itemDTO -> {
            Product product = productService.getById(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
            }
            if (product.getStatus() == 0) {
                throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
            }

            if (!inventoryService.checkStock(request.getCabinetId(), itemDTO.getProductId(), itemDTO.getQuantity())) {
                throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            return orderItem;
        }).collect(Collectors.toList());

        totalAmount = orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setCabinetId(request.getCabinetId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        this.save(order);

        orderItems.forEach(item -> item.setOrderId(order.getOrderId()));
        orderItems.forEach(item -> orderItemMapper.insert(item));

        return convertToVO(order, orderItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, String payChannel) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        inventoryService.deductStock(order.getCabinetId(), orderId);

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setPayChannel(payChannel);
        this.updateById(order);

        paymentService.createPaymentRecord(order, payChannel);
        pickupCodeService.generatePickupCode(order);
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    private OrderVO convertToVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setItems(items.stream().map(item -> {
            OrderVO.ItemVO itemVO = new OrderVO.ItemVO();
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUnitPrice(item.getUnitPrice());
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }
}
