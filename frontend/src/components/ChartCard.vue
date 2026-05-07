<template>
  <div class="section-card">
    <div class="section-title">
      <h3>{{ title }}</h3>
      <slot name="extra"></slot>
    </div>
    <div ref="chartRef" :style="{ height }"></div>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts';
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';

const props = defineProps<{
  title: string;
  option: any;
  height?: string;
}>();

const chartRef = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | undefined;

function render() {
  if (!chartRef.value) return;
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }
  chart.setOption(props.option, true);
}

onMounted(() => {
  render();
  window.addEventListener('resize', render);
});

watch(() => props.option, render, { deep: true });

onBeforeUnmount(() => {
  window.removeEventListener('resize', render);
  chart?.dispose();
});
</script>

<script lang="ts">
export default {
  name: 'ChartCard',
};
</script>
