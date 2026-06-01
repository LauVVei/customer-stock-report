export interface ApiResponse<T> {
  success: boolean;
  message: string;
  result: T;
}

export interface DailyOverviewPoint {
  profit_date: string;
  total_assets: number;
  total_available_amount: number;
  total_market_value: number;
  daily_profit: number;
  daily_profit_rate: number;
  daily_realized_profit: number;
  total_holding_actual_profit: number;
  total_init_cost_holding_profit: number;
  account_profit_rate: number;
  cumulative_realized_net_profit: number;
  daily_intraday_t_profit: number;
  unmatched_trade_count: number;
  total_trades: number;
}

export interface AccountDailyPoint {
  profit_date: string;
  trade_account: string;
  total_assets: number;
  total_available_amount: number;
  holding_stock_count: number;
  total_market_value: number;
  total_allocation_amount: number;
  total_holding_actual_profit: number;
  total_init_cost_holding_profit: number;
  account_profit_rate: number;
  daily_profit: number;
  daily_profit_rate: number;
  daily_realized_profit: number;
  daily_intraday_t_profit: number;
  cumulative_realized_net_profit: number;
  unmatched_trade_count: number;
  total_trades: number;
  report_updated_time: string;
}

export interface StockDailyPoint {
  profit_date: string;
  trade_account: string;
  stock_code: string;
  stock_name: string;
  holding_quantity: number;
  latest_price: number;
  market_value: number;
  allocation_amount: number;
  holding_actual_profit: number;
  holding_actual_profit_rate: number;
  init_cost_holding_profit: number;
  init_cost_holding_profit_rate: number;
  daily_realized_profit: number;
  daily_intraday_t_profit: number;
  unmatched_trade_count: number;
  t_trade_count: number;
  t_success_count: number;
  t_fail_count: number;
}

export interface AccountSummary {
  trade_account: string;
  total_assets: number;
  total_available_amount: number;
  holding_stock_count: number;
  total_market_value: number;
  total_allocation_amount: number;
  total_holding_actual_profit: number;
  total_init_cost_holding_profit: number;
  total_intraday_t_profit: number;
  account_profit_rate: number;
  total_trades: number;
  total_fee: number;
  total_commission: number;
  total_trade_cost: number;
  unmatched_trade_count: number;
  report_updated_time: string;
  latest_profit_date?: string;
  cumulative_realized_net_profit?: number;
  daily_intraday_t_profit?: number;
  daily_profit?: number;
  daily_profit_rate?: number;
  daily_realized_profit?: number;
}

export interface StockDetail {
  trade_account: string;
  stock_code: string;
  stock_name: string;
  holding_quantity: number;
  holding_cost_price: number;
  init_cost_price: number;
  latest_price: number;
  latest_price_time: string;
  total_assets: number;
  available_amount: number;
  market_value: number;
  allocation_amount: number;
  allocation_ratio: number;
  latest_vs_current_cost_price_diff: number;
  current_vs_init_cost_price_diff: number;
  holding_actual_profit: number;
  holding_actual_profit_rate: number;
  init_cost_holding_profit: number;
  init_cost_holding_profit_rate: number;
  allocation_current_profit: number;
  allocation_profit_rate: number;
  total_trades: number;
  win_trades: number;
  loss_trades: number;
  stock_win_rate: number;
  buy_count: number;
  sell_count: number;
  buy_quantity: number;
  sell_quantity: number;
  buy_amount: number;
  sell_amount: number;
  avg_buy_price: number;
  avg_sell_price: number;
  intraday_t_profit: number;
  total_fee: number;
  total_commission: number;
  total_trade_cost: number;
  last_trade_time: string;
  unmatched_trade_count: number;
  unmatched_buy_open_count: number;
  unmatched_sell_open_count: number;
  unmatched_quantity: number;
  unmatched_amount: number;
  unmatched_avg_price: number;
  first_unmatched_trade_time: string;
  last_unmatched_trade_time: string;
  t_trade_count: number;
  t_success_count: number;
  t_fail_count: number;
  avg_t_profit: number;
  max_t_profit: number;
  min_t_profit: number;
  report_updated_time: string;
  latest_profit_date?: string;
  daily_intraday_t_profit?: number;
  daily_realized_profit?: number;
}
