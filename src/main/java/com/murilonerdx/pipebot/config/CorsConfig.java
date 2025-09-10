package com.murilonerdx.pipebot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Value("${cors.allowed-origins.dev}")
	private String devOrigin;

	@Value("${cors.allowed-origins.prd}")
	private String prdOrigin;

	@Value("${spring.profiles.active:dev}")
	private String activeProfile;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		String allowedOrigin = "dev".equals(activeProfile) ? devOrigin : prdOrigin;

		registry.addMapping("/**")
				.allowedOrigins(allowedOrigin)
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(true)
				.maxAge(3600);
	}
}
