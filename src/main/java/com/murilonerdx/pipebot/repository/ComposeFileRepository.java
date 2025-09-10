package com.murilonerdx.pipebot.repository;

import com.murilonerdx.pipebot.model.ComposeFileEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComposeFileRepository extends MongoRepository<ComposeFileEntity, String> {}
