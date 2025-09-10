package com.murilonerdx.pipebot.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageEntity {
	@Id
	private String id;
	private String repoTags;
	private String imageId;
	private LocalDateTime pulledAt;
}
