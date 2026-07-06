# 第 04 章 - 评审状态

## 目标

持久化评审任务、会话元数据、发现项和 Markdown 报告，使 UI 刷新后仍能恢复状态。

## 后端范围

- `ReviewJob` 表示任务生命周期状态。
- `JsonFileReviewJobStore` 将任务 JSON 保存到 `backend/data/jobs`。
- `ReviewReportService` 将 Markdown 报告写入 `backend/data/reports`。
- `GET /api/reviews/{id}` 重新加载持久化任务状态。
- `GET /api/reviews/{id}/report.md` 返回已生成的 Markdown 报告。

## 状态契约

任务记录保留：

- id
- sessionId
- repoPath
- diffMode
- baseRef
- focusCategories
- status
- createdAt
- startedAt
- finishedAt
- findings
- reportPath
- errorMessage

## 前端范围

`reviewStore` 保存当前任务、进度事件、发现项和报告 Markdown。结果页可按 `id` 重新加载任务和报告，因此已完成评审在页面跳转或刷新后仍可查看。

## 验收

- 已完成任务可保存为 JSON 并再次读取。
- Markdown 报告以 UTF-8 写入。
- 报告输出留在 Review Copilot 自身报告目录。
- UI 能加载已完成任务并复制 Markdown 报告，不需要重新执行评审。
