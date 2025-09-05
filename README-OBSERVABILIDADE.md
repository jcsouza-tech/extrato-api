# 🔍 Observabilidade - Extrato API

## 🚀 **Início Rápido**

### **Opção 1: Script Automático (Recomendado)**
```bash
# Windows (PowerShell)
.\start-observability.ps1

# Windows (CMD)
start-observability.bat

# Linux/Mac
docker-compose up -d
```

### **Opção 2: Manual**
```bash
# Subir todos os serviços
docker-compose up -d

# Verificar status
docker-compose ps

# Parar serviços
docker-compose down
```

## 📊 **Acessos**

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **Grafana Dashboard** | http://localhost:3000 | admin / admin |
| **Prometheus** | http://localhost:9090 | - |
| **Zipkin Tracing** | http://localhost:9411 | - |
| **MySQL** | localhost:3306 | appuser / apppassword |

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
- ✅ **Data Source Prometheus** - Conecta ao Prometheus
- ✅ **Dashboard Completo** - 12 painéis prontos
- ✅ **Volumes persistentes** - Dados não são perdidos
- ✅ **Plugins necessários** - Piechart e outros

### **Estrutura de arquivos:**
```
grafana/
├── dashboards/
│   └── dashboard-completo-funcional.json
└── provisioning/
    ├── datasources/
    │   └── prometheus.yml
    └── dashboards/
        └── dashboard.yml
```

## 📈 **Métricas Disponíveis**

### **Métricas HTTP:**
- Requisições por endpoint
- Tempo de resposta
- Códigos de status
- Taxa de erro

### **Métricas JVM:**
- Uso de memória heap
- Garbage Collection
- Threads ativas
- Uso de CPU

### **Métricas JDBC:**
- Conexões ativas/idle
- Pool de conexões
- Performance do banco

### **Métricas do Sistema:**
- Uptime da aplicação
- Uso de CPU
- Uso de memória

## 🚨 **Alertas e Troubleshooting**

### **Status da Aplicação = DOWN:**
1. Verificar se a aplicação está rodando
2. Verificar logs: `docker logs <container_name>`
3. Verificar recursos do sistema

### **Taxa de Erro > 1%:**
1. Verificar logs de erro da aplicação
2. Analisar endpoints específicos no dashboard
3. Verificar dependências externas

### **Tempo de Resposta > 1s:**
1. Verificar uso de CPU/Memória
2. Analisar consultas ao banco
3. Verificar dependências lentas

### **Dashboard não carrega:**
1. Verificar se Grafana está rodando: `docker-compose ps`
2. Verificar logs: `docker logs grafana`
3. Verificar se Prometheus está coletando dados

## 🔄 **Comandos Úteis**

### **Gerenciamento de Containers:**
```bash
# Ver status
docker-compose ps

# Ver logs
docker-compose logs grafana
docker-compose logs prometheus

# Reiniciar serviço
docker-compose restart grafana

# Parar tudo
docker-compose down

# Parar e remover volumes
docker-compose down -v
```

### **Backup e Restore:**
```bash
# Backup do Grafana
docker run --rm -v grafana_data:/data -v $(pwd):/backup alpine tar czf /backup/grafana-backup.tar.gz -C /data .

# Restore do Grafana
docker run --rm -v grafana_data:/data -v $(pwd):/backup alpine tar xzf /backup/grafana-backup.tar.gz -C /data
```

## 📚 **Documentação Completa**

- **Guia Detalhado:** [GUIA-DASHBOARD-COMPLETO.md](GUIA-DASHBOARD-COMPLETO.md)
- **Prometheus:** https://prometheus.io/docs/
- **Grafana:** https://grafana.com/docs/
- **Spring Boot Actuator:** https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

## 🎉 **Próximos Passos**

1. **Configurar Alertas** - Notificações por email/Slack
2. **Adicionar Métricas Customizadas** - Métricas específicas do negócio
3. **Configurar Logs Estruturados** - Correlação com traces
4. **Implementar Health Checks** - Verificações de saúde
5. **Configurar Backup Automático** - Backup dos dashboards

---

**🚀 Agora você tem um sistema completo de observabilidade funcionando!**
