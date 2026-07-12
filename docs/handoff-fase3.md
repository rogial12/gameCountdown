# Handoff — Início da Fase 3 (Backend) — para uma nova sessão do Claude Code

> **Para o Claude que abrir esta sessão:** este documento te coloca a par do projeto sem depender do histórico das sessões anteriores (que rodaram no Android Studio, sobre o app Kotlin). Leia também, nesta ordem: `CLAUDE.md` (regras de processo e arquitetura — valem integralmente aqui), `docs/game-countdown-app-spec.md` (spec completa, seção de backend nas linhas ~108-124) e `docs/diagrama.md` (diagrama vivo + histórico por passo). **Antes de escrever qualquer código, veja a seção "Decisões a resolver com Igor ANTES de começar" no fim deste doc — há escolhas de arquitetura que são de Igor, não suas.**

---

## 1. Onde o projeto está (snapshot em 2026-07-12)

**GameCountdown** — app Android de countdown de lançamentos de jogos com datas **confirmadas** (sem rumores). Dono: **Igor Almenara**, repórter de tecnologia e estudante de sistemas de informação. **Igor não lê Kotlin nem Python com fluência** — o *como* (explicação, segmentação, testes) importa tanto quanto o código.

- **Fase 1 (Conceitual):** concluída — requisitos, regras de negócio, arquitetura, stack, contrato de API, estimativas de custo.
- **Fase 2 (Protótipo):** **ENCERRADA.** App Android completo com dados **mockados** respeitando o contrato de API da Fase 1: telas de Catálogo, Lista Pessoal, Detalhes, Busca e Calendário, navegação, SOLID + Repository + Services + componentes de UI. Testes unitários em toda a lógica **e** testes instrumentados de feature das quatro telas (rodados no aparelho físico SM-S908E). Validado manualmente por Igor: fluxo redondo. Decisão registrada de **pular o spike de widget Glance** (a pesquisa não indicou risco bloqueante — ver `docs/pesquisa-glance-spike.md`).
- **Fase 3 (esta):** **backend real.** É o que esta sessão vai começar.

O código do app vive em `app/` (módulo Android). **O backend Python será um projeto novo** — ver decisão de layout no fim.

---

## 2. O que muda de ambiente (por que VS Code agora)

As sessões anteriores rodaram no **Android Studio**, porque o trabalho era Kotlin/Compose. A Fase 3 é **Python + FastAPI**, então Igor migrou para o **VS Code**, mais adequado ao backend. Consequências práticas:

- O foco sai de `app/` (Kotlin) e vai para o backend Python (pasta/repo a definir).
- Ferramentas de build/teste mudam: em vez de `gradlew`, será `python`/`pip`/`pytest` (gerenciador exato a decidir — ver seção final).
- O app Kotlin **não é mais tocado nesta fase** — ele é o *cliente* que valida se o contrato do backend está correto. Quando o backend existir, a troca no app é barata (só a implementação de `GameRepository`, graças ao Repository Pattern).

---

## 3. O contrato de API que o backend TEM de respeitar (o ponto mais importante)

O app mock **já consome um contrato definido**. O backend da Fase 3 precisa produzir **exatamente essa forma** — é o critério de sucesso da fase. A fonte de verdade é o modelo `app/src/main/java/com/almenara/gamecountdown/data/model/Game.kt` e a interface `GameRepository.kt`. Traduzido para o schema Pydantic que o backend deve expor:

### Recurso `Game` (o schema público, JSON)

| Campo | Tipo | Observações |
|---|---|---|
| `id` | string | identificador único (ex.: `"gta6"`) |
| `title` | string | título |
| `releaseDate` | string ISO `"YYYY-MM-DD"` | data de lançamento confirmada |
| `platforms` | lista de enum | ver `Platform.kt` (valores fixos) |
| `genres` | lista de enum | ver `Genre.kt` (valores fixos) |
| `developer` | string | estúdio |
| `synopsis` | string | descrição para a tela de Detalhes |
| `coverUrl` | string | **só a URL** da capa (sem storage próprio na Fase 3) |
| `priceUsd` | number \| null | null = preço não anunciado |
| `priceBrl` | number \| null | idem |
| `trailerId` | string \| null | ID do vídeo no YouTube (só o ID) |
| `screenshotUrls` | lista de string | capturas para o carrossel de mídia; pode ser vazia |
| `preSaleDate` | string ISO \| null | null = sem pré-venda |
| `anticipationScore` | int | pontuação de antecipação (ordenação "mais aguardados") |

> **`isWatched` é estado do usuário, NÃO do catálogo.** No app ele existe no modelo, mas é resolvido localmente (lista pessoal). Na Fase 3, sem sync entre dispositivos (fora de escopo), a lista pessoal continua **local no app** — o backend **não** precisa persistir `isWatched` por usuário ainda. A arquitetura deve *prever* sync no futuro, mas não implementar. Confirmar com Igor se o schema público deve omitir `isWatched` (recomendado) ou mantê-lo sempre `false`.

### Operações que o app espera (viram endpoints públicos, só leitura)

Derivadas de `GameRepository` (Kotlin):

- `getGames()` → `GET /games` (catálogo; filtros/ordenação são regra de Service — ver `GameService.kt`: filtro por plataforma/gênero/período de lançamento, ordenação por mais aguardados/mais próximos/alfabética)
- `getGameById(id)` → `GET /games/{id}` (retorna 404 quando não existe — no app o mock devolve `null`)
- `searchGames(query)` → `GET /games?search=...` (busca por título, case-insensitive)

O `getDaysUntilRelease` (countdown) é **cálculo derivado da `releaseDate`** — pode ficar no cliente ou virar campo calculado; decisão de Igor. Notícias (`news.py`) são recurso separado previsto na spec, mas a ingestão automática é **Fase 4** — na Fase 3 news pode nascer só como CRUD manual, se Igor quiser.

**Regra de ouro:** ao final da Fase 3, apontar o app Kotlin para o backend real (trocando só a implementação de `GameRepository`) deve funcionar **sem mudar mais nada no app**. Se algo no schema não bate com `Game.kt`, o contrato está errado — corrija o backend, não o app.

---

## 4. Arquitetura do backend (não negociável, vem da Fase 1)

Da spec (linhas ~110-124) e do `CLAUDE.md`:

- **Camadas:** `api/` (rotas, só HTTP) → `services/` (regra de negócio) → `repositories/` (interface + implementação separadas, ex.: `GameRepository`, `ImageStorageRepository`) → `models/` (SQLAlchemy — **domínio fundido ao ORM**; só o **schema Pydantic** fica separado como contrato de API). Módulo `ingestion/` isolado (scraping/RSS/detecção de "anúncio grande") — **trocável**, mas só ativado na Fase 4.
- **Interfaces via `abc.ABC`** (não `Protocol`) — falha explícita se uma implementação esquecer um método (mais previsível para revisão sem leitura fluente).
- **Injeção de dependência via `Depends()`** nativo do FastAPI — permite trocar repositório real por fake em teste sem mexer nas rotas.
- **Rotas separadas desde já:** públicas (`games.py`, `news.py` — consumidas pelo app, só leitura) vs. **curadoria** (`curation.py` — autenticação por API key simples, consumida só pelo painel).
- **Painel de curadoria (SQLAdmin):** CRUD básico **pode nascer já na Fase 3** (custo quase zero, substitui edição manual de JSON/banco). A fila de revisão com lógica customizada fica para a Fase 4.
- **Fontes:** RAWG (principal) e Steam (preço de PC + fallback) ativas; **IGDB só preparado na interface, NÃO ativado** (checkpoint comercial na Fase 6).
- **Imagens:** guardar **só a URL** + um campo `fonte` (proveniência). Sem object storage próprio ainda.
- **Infra v0:** hospedagem free-tier (Railway/Render/Fly.io) + **Postgres**.

---

## 5. Fora de escopo na Fase 3 (não construir)

- Automação de scraping/RSS e a fila de revisão de "anúncio grande" no painel → **Fase 4**.
- IGDB ativo em produção → **Fase 6**.
- Sync de lista pessoal entre dispositivos (só prever na arquitetura).
- Object storage próprio de imagens (só se URLs de terceiros se mostrarem instáveis).
- LGPD e custo de infra em detalhe → **Fase 6**.
- Testes e2e amplos → **Fase 5**. Na Fase 3: **unitários + testes de integração de backend**.
- Widgets Glance (spike foi dispensado; widgets de produto são fase posterior).

---

## 6. Como trabalhar com Igor (obrigatório — igual em todas as fases)

Está tudo em `CLAUDE.md`, mas o essencial:

- **Segmente por camada, não por feature.** Aqui isso significa, por exemplo: primeiro só o schema Pydantic (o contrato) → depois só o model SQLAlchemy → depois só a interface de repositório → depois só a implementação → depois só o service → depois só a rota. **Confirme antes de avançar de camada.** Não entregue o backend inteiro de uma vez.
- **Explique o que foi feito e por quê, em português**, a cada iteração. Igor acompanha pela explicação, não pela leitura fluente do Python.
- **Testes antes ou junto da lógica, nunca depois.** Mudança de lógica sem teste = incompleta. Use `pytest` + o fake de repositório injetado via `Depends()`.
- **Trade-off real → apresente as opções, não decida sozinho.** Igor quer escolher entre abordagens.
- **Documentação incremental por passo** no `docs/diagrama.md` (o mesmo "diário de passos" usado na Fase 2 — continue a numeração) e mantenha o diagrama Mermaid vivo a cada mudança estrutural.
- **Comentários linha a linha, em português**, em todo o código — a mesma regra do Kotlin vale para o Python (Igor não lê Python fluentemente).

---

## 7. Decisões a resolver com Igor ANTES de começar a codar

Estas são de Igor (trade-offs reais) — **pergunte, não presuma**:

1. ~~**Layout do repositório.**~~ **RESOLVIDO (2026-07-12): monorepo.** O backend vive no mesmo repo (`github.com/rogial12/gameCountdown`), na pasta `backend/` (já criada, com `README.md` de âncora). O `.gitignore` da raiz já ganhou a seção de artefatos Python. Não criar repo novo. As subpastas de arquitetura (`api/`, `services/`, etc.) ainda **não** existem — serão criadas camada a camada na Fase 3.
2. **Ferramenta de ambiente/deps Python:** `venv`+`pip`+`requirements.txt` (padrão, simples) vs. `uv` (moderno, rápido) vs. `poetry`. Recomendação: alinhar com o que Igor já tem instalado e conhece.
3. **Escopo do arranque:** começar pelo **contrato (schema Pydantic de `Game`)** — a peça que amarra o backend ao app já pronto — é o ponto de partida de menor risco e maior valor. Confirmar se Igor concorda em começar por aí.
4. **`isWatched` no schema público:** omitir (recomendado, já que a lista pessoal é local) ou manter sempre `false`?

### Ponto de partida sugerido (depois de resolver o acima), segmentado

1. Estrutura de pastas do backend + ambiente/deps + "hello world" do FastAPI rodando.
2. **Schema Pydantic `Game`** fiel ao `Game.kt` + teste que trava o contrato.
3. Enums `Platform`/`Genre` no backend, espelhando os `.kt`.
4. Model SQLAlchemy `Game` (domínio + ORM).
5. Interface `GameRepository` (`abc.ABC`) + implementação (começando com dados semente, sem RAWG ainda).
6. `GameService` (filtro/ordenação/busca — espelhar a lógica já testada no `GameService.kt` do app).
7. Rotas públicas `games.py` com `Depends()`.
8. Testes de integração ponta a ponta (rota → service → repo fake).

---

## 8. Estado do repositório agora

- Branch `main`, working tree limpo. **2 commits locais ainda não enviados ao `origin`** (os testes da Busca e a doc do spike de Glance desta última sessão) — Igor pode querer `git push` antes de começar a Fase 3.
- Arquivos-chave para o novo Claude: `CLAUDE.md`, `docs/game-countdown-app-spec.md`, `docs/diagrama.md`, `docs/handoff-claude-code.md` (handoff original, histórico), este `docs/handoff-fase3.md`, `docs/pesquisa-glance-spike.md`, e — como **contrato de referência** — `app/src/main/java/com/almenara/gamecountdown/data/model/*.kt` e `.../data/repository/GameRepository.kt` + `.../data/service/GameService.kt`.
