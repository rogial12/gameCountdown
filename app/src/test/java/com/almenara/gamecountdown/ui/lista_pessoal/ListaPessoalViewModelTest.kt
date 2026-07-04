package com.almenara.gamecountdown.ui.lista_pessoal // mesmo pacote do código sendo testado

import com.almenara.gamecountdown.data.model.Game     // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre    // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.FiltroCatalogo    // exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.GameService       // interface que o fake abaixo implementa
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Before              // marca o método que roda antes de cada teste
import org.junit.Test                // marca um método como caso de teste

// fake do GameService só para estes testes: guarda os jogos e um conjunto de "watched" em memória.
// devolve dias = id do jogo (ex.: id "2" -> 2 dias), o que permite verificar o pareamento jogo↔countdown.
private class FakeGameService(
    private val jogos: List<Game>,        // catálogo completo fictício
    watchedInicial: Set<String>           // ids que já começam marcados como "de olho"
) : GameService {

    private val watched = watchedInicial.toMutableSet() // conjunto mutável de ids observados

    // getGames não é usado por esta tela; devolve o catálogo como está só para cumprir o contrato
    override fun getGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> = jogos

    override fun getGameById(id: String): Game? = jogos.find { it.id == id }

    override fun searchGames(query: String): List<Game> = emptyList() // não usado aqui

    // devolve só os jogos cujo id está no conjunto watched, marcando isWatched = true
    override fun getWatchedGames(): List<Game> =
        jogos.filter { it.id in watched }.map { it.copy(isWatched = true) }

    override fun setWatched(id: String, watched: Boolean) {
        if (watched) this.watched.add(id) else this.watched.remove(id)
    }

    override fun getDaysUntilRelease(game: Game): Long = game.id.toLong() // dias derivados do id, para verificar pareamento
}

// classe de testes do ListaPessoalViewModel
class ListaPessoalViewModelTest {

    // três jogos; começam com "2" e "3" marcados como "de olho" (o "1" fica de fora)
    private val jogos = listOf(
        gameFake(id = "1", title = "Alpha Quest"),
        gameFake(id = "2", title = "Beta Realm"),
        gameFake(id = "3", title = "Gamma Rising")
    )

    private lateinit var fakeService: FakeGameService
    private lateinit var viewModel: ListaPessoalViewModel

    // antes de cada teste: fake e ViewModel novos, isolando os testes entre si
    @Before
    fun setUp() {
        fakeService = FakeGameService(jogos, watchedInicial = setOf("2", "3"))
        viewModel = ListaPessoalViewModel(fakeService)
    }

    // ao ser criado, o ViewModel deve carregar só os jogos observados, com os dias calculados de cada um
    @Test
    fun `init carrega apenas os jogos observados com dias`() {
        val estado = viewModel.uiState.value
        assertEquals(listOf("2", "3"), estado.jogos.map { it.game.id }) // só os watched
        assertEquals(listOf(2L, 3L), estado.jogos.map { it.dias })      // dias pareados a cada jogo
    }

    // remover um jogo da lista deve desmarcá-lo e tirá-lo da lista recarregada
    @Test
    fun `removerDaLista tira o jogo da lista pessoal`() {
        viewModel.removerDaLista("2")

        val estado = viewModel.uiState.value
        assertEquals(listOf("3"), estado.jogos.map { it.game.id }) // sobra só o "3"
    }

    // remover um id que não está na lista não deve quebrar nem alterar a lista
    @Test
    fun `removerDaLista de id fora da lista nao altera nada`() {
        viewModel.removerDaLista("1") // "1" nunca esteve na lista

        val estado = viewModel.uiState.value
        assertEquals(listOf("2", "3"), estado.jogos.map { it.game.id }) // lista intacta
    }

    // desfazer uma remoção deve trazer o jogo de volta para a lista (ação "Desfazer" do snackbar)
    @Test
    fun `desfazerRemocao readiciona o jogo removido`() {
        viewModel.removerDaLista("2")     // sai da lista
        viewModel.desfazerRemocao("2")    // e volta

        val estado = viewModel.uiState.value
        assertEquals(listOf("2", "3"), estado.jogos.map { it.game.id }) // "2" de volta; ordem preservada
    }
}

// função de apoio: cria um Game fictício preenchendo só o essencial para estes testes
private fun gameFake(id: String, title: String): Game = Game(
    id = id,
    title = title,
    releaseDate = "2026-08-15",
    platforms = listOf(Platform.PS5),
    genres = listOf(Genre.RPG),
    developer = "Fake Studio",
    synopsis = "",
    coverUrl = "",
    priceUsd = null,
    priceBrl = null,
    trailerId = null,
    preSaleDate = null
)
