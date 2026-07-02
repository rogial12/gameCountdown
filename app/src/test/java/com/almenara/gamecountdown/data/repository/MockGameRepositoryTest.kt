package com.almenara.gamecountdown.data.repository

import com.almenara.gamecountdown.data.repository.mock.MockGameRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockGameRepositoryTest {

    private lateinit var repository: GameRepository

    @Before
    fun setUp() {
        repository = MockGameRepository()
    }

    @Test
    fun `getGames retorna todos os jogos`() {
        val games = repository.getGames()
        assertTrue(games.isNotEmpty())
    }

    @Test
    fun `getGames reflete status watched dos ids pre-marcados`() {
        val games = repository.getGames()
        assertTrue(games.find { it.id == "2" }?.isWatched == true)
        assertTrue(games.find { it.id == "4" }?.isWatched == true)
        assertFalse(games.find { it.id == "1" }?.isWatched == true)
    }

    @Test
    fun `getGameById retorna o jogo correto`() {
        val game = repository.getGameById("1")
        assertNotNull(game)
        assertEquals("1", game?.id)
    }

    @Test
    fun `getGameById retorna null para id inexistente`() {
        assertNull(repository.getGameById("999"))
    }

    @Test
    fun `searchGames filtra por titulo ignorando maiusculas`() {
        val results = repository.searchGames("neon")
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.title.contains("neon", ignoreCase = true) })
    }

    @Test
    fun `searchGames retorna lista vazia quando nada corresponde`() {
        assertTrue(repository.searchGames("xyzxyzxyz").isEmpty())
    }

    @Test
    fun `setWatched true adiciona jogo a lista pessoal`() {
        repository.setWatched("1", true)
        assertTrue(repository.getWatchedGames().any { it.id == "1" })
    }

    @Test
    fun `setWatched false remove jogo da lista pessoal`() {
        repository.setWatched("2", false)
        assertFalse(repository.getWatchedGames().any { it.id == "2" })
    }

    @Test
    fun `getWatchedGames retorna apenas jogos com isWatched true`() {
        assertTrue(repository.getWatchedGames().all { it.isWatched })
    }
}
