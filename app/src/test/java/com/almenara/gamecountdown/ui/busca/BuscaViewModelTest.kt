package com.almenara.gamecountdown.ui.busca // mesmo pacote do código sendo testado

import com.almenara.gamecountdown.data.model.Game     // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre    // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.data.repository.HistoricoBusca // entrada crua do histórico (query + gameId)
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.FiltroCatalogo    // exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.GameService       // interface que o fake abaixo implementa
import com.almenara.gamecountdown.data.service.SearchHistoryService // interface do outro fake abaixo
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertNull   // verifica se um valor é nulo
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
    override fun observarMudancasWatched(callback: () -> Unit): () -> Unit = {} // não usado pela Busca
    override fun getDaysUntilRelease(game: Game): Long = game.id.toLong()
}

// fake do SearchHistoryService: guarda o histórico em memória, com a mesma regra de "sem duplicata por jogo,
// mais recente no topo" (simplificada) só o suficiente para testar que o ViewModel repassa e resolve o que o
// Service devolve — a regra de negócio de verdade (dedup, limite) já é testada em SearchHistoryServiceImplTest
private class FakeSearchHistoryService(historicoInicial: List<HistoricoBusca> = emptyList()) : SearchHistoryService {
    private var historico: List<HistoricoBusca> = historicoInicial
    var chamadasLimpar = 0 // conta quantas vezes limpar() foi chamado

    override fun getHistorico(): List<HistoricoBusca> = historico

    override fun adicionar(query: String, gameId: String) {
        if (query.isBlank()) return
        historico = listOf(HistoricoBusca(query, gameId)) + historico.filterNot { it.gameId == gameId }
    }

    override fun limpar() {
        chamadasLimpar++
        historico = emptyList()
    }
}

// classe de testes do BuscaViewModel
class BuscaViewModelTest {

    private val jogos = listOf(
        gameFake(id = "1", title = "Alpha Quest"),
        gameFake(id = "2", title = "Zeta Storm")
    )

    private lateinit var fakeHistoryService: FakeSearchHistoryService
    private lateinit var viewModel: BuscaViewModel

    @Before
    fun setUp() {
        fakeHistoryService = FakeSearchHistoryService()
        viewModel = BuscaViewModel(FakeGameService(jogos), fakeHistoryService)
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

    // ao ser criado, o ViewModel deve carregar o histórico já salvo e RESOLVER cada entrada no jogo
    // correspondente (com dias calculados) — o histórico é de jogos, não de texto (decisão de Igor)
    @Test
    fun `init carrega o historico ja salvo resolvendo os jogos`() {
        val historicoPreExistente = FakeSearchHistoryService(
            listOf(HistoricoBusca(query = "zeta", gameId = "2"), HistoricoBusca(query = "alpha", gameId = "1"))
        )
        val vm = BuscaViewModel(FakeGameService(jogos), historicoPreExistente)

        val historico = vm.uiState.value.historico
        assertEquals(listOf("2", "1"), historico.map { it.jogo?.game?.id })
        assertEquals(listOf(2L, 1L), historico.map { it.jogo?.dias }) // dias pareados (fake devolve id)
    }

    // se o jogo referenciado numa entrada do histórico não existir mais no catálogo, 'jogo' deve ficar null —
    // a tela cai no fallback textual (mostra a query) em vez de uma tile quebrada
    @Test
    fun `init com jogo inexistente no historico cai no fallback textual`() {
        val historicoComJogoSumido = FakeSearchHistoryService(
            listOf(HistoricoBusca(query = "algo antigo", gameId = "id-que-nao-existe-mais"))
        )
        val vm = BuscaViewModel(FakeGameService(jogos), historicoComJogoSumido)

        val entrada = vm.uiState.value.historico.single()
        assertEquals("algo antigo", entrada.query) // o texto original continua disponível como fallback
        assertNull(entrada.jogo) // mas o jogo não foi encontrado
    }

    // registrarBuscaSelecionada deve adicionar o jogo tocado ao histórico, junto com a query atual
    @Test
    fun `registrarBuscaSelecionada adiciona o jogo selecionado ao historico`() {
        viewModel.buscar("alpha")
        viewModel.registrarBuscaSelecionada(gameId = "1")

        val entrada = viewModel.uiState.value.historico.single()
        assertEquals("alpha", entrada.query)
        assertEquals("1", entrada.jogo?.game?.id)
    }

    // limparHistorico deve esvaziar o histórico no estado e repassar ao Service
    @Test
    fun `limparHistorico esvazia o historico no estado`() {
        viewModel.buscar("alpha")
        viewModel.registrarBuscaSelecionada(gameId = "1")

        viewModel.limparHistorico()

        assertTrue(viewModel.uiState.value.historico.isEmpty())
        assertEquals(1, fakeHistoryService.chamadasLimpar)
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
