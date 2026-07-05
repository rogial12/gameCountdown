package com.almenara.gamecountdown.ui.detalhes // mesmo pacote da tela testada

import androidx.compose.ui.test.assertIsDisplayed // verifica se um elemento está visível na tela
import androidx.compose.ui.test.assertIsOff // verifica que um controle (o Switch) está desligado
import androidx.compose.ui.test.assertIsOn // verifica que um controle (o Switch) está ligado
import androidx.compose.ui.test.isToggleable // localiza o único elemento "alternável" da tela (o Switch de olho)
import androidx.compose.ui.test.junit4.createComposeRule // cria a "régua" que desenha e inspeciona Composables no teste
import androidx.compose.ui.test.onAllNodesWithText // encontra TODOS os elementos com o mesmo texto (título aparece 2x)
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
import org.junit.Assert.assertTrue // verifica se uma condição é verdadeira
import org.junit.Rule // marca a régua de teste do Compose como regra do JUnit
import org.junit.Test // marca um método como caso de teste

// fake do GameService só para este teste — mesmo papel do FakeGameService das outras telas instrumentadas
// (Catálogo e Lista Pessoal); versão enxuta com o mínimo pra DetalhesViewModel funcionar de ponta a ponta.
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

    // dias derivados do id do jogo (id "5" -> 5 dias), só pra ter um valor previsível no countdown exibido
    override fun getDaysUntilRelease(game: Game): Long = game.id.toLong()
}

// teste INSTRUMENTADO (roda num aparelho/emulador, não na JVM): valida a tela de Detalhes de ponta a ponta —
// ViewModel real + fake do Service, exatamente como em produção, só trocando de onde os dados vêm.
class DetalhesScreenInstrumentedTest {

    // a régua do Compose: permite desenhar um Composable (setContent) e depois procurar/tocar/verificar elementos
    @get:Rule
    val composeTestRule = createComposeRule()

    // jogo de exemplo; coverUrl e screenshotUrls vazios evitam qualquer carregamento de imagem via rede;
    // trailerId nulo faz o carrossel de mídia não aparecer, o que também evita testar a saída para o YouTube
    // (abrir um app externo não é algo que um teste instrumentado deva disparar)
    private val jogo = Game(
        id = "5", title = "Iron Protocol", releaseDate = "2026-07-10",
        platforms = listOf(Platform.PS5), genres = listOf(Genre.ACTION),
        developer = "Apex Studios", synopsis = "Thriller de ação num mundo pós-colapso.", coverUrl = "",
        priceUsd = 59.99, priceBrl = 299.90, trailerId = null, preSaleDate = null, anticipationScore = 87
    )

    // monta a tela real (ViewModel real + fake do Service) dentro do tema do app, para o id de jogo pedido
    private fun montarTela(gameId: String, onVoltar: () -> Unit = {}) {
        val viewModel = DetalhesViewModel(FakeGameService(listOf(jogo)), gameId = gameId)
        composeTestRule.setContent {
            GameCountdownTheme {
                DetalhesScreen(viewModel = viewModel, onVoltar = onVoltar)
            }
        }
    }

    // ao abrir com um id existente, a tela exibe todas as informações do jogo: título, countdown,
    // desenvolvedor, data formatada e preço
    @Test
    fun detalhesScreen_exibe_o_jogo_encontrado() {
        montarTela(gameId = "5")

        // o título aparece 2x (barra de topo + corpo da tela) -> onAllNodesWithText, não onNodeWithText (exige exatamente 1)
        composeTestRule.onAllNodesWithText("Iron Protocol")[0].assertIsDisplayed()
        composeTestRule.onNodeWithText("faltam 5 dias").assertIsDisplayed()   // countdown; fake devolve dias=5 para o id="5"
        composeTestRule.onNodeWithText("Apex Studios").assertIsDisplayed()    // desenvolvedor
        composeTestRule.onNodeWithText("10/07/2026").assertIsDisplayed()     // releaseDate formatada de "2026-07-10"
        composeTestRule.onNodeWithText("R$ 299,90").assertIsDisplayed()      // preço em BRL formatado
    }

    // ao abrir com um id que não existe no catálogo, a tela mostra a mensagem de "não encontrado" em vez de quebrar
    @Test
    fun detalhesScreen_id_inexistente_exibe_nao_encontrado() {
        montarTela(gameId = "id-que-nao-existe")

        composeTestRule.onNodeWithText("Jogo não encontrado").assertIsDisplayed()
    }

    // tocar no botão de voltar (canto superior esquerdo da barra) dispara o callback onVoltar
    @Test
    fun detalhesScreen_tocar_em_voltar_dispara_onVoltar() {
        var voltou = false // vira true quando o onVoltar é chamado

        montarTela(gameId = "5", onVoltar = { voltou = true })

        composeTestRule.onNodeWithContentDescription("Voltar").performClick()
        assertTrue("o onVoltar deveria ter sido chamado", voltou)
    }

    // tocar no switch "De olho" alterna o watched de verdade (via ViewModel real) e o switch reflete o novo estado
    @Test
    fun detalhesScreen_tocar_no_switch_de_olho_alterna_o_watched() {
        montarTela(gameId = "5")

        // o jogo começa fora da lista pessoal (fake sem watchedIds iniciais) -> o único switch da tela está desligado
        composeTestRule.onNode(isToggleable()).assertIsOff()

        composeTestRule.onNode(isToggleable()).performClick()

        // após o toque, o ViewModel chamou setWatched(true) e recarregou o estado -> o switch aparece ligado
        composeTestRule.onNode(isToggleable()).assertIsOn()
    }
}
