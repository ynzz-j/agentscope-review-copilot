package com.ynzz.agentscope.reviewcopilot.service;

import com.ynzz.agentscope.reviewcopilot.factory.AgentFactory;
import com.ynzz.agentscope.reviewcopilot.factory.ModelFactory;
import com.ynzz.agentscope.reviewcopilot.model.ReviewEvent;
import com.ynzz.agentscope.reviewcopilot.model.ReviewEventType;
import com.ynzz.agentscope.reviewcopilot.model.ReviewFinding;
import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import com.ynzz.agentscope.reviewcopilot.model.ReviewReport;
import com.ynzz.agentscope.reviewcopilot.model.ReviewRequest;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPathGuard;
import com.ynzz.agentscope.reviewcopilot.store.ReviewJobStore;
import com.ynzz.agentscope.reviewcopilot.tool.FileContextTool;
import com.ynzz.agentscope.reviewcopilot.tool.GitDiffTool;
import com.ynzz.agentscope.reviewcopilot.tool.RuleCheckTool;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ReviewService {

    private final ReviewPathGuard pathGuard;
    private final ReviewJobStore jobStore;
    private final ReviewEventPublisher eventPublisher;
    private final ReviewReportService reportService;
    private final GitDiffTool gitDiffTool;
    private final FileContextTool fileContextTool;
    private final RuleCheckTool ruleCheckTool;
    private final AgentFactory agentFactory;

    public ReviewService(
            ReviewPathGuard pathGuard,
            ReviewJobStore jobStore,
            ReviewEventPublisher eventPublisher,
            ReviewReportService reportService,
            GitDiffTool gitDiffTool,
            FileContextTool fileContextTool,
            RuleCheckTool ruleCheckTool,
            AgentFactory agentFactory) {
        this.pathGuard = pathGuard;
        this.jobStore = jobStore;
        this.eventPublisher = eventPublisher;
        this.reportService = reportService;
        this.gitDiffTool = gitDiffTool;
        this.fileContextTool = fileContextTool;
        this.ruleCheckTool = ruleCheckTool;
        this.agentFactory = agentFactory;
    }

    public Mono<ReviewJob> createReview(ReviewRequest request) {
        return Mono.fromCallable(() -> {
            Path repoRoot = pathGuard.resolveRepositoryRoot(request.repoPath());
            ReviewJob job = ReviewJob.created(request, repoRoot.toString());
            jobStore.save(job);
            eventPublisher.publish(ReviewEvent.of(
                    job.id(),
                    ReviewEventType.JOB_CREATED,
                    "评审任务已创建。",
                    Map.of("repoPath", job.repoPath(), "diffMode", job.diffMode())));
            Mono.fromRunnable(() -> runReview(job.id()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            return job;
        });
    }

    public Mono<ReviewJob> getReview(String id) {
        return Mono.fromCallable(() -> jobStore.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("未找到评审任务：" + id)));
    }

    public Flux<ReviewEvent> streamEvents(String id) {
        return eventPublisher.stream(id);
    }

    public Mono<String> readReportMarkdown(String id) {
        return Mono.fromCallable(() -> reportService.readMarkdown(id));
    }

    private void runReview(String jobId) {
        ReviewJob job = jobStore.findById(jobId)
                .orElseThrow(() -> new ReviewNotFoundException("未找到评审任务：" + jobId));
        try {
            job = jobStore.save(job.running());
            eventPublisher.publish(job.id(), ReviewEventType.MODEL_REVIEWING, "评审流水线已启动。");

            GitDiffTool.GitDiffResult diff = gitDiffTool.loadDiff(job.repoPath(), job.diffMode(), job.baseRef());
            eventPublisher.publish(ReviewEvent.of(
                    job.id(),
                    ReviewEventType.DIFF_LOADED,
                    "Git diff 已读取。",
                    Map.of(
                            "changedFiles", diff.changedFiles(),
                            "addedFiles", diff.addedFiles(),
                            "modifiedFiles", diff.modifiedFiles(),
                            "deletedFiles", diff.deletedFiles())));

            Map<String, String> contexts = fileContextTool.loadContexts(diff.repoRoot(), diff.changedFiles());
            eventPublisher.publish(ReviewEvent.of(
                    job.id(),
                    ReviewEventType.FILE_CONTEXT_LOADED,
                    "源码上下文已读取。",
                    Map.of("fileCount", contexts.size())));

            List<ReviewFinding> findings =
                    ruleCheckTool.check(diff, contexts, job.focusCategories());
            eventPublisher.publish(ReviewEvent.of(
                    job.id(),
                    ReviewEventType.RULE_CHECK_DONE,
                    "规则检查已完成。",
                    Map.of("findingCount", findings.size())));

            String modelNote = "";
            if (!agentFactory.isModelConfigured()) {
                modelNote = ModelFactory.MISSING_PROVIDER_MESSAGE
                        + " 当前仍已执行确定性规则检查。";
                eventPublisher.publish(ReviewEvent.of(
                        job.id(),
                        ReviewEventType.MODEL_REVIEWING,
                        "未配置模型提供商，已跳过模型评审并保留规则检查结果。",
                        Map.of("modelConfigured", false)));
            } else {
                eventPublisher.publish(ReviewEvent.of(
                        job.id(),
                        ReviewEventType.MODEL_REVIEWING,
                        "模型提供商已配置，AgentFactory 可继续扩展模型评审。",
                        Map.of("modelConfigured", true)));
            }

            for (ReviewFinding finding : findings) {
                eventPublisher.publish(ReviewEvent.of(
                        job.id(),
                        ReviewEventType.FINDING_GENERATED,
                        "已生成 " + finding.severity() + " 级别的 " + finding.category().getCode() + " 发现项。",
                        Map.of("finding", finding)));
            }

            ReviewJob reportJob = job.completed(findings, null);
            ReviewReport report = reportService.generateAndSave(reportJob, modelNote);
            ReviewJob completed = jobStore.save(job.completed(findings, "data/reports/" + job.id() + ".md"));
            eventPublisher.publish(ReviewEvent.of(
                    job.id(),
                    ReviewEventType.REPORT_READY,
                    "Markdown 报告已生成。",
                    Map.of("reportLength", report.markdown().length())));
            eventPublisher.publish(ReviewEvent.of(
                    job.id(),
                    ReviewEventType.JOB_COMPLETED,
                    "评审任务已完成。",
                    Map.of("status", completed.status())));
            eventPublisher.complete(job.id());
        } catch (Exception e) {
            ReviewJob failed = jobStore.save(job.failed(e.getMessage()));
            eventPublisher.publish(ReviewEvent.of(
                    failed.id(),
                    ReviewEventType.JOB_FAILED,
                    "评审任务失败：" + e.getMessage(),
                    Map.of("error", e.getMessage())));
            eventPublisher.complete(failed.id());
        }
    }

    public static class ReviewNotFoundException extends RuntimeException {
        public ReviewNotFoundException(String message) {
            super(message);
        }
    }
}
