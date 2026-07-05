package com.almenara.gamecountdown.data.repository // mesmo pacote do código sendo testado

import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertTrue   // verifica se uma condição é verdadeira
import org.junit.Before              // marca o método que roda antes de cada teste
import org.junit.Test                // marca um método como caso de teste

// classe de testes do InMemorySearchHistoryRepository
class InMemorySearchHistoryRepositoryTest {

    // tipo é a interface — testa o contrato, não a implementação
    private lateinit var repository: SearchHistoryRepository

    @Before
    fun setUp() {
        repository = InMemorySearchHistoryRepository()
    }

    // antes de qualquer salvamento, o histórico deve começar vazio
    @Test
    fun `getHistorico comeca vazio`() {
        assertTrue(repository.getHistorico().isEmpty())
    }

    // salvarHistorico deve substituir a lista inteira pela recebida
    @Test
    fun `salvarHistorico substitui a lista guardada`() {
        repository.salvarHistorico(listOf("iron", "hearthfall"))
        assertEquals(listOf("iron", "hearthfall"), repository.getHistorico())

        repository.salvarHistorico(listOf("neon")) // uma segunda chamada substitui, não acumula
        assertEquals(listOf("neon"), repository.getHistorico())
    }
}
