# 第 02 章 - 流式评审进度

## 目标

实现评审任务 API，并通过 Server-Sent Events 将评审进度推送到 Vue 进度页。

## 后端范围

- `POST /api/reviews` 创建评审任务，并异步启动评审流水线。
- `GET /api/reviews/{id}` 返回持久化任务状态。
- `GET /api/reviews/{id}/events` 返回 `text/event-stream` 事件流。
- `ReviewEventPublisher` 保存每个任务的事件历史，迟到订阅者也能看到完整时间线。
- `JOB_COMPLETED` 和 `JOB_FAILED` 会关闭事件流，并在后续订阅时以有限历史回放。

## 事件契约

进度页以事件类型作为状态来源：

- `JOB_CREATED`
- `DIFF_LOADED`
- `FILE_CONTEXT_LOADED`
- `RULE_CHECK_DONE`
- `MODEL_REVIEWING`
- `FINDING_GENERATED`
- `REPORT_READY`
- `JOB_COMPLETED`
- `JOB_FAILED`

每个事件包含 `jobId`、`type`、`message`、`payload` 和 `timestamp`。

## 前端范围

- `ReviewCreateView` 提交 `ReviewRequest`。
- `ReviewProgressView` 订阅 `/api/reviews/{id}/events`。
- `reviewStore` 管理 EventSource 生命周期、事件追加、最终任务加载和结果页跳转。

## 验收

- 用户创建评审后能立即看到阶段进度。
- 刷新或重新打开已完成任务的进度页，仍能看到完整时间线。
- 未配置模型 provider 时，事件流会明确提示并在最终报告中保留说明。
- `ReviewEventPublisherTest` 覆盖历史回放和实时事件发送。
