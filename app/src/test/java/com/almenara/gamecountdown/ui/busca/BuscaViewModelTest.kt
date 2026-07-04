package com.almenara.gamecountdown.ui.busca // mesmo pacote do código sendo testado

import com.almenara.gamecountdown.data.model.Game     // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre    // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.FiltroCatalogo    // exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.GameService       // interface que o fake abaixo implementa
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertTrue   // verifica se uma condição é verdadeira
import org.junit.Before              // marca o método que roda antes de cada teste
import org.junit.Test                // marca um método como caso de teste

// fake do GameService: implementa searchGames por título (case-insensitive) e dias = id do jogo.
private class FakeGameService(private val jogos: List<Game>) : GameService {
    override fun getGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> = jogos
    override fun getGameById(id: String): Game? = jogos.find { it.id == id }
    override fun searchGames(query: String): List<Game> =
        jogos.filter { it.title.contains(query, ignoreCase = true) }
    override fun getWatchedGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> = emptyList()
    override fun setWatched(id: String, watched: Boolean) {}
    override fun getDaysUntilRelease(game: Game): Long = game.id.toLong()
}

// classe de testes do BuscaViewModel
class BuscaViewModelTest {

    private val jogos = listOf(
        gameFake(id = "1", title = "Alpha Quest"),
        gameFake(id = "2", title = "Zeta Storm")
    )

    private lateinit var viewModel: BuscaViewModel

    @Before
    fun setUp() {
        viewModel = BuscaViewModel(FakeGameService(jogos))
    }

    // ao criar, sem nada digitado, o estado começa vazio (a tela mostra a dica de "busque um jogo")
    @Test
    fun `estado inicial esta vazio`() {
        val estado = viewModel.uiState.value
        assertEquals("", estado.query)
        assertTrue(estado.resultados.isEmpty())
    }

    // buscar por um termo do título deve retornar o jogo correspondente, com os dias calculados
    @Test
    fun `buscar retorna os jogos que casam com o titulo`() {
        viewModel.buscar("alpha")

        val estado = viewModel.uiState.value
        assertEquals("alpha", estado.query)
        assertEquals(listOf("1"), estado.resultados.map { it.game.id }) // só "Alpha Quest"
        assertEquals(listOf(1L), estado.resultados.map { it.dias })     // dias pareados (fake devolve id)
    }

    // buscar com texto vazio não deve retornar resultados (volta ao estado de dica)
    @Test
    fun `buscar com texto vazio zera os resultados`() {
        viewModel.buscar("alpha") // primeiro acha algo
        viewModel.buscar("")      // depois limpa

        val estado = viewModel.uiState.value
        assertEquals("", estado.query)
        assertTrue(estado.resultados.isEmpty())
    }

    // buscar por um termo sem correspondência deve retornar lista vazia (a tela mostra "nenhum jogo encontrado")
    @Test
    fun `buscar sem correspondencia retorna vazio`() {
        viewModel.buscar("xyz")

        val estado = viewModel.uiState.value
        assertEquals("xyz", estado.query)
        assertTrue(estado.resultados.isEmpty())
    }
}

// função de apoio: cria um Game fictício preenchendo só o essencial para estes testes
private fun gameFake(id: String, title: String): Game = Game(
    id = id,
    title = title,
    releaseDate = "2026-08-15",
    platforms = listOf(Platform.PC),
    genres = listOf(Genre.ACTION),
    developer = "Fake Studio",
    synopsis = "",
    coverUrl = "",
    priceUsd = null,
    priceBrl = null,
    trailerId = null,
    preSaleDate = null
)
