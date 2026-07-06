<script setup lang="ts">
import { categoryLabels, severityLabels } from '@/i18n/zhCN'
import type { ReviewCategory, ReviewSeverity } from '@/types/review'

defineProps<{
  severity: string
  category: string
}>()

defineEmits<{
  'update:severity': [value: string]
  'update:category': [value: string]
}>()

const severities: Array<ReviewSeverity | 'ALL'> = ['ALL', 'BLOCKER', 'HIGH', 'MEDIUM', 'LOW']
const categories: Array<ReviewCategory | 'ALL'> = [
  'ALL',
  'bug-risk',
  'maintainability',
  'concurrency',
  'api-contract',
  'test-gap',
  'agent-boundary',
]

function severityLabel(value: ReviewSeverity | 'ALL') {
  return value === 'ALL' ? '全部级别' : severityLabels[value]
}

function categoryLabel(value: ReviewCategory | 'ALL') {
  return value === 'ALL' ? '全部类型' : categoryLabels[value]
}
</script>

<template>
  <div class="filters">
    <label>
      严重级别
      <select :value="severity" @change="$emit('update:severity', ($event.target as HTMLSelectElement).value)">
        <option v-for="item in severities" :key="item" :value="item">{{ severityLabel(item) }}</option>
      </select>
    </label>
    <label>
      类型
      <select :value="category" @change="$emit('update:category', ($event.target as HTMLSelectElement).value)">
        <option v-for="item in categories" :key="item" :value="item">{{ categoryLabel(item) }}</option>
      </select>
    </label>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

label {
  display: grid;
  gap: 6px;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

select {
  min-width: 180px;
  min-height: 38px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: #fff;
  color: #0f172a;
}
</style>
