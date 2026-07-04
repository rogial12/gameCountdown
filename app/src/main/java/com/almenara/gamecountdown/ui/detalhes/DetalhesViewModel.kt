package com.almenara.gamecountdown.ui.detalhes // pacote da feature de detalhes de um jogo

import androidx.lifecycle.ViewModel // classe base do Android que sobrevive a mudanças de configuração (ex: rotação)
import com.almenara.gamecountdown.data.service.GameService // contrato de regras de negócio, única dependência do ViewModel
import kotlinx.coroutines.flow.MutableStateFlow // versão "gravável" do estado observável, usada só dentro do ViewModel
import kotlinx.coroutines.flow.StateFlow // versão "somente leitura" do estado, exposta para a UI observar
import kotlinx.coroutines.flow.asStateFlow // converte o MutableStateFlow interno numa StateFlow pública somente leitura
import kotlinx.coroutines.flow.update // função que atualiza o StateFlow de forma segura (lê o atual e substitui)

// ViewModel da tela de Detalhes: carrega UM jogo específico (pelo id) e guarda o estado da tela.
// como os outros ViewModels, só conhece o contrato GameService — nunca o Repository nem o Mock.
class DetalhesViewModel(
    private val gameService: GameService, // fonte dos dados/regras; injetado de fora
    private val gameId: String            // qual jogo esta tela mostra; injetado pela Factory (virá da navegação)
) : ViewModel() {

    // estado interno, gravável — só o próprio ViewModel pode alterá-lo
    private val _uiState = MutableStateFlow(DetalhesUiState())

    // estado público, somente leitura — é isso que a tela (Compose) observa via collectAsState()
    val uiState: StateFlow<DetalhesUiState> = _uiState.asStateFlow()

    // ao ser criado, já carrega o jogo pedido
    init {
        carregar()
    }

    // busca o jogo pelo id, calcula os dias até o lançamento e atualiza o estado
    fun carregar() {
        val game = gameService.getGameById(gameId)                 // pode ser null se o id não existir
        val dias = game?.let { gameService.getDaysUntilRelease(it) } ?: 0L // só calcula os dias se o jogo existe
        _uiState.update { it.copy(game = game, dias = dias) }
    }

    // alterna o jogo entre "de olho" e "fora da lista" e recarrega, refletindo o novo isWatched na tela
    fun alternarWatched() {
        val atual = _uiState.value.game ?: return   // se não há jogo carregado, não faz nada
        gameService.setWatched(atual.id, !atual.isWatched) // inverte o estado atual de observado
        carregar()                                   // recarrega para o estado refletir a mudança
    }
}
