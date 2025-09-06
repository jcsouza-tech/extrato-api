# 🔍 Observabilidade - Extrato API

## 🚀 **Início Rápido**

### **Executar a Aplicação (Tudo junto!)**
```bash
# Executar a aplicação Spring Boot
# A aplicação automaticamente sobe o Docker Compose com:
# - MySQL Database
# - Prometheus (coleta de métricas)
# - Grafana (dashboards)
# - Zipkin (tracing)
mvn spring-boot:run
```

### **Verificar se tudo está funcionando:**
```bash
# Verificar status dos containers
docker-compose ps

# Ver logs em tempo real
docker-compose logs -f

# Parar todos os serviços
docker-compose down

# Parar e remover volumes (cuidado: apaga dados)
docker-compose down -v
```

### **✅ Vantagem:**
- **Um comando só** inicia a aplicação + observabilidade completa
- **Configuração automática** - não precisa gerenciar containers separadamente
- **Tudo integrado** - aplicação, banco, métricas e dashboards juntos

## 🔄 **Fluxo Completo de Execução**

### **Passo a Passo:**

1. **Executar um comando só:**
   ```bash
   mvn spring-boot:run
   ```

2. **Aguardar inicialização completa:**
   - A aplicação sobe automaticamente
   - Docker Compose inicia MySQL, Prometheus, Grafana e Zipkin
   - Aguardar alguns segundos para tudo ficar pronto

3. **Verificar se tudo está funcionando:**
   ```bash
   # Testar se a API responde
   curl http://localhost:8080/actuator/health
   
   # Verificar containers
   docker-compose ps
   
   # Verificar métricas
   curl http://localhost:9090/api/v1/targets
   ```

4. **Acessar os Dashboards:**
   - **API:** http://localhost:8080/swagger-ui.html
   - **Grafana:** http://localhost:3000 (admin/admin)
   - **Prometheus:** http://localhost:9090
   - **Zipkin:** http://localhost:9411

### **🎯 Simplicidade:**
- **Um comando** inicia tudo
- **Configuração automática** - sem setup manual
- **Integração completa** - aplicação + observabilidade

## 📊 **Acessos**

| Serviço | URL | Credenciais | Descrição |
|---------|-----|-------------|-----------|
| **Grafana Dashboard** | http://localhost:3000 | admin / admin | Dashboards de monitoramento |
| **Prometheus** | http://localhost:9090 | - | Coleta de métricas |
| **Zipkin Tracing** | http://localhost:9411 | - | Rastreamento distribuído |
| **MySQL Database** | localhost:3306 | appuser / apppassword | Banco de dados principal |
| **Extrato API** | http://localhost:8080 | - | API principal (quando rodando) |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - | Documentação da API |

## 🎯 **O que você verá no Dashboard**

### **Dashboard Completo - 12 Painéis:**

#### **🔴 Linha 1 - KPIs Principais:**
1. **Status da Aplicação** - 🟢 UP / 🔴 DOWN
2. **Requisições por Minuto** - Carga de tráfego
3. **Tempo de Resposta Médio** - Performance
4. **Taxa de Erro (%)** - 🟢 < 1% / 🟡 1-5% / 🔴 > 5%

#### **📈 Linha 2 - Análise de Tráfego:**
5. **Requisições por Endpoint** - Por URI
6. **Status HTTP** - Distribuição de códigos

#### **💾 Linha 3 - Infraestrutura:**
7. **Uso de Memória JVM** - Heap memory
8. **Conexões JDBC** - Pool de conexões

#### **📊 Linha 4 - Contadores:**
9. **Total de Requisições** - Contador geral
10. **Requisições de Sucesso** - Status 2xx
11. **Requisições com Erro** - Status 4xx/5xx
12. **Uptime da Aplicação** - Tempo online

## 🔧 **Configuração Automática**

### **O que é configurado automaticamente:**
- ✅ **Data Source Prometheus** - Conecta automaticamente ao Prometheus
- ✅ **Dashboard Completo** - 12 painéis de monitoramento prontos
- ✅ **Volumes persistentes** - Dados do Grafana são mantidos entre reinicializações
- ✅ **Plugin Piechart** - Instalado automaticamente para gráficos de pizza
- ✅ **Health Checks** - Verificação de saúde dos containers
- ✅ **Dependências** - Ordem correta de inicialização dos serviços

### **Estrutura de arquivos de configuração:**
```
grafana/
├── dashboards/
│   └── dashboard-completo-funcional.json    # Dashboard principal
└── provisioning/
    ├── datasources/
    │   └── prometheus.yml                   # Configuração do Prometheus
    └── dashboards/
        └── dashboard.yml                    # Configuração dos dashboards
```

### **Arquivos de configuração:**
- `compose.yaml` - Orquestração dos serviços Docker
- `prometheus.yml` - Configuração do Prometheus
- `grafana/provisioning/` - Configurações automáticas do Grafana

## 📈 **Métricas Disponíveis**

### **Métricas HTTP (Spring Boot Actuator):**
- `http_server_requests_seconds` - Tempo de resposta por endpoint
- `http_server_requests_total` - Total de requisições por status HTTP
- `http_server_requests_created` - Timestamp das requisições
- Taxa de erro por endpoint e método HTTP

### **Métricas JVM:**
- `jvm_memory_used_bytes` - Uso de memória heap/non-heap
- `jvm_memory_max_bytes` - Memória máxima disponível
- `jvm_gc_pause_seconds` - Tempo de pausa do Garbage Collection
- `jvm_threads_live` - Threads ativas
- `jvm_threads_daemon` - Threads daemon

### **Métricas JDBC (HikariCP):**
- `hikaricp_connections_active` - Conexões ativas
- `hikaricp_connections_idle` - Conexões idle
- `hikaricp_connections_pending` - Conexões pendentes
- `hikaricp_connections_timeout` - Timeouts de conexão

### **Métricas Customizadas (Extrato API):**
- `extrato_arquivos_processados_total` - Total de arquivos processados
- `extrato_transacoes_processadas_total` - Total de transações processadas
- `extrato_duplicatas_ignoradas_total` - Total de duplicatas ignoradas
- `extrato_erros_processamento_total` - Total de erros de processamento

### **Métricas do Sistema:**
- `process_uptime_seconds` - Uptime da aplicação
- `system_cpu_usage` - Uso de CPU do sistema
- `system_memory_usage` - Uso de memória do sistema

## 🚨 **Alertas e Troubleshooting**

### **Status da Aplicação = DOWN:**
1. **Verificar se a aplicação está rodando:**
   ```bash
   # Verificar se a API está respondendo
   curl http://localhost:8080/actuator/health
   
   # Se não responder, verificar se o processo está rodando
   netstat -an | findstr :8080
   ```

2. **Verificar se o Docker Compose foi iniciado:**
   ```bash
   # Verificar containers
   docker-compose ps
   
   # Se não estiver rodando, a aplicação não iniciou o Docker Compose
   # Verificar logs da aplicação para erros
   ```

3. **Verificar dependências:**
   - **Aplicação foi iniciada?** Verificar se `mvn spring-boot:run` foi executado
   - **Docker está rodando?** Verificar se o Docker Desktop está ativo
   - **Porta 8080 está livre?** `netstat -an | findstr :8080`

### **Taxa de Erro > 1%:**
1. **Verificar logs de erro:**
   ```bash
   # Logs da aplicação
   docker logs <container_name> | grep ERROR
   
   # Logs do Spring Boot
   docker logs <container_name> | grep "Exception"
   ```

2. **Analisar no dashboard:**
   - Verificar painel "Status HTTP" para códigos 4xx/5xx
   - Verificar painel "Requisições por Endpoint" para endpoints problemáticos

### **Tempo de Resposta > 1s:**
1. **Verificar recursos:**
   - CPU: Painel "Uso de CPU"
   - Memória: Painel "Uso de Memória JVM"
   - Conexões DB: Painel "Conexões JDBC"

2. **Verificar dependências:**
   - Performance do MySQL
   - Latência de rede
   - Consultas lentas no banco

### **Dashboard não carrega:**
1. **Verificar containers:**
   ```bash
   docker-compose ps
   # Todos devem estar "Up" e "healthy"
   ```

2. **Verificar logs do Grafana:**
   ```bash
   docker logs grafana
   # Procurar por erros de conexão com Prometheus
   ```

3. **Verificar Prometheus:**
   ```bash
   # Acessar http://localhost:9090/targets
   # Verificar se a aplicação está sendo coletada
   ```

### **Problemas Comuns:**

#### **"No data" nos painéis:**
- **Aplicação não iniciou o Docker Compose** - Verificar se `mvn spring-boot:run` foi executado
- **Aguardar inicialização** - Docker Compose demora alguns segundos para subir
- **Verificar containers** - `docker-compose ps` deve mostrar todos os serviços UP

#### **Grafana não conecta no Prometheus:**
- **Aguardar inicialização completa** - Prometheus demora para ficar pronto
- Verificar se Prometheus está rodando: `docker-compose ps prometheus`
- **Verificar se a aplicação está rodando** - Prometheus precisa da aplicação para coletar métricas

#### **Métricas customizadas não aparecem:**
- **Fazer algumas requisições** - Usar o Swagger UI para gerar tráfego
- **Aguardar coleta** - Prometheus precisa de tempo para coletar métricas
- **Verificar se `/actuator/prometheus` está expondo** - Acessar http://localhost:8080/actuator/prometheus

#### **Dashboard vazio mesmo com aplicação rodando:**
- **Aguardar alguns minutos** - Prometheus precisa de tempo para coletar métricas
- **Fazer requisições na API** - Usar o Swagger UI para gerar tráfego
- **Verificar se o Prometheus está coletando** - Acessar http://localhost:9090/targets

#### **Docker Compose não inicia:**
- **Verificar se Docker está rodando** - Docker Desktop deve estar ativo
- **Verificar se a porta 8080 está livre** - `netstat -an | findstr :8080`
- **Verificar logs da aplicação** - Pode haver erro na inicialização do Docker Compose

## 🔄 **Comandos Úteis**

### **Gerenciamento de Containers:**
```bash
# Ver status de todos os containers
docker-compose ps

# Ver logs em tempo real
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs grafana
docker-compose logs prometheus
docker-compose logs mysql

# Reiniciar um serviço específico
docker-compose restart grafana

# Parar todos os serviços
docker-compose down

# Parar e remover volumes (CUIDADO: apaga dados)
docker-compose down -v

# Reconstruir containers
docker-compose up -d --build
```

### **Verificação de Saúde:**
```bash
# Verificar se a API está respondendo
curl http://localhost:8080/actuator/health

# Verificar métricas do Prometheus
curl http://localhost:9090/api/v1/targets

# Verificar status do Grafana
curl http://localhost:3000/api/health

# Verificar logs de erro
docker-compose logs | grep -i error
```

### **Backup e Restore:**
```bash
# Backup do Grafana (dashboards e configurações)
docker run --rm -v grafana_data:/data -v $(pwd):/backup alpine tar czf /backup/grafana-backup.tar.gz -C /data .

# Restore do Grafana
docker run --rm -v grafana_data:/data -v $(pwd):/backup alpine tar xzf /backup/grafana-backup.tar.gz -C /data

# Backup do MySQL
docker exec mysql_db mysqldump -u appuser -papppassword financas_db > backup-database.sql

# Restore do MySQL
docker exec -i mysql_db mysql -u appuser -papppassword financas_db < backup-database.sql
```

## 📚 **Documentação Completa**

- **README Principal:** [README.md](README.md) - Documentação completa da API
- **Estratégia de Testes:** [ESTRATEGIA-TESTES.md](ESTRATEGIA-TESTES.md) - Estratégia de testes implementada
- **Prometheus:** https://prometheus.io/docs/
- **Grafana:** https://grafana.com/docs/
- **Spring Boot Actuator:** https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **Docker Compose:** https://docs.docker.com/compose/

## 🎯 **Funcionalidades Implementadas**

### **✅ Observabilidade Completa:**
- **Prometheus** - Coleta de métricas automática
- **Grafana** - Dashboards visuais com 12 painéis
- **Zipkin** - Rastreamento distribuído de requisições
- **Health Checks** - Verificação de saúde dos containers

### **✅ Métricas Customizadas:**
- Arquivos processados
- Transações processadas
- Duplicatas ignoradas
- Erros de processamento

### **✅ Configuração Automática:**
- Data sources provisionados
- Dashboards carregados automaticamente
- Volumes persistentes configurados
- Dependências entre serviços

## 🎉 **Próximos Passos**

1. **Configurar Alertas** - Notificações por email/Slack quando métricas ultrapassarem limites
2. **Adicionar Métricas de Negócio** - Métricas específicas do processamento de extratos
3. **Configurar Logs Estruturados** - Correlação entre logs e traces
4. **Implementar Health Checks Avançados** - Verificações de dependências externas
5. **Configurar Backup Automático** - Backup automático dos dashboards e configurações
6. **Adicionar Métricas de Performance** - Latência de processamento de arquivos
7. **Implementar Métricas de Qualidade** - Taxa de sucesso por tipo de arquivo

---

**🚀 Sistema de observabilidade completo e funcional para a Extrato API!**
