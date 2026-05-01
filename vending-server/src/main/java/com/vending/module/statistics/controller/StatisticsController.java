package com.vending.module.statistics.controller;

import com.vending.common.result.Result;
import com.vending.module.statistics.dto.StatisticsVO;
import com.vending.module.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public Result<StatisticsVO> getOverview() {
        StatisticsVO statistics = statisticsService.getOverviewStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/range")
    public Result<StatisticsVO> getByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        StatisticsVO statistics = statisticsService.getStatisticsByDateRange(startDate, endDate);
        return Result.success(statistics);
    }
}
