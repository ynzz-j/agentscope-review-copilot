package com.ynzz.agentscope.reviewcopilot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ynzz.agentscope.reviewcopilot.factory.AgentFactory;
import com.ynzz.agentscope.reviewcopilot.model.DiffMode;
import com.ynzz.agentscope.reviewcopilot.model.ReviewCategory;
import com.ynzz.agentscope.reviewcopilot.model.ReviewConfidence;
import com.ynzz.agentscope.reviewcopilot.model.ReviewFinding;
import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import com.ynzz.agentscope.reviewcopilot.model.ReviewRequest;
import com.ynzz.agentscope.reviewcopilot.model.ReviewSeverity;
import com.ynzz.agentscope.reviewcopilot.tool.GitDiffTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

class ModelReviewServiceTest {

    @Test
    void callsAgentScopeAgentAndParsesStructuredFindings() {
        AgentFactory agentFactory = mock(AgentFactory.class);
        ReActAgent agent = mock(ReActAgent.class);
        when(agentFactory.createModelOnlyReviewer("session-1")).thenReturn(agent);
        when(agent.call(anyString(), any(RuntimeContext.class))).thenReturn(Mono.just(new UserMessage("""
                ```json
                {
                  "summary": "模型发现异常处理缺少测试。",
                  "findings": [
                    {
                      "severity": "HIGH",
                      "category": "test-gap",
                      "file": "src/main/java/demo/DemoService.java",
                      "line": 12,
                      "evidence": "catch 分支新增 fallback 逻辑，但 diff 中没有对应测试。",
                      "impact": "异常路径可能在生产环境返回错误结果且无法被回归测试捕获。",
                      "suggestion": "为异常分支补充单元测试，覆盖 fallback 行为。",
                      "confidence": "HIGH"
                    }
                  ]
                }
                ```
                """)));
        ModelReviewService service = new ModelReviewService(agentFactory);

        ModelReviewResult result = service.review(
                job(),
                new GitDiffTool.GitDiffResult(
                        Path.of("D:/demo"),
                        "diff --git a/src/main/java/demo/DemoService.java b/src/main/java/demo/DemoService.java",
                        List.of("src/main/java/demo/DemoService.java"),
                        0,
                        1,
                        0),
                Map.of("src/main/java/demo/DemoService.java", "class DemoService {}"),
                List.of(ruleFinding()));

        assertThat(result.summary()).isEqualTo("模型发现异常处理缺少测试。");
        assertThat(result.findings())
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.severity()).isEqualTo(ReviewSeverity.HIGH);
                    assertThat(finding.category()).isEqualTo(ReviewCategory.TEST_GAP);
                    assertThat(finding.file()).isEqualTo("src/main/java/demo/DemoService.java");
                    assertThat(finding.line()).isEqualTo(12);
                    assertThat(finding.confidence()).isEqualTo(ReviewConfidence.HIGH);
                });

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RuntimeContext> runtimeContext = ArgumentCaptor.forClass(RuntimeContext.class);
        verify(agent).call(prompt.capture(), runtimeContext.capture());
        assertThat(prompt.getValue()).contains("只输出一个 JSON 对象", "Git diff", "源码上下文");
        assertThat(runtimeContext.getValue().getSessionId()).isEqualTo("session-1");
    }

    private ReviewJob job() {
        return ReviewJob.created(
                new ReviewRequest(
                        "D:/demo",
                        DiffMode.WORKING_TREE,
                        null,
                        "session-1",
                        List.of(ReviewCategory.BUG_RISK, ReviewCategory.TEST_GAP)),
                "D:/demo");
    }

    private ReviewFinding ruleFinding() {
        return new ReviewFinding(
                ReviewSeverity.MEDIUM,
                ReviewCategory.TEST_GAP,
                "src/main/java/demo/DemoService.java",
                null,
                "生产代码有改动，但没有测试文件改动。",
                "回归风险升高。",
                "补充测试。",
                ReviewConfidence.MEDIUM);
    }
}
