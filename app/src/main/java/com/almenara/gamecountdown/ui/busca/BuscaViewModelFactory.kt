package com.almenara.gamecountdown.ui.busca // mesmo pacote do BuscaViewModel, que esta Factory constrói

import androidx.lifecycle.ViewModel // classe base de qualquer ViewModel do Android
import androidx.lifecycle.ViewModelProvider // interface que o Android usa para saber "como" criar um ViewModel
import com.almenara.gamecountdown.di.AppContainer // fornece as instâncias COMPARTILHADAS de GameService/SearchHistoryService
import com.almenara.gamecountdown.data.service.GameService // contrato que o ViewModel enxerga
import com.almenara.gamecountdown.data.service.SearchHistoryService // contrato do histórico de buscas

// Factory do BuscaViewModel: cria o ViewModel injetando o GameService e o SearchHistoryService compartilhados do AppContainer.
class BuscaViewModelFactory(
    private val gameService: GameService = AppContainer.gameService, // padrão = instância compartilhada; fake em testes
    private val searchHistoryService: SearchHistoryService = AppContainer.searchHistoryService // idem
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST") // o cast "as T" é seguro por causa do 'if' de verificação de tipo logo acima
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BuscaViewModel::class.java)) {
            return BuscaViewModel(gameService, searchHistoryService) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida: ${modelClass.name}")
    }
}
