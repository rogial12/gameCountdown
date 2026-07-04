package com.almenara.gamecountdown.ui.detalhes // pacote da feature de detalhes de um jogo

import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo, exibido na tela

// data class: tudo que a tela de Detalhes precisa para se desenhar num dado momento.
// a UI só lê este objeto — nunca chama o Service diretamente.
data class DetalhesUiState(
    val game: Game? = null,  // o jogo carregado; null enquanto carrega OU quando o id pedido não existe (mostra "não encontrado")
    val dias: Long = 0L      // dias até o lançamento (countdown), calculados no ViewModel; só relevante quando game != null
)
