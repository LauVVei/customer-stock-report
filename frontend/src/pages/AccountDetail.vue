<template>
  <div class="report-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <a-button class="ghost-back" @click="router.push('/customer-report/accounts')">返回账户总览</a-button>
        <div class="eyebrow">ACCOUNT P&L WORKBENCH</div>
        <h1>{{ tradeAccount }} · 账户收益诊断台</h1>
        <p>
          当前页面只保留仍然有效的账户总净利润、日内 T 收益、仓位和未匹配交易风险。卡片分成“快照信号”和“资产结构”两层，便于快速扫读。
        </p>
      </div>
      <div class="hero-actions">
        <span class="summary-tag">最新日表 {{ formatDate(summary?.latest_profit_date) }}</span>
        <span class="summary-tag">当前查看 {{ selectedProfitDate === ALL_HOLDINGS_VALUE ? '全部持仓' : formatDate(selectedProfitDate) }}</span>
        <span class="summary-tag">快照时间 {{ formatTime(summary?.report_updated_time) }}</span>
        <a-button @click="loadAll" :loading="loading">刷新账户工作台</a-button>
      </div>
    </section>

    <section class="signal-strip">
      <div class="signal-item">
        <span class="signal-label">持仓股票数</span>
        <strong>{{ summary?.holding_stock_count ?? 0 }}</strong>
      </div>
      <div class="signal-item">
        <span class="signal-label">当日资产变化</span>
        <strong :class="profitClass(selectedDaySummary?.daily_profit)">{{ formatMoney(selectedDaySummary?.daily_profit) }}</strong>
      </div>
      <div class="signal-item">
        <span class="signal-label">当日变化率</span>
        <strong :class="profitClass(selectedDaySummary?.daily_profit_rate)">{{ formatPercent(selectedDaySummary?.daily_profit_rate) }}</strong>
      </div>
      <div class="signal-item">
        <span class="signal-label">当日已实现收益</span>
        <strong :class="profitClass(selectedDaySummary?.daily_realized_profit)">{{ formatMoney(selectedDaySummary?.daily_realized_profit) }}</strong>
      </div>
    </section>

    <div class="kpi-grid detail-kpi-grid">
      <MetricCard title="日终总资产" :value="formatMoney(selectedDaySummary?.total_assets)" subtext="所选交易日收盘后的账户资产" />
      <MetricCard title="日终现金" :value="formatMoney(selectedDaySummary?.total_available_amount)" subtext="所选交易日回推后的现金余额" />
      <MetricCard title="日终持仓市值" :value="formatMoney(selectedDaySummary?.total_market_value)" subtext="按当日收盘价计算" />
      <MetricCard title="当日资产变化" :value="formatMoney(selectedDaySummary?.daily_profit)" :type="profitType(selectedDaySummary?.daily_profit)" subtext="当日总资产 - 前一交易日总资产" />
      <MetricCard title="当日变化率" :value="formatPercent(selectedDaySummary?.daily_profit_rate)" :type="profitType(selectedDaySummary?.daily_profit_rate)" subtext="当日资产变化 ÷ 前一交易日总资产" />
      <MetricCard title="当日已实现收益" :value="formatMoney(selectedDaySummary?.daily_realized_profit)" :type="profitType(selectedDaySummary?.daily_realized_profit)" subtext="所选交易日交易兑现收益" />
      <MetricCard title="当前总净利润" :value="formatMoney(summary?.total_holding_actual_profit)" :type="profitType(summary?.total_holding_actual_profit)" subtext="按 最新价 - 当前成本价 计算" />
      <MetricCard title="今日日内T收益" :value="formatMoney(summary?.daily_intraday_t_profit)" :type="profitType(summary?.daily_intraday_t_profit)" subtext="最新日表中的当日 T 收益" />
    </div>

    <div class="chart-grid-3">
      <ChartCard title="每日总资产曲线" :option="totalAssetsTrendOption" height="320px" />
      <ChartCard title="每日资产变化" :option="dailyProfitTrendOption" height="320px" />
      <ChartCard title="每日变化率" :option="dailyProfitRateTrendOption" height="320px" />
    </div>

    <div class="chart-grid-3">
      <ChartCard title="现金 / 持仓市值结构" :option="assetStructureOption" height="320px" />
      <ChartCard title="每日已实现收益" :option="dailyRealizedTrendOption" height="320px" />
      <ChartCard title="每日持仓浮盈" :option="holdingProfitTrendOption" height="320px" />
    </div>

    <div class="chart-grid-3">
      <ChartCard title="股票当前净利润 Top10" :option="holdingProfitTopOption" height="300px" />
      <ChartCard title="股票今日日内T收益 Top10" :option="dailyTTopOption" height="300px" />
      <ChartCard title="股票未匹配交易 Top10" :option="unmatchedTopOption" height="300px" />
    </div>

    <div class="section-card" style="margin-bottom: 20px">
      <div class="section-title">
        <h3>账户风险切片</h3>
        <span class="meta-text">用最新快照快速划分收益扩张、亏损暴露、T 收益贡献和未匹配交易堆积情况。</span>
      </div>
      <div class="chip-grid">
        <span class="summary-tag">当前净利润为正股票 {{ distribution.holding_profit_count ?? 0 }}</span>
        <span class="summary-tag">当前净利润为负股票 {{ distribution.holding_loss_count ?? 0 }}</span>
        <span class="summary-tag">今日日内T收益为正股票 {{ distribution.t_profit_count ?? 0 }}</span>
        <span class="summary-tag">今日日内T收益为负股票 {{ distribution.t_loss_count ?? 0 }}</span>
        <span class="summary-tag">存在未匹配交易股票 {{ distribution.unmatched_count ?? 0 }}</span>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title">
        <h3>持仓明细</h3>
        <div class="filter-row">
          <a-select v-model:value="selectedProfitDate" style="width: 180px" @change="reloadStocks">
            <a-select-option :value="ALL_HOLDINGS_VALUE">全部持仓</a-select-option>
            <a-select-option v-for="item in accountDailyOptions" :key="item.profit_date" :value="item.profit_date">
              {{ formatDate(item.profit_date) }}
            </a-select-option>
          </a-select>
          <a-input v-model:value="keyword" allow-clear placeholder="搜索股票代码或名称" style="width: 220px" @pressEnter="reloadStocks" />
          <a-select v-model:value="profitTypeFilter" style="width: 220px" @change="reloadStocks">
            <a-select-option value="all">全部股票</a-select-option>
            <a-select-option value="profit">仅看当前净利润为正</a-select-option>
            <a-select-option value="loss">仅看当前净利润为负</a-select-option>
            <a-select-option value="tplus">仅看今日日内T收益为正</a-select-option>
          </a-select>
          <a-button @click="reloadStocks">应用筛选</a-button>
        </div>
      </div>

      <a-table
        :data-source="stocks"
        :columns="columns"
        :loading="loading"
        row-key="stock_code"
        :pagination="{ pageSize: 12 }"
        :scroll="{ x: 2460 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="moneyColumns.includes(String(column.key))">
            <span :class="profitClass(record[column.key])">{{ formatMoney(record[column.key]) }}</span>
          </template>
          <template v-else-if="percentColumns.includes(String(column.key))">
            <span :class="profitClass(record[column.key])">{{ formatPercent(record[column.key]) }}</span>
          </template>
          <template v-else-if="dateColumns.includes(String(column.key))">
            {{ formatDate(record[column.key]) }}
          </template>
          <template v-else-if="timeColumns.includes(String(column.key))">
            {{ formatTime(record[column.key]) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" @click="openStockDrawer(record.stock_code)">打开股票台账</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <StockDrawer :open="drawerOpen" :stock="activeStock" :loading="drawerLoading" @close="drawerOpen = false" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dayjs from 'dayjs';
import MetricCard from '@/components/MetricCard.vue';
import ChartCard from '@/components/ChartCard.vue';
import StockDrawer from '@/components/StockDrawer.vue';
import { getAccountCharts, getAccountDailyProfit, getAccountStocks, getAccountSummary, getStockDetail } from '@/api/report';
import type { AccountDailyPoint, AccountSummary, StockDetail } from '@/types/report';

const palette = ['#d62828', '#f77f00', '#fcbf49', '#2a9d8f', '#277da1', '#577590', '#7b2cbf', '#4d908e', '#c1121f', '#3a86ff'];

const route = useRoute();
const router = useRouter();
const ALL_HOLDINGS_VALUE = '__all__';
const tradeAccount = computed(() => String(route.params.tradeAccount || ''));
const loading = ref(false);
const drawerLoading = ref(false);
const drawerOpen = ref(false);
const summary = ref<AccountSummary | null>(null);
const accountDaily = ref<AccountDailyPoint[]>([]);
const charts = ref<any>({});
const stocks = ref<StockDetail[]>([]);
const activeStock = ref<StockDetail | null>(null);
const keyword = ref('');
const profitTypeFilter = ref('all');
const selectedProfitDate = ref<string>(ALL_HOLDINGS_VALUE);
const selectedDaySummary = computed(() => {
  const selectedDate = normalizeDateParam(selectedProfitDate.value);
  return accountDaily.value.find((item) => normalizeDateParam(item.profit_date) === selectedDate) || accountDaily.value.at(-1) || null;
});
const accountDailyOptions = computed(() => [...accountDaily.value].reverse());
const sortBy = ref('market_value');
const sortOrder = ref<'asc' | 'desc'>('desc');

const moneyColumns = [
  'holding_cost_price',
  'init_cost_price',
  'latest_price',
  'market_value',
  'allocation_amount',
  'holding_actual_profit',
  'init_cost_holding_profit',
  'latest_vs_current_cost_price_diff',
  'current_vs_init_cost_price_diff',
  'intraday_t_profit',
  'daily_intraday_t_profit',
  'daily_realized_profit',
];
const percentColumns = ['holding_actual_profit_rate', 'init_cost_holding_profit_rate', 'allocation_ratio', 'stock_win_rate'];
const timeColumns = ['latest_price_time', 'last_trade_time'];
const dateColumns = ['latest_profit_date'];

const columns = [
  { title: '股票代码', dataIndex: 'stock_code', key: 'stock_code', fixed: 'left', width: 110 },
  { title: '股票名称', dataIndex: 'stock_name', key: 'stock_name', fixed: 'left', width: 150 },
  { title: '最新日表', dataIndex: 'latest_profit_date', key: 'latest_profit_date', width: 110 },
  { title: '持仓数量', dataIndex: 'holding_quantity', key: 'holding_quantity', width: 100 },
  { title: '当前成本价', dataIndex: 'holding_cost_price', key: 'holding_cost_price', width: 120 },
  { title: '初始成本价', dataIndex: 'init_cost_price', key: 'init_cost_price', width: 120 },
  { title: '最新价', dataIndex: 'latest_price', key: 'latest_price', sorter: true, width: 110 },
  { title: '持仓市值', dataIndex: 'market_value', key: 'market_value', sorter: true, width: 130 },
  { title: '持仓成本金额', dataIndex: 'allocation_amount', key: 'allocation_amount', sorter: true, width: 135 },
  { title: '当前净利润', dataIndex: 'holding_actual_profit', key: 'holding_actual_profit', sorter: true, width: 135 },
  { title: '当前净利润率', dataIndex: 'holding_actual_profit_rate', key: 'holding_actual_profit_rate', sorter: true, width: 140 },
  { title: '日内 T 累计收益', dataIndex: 'intraday_t_profit', key: 'intraday_t_profit', sorter: true, width: 150 },
  { title: '当日已实现收益', dataIndex: 'daily_realized_profit', key: 'daily_realized_profit', sorter: true, width: 150 },
  { title: '今日日内T收益', dataIndex: 'daily_intraday_t_profit', key: 'daily_intraday_t_profit', sorter: true, width: 150 },
  { title: '未匹配交易', dataIndex: 'unmatched_trade_count', key: 'unmatched_trade_count', sorter: true, width: 120 },
  { title: '最近成交时间', dataIndex: 'last_trade_time', key: 'last_trade_time', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 140 },
];

const totalAssetsTrendOption = computed(() => buildLineOption('每日总资产曲线', accountDaily.value, 'total_assets', '#243447'));
const dailyProfitTrendOption = computed(() => buildBarOption('每日资产变化', accountDaily.value, 'daily_profit'));
const dailyProfitRateTrendOption = computed(() => buildLineOption('每日变化率', accountDaily.value, 'daily_profit_rate', '#277da1'));
const assetStructureOption = computed(() => buildStackOption(accountDaily.value));
const dailyRealizedTrendOption = computed(() => buildBarOption('每日已实现收益', accountDaily.value, 'daily_realized_profit'));
const holdingProfitTrendOption = computed(() => buildLineOption('近 10 日账户当前总净利润', accountDaily.value, 'total_holding_actual_profit', '#c1121f'));
const holdingProfitTopOption = computed(() => buildRankingOption('股票当前净利润 Top10', charts.value.holdingProfitTop, 'holding_actual_profit'));
const dailyTTopOption = computed(() => buildRankingOption('股票今日日内T收益 Top10', charts.value.dailyIntradayTop, 'daily_intraday_t_profit'));
const unmatchedTopOption = computed(() => buildRankingOption('股票未匹配交易 Top10', charts.value.unmatchedTop, 'unmatched_trade_count'));
const distribution = computed(() => charts.value.distribution || {});

async function loadAll() {
  loading.value = true;
  try {
    const [summaryData, dailyData, chartData] = await Promise.all([
      getAccountSummary(tradeAccount.value),
      getAccountDailyProfit(tradeAccount.value),
      getAccountCharts(tradeAccount.value),
    ]);
    summary.value = summaryData;
    accountDaily.value = dailyData;
    selectedProfitDate.value = ALL_HOLDINGS_VALUE;
    charts.value = normalizeCharts(chartData);
    await reloadStocks();
  } finally {
    loading.value = false;
  }
}

async function reloadStocks() {
  stocks.value = await getAccountStocks(tradeAccount.value, {
    keyword: keyword.value || undefined,
    profitDate: selectedProfitDate.value === ALL_HOLDINGS_VALUE ? undefined : normalizeDateParam(selectedProfitDate.value),
    profitType: profitTypeFilter.value === 'all' ? undefined : profitTypeFilter.value,
    sortBy: sortBy.value,
    sortOrder: sortOrder.value,
  });
}

async function openStockDrawer(stockCode: string) {
  drawerOpen.value = true;
  drawerLoading.value = true;
  try {
    activeStock.value = await getStockDetail(tradeAccount.value, stockCode);
  } finally {
    drawerLoading.value = false;
  }
}

function handleTableChange(_: any, __: any, sorter: any) {
  const activeSorter = Array.isArray(sorter) ? sorter.find((item) => item?.order) : sorter;
  if (activeSorter?.order) {
    sortBy.value = String(activeSorter.columnKey || activeSorter.field || '');
    sortOrder.value = activeSorter.order === 'ascend' ? 'asc' : 'desc';
  } else {
    sortBy.value = 'daily_intraday_t_profit';
    sortOrder.value = 'desc';
  }
  reloadStocks();
}

function normalizeCharts(data: any) {
  return {
    holdingProfitTop: normalizeList(data.holdingProfitTop, 'holding_actual_profit'),
    dailyIntradayTop: normalizeList(data.dailyIntradayTop, 'daily_intraday_t_profit'),
    unmatchedTop: normalizeList(data.unmatchedTop, 'unmatched_trade_count'),
    distribution: data.distribution || {},
  };
}

function normalizeList(list: any[] = [], key: string) {
  return list.map((item) => ({ stock_name: item.stock_name, [key]: Number(item[key] || 0) }));
}

function buildLineOption(title: string, list: AccountDailyPoint[], valueKey: keyof AccountDailyPoint, color: string) {
  const filtered = list.filter((item) => item[valueKey] != null);
  const zoomStart = filtered.length > 10 ? ((filtered.length - 10) / filtered.length) * 100 : 0;
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 46, bottom: 80 },
    xAxis: { type: 'category', data: filtered.map((item) => item.profit_date), axisLabel: { color: '#6b7280' } },
    yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
    dataZoom: [
      { type: 'inside', start: zoomStart, end: 100, zoomOnMouseWheel: false, moveOnMouseMove: true, moveOnMouseWheel: true },
    ],
    series: [{ type: 'line', smooth: true, data: filtered.map((item) => Number(item[valueKey])), symbol: 'circle', symbolSize: 8, lineStyle: { width: 3, color }, itemStyle: { color }, areaStyle: { color: `${color}22` } }],
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
  };
}

function buildBarOption(title: string, list: AccountDailyPoint[], valueKey: keyof AccountDailyPoint) {
  const filtered = list.filter((item) => item[valueKey] != null);
  const zoomStart = filtered.length > 10 ? ((filtered.length - 10) / filtered.length) * 100 : 0;
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 46, bottom: 80 },
    xAxis: { type: 'category', data: filtered.map((item) => item.profit_date), axisLabel: { color: '#6b7280' } },
    yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
    dataZoom: [
      { type: 'inside', start: zoomStart, end: 100, zoomOnMouseWheel: false, moveOnMouseMove: true, moveOnMouseWheel: true },
    ],
    series: [{ type: 'bar', data: filtered.map((item) => Number(item[valueKey])), barMaxWidth: 28, itemStyle: { borderRadius: [8, 8, 0, 0], color: (params: any) => Number(params.value) >= 0 ? '#c1121f' : '#067647' } }],
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
  };
}

function buildStackOption(list: AccountDailyPoint[]) {
  const zoomStart = list.length > 10 ? ((list.length - 10) / list.length) * 100 : 0;
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 22, data: ['现金', '持仓市值'] },
    grid: { left: 50, right: 20, top: 70, bottom: 80 },
    xAxis: { type: 'category', data: list.map((item) => item.profit_date), axisLabel: { color: '#6b7280' } },
    yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
    dataZoom: [
      { type: 'inside', start: zoomStart, end: 100, zoomOnMouseWheel: false, moveOnMouseMove: true, moveOnMouseWheel: true },
    ],
    series: [
      {
        name: '现金',
        type: 'bar',
        stack: 'asset',
        data: list.map((item) => Number(item.total_available_amount ?? 0)),
        itemStyle: { color: '#277da1' },
      },
      {
        name: '持仓市值',
        type: 'bar',
        stack: 'asset',
        data: list.map((item) => Number(item.total_market_value ?? 0)),
        itemStyle: { color: '#d9ab4d' },
      },
    ],
    title: { text: '现金 / 持仓市值结构', left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
  };
}

function buildRankingOption(title: string, list: any[] = [], key: string) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 46, bottom: 70 },
    xAxis: { type: 'category', data: list.map((item) => item.stock_name), axisLabel: { rotate: 18, color: '#6b7280' } },
    yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
    series: [{ type: 'bar', data: list.map((item) => item[key]), barMaxWidth: 32, itemStyle: { borderRadius: [8, 8, 0, 0], color: (params: any) => palette[params.dataIndex % palette.length] } }],
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
  };
}

function formatMoney(value?: number | null) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function formatPercent(value?: number | null) {
  return `${Number(value || 0).toFixed(2)}%`;
}
function formatDate(value?: string | null) {
  return value ? dayjs(value).format('YYYY-MM-DD') : '--';
}
function normalizeDateParam(value?: string | null) {
  if (!value || value === ALL_HOLDINGS_VALUE) return undefined;
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format('YYYY-MM-DD') : undefined;
}
function formatTime(value?: string | null) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '--';
}
function profitType(value?: number | null) {
  if (Number(value || 0) > 0) return 'profit';
  if (Number(value || 0) < 0) return 'loss';
  return 'neutral';
}
function profitClass(value?: number | null) {
  if (Number(value || 0) > 0) return 'profit-text';
  if (Number(value || 0) < 0) return 'loss-text';
  return '';
}

onMounted(loadAll);
</script>
