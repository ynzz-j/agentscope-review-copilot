package com.ynzz.agentscope.reviewcopilot.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewIdValidator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class JsonFileReviewJobStore implements ReviewJobStore {

    private final Path root;
    private final ObjectMapper objectMapper;

    public JsonFileReviewJobStore(Path root) {
        this.root = root;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public synchronized ReviewJob save(ReviewJob job) {
        try {
            Files.createDirectories(root.toAbsolutePath().normalize());
            Path target = file(job.id());
            Path temp = target.resolveSibling(target.getFileName() + ".tmp");
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(job);
            Files.writeString(temp, json, StandardCharsets.UTF_8);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return job;
        } catch (IOException e) {
            throw new IllegalStateException("保存评审任务失败：" + job.id(), e);
        }
    }

    @Override
    public Optional<ReviewJob> findById(String id) {
        Path file = file(id);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(Files.readString(file, StandardCharsets.UTF_8), ReviewJob.class));
        } catch (IOException e) {
            throw new IllegalStateException("读取评审任务失败：" + id, e);
        }
    }

    private Path file(String id) {
        String safeId = ReviewIdValidator.requireSafe(id, "review id");
        Path base = root.toAbsolutePath().normalize();
        Path target = base.resolve(safeId + ".json").normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("评审任务路径越过任务目录：" + id);
        }
        return target;
    }
}
