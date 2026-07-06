package com.ynzz.agentscope.reviewcopilot.service;

import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import com.ynzz.agentscope.reviewcopilot.model.ReviewReport;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPermissionPolicy;
import com.ynzz.agentscope.reviewcopilot.tool.ReportTool;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ReviewReportService {

    private final ReviewPermissionPolicy permissionPolicy;
    private final ReportTool reportTool;

    public ReviewReportService(ReviewPermissionPolicy permissionPolicy, ReportTool reportTool) {
        this.permissionPolicy = permissionPolicy;
        this.reportTool = reportTool;
    }

    public ReviewReport generateAndSave(ReviewJob job, String modelNote) {
        String markdown = reportTool.renderMarkdown(job, modelNote);
        Path target = permissionPolicy.requireReportOutput(job.id());
        try {
            Files.writeString(target, markdown, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("写入评审报告失败：" + target, e);
        }
        String summary = "共发现 " + job.findings().size() + " 条工程质量发现项。";
        return new ReviewReport(job.id(), summary, job.findings(), markdown, Instant.now());
    }

    public String readMarkdown(String jobId) {
        Path target = permissionPolicy.requireReportOutput(jobId);
        if (!Files.exists(target)) {
            throw new ReportNotFoundException("评审报告不存在：" + jobId);
        }
        try {
            return Files.readString(target, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取评审报告失败：" + target, e);
        }
    }

    public static class ReportNotFoundException extends RuntimeException {
        public ReportNotFoundException(String message) {
            super(message);
        }
    }
}
