package com.almenara.gamecountdown.data.service // mesmo pacote do código sendo testado

import com.almenara.gamecountdown.data.repository.HistoricoBusca // entrada do histórico (query + gameId)
import com.almenara.gamecountdown.data.repository.SearchHistoryRepository // interface que o fake abaixo implementa
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertTrue   // verifica se uma condição é verdadeira
import org.junit.Before              // marca o método que roda antes de cada teste
import org.junit.Test                // marca um método como caso de teste

// fake do SearchHistoryRepository: guarda a lista em memória, do jeito mais simples possível —
// só para isolar os testes do Service da implementação real (InMemorySearchHistoryRepository)
private class FakeSearchHistoryRepository : SearchHistoryRepository {
    private var historico: List<HistoricoBusca> = emptyList()
    override fun getHistorico(): List<HistoricoBusca> = historico
    override fun salvarHistorico(historico: List<HistoricoBusca>) {
        this.historico = historico
    }
}

// classe de testes do SearchHistoryServiceImpl — aqui mora a regra de negócio real desta feature
class SearchHistoryServiceImplTest {

    private lateinit var repository: FakeSearchHistoryRepository
    private lateinit var service: SearchHistoryService

    @Before
    fun setUp() {
        repository = FakeSearchHistoryRepository()
        service = SearchHistoryServiceImpl(repository, limite = 3) // limite baixo pra testar o corte sem precisar de 10 chamadas
    }

    // getHistorico deve começar vazio, antes de qualquer adicionar()
    @Test
    fun `getHistorico comeca vazio`() {
        assertTrue(service.getHistorico().isEmpty())
    }

    // adicionar deve colocar o jogo selecionado no topo do histórico, junto com a query que levou até ele
    @Test
    fun `adicionar coloca o jogo selecionado no topo`() {
        service.adicionar("iron", gameId = "1")
        service.adicionar("hearth", gameId = "2")

        assertEquals(
            listOf(HistoricoBusca("hearth", "2"), HistoricoBusca("iron", "1")),
            service.getHistorico()
        )
    }

    // adicionar com uma busca em branco (vazia ou só espaços) não deve criar entrada nenhuma,
    // mesmo com um gameId válido — sem termo não há o que guardar como fallback
    @Test
    fun `adicionar ignora busca em branco`() {
        service.adicionar("", gameId = "1")
        service.adicionar("   ", gameId = "2")

        assertTrue(service.getHistorico().isEmpty())
    }

    // adicionar deve remover espaços nas pontas da query antes de guardar
    @Test
    fun `adicionar remove espacos nas pontas da query`() {
        service.adicionar("  iron  ", gameId = "1")

        assertEquals(listOf(HistoricoBusca("iron", "1")), service.getHistorico())
    }

    // selecionar de novo o MESMO jogo não deve duplicar a entrada — deve mover ela pro topo,
    // atualizando a query guardada para a busca mais recente que levou até ele
    @Test
    fun `adicionar o mesmo jogo de novo move pro topo em vez de duplicar`() {
        service.adicionar("iron", gameId = "1")
        service.adicionar("hearth", gameId = "2")
        service.adicionar("protocol", gameId = "1") // mesmo jogo "1", achado por outro termo

        assertEquals(
            listOf(HistoricoBusca("protocol", "1"), HistoricoBusca("hearth", "2")), // uma entrada só pro jogo "1", no topo, com a query nova
            service.getHistorico()
        )
    }

    // jogos diferentes encontrados pela MESMA query devem gerar entradas separadas —
    // a deduplicação é por jogo, não por texto buscado
    @Test
    fun `adicionar jogos diferentes com a mesma query nao deduplica`() {
        service.adicionar("iron", gameId = "1")
        service.adicionar("iron", gameId = "2") // busca igual, jogo diferente

        assertEquals(
            listOf(HistoricoBusca("iron", "2"), HistoricoBusca("iron", "1")),
            service.getHistorico()
        )
    }

    // o histórico não deve crescer além do limite configurado — o mais antigo cai fora
    @Test
    fun `adicionar corta no limite configurado`() {
        service.adicionar("um", gameId = "1")
        service.adicionar("dois", gameId = "2")
        service.adicionar("tres", gameId = "3")
        service.adicionar("quatro", gameId = "4") // limite é 3; o jogo "1" (o mais antigo) deve sair

        assertEquals(
            listOf(HistoricoBusca("quatro", "4"), HistoricoBusca("tres", "3"), HistoricoBusca("dois", "2")),
            service.getHistorico()
        )
    }

    // limpar deve esvaziar o histórico inteiro
    @Test
    fun `limpar esvazia o historico`() {
        service.adicionar("iron", gameId = "1")
        service.adicionar("hearth", gameId = "2")

        service.limpar()

        assertTrue(service.getHistorico().isEmpty())
    }
}
