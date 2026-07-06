package com.ynzz.agentscope.reviewcopilot.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "review-copilot")
public class ReviewCopilotProperties {

    private final Model model = new Model();
    private final Agent agent = new Agent();
    private final Storage storage = new Storage();
    private final Review review = new Review();

    public Model getModel() {
        return model;
    }

    public Agent getAgent() {
        return agent;
    }

    public Storage getStorage() {
        return storage;
    }

    public Review getReview() {
        return review;
    }

    public static class Model {
        private String provider;
        private String modelName;
        private String apiKey;
        private String baseUrl;
        private String endpointPath;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getEndpointPath() {
            return endpointPath;
        }

        public void setEndpointPath(String endpointPath) {
            this.endpointPath = endpointPath;
        }
    }

    public static class Agent {
        private String name = "AgentScope Review Copilot";
        private int maxIters = 8;
        private String sysPrompt =
                """
                You are AgentScope Review Copilot.
                Review only the supplied Git diff and file context.
                Produce evidence-backed findings, avoid broad refactoring advice, and never modify source files.
                """;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getMaxIters() {
            return maxIters;
        }

        public void setMaxIters(int maxIters) {
            this.maxIters = maxIters;
        }

        public String getSysPrompt() {
            return sysPrompt;
        }

        public void setSysPrompt(String sysPrompt) {
            this.sysPrompt = sysPrompt;
        }
    }

    public static class Storage {
        private Path dataDir = Path.of("data");
        private Path agentStateDir = Path.of("data", "agent-states");
        private Path jobDir = Path.of("data", "jobs");
        private Path reportDir = Path.of("data", "reports");

        public Path getDataDir() {
            return dataDir;
        }

        public void setDataDir(Path dataDir) {
            this.dataDir = dataDir;
        }

        public Path getAgentStateDir() {
            return agentStateDir;
        }

        public void setAgentStateDir(Path agentStateDir) {
            this.agentStateDir = agentStateDir;
        }

        public Path getJobDir() {
            return jobDir;
        }

        public void setJobDir(Path jobDir) {
            this.jobDir = jobDir;
        }

        public Path getReportDir() {
            return reportDir;
        }

        public void setReportDir(Path reportDir) {
            this.reportDir = reportDir;
        }
    }

    public static class Review {
        private int maxContextLinesPerFile = 220;
        private final List<String> sensitiveFileNames = new ArrayList<>(
                List.of(".env", ".env.local", "id_rsa", "id_ed25519", "credentials.json"));
        private final List<String> sensitiveDirectories = new ArrayList<>(
                List.of(".ssh", ".aws", ".azure", ".gcp", ".config"));

        public int getMaxContextLinesPerFile() {
            return maxContextLinesPerFile;
        }

        public void setMaxContextLinesPerFile(int maxContextLinesPerFile) {
            this.maxContextLinesPerFile = maxContextLinesPerFile;
        }

        public List<String> getSensitiveFileNames() {
            return sensitiveFileNames;
        }

        public List<String> getSensitiveDirectories() {
            return sensitiveDirectories;
        }
    }
}
