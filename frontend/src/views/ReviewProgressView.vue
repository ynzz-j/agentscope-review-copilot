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
      <p class="eyebrow">Review progress</p>
      <h1>{{ id }}</h1>
      <p class="lead">Streaming review events from the backend SSE endpoint.</p>
      <ReviewStatusBadge v-if="store.currentJob" :status="store.currentJob.status" />
      <p v-if="store.error" class="error">{{ store.error }}</p>
      <RouterLink v-if="store.currentJob?.status === 'COMPLETED'" class="button-link" :to="`/reviews/${id}/result`">
        Open result
      </RouterLink>
    </div>

    <div class="panel">
      <ReviewEventTimeline :events="store.events" />
    </div>
  </section>
</template>
