import { http } from './http';
import type {
  AccountDailyPoint,
  AccountSummary,
  ApiResponse,
  DailyOverviewPoint,
  StockDailyPoint,
  StockDetail,
} from '@/types/report';

export async function getOverview() {
  const { data } = await http.get<ApiResponse<any>>('/report/accounts/overview');
  return data.result;
}

export async function getOverviewDaily(params?: Record<string, string | undefined>) {
  const { data } = await http.get<ApiResponse<DailyOverviewPoint[]>>('/report/accounts/overview/daily', { params });
  return data.result;
}

export async function getAccounts() {
  const { data } = await http.get<ApiResponse<AccountSummary[]>>('/report/accounts/list');
  return data.result;
}

export async function getAccountSummary(tradeAccount: string) {
  const { data } = await http.get<ApiResponse<AccountSummary>>(`/report/accounts/${tradeAccount}/summary`);
  return data.result;
}

export async function getAccountDailyProfit(tradeAccount: string, params?: Record<string, string | undefined>) {
  const { data } = await http.get<ApiResponse<AccountDailyPoint[]>>(`/report/accounts/${tradeAccount}/daily-profit`, { params });
  return data.result;
}

export async function getAccountStocks(tradeAccount: string, params: Record<string, string | undefined>) {
  const { data } = await http.get<ApiResponse<StockDetail[]>>(`/report/accounts/${tradeAccount}/stocks`, { params });
  return data.result;
}

export async function getAccountCharts(tradeAccount: string) {
  const { data } = await http.get<ApiResponse<any>>(`/report/accounts/${tradeAccount}/charts`);
  return data.result;
}

export async function getStockDetail(tradeAccount: string, stockCode: string) {
  const { data } = await http.get<ApiResponse<StockDetail>>(`/report/accounts/${tradeAccount}/stocks/${stockCode}`);
  return data.result;
}

export async function getStockDailyProfit(tradeAccount: string, stockCode: string, params?: Record<string, string | undefined>) {
  const { data } = await http.get<ApiResponse<StockDailyPoint[]>>(`/report/accounts/${tradeAccount}/stocks/${stockCode}/daily-profit`, { params });
  return data.result;
}
