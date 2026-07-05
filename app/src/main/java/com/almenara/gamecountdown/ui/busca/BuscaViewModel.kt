package com.almenara.gamecountdown.ui.busca // pacote da feature de busca dedicada

import androidx.lifecycle.ViewModel // classe base do Android que sobrevive a mudanças de configuração
import com.almenara.gamecountdown.data.service.GameService // contrato de regras de negócio, única dependência do ViewModel
import com.almenara.gamecountdown.data.service.SearchHistoryService // contrato das regras do histórico de buscas
import kotlinx.coroutines.flow.MutableStateFlow // versão "gravável" do estado observável, usada só dentro do ViewModel
import kotlinx.coroutines.flow.StateFlow // versão "somente leitura" do estado, exposta para a UI observar
import kotlinx.coroutines.flow.asStateFlow // converte o MutableStateFlow interno numa StateFlow pública somente leitura
import kotlinx.coroutines.flow.update // função que atualiza o StateFlow de forma segura (lê o atual e substitui)

// ViewModel da tela de Busca dedicada: guarda o texto digitado, os resultados por título e o histórico de buscas.
// como os outros ViewModels, só conhece os contratos GameService e SearchHistoryService.
// não carrega resultados no início (a tela mostra uma dica/histórico e só busca quando o usuário digita),
// mas já carrega o histórico, que existe independente de haver uma busca em andamento.
class BuscaViewModel(
    private val gameService: GameService,               // busca jogos por título e calcula o countdown
    private val searchHistoryService: SearchHistoryService // guarda/lê o histórico de buscas do usuário
) : ViewModel() {

    // estado interno, gravável — só o próprio ViewModel altera
    private val _uiState = MutableStateFlow(BuscaUiState())

    // estado público, somente leitura — a tela observa via collectAsState()
    val uiState: StateFlow<BuscaUiState> = _uiState.asStateFlow()

    // ao ser criado, já carrega o histórico salvo (independente de haver alguma busca em andamento)
    init {
        _uiState.update { it.copy(historico = searchHistoryService.getHistorico()) }
    }

    // atualiza o texto buscado e recalcula os resultados
    fun buscar(query: String) {
        // texto vazio ou só espaços = sem busca ativa: guarda o texto e zera os resultados (a tela mostra a dica/histórico)
        val resultados = if (query.isBlank()) {
            emptyList()
        } else {
            // busca por título no Service e calcula os dias de cada resultado (o card precisa do countdown)
            gameService.searchGames(query)
                .map { game -> JogoBusca(game, gameService.getDaysUntilRelease(game)) }
        }
        _uiState.update { it.copy(query = query, resultados = resultados) }
    }

    // chamado quando o usuário toca um resultado da busca: é o sinal de que a busca "deu certo",
    // então a query atual entra pro histórico (o toque no resultado em si é tratado por quem chama a tela,
    // que ainda navega para os Detalhes — este método só cuida do histórico)
    fun registrarBuscaSelecionada() {
        searchHistoryService.adicionar(_uiState.value.query)
        _uiState.update { it.copy(historico = searchHistoryService.getHistorico()) }
    }

    // apaga todo o histórico de buscas e atualiza o estado para refletir a lista vazia
    fun limparHistorico() {
        searchHistoryService.limpar()
        _uiState.update { it.copy(historico = emptyList()) }
    }
}
