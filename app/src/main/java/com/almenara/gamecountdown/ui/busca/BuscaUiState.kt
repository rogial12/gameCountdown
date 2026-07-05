package com.almenara.gamecountdown.ui.busca // pacote da feature de busca dedicada

import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo, exibido nos resultados

// data class: um resultado de busca já pronto para exibição, com o countdown JÁ CALCULADO.
// espelha JogoCatalogo/JogoLista (jogo + dias); é um tipo próprio da feature para não acoplar Busca a outras telas.
data class JogoBusca(
    val game: Game,   // o jogo encontrado
    val dias: Long    // dias até o lançamento, calculados por GameService.getDaysUntilRelease
)

// data class: uma entrada do histórico já pronta pra tela — o JOGO selecionado (com dias calculados),
// se ele ainda existir no catálogo; ou só o termo buscado, como fallback, se o jogo não existir mais
// (decisão de Igor: o histórico mostra tiles de jogos, o texto é só um substituto para o caso de o jogo sumir).
data class HistoricoBuscaItem(
    val query: String,      // termo que levou até o jogo; usado como fallback textual
    val jogo: JogoBusca?    // o jogo resolvido, com dias já calculados; null = mostra só o texto de 'query'
)

// data class: tudo que a tela de Busca precisa para se desenhar num dado momento.
data class BuscaUiState(
    val query: String = "",                        // texto digitado; vazio = ainda não buscou (mostra a dica/histórico)
    val resultados: List<JogoBusca> = emptyList(), // jogos que casam com o texto (por título)
    val historico: List<HistoricoBuscaItem> = emptyList() // jogos selecionados após buscas anteriores, mais recente primeiro
)
