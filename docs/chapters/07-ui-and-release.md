# 第 07 章 - UI 与发布

## 目标

完善 Vue 界面、报告复制流程、README、验证脚本、分支策略和 GitHub 发布结构。

## 前端范围

- `ReviewCreateView` 根据本地仓库路径创建评审任务。
- `ReviewProgressView` 渲染 SSE 进度事件。
- `ReviewResultView` 渲染发现项、筛选器、Markdown 报告预览和一键复制。
- `reviewStore` 管理 API 调用、EventSource 生命周期、发现项、报告 Markdown 和复制行为。
- Vite dev proxy 将 `/api` 和 `/health` 转发到后端。
- 前端默认界面语言为简体中文。

## 发布范围

- `README.md` 默认显示中文，说明本地启动、模型配置、API、安全边界和分支/tag 策略。
- `README.en.md` 保留英文说明。
- `scripts/verify.ps1` 运行后端和前端验证。
- `scripts/smoke-review.ps1` 执行端到端本地 diff 评审，并确认被评审 demo 仓库没有被工具修改。
- 完成 tag 从 `base-framework` 到 `chapter-07-complete` 均已存在。

## 验收

- `npm run type-check` 通过。
- `npm run test:unit -- --run` 通过。
- `npm run build` 通过。
- `npm run lint` 通过。
- 报告预览有已测试的空状态和 Markdown 渲染状态。
- 仓库已推送到 GitHub。
