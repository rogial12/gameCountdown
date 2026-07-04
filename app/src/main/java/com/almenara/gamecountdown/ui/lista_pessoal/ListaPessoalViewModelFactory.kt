package com.almenara.gamecountdown.ui.lista_pessoal // mesmo pacote do ListaPessoalViewModel, que esta Factory constrói

import androidx.lifecycle.ViewModel // classe base de qualquer ViewModel do Android
import androidx.lifecycle.ViewModelProvider // interface que o Android usa para saber "como" criar um ViewModel
import com.almenara.gamecountdown.di.AppContainer // fornece a instância COMPARTILHADA de GameService
import com.almenara.gamecountdown.data.service.GameService // contrato que o ViewModel enxerga

// Factory do ListaPessoalViewModel: ensina o Android a criar o ViewModel injetando um GameService.
// Usa o MESMO GameService do AppContainer que o Catálogo usa — é isso que faz a lista "de olho" ser
// consistente entre as duas telas (marcar no Catálogo reflete aqui, e remover aqui reflete no Catálogo).
class ListaPessoalViewModelFactory(
    // padrão = instância compartilhada do AppContainer; em testes, injeta-se um GameService fake
    private val gameService: GameService = AppContainer.gameService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST") // o cast "as T" é seguro por causa do 'if' de verificação de tipo logo acima
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // isAssignableFrom: verifica se o tipo pedido é (ou herda de) ListaPessoalViewModel
        if (modelClass.isAssignableFrom(ListaPessoalViewModel::class.java)) {
            return ListaPessoalViewModel(gameService) as T // cria o ViewModel injetando o Service
        }
        // se pedirem um ViewModel diferente, falha de forma explícita
        throw IllegalArgumentException("Classe de ViewModel desconhecida: ${modelClass.name}")
    }
}
