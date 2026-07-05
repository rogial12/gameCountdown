package com.almenara.gamecountdown.data.service // mesmo pacote das outras interfaces de Service

// interface: regras de negócio do histórico de buscas. A UI/ViewModel só conhece este contrato,
// nunca o SearchHistoryRepository diretamente — mesma separação já usada em GameService/GameRepository.
interface SearchHistoryService {
    fun getHistorico(): List<String> // devolve o histórico, mais recente primeiro

    // adiciona uma busca ao histórico, aplicando as regras: ignora texto em branco, não duplica
    // (repetir uma busca já existente move ela pro topo em vez de duplicar) e limita o tamanho da lista
    fun adicionar(query: String)

    fun limpar() // apaga todo o histórico
}
