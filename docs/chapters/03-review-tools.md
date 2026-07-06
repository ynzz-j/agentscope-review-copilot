# 第 03 章 - 评审工具

## 目标

实现 AgentScope-Java 入门项目使用的本地评审工具集。

## 工具范围

- `GitDiffTool` 以只读方式读取本地 Git diff。
- `FileContextTool` 从变更文件读取受限 UTF-8 文本上下文。
- `RuleCheckTool` 将确定性工程质量检查转换为结构化 `ReviewFinding`。
- `ReportTool` 渲染最终 Markdown 报告。

## 安全形态

工具层不提供通用命令执行接口。`GitDiffTool` 只构造固定参数的 `git diff` 进程，拒绝以选项开头的 `baseRef`，并只返回 diff 文本和变更文件元数据。

文件上下文读取必须经过 `ReviewPermissionPolicy` 和 `ReviewPathGuard`，因此 `.env` 等敏感文件会被跳过，不会进入模型上下文或报告。

## Finding 契约

每条 `ReviewFinding` 包含：

- 严重级别
- 类型
- 文件
- 行号
- 证据
- 影响
- 建议
- 置信度

## 验收

- 能从本地 Git 仓库读取工作区 diff。
- 上下文会按配置的行数上限截断。
- 敏感文件不会被读取到上下文中。
- focus category 能将规则检查收敛到指定评审重点。
