package com.almenara.gamecountdown.ui.busca // pacote da feature de busca dedicada

import androidx.lifecycle.ViewModel // classe base do Android que sobrevive a mudanças de configuração
import com.almenara.gamecountdown.data.service.GameService // contrato de regras de negócio, única dependência do ViewModel
import kotlinx.coroutines.flow.MutableStateFlow // versão "gravável" do estado observável, usada só dentro do ViewModel
import kotlinx.coroutines.flow.StateFlow // versão "somente leitura" do estado, exposta para a UI observar
import kotlinx.coroutines.flow.asStateFlow // converte o MutableStateFlow interno numa StateFlow pública somente leitura
import kotlinx.coroutines.flow.update // função que atualiza o StateFlow de forma segura (lê o atual e substitui)

// ViewModel da tela de Busca dedicada: guarda o texto digitado e os resultados por título.
// como os outros ViewModels, só conhece o contrato GameService. Não carrega nada no início —
// começa vazio (a tela mostra uma dica) e só busca quando o usuário digita.
class BuscaViewModel(
    private val gameService: GameService // única dependência; injetada de fora (ver BuscaViewModelFactory)
) : ViewModel() {

    // estado interno, gravável — só o próprio ViewModel altera
    private val _uiState = MutableStateFlow(BuscaUiState())

    // estado público, somente leitura — a tela observa via collectAsState()
    val uiState: StateFlow<BuscaUiState> = _uiState.asStateFlow()

    // atualiza o texto buscado e recalcula os resultados
    fun buscar(query: String) {
        // texto vazio ou só espaços = sem busca ativa: guarda o texto e zera os resultados (a tela mostra a dica)
        val resultados = if (query.isBlank()) {
            emptyList()
        } else {
            // busca por título no Service e calcula os dias de cada resultado (o card precisa do countdown)
            gameService.searchGames(query)
                .map { game -> JogoBusca(game, gameService.getDaysUntilRelease(game)) }
        }
        _uiState.update { it.copy(query = query, resultados = resultados) }
    }
}
