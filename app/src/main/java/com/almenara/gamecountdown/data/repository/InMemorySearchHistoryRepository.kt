package com.almenara.gamecountdown.data.repository // mesmo pacote da interface que esta classe implementa

// implementação em memória do histórico de buscas: some quando o app fecha.
// não fica em repository/mock/ porque não está "fingindo" ser uma API — é uma implementação real,
// só que volátil; na Fase 3+ deve ser trocada por uma versão que persista de verdade (ex.: DataStore),
// sem precisar mudar o SearchHistoryService nem a UI (mesmo motivo de existir da interface).
class InMemorySearchHistoryRepository : SearchHistoryRepository {

    // 'var' porque salvarHistorico troca a lista inteira de uma vez (não é uma coleção mutável acumulando)
    private var historico: List<HistoricoBusca> = emptyList()

    override fun getHistorico(): List<HistoricoBusca> = historico

    override fun salvarHistorico(historico: List<HistoricoBusca>) {
        this.historico = historico
    }
}
