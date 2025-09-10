package com.murilonerdx.pipebot.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class DockerConfig {

	@Bean
	public DockerClient getDockerClient() {
		String osName = System.getProperty("os.name").toLowerCase();

		String dockerHost;
		if (osName.contains("win")) {
			dockerHost = "npipe:////./pipe/docker_engine";
		} else {
			dockerHost = "unix:///var/run/docker.sock";
		}

		DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost(dockerHost)
				.build();

		ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
				.dockerHost(config.getDockerHost())
				.maxConnections(100)
				.connectionTimeout(Duration.ofSeconds(30))
				.responseTimeout(Duration.ofSeconds(45))
				.build();

		return DockerClientImpl.getInstance(config, httpClient);
	}
}
