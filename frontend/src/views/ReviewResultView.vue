<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import FindingCard from '@/components/FindingCard.vue'
import FindingFilters from '@/components/FindingFilters.vue'
import ReportPreview from '@/components/ReportPreview.vue'
import ReviewStatusBadge from '@/components/ReviewStatusBadge.vue'
import { useReviewStore } from '@/stores/reviewStore'

const route = useRoute()
const store = useReviewStore()
const id = String(route.params.id)
const severity = ref('ALL')
const category = ref('ALL')
const copied = ref(false)

const filteredFindings = computed(() =>
  store.findings.filter((finding) => {
    const severityOk = severity.value === 'ALL' || finding.severity === severity.value
    const categoryOk = category.value === 'ALL' || finding.category === category.value
    return severityOk && categoryOk
  }),
)

onMounted(async () => {
  await store.load(id)
  if (store.currentJob?.status === 'COMPLETED') {
    await store.loadMarkdown(id)
  }
})

async function copy() {
  await store.copyReport()
  copied.value = true
  window.setTimeout(() => (copied.value = false), 1800)
}
</script>

<template>
  <section class="result-layout">
    <header>
      <div>
        <p class="eyebrow">评审结果</p>
        <h1>{{ id }}</h1>
      </div>
      <ReviewStatusBadge v-if="store.currentJob" :status="store.currentJob.status" />
    </header>

    <p v-if="store.error" class="error">{{ store.error }}</p>

    <section class="panel">
      <div class="panel-heading">
        <h2>发现项</h2>
        <FindingFilters v-model:severity="severity" v-model:category="category" />
      </div>
      <div class="finding-list">
        <FindingCard v-for="finding in filteredFindings" :key="`${finding.file}-${finding.category}-${finding.line}`" :finding="finding" />
        <p v-if="filteredFindings.length === 0" class="muted">当前筛选条件下没有发现项。</p>
      </div>
    </section>

    <section class="panel">
      <div class="panel-heading">
        <h2>Markdown 报告</h2>
        <button class="secondary" :disabled="!store.reportMarkdown" @click="copy">
          {{ copied ? '已复制' : '复制报告' }}
        </button>
      </div>
      <ReportPreview :markdown="store.reportMarkdown" />
    </section>
  </section>
</template>
