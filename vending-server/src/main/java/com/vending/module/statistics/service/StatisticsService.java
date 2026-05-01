package com.vending.module.statistics.service;

import com.vending.module.statistics.dto.StatisticsVO;

import java.time.LocalDateTime;

public interface StatisticsService {
    StatisticsVO getOverviewStatistics();
    StatisticsVO getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
