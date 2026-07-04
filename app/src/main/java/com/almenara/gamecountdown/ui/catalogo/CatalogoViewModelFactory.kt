package com.almenara.gamecountdown.ui.catalogo // mesmo pacote do CatalogoViewModel, que esta Factory constrói

import androidx.lifecycle.ViewModel // classe base de qualquer ViewModel do Android
import androidx.lifecycle.ViewModelProvider // interface que o Android usa para saber "como" criar um ViewModel
import com.almenara.gamecountdown.data.repository.mock.MockGameRepository // implementação falsa do Repository (Fase 2)
import com.almenara.gamecountdown.data.service.GameService // contrato que o ViewModel enxerga
import com.almenara.gamecountdown.data.service.GameServiceImpl // implementação real das regras de negócio

// Factory: o Android não sabe criar um CatalogoViewModel sozinho, porque o construtor dele pede um GameService
// (o Android só cria ViewModels de construtor vazio por padrão). Esta classe ensina o Android a fazer essa criação.
// é também o único lugar do app que conhece as implementações concretas (GameServiceImpl + MockGameRepository) —
// o resto do app (ViewModel, UI) só enxerga a interface GameService. Isso é o que chamamos de "composition root".
class CatalogoViewModelFactory(
    // parâmetro com valor padrão: em produção, ninguém precisa passar nada, a Factory monta a cadeia real sozinha;
    // em testes, dá pra injetar um GameService fake no lugar, se for necessário
    private val gameService: GameService = GameServiceImpl(MockGameRepository())
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
