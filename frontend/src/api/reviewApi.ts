import type { ReviewEvent, ReviewJob, ReviewRequest } from '@/types/review'

async function parseResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(await response.text())
  }
  return (await response.json()) as T
}

export async function createReview(request: ReviewRequest): Promise<ReviewJob> {
  const response = await fetch('/api/reviews', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  return parseResponse<ReviewJob>(response)
}

export async function loadReview(id: string): Promise<ReviewJob> {
  const response = await fetch(`/api/reviews/${encodeURIComponent(id)}`)
  return parseResponse<ReviewJob>(response)
}

export async function loadReport(id: string): Promise<string> {
  const response = await fetch(`/api/reviews/${encodeURIComponent(id)}/report.md`)
  if (!response.ok) {
    throw new Error(await response.text())
  }
  return response.text()
}

export function connectReviewEvents(
  id: string,
  handlers: {
    onEvent: (event: ReviewEvent) => void
    onError: (error: Event) => void
  },
): EventSource {
  const source = new EventSource(`/api/reviews/${encodeURIComponent(id)}/events`)
  source.onmessage = (message) => {
    handlers.onEvent(JSON.parse(message.data) as ReviewEvent)
  }
  source.onerror = handlers.onError

  for (const type of [
    'JOB_CREATED',
    'DIFF_LOADED',
    'FILE_CONTEXT_LOADED',
    'RULE_CHECK_DONE',
    'MODEL_REVIEWING',
    'FINDING_GENERATED',
    'REPORT_READY',
    'JOB_COMPLETED',
    'JOB_FAILED',
  ]) {
    source.addEventListener(type, (message) => {
      handlers.onEvent(JSON.parse((message as MessageEvent).data) as ReviewEvent)
    })
  }

  return source
}
