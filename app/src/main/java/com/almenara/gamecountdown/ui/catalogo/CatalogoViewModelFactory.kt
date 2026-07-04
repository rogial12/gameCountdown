package com.almenara.gamecountdown.ui.catalogo // mesmo pacote do CatalogoViewModel, que esta Factory constrói

import androidx.lifecycle.ViewModel // classe base de qualquer ViewModel do Android
import androidx.lifecycle.ViewModelProvider // interface que o Android usa para saber "como" criar um ViewModel
import com.almenara.gamecountdown.di.AppContainer // fornece a instância compartilhada de GameService
import com.almenara.gamecountdown.data.service.GameService // contrato que o ViewModel enxerga

// Factory: o Android não sabe criar um CatalogoViewModel sozinho, porque o construtor dele pede um GameService
// (o Android só cria ViewModels de construtor vazio por padrão). Esta classe ensina o Android a fazer essa criação.
// O GameService concreto vem do AppContainer (composition root único), para ser compartilhado com as outras telas.
class CatalogoViewModelFactory(
    // parâmetro com valor padrão: em produção usa a instância compartilhada do AppContainer;
    // em testes, dá pra injetar um GameService fake no lugar
    private val gameService: GameService = AppContainer.gameService
) : ViewModelProvider.Factory { // implementa o contrato que o Android exige para criar ViewModels sob demanda

    // @Suppress: o cast "as T" abaixo não tem como o compilador verificar em tempo de compilação,
    // mas o "if" logo acima garante que só chega lá quando o tipo pedido é o esperado
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // isAssignableFrom: verifica se modelClass é (ou herda de) CatalogoViewModel
        if (modelClass.isAssignableFrom(CatalogoViewModel::class.java)) {
            return CatalogoViewModel(gameService) as T // cria o ViewModel de verdade, injetando o Service
        }
        // se alguém tentar usar esta Factory para criar um ViewModel diferente, falha de forma explícita
        throw IllegalArgumentException("Classe de ViewModel desconhecida: ${modelClass.name}")
    }
}
