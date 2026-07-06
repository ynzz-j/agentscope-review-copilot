<script setup lang="ts">
import { statusLabels } from '@/i18n/zhCN'
import type { ReviewStatus } from '@/types/review'

defineProps<{
  status: ReviewStatus
}>()
</script>

<template>
  <span class="status-badge" :class="status.toLowerCase()">{{ statusLabels[status] }}</span>
</template>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: #edf2f7;
  color: #2d3748;
  font-size: 12px;
  font-weight: 700;
}

.running {
  background: #e8f4ff;
  color: #1455a8;
}

.completed {
  background: #e8f7ef;
  color: #057a42;
}

.failed {
  background: #ffe8e8;
  color: #b42318;
}
</style>
