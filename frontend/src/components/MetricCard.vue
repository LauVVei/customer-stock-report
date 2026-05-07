<template>
  <div :class="['section-card', 'metric-card', cardToneClass]">
    <div class="metric-card-header">
      <span class="metric-card-kicker">CORE METRIC</span>
      <span class="metric-card-label">{{ title }}</span>
    </div>
    <div :class="['metric-card-value', valueClass]">{{ displayValue }}</div>
    <div v-if="subtext" class="metric-card-subtext">{{ subtext }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  title: string;
  value: string | number;
  type?: 'profit' | 'loss' | 'neutral';
  subtext?: string;
}>();

const displayValue = computed(() => props.value ?? '--');
const valueClass = computed(() => {
  if (props.type === 'profit') return 'profit-text';
  if (props.type === 'loss') return 'loss-text';
  return '';
});
const cardToneClass = computed(() => {
  if (props.type === 'profit') return 'metric-card-profit';
  if (props.type === 'loss') return 'metric-card-loss';
  return 'metric-card-neutral';
});
</script>
