<template>
  <a-drawer :open="open" width="960" :title="title" @close="$emit('close')">
    <div v-if="loading" class="meta-text">正在加载股票台账...</div>
    <template v-else-if="stock">
      <div class="section-card" style="margin-bottom: 16px">
        <div class="section-title">
          <h3>单股快照判断</h3>
        </div>
        <div class="chip-grid">
          <span class="summary-tag">最新日表 {{ formatDate(stock.latest_profit_date) }}</span>
          <span class="summary-tag">当前净利润 <span :class="profitClass(stock.holding_actual_profit)">{{ formatMoney(stock.holding_actual_profit) }}</span></span>
          <span class="summary-tag">日内 T 累计已实现收益 <span :class="profitClass(stock.intraday_t_profit)">{{ formatMoney(stock.intraday_t_profit) }}</span></span>
          <span class="summary-tag">今日日内T收益 <span :class="profitClass(stock.daily_intraday_t_profit)">{{ formatMoney(stock.daily_intraday_t_profit) }}</span></span>
          <span class="summary-tag">未匹配交易 {{ stock.unmatched_trade_count }}</span>
        </div>
      </div>

      <div class="chart-grid" style="margin-bottom: 16px">
        <ChartCard title="近 10 日当前净利润" :option="holdingProfitTrendOption" height="280px" />
        <ChartCard title="近 10 日当前净利润率" :option="holdingProfitRateTrendOption" height="280px" />
      </div>

      <div class="chart-grid" style="margin-bottom: 16px">
        <ChartCard title="近 10 日今日日内T收益" :option="dailyTTrendOption" height="280px" />
        <ChartCard title="近 10 日 T 成功 / 失败次数" :option="tSuccessFailOption" height="280px" />
      </div>

      <div class="section-card" style="margin-bottom: 16px">
        <div class="section-title"><h3>价格与成本结构</h3></div>
        <div class="drawer-grid">
          <Field label="股票代码" :value="stock.stock_code" />
          <Field label="股票名称" :value="stock.stock_name" />
          <Field label="持仓数量" :value="stock.holding_quantity" />
          <Field label="当前成本价" :value="formatMoney(stock.holding_cost_price)" />
          <Field label="初始成本价" :value="formatMoney(stock.init_cost_price)" />
          <Field label="最新价" :value="formatMoney(stock.latest_price)" />
          <Field label="最新价时间" :value="formatTime(stock.latest_price_time)" />
          <Field label="持仓市值" :value="formatMoney(stock.market_value)" />
          <Field label="账户可用资金" :value="formatMoney(stock.available_amount)" />
          <Field label="仓位占比" :value="formatPercent(stock.allocation_ratio)" />
          <Field label="最新价 - 当前成本价" :value="formatMoney(stock.latest_vs_current_cost_price_diff)" :class-name="profitClass(stock.latest_vs_current_cost_price_diff)" />
          <Field label="当前成本价 - 初始成本价" :value="formatMoney(stock.current_vs_init_cost_price_diff)" :class-name="profitClass(stock.current_vs_init_cost_price_diff)" />
        </div>
      </div>

      <div class="section-card" style="margin-bottom: 16px">
        <div class="section-title"><h3>收益口径对照</h3></div>
        <div class="drawer-grid">
          <Field label="当前净利润" :value="formatMoney(stock.holding_actual_profit)" :class-name="profitClass(stock.holding_actual_profit)" />
          <Field label="当前净利润率" :value="formatPercent(stock.holding_actual_profit_rate)" :class-name="profitClass(stock.holding_actual_profit_rate)" />
          <Field label="初始成本口径净利润" :value="formatMoney(stock.init_cost_holding_profit)" :class-name="profitClass(stock.init_cost_holding_profit)" />
          <Field label="初始成本口径净利润率" :value="formatPercent(stock.init_cost_holding_profit_rate)" :class-name="profitClass(stock.init_cost_holding_profit_rate)" />
          <Field label="日内 T 累计已实现收益" :value="formatMoney(stock.intraday_t_profit)" :class-name="profitClass(stock.intraday_t_profit)" />
          <Field label="今日日内T收益" :value="formatMoney(stock.daily_intraday_t_profit)" :class-name="profitClass(stock.daily_intraday_t_profit)" />
        </div>
      </div>

      <div class="section-card" style="margin-bottom: 16px">
        <div class="section-title"><h3>交易兑现表现</h3></div>
        <div class="drawer-grid">
          <Field label="累计成交笔数" :value="stock.total_trades" />
          <Field label="盈利笔数" :value="stock.win_trades" />
          <Field label="亏损 / 非盈利笔数" :value="stock.loss_trades" />
          <Field label="单股胜率" :value="formatPercent(stock.stock_win_rate)" />
          <Field label="买入次数" :value="stock.buy_count" />
          <Field label="卖出次数" :value="stock.sell_count" />
          <Field label="买入数量" :value="stock.buy_quantity" />
          <Field label="卖出数量" :value="stock.sell_quantity" />
          <Field label="买入金额" :value="formatMoney(stock.buy_amount)" />
          <Field label="卖出金额" :value="formatMoney(stock.sell_amount)" />
          <Field label="平均买入价" :value="formatMoney(stock.avg_buy_price)" />
          <Field label="平均卖出价" :value="formatMoney(stock.avg_sell_price)" />
          <Field label="总交易成本" :value="formatMoney(stock.total_trade_cost)" />
          <Field label="最近成交时间" :value="formatTime(stock.last_trade_time)" />
        </div>
      </div>

      <div class="section-card">
        <div class="section-title"><h3>未匹配交易与 T 统计</h3></div>
        <div class="drawer-grid">
          <Field label="未匹配交易数" :value="stock.unmatched_trade_count" />
          <Field label="未匹配买入开仓数" :value="stock.unmatched_buy_open_count" />
          <Field label="未匹配卖出开仓数" :value="stock.unmatched_sell_open_count" />
          <Field label="未匹配股数" :value="stock.unmatched_quantity" />
          <Field label="未匹配金额" :value="formatMoney(stock.unmatched_amount)" />
          <Field label="未匹配均价" :value="formatMoney(stock.unmatched_avg_price)" />
          <Field label="T 交易笔数" :value="stock.t_trade_count" />
          <Field label="平均单笔 T 收益" :value="formatMoney(stock.avg_t_profit)" :class-name="profitClass(stock.avg_t_profit)" />
          <Field label="最大单笔 T 收益" :value="formatMoney(stock.max_t_profit)" class-name="profit-text" />
          <Field label="最小单笔 T 收益" :value="formatMoney(stock.min_t_profit)" class-name="loss-text" />
          <Field label="最早未匹配时间" :value="formatTime(stock.first_unmatched_trade_time)" />
          <Field label="最近未匹配时间" :value="formatTime(stock.last_unmatched_trade_time)" />
        </div>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import dayjs from 'dayjs';
import ChartCard from '@/components/ChartCard.vue';
import { getStockDailyProfit } from '@/api/report';
import type { StockDailyPoint, StockDetail } from '@/types/report';

const props = defineProps<{ open: boolean; stock?: StockDetail | null; loading?: boolean }>();
defineEmits(['close']);

const trendSeries = ref<StockDailyPoint[]>([]);

const title = computed(() => {
  if (!props.stock) return '股票台账';
  return `${props.stock.stock_name} · ${props.stock.stock_code}`;
});

const holdingProfitTrendOption = computed(() => buildLineOption('近 10 日当前净利润', trendSeries.value, 'holding_actual_profit', '#c1121f'));
const holdingProfitRateTrendOption = computed(() => buildLineOption('近 10 日当前净利润率', trendSeries.value, 'holding_actual_profit_rate', '#277da1'));
const dailyTTrendOption = computed(() => buildBarOption('近 10 日今日日内T收益', trendSeries.value, 'daily_intraday_t_profit'));
const tSuccessFailOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { top: 24, textStyle: { color: '#6b7280' } },
  grid: { left: 50, right: 20, top: 60, bottom: 56 },
  xAxis: { type: 'category', data: trendSeries.value.map((item) => item.profit_date), axisLabel: { color: '#6b7280' } },
  yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
  series: [
    { name: 'T 成功', type: 'bar', stack: 't-trade', data: trendSeries.value.map((item) => Number(item.t_success_count || 0)), itemStyle: { color: '#c1121f', borderRadius: [6, 6, 0, 0] } },
    { name: 'T 失败', type: 'bar', stack: 't-trade', data: trendSeries.value.map((item) => Number(item.t_fail_count || 0)), itemStyle: { color: '#067647', borderRadius: [6, 6, 0, 0] } },
  ],
  title: { text: '近 10 日 T 成功 / 失败次数', left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
}));

watch(
  () => [props.open, props.stock?.trade_account, props.stock?.stock_code],
  async ([open, tradeAccount, stockCode]) => {
    if (!open || !tradeAccount || !stockCode) {
      trendSeries.value = [];
      return;
    }
    trendSeries.value = await getStockDailyProfit(String(tradeAccount), String(stockCode));
  },
  { immediate: true },
);

function buildLineOption(title: string, list: StockDailyPoint[], key: keyof StockDailyPoint, color: string) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 46, bottom: 56 },
    xAxis: { type: 'category', data: list.map((item) => item.profit_date), axisLabel: { color: '#6b7280' } },
    yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
    series: [{ type: 'line', smooth: true, data: list.map((item) => Number(item[key] || 0)), symbol: 'circle', symbolSize: 7, lineStyle: { width: 3, color }, itemStyle: { color }, areaStyle: { color: `${color}22` } }],
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
  };
}

function buildBarOption(title: string, list: StockDailyPoint[], key: keyof StockDailyPoint) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 46, bottom: 56 },
    xAxis: { type: 'category', data: list.map((item) => item.profit_date), axisLabel: { color: '#6b7280' } },
    yAxis: { type: 'value', axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: 'rgba(29, 39, 53, 0.08)' } } },
    series: [{ type: 'bar', data: list.map((item) => Number(item[key] || 0)), barMaxWidth: 26, itemStyle: { borderRadius: [8, 8, 0, 0], color: (params: any) => Number(params.value || 0) >= 0 ? '#c1121f' : '#067647' } }],
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontFamily: 'STZhongsong, serif', color: '#1f2937' } },
  };
}

function formatMoney(value?: number | string | null) {
  if (value === null || value === undefined || value === '') return '--';
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function formatPercent(value?: number | string | null) {
  if (value === null || value === undefined || value === '') return '--';
  return `${Number(value).toFixed(2)}%`;
}
function formatDate(value?: string | null) {
  return value ? dayjs(value).format('YYYY-MM-DD') : '--';
}
function formatTime(value?: string | null) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '--';
}
function profitClass(value?: number | string | null) {
  const num = Number(value ?? 0);
  if (num > 0) return 'profit-text';
  if (num < 0) return 'loss-text';
  return '';
}
</script>

<script lang="ts">
import { defineComponent, h } from 'vue';

const Field = defineComponent({
  name: 'DrawerField',
  props: { label: { type: String, required: true }, value: { type: [String, Number], default: '--' }, className: { type: String, default: '' } },
  setup(props) {
    return () => h('div', { class: 'drawer-field' }, [h('span', { class: 'label' }, props.label), h('span', { class: props.className }, String(props.value ?? '--'))]);
  },
});

export default { components: { Field } };
</script>
