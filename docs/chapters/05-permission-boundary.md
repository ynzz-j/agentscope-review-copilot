# 第 05 章 - 权限边界

## 目标

让入门项目默认安全：可以读取本地 Git diff，可以写入自身报告，但不能修改被评审仓库。

## 边界规则

- 被评审仓库只读。
- `GitDiffTool` 只暴露固定的 `git diff` 命令。
- `FileContextTool` 只读取解析后仓库根目录内的文件。
- 敏感文件和敏感目录禁止进入上下文。
- 任务 JSON 只存储在 Review Copilot 配置的 job 目录。
- Markdown 报告只存储在 Review Copilot 配置的 report 目录。
- 评审任务 ID 和报告 ID 必须是简单安全 ID，不能是路径。

## 为什么重要

本系列会让 AgentScope-Java 接触本地开发文件。项目必须将副作用控制在可见且狭窄的范围内：允许生成报告，不允许改源码。

## 验收

- `../` 路径会被拒绝。
- `.env` 等敏感路径不会被读取。
- 不安全任务 ID 不能越过 job 目录。
- 不安全报告 ID 不能越过 report 目录。
- smoke test 确认 demo 仓库在评审后仍只有预期测试 diff。
