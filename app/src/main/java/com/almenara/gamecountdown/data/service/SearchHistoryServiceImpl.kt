package com.almenara.gamecountdown.data.service // mesmo pacote da interface que esta classe implementa

import com.almenara.gamecountdown.data.repository.HistoricoBusca // entrada do histórico (query + gameId)
import com.almenara.gamecountdown.data.repository.SearchHistoryRepository // fonte dos dados, injetada aqui

// implementação real das regras de negócio do histórico de buscas, usando o SearchHistoryRepository injetado
class SearchHistoryServiceImpl(
    private val repository: SearchHistoryRepository, // fonte dos dados; guarda só a lista crua
    private val limite: Int = 10                     // quantas buscas manter no máximo; 10 por decisão de Igor
) : SearchHistoryService {

    override fun getHistorico(): List<HistoricoBusca> = repository.getHistorico()

    override fun adicionar(query: String, gameId: String) {
        val normalizada = query.trim() // remove espaços nas pontas antes de guardar
        if (normalizada.isBlank()) return // busca vazia não vira entrada de histórico

        val atual = repository.getHistorico()
        // remove qualquer entrada do MESMO jogo (o histórico é de jogos, não de termos — decisão de Igor);
        // a seleção de agora vai pro topo mesmo que o jogo já estivesse lá, com a query mais recente
        val semDuplicata = atual.filterNot { it.gameId == gameId }
        val novoHistorico = (listOf(HistoricoBusca(normalizada, gameId)) + semDuplicata).take(limite)

        repository.salvarHistorico(novoHistorico)
    }

    override fun limpar() {
        repository.salvarHistorico(emptyList())
    }
}
