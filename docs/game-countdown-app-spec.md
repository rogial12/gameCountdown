# App: Countdown de Lançamentos de Jogos
*Documento vivo de requisitos — atualizado conforme decisões do dono do projeto (Igor Almenara).*

## Conceito

Aplicativo mobile-first para Android, para acompanhamento de lançamentos de jogos com datas **confirmadas**, sem rumores. O usuário monta uma lista pessoal ("Jogos que estou de olho") e acompanha informações essenciais (data, plataformas, preço) dentro do app e via widgets na tela inicial.

## Plataformas e diretrizes visuais

- **Android**, sem suporte a iOS por ora — descartado devido a políticas mais rigorosas de link afiliado na plataforma. Possível revisão futura, não no horizonte atual.
- Linguagem visual **Material 3 Expressive**, com identidade própria.
- Público gamer — interface direta, informativa, sem clichês.
- UI majoritariamente baseada em listas, simples e amigável.

## Escopo de conteúdo (decidido)

- Catálogo inicial: **grandes lançamentos ocidentais**. Indies e jogos japoneses previstos para fases posteriores, se o projeto crescer.
- Notícias: **fontes abertas via RSS/scraping, com priorização para o Voxel**.
- Linkagem de publicações: sem risco autoral — o app encaminha cliques à fonte, não reproduz conteúdo.
- Imagens/capas: somente **material de divulgação** (uso justo).
- Preço exibido em **USD**, e em **BRL quando disponível**. Arquitetura preparada para múltiplas moedas.
- App **totalmente gratuito**, potencialmente sustentado por **links de afiliado**.

## Funcionalidades funcionais

### Catálogo, busca e navegação
- Busca e **filtros**: plataforma, gênero, lançamento iminente (semana, mês, trimestre, semestre, ano).
- **Ordenação**: mais aguardados, mais próximos do lançamento, alfabética.
- **Onboarding leve** (ex.: plataformas que o usuário possui, para personalizar catálogo e widget).
- **Visão de calendário** como exibição alternativa — ver seção "Visão de Calendário" abaixo.

### Visão de Calendário
Exibição alternativa (não é uma aba própria) disponível **dentro** das telas de Catálogo e Lista Pessoal, acessível por um botão no canto superior direito da tela. Alterna entre a visão em lista e a visão em grade de calendário.

- **Instâncias independentes por tela.** O Calendário do Catálogo mostra os mesmos jogos exibidos ali, respeitando os filtros de gênero e plataforma ativos. O Calendário da Lista Pessoal mostra apenas os jogos da lista pessoal. As duas instâncias são separadas.
- **Grade mensal.** Cada dia com lançamento previsto exibe um círculo com uma versão reduzida da **capa** do jogo daquele dia. Havendo mais de um lançamento no mesmo dia, mostra-se o de **maior interesse público** (o `anticipationScore`); um selo "+N" indica que há outros lançamentos naquele dia.
- **Dia atual destacado** visualmente (apenas quando o mês exibido é o corrente).
- **Semana começa no domingo** (convenção brasileira).
- **Tocar num dia com lançamento** abre um painel inferior (bottom sheet) com a lista dos jogos daquele dia. Dias sem lançamento não são interativos.
- **Navegação entre meses** por setas (◀ retroceder, ▶ avançar).

### Lista pessoal ("Jogos que estou de olho")
- Jogos em destaque com capa, preço, data e plataformas.
- Adição/remoção rápida via **switch**.
- **Exportar/importar** lista (backup manual). Sync entre dispositivos previsto para fase avançada — arquitetura deve prever isso.

### Página de detalhes
- Sinopse, imagens promocionais, **trailers incorporados**.
- Dados: data de lançamento, desenvolvedor, data de pré-venda (se houver).
- Seção **"Onde comprar?"** com links para lojas (afiliados como possível receita).
- Sub-aba de **notícias relacionadas**.

### Estados visuais
- **Modo LANÇAMENTO IMINENTE**: estado visual diferenciado em card/widget para jogos que saem na semana, no dia seguinte ou no dia.

### Widgets (múltiplos simultâneos)
- **Jogo único**: capa e mídias promocionais em destaque, data de lançamento, countdown dinâmico.
- **Lista**: subconjunto da lista pessoal; quais jogos aparecem é configurável.
- Redimensionáveis (mais tamanho = mais informação).
- Mudam de estado no modo LANÇAMENTO IMINENTE.

### Notificações
- Inicialmente, apenas sobre jogos da lista pessoal.
- **Resumos periódicos**: lançamentos do mês (início do mês) e da semana (início da semana).
- Forte segmentação/controle pelo usuário, em dois eixos:
  - **Global, por categoria** (Configurações): resumos periódicos (semanal + mensal, toggle único); trailer/gameplay (trailer oficial e trailer de gameplay, uma categoria única); datas confirmadas (lançamento + pré-venda, uma categoria, já que raramente saem em momentos diferentes); preço confirmado; lançamento iminente (categoria única, dispara nos três gatilhos já definidos pro estado visual: 7 dias antes, véspera, dia do lançamento).
  - **Por jogo**: notificações **ativadas por padrão** para todo jogo adicionado à lista pessoal, com opção de **opt-out individual** (silenciar aquele jogo específico) — camada adicional sobre o eixo de categoria, sem replicar a matriz completa por jogo.
- **"Anúncio grande"** (trailer oficial, trailer de gameplay, confirmação de data, pré-venda, preço), com três caminhos complementares de detecção:
  - **Caminho A — Observador de mudança estruturada** (principal para data de lançamento e pré-venda): job agendado (APScheduler) reconsulta periodicamente RAWG/IGDB/Steam por jogo do catálogo e compara com o valor salvo; mudança em campo crítico gera candidato de anúncio grande. Mais confiável que palavra-chave por comparar dado estruturado, não texto — mas ainda passa por revisão humana no painel (bases são movidas por comunidade, campo pode mudar por correção, não por anúncio real).
  - **Caminho B — Scraper/RSS + palavra-chave** (escopo reduzido: cobre trailer oficial e trailer de gameplay, além da cobertura editorial geral, priorizando Voxel): pipeline de 4 passos já definido (associar notícia a jogo → classificar por título → padrão estruturado + verbo de confirmação/exclusão de especulação → fila de revisão humana).
  - **Caminho C — Publicação manual de urgência**: ação no painel de curadoria para o curador publicar/disparar notificação imediatamente, sem esperar detecção automática — cobre casos de alta criticidade e baixa tolerância a atraso de propagação das fontes (ex.: adiamento de lançamento anunciado poucos dias antes da data). Reaproveita o mesmo mecanismo de disparo de notificação dos outros dois caminhos.

## Requisitos não funcionais

- **Persistência offline**: lista pessoal sempre visível offline; dados atualizam com internet e permanecem em cache sem conexão.
- **Cache de imagens e armazenamento offline** com política de tamanho/expiração.
- **Background sync** para captação de novidades. **Confiabilidade em fabricantes Android agressivos (Xiaomi, Samsung, Asus)**: risco real, mas segmentado em duas partes com exposição diferente — (a) countdown visual do widget: baixo risco, recalculado no ciclo nativo de `update()` do próprio sistema, não depende de job próprio; (b) sincronização de dado novo + notificação: risco real, mitigado por FCM de alta prioridade (mais resistente a Doze mode) + fallback garantido de sync sempre que o app abre em primeiro plano + orientação ao usuário no onboarding para excluir o app da otimização de bateria em fabricantes conhecidos. Esforço proporcional ao risco: pior cenário é atraso cosmético, não perda crítica — não justifica mecanismos pesados (wake-lock constante, foreground service permanente).
- **Telemetria/crash reporting básico** (analytics anônimo).
- **Arquitetura preparada (mas não dependente)** de infra de backend no MVP/protótipos.
- **Testes**: unitários como rede de segurança primária, contínuos desde o protótipo (Fase 2). **Integração no backend**: começa já na Fase 3 (junto ao backend v0) — barato graças à injeção de dependência via `Depends()`, testando rota completa com banco de teste; sem camada e2e separada pro backend (rota + banco de teste já cobre "ponta a ponta"). **Integração no app**: testes de UI do Compose por componente (`ComposeTestRule`) + testes próprios do Glance para o widget (`runGlanceAppWidgetUnitTest`), usados já no spike opcional de widget se acionado. **E2E**: reservado a poucos fluxos críticos (ex.: adicionar à lista → persistência offline), concentrado na Fase 5 (Qualidade) — caro de montar e manter, não compensa adiantar. **Complemento recomendado**: CI (ex.: GitHub Actions) rodando os testes a cada mudança, reforço de segurança adicional dado que o dono do projeto não lê Kotlin/Python fluentemente.
- **LGPD**: a tratar apenas em eventual lançamento público (conta, sync, push, identificadores).
- **Custo de infraestrutura**: avaliar em detalhe apenas para lançamento público.

## Princípios de arquitetura e engenharia (obrigatórios desde a Fase 1)

- **Repository Pattern** em toda fonte de dado externa (catálogo, notícias, preços) — interface + implementação separadas.
- **Camada de Services** separada da lógica de estado (ViewModel), evitando que a ViewModel concentre responsabilidades demais.
- **SOLID** aplicado desde a primeira iteração, não retrofitado depois.
- **Diagrama de classes/componentes vivo** (Mermaid, versionado no repositório), atualizado a cada mudança arquitetural relevante.
- **Documentação incremental por passo**: cada mudança estrutural relevante é registrada (o quê e por quê), em português — funciona como rede de segurança para revisão sem leitura fluente de código e como material pedagógico.
- **Testes unitários como rede de segurança primária**, contínuos desde o protótipo (Fase 2) — não uma fase isolada no fim. Demais tipos de teste (integração, e2e) a definir mais adiante, sem familiaridade prévia do dono do projeto.
- **Segmentação por camada** nas iterações com Claude Code (modelo → repositório → lógica → UI), não por feature inteira de uma vez, para manter revisão viável mesmo sem leitura fluente do código gerado.
- **Arquitetura baseada em componentes no front-end**: telas compostas por componentes de UI pequenos, reutilizáveis e testáveis isoladamente (ex.: `GameCard`, `CountdownBadge`, `IminenteBadge`, `PlatformBadge`, `PriceTag`, `FilterBar`, `AddToListSwitch`). **Organização decidida**: por tela/feature (ex.: `ui/catalogo/`, `ui/lista_pessoal/`, `ui/detalhes/`) + uma pasta compartilhada (`ui/comum/`) para componentes genuinamente cross-cutting — mesmo padrão já validado no Anotai. Complementa (não substitui) as camadas MVVM/Repository/Service — rege a estrutura interna da camada de View, reduzindo risco de regressão e viabilizando iterações segmentadas por componente com o Claude Code.
  - **Nota para o futuro**: Atomic Design (átomos/moléculas/organismos/templates/páginas) foi considerado e descartado por ora — estrutura desproporcional ao tamanho atual do projeto. Fica registrado como possível refatoração futura, a reconsiderar se: (a) o painel de curadoria web precisar compartilhar linguagem visual formal com o app, ou (b) a pasta `ui/comum/` crescer a ponto de ficar difícil de navegar.

## Fases de desenvolvimento

1. **[atual]** Construção conceitual, regras de negócio, requisitos. Arquitetura de **app + backend + painel de curadoria** desenhada em conjunto — contrato de API e modelo de dados definidos já aqui, mesmo sem implementação do backend. Diagrama de classes/componentes inicial. Estimativas grosseiras de custo. Stack definida. **Tarefa leve adicional**: pesquisa das restrições reais do Glance (tipos de dado aceitos no estado, limites de tamanho, mecanismo de refresh), usada para desenhar o modelo de dados/Repository já compatível — sem implementar widget ainda.
2. **Protótipo**: app com dados mockados, mas o mock respeita o contrato de API já definido na Fase 1 (troca futura pelo backend real fica barata via Repository Pattern). UI descartável. Testes unitários já ativos. **Prioridade**: validar a ideia central do app (fluxo, telas, conceito) — não o widget.
   - **Bifurcação opcional (a critério do dono do projeto, não obrigatória)**: ao final desta fase ou no início da Fase 3, pode-se inserir um *spike* mínimo de widget (Glance) — versão só com capa e countdown, sem redimensionamento ou modo iminente — para validar cedo que o pipeline Repository → widget funciona de ponta a ponta. Vale acionar essa bifurcação se, durante a Fase 1/2, a pesquisa sobre Glance indicar incerteza real sobre compatibilidade de dados. Se não houver sinal de risco, segue-se direto para a Fase 3 sem o spike.
3. **Backend v0 + MVP**: backend real implementado pela primeira vez (curadoria manual, com CRUD básico via SQLAdmin — custo próximo de zero, substitui edição direta de JSON/banco). App consome dados reais, recursos básicos.
4. **MVP 2.0**: recursos adicionais, widgets (múltiplos, redimensionável, modo iminente) — amadurecendo o que foi validado no spike opcional, se ele tiver sido feito, ou implementados pela primeira vez, caso contrário. Backend evolui (scraping/RSS automatizado). Painel de curadoria ganha a fila de revisão e ações customizadas (aprovar/rejeitar candidato de anúncio grande), quando a ingestão automatizada passa a existir.
5. **Qualidade**: testes de integração/e2e, hardening, revisão geral.
6. **Versão 1.0**: lançamento público — LGPD, custo de infraestrutura em detalhe, políticas de link afiliado.

## Stack técnica

- **App**: Kotlin + Jetpack Compose (nativo, Android-first, sem iOS por ora), para as telas do app.
- **Widgets**: **Jetpack Glance** — biblioteca separada (não incluída no Compose), obrigatória para renderizar UI na tela inicial. API inspirada em Compose mas com superfície restrita (`GlanceModifier`, componentes limitados, sem desenho customizado livre). Estável desde a versão 1.1.0 (2024). Compartilha tema Material 3 com o app via configuração explícita.
- **Backend**: Python + FastAPI, API estilo REST. Contrato de API desenhado na Fase 1.
  - **Camadas**: `api/` (rotas, só HTTP) → `services/` (regra de negócio) → `repositories/` (interfaces + implementações, ex.: `GameRepository`, `ImageStorageRepository`) → `models/` (SQLAlchemy, domínio fundido ao ORM — só o schema Pydantic fica separado como contrato de API). Módulo `ingestion/` isolado para scraping/RSS/detecção de "anúncio grande" por palavra-chave, trocável sem propagar mudança pro resto do sistema.
  - **Interfaces**: `abc.ABC` (não `Protocol`) — falha explícita se uma implementação esquecer um método, mais previsível para revisão sem leitura fluente de código.
  - **Injeção de dependência**: via `Depends()` nativo do FastAPI — permite trocar repositório real por fake em teste sem alterar rotas.
  - **Rotas separadas desde já**: públicas (`games.py`, `news.py`, consumidas pelo app, só leitura) vs. curadoria (`curation.py`, autenticação por API key simples, consumida só pelo painel — mesmo que o painel só chegue na Fase 4).
  - **Scraping em background (v0)**: scheduler simples (APScheduler ou cron do provedor de hospedagem). Celery+Redis adiado até volume justificar.
  - **Trailers**: embed do YouTube (guarda só o ID do vídeo, sem persistir o vídeo).
  - **Imagens**: Fase 3 (backend v0) guarda apenas a **URL** da imagem (da fonte de curadoria) — sem armazenamento próprio ainda. Podem vir de fontes diferentes por jogo; cada imagem guarda também um campo `fonte` (proveniência), útil para rastrear dependência caso os termos de uso de uma fonte mudem. Object storage (Cloudflare R2/Backblaze B2/S3) só entra se URLs de terceiros se mostrarem instáveis.
  - **Fonte de catálogo**: RAWG — base agnóstica de plataforma, inclui links de "onde comprar" prontos. Termos comerciais favoráveis a projeto solo com monetização via afiliado: grátis para uso comercial até 100 mil usuários ativos/mês ou 500 mil pageviews/mês, com atribuição obrigatória (reavaliar se o projeto crescer além disso). Trailers seguem via embed do YouTube, independente da API de catálogo.
  - **Fonte secundária — Steam API**: ativa desde já para preço de títulos de PC (lacuna que RAWG/IGDB não cobrem) e como fallback de catálogo para títulos disponíveis na Steam. Uso comercial permitido pelos termos da Valve, com restrição de atribuição (não apresentar o app como endossado/afiliado à Valve/Steam). Também alimenta o observador de mudança estruturada (Caminho A da detecção de anúncio grande).
  - **Fonte candidata — IGDB**: interface de repositório já preparada para suportar, mas **não ativado em produção** até resolver o checkpoint comercial da Fase 6 (uso como fallback ainda conta como uso comercial, mesmo não sendo fonte principal).
  - **Resiliência de fonte**: como o app consome só o backend próprio (nunca fala direto com RAWG/IGDB/Steam), indisponibilidade de uma fonte afeta apenas a ingestão/curadoria durante a janela de instabilidade — o catálogo já curado no banco próprio continua servindo o app normalmente. Por isso, troca de fonte principal é **manual/configurável**, não failover automático em tempo real — complexidade não justificada pelo impacto real.
  - **Infra v0**: hospedagem com camada gratuita (Railway/Render/Fly.io) + Postgres (dado relacional).
  - **Notificações push**: Firebase Cloud Messaging (FCM); backend decide quando disparar, FCM entrega.
- **Painel de curadoria**: **SQLAdmin** — biblioteca Python nativa para FastAPI/Starlette + SQLAlchemy, gera CRUD administrativo direto dos modelos já existentes, sem linguagem nova. Suporta views/ações customizadas (necessário para a fila de revisão de "anúncio grande"). Sem dependência de SaaS externo. Ressalva: pacote em status Beta (ativo, mas ainda não 1.0) — alternativa de reserva: starlette-admin (ORM-agnóstico), se necessário no futuro. **Ajuste de roadmap**: CRUD básico pode nascer já na Fase 3 (custo próximo de zero), substituindo edição manual direta de JSON/banco; a fila de revisão com lógica customizada (aprovar/rejeitar candidato de anúncio grande) permanece reservada para a Fase 4, quando a ingestão automatizada existir.
