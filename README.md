# FIAP-X Authentication Microservice

Microsserviço de autenticação construído com Kotlin, Spring Boot, e Clean Architecture.

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Configuração](#configuração)
- [API Endpoints](#api-endpoints)
- [Exemplos de Uso](#exemplos-de-uso)
- [Testes](#testes)
- [Deployment](#deployment)

## 🎯 Visão Geral

Este microsserviço é responsável pela autenticação e autorização de usuários no sistema FIAP-X. Ele fornece funcionalidades de:

- **Login de usuários** com validação de credenciais
- **Geração de tokens JWT** (access e refresh tokens)
- **Validação de tokens** JWT
- **Refresh de tokens** com rotação automática
- **Registro de tentativas de login**
- **Integração com User Service** via HTTP

## 🏗️ Arquitetura

O projeto segue os princípios da **Clean Architecture**, garantindo separação clara de responsabilidades:

```
src/main/kotlin/br/com/fiapx/auth/
├── domain/              # Camada de domínio (sem dependências de framework)
│   ├── model/          # Modelos de domínio
│   ├── repository/     # Interfaces de repositórios
│   ├── service/        # Interfaces de serviços
│   └── exception/      # Exceções de domínio
├── application/         # Camada de aplicação
│   └── usecase/        # Casos de uso (LoginUseCase, ValidateTokenUseCase, RefreshTokenUseCase)
├── infrastructure/      # Camada de infraestrutura
│   ├── jwt/            # Implementação JWT
│   ├── persistence/    # JPA entities, repositories
│   ├── client/         # Clientes HTTP (UserService)
│   └── security/       # Implementação BCrypt
├── interfaces/          # Camada de interface
│   ├── controller/     # REST Controllers
│   └── dto/            # DTOs de request/response
└── config/              # Configurações Spring
```

### Princípios Aplicados

- **Domain-Driven Design**: Domínio livre de dependências de framework
- **Dependency Inversion**: Interfaces no domínio, implementações na infraestrutura
- **Single Responsibility**: Cada classe com uma única responsabilidade
- **SOLID**: Todos os princípios SOLID aplicados

## 🛠️ Tecnologias

- **Kotlin** 2.2.0
- **Spring Boot** 4.0.2
- **Maven** - Gerenciamento de dependências
- **PostgreSQL** - Banco de dados
- **Flyway** - Migração de banco de dados
- **JWT (jjwt)** 0.12.6 - Geração e validação de tokens
- **Spring Security** - Segurança e RBAC
- **WebClient** - Cliente HTTP reativo
- **BCrypt** - Hash de senhas
- **JUnit 5** + **MockK** - Testes unitários
- **JaCoCo** - Cobertura de testes
- **OpenAPI/Swagger** - Documentação da API

## ⚙️ Configuração

### Variáveis de Ambiente

Configure as seguintes variáveis de ambiente antes de executar o serviço:

```bash
# Banco de Dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/auth_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT
JWT_SECRET=your-256-bit-secret-key-change-this-in-production-min-32-chars
JWT_ACCESS_EXPIRATION=900000        # 15 minutos em ms
JWT_REFRESH_EXPIRATION=604800000    # 7 dias em ms

# User Service
USER_SERVICE_URL=http://localhost:8080

# Server
PORT=8082

# Logging
LOG_LEVEL=DEBUG
SECURITY_LOG_LEVEL=DEBUG
```

### Configuração do Banco de Dados

1. Crie o banco de dados PostgreSQL:
```sql
CREATE DATABASE auth_db;
```

2. As tabelas serão criadas automaticamente pelo Flyway na primeira execução:
   - `refresh_tokens` - Armazena tokens de refresh
   - `login_attempts` - Registra tentativas de login

### Executando Localmente

```bash
# Compile o projeto
mvn clean install

# Execute os testes
mvn test

# Inicie o serviço
mvn spring-boot:run

# Ou usando variáveis de ambiente
JWT_SECRET=my-secret USER_SERVICE_URL=http://localhost:8080 mvn spring-boot:run
```

## 📡 API Endpoints

### 1. Login

Autentica um usuário e retorna tokens de acesso.

**Endpoint:** `POST /auth/login`

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 604800
}
```

**Errors:**
- `401 Unauthorized` - Credenciais inválidas
- `404 Not Found` - Usuário não encontrado
- `503 Service Unavailable` - User Service indisponível

### 2. Validar Token

Valida um token JWT e retorna as informações do usuário.

**Endpoint:** `POST /auth/validate`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:** `200 OK`
```json
{
  "valid": true,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "role": "USER"
}
```

**Errors:**
- `401 Unauthorized` - Token inválido ou expirado

### 3. Refresh Token

Gera um novo access token usando um refresh token válido.

**Endpoint:** `POST /auth/refresh`

**Request:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "660e8400-e29b-41d4-a716-446655440001",
  "tokenType": "Bearer"
}
```

**Errors:**
- `401 Unauthorized` - Refresh token inválido, expirado ou revogado

## 💡 Exemplos de Uso

### Exemplo com cURL

```bash
# 1. Login
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# 2. Validar Token
curl -X POST http://localhost:8082/auth/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "token": "YOUR_ACCESS_TOKEN"
  }'

# 3. Refresh Token
curl -X POST http://localhost:8082/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

### Exemplo com HTTPie

```bash
# Login
http POST :8082/auth/login email=user@example.com password=password123

# Validar Token
http POST :8082/auth/validate token=YOUR_ACCESS_TOKEN Authorization:"Bearer YOUR_ACCESS_TOKEN"

# Refresh Token
http POST :8082/auth/refresh refreshToken=YOUR_REFRESH_TOKEN
```

## 🔐 Segurança

### JWT Claims

Os tokens JWT contêm os seguintes claims:

```json
{
  "sub": "user-id-uuid",
  "email": "user@example.com",
  "role": "USER",
  "iat": 1234567890,
  "exp": 1234568790,
  "iss": "fiap-x-auth-service"
}
```

### RBAC (Role-Based Access Control)

O serviço suporta controle de acesso baseado em roles:

```kotlin
@PreAuthorize("hasRole('ADMIN')")
fun adminOnlyEndpoint() { ... }

@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
fun userEndpoint() { ... }
```

### Refresh Token Rotation

Por padrão, o refresh token rotation está habilitado. Cada vez que um refresh token é usado:
1. Um novo access token é gerado
2. Um novo refresh token é gerado
3. O refresh token antigo é revogado

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=LoginUseCaseTest

# Com relatório de cobertura
mvn clean test jacoco:report
```

### Cobertura de Testes

O projeto mantém **90%+ de cobertura** nos casos de uso:

| Componente | Cobertura |
|-----------|-----------|
| LoginUseCase | 96% |
| ValidateTokenUseCase | 96% |
| RefreshTokenUseCase | 96% |
| JwtServiceImpl | 96% |
| PasswordServiceImpl | 94% |

Relatório completo em: `target/site/jacoco/index.html`

### Testes Implementados

- ✅ Login com credenciais válidas
- ✅ Login com senha inválida
- ✅ Login com usuário inexistente
- ✅ Registro de tentativas de login
- ✅ Validação de token válido
- ✅ Validação de token expirado
- ✅ Validação de token inválido
- ✅ Refresh com token válido
- ✅ Refresh com token revogado
- ✅ Refresh com token expirado
- ✅ Rotação de refresh tokens

## BDD
Os cenários BDD de autenticação estão em `tests/bdd/authentication.feature`.

## 📚 Documentação da API

A documentação interativa da API está disponível via Swagger UI:

```
http://localhost:8082/swagger-ui.html
```

OpenAPI JSON:
```
http://localhost:8082/v3/api-docs
```

## 🚀 Deployment

### Docker

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
# Build
docker build -t fiap-x-auth-service .

# Run
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/auth_db \
  -e JWT_SECRET=your-secret \
  -e USER_SERVICE_URL=http://user-service:8080 \
  fiap-x-auth-service
```

### Docker Compose

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: auth_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
  
  auth-service:
    build: .
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/auth_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      JWT_SECRET: your-256-bit-secret
      USER_SERVICE_URL: http://user-service:8080
    depends_on:
      - postgres
```

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👥 Contribuindo

1. Fork o projeto
2. Crie uma feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📞 Suporte

Para questões e suporte, abra uma issue no repositório do GitHub.
