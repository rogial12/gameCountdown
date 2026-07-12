package com.almenara.gamecountdown.ui.busca // mesmo pacote da tela testada

import androidx.compose.ui.test.assertIsDisplayed // verifica se um elemento está visível na tela
import androidx.compose.ui.test.junit4.createComposeRule // cria a "régua" que desenha e inspeciona Composables no teste
import androidx.compose.ui.test.onNodeWithText // encontra um elemento pelo texto exibido
import androidx.compose.ui.test.performClick // simula um toque num elemento
import androidx.compose.ui.test.performTextInput // simula a digitação de texto num campo
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.data.repository.HistoricoBusca // entrada crua do histórico (query + gameId)
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação, parte do contrato do GameService
import com.almenara.gamecountdown.data.service.FiltroCatalogo // agrupador de filtros, parte do contrato do GameService
import com.almenara.gamecountdown.data.service.GameService // contrato que o fake abaixo implementa
import com.almenara.gamecountdown.data.service.SearchHistoryService // contrato do outro fake abaixo
import com.almenara.gamecountdown.ui.theme.GameCountdownTheme // tema do app (para a tela renderizar como em produção)
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Rule // marca a régua de teste do Compose como regra do JUnit
import org.junit.Test // marca um método como caso de teste

// fake do GameService só para este teste — não dá pra reaproveitar o fake de src/test (source set diferente).
// a Busca não exibe estado "de olho" na tela, então setWatched/observarMudancasWatched ficam sem efeito real.
private class FakeGameService(private val jogosBase: List<Game>) : GameService {

    override fun getGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> = jogosBase

    override fun getGameById(id: String): Game? = jogosBase.find { it.id == id }

    override fun searchGames(query: String): List<Game> =
        jogosBase.filter { it.title.contains(query, ignoreCase = true) }

    override fun getWatchedGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> = emptyList()

    override fun setWatched(id: String, watched: Boolean) {} // não usado pela tela de Busca

    override fun observarMudancasWatched(callback: () -> Unit): () -> Unit = {} // não usado pela tela de Busca

    // dias derivados do id do jogo (id "1" -> 1 dia), só pra ter um valor previsível no countdown exibido
    override fun getDaysUntilRelease(game: Game): Long = game.id.toLong()
}

// fake do SearchHistoryService só para este teste — guarda o histórico em memória, com a mesma regra de
// "sem duplicata por jogo, mais recente no topo" (simplificada); a regra de negócio de verdade já é
// testada em SearchHistoryServiceImplTest, aqui só precisa ser fiel o bastante pra tela reagir de ponta a ponta.
private class FakeSearchHistoryService(historicoInicial: List<HistoricoBusca> = emptyList()) : SearchHistoryService {
    private var historico: List<HistoricoBusca> = historicoInicial

    override fun getHistorico(): List<HistoricoBusca> = historico

    override fun adicionar(query: String, gameId: String) {
        if (query.isBlank()) return
        historico = listOf(HistoricoBusca(query, gameId)) + historico.filterNot { it.gameId == gameId }
    }

    override fun limpar() {
        historico = emptyList()
    }
}

// teste INSTRUMENTADO (roda num aparelho/emulador, não na JVM): valida a tela de Busca de ponta a ponta —
// ViewModel real + fakes dos dois Services, exatamente como em produção, só trocando de onde os dados vêm.
class BuscaScreenInstrumentedTest {

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

    // monta a tela real (ViewModel real + fakes dos Services) dentro do tema do app; 'historicoInicial'
    // permite testar a tela já abrindo com entradas salvas, sem precisar simular buscas anteriores
    private fun montarTela(
        historicoInicial: List<HistoricoBusca> = emptyList(),
        onJogoClick: (String) -> Unit = {}
    ) {
        val viewModel = BuscaViewModel(FakeGameService(jogos), FakeSearchHistoryService(historicoInicial))
        composeTestRule.setContent {
            GameCountdownTheme {
                BuscaScreen(viewModel = viewModel, onJogoClick = onJogoClick)
            }
        }
    }

    // ao abrir a tela sem nenhum histórico salvo e sem nada digitado, mostra a dica inicial
    @Test
    fun buscaScreen_campo_vazio_sem_historico_exibe_dica() {
        montarTela()

        composeTestRule.onNodeWithText("Busque um jogo pelo título").assertIsDisplayed()
    }

    // digitar um termo que casa com o título de um jogo mostra o resultado (card com título + countdown)
    @Test
    fun buscaScreen_digitar_termo_exibe_resultado_correspondente() {
        montarTela()

        // localiza o campo pelo placeholder (só ele exibe esse texto enquanto o campo está vazio) e digita
        composeTestRule.onNodeWithText("Buscar jogo...").performTextInput("iron")

        composeTestRule.onNodeWithText("Iron Protocol").assertIsDisplayed()
        composeTestRule.onNodeWithText("LANÇA AMANHÃ").assertIsDisplayed() // fake devolve dias=1 para o id="1"
    }

    // digitar um termo sem correspondência mostra "nenhum resultado" em vez de uma lista vazia sem explicação
    @Test
    fun buscaScreen_digitar_termo_sem_correspondencia_exibe_nenhum_resultado() {
        montarTela()

        composeTestRule.onNodeWithText("Buscar jogo...").performTextInput("zzz")

        composeTestRule.onNodeWithText("Nenhum jogo encontrado").assertIsDisplayed()
    }

    // tocar num resultado da busca dispara onJogoClick com o id do jogo tocado, para a navegação a Detalhes
    @Test
    fun buscaScreen_tocar_no_resultado_dispara_onJogoClick_com_o_id_certo() {
        var idClicado: String? = null

        montarTela(onJogoClick = { id -> idClicado = id })

        composeTestRule.onNodeWithText("Buscar jogo...").performTextInput("hearth")
        composeTestRule.onNodeWithText("Hearthfall").performClick()

        assertEquals("2", idClicado)
    }

    // ao abrir com histórico já salvo (campo vazio), a tela mostra a tile do jogo salvo sob "Buscas recentes";
    // tocar em "Limpar" apaga o histórico de ponta a ponta (ViewModel real -> Service) e volta pra dica inicial
    @Test
    fun buscaScreen_historico_exibe_jogo_salvo_e_limpar_esvazia() {
        montarTela(historicoInicial = listOf(HistoricoBusca(query = "iron", gameId = "1")))

        composeTestRule.onNodeWithText("Buscas recentes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Iron Protocol").assertIsDisplayed()

        composeTestRule.onNodeWithText("Limpar").performClick()

        composeTestRule.onNodeWithText("Busque um jogo pelo título").assertIsDisplayed()
    }
}
