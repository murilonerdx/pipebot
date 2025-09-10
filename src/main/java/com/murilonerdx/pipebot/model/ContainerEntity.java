package com.murilonerdx.pipebot.model;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContainerEntity {
	@Id
	private String id;
	private String containerId;
	private String image;
	private String name;
	private LocalDateTime createdAt;
}
