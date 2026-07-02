# Handoff para Claude Code — App de Countdown de Lançamentos de Jogos

## Sobre este documento

Este é um documento de contexto complementar à especificação completa (`game-countdown-app-spec.md`), que também está sendo entregue junto. A spec cobre features, requisitos e arquitetura em detalhe. Este handoff cobre o que a spec não cobre: em que fase o projeto está, como eu (Igor) gosto de trabalhar, e o que evitar construir por enquanto.

## Quem sou e o que é este projeto

Sou repórter de tecnologia (TecMundo e Voxel) e estudante de sistemas de informação, desenvolvendo este app sozinho. Tem caráter pedagógico, mas mira lançamento público de verdade. A ideia: um app de countdown de lançamentos de jogos com datas **confirmadas** (sem rumor), lista pessoal de jogos acompanhados, e widgets de tela inicial.

## Onde estamos agora

Concluímos a **Fase 1** (construção conceitual — requisitos, regras de negócio, arquitetura, stack, estimativas grosseiras de custo). Ainda não escrevemos nenhuma linha de código de produto. O próximo passo é a **Fase 2 — Protótipo**: app com dados mockados (mas respeitando o contrato de API já definido na Fase 1), UI descartável, prioridade em validar a ideia central (fluxo, telas, conceito) — não o widget. Testes unitários já ativos desde esta fase.

Ver a seção "Fases de desenvolvimento" da spec pro roadmap completo — inclusive a bifurcação opcional de um spike de widget, que só deve ser acionada se a pesquisa sobre restrições do Glance (tarefa leve da Fase 1) indicar risco real de incompatibilidade de dados.

## Como eu gosto de trabalhar (importante)

Não leio Kotlin nem Python com fluência — consigo acompanhar Java superficialmente. Por isso, o processo importa tanto quanto o código em si:

- **Segmente por camada, não por feature inteira.** Prefiro pedidos pequenos (ex.: "crie só o modelo", depois "crie só o repositório", depois "crie só a lógica", depois "crie só a UI") a um pedido grande que mexe em tudo de uma vez. Isso me permite revisar de verdade, mesmo sem ler o código fluentemente.
- **Explique o que foi feito e por quê, em português**, a cada iteração — não só entregue o código. Isso substitui minha leitura direta como forma de acompanhar decisões.
- **Teste antes ou junto da lógica, não depois.** Testes unitários são minha rede de segurança primária. Se uma mudança de lógica não vem acompanhada de teste, trate isso como incompleto, não como "posso adicionar depois".
- **Quando houver trade-off real, apresente as opções — não decida sozinho e siga em frente.** Prefiro escolher entre abordagens, mesmo que isso deixe o processo mais lento, a ter uma decisão relevante tomada sem eu perceber. Já tive uma boa experiência trabalhando assim num projeto anterior (Anotai) e quero repetir esse padrão aqui.
- **Não libere uma feature inteira de uma vez**, mesmo que eu peça de forma ampla. Se meu pedido for amplo demais, quebre em pedaços e confirme comigo antes de seguir adiante.

## Princípios de arquitetura (não negociáveis, valem desde a primeira linha)

- SOLID desde o início, não retrofitado depois.
- Repository Pattern em toda fonte de dado externa — interface + implementação separadas.
- Camada de Services separada da lógica de estado/ViewModel.
- Arquitetura baseada em componentes no front-end (Compose), organizada por tela/feature + pasta `comum/` para o que é cross-cutting — mesmo padrão usado no Anotai. Atomic Design foi considerado e descartado por ora.
- Diagrama de classes/componentes vivo (Mermaid) e documentação incremental por passo — no espírito do `docs/diagrama.md` do Anotai (histórico de mudanças registrado por "passo"). Vale manter esse hábito aqui também.

## Decisões-chave (resumo — ver spec para o detalhe completo)

- **App**: Kotlin + Jetpack Compose, Android-first, sem iOS por ora. Widgets via Jetpack Glance (biblioteca separada, não incluída no Compose).
- **Backend**: Python + FastAPI + SQLAlchemy, API estilo REST. Interfaces via `abc.ABC`. Injeção de dependência via `Depends()`. Domínio fundido ao ORM (só o schema Pydantic fica separado como contrato de API).
- **Painel de curadoria**: SQLAdmin (Python nativo, sem stack nova, CRUD gerado direto dos modelos).
- **Fonte de catálogo**: RAWG (principal), Steam API (secundária — preço de PC e fallback), IGDB (candidata, mas travada em produção até checkpoint comercial da Fase 6).
- **Detecção de "anúncio grande"**: três caminhos — observador de mudança estruturada (datas/pré-venda), scraper + palavra-chave (trailer oficial/gameplay + notícia editorial, priorizando Voxel), publicação manual de urgência via painel.

## Fora de escopo agora — não construir ainda

- iOS (descartado por enquanto).
- LGPD e custo de infraestrutura em detalhe (reservados para a Fase 6, lançamento público).
- Ativação do IGDB em produção (aguardando checkpoint comercial da Fase 6).
- Automação de scraping/RSS e fila de revisão do painel de curadoria (reservadas para a Fase 4).
- Sync entre dispositivos (fase avançada — a arquitetura só precisa estar preparada, não implementada).
- Testes e2e amplos (reservados para a Fase 5 — por enquanto, só unitários e integração de backend).

## Uma sugestão

Talvez valha adaptar este documento (ou parte dele) num `CLAUDE.md` no repositório, como já foi feito no Anotai — mantém essas instruções de processo sempre visíveis para qualquer sessão futura, sem precisar reapresentar isso toda vez.
