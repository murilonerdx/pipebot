package com.murilonerdx.pipebot.controller;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.murilonerdx.pipebot.dto.AdvancedContainerDTO;
import com.murilonerdx.pipebot.dto.DockerComposeDTO;
import com.murilonerdx.pipebot.model.ComposeFileEntity;
import com.murilonerdx.pipebot.model.ContainerEntity;
import com.murilonerdx.pipebot.model.ExecutionLogEntity;
import com.murilonerdx.pipebot.model.ImageEntity;
import com.murilonerdx.pipebot.repository.ComposeFileRepository;
import com.murilonerdx.pipebot.repository.ContainerRepository;
import com.murilonerdx.pipebot.repository.ExecutionLogRepository;
import com.murilonerdx.pipebot.repository.ImageRepository;
import com.murilonerdx.pipebot.service.DockerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/docker")
@RequiredArgsConstructor
public class DockerController {

	private final DockerService dockerService;
	private final ComposeFileRepository composeFileRepository;
	private final ContainerRepository containerRepository;
	private final ImageRepository imageRepository;
	private final ExecutionLogRepository executionLogRepository;

	// ---------------- Docker Compose ----------------
	@PostMapping("/compose/up")
	public ResponseEntity<String> composeUp(@RequestBody DockerComposeDTO composeDTO) {
		try {
			dockerService.executeComposeUp(composeDTO);
			return ResponseEntity.ok("Docker Compose UP executado com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Erro: " + e.getMessage());
		}
	}

	@PostMapping("/compose/down")
	public ResponseEntity<String> composeDown(@RequestBody DockerComposeDTO composeDTO) {
		try {
			dockerService.executeComposeDown(composeDTO);
			return ResponseEntity.ok("Docker Compose DOWN executado com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Erro: " + e.getMessage());
		}
	}

	@PostMapping("/compose/restart")
	public ResponseEntity<String> composeRestart(@RequestBody DockerComposeDTO composeDTO) {
		try {
			dockerService.executeComposeDown(composeDTO);
			dockerService.executeComposeUp(composeDTO);
			return ResponseEntity.ok("Docker Compose RESTART executado com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Erro: " + e.getMessage());
		}
	}

	@GetMapping("/compose/files")
	public ResponseEntity<List<String>> listComposeFiles(@RequestParam(defaultValue = "./") String path) {
		try {
			List<File> files = dockerService.listLocalComposeFiles(path);
			List<String> fileNames = files.stream().map(File::getName).collect(Collectors.toList());
			return ResponseEntity.ok(fileNames);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(List.of("Erro: " + e.getMessage()));
		}
	}

	@GetMapping("/compose/saved")
	public ResponseEntity<List<ComposeFileEntity>> getSavedComposeFiles() {
		return ResponseEntity.ok(composeFileRepository.findAll());
	}

	// ---------------- Containers ----------------
	@GetMapping("/containers")
	public ResponseEntity<List<Container>> listContainers(@RequestParam(defaultValue = "false") boolean showAll,
														  @RequestParam(required = false) String[] statusFilter) {
		return ResponseEntity.ok(dockerService.listContainers(showAll, statusFilter != null ? statusFilter : new String[]{}));
	}

	@GetMapping("/containers/saved")
	public ResponseEntity<List<ContainerEntity>> getSavedContainers() {
		return ResponseEntity.ok(containerRepository.findAll());
	}

	@PostMapping("/containers/start/{id}")
	public ResponseEntity<String> startContainer(@PathVariable String id) {
		dockerService.startContainer(id);
		return ResponseEntity.ok("Container iniciado: " + id);
	}

	@PostMapping("/containers/stop/{id}")
	public ResponseEntity<String> stopContainer(@PathVariable String id) {
		dockerService.stopContainer(id);
		return ResponseEntity.ok("Container parado: " + id);
	}

	@DeleteMapping("/containers/{id}")
	public ResponseEntity<String> removeContainer(@PathVariable String id,
												  @RequestParam(defaultValue = "false") boolean force) {
		dockerService.removeContainer(id, force);
		return ResponseEntity.ok("Container removido: " + id);
	}

	@PostMapping("/containers/create")
	public ResponseEntity<String> createContainer(@RequestParam String image,
												  @RequestParam String name,
												  @RequestParam(required = false) String[] cmd) {
		String id = dockerService.createAndStartContainer(image, name, cmd != null ? cmd : new String[]{});
		return ResponseEntity.ok("Container criado e iniciado: " + id);
	}

	@PostMapping("/containers/scale")
	public ResponseEntity<String> scaleContainer(@RequestParam String image,
												 @RequestParam String baseName,
												 @RequestParam int replicas) {
		dockerService.scaleContainer(image, baseName, replicas);
		return ResponseEntity.ok("Containers escalados: " + replicas);
	}

	@PostMapping("/containers/create-advanced")
	public ResponseEntity<String> createAdvancedContainer(@RequestBody AdvancedContainerDTO dto) {
		try {
			String containerId = dockerService.createAdvancedContainer(
					dto.getImageName(),
					dto.getContainerName(),
					dto.getCmd(),
					dto.getEnvVars(),
					dto.getPortMappings(),
					dto.getLabels(),
					dto.getVolumeMappings()
			);
			return ResponseEntity.ok("Container avançado criado: " + containerId);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Erro ao criar container avançado: " + e.getMessage());
		}
	}

	// ---------------- Images ----------------
	@GetMapping("/images")
	public ResponseEntity<List<Image>> listImages() {
		return ResponseEntity.ok(dockerService.listImages());
	}

	@GetMapping("/images/saved")
	public ResponseEntity<List<ImageEntity>> getSavedImages() {
		return ResponseEntity.ok(imageRepository.findAll());
	}

	@PostMapping("/images/build")
	public ResponseEntity<String> buildImage(@RequestParam String path,
											 @RequestParam String tag) {
		try {
			String imageId = dockerService.buildImage(new File(path), tag);
			return ResponseEntity.ok("Imagem criada: " + imageId);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Erro: " + e.getMessage());
		}
	}

	@DeleteMapping("/images/{id}")
	public ResponseEntity<String> removeImage(@PathVariable String id,
											  @RequestParam(defaultValue = "false") boolean force) {
		dockerService.removeImage(id, force);
		return ResponseEntity.ok("Imagem removida: " + id);
	}

	@PostMapping("/images/pull")
	public ResponseEntity<String> pullImage(@RequestParam String image) {
		try {
			dockerService.pullImage(image);
			return ResponseEntity.ok("Imagem puxada: " + image);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Erro: " + e.getMessage());
		}
	}

	@PostMapping("/containers/update-image")
	public ResponseEntity<String> updateContainerImage(@RequestParam String containerId,
													   @RequestParam String newImage) {
		dockerService.updateContainerImage(containerId, newImage);
		return ResponseEntity.ok("Container atualizado para nova imagem: " + newImage);
	}

	// ---------------- Logs ----------------
	@GetMapping("/logs")
	public ResponseEntity<List<ExecutionLogEntity>> getExecutionLogs() {
		return ResponseEntity.ok(executionLogRepository.findAll());
	}

	@GetMapping("/compose/view/id/{id}")
	public ResponseEntity<String> viewComposeFileById(@PathVariable String id) {
		try {
			return ResponseEntity.ok(dockerService.viewComposeFileById(id));
		} catch (Exception e) {
			return ResponseEntity.status(404).body("Erro: " + e.getMessage());
		}
	}

	@GetMapping("/compose/view/name/{name}")
	public ResponseEntity<String> viewComposeFileByName(@PathVariable String name) {
		try {
			return ResponseEntity.ok(dockerService.viewComposeFileByName(name));
		} catch (Exception e) {
			return ResponseEntity.status(404).body("Erro: " + e.getMessage());
		}
	}

	@GetMapping("/compose/download/id/{id}")
	public ResponseEntity<Resource> downloadComposeFileById(@PathVariable String id) {
		try {
			Resource resource = dockerService.downloadComposeFileById(id);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(resource);
		} catch (Exception e) {
			return ResponseEntity.status(404).build();
		}
	}

	@GetMapping("/compose/download/name/{name}")
	public ResponseEntity<Resource> downloadComposeFileByName(@PathVariable String name) {
		try {
			Resource resource = dockerService.downloadComposeFileByName(name);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(resource);
		} catch (Exception e) {
			return ResponseEntity.status(404).build();
		}
	}
}
