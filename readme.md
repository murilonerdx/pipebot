# 📦 PipeBot

PipeBot é uma aplicação backend desenvolvida em **Spring Boot** para gerenciar **containers Docker, imagens, e arquivos docker-compose** de forma automatizada, através de uma API REST.

Ele permite:
- Subir, parar e reiniciar **stacks docker-compose**
- Listar, criar, escalar e remover **containers**
- Listar, puxar, buildar e remover **imagens Docker**
- Visualizar **logs de execução**
- Baixar e visualizar arquivos `docker-compose.yml` locais

---

## 🚀 Tecnologias

- **Java 17**
- **Spring Boot 3.x**
- **MongoDB** (armazenamento de entidades)
- **Docker SDK for Java** (`docker-java`)
- **Docker / Docker Compose**

---

## ⚙️ Pré-requisitos

Antes de rodar a aplicação, instale:

- [Java 17+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

---

## 🔧 Configuração

### 📌 `application.properties`

Por padrão, a aplicação já está configurada para usar **MongoDB local**.

```properties
# Nome da aplicação
spring.application.name=pipebot

# Porta
server.port=8081

# MongoDB local
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=pipebotdb

# CORS - Ambiente Dev
cors.allowed-origins.dev=http://localhost:8080

# CORS - Ambiente Prod
cors.allowed-origins.prd=https://meusite.com
```

👉 Caso use **Docker Compose**, as variáveis do banco também podem ser lidas automaticamente via `environment`.

---

## ▶️ Rodando Localmente

### 1. Clonar o repositório
```bash
git clone https://github.com/seuusuario/pipebot.git
cd pipebot
```

### 2. Buildar a aplicação
```bash
mvn clean package -DskipTests
```

### 3. Rodar
```bash
java -jar target/pipebot-0.0.1-SNAPSHOT.jar
```

Acesse em:  
👉 [http://localhost:8081](http://localhost:8081)

---

## 🐳 Rodando com Docker

### Build da imagem
```bash
docker build -t pipebot:latest .
```

### Rodar o container
```bash
docker run -p 8081:8081 pipebot:latest
```

---

## 🐙 Rodando com Docker Compose

Crie um `docker-compose.yml`:

```yaml
version: "3.8"
services:
  pipebot:
    image: murilonerdx/pipebot:latest
    container_name: pipebot
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATA_MONGODB_HOST=mongodb
    depends_on:
      - mongodb

  mongodb:
    image: mongo:6
    container_name: mongodb
    ports:
      - "27017:27017"
    volumes:
      - ./mongo-data:/data/db
```

Rodar:
```bash
docker compose up -d
```

---

## 🌐 Endpoints Principais

### 🔹 Docker Compose
- `POST /api/v1/docker/compose/up` → Sobe stack docker-compose
- `POST /api/v1/docker/compose/down` → Para stack
- `GET /api/v1/docker/compose/files` → Lista arquivos `docker-compose.yml` locais
- `GET /api/v1/docker/compose/download?path=...` → Baixa arquivo compose

### 🔹 Containers
- `GET /api/v1/docker/containers` → Lista containers
- `POST /api/v1/docker/containers/start/{id}` → Inicia container
- `POST /api/v1/docker/containers/stop/{id}` → Para container
- `DELETE /api/v1/docker/containers/{id}` → Remove container
- `POST /api/v1/docker/containers/scale?image=nginx&baseName=test&replicas=3` → Escala containers

### 🔹 Imagens
- `GET /api/v1/docker/images` → Lista imagens
- `POST /api/v1/docker/images/pull?image=nginx:latest` → Puxa imagem
- `POST /api/v1/docker/images/build?path=./&tag=pipebot:custom` → Builda imagem
- `DELETE /api/v1/docker/images/{id}` → Remove imagem

### 🔹 Logs
- `GET /api/v1/docker/logs` → Lista execuções registradas

---

## 📤 Publicando no Docker Hub

### Login
```bash
docker login
```

### Tag
```bash
docker tag pipebot:latest murilonerdx/pipebot:latest
```

### Push
```bash
docker push murilonerdx/pipebot:latest
```

---

## 📌 Roadmap

- [ ] UI para gerenciar containers e compose
- [ ] Integração com Kubernetes
- [ ] Autenticação JWT nos endpoints

---

## 📜 Licença
Este projeto está sob a licença MIT.  