package com.ynzz.agentscope.reviewcopilot.store;

import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import java.util.Optional;

public interface ReviewJobStore {

    ReviewJob save(ReviewJob job);

    Optional<ReviewJob> findById(String id);
}
