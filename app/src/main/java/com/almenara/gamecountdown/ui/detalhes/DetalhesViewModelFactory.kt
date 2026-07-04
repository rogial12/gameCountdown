package com.almenara.gamecountdown.ui.detalhes // mesmo pacote do DetalhesViewModel, que esta Factory constrói

import androidx.lifecycle.ViewModel // classe base de qualquer ViewModel do Android
import androidx.lifecycle.ViewModelProvider // interface que o Android usa para saber "como" criar um ViewModel
import com.almenara.gamecountdown.di.AppContainer // fornece a instância COMPARTILHADA de GameService
import com.almenara.gamecountdown.data.service.GameService // contrato que o ViewModel enxerga

// Factory do DetalhesViewModel: além do GameService, precisa saber QUAL jogo mostrar (gameId).
// diferente das outras Factories, esta carrega um argumento variável (o id), que virá da navegação
// (a tela de origem passa o id do jogo tocado). O GameService vem do AppContainer, compartilhado com as demais telas.
class DetalhesViewModelFactory(
    private val gameId: String,                             // id do jogo a exibir; obrigatório
    private val gameService: GameService = AppContainer.gameService // padrão = instância compartilhada; fake em testes
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST") // o cast "as T" é seguro por causa do 'if' de verificação de tipo logo acima
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // isAssignableFrom: verifica se o tipo pedido é (ou herda de) DetalhesViewModel
        if (modelClass.isAssignableFrom(DetalhesViewModel::class.java)) {
            return DetalhesViewModel(gameService, gameId) as T // cria o ViewModel injetando Service e id
        }
        // se pedirem um ViewModel diferente, falha de forma explícita
        throw IllegalArgumentException("Classe de ViewModel desconhecida: ${modelClass.name}")
    }
}
