# Monitoramento da aplicação `fiap-x-microsservice-auth`

Este guia documenta a implementação de observabilidade separada da aplicação usando:

- **Prometheus** para métricas
- **Grafana** para visualização
- **Loki** para centralização de logs
- **Promtail** para coleta dos logs da aplicação

A organização foi mantida dentro do diretório `monitoring/`, como você pediu.

---

## 1. Objetivo da abordagem adotada

A estratégia usada aqui é a de **stack de monitoramento separada da aplicação**.

Isso significa que:

- a aplicação continua sendo executada no projeto principal;
- o monitoramento sobe em um `docker-compose` separado dentro de `monitoring/`;
- o Prometheus coleta métricas expostas pela aplicação via Actuator;
- o Promtail lê os logs gerados pela aplicação e envia para o Loki;
- o Grafana consome Prometheus e Loki como fontes de dados.

### Vantagens dessa abordagem

- separa responsabilidade da aplicação e da observabilidade;
- facilita replicar o mesmo padrão para outros microsserviços;
- evita acoplamento excessivo no `docker-compose` principal;
- permite evoluir dashboards e alertas sem mexer na aplicação.

---

## 2. O que foi configurado

### 2.1. Aplicação (`application.yml`)

Arquivo alterado: `src/main/resources/application.yml`

### O que foi feito

1. **Mantive os endpoints do Actuator expostos**:
   - `health`
   - `info`
   - `metrics`
   - `prometheus`

2. **Garanti a exportação de métricas Prometheus**:
   - `management.prometheus.metrics.export.enabled: true`

3. **Adicionei configuração de log em arquivo**:
   - `logging.file.name: ./logs/fiap-x-auth.log`

4. **Adicionei um pattern de log de arquivo** para facilitar leitura pelo Promtail.

### Por que isso é importante

- O **Prometheus** precisa do endpoint `/actuator/prometheus`.
- O **Promtail** precisa de um arquivo físico de log para ler e enviar ao Loki.
- Sem isso, a stack sobe, mas não coleta nada útil.

---

### 2.2. `docker-compose.yml` principal

Arquivo alterado: `docker-compose.yml`

### O que foi feito

1. Adicionei um volume nomeado chamado `app_logs`.
2. Montei esse volume no container da aplicação em `/app/logs`.
3. Passei a variável:
   - `LOG_FILE_PATH=/app/logs/fiap-x-auth.log`
4. Reforcei as variáveis de observabilidade do Actuator/Prometheus.

### Observação importante

A stack de `monitoring/` **não depende mais** desse volume Docker para o Promtail.

Agora o Promtail usa um **bind mount local** da pasta:

- `../logs`

Isso foi feito para evitar o erro:

- `external volume "fiap-x-auth-services_app_logs" not found`

Ou seja:

- a aplicação continua podendo gravar logs em volume/container;
- o monitoramento passou a usar a pasta local `logs/` do projeto, que é mais simples para desenvolvimento local no Windows.

---

### 2.3. Stack `monitoring/docker-compose.yml`

Arquivo alterado: `monitoring/docker-compose.yml`

### O que foi feito

1. Corrigi os mounts dos arquivos de configuração.
2. Montei o arquivo real do Loki:
   - `./loki/loki-config.yml`
3. Montei o arquivo real do Promtail:
   - `./promtail/promtail-config.yml`
4. Configurei volumes persistentes para:
   - Loki
   - Grafana
5. Configurei o Promtail para ler logs por bind mount local:
   - `../logs:/var/log/app:ro`
6. Adicionei dependências entre Grafana, Prometheus e Loki.

### Por que isso é importante

Antes, a stack dependia de um volume externo do Docker. Isso causava falha de subida quando o volume ainda não existia.

Agora a stack sobe sem depender desse volume externo.

---

### 2.4. Prometheus

Arquivo alterado: `monitoring/prometheus/prometheus.yml`

### O que foi feito

1. Mantive o scrape do serviço auth em:
   - `host.docker.internal:8082`
   - path `/actuator/prometheus`

2. Adicionei auto-monitoramento do próprio Prometheus:
   - `localhost:9090`

3. Mantive labels úteis no target:
   - `application=fiap-x-microservice-auth`
   - `environment=local`

### Por que isso é importante

- `host.docker.internal` permite que o container do Prometheus acesse a aplicação rodando fora do compose de monitoramento.
- As labels ajudam muito no Grafana, filtros e futuras replicações.

> Observação importante: em Docker Desktop no Windows isso costuma funcionar bem. Se no futuro você rodar em outro ambiente, talvez precise ajustar o hostname/alvo de scrape.

---

### 2.5. Alertas do Prometheus

Arquivo mantido e reutilizado: `monitoring/prometheus/alerts.yml`

### Alertas existentes

- `AuthServiceDown`
- `AuthHigh5xxRate`
- `AuthHighJvmHeapUsage`

### O que eles monitoram

- indisponibilidade da aplicação;
- taxa alta de erro HTTP 5xx;
- uso elevado de heap JVM.

---

### 2.6. Loki

Arquivo alterado: `monitoring/loki/loki-config.yml`

### O que foi feito

1. Completei a configuração mínima para execução local estável:
   - `ring`
   - `replication_factor`
   - `boltdb_shipper`
   - `filesystem`
   - `limits_config`

2. Configurei persistência em `/loki`.

---

### 2.7. Promtail

Arquivo alterado: `monitoring/promtail/promtail-config.yml`

### O que foi feito

1. Configurei o Promtail para ler:
   - `/var/log/app/*.log`

2. No compose, esse caminho recebe um bind mount local de:
   - `../logs`

3. Adicionei labels úteis:
   - `job=fiap-x-auth`
   - `application=fiap-x-microservice-auth`
   - `environment=local`

4. Mantive envio para:
   - `http://loki:3100/loki/api/v1/push`

### Por que isso é importante

Essa abordagem evita dependência de volume externo do Docker e funciona melhor no cenário local que você está usando.

---

### 2.8. Grafana - datasources

Arquivo alterado: `monitoring/grafana/provisioning/datasources/prometheus.yml`

### O que foi feito

Provisionei automaticamente duas fontes de dados:

- **Prometheus** (`uid: prometheus`)
- **Loki** (`uid: loki`)

---

### 2.9. Grafana - dashboard

Arquivo alterado: `monitoring/grafana/dashboards/auth-overview.json`

### O que foi feito

Atualizei o dashboard para mostrar:

1. **Status da aplicação** (`up`)
2. **Requests por segundo**
3. **Taxa de erro 5xx**
4. **Uso de heap JVM**
5. **Latência HTTP p95**
6. **Métricas customizadas de login**
7. **Métricas customizadas de refresh token**
8. **Métricas customizadas de validate token**
9. **Logs da aplicação via Loki**

---

## 3. Estrutura final da pasta `monitoring/`

```text
monitoring/
  docker-compose.yml
  README.md
  prometheus/
    prometheus.yml
    alerts.yml
  loki/
    loki-config.yml
  promtail/
    promtail-config.yml
  grafana/
    dashboards/
      auth-overview.json
    provisioning/
      dashboards/
        dashboards.yml
      datasources/
        prometheus.yml
```

---

## 4. Pré-requisito para logs locais

Como o Promtail lê a pasta local `logs/`, ela precisa existir no projeto.

Caminho esperado:

- `fiap-x-microsservice-auth/logs/fiap-x-auth.log`

Se necessário, crie manualmente:

```powershell
cd "C:\Users\Vitória Vitoria\Documents\Projetos\Pós\fase 5\fiap-x-microsservice-auth"
mkdir logs
ni .\logs\fiap-x-auth.log -ItemType File
```

---

## 5. Como subir a solução

### Passo 1 - subir a aplicação principal

Na raiz do projeto:

```powershell
cd "C:\Users\Vitória Vitoria\Documents\Projetos\Pós\fase 5\fiap-x-microsservice-auth"
docker compose up -d --build
```

### Passo 2 - subir a stack de monitoramento

Dentro de `monitoring/`:

```powershell
cd "C:\Users\Vitória Vitoria\Documents\Projetos\Pós\fase 5\fiap-x-microsservice-auth\monitoring"
docker compose up -d
```

---

## 6. Endereços para acessar

### Aplicação

- Health: `http://localhost:8082/actuator/health`
- Prometheus metrics: `http://localhost:8082/actuator/prometheus`

### Prometheus

- `http://localhost:9090`

### Grafana

- `http://localhost:3000`

Credenciais padrão:

- usuário: `admin`
- senha: `admin`

### Loki

- `http://localhost:3100`

---

## 7. Como validar se está funcionando

### 7.1. Validar aplicação

```powershell
Invoke-WebRequest http://localhost:8082/actuator/health
Invoke-WebRequest http://localhost:8082/actuator/prometheus
```

### 7.2. Validar Prometheus

No Prometheus, abra:

- `Status > Targets`

O target `auth-service` deve aparecer como **UP**.

### 7.3. Validar Grafana

Ao abrir o Grafana:

- o datasource Prometheus deve existir;
- o datasource Loki deve existir;
- o dashboard `FIAP-X Auth Observability` deve aparecer automaticamente.

### 7.4. Validar Loki/Promtail

No Grafana, em **Explore**, use:

```logql
{job="fiap-x-auth", application="fiap-x-microservice-auth"}
```

Se aparecerem logs, o pipeline está funcionando.

---

## 8. Fluxo completo da observabilidade

```text
Aplicação Spring Boot
  ├─ expõe métricas em /actuator/prometheus
  ├─ grava logs em ./logs/fiap-x-auth.log
  │
  ├──> Prometheus coleta métricas
  └──> Promtail lê logs locais e envia para Loki

Grafana
  ├─ lê métricas do Prometheus
  └─ lê logs do Loki
```

---

## 9. Resultado esperado

Ao final, você terá:

- métricas da aplicação em tempo real;
- dashboard pronto no Grafana;
- logs centralizados no Loki;
- estrutura organizada em `monitoring/`;
- padrão replicável para os outros microsserviços.
