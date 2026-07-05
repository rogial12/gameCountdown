package com.almenara.gamecountdown.data.repository // mesmo pacote das outras interfaces de repositório

// data class: uma entrada do histórico de buscas — o JOGO que o usuário selecionou depois de buscar,
// não o termo em si (decisão de Igor: o histórico deve mostrar as tiles dos jogos).
// 'query' fica guardado como fallback: se 'gameId' não existir mais no catálogo no momento de exibir
// o histórico, a tela mostra o termo em vez de uma tile quebrada.
data class HistoricoBusca(
    val query: String,  // o termo que levou até o jogo; usado só como fallback textual
    val gameId: String  // o jogo selecionado depois daquela busca
)

// interface: define o contrato de ONDE o histórico de buscas fica guardado, sem dizer como.
// é "burra" de propósito — só guarda e devolve a lista tal como está. Regras de negócio (não duplicar,
// colocar a mais recente no topo, limitar o tamanho) NÃO vivem aqui; ficam no SearchHistoryService,
// mesma separação já usada entre GameRepository (dado cru) e GameServiceImpl (regras).
interface SearchHistoryRepository {
    fun getHistorico(): List<HistoricoBusca>              // devolve a lista guardada, na ordem em que foi salva
    fun salvarHistorico(historico: List<HistoricoBusca>)   // substitui a lista inteira pela nova, já pronta
}
