# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Projeto

**GameCountdown** — App Android de countdown de lançamentos de jogos com datas **confirmadas** (sem rumores). O usuário monta uma lista pessoal e acompanha lançamentos via app e widgets de tela inicial.

- Dono do projeto: Igor Almenara (repórter de tecnologia, estudante de sistemas de informação)
- Igor **não lê Kotlin nem Python com fluência** — o processo de explicação é tão importante quanto o código em si
- Spec completa: `docs/game-countdown-app-spec.md` | Handoff de processo: `docs/handoff-claude-code.md`

## Fase atual

**Fase 2 — Protótipo**: app com dados mockados (respeitando o contrato de API da Fase 1), UI descartável, validar fluxo e conceito. Testes unitários ativos desde esta fase.

A Fase 1 (conceitual) está concluída. Nenhuma linha de código de produto foi escrita ainda.

## Regras de código

- **Comentários em todo o código, em português, linha a linha.** Todo arquivo Kotlin deve ter comentários explicando o que cada linha faz — inclusive `package`, `import`, campos de data class, métodos de interface e lógica interna. Essa regra existe porque Igor não lê Kotlin com fluência e usa os comentários como principal forma de acompanhar o código.

## Como trabalhar com Igor (obrigatório ler)

- **Segmente por camada, não por feature inteira.** Crie só o modelo → depois só o repositório → depois só a lógica → depois só a UI. Não entregue uma feature inteira de uma vez, mesmo que o pedido seja amplo — quebre e confirme antes de seguir.
- **Explique o que foi feito e por quê, em português**, a cada iteração.
- **Testes unitários antes ou junto da lógica**, nunca depois. Mudança de lógica sem teste = incompleto.
- **Quando houver trade-off real, apresente as opções** — não decida sozinho. Igor quer escolher entre abordagens.
- Se o pedido for amplo demais, quebre em pedaços e confirme antes de avançar.

## Princípios de arquitetura (não negociáveis desde a primeira linha)

- **SOLID** desde o início, não retrofitado depois.
- **Repository Pattern** em toda fonte de dado externa — interface + implementação separadas.
- **Camada de Services** separada da lógica de estado (ViewModel).
- **UI baseada em componentes** (ex.: `GameCard`, `CountdownBadge`, `PlatformBadge`), organizada por tela/feature (`ui/catalogo/`, `ui/lista_pessoal/`, `ui/detalhes/`) + pasta `ui/comum/` para componentes cross-cutting.
- **Diagrama de classes/componentes vivo** (Mermaid, `docs/diagrama.md`), atualizado a cada mudança arquitetural relevante.
- **Documentação incremental por passo** — cada mudança estrutural relevante registrada (o quê e por quê), em português.

## Stack técnica

- **App**: Kotlin + Jetpack Compose + Material 3 Expressive, Android-first (sem iOS)
- **Widgets**: Jetpack Glance (biblioteca separada do Compose)
- **Backend** (Fase 3+): Python + FastAPI + SQLAlchemy, API REST
  - Interfaces via `abc.ABC`; injeção de dependência via `Depends()`
  - Domínio fundido ao ORM; só o schema Pydantic fica separado como contrato de API
  - Rotas separadas: públicas (`games.py`, `news.py`) vs. curadoria (`curation.py`)
- **Painel de curadoria**: SQLAdmin (Python nativo, sem stack nova)
- **Fonte de catálogo**: RAWG (principal), Steam API (preço de PC e fallback)
- **Notificações push**: Firebase Cloud Messaging (FCM)

## Build & Run

```bash
# Build debug
./gradlew assembleDebug          # Windows: gradlew.bat assembleDebug

# Testes unitários
./gradlew test

# Teste unitário específico
./gradlew :app:test --tests "com.almenara.gamecountdown.ExampleUnitTest"

# Testes instrumentados (requer device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

- Min SDK: 24 | Target/Compile SDK: 36 | AGP: 9.2.1 | Kotlin: 2.2.10 | Compose BOM: 2026.02.01
- Versões centralizadas em `gradle/libs.versions.toml` — adicionar dependências lá antes de referenciar em `app/build.gradle.kts`

## Fora de escopo agora — não construir

- iOS
- Ativação do IGDB em produção (aguarda checkpoint comercial na Fase 6)
- Automação de scraping/RSS e fila de revisão do painel (Fase 4)
- Sync entre dispositivos (a arquitetura deve prever, mas não implementar)
- Testes e2e amplos (Fase 5 — por enquanto só unitários e, no backend, integração)
- LGPD e custo de infra em detalhe (Fase 6)
