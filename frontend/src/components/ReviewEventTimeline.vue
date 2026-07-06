<script setup lang="ts">
import { eventTypeLabel } from '@/i18n/zhCN'
import type { ReviewEvent } from '@/types/review'

defineProps<{
  events: ReviewEvent[]
}>()
</script>

<template>
  <ol class="timeline">
    <li v-for="event in events" :key="`${event.timestamp}-${event.type}`">
      <span class="dot"></span>
      <div>
        <strong>{{ eventTypeLabel(event.type) }}</strong>
        <p>{{ event.message }}</p>
        <time>{{ new Date(event.timestamp).toLocaleString('zh-CN') }}</time>
      </div>
    </li>
  </ol>
</template>

<style scoped>
.timeline {
  display: grid;
  gap: 14px;
  margin: 0;
  padding: 0;
  list-style: none;
}

li {
  display: grid;
  grid-template-columns: 14px 1fr;
  gap: 10px;
}

.dot {
  width: 10px;
  height: 10px;
  margin-top: 7px;
  border-radius: 999px;
  background: #10b981;
}

strong {
  color: #0f172a;
}

p {
  margin: 3px 0;
  color: #475569;
}

time {
  color: #94a3b8;
  font-size: 12px;
}
</style>
