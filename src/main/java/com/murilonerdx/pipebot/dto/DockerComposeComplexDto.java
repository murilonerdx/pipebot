package com.murilonerdx.pipebot.dto;

import lombok.Data;

import java.util.List;

@Data
public class DockerComposeComplexDto {
	private String version = "3.8";
	private List<DockerComposeServiceDTO> services;
}
