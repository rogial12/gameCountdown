package com.almenara.gamecountdown.ui.catalogo // pacote da feature de catálogo, dentro de ui/

import androidx.lifecycle.ViewModel // classe base do Android que sobrevive a mudanças de configuração (ex: rotação de tela)
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação, vem do Service
import com.almenara.gamecountdown.data.service.FiltroCatalogo // agrupador de filtros, vem do Service
import com.almenara.gamecountdown.data.service.GameService // contrato de regras de negócio, única dependência do ViewModel
import kotlinx.coroutines.flow.MutableStateFlow // versão "gravável" do estado observável, usada só dentro do ViewModel
import kotlinx.coroutines.flow.StateFlow // versão "somente leitura" do estado, exposta para a UI observar
import kotlinx.coroutines.flow.asStateFlow // converte o MutableStateFlow interno numa StateFlow pública somente leitura
import kotlinx.coroutines.flow.update // função de extensão que atualiza o StateFlow de forma segura (lê o valor atual e substitui)

// ViewModel da tela de Catálogo: guarda o estado da tela e traduz ações do usuário em chamadas ao GameService
// note que o ViewModel não conhece o Repository nem o Mock — só enxerga o contrato GameService
class CatalogoViewModel(
    private val gameService: GameService // única dependência; injetada de fora (ver CatalogoViewModelFactory)
) : ViewModel() {

    // estado interno, gravável — só o próprio ViewModel pode alterá-lo
    private val _uiState = MutableStateFlow(CatalogoUiState())

    // estado público, somente leitura — é isso que a tela (Compose) observa via collectAsState()
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    // bloco de inicialização: roda assim que o ViewModel é criado, carregando o catálogo inicial
    init {
        carregarJogos()
    }

    // busca os jogos no Service usando o filtro e a ordenação atuais do estado, e atualiza o estado com o resultado
    fun carregarJogos() {
        // 1) pede ao Service a lista já filtrada e ordenada
        val jogos = gameService.getGames(
            filtro = _uiState.value.filtro,
            ordenacao = _uiState.value.ordenacao
        )
            // 2) para cada jogo, calcula os dias até o lançamento e empacota tudo num JogoCatalogo (pronto pra exibir)
            //    isso é feito AQUI (no ViewModel) para a tela não precisar chamar o Service durante o desenho
            .map { game -> JogoCatalogo(game, gameService.getDaysUntilRelease(game)) }
        _uiState.update { it.copy(jogos = jogos) } // copy: gera um novo estado, só trocando o campo "jogos"
    }

    // troca o filtro ativo e recarrega a lista de jogos com o novo filtro aplicado
    fun aplicarFiltro(filtro: FiltroCatalogo) {
        _uiState.update { it.copy(filtro = filtro) }
        carregarJogos()
    }

    // troca o critério de ordenação e recarrega a lista de jogos já reordenada
    fun aplicarOrdenacao(ordenacao: CriterioOrdenacao) {
        _uiState.update { it.copy(ordenacao = ordenacao) }
        carregarJogos()
    }

    // alterna o jogo entre "na lista pessoal" e "fora dela", e recarrega a lista pra refletir a mudança na UI
    fun alternarWatched(id: String) {
        val jogo = gameService.getGameById(id) ?: return // se o id não existir, não faz nada
        gameService.setWatched(id, !jogo.isWatched) // inverte o estado atual de "observado"
        carregarJogos()
    }
}
