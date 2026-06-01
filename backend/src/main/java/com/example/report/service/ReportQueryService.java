package com.example.report.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportQueryService {
    private static final String SUMMARY_TABLE = "public.customer_stock_report_summary";
    private static final String DETAIL_TABLE = "public.customer_stock_report_detail";
    private static final String SUMMARY_DAILY_TABLE = "public.customer_stock_report_summary_daily";
    private static final String DETAIL_DAILY_TABLE = "public.customer_stock_report_detail_daily";

    private final JdbcTemplate jdbcTemplate;

    public ReportQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getOverview() {
        String cardsSql = """
                with latest_daily as (
                    select *
                    from %s
                    where profit_date = (select max(profit_date) from %s)
                )
                select
                    count(*) as account_count,
                    coalesce(sum(s.total_assets), 0) as total_assets,
                    coalesce(sum(s.total_available_amount), 0) as total_available_amount,
                    coalesce(sum(s.total_market_value), 0) as total_market_value,
                    coalesce(sum(s.total_allocation_amount), 0) as total_allocation_amount,
                    coalesce(sum(s.total_holding_actual_profit), 0) as total_holding_actual_profit,
                    coalesce(sum(s.total_init_cost_holding_profit), 0) as total_init_cost_holding_profit,
                    coalesce(sum(s.total_intraday_t_profit), 0) as total_intraday_t_profit,
                    coalesce(sum(s.total_trades), 0) as total_trades,
                    coalesce(sum(s.total_trade_cost), 0) as total_trade_cost,
                    coalesce(sum(s.unmatched_trade_count), 0) as unmatched_trade_count,
                    coalesce(sum(ld.cumulative_realized_net_profit), 0) as cumulative_realized_net_profit,
                    coalesce(sum(ld.daily_profit), 0) as daily_profit,
                    coalesce(sum(ld.daily_realized_profit), 0) as daily_realized_profit,
                    coalesce(sum(ld.daily_intraday_t_profit), 0) as daily_intraday_t_profit,
                    max(ld.profit_date) as latest_profit_date,
                    max(s.report_updated_time) as report_updated_time
                from %s s
                left join latest_daily ld on ld.trade_account = s.trade_account
                """.formatted(SUMMARY_DAILY_TABLE, SUMMARY_DAILY_TABLE, SUMMARY_TABLE);

        String holdingProfitRankingSql = """
                select trade_account, total_holding_actual_profit
                from %s
                order by total_holding_actual_profit desc nulls last
                limit 10
                """.formatted(SUMMARY_TABLE);

        String dailyTRankingSql = """
                select trade_account, daily_intraday_t_profit
                from %s
                where profit_date = (select max(profit_date) from %s)
                order by daily_intraday_t_profit desc nulls last
                limit 10
                """.formatted(SUMMARY_DAILY_TABLE, SUMMARY_DAILY_TABLE);

        String unmatchedRankingSql = """
                select trade_account, unmatched_trade_count
                from %s
                order by unmatched_trade_count desc nulls last
                limit 10
                """.formatted(SUMMARY_TABLE);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cards", jdbcTemplate.queryForMap(cardsSql));
        result.put("holdingProfitRanking", jdbcTemplate.queryForList(holdingProfitRankingSql));
        result.put("dailyTRanking", jdbcTemplate.queryForList(dailyTRankingSql));
        result.put("unmatchedRanking", jdbcTemplate.queryForList(unmatchedRankingSql));
        return result;
    }

    public List<Map<String, Object>> getOverviewDaily(String startDate, String endDate) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                select
                    profit_date,
                    sum(total_assets) as total_assets,
                    sum(total_available_amount) as total_available_amount,
                    sum(total_market_value) as total_market_value,
                    sum(daily_profit) as daily_profit,
                    case
                        when lag(sum(total_assets)) over (order by profit_date) is null then null
                        else sum(daily_profit) / nullif(lag(sum(total_assets)) over (order by profit_date), 0) * 100
                    end as daily_profit_rate,
                    sum(daily_realized_profit) as daily_realized_profit,
                    sum(total_holding_actual_profit) as total_holding_actual_profit,
                    sum(total_init_cost_holding_profit) as total_init_cost_holding_profit,
                    case
                        when sum(total_assets) is null then null
                        else sum(total_holding_actual_profit) / nullif(sum(total_assets), 0) * 100
                    end as account_profit_rate,
                    sum(cumulative_realized_net_profit) as cumulative_realized_net_profit,
                    sum(daily_intraday_t_profit) as daily_intraday_t_profit,
                    sum(unmatched_trade_count) as unmatched_trade_count,
                    sum(total_trades) as total_trades
                from %s
                where 1 = 1
                """.formatted(SUMMARY_DAILY_TABLE));
        appendDateFilters(sql, params, startDate, endDate);
        sql.append(" group by profit_date order by profit_date asc");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> getAccounts() {
        String sql = """
                with latest_daily as (
                    select *
                    from %s
                    where profit_date = (select max(profit_date) from %s)
                )
                select
                    s.trade_account,
                    s.total_assets,
                    s.total_available_amount,
                    s.holding_stock_count,
                    s.total_market_value,
                    s.total_allocation_amount,
                    s.total_holding_actual_profit,
                    s.total_init_cost_holding_profit,
                    s.total_intraday_t_profit,
                    s.account_profit_rate,
                    s.total_trades,
                    s.total_fee,
                    s.total_commission,
                    s.total_trade_cost,
                    s.unmatched_trade_count,
                    s.report_updated_time,
                    ld.profit_date as latest_profit_date,
                    coalesce(ld.cumulative_realized_net_profit, 0) as cumulative_realized_net_profit,
                    coalesce(ld.daily_intraday_t_profit, 0) as daily_intraday_t_profit
                from %s s
                left join latest_daily ld on ld.trade_account = s.trade_account
                order by coalesce(ld.daily_intraday_t_profit, 0) desc nulls last, s.total_holding_actual_profit desc nulls last
                """.formatted(SUMMARY_DAILY_TABLE, SUMMARY_DAILY_TABLE, SUMMARY_TABLE);
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getAccountSummary(String tradeAccount) {
        String sql = """
                with latest_daily as (
                    select *
                    from %s
                    where profit_date = (select max(profit_date) from %s)
                )
                select
                    s.trade_account,
                    s.total_assets,
                    s.total_available_amount,
                    s.holding_stock_count,
                    s.total_market_value,
                    s.total_allocation_amount,
                    s.total_holding_actual_profit,
                    s.total_init_cost_holding_profit,
                    s.total_intraday_t_profit,
                    s.account_profit_rate,
                    s.total_trades,
                    s.total_fee,
                    s.total_commission,
                    s.total_trade_cost,
                    s.unmatched_trade_count,
                    s.report_updated_time,
                    ld.profit_date as latest_profit_date,
                    coalesce(ld.cumulative_realized_net_profit, 0) as cumulative_realized_net_profit,
                    coalesce(ld.daily_intraday_t_profit, 0) as daily_intraday_t_profit
                from %s s
                left join latest_daily ld on ld.trade_account = s.trade_account
                where s.trade_account = ?
                """.formatted(SUMMARY_DAILY_TABLE, SUMMARY_DAILY_TABLE, SUMMARY_TABLE);
        return jdbcTemplate.queryForMap(sql, tradeAccount);
    }

    public List<Map<String, Object>> getAccountDailyProfit(String tradeAccount, String startDate, String endDate) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                select
                    profit_date,
                    trade_account,
                    total_assets,
                    total_available_amount,
                    holding_stock_count,
                    total_market_value,
                    total_allocation_amount,
                    total_holding_actual_profit,
                    total_init_cost_holding_profit,
                    account_profit_rate,
                    daily_profit,
                    daily_profit_rate,
                    daily_realized_profit,
                    daily_intraday_t_profit,
                    cumulative_realized_net_profit,
                    unmatched_trade_count,
                    total_trades,
                    report_updated_time
                from %s
                where trade_account = ?
                """.formatted(SUMMARY_DAILY_TABLE));
        params.add(tradeAccount);
        appendDateFilters(sql, params, startDate, endDate);
        sql.append(" order by profit_date asc");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> getAccountStocks(String tradeAccount, String profitDate, String keyword, String profitType, String sortBy, String sortOrder) {
        if (profitDate != null && !profitDate.isBlank()) {
            return getAccountStocksByProfitDate(tradeAccount, profitDate, keyword, profitType, sortBy, sortOrder);
        }

        String safeSortBy = mapSortBy(sortBy);
        String safeSortOrder = "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc";

        StringBuilder sql = new StringBuilder("""
                with latest_daily as (
                    select *
                    from %s
                    where profit_date = (select max(profit_date) from %s)
                )
                select
                    d.trade_account,
                    d.stock_code,
                    d.stock_name,
                    d.holding_quantity,
                    d.holding_cost_price,
                    d.init_cost_price,
                    d.latest_price,
                    d.latest_price_time,
                    d.total_assets,
                    d.available_amount,
                    d.market_value,
                    d.allocation_amount,
                    d.allocation_ratio,
                    d.latest_vs_current_cost_price_diff,
                    d.current_vs_init_cost_price_diff,
                    d.holding_actual_profit,
                    d.holding_actual_profit_rate,
                    d.init_cost_holding_profit,
                    d.init_cost_holding_profit_rate,
                    d.allocation_current_profit,
                    d.allocation_profit_rate,
                    d.total_trades,
                    d.win_trades,
                    d.loss_trades,
                    d.stock_win_rate,
                    d.buy_count,
                    d.sell_count,
                    d.buy_quantity,
                    d.sell_quantity,
                    d.buy_amount,
                    d.sell_amount,
                    d.avg_buy_price,
                    d.avg_sell_price,
                    d.intraday_t_profit,
                    d.total_fee,
                    d.total_commission,
                    d.total_trade_cost,
                    d.last_trade_time,
                    d.unmatched_trade_count,
                    d.unmatched_buy_open_count,
                    d.unmatched_sell_open_count,
                    d.unmatched_quantity,
                    d.unmatched_amount,
                    d.unmatched_avg_price,
                    d.first_unmatched_trade_time,
                    d.last_unmatched_trade_time,
                    d.t_trade_count,
                    d.t_success_count,
                    d.t_fail_count,
                    d.avg_t_profit,
                    d.max_t_profit,
                    d.min_t_profit,
                    d.report_updated_time,
                    ld.profit_date as latest_profit_date,
                    coalesce(ld.daily_intraday_t_profit, 0) as daily_intraday_t_profit
                from %s d
                left join latest_daily ld
                  on ld.trade_account = d.trade_account
                 and ld.stock_code = d.stock_code
                where d.trade_account = ?
                """.formatted(DETAIL_DAILY_TABLE, DETAIL_DAILY_TABLE, DETAIL_TABLE));

        List<Object> params = new ArrayList<>();
        params.add(tradeAccount);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and (d.stock_code like ? or d.stock_name like ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        if ("profit".equalsIgnoreCase(profitType)) {
            sql.append(" and d.holding_actual_profit > 0");
        } else if ("loss".equalsIgnoreCase(profitType)) {
            sql.append(" and d.holding_actual_profit < 0");
        } else if ("unmatched".equalsIgnoreCase(profitType)) {
            sql.append(" and d.unmatched_trade_count > 0");
        } else if ("tplus".equalsIgnoreCase(profitType)) {
            sql.append(" and coalesce(ld.daily_intraday_t_profit, 0) > 0");
        }

        sql.append(" order by ").append(safeSortBy).append(" ").append(safeSortOrder).append(", d.market_value desc");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    private List<Map<String, Object>> getAccountStocksByProfitDate(String tradeAccount, String profitDate, String keyword, String profitType, String sortBy, String sortOrder) {
        String safeSortBy = mapDailySortBy(sortBy);
        String safeSortOrder = "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc";

        StringBuilder sql = new StringBuilder("""
                select
                    trade_account,
                    stock_code,
                    stock_name,
                    profit_date as latest_profit_date,
                    holding_quantity,
                    holding_cost_price,
                    init_cost_price,
                    latest_price,
                    null as latest_price_time,
                    null as total_assets,
                    null as available_amount,
                    market_value,
                    allocation_amount,
                    null as allocation_ratio,
                    null as latest_vs_current_cost_price_diff,
                    null as current_vs_init_cost_price_diff,
                    holding_actual_profit,
                    holding_actual_profit_rate,
                    null as init_cost_holding_profit,
                    null as init_cost_holding_profit_rate,
                    null as allocation_current_profit,
                    null as allocation_profit_rate,
                    null as total_trades,
                    null as win_trades,
                    null as loss_trades,
                    null as stock_win_rate,
                    null as buy_count,
                    null as sell_count,
                    null as buy_quantity,
                    null as sell_quantity,
                    null as buy_amount,
                    null as sell_amount,
                    null as avg_buy_price,
                    null as avg_sell_price,
                    null as intraday_t_profit,
                    null as total_fee,
                    null as total_commission,
                    null as total_trade_cost,
                    null as last_trade_time,
                    null as unmatched_trade_count,
                    null as unmatched_buy_open_count,
                    null as unmatched_sell_open_count,
                    null as unmatched_quantity,
                    null as unmatched_amount,
                    null as unmatched_avg_price,
                    null as first_unmatched_trade_time,
                    null as last_unmatched_trade_time,
                    t_trade_count,
                    null as t_success_count,
                    null as t_fail_count,
                    null as avg_t_profit,
                    null as max_t_profit,
                    null as min_t_profit,
                    null as report_updated_time,
                    daily_realized_profit,
                    daily_intraday_t_profit
                from %s
                where trade_account = ?
                  and profit_date::date = ?::date
                """.formatted(DETAIL_DAILY_TABLE));

        List<Object> params = new ArrayList<>();
        params.add(tradeAccount);
        params.add(profitDate);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and (stock_code like ? or stock_name like ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        if ("profit".equalsIgnoreCase(profitType)) {
            sql.append(" and holding_actual_profit > 0");
        } else if ("loss".equalsIgnoreCase(profitType)) {
            sql.append(" and holding_actual_profit < 0");
        } else if ("tplus".equalsIgnoreCase(profitType)) {
            sql.append(" and coalesce(daily_intraday_t_profit, 0) > 0");
        }

        sql.append(" order by ").append(safeSortBy).append(" ").append(safeSortOrder).append(", market_value desc");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public Map<String, Object> getAccountCharts(String tradeAccount) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("holdingProfitTop", jdbcTemplate.queryForList("""
                select stock_name, holding_actual_profit
                from %s
                where trade_account = ?
                order by holding_actual_profit desc nulls last
                limit 10
                """.formatted(DETAIL_TABLE), tradeAccount));

        result.put("marketValueTop", jdbcTemplate.queryForList("""
                select stock_name, market_value
                from %s
                where trade_account = ?
                order by market_value desc nulls last
                limit 10
                """.formatted(DETAIL_TABLE), tradeAccount));

        result.put("dailyIntradayTop", jdbcTemplate.queryForList("""
                select stock_name, daily_intraday_t_profit
                from %s
                where trade_account = ?
                  and profit_date = (select max(profit_date) from %s)
                order by daily_intraday_t_profit desc nulls last
                limit 10
                """.formatted(DETAIL_DAILY_TABLE, DETAIL_DAILY_TABLE), tradeAccount));

        result.put("unmatchedTop", jdbcTemplate.queryForList("""
                select stock_name, unmatched_trade_count
                from %s
                where trade_account = ?
                order by unmatched_trade_count desc nulls last
                limit 10
                """.formatted(DETAIL_TABLE), tradeAccount));

        result.put("distribution", jdbcTemplate.queryForMap("""
                with latest_daily as (
                    select *
                    from %s
                    where trade_account = ?
                      and profit_date = (select max(profit_date) from %s)
                )
                select
                    count(*) filter (where d.holding_actual_profit > 0) as holding_profit_count,
                    count(*) filter (where d.holding_actual_profit < 0) as holding_loss_count,
                    count(*) filter (where coalesce(ld.daily_intraday_t_profit, 0) > 0) as t_profit_count,
                    count(*) filter (where coalesce(ld.daily_intraday_t_profit, 0) < 0) as t_loss_count,
                    count(*) filter (where d.unmatched_trade_count > 0) as unmatched_count
                from %s d
                left join latest_daily ld
                  on ld.trade_account = d.trade_account
                 and ld.stock_code = d.stock_code
                where d.trade_account = ?
                """.formatted(DETAIL_DAILY_TABLE, DETAIL_DAILY_TABLE, DETAIL_TABLE), tradeAccount, tradeAccount));

        return result;
    }

    public Map<String, Object> getStockDetail(String tradeAccount, String stockCode) {
        String sql = """
                with latest_daily as (
                    select *
                    from %s
                    where trade_account = ?
                      and stock_code = ?
                      and profit_date = (select max(profit_date) from %s)
                )
                select
                    d.*,
                    ld.profit_date as latest_profit_date,
                    coalesce(ld.daily_intraday_t_profit, 0) as daily_intraday_t_profit
                from %s d
                left join latest_daily ld
                  on ld.trade_account = d.trade_account
                 and ld.stock_code = d.stock_code
                where d.trade_account = ?
                  and d.stock_code = ?
                """.formatted(DETAIL_DAILY_TABLE, DETAIL_DAILY_TABLE, DETAIL_TABLE);
        return jdbcTemplate.queryForMap(sql, tradeAccount, stockCode, tradeAccount, stockCode);
    }

    public List<Map<String, Object>> getStockDailyProfit(String tradeAccount, String stockCode, String startDate, String endDate) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                select
                    profit_date,
                    trade_account,
                    stock_code,
                    stock_name,
                    holding_quantity,
                    latest_price,
                    market_value,
                    allocation_amount,
                    holding_actual_profit,
                    holding_actual_profit_rate,
                    init_cost_holding_profit,
                    init_cost_holding_profit_rate,
                    daily_realized_profit,
                    daily_intraday_t_profit,
                    unmatched_trade_count,
                    t_trade_count,
                    t_success_count,
                    t_fail_count
                from %s
                where trade_account = ?
                  and stock_code = ?
                """.formatted(DETAIL_DAILY_TABLE));
        params.add(tradeAccount);
        params.add(stockCode);
        appendDateFilters(sql, params, startDate, endDate);
        sql.append(" order by profit_date asc");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    private void appendDateFilters(StringBuilder sql, List<Object> params, String startDate, String endDate) {
        if (startDate != null && !startDate.isBlank()) {
            sql.append(" and profit_date >= ?");
            params.add(startDate);
        }
        if (endDate != null && !endDate.isBlank()) {
            sql.append(" and profit_date <= ?");
            params.add(endDate);
        }
    }

    private String mapSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "coalesce(ld.daily_intraday_t_profit, 0)";
        }
        return switch (sortBy) {
            case "holdingActualProfitRate", "holding_actual_profit_rate" -> "d.holding_actual_profit_rate";
            case "holdingActualProfit", "holding_actual_profit", "currentProfit" -> "d.holding_actual_profit";
            case "initCostHoldingProfit", "init_cost_holding_profit" -> "d.init_cost_holding_profit";
            case "initCostHoldingProfitRate", "init_cost_holding_profit_rate" -> "d.init_cost_holding_profit_rate";
            case "marketValue", "market_value" -> "d.market_value";
            case "unmatchedTradeCount", "unmatched_trade_count" -> "d.unmatched_trade_count";
            case "intradayTProfit", "daily_intraday_t_profit" -> "coalesce(ld.daily_intraday_t_profit, 0)";
            case "intraday_t_profit" -> "d.intraday_t_profit";
            case "latestPrice", "latest_price" -> "d.latest_price";
            case "latestDiff", "latest_vs_current_cost_price_diff" -> "d.latest_vs_current_cost_price_diff";
            case "costDrift", "current_vs_init_cost_price_diff" -> "d.current_vs_init_cost_price_diff";
            default -> "coalesce(ld.daily_intraday_t_profit, 0)";
        };
    }

    private String mapDailySortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "market_value";
        }
        return switch (sortBy) {
            case "holdingActualProfitRate", "holding_actual_profit_rate" -> "holding_actual_profit_rate";
            case "holdingActualProfit", "holding_actual_profit", "currentProfit" -> "holding_actual_profit";
            case "marketValue", "market_value" -> "market_value";
            case "dailyRealizedProfit", "daily_realized_profit" -> "daily_realized_profit";
            case "intradayTProfit", "daily_intraday_t_profit", "intraday_t_profit" -> "daily_intraday_t_profit";
            case "latestPrice", "latest_price" -> "latest_price";
            case "allocationAmount", "allocation_amount" -> "allocation_amount";
            default -> "market_value";
        };
    }
}
