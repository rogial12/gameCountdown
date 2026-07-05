package com.almenara.gamecountdown.data.repository // mesmo pacote das outras interfaces de repositório

// interface: define o contrato de ONDE o histórico de buscas fica guardado, sem dizer como.
// é "burra" de propósito — só guarda e devolve a lista tal como está. Regras de negócio (não duplicar,
// colocar a mais recente no topo, limitar o tamanho) NÃO vivem aqui; ficam no SearchHistoryService,
// mesma separação já usada entre GameRepository (dado cru) e GameServiceImpl (regras).
interface SearchHistoryRepository {
    fun getHistorico(): List<String>              // devolve a lista guardada, na ordem em que foi salva
    fun salvarHistorico(historico: List<String>)   // substitui a lista inteira pela nova, já pronta
}
