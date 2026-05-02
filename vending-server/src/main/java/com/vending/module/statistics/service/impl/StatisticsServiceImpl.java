package com.vending.module.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vending.module.order.entity.Order;
import com.vending.module.order.mapper.OrderMapper;
import com.vending.module.order.service.OrderService;
import com.vending.module.product.entity.Product;
import com.vending.module.product.mapper.ProductMapper;
import com.vending.module.statistics.dto.StatisticsVO;
import com.vending.module.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    public StatisticsVO getOverviewStatistics() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return getStatisticsByDateRange(todayStart, LocalDateTime.now());
    }

    @Override
    public StatisticsVO getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        StatisticsVO vo = new StatisticsVO();

        List<Order> orders = orderService.lambdaQuery()
                .between(Order::getCreateTime, startDate, endDate)
                .in(Order::getStatus, Arrays.asList(1, 2))
                .list();

        vo.setTotalOrders((long) orders.size());
        vo.setTodayOrders((long) orders.size());

        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalSales(totalSales);
        vo.setTodaySales(totalSales);

        if (!orders.isEmpty()) {
            vo.setAvgOrderAmount(totalSales.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP));
        } else {
            vo.setAvgOrderAmount(BigDecimal.ZERO);
        }

        vo.setTopProducts(getTopProducts());
        vo.setSalesByCabinet(getSalesByCabinet(orders));
        vo.setSalesByCategory(getSalesByCategory(orders));

        return vo;
    }

    private List<Map<String, Object>> getTopProducts() {
        List<Map<String, Object>> topProducts = new ArrayList<>();
        return topProducts;
    }

    private List<Map<String, Object>> getSalesByCabinet(List<Order> orders) {
        Map<Long, List<Order>> ordersByCabinet = orders.stream()
                .collect(Collectors.groupingBy(Order::getCabinetId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<Order>> entry : ordersByCabinet.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("cabinetId", entry.getKey());
            item.put("orderCount", entry.getValue().size());
            BigDecimal sales = entry.getValue().stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.put("totalSales", sales);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> getSalesByCategory(List<Order> orders) {
        List<Map<String, Object>> result = new ArrayList<>();
        return result;
    }
}
