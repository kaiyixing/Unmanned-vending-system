package com.vending.module.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderVO {
    private Long orderId;
    private String orderNo;
    private BigDecimal totalAmount;
    private Integer status;
    private List<ItemVO> items;

    @Data
    public static class ItemVO {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
