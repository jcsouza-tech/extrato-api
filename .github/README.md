# GitHub Actions - Extrato API

Esta pasta contém os workflows de CI/CD para o projeto extrato-api.

## 📁 Estrutura

```
.github/
├── workflows/
│   └── docker-build.yml        # Workflow completo
└── README.md                    # Este arquivo
```

## 🚀 Workflow de CI/CD

### `docker-build.yml`

Pipeline completo de CI/CD com:
- ✅ Execução de testes automatizados
- ✅ Geração de relatório de cobertura (JaCoCo)
- ✅ Build multi-arquitetura (amd64, arm64)
- ✅ Push automático para Docker Hub
- ✅ Scan de segurança com Trivy
- ✅ Cache otimizado para builds rápidos

**Quando é executado:**
- Push para branches: `main`, `develop`, `feature/**`
- Criação de tags: `v*.*.*`
- Pull Requests para `main` ou `develop` (apenas testes)

**Tags geradas:**
- `latest` - Última versão da branch main
- `<branch>-<hash>` - Identificador único por branch
- `v1.0.0`, `1.0.0`, `1.0`, `1` - Para releases

**Jobs executados:**
1. **test** - Executa testes e gera relatório de cobertura
2. **build-and-push** - Build e publicação da imagem Docker
3. **security-scan** - Scan de vulnerabilidades com Trivy

## 🔐 Secrets Necessários

Configure estes secrets no GitHub antes de usar:

| Secret | Descrição | Onde Obter |
|--------|-----------|------------|
| `DOCKERHUB_USERNAME` | Usuário do Docker Hub | Seu username em hub.docker.com |
| `DOCKERHUB_TOKEN` | Token de acesso | Docker Hub → Settings → Security → New Token |

**Configurar em:** `Settings → Secrets and variables → Actions → New repository secret`

## 📊 Monitoramento

### Ver Execuções
1. Vá para a aba **Actions** no GitHub
2. Clique no workflow "CI/CD Pipeline - Docker Build and Push"
3. Selecione uma execução para ver detalhes e logs

### Status Badge
Adicione ao README.md principal:

```markdown
![CI/CD Pipeline](https://github.com/seu-usuario/extrato-api/workflows/CI%2FCD%20Pipeline%20-%20Docker%20Build%20and%20Push/badge.svg)
```

## 🛠️ Modificar Workflows

### Adicionar Step ao Workflow
```yaml
- name: Meu novo step
  run: echo "Fazendo algo útil"
```

### Mudar Branches Monitorados
```yaml
on:
  push:
    branches:
      - main
      - minha-branch-especial  # adicione aqui
```

### Desabilitar Jobs
```yaml
jobs:
  security-scan:
    if: false  # desabilita o job
    # ... resto da configuração
```

## 📚 Documentação Completa

Para mais informações, consulte:
- [GITHUB-ACTIONS-SETUP.md](../GITHUB-ACTIONS-SETUP.md) - Guia completo de configuração
- [QUICK-START-DOCKER-HUB.md](../QUICK-START-DOCKER-HUB.md) - Comandos úteis
- [environment-variables.md](../environment-variables.md) - Variáveis de ambiente

## ⚙️ Configuração Avançada

### Multi-Registry (Docker Hub + GHCR)
Para publicar em múltiplos registries, adicione ao workflow:

```yaml
- name: Login to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}

- name: Extract metadata
  uses: docker/metadata-action@v5
  with:
    images: |
      ${{ secrets.DOCKERHUB_USERNAME }}/extrato-api
      ghcr.io/${{ github.repository }}
```

### Notificações
Adicione notificações ao final do workflow:

```yaml
- name: Notify on Slack
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## 🔍 Troubleshooting

### Workflow não executa
- Verifique se o arquivo está em `.github/workflows/`
- Verifique a sintaxe YAML
- Veja a aba Actions → Workflows

### Build falha
- Veja os logs na execução específica
- Verifique se os secrets estão configurados
- Teste o Dockerfile localmente

### Push falha
- Verifique se o token Docker Hub é válido
- Verifique se o repositório existe no Docker Hub
- Verifique as permissões do token

## 🎯 Best Practices

✅ **Sempre execute testes antes do build**
✅ **Use cache para builds mais rápidos**
✅ **Faça scan de segurança nas imagens**
✅ **Use tags semânticas para releases**
✅ **Mantenha secrets seguros**
✅ **Monitore execuções regularmente**

## 📝 Changelog

### v1.0.0 (2024-10)
- ✨ Pipeline CI/CD completo com testes automatizados
- ✨ Build multi-arquitetura (amd64/arm64)
- ✨ Scan de segurança com Trivy
- ✨ Relatórios de cobertura em Pull Requests
- ✨ Cache otimizado para builds rápidos
- ✨ Publicação automática no Docker Hub

---

**Mantido por:** [@seu-usuario](https://github.com/seu-usuario)
**Última atualização:** Outubro 2024

