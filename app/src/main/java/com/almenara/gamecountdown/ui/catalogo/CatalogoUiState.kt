package com.almenara.gamecountdown.ui.catalogo // pacote da feature de catálogo, dentro de ui/

import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo, usado na lista exibida
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação, vem do Service
import com.almenara.gamecountdown.data.service.FiltroCatalogo // agrupador de filtros, vem do Service

// data class: um jogo já pronto para ser exibido na lista do Catálogo, com o countdown JÁ CALCULADO.
// existe porque o GameCard precisa dos "dias até o lançamento", que só o GameService sabe calcular (depende do relógio).
// em vez de a tela chamar o Service durante o desenho, o ViewModel calcula os dias ao montar o estado e guarda aqui —
// assim o CatalogoUiState descreve, sozinho, tudo que a tela precisa desenhar (princípio central do MVVM).
data class JogoCatalogo(
    val game: Game,   // o jogo em si (título, plataformas, preços, etc.)
    val dias: Long    // dias até o lançamento, calculados por GameService.getDaysUntilRelease; pode ser negativo (já lançou)
)

// data class: representa TUDO que a tela de Catálogo precisa saber para se desenhar num dado momento
// a UI (Compose) só lê este objeto — nunca chama o Service diretamente, nem guarda estado próprio
data class CatalogoUiState(
    val jogos: List<JogoCatalogo> = emptyList(),  // lista de jogos (com dias) já filtrada e ordenada, pronta pra exibir
    val filtro: FiltroCatalogo = FiltroCatalogo(), // filtro atualmente aplicado (plataforma/gênero/período)
    val ordenacao: CriterioOrdenacao = CriterioOrdenacao.MAIS_PROXIMOS, // critério de ordenação atual
    val buscando: Boolean = false,                // true quando o campo de busca está aberto (modo busca)
    val busca: String = "",                       // texto digitado na busca; enquanto vazio, o modo busca lista todos
    val carregando: Boolean = false,              // true enquanto os dados ainda estão sendo buscados
    val mensagemErro: String? = null              // texto de erro a exibir; null quando não há erro
)
