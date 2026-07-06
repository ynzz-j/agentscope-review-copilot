package com.ynzz.agentscope.reviewcopilot.factory;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import io.agentscope.core.formatter.openai.DeepSeekFormatter;
import io.agentscope.core.model.AnthropicChatModel;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.GeminiChatModel;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ModelRegistry;
import io.agentscope.core.model.OllamaChatModel;
import io.agentscope.core.model.OpenAIChatModel;
import java.util.Locale;
import org.springframework.util.StringUtils;

public class ConfigurableModelFactory implements ModelFactory {

    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com";
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";

    private final ReviewCopilotProperties properties;

    public ConfigurableModelFactory(ReviewCopilotProperties properties) {
        this.properties = properties;
    }

    @Override
    public Model getModel() {
        String provider = properties.getModel().getProvider().trim();
        if (!StringUtils.hasText(provider)) {
            throw new ModelConfigurationException(MISSING_PROVIDER_MESSAGE);
        }
        String modelName = requireModelName(provider);
        return switch (normalizeProvider(provider)) {
            case "dashscope" -> DashScopeChatModel.builder()
                    .apiKey(requireApiKey("DASHSCOPE_API_KEY", "DashScope"))
                    .modelName(modelName)
                    .stream(true)
                    .build();
            case "openai" -> openAIModel(modelName, "OPENAI_API_KEY", null);
            case "deepseek" -> openAIModel(modelName, "DEEPSEEK_API_KEY", DEEPSEEK_BASE_URL);
            case "anthropic" -> AnthropicChatModel.builder()
                    .apiKey(requireApiKey("ANTHROPIC_API_KEY", "Anthropic"))
                    .modelName(modelName)
                    .stream(true)
                    .build();
            case "gemini" -> GeminiChatModel.builder()
                    .apiKey(requireApiKey("GEMINI_API_KEY", "Gemini"))
                    .modelName(modelName)
                    .streamEnabled(true)
                    .build();
            case "ollama" -> OllamaChatModel.builder()
                    .modelName(modelName)
                    .baseUrl(resolveBaseUrl(OLLAMA_BASE_URL, "OLLAMA_BASE_URL"))
                    .build();
            case "registry" -> resolveFromRegistry(modelName);
            default -> resolveKnownModelId(provider, modelName);
        };
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(properties.getModel().getProvider());
    }

    private Model openAIModel(String modelName, String envKey, String defaultBaseUrl) {
        OpenAIChatModel.Builder builder = OpenAIChatModel.builder()
                .formatter(new DeepSeekFormatter())
                .apiKey(requireApiKey(envKey, envKey.replace("_API_KEY", "")))
                .modelName(modelName)
                .stream(true);
        String baseUrl = resolveBaseUrl(defaultBaseUrl, null);
        if (StringUtils.hasText(baseUrl)) {
            builder.baseUrl(baseUrl);
        }
        String endpointPath = properties.getModel().getEndpointPath();
        if (StringUtils.hasText(endpointPath)) {
            builder.endpointPath(endpointPath.trim());
        }
        return builder.build();
    }

    private Model resolveFromRegistry(String modelName) {
        try {
            return ModelRegistry.resolve(modelName);
        } catch (RuntimeException e) {
            throw new ModelConfigurationException("无法通过 AgentScope ModelRegistry 解析模型：" + e.getMessage());
        }
    }

    private Model resolveKnownModelId(String provider, String modelName) {
        String modelId = provider + ":" + modelName;
        if (ModelRegistry.canResolve(modelId)) {
            return resolveFromRegistry(modelId);
        }
        throw new ModelConfigurationException(
                "不支持的模型提供商："
                        + provider
                        + "。请使用 dashscope/dsp、openai、deepseek、anthropic、gemini、ollama 或 registry。");
    }

    private String requireModelName(String provider) {
        String modelName = properties.getModel().getModelName();
        if (!StringUtils.hasText(modelName)) {
            throw new ModelConfigurationException("模型提供商 '" + provider + "' 已配置，但缺少 model-name。");
        }
        return modelName.trim();
    }

    private String requireApiKey(String envKey, String providerLabel) {
        String configured = properties.getModel().getApiKey();
        if (StringUtils.hasText(configured)) {
            return configured.trim();
        }
        String fromEnv = System.getenv(envKey);
        if (StringUtils.hasText(fromEnv)) {
            return fromEnv.trim();
        }
        throw new ModelConfigurationException(
                "模型提供商 "
                        + providerLabel
                        + " 已配置，但缺少 api-key。请配置 review-copilot.model.api-key 或环境变量 "
                        + envKey
                        + "。");
    }

    private String resolveBaseUrl(String defaultBaseUrl, String envKey) {
        String configured = properties.getModel().getBaseUrl();
        if (StringUtils.hasText(configured)) {
            return configured.trim();
        }
        if (StringUtils.hasText(envKey)) {
            String fromEnv = System.getenv(envKey);
            if (StringUtils.hasText(fromEnv)) {
                return fromEnv.trim();
            }
        }
        return defaultBaseUrl;
    }

    private String normalizeProvider(String provider) {
        String normalized = provider.toLowerCase(Locale.ROOT).replace("_", "-").trim();
        return switch (normalized) {
            case "dash", "dashscope", "dsp", "qwen" -> "dashscope";
            case "open-ai" -> "openai";
            case "deep-seek" -> "deepseek";
            case "model-registry", "agentscope-registry" -> "registry";
            default -> normalized;
        };
    }
}
