package com.faultticket.controller;

import com.faultticket.common.Result;
import com.faultticket.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(statsService.overview());
    }

    @GetMapping("/by-category")
    public Result<List<Map<String, Object>>> byCategory() {
        return Result.success(statsService.byCategory());
    }

    @GetMapping("/by-priority")
    public Result<List<Map<String, Object>>> byPriority() {
        return Result.success(statsService.byPriority());
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(statsService.trend(days));
    }
}
