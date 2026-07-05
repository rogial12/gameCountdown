package com.almenara.gamecountdown.data.service // mesmo pacote das outras interfaces de Service

import com.almenara.gamecountdown.data.repository.HistoricoBusca // entrada do histórico (query + gameId)

// interface: regras de negócio do histórico de buscas. A UI/ViewModel só conhece este contrato,
// nunca o SearchHistoryRepository diretamente — mesma separação já usada em GameService/GameRepository.
interface SearchHistoryService {
    fun getHistorico(): List<HistoricoBusca> // devolve o histórico, mais recente primeiro

    // registra que, depois de buscar 'query', o usuário selecionou o jogo 'gameId'.
    // regras aplicadas: ignora query em branco, não duplica o MESMO jogo (repetir a seleção de um jogo
    // já presente move a entrada pro topo, com a query mais recente, em vez de duplicar) e limita o tamanho da lista
    fun adicionar(query: String, gameId: String)

    fun limpar() // apaga todo o histórico
}
