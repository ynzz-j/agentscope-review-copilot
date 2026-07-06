# 开发状态

本仓库根据 Obsidian 计划生成：

`D:\Obsidian\KnowledgeBase\00-Inbox\AgentScope-Java 2.0.0-RC4 Review Copilot 自动开发计划.md`

## 当前状态

- 本地路径：`D:\workspace\whd\ynzz\articles\agentscope-review-copilot`
- Git 分支：`main`
- GitHub 仓库：`https://github.com/ynzz-j/agentscope-review-copilot`
- 基础框架 tag：`base-framework`
- 后端包根：`com.ynzz.agentscope.reviewcopilot`
- Maven `groupId`：`com.ynzz`
- AgentScope-Java 版本：`2.0.0-RC4`
- 前端技术栈：Vue 3 + TypeScript + Vite + Router + Pinia
- 默认界面语言：简体中文

## 已实现

- Spring Boot WebFlux 后端。
- Vue 3 前端。
- `/health`。
- `POST /api/reviews`。
- `GET /api/reviews/{id}`。
- `GET /api/reviews/{id}/events`。
- `GET /api/reviews/{id}/report.md`。
- 使用固定参数 `git diff` 命令读取本地 Git diff。
- 基于仓库根目录检查的文件上下文读取。
- 针对测试缺口、异常吞掉、静态可变状态、API 校验缺失、Agent 工具边界的规则发现项。
- `backend/data/jobs` 下的 JSON 任务持久化。
- `backend/data/reports` 下的 Markdown 报告。
- `ModelFactory` 中明确的无默认模型行为。
- 显式 provider 配置后的 AgentScope-Java 模型评审调用。
- AgentScope toolkit 注册和 middleware 扩展点。
- 可审计的模型调用 middleware 和可替换 audit sink。
- 中文默认的前端创建、进度、结果页面。
- 中文默认的 Markdown 评审报告。
- `scripts/` 下的验证和 smoke test 脚本。

## 验证命令

运行完整本地验证：

```powershell
.\scripts\verify.ps1 -SkipFrontendInstall
```

运行端到端 smoke review：

```powershell
.\scripts\smoke-review.ps1
```

## 已知边界

- 未配置模型 provider 时，当前确定性规则检查仍可运行，不会默认选择 provider。
- 配置模型 provider 后，Git diff 和源码上下文会发送到所选模型服务。
- 项目不会修改被评审源码。

## 章节分支

| 章节 | 分支 | 完成 tag |
|---|---|---|
| 基础框架 | `main` | `base-framework` |
| 第 01 章 | `chapter/01-skeleton` | `chapter-01-complete` |
| 第 02 章 | `chapter/02-streaming-review` | `chapter-02-complete` |
| 第 03 章 | `chapter/03-review-tools` | `chapter-03-complete` |
| 第 04 章 | `chapter/04-review-state` | `chapter-04-complete` |
| 第 05 章 | `chapter/05-permission-boundary` | `chapter-05-complete` |
| 第 06 章 | `chapter/06-audit-middleware` | `chapter-06-complete` |
| 第 07 章 | `chapter/07-ui-and-release` | `chapter-07-complete` |
