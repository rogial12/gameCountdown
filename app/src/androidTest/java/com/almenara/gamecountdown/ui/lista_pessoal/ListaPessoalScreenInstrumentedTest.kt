package com.almenara.gamecountdown.ui.lista_pessoal // mesmo pacote da tela testada

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

// fake do GameService só para este teste — mesmo papel do FakeGameService de CatalogoScreenInstrumentedTest,
// mas aqui os dois jogos JÁ COMEÇAM "de olho" (watchedIds pré-preenchido), pois é isso que a Lista Pessoal exibe.
private class FakeGameService(
    private val jogosBase: List<Game>,
    watchedIniciais: Set<String> // ids que já nascem marcados como "de olho", simulando quem chegou à tela
) : GameService {

    private val watchedIds = watchedIniciais.toMutableSet() // ids marcados como "na lista pessoal" dentro do fake
    private val listenersWatched = mutableListOf<() -> Unit>() // callbacks inscritos via observarMudancasWatched

    override fun getGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> =
        jogosBase.map { it.copy(isWatched = it.id in watchedIds) }

    override fun getGameById(id: String): Game? =
        jogosBase.find { it.id == id }?.copy(isWatched = id in watchedIds)

    override fun searchGames(query: String): List<Game> =
        jogosBase.filter { it.title.contains(query, ignoreCase = true) }

    // devolve só os jogos marcados como watched; filtro/ordenação não são exercitados aqui, pois já são
    // cobertos pelos testes unitários do Service e do ListaPessoalViewModel
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

    // dias derivados do id do jogo (id "2" -> 2 dias), só pra ter um valor previsível no countdown exibido
    override fun getDaysUntilRelease(game: Game): Long = game.id.toLong()
}

// teste INSTRUMENTADO (roda num aparelho/emulador, não na JVM): valida a tela de Lista Pessoal de ponta a ponta —
// ViewModel real + fake do Service, exatamente como em produção, só trocando de onde os dados vêm.
class ListaPessoalScreenInstrumentedTest {

    // a régua do Compose: permite desenhar um Composable (setContent) e depois procurar/tocar/verificar elementos
    @get:Rule
    val composeTestRule = createComposeRule()

    // dois jogos de exemplo, ambos já "de olho"; coverUrl vazio faz o GameCover usar o placeholder (sem rede)
    private val jogos = listOf(
        Game(
            id = "2", title = "Hearthfall", releaseDate = "2026-08-15",
            platforms = listOf(Platform.PC), genres = listOf(Genre.RPG),
            developer = "Crimson Forge", synopsis = "", coverUrl = "",
            priceUsd = 69.99, priceBrl = 349.90, trailerId = null, preSaleDate = null, anticipationScore = 92
        ),
        Game(
            id = "4", title = "Neon Samurai 2", releaseDate = "2026-11-05",
            platforms = listOf(Platform.PS5), genres = listOf(Genre.ACTION),
            developer = "Ronin Interactive", synopsis = "", coverUrl = "",
            priceUsd = 69.99, priceBrl = 349.90, trailerId = null, preSaleDate = null, anticipationScore = 95
        )
    )

    // monta a tela real (ViewModel real + fake do Service) dentro do tema do app, já com os dois jogos "de olho"
    private fun montarTela(onJogoClick: (String) -> Unit = {}) {
        val viewModel = ListaPessoalViewModel(FakeGameService(jogos, watchedIniciais = setOf("2", "4")))
        composeTestRule.setContent {
            GameCountdownTheme {
                ListaPessoalScreen(viewModel = viewModel, onJogoClick = onJogoClick)
            }
        }
    }

    // ao abrir a tela, a lista inicial deve mostrar os cards dos dois jogos "de olho", com título e countdown corretos
    @Test
    fun listaPessoalScreen_exibe_a_lista_de_jogos_observados() {
        montarTela()

        composeTestRule.onNodeWithText("Hearthfall").assertIsDisplayed()
        composeTestRule.onNodeWithText("faltam 2 dias").assertIsDisplayed() // fake devolve dias=2 para o jogo id="2"
        composeTestRule.onNodeWithText("Neon Samurai 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("faltam 4 dias").assertIsDisplayed() // fake devolve dias=4 para o jogo id="4"
    }

    // tocar no botão de alternância (canto superior direito) troca a lista pela grade de calendário
    @Test
    fun listaPessoalScreen_alternar_para_grade_exibe_o_calendario() {
        montarTela()

        // antes de tocar, estamos na lista: o botão oferece ir para o calendário
        composeTestRule.onNodeWithContentDescription("Ver em calendário").performClick()

        // agora a grade do Calendario está desenhada (cabeçalho com as setas de mês é exclusivo dela)
        composeTestRule.onNodeWithContentDescription("Mês anterior").assertIsDisplayed()
        // e o botão trocou de rótulo, oferecendo voltar pra lista
        composeTestRule.onNodeWithContentDescription("Ver em lista").assertIsDisplayed()
    }

    // tocar na lixeira do card remove o jogo da lista pessoal de verdade (via ViewModel real) e ele some da tela
    @Test
    fun listaPessoalScreen_tocar_na_lixeira_remove_o_jogo_da_lista() {
        montarTela()

        // os dois cards têm o botão de lixeira; clicamos no primeiro, que corresponde ao Hearthfall (topo da lista)
        val botoesRemover = composeTestRule.onAllNodesWithContentDescription("Remover da lista")
        botoesRemover[0].assertIsDisplayed()
        botoesRemover[0].performClick()

        // após o toque, o Hearthfall não está mais na lista observada; o Neon Samurai 2 continua
        composeTestRule.onNodeWithText("Hearthfall").assertDoesNotExist()
        composeTestRule.onNodeWithText("Neon Samurai 2").assertIsDisplayed()
    }

    // tocar no card (fora da lixeira) dispara onJogoClick com o id do jogo tocado, para a navegação a Detalhes
    @Test
    fun listaPessoalScreen_tocar_no_card_dispara_onJogoClick_com_o_id_certo() {
        var idClicado: String? = null

        montarTela(onJogoClick = { id -> idClicado = id })

        composeTestRule.onNodeWithText("Neon Samurai 2").performClick()

        assertEquals("4", idClicado)
    }
}
