package com.vending.module.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.mapper.CabinetMapper;
import com.vending.module.order.entity.Order;
import com.vending.module.order.entity.OrderItem;
import com.vending.module.order.mapper.OrderItemMapper;
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
    private final CabinetMapper cabinetMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public StatisticsVO getOverviewStatistics() {
        StatisticsVO vo = new StatisticsVO();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 1. 查询今日订单（00:00:00 - 现在）
        List<Order> todayOrders = orderService.lambdaQuery()
                .between(Order::getCreateTime, todayStart, LocalDateTime.now())
                .in(Order::getStatus, Arrays.asList(1, 2))
                .list();

        // 2. 查询所有历史订单（不限制时间）
        List<Order> allOrders = orderService.lambdaQuery()
                .in(Order::getStatus, Arrays.asList(1, 2))
                .list();

        // 3. 设置今日统计
        vo.setTodayOrders((long) todayOrders.size());
        BigDecimal todaySales = todayOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodaySales(todaySales);

        // 4. 设置总统计
        vo.setTotalOrders((long) allOrders.size());
        BigDecimal totalSales = allOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalSales(totalSales);

        // 5. 计算平均订单金额（基于所有订单）
        if (!allOrders.isEmpty()) {
            vo.setAvgOrderAmount(totalSales.divide(BigDecimal.valueOf(allOrders.size()), 2, RoundingMode.HALF_UP));
        } else {
            vo.setAvgOrderAmount(BigDecimal.ZERO);
        }

        // 6. 获取今日的所有订单项（用于分类和商品统计）
        List<Long> todayOrderIds = todayOrders.stream().map(Order::getOrderId).collect(Collectors.toList());
        List<OrderItem> todayOrderItems = new ArrayList<>();
        if (!todayOrderIds.isEmpty()) {
            todayOrderItems = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().in("order_id", todayOrderIds)
            );
        }

        vo.setTopProducts(getTopProducts(todayOrderItems));
        vo.setSalesByCabinet(getSalesByCabinet(todayOrders));  // 今日货柜销售
        vo.setSalesByCategory(getSalesByCategory(todayOrderItems));  // 今日分类销售

        return vo;
    }

    @Override
    public StatisticsVO getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        StatisticsVO vo = new StatisticsVO();

        List<Order> orders = orderService.lambdaQuery()
                .between(Order::getCreateTime, startDate, endDate)
                .in(Order::getStatus, Arrays.asList(1, 2))
                .list();

        vo.setTodayOrders((long) orders.size());
        vo.setTotalOrders((long) orders.size());

        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodaySales(totalSales);
        vo.setTotalSales(totalSales);

        if (!orders.isEmpty()) {
            vo.setAvgOrderAmount(totalSales.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP));
        } else {
            vo.setAvgOrderAmount(BigDecimal.ZERO);
        }

        // 获取该时间段的所有订单项
        List<Long> orderIds = orders.stream().map(Order::getOrderId).collect(Collectors.toList());
        List<OrderItem> orderItems = new ArrayList<>();
        if (!orderIds.isEmpty()) {
            orderItems = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().in("order_id", orderIds)
            );
        }

        vo.setTopProducts(getTopProducts(orderItems));
        vo.setSalesByCabinet(getSalesByCabinet(orders));
        vo.setSalesByCategory(getSalesByCategory(orderItems));

        return vo;
    }

    private List<Map<String, Object>> getTopProducts(List<OrderItem> orderItems) {
        List<Map<String, Object>> topProducts = new ArrayList<>();

        if (orderItems.isEmpty()) {
            return topProducts;
        }

        // 按商品ID分组统计
        Map<Long, List<OrderItem>> itemsByProduct = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getProductId));

        // 转换为Map并统计
        List<Map<String, Object>> productStats = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : itemsByProduct.entrySet()) {
            Map<String, Object> stat = new HashMap<>();
            Long productId = entry.getKey();
            List<OrderItem> items = entry.getValue();

            int totalQuantity = items.stream().mapToInt(OrderItem::getQuantity).sum();
            BigDecimal totalAmount = items.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stat.put("productId", productId);
            stat.put("productName", items.get(0).getProductName());
            stat.put("totalQuantity", totalQuantity);
            stat.put("totalAmount", totalAmount);
            productStats.add(stat);
        }

        // 按销量排序，取TOP10
        productStats.sort((a, b) -> {
            int qtyA = (int) a.get("totalQuantity");
            int qtyB = (int) b.get("totalQuantity");
            return Integer.compare(qtyB, qtyA);
        });

        return productStats.stream().limit(10).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getSalesByCabinet(List<Order> orders) {
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<Order>> ordersByCabinet = orders.stream()
                .collect(Collectors.groupingBy(Order::getCabinetId));

        // 获取所有货柜信息
        List<Long> cabinetIds = new ArrayList<>(ordersByCabinet.keySet());
        List<Cabinet> cabinets = cabinetMapper.selectBatchIds(cabinetIds);
        Map<Long, Cabinet> cabinetMap = cabinets.stream()
                .collect(Collectors.toMap(Cabinet::getCabinetId, c -> c));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<Order>> entry : ordersByCabinet.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            Long cabinetId = entry.getKey();
            List<Order> orderList = entry.getValue();

            Cabinet cabinet = cabinetMap.get(cabinetId);
            String cabinetName = cabinet != null ? cabinet.getName() : "货柜" + cabinetId;

            item.put("cabinetId", cabinetId);
            item.put("cabinetName", cabinetName);
            item.put("orderCount", orderList.size());
            BigDecimal sales = orderList.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.put("totalSales", sales);
            result.add(item);
        }

        // 按销售额排序
        result.sort((a, b) -> {
            BigDecimal salesA = (BigDecimal) a.get("totalSales");
            BigDecimal salesB = (BigDecimal) b.get("totalSales");
            return salesB.compareTo(salesA);
        });

        return result;
    }

    private List<Map<String, Object>> getSalesByCategory(List<OrderItem> orderItems) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (orderItems.isEmpty()) {
            return result;
        }

        // 获取所有商品信息
        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .collect(Collectors.toList());
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        // 按分类统计
        Map<String, List<OrderItem>> itemsByCategory = new HashMap<>();
        for (OrderItem item : orderItems) {
            Product product = productMap.get(item.getProductId());
            String category = product != null ? product.getCategory() : "其他";
            itemsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
        }

        for (Map.Entry<String, List<OrderItem>> entry : itemsByCategory.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            String category = entry.getKey();
            List<OrderItem> items = entry.getValue();

            BigDecimal totalSales = items.stream()
                    .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalQuantity = items.stream().mapToInt(OrderItem::getQuantity).sum();

            item.put("category", category);
            item.put("name", category);
            item.put("totalSales", totalSales);
            item.put("totalQuantity", totalQuantity);
            result.add(item);
        }

        // 按销售额排序
        result.sort((a, b) -> {
            BigDecimal salesA = (BigDecimal) a.get("totalSales");
            BigDecimal salesB = (BigDecimal) b.get("totalSales");
            return salesB.compareTo(salesA);
        });

        return result;
    }
}
