package com.murilonerdx.pipebot;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.murilonerdx.pipebot.service.DockerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;
import java.util.List;

@SpringBootApplication
public class PipebotApplication {
	public static void main(String[] args) {
		SpringApplication.run(PipebotApplication.class, args);
	}
}
