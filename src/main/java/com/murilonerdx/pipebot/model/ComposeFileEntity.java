package com.murilonerdx.pipebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComposeFileEntity {

	@Id
	private String id;
	private String path;
	private String version;
	private LocalDateTime createdAt;
}
