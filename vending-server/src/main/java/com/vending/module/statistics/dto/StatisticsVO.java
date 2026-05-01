package com.vending.module.statistics.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsVO {
    private Long totalOrders;
    private Long todayOrders;
    private BigDecimal totalSales;
    private BigDecimal todaySales;
    private BigDecimal avgOrderAmount;
    private List<Map<String, Object>> salesByCategory;
    private List<Map<String, Object>> topProducts;
    private List<Map<String, Object>> salesByCabinet;
}
