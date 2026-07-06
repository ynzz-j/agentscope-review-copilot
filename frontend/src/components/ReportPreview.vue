<script setup lang="ts">
defineProps<{
  markdown: string
}>()
</script>

<template>
  <pre class="report-preview">{{ markdown || 'Report is not ready yet.' }}</pre>
</template>

<style scoped>
.report-preview {
  min-height: 320px;
  max-height: 520px;
  margin: 0;
  padding: 16px;
  overflow: auto;
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #0f172a;
  color: #e2e8f0;
  font-family: ui-monospace, SFMono-Regular, Consolas, 'Liberation Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
}
</style>
