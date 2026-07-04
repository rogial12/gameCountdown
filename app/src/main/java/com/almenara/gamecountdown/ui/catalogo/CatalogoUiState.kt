package com.almenara.gamecountdown.ui.catalogo // pacote da feature de catálogo, dentro de ui/

import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo, usado na lista exibida
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação, vem do Service
import com.almenara.gamecountdown.data.service.FiltroCatalogo // agrupador de filtros, vem do Service

// data class: representa TUDO que a tela de Catálogo precisa saber para se desenhar num dado momento
// a UI (Compose) só lê este objeto — nunca chama o Service diretamente, nem guarda estado próprio
data class CatalogoUiState(
    val jogos: List<Game> = emptyList(),          // lista de jogos já filtrada e ordenada, pronta pra exibir
    val filtro: FiltroCatalogo = FiltroCatalogo(), // filtro atualmente aplicado (plataforma/gênero/período)
    val ordenacao: CriterioOrdenacao = CriterioOrdenacao.MAIS_PROXIMOS, // critério de ordenação atual
    val carregando: Boolean = false,              // true enquanto os dados ainda estão sendo buscados
    val mensagemErro: String? = null              // texto de erro a exibir; null quando não há erro
)
