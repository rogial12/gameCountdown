package com.almenara.gamecountdown.data.service // mesmo pacote da interface que esta classe implementa

import com.almenara.gamecountdown.data.repository.SearchHistoryRepository // fonte dos dados, injetada aqui

// implementação real das regras de negócio do histórico de buscas, usando o SearchHistoryRepository injetado
class SearchHistoryServiceImpl(
    private val repository: SearchHistoryRepository, // fonte dos dados; guarda só a lista crua
    private val limite: Int = 10                     // quantas buscas manter no máximo; 10 por decisão de Igor
) : SearchHistoryService {

    override fun getHistorico(): List<String> = repository.getHistorico()

    override fun adicionar(query: String) {
        val normalizada = query.trim() // remove espaços nas pontas antes de guardar/comparar
        if (normalizada.isBlank()) return // busca vazia não vira entrada de histórico

        val atual = repository.getHistorico()
        // remove qualquer ocorrência igual (sem diferenciar maiúsculas/minúsculas) para não duplicar;
        // a busca de agora vai pro topo mesmo que já existisse antes, refletindo que foi a mais recente
        val semDuplicata = atual.filterNot { it.equals(normalizada, ignoreCase = true) }
        val novoHistorico = (listOf(normalizada) + semDuplicata).take(limite) // corta no limite após inserir no topo

        repository.salvarHistorico(novoHistorico)
    }

    override fun limpar() {
        repository.salvarHistorico(emptyList())
    }
}
