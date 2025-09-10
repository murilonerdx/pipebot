package com.murilonerdx.pipebot.model;

import lombok.*;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionLogEntity {
	@Id
	private String id;
	private String action;
	private String filePath;
	private LocalDateTime executedAt;
	private String status;
}
