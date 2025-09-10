package com.murilonerdx.pipebot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DockerComposeDTO {
	private String version;
	private Map<String, Service> services;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Service {
		private String image;
		private List<String> command;
		private Map<String, String> environment; // variáveis de ambiente
		private List<String> ports; // ex: "8080:80"
		private List<String> volumes; // ex: "./data:/data"
		private String restart; // ex: "always"
		private List<String> depends_on; // dependências
	}
}

