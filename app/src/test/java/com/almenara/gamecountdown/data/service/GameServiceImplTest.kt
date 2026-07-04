package com.almenara.gamecountdown.data.service // mesmo pacote do código sendo testado

import com.almenara.gamecountdown.data.model.Game     // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre    // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.data.repository.GameRepository // interface que o fake abaixo implementa
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertTrue   // verifica se uma condição é verdadeira
import org.junit.Before              // marca o método que roda antes de cada teste
import org.junit.Test                // marca um método como caso de teste
import java.time.Clock       // relógio abstrato, usado aqui para "congelar" a data de hoje
import java.time.LocalDate   // representa uma data (ano-mês-dia)
import java.time.ZoneOffset  // fuso usado para converter LocalDate em um instante fixo do Clock

// fake do Repository feito só para este teste, com datas escolhidas a dedo em relação a "hoje"
// (em vez de usar o MockGameRepository, que tem datas fixas em calendário e quebraria com o tempo)
private class FakeGameRepository(hoje: LocalDate) : GameRepository {

    // 4 jogos com plataforma, gênero, score e data de lançamento pensados para diferenciar cada ordenação/filtro
    private val games = listOf(
        Game( // lança em exatamente 7 dias — testa o limite (inclusive) do filtro SEMANA
            id = "1", title = "Alpha Quest", releaseDate = hoje.plusDays(7).toString(),
            platforms = listOf(Platform.PS5), genres = listOf(Genre.ACTION),
            developer = "Fake Studio", synopsis = "", coverUrl = "",
            priceUsd = null, priceBrl = null, trailerId = null, preSaleDate = null,
            anticipationScore = 50
        ),
        Game( // lança em 8 dias — um dia fora da janela SEMANA, dentro de MES/TRIMESTRE/ANO
            id = "2", title = "Zeta Storm", releaseDate = hoje.plusDays(8).toString(),
            platforms = listOf(Platform.PC), genres = listOf(Genre.RPG),
            developer = "Fake Studio", synopsis = "", coverUrl = "",
            priceUsd = null, priceBrl = null, trailerId = null, preSaleDate = null,
            anticipationScore = 90 // maior score do conjunto — deve vir primeiro em MAIS_AGUARDADOS
        ),
        Game( // lança em exatamente 90 dias — testa o limite (inclusive) do filtro TRIMESTRE
            id = "3", title = "Mid Kingdom", releaseDate = hoje.plusDays(90).toString(),
            platforms = listOf(Platform.PS5), genres = listOf(Genre.RPG),
            developer = "Fake Studio", synopsis = "", coverUrl = "",
            priceUsd = null, priceBrl = null, trailerId = null, preSaleDate = null,
            anticipationScore = 70
        ),
        Game( // lança em 400 dias — fora até da janela ANO (365 dias)
            id = "4", title = "Omega Void", releaseDate = hoje.plusDays(400).toString(),
            platforms = listOf(Platform.XBOX_SERIES), genres = listOf(Genre.ACTION),
            developer = "Fake Studio", synopsis = "", coverUrl = "",
            priceUsd = null, priceBrl = null, trailerId = null, preSaleDate = null,
            anticipationScore = 30 // menor score do conjunto — deve vir por último em MAIS_AGUARDADOS
        )
    )

    private val watchedIds = mutableSetOf<String>() // conjunto simples; começa vazio neste fake

    override fun getGames(): List<Game> = games.map { it.copy(isWatched = it.id in watchedIds) }
    override fun getGameById(id: String): Game? = games.find { it.id == id }
    override fun searchGames(query: String): List<Game> = games.filter { it.title.contains(query, ignoreCase = true) }
    override fun getWatchedGames(): List<Game> = games.filter { it.id in watchedIds }
    override fun setWatched(id: String, watched: Boolean) {
        if (watched) watchedIds.add(id) else watchedIds.remove(id)
    }
}

// classe de testes do GameServiceImpl
class GameServiceImplTest {

    // "hoje" fixo para os testes: 1º de julho de 2026 — qualquer dia serve, o importante é ser fixo
    private val hoje: LocalDate = LocalDate.of(2026, 7, 1)

    // Clock.fixed: cria um relógio que sempre reporta o mesmo instante, não importa quando o teste rode
    // atStartOfDay(UTC).toInstant() converte a LocalDate escolhida no instante correspondente à meia-noite UTC
    private val clock: Clock = Clock.fixed(hoje.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private lateinit var service: GameService // tipo é a interface — testa o contrato, não a implementação

    // roda antes de cada teste: cria um fake novo e um service novo, isolando os testes entre si
    @Before
    fun setUp() {
        service = GameServiceImpl(repository = FakeGameRepository(hoje), clock = clock)
    }

    // MAIS_AGUARDADOS deve ordenar por anticipationScore, do maior para o menor
    @Test
    fun `getGames ordena por mais aguardados`() {
        val ids = service.getGames(ordenacao = CriterioOrdenacao.MAIS_AGUARDADOS).map { it.id }
        assertEquals(listOf("2", "3", "1", "4"), ids) // scores: 90, 70, 50, 30
    }

    // MAIS_PROXIMOS deve ordenar pela data de lançamento, da mais próxima para a mais distante
    @Test
    fun `getGames ordena por mais proximos`() {
        val ids = service.getGames(ordenacao = CriterioOrdenacao.MAIS_PROXIMOS).map { it.id }
        assertEquals(listOf("1", "2", "3", "4"), ids) // +7, +8, +90, +400 dias
    }

    // ALFABETICA deve ordenar pelo título, em ordem alfabética
    @Test
    fun `getGames ordena alfabeticamente`() {
        val titulos = service.getGames(ordenacao = CriterioOrdenacao.ALFABETICA).map { it.title }
        assertEquals(listOf("Alpha Quest", "Mid Kingdom", "Omega Void", "Zeta Storm"), titulos)
    }

    // filtro de plataforma deve manter só os jogos daquela plataforma
    @Test
    fun `getGames filtra por plataforma`() {
        val ids = service.getGames(filtro = FiltroCatalogo(plataforma = Platform.PS5)).map { it.id }
        assertEquals(setOf("1", "3"), ids.toSet()) // só "Alpha Quest" e "Mid Kingdom" são PS5
    }

    // filtro de gênero deve manter só os jogos daquele gênero
    @Test
    fun `getGames filtra por genero`() {
        val ids = service.getGames(filtro = FiltroCatalogo(genero = Genre.RPG)).map { it.id }
        assertEquals(setOf("2", "3"), ids.toSet()) // só "Zeta Storm" e "Mid Kingdom" são RPG
    }

    // filtro de período SEMANA deve incluir o jogo a 7 dias (limite) e excluir o de 8 dias
    @Test
    fun `getGames filtra por periodo semana incluindo o limite`() {
        val ids = service.getGames(filtro = FiltroCatalogo(periodoLancamento = PeriodoLancamento.SEMANA)).map { it.id }
        assertEquals(setOf("1"), ids.toSet())
    }

    // filtro de período TRIMESTRE deve incluir os jogos a 7, 8 e 90 dias (limite), excluindo o de 400 dias
    @Test
    fun `getGames filtra por periodo trimestre incluindo o limite`() {
        val ids = service.getGames(filtro = FiltroCatalogo(periodoLancamento = PeriodoLancamento.TRIMESTRE)).map { it.id }
        assertEquals(setOf("1", "2", "3"), ids.toSet())
    }

    // filtro de período ANO deve excluir o jogo a 400 dias, mesmo sendo o único período que quase o alcançaria
    @Test
    fun `getGames filtra por periodo ano excluindo jogo alem de 365 dias`() {
        val ids = service.getGames(filtro = FiltroCatalogo(periodoLancamento = PeriodoLancamento.ANO)).map { it.id }
        assertTrue("4" !in ids) // "Omega Void" (400 dias) não deve aparecer
        assertEquals(setOf("1", "2", "3"), ids.toSet())
    }

    // filtros de plataforma e gênero combinados devem ser aplicados em conjunto (E lógico, não OU)
    @Test
    fun `getGames combina filtro de plataforma e genero`() {
        val ids = service.getGames(
            filtro = FiltroCatalogo(plataforma = Platform.PS5, genero = Genre.RPG)
        ).map { it.id }
        assertEquals(listOf("3"), ids) // só "Mid Kingdom" é PS5 e RPG ao mesmo tempo
    }

    // getDaysUntilRelease deve retornar exatamente a diferença de dias entre "hoje" e a releaseDate
    @Test
    fun `getDaysUntilRelease calcula dias corretamente`() {
        val jogo = service.getGameById("1")!!
        assertEquals(7L, service.getDaysUntilRelease(jogo))
    }

    // getGameById, searchGames, getWatchedGames e setWatched devem repassar corretamente ao Repository
    @Test
    fun `metodos de passagem repassam ao repository`() {
        assertEquals("Alpha Quest", service.getGameById("1")?.title)
        assertTrue(service.searchGames("zeta").any { it.id == "2" })

        service.setWatched("1", true)
        assertTrue(service.getWatchedGames().any { it.id == "1" })

        service.setWatched("1", false)
        assertTrue(service.getWatchedGames().none { it.id == "1" })
    }

    // getWatchedGames deve aplicar o filtro só sobre os jogos já marcados como "de olho" (não sobre o catálogo inteiro)
    @Test
    fun `getWatchedGames filtra apenas dentro dos jogos observados`() {
        service.setWatched("1", true) // Alpha Quest — PS5
        service.setWatched("2", true) // Zeta Storm — PC
        service.setWatched("3", true) // Mid Kingdom — PS5

        val ids = service.getWatchedGames(filtro = FiltroCatalogo(plataforma = Platform.PS5)).map { it.id }
        assertEquals(setOf("1", "3"), ids.toSet()) // "2" é PC, mesmo observado, some do resultado
    }

    // getWatchedGames deve ordenar os jogos observados com o mesmo critério usado no catálogo
    @Test
    fun `getWatchedGames ordena os jogos observados`() {
        service.setWatched("2", true) // score 90
        service.setWatched("4", true) // score 30

        val ids = service.getWatchedGames(ordenacao = CriterioOrdenacao.MAIS_AGUARDADOS).map { it.id }
        assertEquals(listOf("2", "4"), ids) // maior score primeiro
    }
}
