package com.ynzz.agentscope.reviewcopilot.config;

import com.ynzz.agentscope.reviewcopilot.tool.FileContextTool;
import com.ynzz.agentscope.reviewcopilot.tool.GitDiffTool;
import com.ynzz.agentscope.reviewcopilot.tool.ReportTool;
import com.ynzz.agentscope.reviewcopilot.tool.RuleCheckTool;
import io.agentscope.core.tool.Toolkit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolkitConfig {

    @Bean
    public Toolkit reviewToolkit(
            GitDiffTool gitDiffTool,
            FileContextTool fileContextTool,
            RuleCheckTool ruleCheckTool,
            ReportTool reportTool) {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(gitDiffTool);
        toolkit.registerTool(fileContextTool);
        toolkit.registerTool(ruleCheckTool);
        toolkit.registerTool(reportTool);
        return toolkit;
    }
}
