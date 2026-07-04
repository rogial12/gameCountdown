package com.almenara.gamecountdown.ui.catalogo // mesmo pacote do código sendo testado

import com.almenara.gamecountdown.data.model.Game     // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre    // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação, vem do Service
import com.almenara.gamecountdown.data.service.FiltroCatalogo    // agrupador de filtros, vem do Service
import com.almenara.gamecountdown.data.service.GameService       // interface que o fake abaixo implementa
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertNull   // verifica se um valor é nulo
import org.junit.Assert.assertTrue   // verifica se uma condição é verdadeira
import org.junit.Before              // marca o método que roda antes de cada teste
import org.junit.Test                // marca um método como caso de teste

// fake do GameService feito só para este teste — a lógica de filtro/ordenação já é testada em GameServiceImplTest,
// então este fake só precisa devolver dados previsíveis e registrar com quais argumentos foi chamado
private class FakeGameService(private val jogosBase: List<Game>) : GameService {

    private val watchedIds = mutableSetOf<String>() // ids marcados como "na lista pessoal" dentro do fake
    var ultimoFiltro: FiltroCatalogo? = null         // guarda o último filtro recebido, pra inspecionar no teste
    var ultimaOrdenacao: CriterioOrdenacao? = null   // guarda a última ordenação recebida, pra inspecionar no teste
    var chamadasSetWatched = 0                       // conta quantas vezes setWatched foi efetivamente chamado

    override fun getGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> {
        ultimoFiltro = filtro
        ultimaOrdenacao = ordenacao
        return jogosBase.map { it.copy(isWatched = it.id in watchedIds) }
    }

    override fun getGameById(id: String): Game? =
        jogosBase.find { it.id == id }?.copy(isWatched = id in watchedIds)

    override fun searchGames(query: String): List<Game> =
        jogosBase.filter { it.title.contains(query, ignoreCase = true) }

    override fun getWatchedGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> =
        jogosBase.filter { it.id in watchedIds }

    override fun setWatched(id: String, watched: Boolean) {
        chamadasSetWatched++
        if (watched) watchedIds.add(id) else watchedIds.remove(id)
    }

    // devolve um número de dias derivado do id do jogo (ex.: id "1" -> 1 dia) —
    // assim os testes conseguem verificar que CADA jogo recebeu o countdown correspondente ao seu id
    override fun getDaysUntilRelease(game: Game): Long = game.id.toLong()
}

// classe de testes do CatalogoViewModel
class CatalogoViewModelTest {

    // dois jogos bastam pra testar orquestração; o conteúdo de cada campo não importa muito aqui
    private val jogos = listOf(
        Game(
            id = "1", title = "Alpha Quest", releaseDate = "2026-08-01",
            platforms = listOf(Platform.PS5), genres = listOf(Genre.ACTION),
            developer = "Fake Studio", synopsis = "", coverUrl = "",
            priceUsd = null, priceBrl = null, trailerId = null, preSaleDate = null
        ),
        Game(
            id = "2", title = "Zeta Storm", releaseDate = "2026-09-01",
            platforms = listOf(Platform.PC), genres = listOf(Genre.RPG),
            developer = "Fake Studio", synopsis = "", coverUrl = "",
            priceUsd = null, priceBrl = null, trailerId = null, preSaleDate = null
        )
    )

    private lateinit var fakeService: FakeGameService
    private lateinit var viewModel: CatalogoViewModel

    // roda antes de cada teste: cria um fake e um ViewModel novos, isolando os testes entre si
    @Before
    fun setUp() {
        fakeService = FakeGameService(jogos)
        viewModel = CatalogoViewModel(fakeService)
    }

    // ao ser criado, o ViewModel deve carregar o catálogo automaticamente (via init { carregarJogos() })
    // agora cada item do estado é um JogoCatalogo (jogo + dias), então acessamos o jogo via it.game
    @Test
    fun `estado inicial carrega o catalogo automaticamente`() {
        val estado = viewModel.uiState.value
        assertEquals(listOf("1", "2"), estado.jogos.map { it.game.id })
        assertEquals(FiltroCatalogo(), estado.filtro) // filtro padrão: nenhum
        assertEquals(CriterioOrdenacao.MAIS_PROXIMOS, estado.ordenacao) // ordenação padrão
    }

    // cada JogoCatalogo deve carregar os dias calculados pelo Service para AQUELE jogo (pareamento correto)
    // o fake devolve dias = id do jogo, então o jogo "1" deve ter dias=1 e o jogo "2" dias=2
    @Test
    fun `estado carrega os dias de countdown de cada jogo`() {
        val estado = viewModel.uiState.value
        assertEquals(listOf(1L, 2L), estado.jogos.map { it.dias })
    }

    // aplicarFiltro deve atualizar o filtro no estado E repassar esse filtro ao Service na recarga
    @Test
    fun `aplicarFiltro atualiza o estado e repassa o filtro ao service`() {
        val filtro = FiltroCatalogo(plataforma = Platform.PS5)

        viewModel.aplicarFiltro(filtro)

        assertEquals(filtro, viewModel.uiState.value.filtro)
        assertEquals(filtro, fakeService.ultimoFiltro)
    }

    // aplicarOrdenacao deve atualizar a ordenação no estado E repassá-la ao Service na recarga
    @Test
    fun `aplicarOrdenacao atualiza o estado e repassa a ordenacao ao service`() {
        viewModel.aplicarOrdenacao(CriterioOrdenacao.ALFABETICA)

        assertEquals(CriterioOrdenacao.ALFABETICA, viewModel.uiState.value.ordenacao)
        assertEquals(CriterioOrdenacao.ALFABETICA, fakeService.ultimaOrdenacao)
    }

    // alternarWatched deve inverter o isWatched do jogo e refletir isso na lista recarregada
    @Test
    fun `alternarWatched marca o jogo como observado e recarrega a lista`() {
        viewModel.alternarWatched("1")

        val jogo1 = viewModel.uiState.value.jogos.find { it.game.id == "1" }
        assertTrue(jogo1?.game?.isWatched == true)
        assertEquals(1, fakeService.chamadasSetWatched)
    }

    // chamar alternarWatched de novo no mesmo jogo deve desmarcá-lo (é uma alternância, não só ligar)
    @Test
    fun `alternarWatched chamado duas vezes desfaz a marcacao`() {
        viewModel.alternarWatched("1")
        viewModel.alternarWatched("1")

        val jogo1 = viewModel.uiState.value.jogos.find { it.game.id == "1" }
        assertTrue(jogo1?.game?.isWatched == false)
        assertEquals(2, fakeService.chamadasSetWatched)
    }

    // se o id não existir no catálogo, alternarWatched não deve chamar o Service nem alterar o estado
    @Test
    fun `alternarWatched com id inexistente nao chama o service`() {
        viewModel.alternarWatched("id-que-nao-existe")

        assertEquals(0, fakeService.chamadasSetWatched)
        assertNull(viewModel.uiState.value.mensagemErro) // e não deve gerar erro nenhum
    }
}
