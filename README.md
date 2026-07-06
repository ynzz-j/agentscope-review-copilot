# AgentScope Review Copilot

[English](README.en.md)

AgentScope Review Copilot 是 **AgentScope-Java 2.0.0-RC4** 的项目式入门示例。它围绕一个可运行的本地 Git diff 代码评审助手展开：读取本地 diff，生成结构化工程质量发现项，通过 SSE 推送进度，并输出可复制的 Markdown 评审报告。

## 技术栈

- 后端：JDK 17+、Maven、Spring Boot WebFlux、AgentScope-Java 2.0.0-RC4、Reactor、JGit
- 前端：Vue 3、TypeScript、Vite、Vue Router、Pinia、Vitest、ESLint、Prettier
- Java 包根：`com.ynzz.agentscope.reviewcopilot`
- Maven `groupId`：`com.ynzz`

## 快速启动

启动后端：

```bash
cd backend
mvn spring-boot:run
```

健康检查：

```bash
curl http://localhost:8080/health
```

启动前端：

```bash
cd frontend
npm install
npm run dev
```

打开 `http://localhost:5173`。

## 模型配置

本项目**不会指定默认模型提供商**。

模型层保留 `ModelFactory` / `AgentFactory` 抽象。未配置 provider 时，后端不会默认选择 DashScope 或其他模型，确定性规则检查仍会执行，报告中会明确说明模型 provider 尚未配置。

显式配置 provider 后，评审流水线会真实调用 AgentScope-Java 模型，并把模型输出解析成结构化发现项。支持的 provider：

- `dashscope` / `dsp`
- `openai`
- `deepseek`
- `anthropic`
- `gemini`
- `ollama`
- `registry`

示例：

```yaml
review-copilot:
  model:
    provider: dsp
    model-name: qwen-plus
    api-key: ${DASHSCOPE_API_KEY}
```

配置模型后，Git diff 和源码上下文会发送到你选择的模型服务。

## API

- `GET /health`
- `POST /api/reviews`
- `GET /api/reviews/{id}`
- `GET /api/reviews/{id}/events`
- `GET /api/reviews/{id}/report.md`

创建评审任务示例：

```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -d "{\"repoPath\":\"D:\\workspace\\demo-project\",\"diffMode\":\"WORKING_TREE\",\"sessionId\":\"demo-session\"}"
```

## 评审范围

入门项目聚焦工程质量：

- 缺陷风险
- 可维护性
- 并发与状态
- API 契约
- 测试缺口
- Agent 边界

每条发现项包含严重级别、类型、文件、行号、证据、影响、建议和置信度。

## 安全边界

- 被评审仓库只读。
- 报告只写入 Review Copilot 自身的 `backend/data/reports` 目录。
- 后端不暴露任意 shell 执行接口。
- 文件读取经过仓库根目录路径检查。
- `.env`、`.ssh`、常见凭据文件等敏感路径会被阻止读取。

## 分支与 Tag 策略

| 阶段 | 分支 | 完成 tag | 说明 |
|---|---|---|---|
| 基础框架 | `main` | `base-framework` | 可运行基础框架 |
| 第 01 章 | `chapter/01-skeleton` | `chapter-01-complete` | Spring Boot + Vue 脚手架 |
| 第 02 章 | `chapter/02-streaming-review` | `chapter-02-complete` | SSE 评审进度 |
| 第 03 章 | `chapter/03-review-tools` | `chapter-03-complete` | Git diff 与评审工具 |
| 第 04 章 | `chapter/04-review-state` | `chapter-04-complete` | 状态与报告存储 |
| 第 05 章 | `chapter/05-permission-boundary` | `chapter-05-complete` | 只读权限边界 |
| 第 06 章 | `chapter/06-audit-middleware` | `chapter-06-complete` | AgentScope middleware 审计 |
| 第 07 章 | `chapter/07-ui-and-release` | `chapter-07-complete` | Vue 页面完善与 GitHub 发布 |

章节开发流程：

```bash
git checkout main
git pull --ff-only
git checkout -b chapter/NN-topic
# implement chapter
git add .
git commit -m "feat: implement chapter NN topic"
git checkout main
git merge --no-ff chapter/NN-topic
git tag chapter-NN-complete
git push origin main
git push origin chapter/NN-topic
git push origin chapter-NN-complete
```

## 验证

后端：

```bash
cd backend
mvn test
```

前端：

```bash
cd frontend
npm run type-check
npm run test:unit -- --run
npm run build
npm run lint
```

完整本地验证：

```powershell
.\scripts\verify.ps1 -SkipFrontendInstall
.\scripts\smoke-review.ps1
```
