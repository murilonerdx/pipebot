package com.murilonerdx.pipebot.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.murilonerdx.pipebot.dto.*;
import com.murilonerdx.pipebot.model.*;
import com.murilonerdx.pipebot.repository.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DockerService {

	private final DockerClient dockerClient;
	private final ComposeFileRepository composeFileRepository;
	private final ContainerRepository containerRepository;
	private final ImageRepository imageRepository;
	private final ExecutionLogRepository executionLogRepository;

	public DockerService(
			DockerClient dockerClient,
			ComposeFileRepository composeFileRepository,
			ContainerRepository containerRepository,
			ImageRepository imageRepository,
			ExecutionLogRepository executionLogRepository
	) {
		this.dockerClient = dockerClient;
		this.composeFileRepository = composeFileRepository;
		this.containerRepository = containerRepository;
		this.imageRepository = imageRepository;
		this.executionLogRepository = executionLogRepository;
	}

	// ---------------- Docker Compose ----------------
	public File generateComposeFile(DockerComposeDTO composeDTO, String fileName) throws IOException {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		Yaml yaml = new Yaml(options);

		Map<String, Object> servicesMap = new LinkedHashMap<>();
		for (Map.Entry<String, DockerComposeDTO.Service> entry : composeDTO.getServices().entrySet()) {
			DockerComposeDTO.Service service = entry.getValue();
			Map<String, Object> serviceMap = new LinkedHashMap<>();

			if (service.getImage() != null) serviceMap.put("image", service.getImage());
			if (service.getCommand() != null) serviceMap.put("command", service.getCommand());
			if (service.getEnvironment() != null) serviceMap.put("environment", service.getEnvironment());
			if (service.getPorts() != null) serviceMap.put("ports", service.getPorts());
			if (service.getRestart() != null) serviceMap.put("restart", service.getRestart());
			if (service.getVolumes() != null) serviceMap.put("volumes", service.getVolumes());
			if (service.getDepends_on() != null) serviceMap.put("depends_on", service.getDepends_on());

			servicesMap.put(entry.getKey(), serviceMap);
		}

		return getFile(fileName, yaml, servicesMap, composeDTO.getVersion(), composeDTO);
	}

	private File getFile(String fileName, Yaml yaml, Map<String, Object> servicesMap, String version, DockerComposeDTO composeDTO) throws IOException {
		Map<String, Object> yamlMap = Map.of(
				"version", version,
				"services", servicesMap
		);

		File file = new File(fileName);
		try (FileWriter writer = new FileWriter(file)) {
			yaml.dump(yamlMap, writer);
		}

		composeFileRepository.save(new ComposeFileEntity(
				null,
				file.getAbsolutePath(),
				version,
				LocalDateTime.now()
		));

		return file;
	}

	private File getFile(String fileName, Yaml yaml, Map<String, Object> servicesMap, String version, DockerComposeComplexDto composeDTO) throws IOException {
		Map<String, Object> yamlMap = Map.of(
				"version", version,
				"services", servicesMap
		);

		File file = new File(fileName);
		try (FileWriter writer = new FileWriter(file)) {
			yaml.dump(yamlMap, writer);
		}

		composeFileRepository.save(new ComposeFileEntity(
				null,
				file.getAbsolutePath(),
				version,
				LocalDateTime.now()
		));

		return file;
	}

	public File generateComposeFile(DockerComposeComplexDto composeDTO, String fileName) throws IOException {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		Yaml yaml = new Yaml(options);

		Map<String, Object> servicesMap = composeDTO.getServices().stream()
				.collect(Collectors.toMap(
						DockerComposeServiceDTO::getName,
						service -> {
							Map<String, Object> map = new LinkedHashMap<>();
							if (service.getImage() != null) map.put("image", service.getImage());
							if (service.getEnvironment() != null) map.put("environment", service.getEnvironment());
							if (service.getRestart() != null) map.put("restart", service.getRestart());
							if (service.getShmSize() != null) map.put("shm_size", service.getShmSize());
							if (service.getDeploy() != null) {
								Map<String, Object> deploy = new LinkedHashMap<>();
								deploy.put("replicas", service.getDeploy().getReplicas());
								if (service.getDeploy().getRestartPolicy() != null) {
									deploy.put("restart_policy", Map.of(
											"condition", service.getDeploy().getRestartPolicy().getCondition()
									));
								}
								map.put("deploy", deploy);
							}
							return map;
						}
				));

		return getFile(fileName, yaml, servicesMap, composeDTO.getVersion(), composeDTO);
	}

	public void executeComposeUp(DockerComposeDTO composeDTO) throws IOException, InterruptedException {
		String fileName = UUID.randomUUID() + "-docker-compose.yml";
		File composeFile = generateComposeFile(composeDTO, fileName);
		executeComposeUp(composeFile);
	}

	public void executeComposeDown(DockerComposeDTO composeDTO) throws IOException, InterruptedException {
		String fileName = UUID.randomUUID() + "-docker-compose.yml";
		File composeFile = generateComposeFile(composeDTO, fileName);
		executeComposeDown(composeFile);
	}

	public void executeComposeUp(File composeFile) throws IOException, InterruptedException {
		try {
			executeProcess("docker", "compose", "-f", composeFile.getAbsolutePath(), "up", "-d");
			executionLogRepository.save(new ExecutionLogEntity(null, "UP", composeFile.getAbsolutePath(), LocalDateTime.now(), "SUCCESS"));
		} catch (Exception e) {
			executionLogRepository.save(new ExecutionLogEntity(null, "UP", composeFile.getAbsolutePath(), LocalDateTime.now(), "ERROR: " + e.getMessage()));
			throw e;
		}
	}

	public void executeComposeDown(File composeFile) throws IOException, InterruptedException {
		try {
			executeProcess("docker", "compose", "-f", composeFile.getAbsolutePath(), "down");
			executionLogRepository.save(new ExecutionLogEntity(null, "DOWN", composeFile.getAbsolutePath(), LocalDateTime.now(), "SUCCESS"));
		} catch (Exception e) {
			executionLogRepository.save(new ExecutionLogEntity(null, "DOWN", composeFile.getAbsolutePath(), LocalDateTime.now(), "ERROR: " + e.getMessage()));
			throw e;
		}
	}

	public void dockerComposeRestart(File composeFile) throws Exception {
		executeComposeDown(composeFile);
		executeComposeUp(composeFile);
	}

	public List<File> listLocalComposeFiles(String directoryPath) throws IOException {
		Path dir = Paths.get(directoryPath);
		if (!Files.exists(dir) || !Files.isDirectory(dir)) {
			throw new IllegalArgumentException("Diretório não encontrado: " + directoryPath);
		}
		try (Stream<Path> stream = Files.list(dir)) {
			return stream
					.filter(Files::isRegularFile)
					.filter(p -> p.getFileName().toString().endsWith(".yml") || p.getFileName().toString().endsWith(".yaml"))
					.map(Path::toFile)
					.collect(Collectors.toList());
		}
	}

	// ---------------- Containers ----------------
	public List<Container> listContainers(boolean showAll, String... statusFilter) {
		List<Container> containers = dockerClient.listContainersCmd()
				.withShowAll(showAll)
				.withStatusFilter(List.of(statusFilter))
				.exec();

		containers.forEach(c -> containerRepository.save(
				new ContainerEntity(null, c.getId(), c.getImage(), Arrays.toString(c.getNames()), LocalDateTime.now())
		));

		return containers;
	}

	public void startContainer(String containerId) {
		dockerClient.startContainerCmd(containerId).exec();
		executionLogRepository.save(new ExecutionLogEntity(null, "START", containerId, LocalDateTime.now(), "SUCCESS"));
	}

	public void stopContainer(String containerId) {
		dockerClient.stopContainerCmd(containerId).exec();
		executionLogRepository.save(new ExecutionLogEntity(null, "STOP", containerId, LocalDateTime.now(), "SUCCESS"));
	}

	public void removeContainer(String containerId, boolean force) {
		dockerClient.removeContainerCmd(containerId).withForce(force).exec();
		executionLogRepository.save(new ExecutionLogEntity(null, "REMOVE", containerId, LocalDateTime.now(), "SUCCESS"));
	}

	public String createAndStartContainer(String imageName, String containerName, String... cmd) {
		CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
				.withName(containerName)
				.withCmd(cmd)
				.exec();
		dockerClient.startContainerCmd(container.getId()).exec();

		containerRepository.save(new ContainerEntity(null, container.getId(), imageName, containerName, LocalDateTime.now()));
		return container.getId();
	}

	public void scaleContainer(String imageName, String baseContainerName, int replicas) {
		for (int i = 1; i <= replicas; i++) {
			String containerName = baseContainerName + "-" + i;
			createAndStartContainer(imageName, containerName);
		}
	}

	public String createAdvancedContainer(
			String imageName,
			String containerName,
			List<String> cmd,
			Map<String, String> envVars,
			Map<Integer, Integer> portMappings,
			Map<String, String> labels,
			Map<String, String> volumeMappings
	) {
		Ports ports = new Ports();
		portMappings.forEach((host, container) -> ports.bind(ExposedPort.tcp(container), Ports.Binding.bindPort(host)));

		List<Bind> binds = volumeMappings.entrySet().stream()
				.map(e -> new Bind(e.getKey(), new Volume(e.getValue())))
				.collect(Collectors.toList());

		HostConfig hostConfig = HostConfig.newHostConfig()
				.withPortBindings(ports)
				.withBinds(binds)
				.withRestartPolicy(RestartPolicy.alwaysRestart());

		CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
				.withName(containerName)
				.withCmd(cmd)
				.withEnv(envVars.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList()))
				.withLabels(labels)
				.withHostConfig(hostConfig)
				.exec();

		dockerClient.startContainerCmd(container.getId()).exec();

		containerRepository.save(new ContainerEntity(null, container.getId(), imageName, containerName, LocalDateTime.now()));
		return container.getId();
	}

	// ---------------- Images ----------------
	public List<Image> listImages() {
		List<Image> images = dockerClient.listImagesCmd().exec();
		images.forEach(img -> imageRepository.save(new ImageEntity(null, Arrays.toString(img.getRepoTags()), img.getId(), LocalDateTime.now())));
		return images;
	}

	public String buildImage(File dockerfileDir, String tag) {
		String imageId = dockerClient.buildImageCmd(dockerfileDir)
				.withTags(Set.of(tag))
				.exec(new BuildImageResultCallback())
				.awaitImageId();
		imageRepository.save(new ImageEntity(null, tag, imageId, LocalDateTime.now()));
		return imageId;
	}

	public void removeImage(String imageId, boolean force) {
		dockerClient.removeImageCmd(imageId).withForce(force).exec();
		executionLogRepository.save(new ExecutionLogEntity(null, "REMOVE_IMAGE", imageId, LocalDateTime.now(), "SUCCESS"));
	}

	public void pullImage(String imageName) throws InterruptedException {
		dockerClient.pullImageCmd(imageName).start().awaitCompletion();
		imageRepository.save(new ImageEntity(null, imageName, "pulled", LocalDateTime.now()));
	}

	public void updateContainerImage(String containerId, String newImage) {
		stopContainer(containerId);
		removeContainer(containerId, true);
		createAndStartContainer(newImage, containerId);
	}

	// ---------------- Utils ----------------
	private void executeProcess(String... command) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.inheritIO();
		Process process = pb.start();
		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Erro ao executar comando: " + String.join(" ", command) + " - Código: " + exitCode);
		}
	}

	public String viewComposeFileById(String id) throws IOException {
		ComposeFileEntity entity = composeFileRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Compose file não encontrado com id: " + id));

		File file = new File(entity.getPath());
		if (!file.exists()) {
			throw new IllegalArgumentException("Arquivo não encontrado no caminho: " + entity.getPath());
		}
		return Files.readString(file.toPath());
	}

	// Buscar e ler conteúdo de compose file por nome
	public String viewComposeFileByName(String fileName) throws IOException {
		ComposeFileEntity composeFileEntity = extractComposeFile(fileName);
		File file = new File(composeFileEntity.getPath());
		if (!file.exists()) {
			throw new IllegalArgumentException("Arquivo não encontrado no caminho: " + composeFileEntity.getPath());
		}
		return Files.readString(file.toPath());
	}

	// Download por id
	public Resource downloadComposeFileById(String id) {
		ComposeFileEntity entity = composeFileRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Compose file não encontrado com id: " + id));

		File file = new File(entity.getPath());
		if (!file.exists()) {
			throw new IllegalArgumentException("Arquivo não encontrado no caminho: " + entity.getPath());
		}
		return new FileSystemResource(file);
	}

	// Download por nome
	public Resource downloadComposeFileByName(String fileName) {
		ComposeFileEntity composeFileEntity = extractComposeFile(fileName);

		File file = new File(composeFileEntity.getPath());
		if (!file.exists()) {
			throw new IllegalArgumentException("Arquivo não encontrado no caminho: " + composeFileEntity.getPath());
		}
		return new FileSystemResource(file);
	}

	public ComposeFileEntity extractComposeFile(String fileName){
		return composeFileRepository.findAll().stream()
				.filter(c -> c.getPath().endsWith(fileName))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Compose file não encontrado com nome: " + fileName));
	}
}
