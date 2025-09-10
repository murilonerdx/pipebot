package com.murilonerdx.pipebot.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DockerComposeServiceDTO {
	private String name;
	private String image;
	private Map<String, String> environment;
	private String restart;        // ex: "unless-stopped"
	private String shmSize;        // ex: "2gb"
	private Deploy deploy;         // replicação e restart_policy

	@Data
	public static class Deploy {
		private int replicas;
		private RestartPolicy restartPolicy;

		@Data
		public static class RestartPolicy {
			private String condition;  // ex: "any"
		}
	}
}
