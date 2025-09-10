package com.murilonerdx.pipebot.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DockerContainerEntity {

	@Id
	private String id;
	private String containerId;      // ID real do container
	private String name;             // Nome do container
	private String image;            // Imagem usada
	private String status;           // running, stopped, etc
	private String composeFile;      // se veio de docker-compose, salvar caminho

}
