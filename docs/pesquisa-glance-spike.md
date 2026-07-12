# Pesquisa — Spike de widget Glance (confirmação para a Fase 2)

**Data:** 2026-07-12
**Contexto:** a spec (Fase 1) previa um *spike mínimo* de widget Glance — versão só com capa + countdown — para validar cedo o pipeline `Repository → widget`. A regra da spec é: **só aciona o spike se a pesquisa da Fase 1 tiver indicado risco real de incompatibilidade**. Igor confirmou que aquela pesquisa foi feita e **não** apontou problemas. Esta é uma pesquisa de *confirmação*, revisitando o estado atual do Glance (julho/2026) antes de decidir construir ou não o spike.

**Conclusão em uma linha:** nenhum risco bloqueante confirmado. O countdown (texto) é trivial. A única nuance real é o carregamento da **capa** (imagem remota) dentro de um widget — um problema *conhecido e com solução padrão*, não um impedimento. Isso é consistente com o que a pesquisa da Fase 1 já havia concluído.

---

## 1. Versão e compatibilidade com o toolchain do projeto

| Item | Projeto (hoje) | Glance exige | Situação |
|---|---|---|---|
| AGP | 9.2.1 | 9.2.0+ (Glance 1.3.0-alpha01+) | ✅ compatível |
| minSdk | 24 | 23 (desde Glance 1.2.0) | ✅ compatível |
| Kotlin / Compose | Kotlin 2.2.10, Compose BOM 2026.02.01 | acompanha o Compose | ✅ mesma família |

- **Glance estável:** `1.1.1` (out/2024). **Pré-lançamento mais novo:** `1.3.0-alpha02` (jul/2026).
- **Ponto de atenção (decisão de versão):** o projeto está num toolchain *muito novo* (AGP 9.2.1, Compose BOM 2026.02). O estável `1.1.1` é de 2024 e pode ter atrito com um compilador Compose tão recente; já o `1.3.0-alpha02` **exige** justamente AGP 9.2.0+, casando com o projeto — mas é alpha. Ou seja, a escolha provável é entre "estável porém antigo" e "alinhado ao toolchain porém alpha". Isso é uma decisão de quando formos de fato adotar o Glance (Fase 3+), não um bloqueio agora.

## 2. Pipeline de dados `Repository → widget` (o que o spike validaria)

O Glance **não** usa ViewModel. O fluxo padrão é:

1. `GlanceAppWidget.provideGlance()` é o ponto onde se **carrega os dados** antes de compor — é lá que o widget chama o `Repository`/`Service`.
2. `provideContent { ... }` recebe o Composable Glance depois que os dados estão prontos.
3. Estado persistente do widget via `GlanceStateDefinition` + **DataStore** (`currentState<Preferences>()`), que recompõe o widget quando muda.
4. Atualização periódica (o countdown muda a cada dia) via **WorkManager** agendando `update()`.

**Encaixe na arquitetura atual:** o app já tem `Repository` + `Service` separados por interface (SOLID). O widget consumiria o **mesmo `GameService`/`GameRepository`** — nenhuma reengenharia. Esse é exatamente o pipeline que a arquitetura da Fase 1 já previu ("a arquitetura deve prever sync/widget, mas não implementar").

## 3. O único risco real: a capa (imagem remota)

Aqui está a diferença entre widget e app comum, e é o que um spike de verdade exercitaria:

- No app, o Compose carrega imagem por URL direto (Coil `AsyncImage`). **No Glance isso não existe do mesmo jeito**: `Image` usa `ImageProvider` (bitmap, drawable ou URI), não um painter com URL.
- Para exibir a capa remota é preciso **buscar o bitmap você mesmo** (via Coil/WorkManager) e passar por `ImageProvider(bitmap)`, **ou** usar um **content URI** com permissão concedida ao launcher.
- **Limite de tamanho do `RemoteViews`:** widget é desenhado noutro processo (o launcher), e todo o `RemoteViews` tem um teto de memória. Bitmap grande embutido pode estourar (`TransactionTooLargeException`). A recomendação oficial (sample do Android) é **preferir URI com permissão** a embutir bitmap quando a imagem é grande.
- **Alívio no horizonte (não essencial):** o Android 16 introduz `RemoteViews.DrawInstructions` + engine "RemoteCompose", que reduz parte das restrições do `RemoteViews`. Não dá pra depender disso com `minSdk 24`, mas indica que a plataforma caminha pra facilitar.

**Tradução prática para este app:** um widget **só com countdown (texto)** ou com **capa como placeholder/drawable** é praticamente risco zero. O trabalho de verdade — e o que valeria um spike — é o caminho da **capa remota**: buscar bitmap de forma assíncrona, redimensionar para caber no `RemoteViews` e lidar com cache/atualização.

## 4. Restrições gerais do `RemoteViews` (contexto, não bloqueio)

Herdadas por qualquer widget Glance: conjunto limitado de tipos de view, máx. ~10 filhos por container, sem desenho custom/canvas. Para um card "capa + título + countdown + talvez plataforma" isso é **folgado** — nenhuma dessas restrições atrapalha o layout planejado.

---

## Recomendação

Pela regra da spec, como a pesquisa **não** indica risco bloqueante, o caminho *by-the-book* é **seguir direto para a Fase 3 sem o spike**. Porém há uma nuance real (a capa remota) que um spike mínimo de-riscaria barato. Como é um trade-off real, fica a decisão de Igor entre três caminhos:

- **A) Pular o spike, ir para a Fase 3.** Alinhado à spec (sem risco bloqueante). O caminho da capa remota é conhecido e será resolvido quando o widget for construído de fato.
- **B) Spike mínimo texto-only (countdown, sem capa).** Valida o pipeline `Repository → provideGlance → DataStore → update via WorkManager` de ponta a ponta, com risco ~zero. Não toca no ponto sensível (imagem).
- **C) Spike completo (capa remota + countdown).** Valida também o carregamento de bitmap e o limite do `RemoteViews` — o único ponto de incerteza real. Mais trabalho, mas ataca justamente o risco que sobrou.

**Decisão de Igor (2026-07-12): opção A — pular o spike e seguir para a Fase 3.** Alinhado à spec, já que a pesquisa não indicou risco bloqueante. O caminho da capa remota (seção 3) fica registrado aqui e será resolvido quando o widget for de fato construído na Fase 3+.

---

## Fontes

- [Glance — release notes (versões, AGP/minSdk)](https://developer.android.com/jetpack/androidx/releases/glance)
- [Jetpack Glance — guia (provideGlance/provideContent, GlanceStateDefinition)](https://developer.android.com/develop/ui/compose/glance)
- [GlanceStateDefinition — API reference](https://developer.android.com/reference/kotlin/androidx/glance/state/GlanceStateDefinition)
- [Sample oficial: ImageGlanceWidget (bitmap vs. URI, limite de RemoteViews)](https://github.com/android/user-interface-samples/blob/main/AppWidget/app/src/main/java/com/example/android/appwidget/glance/image/ImageGlanceWidget.kt)
- [Schedule Image Displaying in Glance Widget with WorkManager (ITNEXT)](https://itnext.io/schedule-image-displaying-in-glance-widget-with-work-manager-api-cc474ed8571c)
- [From RemoteViews to RemoteCompose: mudanças no Android 16 (Medium)](https://medium.com/@fioravanti.luka/glance-remoteviews-and-remotecompose-what-actually-changed-in-android-16-4afc4b63b0ad)
- [Coil #2821 — imagem não baixa da rede em Glance (limitação de painter por URL)](https://github.com/coil-kt/coil/issues/2821)
