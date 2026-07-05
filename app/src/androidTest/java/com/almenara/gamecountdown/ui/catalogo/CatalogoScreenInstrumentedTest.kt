package com.almenara.gamecountdown.ui.catalogo // mesmo pacote da tela testada

import androidx.compose.ui.test.assertIsDisplayed // verifica se um elemento está visível na tela
import androidx.compose.ui.test.junit4.createComposeRule // cria a "régua" que desenha e inspeciona Composables no teste
import androidx.compose.ui.test.onAllNodesWithContentDescription // encontra TODOS os elementos com a mesma contentDescription
import androidx.compose.ui.test.onNodeWithContentDescription // encontra um elemento pela contentDescription (ícones)
import androidx.compose.ui.test.onNodeWithText // encontra um elemento pelo texto exibido
import androidx.compose.ui.test.performClick // simula um toque num elemento
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação, parte do contrato do GameService
import com.almenara.gamecountdown.data.service.FiltroCatalogo // agrupador de filtros, parte do contrato do GameService
import com.almenara.gamecountdown.data.service.GameService // contrato que o fake abaixo implementa
import com.almenara.gamecountdown.ui.theme.GameCountdownTheme // tema do app (para a tela renderizar como em produção)
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Rule // marca a régua de teste do Compose como regra do JUnit
import org.junit.Test // marca um método como caso de teste

// fake do GameService só para este teste — não dá pra reaproveitar o fake de src/test (source set diferente),
// então este é uma versão enxuta, com o mínimo pra CatalogoViewModel funcionar de ponta a ponta na tela real.
private class FakeGameService(private val jogosBase: List<Game>) : GameService {

    private val watchedIds = mutableSetOf<String>() // ids marcados como "na lista pessoal" dentro do fake
    private val listenersWatched = mutableListOf<() -> Unit>() // callbacks inscritos via observarMudancasWatched

    override fun getGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> =
        jogosBase.map { it.copy(isWatched = it.id in watchedIds) }

    override fun getGameById(id: String): Game? =
        jogosBase.find { it.id == id }?.copy(isWatched = id in watchedIds)

    override fun searchGames(query: String): List<Game> =
        jogosBase.filter { it.title.contains(query, ignoreCase = true) }

    override fun getWatchedGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> =
        jogosBase.filter { it.id in watchedIds }

    override fun setWatched(id: String, watched: Boolean) {
        if (watched) watchedIds.add(id) else watchedIds.remove(id)
        listenersWatched.forEach { it() }
    }

    override fun observarMudancasWatched(callback: () -> Unit): () -> Unit {
        listenersWatched.add(callback)
        return { listenersWatched.remove(callback) }
    }

    // dias derivados do id do jogo (id "1" -> 1 dia), só pra ter um valor previsível no countdown exibido
    override fun getDaysUntilRelease(game: Game): Long = game.id.toLong()
}

// teste INSTRUMENTADO (roda num aparelho/emulador, não na JVM): valida a tela de Catálogo de ponta a ponta —
// ViewModel real + fake do Service, exatamente como em produção, só trocando de onde os dados vêm.
class CatalogoScreenInstrumentedTest {

    // a régua do Compose: permite desenhar um Composable (setContent) e depois procurar/tocar/verificar elementos
    @get:Rule
    val composeTestRule = createComposeRule()

    // dois jogos de exemplo; coverUrl vazio faz o GameCover usar o placeholder (sem baixar imagem da rede)
    private val jogos = listOf(
        Game(
            id = "1", title = "Iron Protocol", releaseDate = "2026-07-10",
            platforms = listOf(Platform.PS5), genres = listOf(Genre.ACTION),
            developer = "Apex Studios", synopsis = "", coverUrl = "",
            priceUsd = 59.99, priceBrl = 299.90, trailerId = null, preSaleDate = null, anticipationScore = 87
        ),
        Game(
            id = "2", title = "Hearthfall", releaseDate = "2026-08-15",
            platforms = listOf(Platform.PC), genres = listOf(Genre.RPG),
            developer = "Crimson Forge", synopsis = "", coverUrl = "",
            priceUsd = 69.99, priceBrl = 349.90, trailerId = null, preSaleDate = null, anticipationScore = 92
        )
    )

    // monta a tela real (ViewModel real + fake do Service) dentro do tema do app
    private fun montarTela(onJogoClick: (String) -> Unit = {}) {
        val viewModel = CatalogoViewModel(FakeGameService(jogos))
        composeTestRule.setContent {
            GameCountdownTheme {
                CatalogoScreen(viewModel = viewModel, onJogoClick = onJogoClick)
            }
        }
    }

    // ao abrir a tela, a lista inicial deve mostrar os cards dos dois jogos, com título e countdown corretos
    @Test
    fun catalogoScreen_exibe_a_lista_de_jogos_inicial() {
        montarTela()

        composeTestRule.onNodeWithText("Iron Protocol").assertIsDisplayed()
        composeTestRule.onNodeWithText("LANÇA AMANHÃ").assertIsDisplayed() // fake devolve dias=1 para o jogo id="1"
        composeTestRule.onNodeWithText("Hearthfall").assertIsDisplayed()
        composeTestRule.onNodeWithText("faltam 2 dias").assertIsDisplayed() // fake devolve dias=2 para o jogo id="2"
    }

    // tocar no botão de alternância (canto superior direito) troca a lista pela grade de calendário
    @Test
    fun catalogoScreen_alternar_para_grade_exibe_o_calendario() {
        montarTela()

        // antes de tocar, estamos na lista: o botão oferece ir para o calendário
        composeTestRule.onNodeWithContentDescription("Ver em calendário").performClick()

        // agora a grade do Calendario está desenhada (cabeçalho com as setas de mês é exclusivo dela)
        composeTestRule.onNodeWithContentDescription("Mês anterior").assertIsDisplayed()
        // e o botão trocou de rótulo, oferecendo voltar pra lista
        composeTestRule.onNodeWithContentDescription("Ver em lista").assertIsDisplayed()
    }

    // tocar no "+" do card adiciona o jogo à Lista Pessoal de verdade (via ViewModel real) e o ícone vira "✓"
    @Test
    fun catalogoScreen_tocar_no_mais_adiciona_a_lista_pessoal() {
        montarTela()

        // os dois cards começam com "+" (nenhum jogo está na lista pessoal ainda), então há 2 nós com essa
        // contentDescription — por isso usamos onAllNodes... em vez de onNodeWith... (que exige exatamente 1)
        val botoesAdicionar = composeTestRule.onAllNodesWithContentDescription("Adicionar à lista pessoal")
        botoesAdicionar[0].assertIsDisplayed()
        // clica no primeiro, que corresponde ao Iron Protocol (topo da lista)
        botoesAdicionar[0].performClick()

        // após o toque, aquele card passa a mostrar o ícone de "já está na lista"
        composeTestRule.onNodeWithContentDescription("Já está na lista pessoal").assertIsDisplayed()
    }

    // tocar no card (fora do "+") dispara onJogoClick com o id do jogo tocado, para a navegação a Detalhes
    @Test
    fun catalogoScreen_tocar_no_card_dispara_onJogoClick_com_o_id_certo() {
        var idClicado: String? = null

        montarTela(onJogoClick = { id -> idClicado = id })

        composeTestRule.onNodeWithText("Hearthfall").performClick()

        assertEquals("2", idClicado)
    }
}
