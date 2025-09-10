package com.murilonerdx.pipebot.repository;

import com.murilonerdx.pipebot.model.ContainerEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContainerRepository extends MongoRepository<ContainerEntity, String> {}