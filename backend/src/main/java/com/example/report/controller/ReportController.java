package com.example.report.controller;

import com.example.report.dto.ApiResponse;
import com.example.report.service.ReportQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    private final ReportQueryService reportQueryService;

    public ReportController(ReportQueryService reportQueryService) {
        this.reportQueryService = reportQueryService;
    }

    @GetMapping("/accounts/overview")
    public ApiResponse<?> accountsOverview() {
        return ApiResponse.ok(reportQueryService.getOverview());
    }

    @GetMapping("/accounts/overview/daily")
    public ApiResponse<?> accountsOverviewDaily(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.ok(reportQueryService.getOverviewDaily(startDate, endDate));
    }

    @GetMapping("/accounts/list")
    public ApiResponse<?> accountsList() {
        return ApiResponse.ok(reportQueryService.getAccounts());
    }

    @GetMapping("/accounts/{tradeAccount}/summary")
    public ApiResponse<?> accountSummary(@PathVariable String tradeAccount) {
        return ApiResponse.ok(reportQueryService.getAccountSummary(tradeAccount));
    }

    @GetMapping("/accounts/{tradeAccount}/daily-profit")
    public ApiResponse<?> accountDailyProfit(
            @PathVariable String tradeAccount,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.ok(reportQueryService.getAccountDailyProfit(tradeAccount, startDate, endDate));
    }

    @GetMapping("/accounts/{tradeAccount}/stocks")
    public ApiResponse<?> accountStocks(
            @PathVariable String tradeAccount,
            @RequestParam(required = false) String profitDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String profitType,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        return ApiResponse.ok(reportQueryService.getAccountStocks(tradeAccount, profitDate, keyword, profitType, sortBy, sortOrder));
    }

    @GetMapping("/accounts/{tradeAccount}/charts")
    public ApiResponse<?> accountCharts(@PathVariable String tradeAccount) {
        return ApiResponse.ok(reportQueryService.getAccountCharts(tradeAccount));
    }

    @GetMapping("/accounts/{tradeAccount}/stocks/{stockCode}")
    public ApiResponse<?> stockDetail(@PathVariable String tradeAccount, @PathVariable String stockCode) {
        return ApiResponse.ok(reportQueryService.getStockDetail(tradeAccount, stockCode));
    }

    @GetMapping("/accounts/{tradeAccount}/stocks/{stockCode}/daily-profit")
    public ApiResponse<?> stockDailyProfit(
            @PathVariable String tradeAccount,
            @PathVariable String stockCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.ok(reportQueryService.getStockDailyProfit(tradeAccount, stockCode, startDate, endDate));
    }
}
