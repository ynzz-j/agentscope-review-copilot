# 第 06 章 - 审计 Middleware

## 目标

增加 AgentScope middleware 层，用于审计模型调用、工具数量、耗时和错误，同时不把评审逻辑绑定到具体模型 provider。

## 后端范围

- `AuditingMiddleware` 实现 AgentScope-Java `MiddlewareBase`。
- `AuditRecord` 记录模型调用状态、Agent 名称、消息数、工具数、耗时、错误文本和时间戳。
- `ReviewAuditSink` 隔离审计记录的写入目标。
- `LoggingReviewAuditSink` 是入门项目默认 sink。
- `AgentFactory` 装配 AgentScope toolkit 和 middleware，但模型 provider 仍必须显式配置。

## 模型边界

项目仍不选择默认 provider。`ModelFactory` 会在实际选择 DashScope、OpenAI、Anthropic、Gemini、Ollama 或其他 RC4 支持方式前返回清晰配置错误。

## 验收

- 成功模型调用会产生成功审计记录。
- 失败模型调用会产生失败审计记录，并包含错误文本。
- 缺少模型 provider 是显式配置状态，不会隐式选择默认值。
- 未配置模型时，确定性规则检查仍可运行。
