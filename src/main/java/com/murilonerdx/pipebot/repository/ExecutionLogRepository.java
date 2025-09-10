package com.murilonerdx.pipebot.repository;

import com.murilonerdx.pipebot.model.ExecutionLogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionLogRepository extends MongoRepository<ExecutionLogEntity, String> {}
