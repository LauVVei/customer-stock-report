<template>
  <div class="report-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <div class="eyebrow">DAILY P&L ATLAS</div>
        <h1>账户盈利与日内 T 交易总览</h1>
        <p>
          当前首页聚焦三类核心信号：账户当前总净利润、日内 T 相关收益，以及未匹配交易风险。这样可以更直接地区分持仓盈利质量、T 交易贡献和风险堆积。
        </p>
      </div>
      <div class="hero-actions">
        <span class="summary-tag">最新日表 {{ formatDate(overviewCards.latest_profit_date) }}</span>
        <span class="summary-tag">快照时间 {{ formatTime(overviewCards.report_updated_time) }}</span>
        <a-button @click="loadAll" :loading="loading">刷新总览</a-button>
      </div>
    </section>

    <section class="signal-strip">
      <div class="signal-item">
        <span class="signal-label">账户总数</span>
        <strong>{{ formatInt(overviewCards.account_count) }}</strong>
      </div>
      <div class="signal-item">
        <span class="signal-label">今日净收益</span>
        <strong :class="profitTypeClass(overviewCards.total_holding_actual_profit)">{{ formatMoney(overviewCards.total_holding_actual_profit) }}</strong>
      </div>
      <div class="signal-item">
        <span class="signal-label">日内 T 累计已实现收益</span>
        <strong :class="profitTypeClass(overviewCards.total_intraday_t_profit)">{{ formatMoney(overviewCards.total_intraday_t_profit) }}</strong>
      </div>
      <div class="signal-item">
        <span class="signal-label">未匹配交易总数</span>
        <strong>{{ formatInt(overviewCards.unmatched_trade_count) }}</strong>
      </div>
    </section>

    <div class="kpi-grid overview-kpi-grid">
      <MetricCard title="总资产" :value="formatMoney(overviewCards.total_assets)" subtext="账户层总资产合计" />
      <MetricCard title="总可用资金" :value="formatMoney(overviewCards.total_available_amount)" subtext="当前可用于后续交易的资金" />
      <MetricCard title="总持仓市值" :value="formatMoney(overviewCards.total_market_value)" subtext="全部账户当前持仓市值合计" />
      <MetricCard title="今日净收益" :value="formatMoney(overviewCards.total_holding_actual_profit)" :type="profitType(overviewCards.total_holding_actual_profit)" subtext="按最新快照口径汇总的当日净收益" />
      <MetricCard
        title="日内 T 累计已实现收益"
        :value="formatMoney(overviewCards.total_intraday_t_profit)"
        :type="profitType(overviewCards.total_intraday_t_profit)"
        subtext="仅统计日内 T 闭环交易收益"
      />
      <MetricCard
        title="初始成本口径总净利润"
        :value="formatMoney(overviewCards.total_init_cost_holding_profit)"
        :type="profitType(overviewCards.total_init_cost_holding_profit)"
        subtext="按 最新价 - 初始成本价 计算"
      />
    </div>

    <div class="chart-grid-3">
      <ChartCard title="近 10 日账户当前总净利润" :option="holdingProfitTrendOption" height="320px" />
      <ChartCard title="近 10 日初始成本口径总净利润" :option="initProfitTrendOption" height="320px" />
      <ChartCard title="近 10 日今日日内T收益" :option="dailyTTrendOption" height="320px" />
    </div>

    <div class="chart-grid">
      <ChartCard title="近 10 日账户当前净利润率" :option="profitRateTrendOption" height="320px" />
      <ChartCard title="账户今日日内T收益排行" :option="dailyTRankingOption" height="320px" />
    </div>

    <div class="section-card">
      <div class="section-title">
        <h3>账户扫描列表</h3>
        <span class="meta-text">
          表格只保留当前仍有效的口径，重点观察账户今日净收益、初始成本口径净利润和今日日内T收益的组合关系。
        </span>
      </div>
      <a-table
        :data-source="accounts"
        :columns="columns"
        :loading="loading"
        row-key="trade_account"
        :pagination="{ pageSize: 10 }"
        :scroll="{ x: 1880 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="moneyColumns.includes(String(column.key))">
            <span :class="profitTypeClass(record[column.key])">{{ formatMoney(record[column.key]) }}</span>
          </template>
          <template v-else-if="percentColumns.includes(String(column.key))">
            <span :class="profitTypeClass(record[column.key])">{{ formatPercent(record[column.key]) }}</span>
          </template>
          <template v-else-if="column.key === 'latest_profit_date'">
            {{ formatDate(record.latest_profit_date) }}
          </template>
          <template v-else-if="column.key === 'report_updated_time'">
            {{ formatTime(record.report_updated_time) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" @click="goDetail(record.trade_account)">进入账户工作台</a-button>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import MetricCard from '@/components/MetricCard.vue';
import ChartCard from '@/components/ChartCard.vue';
import { getAccounts, getOverview, getOverviewDaily } from '@/api/report';
import type { AccountSummary, DailyOverviewPoint } from '@/types/report';

const rankingPalette = ['#c1121f', '#f77f00', '#fcbf49', '#2a9d8f', '#277da1', '#577590', '#7b2cbf', '#4d908e', '#d62828', '#3a86ff'];

const router = useRouter();
const loading = ref(false);
const accounts = ref<AccountSummary[]>([]);
const overviewCards = ref<Record<string, any>>({});
const overviewCharts = ref<Record<string, any[]>>({});
const overviewDaily = ref<DailyOverviewPoint[]>([]);

const moneyColumns = [
  'total_assets',
  'total_available_amount',
  'total_market_value',
  'total_holding_actual_profit',
  'total_init_cost_holding_profit',
  'total_intraday_t_profit',
  'daily_intraday_t_profit',
];
const percentColumns = ['account_profit_rate'];

const columns = [
  { title: '交易账户', dataIndex: 'trade_account', key: 'trade_account', fixed: 'left', width: 160 },
  { title: '最新日表', dataIndex: 'latest_profit_date', key: 'latest_profit_date', width: 110 },
  { title: '总资产', dataIndex: 'total_assets', key: 'total_assets', width: 135 },
  { title: '总可用资金', dataIndex: 'total_available_amount', key: 'total_available_amount', width: 135 },
  { title: '持仓市值', dataIndex: 'total_market_value', key: 'total_market_value', width: 135 },
  { title: '当前总净利润', dataIndex: 'total_holding_actual_profit', key: 'total_holding_actual_profit', width: 145 },
  { title: '初始成本口径总净利润', dataIndex: 'total_init_cost_holding_profit', key: 'total_init_cost_holding_profit', width: 165 },
  { title: '账户当前净利润率', dataIndex: 'account_profit_rate', key: 'account_profit_rate', width: 135 },
  { title: '今日日内T收益', dataIndex: 'daily_intraday_t_profit', key: 'daily_intraday_t_profit', width: 150 },
  { title: '未匹配交易', dataIndex: 'unmatched_trade_count', key: 'unmatched_trade_count', width: 120 },
  { title: '快照更新时间', dataIndex: 'report_updated_time', key: 'report_updated_time', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 140 },
];

const holdingProfitTrendOption = computed(() => buildLineOption('近 10 日账户当前总净利润', overviewDaily.value, 'total_holding_actual_profit', '#c1121f'));
const initProfitTrendOption = computed(() => buildLineOption('近 10 日初始成本口径总净利润', overviewDaily.value, 'total_init_cost_holding_profit', '#8b5cf6'));
const dailyTTrendOption = computed(() => buildBarOption('近 10 日今日日内T收益', overviewDaily.value, 'daily_intraday_t_profit'));
const profitRateTrendOption = computed(() => buildLineOption('近 10 日账户当前净利润率', overviewDaily.value, 'account_profit_rate', '#277da1'));
const dailyTRankingOption = computed(() => buildRankingOption('账户今日日内T收益排行', overviewCharts.value.dailyTRanking, 'daily_intraday_t_profit'));

async function loadAll() {
  loading.value = true;
  try {
    const [overview, list, daily] = await Promise.all([getOverview(), getAccounts(), getOverviewDaily()]);
    overviewCards.value = overview.cards || {};
    overviewCharts.value = {
      dailyTRanking: normalizeRankingData(overview.dailyTRanking, 'daily_intraday_t_profit'),
      unmatchedRanking: normalizeRankingData(overview.unmatchedRanking, 'unmatched_trade_count'),
    };
    overviewDaily.value = daily;
    accounts.value = list;
  } finally {
    loading.value = false;
  }
}

function normalizeRankingData(list: any[] = [], valueKey: string) {
  return list.map((item) => ({
    trade_account: item.trade_account,
    [valueKey]: Number(item[valueKey] || 0),
  }));
}

function buildLineOption(title: string, list: DailyOverviewPoint[], valueKey: keyof DailyOverviewPoint, color: string) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 46, bottom: 56 },
    xAxis: { type: 'category', data: list.map((item) => item.profit_date), axisLabel: { color: '#6b7280' } },
    yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
    series: [{ type: 'line', smooth: true, data: list.map((item) => Number(item[valueKey] || 0)), symbol: 'circle', symbolSize: 8, lineStyle: { width: 3, color }, itemStyle: { color }, areaStyle: { color: `${color}22` } }],
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
  };
}

function buildBarOption(title: string, list: DailyOverviewPoint[], valueKey: keyof DailyOverviewPoint) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 46, bottom: 56 },
    xAxis: { type: 'category', data: list.map((item) => item.profit_date), axisLabel: { color: '#6b7280' } },
    yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
    series: [{ type: 'bar', data: list.map((item) => Number(item[valueKey] || 0)), barMaxWidth: 28, itemStyle: { borderRadius: [8, 8, 0, 0], color: (params: any) => Number(params.value || 0) >= 0 ? '#c1121f' : '#067647' } }],
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
  };
}

function buildRankingOption(title: string, list: any[] = [], valueKey: string) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 46, bottom: 70 },
    xAxis: { type: 'category', data: list.map((item) => item.trade_account), axisLabel: { rotate: 18, color: '#6b7280' } },
    yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
    series: [{ type: 'bar', data: list.map((item) => item[valueKey]), barMaxWidth: 34, itemStyle: { borderRadius: [8, 8, 0, 0], color: (params: any) => rankingPalette[params.dataIndex % rankingPalette.length] } }],
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
  };
}

function goDetail(tradeAccount: string) {
  router.push(`/customer-report/accounts/${tradeAccount}`);
}

function formatMoney(value?: number | null) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function formatInt(value?: number | null) {
  return Number(value || 0).toLocaleString('zh-CN');
}
function formatPercent(value?: number | null) {
  return `${Number(value || 0).toFixed(2)}%`;
}
function formatDate(value?: string | null) {
  return value ? dayjs(value).format('YYYY-MM-DD') : '--';
}
function formatTime(value?: string | null) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '--';
}
function profitType(value?: number | null) {
  if (Number(value || 0) > 0) return 'profit';
  if (Number(value || 0) < 0) return 'loss';
  return 'neutral';
}
function profitTypeClass(value?: number | null) {
  if (Number(value || 0) > 0) return 'profit-text';
  if (Number(value || 0) < 0) return 'loss-text';
  return '';
}

onMounted(loadAll);
</script>
