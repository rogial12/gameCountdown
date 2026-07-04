package com.almenara.gamecountdown.ui.lista_pessoal // pacote da feature "Jogos que estou de olho"

import androidx.lifecycle.ViewModel // classe base do Android que sobrevive a mudanças de configuração (ex: rotação)
import com.almenara.gamecountdown.data.service.GameService // contrato de regras de negócio, única dependência do ViewModel
import kotlinx.coroutines.flow.MutableStateFlow // versão "gravável" do estado observável, usada só dentro do ViewModel
import kotlinx.coroutines.flow.StateFlow // versão "somente leitura" do estado, exposta para a UI observar
import kotlinx.coroutines.flow.asStateFlow // converte o MutableStateFlow interno numa StateFlow pública somente leitura
import kotlinx.coroutines.flow.update // função que atualiza o StateFlow de forma segura (lê o atual e substitui)

// ViewModel da tela "Jogos que estou de olho": guarda o estado da tela e traduz ações em chamadas ao GameService.
// como o CatalogoViewModel, só conhece o contrato GameService — nunca o Repository nem o Mock.
class ListaPessoalViewModel(
    private val gameService: GameService // única dependência; injetada de fora (ver ListaPessoalViewModelFactory)
) : ViewModel() {

    // estado interno, gravável — só o próprio ViewModel pode alterá-lo
    private val _uiState = MutableStateFlow(ListaPessoalUiState())

    // estado público, somente leitura — é isso que a tela (Compose) observa via collectAsState()
    val uiState: StateFlow<ListaPessoalUiState> = _uiState.asStateFlow()

    // ao ser criado, o ViewModel já carrega a lista pessoal
    init {
        carregar()
    }

    // busca no Service os jogos marcados como "de olho", calcula os dias de cada um e atualiza o estado
    fun carregar() {
        val jogos = gameService.getWatchedGames() // só os jogos da lista pessoal
            // para cada jogo, calcula os dias até o lançamento e empacota num JogoLista (pronto pra exibir)
            .map { game -> JogoLista(game, gameService.getDaysUntilRelease(game)) }
        _uiState.update { it.copy(jogos = jogos) } // copy: novo estado, só trocando o campo "jogos"
    }

    // remove um jogo da lista pessoal (desmarca o "de olho") e recarrega a lista para refletir a saída na UI
    fun removerDaLista(id: String) {
        gameService.setWatched(id, false) // marca como não-observado; se já não estava, a operação é inofensiva
        carregar()
    }
}
