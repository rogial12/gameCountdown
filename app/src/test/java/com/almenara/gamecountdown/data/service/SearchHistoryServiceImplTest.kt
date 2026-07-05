package com.almenara.gamecountdown.data.service // mesmo pacote do código sendo testado

import com.almenara.gamecountdown.data.repository.SearchHistoryRepository // interface que o fake abaixo implementa
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertTrue   // verifica se uma condição é verdadeira
import org.junit.Before              // marca o método que roda antes de cada teste
import org.junit.Test                // marca um método como caso de teste

// fake do SearchHistoryRepository: guarda a lista em memória, do jeito mais simples possível —
// só para isolar os testes do Service da implementação real (InMemorySearchHistoryRepository)
private class FakeSearchHistoryRepository : SearchHistoryRepository {
    private var historico: List<String> = emptyList()
    override fun getHistorico(): List<String> = historico
    override fun salvarHistorico(historico: List<String>) {
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

    // adicionar deve colocar a busca no topo do histórico
    @Test
    fun `adicionar coloca a busca no topo`() {
        service.adicionar("iron")
        service.adicionar("hearthfall")

        assertEquals(listOf("hearthfall", "iron"), service.getHistorico())
    }

    // adicionar uma busca em branco (vazia ou só espaços) não deve criar entrada nenhuma
    @Test
    fun `adicionar ignora busca em branco`() {
        service.adicionar("")
        service.adicionar("   ")

        assertTrue(service.getHistorico().isEmpty())
    }

    // adicionar deve remover espaços nas pontas antes de guardar
    @Test
    fun `adicionar remove espacos nas pontas`() {
        service.adicionar("  iron  ")

        assertEquals(listOf("iron"), service.getHistorico())
    }

    // repetir uma busca já existente não deve duplicar — deve mover ela pro topo
    @Test
    fun `adicionar busca repetida move pro topo em vez de duplicar`() {
        service.adicionar("iron")
        service.adicionar("hearthfall")
        service.adicionar("iron") // repete a primeira

        assertEquals(listOf("iron", "hearthfall"), service.getHistorico()) // só uma ocorrência de "iron", agora no topo
    }

    // a comparação de duplicata não deve diferenciar maiúsculas de minúsculas
    @Test
    fun `adicionar trata duplicata sem diferenciar maiusculas`() {
        service.adicionar("Iron")
        service.adicionar("iron") // mesma busca, caixa diferente

        assertEquals(listOf("iron"), service.getHistorico()) // uma entrada só, com o texto da chamada mais recente
    }

    // o histórico não deve crescer além do limite configurado — o mais antigo cai fora
    @Test
    fun `adicionar corta no limite configurado`() {
        service.adicionar("um")
        service.adicionar("dois")
        service.adicionar("tres")
        service.adicionar("quatro") // limite é 3; "um" (o mais antigo) deve sair

        assertEquals(listOf("quatro", "tres", "dois"), service.getHistorico())
    }

    // limpar deve esvaziar o histórico inteiro
    @Test
    fun `limpar esvazia o historico`() {
        service.adicionar("iron")
        service.adicionar("hearthfall")

        service.limpar()

        assertTrue(service.getHistorico().isEmpty())
    }
}
