export type DiffMode = 'WORKING_TREE' | 'STAGED' | 'BASE_REF'

export type ReviewStatus = 'CREATED' | 'RUNNING' | 'COMPLETED' | 'FAILED'

export type ReviewSeverity = 'BLOCKER' | 'HIGH' | 'MEDIUM' | 'LOW'

export type ReviewCategory =
  | 'bug-risk'
  | 'maintainability'
  | 'concurrency'
  | 'api-contract'
  | 'test-gap'
  | 'agent-boundary'

export type ReviewConfidence = 'HIGH' | 'MEDIUM' | 'LOW'

export interface ReviewRequest {
  repoPath: string
  diffMode: DiffMode
  baseRef?: string
  sessionId?: string
  focusCategories: ReviewCategory[]
}

export interface ReviewFinding {
  severity: ReviewSeverity
  category: ReviewCategory
  file: string
  line?: number
  evidence: string
  impact: string
  suggestion: string
  confidence: ReviewConfidence
}

export interface ReviewJob {
  id: string
  sessionId: string
  repoPath: string
  diffMode: DiffMode
  baseRef?: string
  focusCategories: ReviewCategory[]
  status: ReviewStatus
  createdAt: string
  startedAt?: string
  finishedAt?: string
  findings: ReviewFinding[]
  reportPath?: string
  errorMessage?: string
}

export interface ReviewEvent {
  jobId: string
  type: string
  message: string
  payload: Record<string, unknown>
  timestamp: string
}
