import type {
  DiffMode,
  ReviewCategory,
  ReviewConfidence,
  ReviewSeverity,
  ReviewStatus,
} from '@/types/review'

export const statusLabels: Record<ReviewStatus, string> = {
  CREATED: '已创建',
  RUNNING: '评审中',
  COMPLETED: '已完成',
  FAILED: '失败',
}

export const severityLabels: Record<ReviewSeverity, string> = {
  BLOCKER: '阻断',
  HIGH: '高',
  MEDIUM: '中',
  LOW: '低',
}

export const confidenceLabels: Record<ReviewConfidence, string> = {
  HIGH: '高',
  MEDIUM: '中',
  LOW: '低',
}

export const categoryLabels: Record<ReviewCategory, string> = {
  'bug-risk': '缺陷风险',
  maintainability: '可维护性',
  concurrency: '并发与状态',
  'api-contract': 'API 契约',
  'test-gap': '测试缺口',
  'agent-boundary': 'Agent 边界',
}

export const diffModeLabels: Record<DiffMode, string> = {
  WORKING_TREE: '工作区改动',
  STAGED: '暂存区改动',
  BASE_REF: '与基准分支对比',
}

const eventTypeLabels: Record<string, string> = {
  JOB_CREATED: '任务已创建',
  DIFF_LOADED: 'Git diff 已读取',
  FILE_CONTEXT_LOADED: '源码上下文已读取',
  RULE_CHECK_DONE: '规则检查已完成',
  MODEL_REVIEWING: '模型评审阶段',
  FINDING_GENERATED: '已生成发现项',
  REPORT_READY: '报告已生成',
  JOB_COMPLETED: '任务已完成',
  JOB_FAILED: '任务失败',
}

export function eventTypeLabel(type: string) {
  return eventTypeLabels[type] ?? type
}
