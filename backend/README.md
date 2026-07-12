# Backend — GameCountdown (Fase 3)

Backend real do GameCountdown: **Python + FastAPI + SQLAlchemy**, API estilo REST. Serve o catálogo de jogos ao app Android, respeitando o **contrato de API já definido na Fase 1** (o mesmo que o app mock da Fase 2 já consome).

## Por que esta pasta existe aqui (decisão de monorepo — 2026-07-12)

Este backend vive **no mesmo repositório** do app Android (`github.com/rogial12/gameCountdown`), como uma pasta ao lado de `app/`. Decisão de Igor pela abordagem **monorepo** (em vez de um repositório separado), por ser mais simples de manter coerente num projeto solo:

- um histórico Git só, um `git push` só;
- `CLAUDE.md`, spec e `docs/` compartilhados entre app e backend (sem duplicar regras de processo);
- o contrato app↔backend fica lado a lado (o `Game.kt` do app e o schema Pydantic deste backend no mesmo repo, fáceis de manter fiéis).

As ferramentas não se cruzam: o Gradle (Android) só enxerga o módulo `:app`; o Python só olha para dentro de `backend/`.

## Estado atual

**Ambiente de pé (Passo 36) — hello world do FastAPI rodando; ainda sem código de produto.** O bootstrap está feito: `uv` como gerenciador, Python 3.13 fixado, `fastapi[standard]` instalado e um endpoint de saúde (`GET /`) provado por HTTP. As camadas de arquitetura (schema, model, repositório, service, rotas) começam a partir do Passo 37, segmentadas uma a uma.

### Como rodar (em um terminal novo, com `uv` já no PATH)

```bash
uv run --directory backend fastapi dev main.py   # sobe o servidor de desenvolvimento (auto-reload) em http://127.0.0.1:8000
uv sync --directory backend                      # recria o ambiente virtual a partir do pyproject.toml/uv.lock
```

A documentação interativa da API fica em `http://127.0.0.1:8000/docs` (gerada automaticamente pelo FastAPI).

Contexto completo e o roteiro segmentado por camada:

- **Handoff da Fase 3:** [`../docs/handoff-fase3.md`](../docs/handoff-fase3.md) — contrato de API a honrar, arquitetura, escopo e o roteiro de arranque.
- **Spec completa:** [`../docs/game-countdown-app-spec.md`](../docs/game-countdown-app-spec.md) (seção de backend).
- **Diagrama vivo / histórico por passo:** [`../docs/diagrama.md`](../docs/diagrama.md).
- **Regras de processo e arquitetura:** [`../CLAUDE.md`](../CLAUDE.md) — valem integralmente aqui.

## Arquitetura prevista (a construir, camada a camada)

```
backend/
├── api/            rotas HTTP (públicas: games.py, news.py | curadoria: curation.py)
├── services/       regra de negócio (filtro, ordenação, busca, countdown)
├── repositories/   interfaces (abc.ABC) + implementações (ex.: GameRepository)
├── models/         SQLAlchemy (domínio fundido ao ORM) + schemas Pydantic (contrato de API)
└── ingestion/      scraping/RSS/detecção de "anúncio grande" — isolado, só ativado na Fase 4
```

> Estas subpastas **ainda não existem** — serão criadas uma a uma, com testes junto da lógica, na Fase 3. Este README é a âncora inicial da pasta.
