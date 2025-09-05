# 🏦 Extrato API

API para processamento e visualização de extratos bancários com suporte a múltiplos bancos.

## 📋 Sobre a Aplicação

A **Extrato API** é uma aplicação Spring Boot que permite o upload, processamento e visualização de extratos bancários em formato CSV. A aplicação foi desenvolvida com foco em:

- **Processamento de extratos** de diferentes bancos
- **Validação** de dados monetários
- **Transações atômicas** com rollback automático
- **Observabilidade completa** com Prometheus, Grafana e Zipkin
- **Arquitetura HATEOAS** para APIs RESTful

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.6+
- Docker e Docker Compose

### Execução Local

1. **Clone o repositório:**
```bash
git clone <repository-url>
cd extrato-api
```

2. **Execute a aplicação:**
```bash
mvn spring-boot:run
```

3. **A aplicação estará disponível em:**
- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs
- **Health Check:** http://localhost:8080/actuator/health

### Execução com Docker

1. **Execute com Docker Compose:**
```bash
docker-compose up -d
```

2. **Acesse os serviços:**
- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs
- **Grafana:** http://localhost:3000 (admin/admin)
- **Prometheus:** http://localhost:9090
- **Zipkin:** http://localhost:9411

## 📚 Documentação da API

### Swagger UI
A aplicação inclui documentação interativa da API através do Swagger UI:

- **URL:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs

**Recursos disponíveis:**
- Documentação completa de todos os endpoints
- Exemplos de requisições e respostas
- Interface para testar a API diretamente
- Esquemas de dados detalhados
- Códigos de status HTTP explicados

### Como usar o Swagger:
1. Acesse http://localhost:8080/swagger-ui.html
2. Explore os endpoints disponíveis
3. Clique em "Try it out" para testar
4. Preencha os parâmetros necessários
5. Execute a requisição e veja a resposta

## 📊 Observabilidade

A aplicação inclui monitoramento completo:

### Grafana Dashboard
- **URL:** http://localhost:3000
- **Login:** admin/admin
- **Dashboard:** "Extrato API - Monitoramento Completo"

### Métricas Disponíveis
- Taxa de processamento de arquivos
- Número de transações processadas
- Tempo de resposta da API
- Uso de memória e CPU
- Health checks dos componentes

### Tracing
- **Zipkin:** http://localhost:9411
- Rastreamento completo de requisições
- Análise de performance end-to-end

## 🔌 Endpoints da API

### 1. Upload de Extrato

**POST** `/api/v1/extrato/upload`

Upload de arquivo CSV com extrato bancário.

**Parâmetros:**
- `arquivo` (MultipartFile): Arquivo CSV do extrato
- `banco` (String): Nome do banco (ex: "banco-do-brasil")

**Exemplo de uso:**
```bash
curl -X POST "http://localhost:8080/api/v1/extrato/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "arquivo=@extrato_bb.csv" \
  -F "banco=banco-do-brasil"
```

**Resposta de sucesso:**
```json
{
  "sucesso": true,
  "mensagem": "Arquivo processado com sucesso",
  "transacoesSalvas": 150,
  "duplicatasIgnoradas": 5,
  "uploadId": 123
}
```

### 2. Visualizar Extrato

**GET** `/api/v1/extrato`

Lista todas as transações com paginação e filtros.

**Parâmetros de query:**
- `page` (int): Página (padrão: 0)
- `size` (int): Tamanho da página (padrão: 20)
- `banco` (String): Filtrar por banco
- `dataInicio` (String): Data início (yyyy-MM-dd)
- `dataFim` (String): Data fim (yyyy-MM-dd)

**Exemplo de uso:**
```bash
curl "http://localhost:8080/api/v1/extrato?page=0&size=10&banco=banco-do-brasil"
```

**Resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "data": "2024-01-15",
      "lancamento": "SAQUE",
      "detalhes": "SAQUE 24H 001",
      "numeroDocumento": "123456",
      "valor": 1234.56,
      "tipoLancamento": "SAQUE",
      "banco": "banco-do-brasil",
      "links": [
        {
          "rel": "self",
          "href": "http://localhost:8080/api/v1/extrato/1"
        }
      ]
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 150,
    "totalPages": 15
  },
  "links": [
    {
      "rel": "self",
      "href": "http://localhost:8080/api/v1/extrato?page=0&size=10"
    },
    {
      "rel": "next",
      "href": "http://localhost:8080/api/v1/extrato?page=1&size=10"
    }
  ]
}
```

### 3. Health Check

**GET** `/actuator/health`

Verifica a saúde da aplicação.

**Resposta:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

## 🏦 Bancos Suportados

### Banco do Brasil ✅
- **Formato:** CSV com separador `,`
- **Encoding:** ISO-8859-1
- **Campos:** Data, Lançamento, Detalhes, Número do Documento, Valor, Tipo do Lançamento
- **Valores monetários:** Formato brasileiro (1.234,56)

**Exemplo de arquivo(Baixado do app):**
```csv
Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
15/01/2024,SAQUE,SAQUE 24H 001,123456,1234,56,SAQUE
16/01/2024,DEPOSITO,DEPOSITO EM CONTA,789012,2500,00,DEPOSITO
```

## 🔧 Configuração

### application.yml
```yaml
# Configurações principais
spring:
  application:
    name: extrato-api
  datasource:
    url: jdbc:h2:mem:extrato
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
# Configurações de parser
parser:
  config:
    banco-do-brasil:
      csv:
        separator: ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"
        encoding: ISO-8859-1
        file-patterns: 
          - ".*banco.*bb.*\\.csv$"
          - ".*bb.*\\.csv$"
      validation:
        required-fields: ["Data", "Lançamento", "Detalhes", "Número do Documento", "Valor", "Tipo do Lançamento"]
```

## 🧪 Testes

### Executar Testes
```bash
# Todos os testes
mvn test

# Testes com relatório de cobertura
mvn clean test jacoco:report

# Apenas testes unitários
mvn test -Dtest="*Unit*"

# Apenas testes de integração
mvn test -Dtest="*Integration*"
```

### Cobertura de Testes
- **Cobertura Total:** 76%
- **Cobertura de Branches:** 68%
- **Cobertura de Linhas:** 72%

Relatório disponível em: `target/site/jacoco/index.html`

## 📈 Próximos Passos

### 🏦 Implementação do Itaú

**Objetivo:** Adicionar suporte ao processamento de extratos do Banco Itaú.

#### Tarefas:
1. **Criar parser específico:**
   - `ItauParser.java` - Parser para formato Itaú
   - `ItauValidation.java` - Validações específicas
   - `ItauService.java` - Service de processamento

2. **Configurar formato Itaú:**
   - Definir separadores e encoding
   - Mapear campos específicos
   - Configurar validações

3. **Testes:**
   - Testes unitários do parser
   - Testes de integração
   - Testes com dados reais

4. **Documentação:**
   - Atualizar README com formato Itaú
   - Exemplos de arquivos
   - Guia de migração

#### Formato Esperado Itaú:
```pdf
```

### 🔄 Melhorias Futuras
1. **Funcionalidades:**
   - Categorização automática de transações
   - Relatórios personalizados

2. **Performance:**
   - Processamento assíncrono
   - Cache de consultas

## 🛠️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Data JPA**
- **Spring HATEOAS**
- **H2 Database** (desenvolvimento)
- **HikariCP** (connection pooling)
- **JTA Atomikos** (transações distribuídas)
- **Prometheus** (métricas)
- **Grafana** (dashboards)
- **Zipkin** (tracing)
- **Docker** (containerização)
- **Maven** (build)

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

