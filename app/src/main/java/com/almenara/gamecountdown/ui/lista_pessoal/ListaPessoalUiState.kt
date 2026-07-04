package com.almenara.gamecountdown.ui.lista_pessoal // pacote da feature "Jogos que estou de olho"

import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo, exibido na lista

// data class: um jogo da lista pessoal já pronto para exibição, com o countdown JÁ CALCULADO.
// espelha o papel do JogoCatalogo (jogo + dias), mas é um tipo próprio desta feature para não acoplar
// a Lista Pessoal ao pacote do Catálogo. Se no futuro valer a pena, os dois podem ser unificados num tipo comum.
data class JogoLista(
    val game: Game,   // o jogo em si (título, plataformas, preços, etc.)
    val dias: Long    // dias até o lançamento, calculados por GameService.getDaysUntilRelease; pode ser negativo (já lançou)
)

// data class: tudo que a tela de Lista Pessoal precisa para se desenhar num dado momento.
// a UI só lê este objeto — nunca chama o Service diretamente.
data class ListaPessoalUiState(
    val jogos: List<JogoLista> = emptyList() // jogos marcados como "de olho" (com dias); vazio = lista sem nada ainda
)
