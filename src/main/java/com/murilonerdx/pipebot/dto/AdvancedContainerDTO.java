package com.murilonerdx.pipebot.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AdvancedContainerDTO {
	private String imageName;
	private String containerName;
	private List<String> cmd;
	private Map<String, String> envVars;
	private Map<Integer, Integer> portMappings;   // hostPort -> containerPort
	private Map<String, String> labels;           // Traefik ou outros
	private Map<String, String> volumeMappings;   // hostPath -> containerPath
}
