<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import ReviewEventTimeline from '@/components/ReviewEventTimeline.vue'
import ReviewStatusBadge from '@/components/ReviewStatusBadge.vue'
import { useReviewStore } from '@/stores/reviewStore'

const route = useRoute()
const store = useReviewStore()
const id = String(route.params.id)

onMounted(async () => {
  await store.load(id)
  store.connectEvents(id)
})

onBeforeUnmount(() => store.disconnect())
</script>

<template>
  <section class="page-grid">
    <div>
      <p class="eyebrow">评审进度</p>
      <h1>{{ id }}</h1>
      <p class="lead">通过后端 SSE 接口实时接收评审阶段事件。</p>
      <ReviewStatusBadge v-if="store.currentJob" :status="store.currentJob.status" />
      <p v-if="store.error" class="error">{{ store.error }}</p>
      <RouterLink v-if="store.currentJob?.status === 'COMPLETED'" class="button-link" :to="`/reviews/${id}/result`">
        查看结果
      </RouterLink>
    </div>

    <div class="panel">
      <ReviewEventTimeline :events="store.events" />
    </div>
  </section>
</template>
