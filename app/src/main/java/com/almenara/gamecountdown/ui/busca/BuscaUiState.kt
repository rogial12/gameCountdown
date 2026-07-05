package com.almenara.gamecountdown.ui.busca // pacote da feature de busca dedicada

import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo, exibido nos resultados

// data class: um resultado de busca já pronto para exibição, com o countdown JÁ CALCULADO.
// espelha JogoCatalogo/JogoLista (jogo + dias); é um tipo próprio da feature para não acoplar Busca a outras telas.
data class JogoBusca(
    val game: Game,   // o jogo encontrado
    val dias: Long    // dias até o lançamento, calculados por GameService.getDaysUntilRelease
)

// data class: tudo que a tela de Busca precisa para se desenhar num dado momento.
data class BuscaUiState(
    val query: String = "",                        // texto digitado; vazio = ainda não buscou (mostra a dica)
    val resultados: List<JogoBusca> = emptyList(), // jogos que casam com o texto (por título)
    val historico: List<String> = emptyList()      // buscas anteriores, mais recente primeiro; exibido quando query está vazia
)
