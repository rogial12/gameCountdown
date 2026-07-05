package com.almenara.gamecountdown.di // pacote de "injeção de dependência" manual do app

import com.almenara.gamecountdown.data.repository.InMemorySearchHistoryRepository // guarda o histórico de buscas em memória
import com.almenara.gamecountdown.data.repository.mock.MockGameRepository // implementação falsa do Repository (Fase 2)
import com.almenara.gamecountdown.data.service.GameService // contrato das regras de negócio, exposto ao resto do app
import com.almenara.gamecountdown.data.service.GameServiceImpl // implementação real das regras de negócio
import com.almenara.gamecountdown.data.service.SearchHistoryService // contrato das regras do histórico de buscas
import com.almenara.gamecountdown.data.service.SearchHistoryServiceImpl // implementação real das regras do histórico

// AppContainer: o "composition root" único do app — o ÚNICO lugar que conhece as implementações concretas
// (GameServiceImpl + MockGameRepository). O resto do app (ViewModels, telas) só enxerga a interface GameService.
//
// Por que existe: agora há mais de uma tela (Catálogo e Lista Pessoal) que mexem no MESMO dado — a lista "de olho".
// Se cada Factory criasse seu próprio MockGameRepository, cada tela teria uma lista de watched separada e elas não
// conversariam. Com uma única instância compartilhada aqui, o que você marca no Catálogo aparece na Lista Pessoal.
//
// É um 'object' (singleton do Kotlin): existe uma só instância dele em todo o app. Quando a Fase 3 trocar o mock
// pela API real, só esta linha muda — nenhuma tela, ViewModel ou Factory precisa ser tocado.
object AppContainer {
    // 'by lazy': a instância só é criada na primeira vez que alguém usa gameService, e depois é reaproveitada.
    // como é um único objeto compartilhado, o MockGameRepository (e seu conjunto de watched) é o mesmo para todas as telas.
    val gameService: GameService by lazy { GameServiceImpl(MockGameRepository()) }

    // mesma ideia do gameService: uma única instância compartilhada do histórico de buscas.
    // hoje só a tela de Busca usa, mas centralizar aqui evita repetir o erro que o gameService já corrigiu
    // (cada Factory criando sua própria instância) caso outra tela venha a precisar do histórico no futuro.
    val searchHistoryService: SearchHistoryService by lazy {
        SearchHistoryServiceImpl(InMemorySearchHistoryRepository())
    }
}
