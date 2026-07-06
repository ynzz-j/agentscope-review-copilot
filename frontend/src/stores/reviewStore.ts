import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { connectReviewEvents, createReview, loadReport, loadReview } from '@/api/reviewApi'
import type { ReviewCategory, ReviewEvent, ReviewFinding, ReviewJob, ReviewRequest } from '@/types/review'

export const reviewCategories: ReviewCategory[] = [
  'bug-risk',
  'maintainability',
  'concurrency',
  'api-contract',
  'test-gap',
  'agent-boundary',
]

export const useReviewStore = defineStore('review', () => {
  const currentJob = ref<ReviewJob | null>(null)
  const events = ref<ReviewEvent[]>([])
  const reportMarkdown = ref('')
  const loading = ref(false)
  const error = ref('')
  let eventSource: EventSource | null = null

  const findings = computed<ReviewFinding[]>(() => currentJob.value?.findings ?? [])

  async function create(request: ReviewRequest) {
    loading.value = true
    error.value = ''
    events.value = []
    reportMarkdown.value = ''
    try {
      currentJob.value = await createReview(request)
      return currentJob.value
    } catch (caught) {
      error.value = caught instanceof Error ? caught.message : String(caught)
      throw caught
    } finally {
      loading.value = false
    }
  }

  async function load(id: string) {
    loading.value = true
    error.value = ''
    try {
      currentJob.value = await loadReview(id)
      return currentJob.value
    } catch (caught) {
      error.value = caught instanceof Error ? caught.message : String(caught)
      throw caught
    } finally {
      loading.value = false
    }
  }

  function connectEvents(id: string) {
    eventSource?.close()
    eventSource = connectReviewEvents(id, {
      onEvent(event) {
        if (!events.value.some((existing) => existing.timestamp === event.timestamp && existing.type === event.type)) {
          events.value.push(event)
        }
        if (event.type === 'JOB_COMPLETED' || event.type === 'JOB_FAILED') {
          void load(id)
          eventSource?.close()
          eventSource = null
        }
      },
      onError() {
        error.value = 'SSE 连接已中断。请重新加载任务以刷新状态。'
      },
    })
  }

  async function loadMarkdown(id: string) {
    loading.value = true
    error.value = ''
    try {
      reportMarkdown.value = await loadReport(id)
      return reportMarkdown.value
    } catch (caught) {
      error.value = caught instanceof Error ? caught.message : String(caught)
      throw caught
    } finally {
      loading.value = false
    }
  }

  async function copyReport() {
    if (!reportMarkdown.value) return
    await navigator.clipboard.writeText(reportMarkdown.value)
  }

  function disconnect() {
    eventSource?.close()
    eventSource = null
  }

  return {
    currentJob,
    events,
    reportMarkdown,
    loading,
    error,
    findings,
    create,
    load,
    connectEvents,
    loadMarkdown,
    copyReport,
    disconnect,
  }
})
