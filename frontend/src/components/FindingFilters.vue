<script setup lang="ts">
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
</script>

<template>
  <div class="filters">
    <label>
      Severity
      <select :value="severity" @change="$emit('update:severity', ($event.target as HTMLSelectElement).value)">
        <option v-for="item in severities" :key="item" :value="item">{{ item }}</option>
      </select>
    </label>
    <label>
      Category
      <select :value="category" @change="$emit('update:category', ($event.target as HTMLSelectElement).value)">
        <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
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
