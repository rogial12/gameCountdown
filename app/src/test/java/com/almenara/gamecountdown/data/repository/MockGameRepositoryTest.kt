package com.almenara.gamecountdown.data.repository // mesmo pacote do código que está sendo testado

import com.almenara.gamecountdown.data.repository.mock.MockGameRepository // importa a classe que será testada
import org.junit.Assert.assertEquals  // verifica se dois valores são iguais
import org.junit.Assert.assertFalse   // verifica se uma condição é falsa
import org.junit.Assert.assertNotNull // verifica se um valor não é null
import org.junit.Assert.assertNull    // verifica se um valor é null
import org.junit.Assert.assertTrue    // verifica se uma condição é verdadeira
import org.junit.Before               // marca o método que roda antes de cada teste
import org.junit.Test                 // marca um método como caso de teste

// classe de testes do MockGameRepository
// cada @Test é um cenário independente que verifica um comportamento esperado
class MockGameRepositoryTest {

    // lateinit: declara a variável agora, mas inicializa no @Before
    // o tipo é a interface (não o mock) — testa o contrato, não a implementação
    private lateinit var repository: GameRepository

    // @Before: este método roda automaticamente antes de cada @Test
    // cria uma instância nova do mock para que cada teste comece do zero
    @Before
    fun setUp() {
        repository = MockGameRepository()
    }

    // verifica se o catálogo tem pelo menos um jogo
    @Test
    fun `getGames retorna todos os jogos`() {
        val games = repository.getGames()          // chama o método sendo testado
        assertTrue(games.isNotEmpty())             // falha se a lista estiver vazia
    }

    // verifica se os jogos 2 e 4 (pré-marcados no mock) aparecem como watched,
    // e se o jogo 1 (não pré-marcado) aparece como não watched
    @Test
    fun `getGames reflete status watched dos ids pre-marcados`() {
        val games = repository.getGames()
        assertTrue(games.find { it.id == "2" }?.isWatched == true)  // id "2" deve ser watched
        assertTrue(games.find { it.id == "4" }?.isWatched == true)  // id "4" deve ser watched
        assertFalse(games.find { it.id == "1" }?.isWatched == true) // id "1" NÃO deve ser watched
    }

    // verifica se buscar por um ID existente retorna o jogo correto
    @Test
    fun `getGameById retorna o jogo correto`() {
        val game = repository.getGameById("1") // busca o jogo com id "1"
        assertNotNull(game)                    // falha se retornar null
        assertEquals("1", game?.id)           // falha se o ID retornado for diferente de "1"
    }

    // verifica se buscar por um ID inexistente retorna null (sem quebrar o app)
    @Test
    fun `getGameById retorna null para id inexistente`() {
        assertNull(repository.getGameById("999")) // "999" não existe no mock; deve retornar null
    }

    // verifica se a busca por texto funciona sem diferenciar maiúsculas de minúsculas
    @Test
    fun `searchGames filtra por titulo ignorando maiusculas`() {
        val results = repository.searchGames("neon")              // busca em minúsculo
        assertTrue(results.isNotEmpty())                          // deve encontrar "Neon Samurai 2"
        assertTrue(results.all { it.title.contains("neon", ignoreCase = true) }) // todos os resultados devem conter "neon"
    }

    // verifica se a busca retorna lista vazia quando nenhum jogo corresponde
    @Test
    fun `searchGames retorna lista vazia quando nada corresponde`() {
        assertTrue(repository.searchGames("xyzxyzxyz").isEmpty()) // query sem correspondência → lista vazia
    }

    // verifica se marcar um jogo como watched o adiciona à lista pessoal
    @Test
    fun `setWatched true adiciona jogo a lista pessoal`() {
        repository.setWatched("1", true)                            // marca o jogo "1" como watched
        assertTrue(repository.getWatchedGames().any { it.id == "1" }) // "1" deve aparecer na lista pessoal
    }

    // verifica se desmarcar um jogo como watched o remove da lista pessoal
    @Test
    fun `setWatched false remove jogo da lista pessoal`() {
        repository.setWatched("2", false)                              // remove o jogo "2" (que começa watched)
        assertFalse(repository.getWatchedGames().any { it.id == "2" }) // "2" não deve mais aparecer na lista
    }

    // verifica se getWatchedGames retorna apenas jogos com isWatched = true
    @Test
    fun `getWatchedGames retorna apenas jogos com isWatched true`() {
        assertTrue(repository.getWatchedGames().all { it.isWatched }) // todos os itens devem ter isWatched = true
    }

    // verifica se o callback inscrito é chamado quando setWatched muda o conjunto de observados
    // (é essa notificação que mantém Catálogo/Lista Pessoal/Detalhes sincronizados entre si)
    @Test
    fun `observarMudancasWatched chama o callback quando setWatched muda algo`() {
        var chamadas = 0
        repository.observarMudancasWatched { chamadas++ }

        repository.setWatched("1", true)
        assertEquals(1, chamadas)

        repository.setWatched("1", false)
        assertEquals(2, chamadas)
    }

    // verifica se a função de cancelamento devolvida realmente para de notificar aquele callback
    @Test
    fun `observarMudancasWatched cancelar inscricao para de notificar`() {
        var chamadas = 0
        val cancelar = repository.observarMudancasWatched { chamadas++ }

        repository.setWatched("1", true) // ainda inscrito
        cancelar()                       // cancela a inscrição
        repository.setWatched("1", false) // não deveria mais notificar

        assertEquals(1, chamadas)
    }

    // verifica se múltiplos inscritos são todos notificados (várias telas vivas ao mesmo tempo)
    @Test
    fun `observarMudancasWatched notifica todos os inscritos`() {
        var chamadasA = 0
        var chamadasB = 0
        repository.observarMudancasWatched { chamadasA++ }
        repository.observarMudancasWatched { chamadasB++ }

        repository.setWatched("1", true)

        assertEquals(1, chamadasA)
        assertEquals(1, chamadasB)
    }
}
