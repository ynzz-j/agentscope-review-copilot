<script setup lang="ts">
import type { ReviewFinding } from '@/types/review'

defineProps<{
  finding: ReviewFinding
}>()
</script>

<template>
  <article class="finding-card" :class="finding.severity.toLowerCase()">
    <header>
      <span class="severity">{{ finding.severity }}</span>
      <span class="category">{{ finding.category }}</span>
      <code>{{ finding.file }}<template v-if="finding.line">:{{ finding.line }}</template></code>
    </header>
    <p><strong>Evidence</strong>{{ finding.evidence }}</p>
    <p><strong>Impact</strong>{{ finding.impact }}</p>
    <p><strong>Suggestion</strong>{{ finding.suggestion }}</p>
    <footer>Confidence: {{ finding.confidence }}</footer>
  </article>
</template>

<style scoped>
.finding-card {
  display: grid;
  gap: 10px;
  padding: 16px;
  border: 1px solid #d9e2ec;
  border-left: 4px solid #7b8794;
  border-radius: 8px;
  background: #fff;
}

header {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.severity,
.category {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.severity {
  background: #edf2f7;
}

.category {
  background: #f0fdf4;
  color: #057a42;
}

.high,
.blocker {
  border-left-color: #d92d20;
}

.medium {
  border-left-color: #f79009;
}

.low {
  border-left-color: #667085;
}

p {
  margin: 0;
  color: #334155;
}

strong {
  display: block;
  color: #0f172a;
}

code {
  color: #475569;
  word-break: break-all;
}

footer {
  color: #64748b;
  font-size: 13px;
}
</style>
