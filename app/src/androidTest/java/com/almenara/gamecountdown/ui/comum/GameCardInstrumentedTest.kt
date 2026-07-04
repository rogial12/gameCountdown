package com.almenara.gamecountdown.ui.comum // mesmo pacote do componente testado

import androidx.compose.ui.test.assertIsDisplayed // verifica se um elemento está visível na tela
import androidx.compose.ui.test.junit4.createComposeRule // cria a "régua" que desenha e inspeciona Composables no teste
import androidx.compose.ui.test.onNodeWithText // encontra um elemento pelo texto exibido
import androidx.compose.ui.test.performClick // simula um toque num elemento
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.ui.theme.GameCountdownTheme // tema do app (para o card renderizar como em produção)
import org.junit.Assert.assertTrue // verifica se uma condição é verdadeira
import org.junit.Rule // marca a régua de teste do Compose como regra do JUnit
import org.junit.Test // marca um método como caso de teste

// teste INSTRUMENTADO (roda num aparelho/emulador, não na JVM): verifica a UI desenhada do GameCard.
// diferente dos testes unitários, aqui checamos o que aparece na tela e o que acontece ao tocar.
class GameCardInstrumentedTest {

    // a régua do Compose: permite desenhar um Composable (setContent) e depois procurar/tocar/verificar elementos
    @get:Rule
    val composeTestRule = createComposeRule()

    // jogo de exemplo; coverUrl vazio faz o GameCover usar o placeholder da inicial (sem baixar imagem da rede)
    private val jogoExemplo = Game(
        id = "1", title = "Iron Protocol", releaseDate = "2026-07-10",
        platforms = listOf(Platform.PS5), genres = listOf(Genre.ACTION),
        developer = "Apex Studios", synopsis = "", coverUrl = "",
        priceUsd = 59.99, priceBrl = 299.90, trailerId = null, preSaleDate = null, anticipationScore = 87
    )

    // verifica que o card mostra na tela o título, a plataforma, o preço e o countdown corretos
    @Test
    fun gameCard_exibe_titulo_plataforma_preco_e_countdown() {
        // desenha o GameCard dentro do tema do app
        composeTestRule.setContent {
            GameCountdownTheme {
                GameCard(game = jogoExemplo, dias = 5L, onClick = {})
            }
        }

        // cada asserção procura o texto na tela e confirma que está visível
        composeTestRule.onNodeWithText("Iron Protocol").assertIsDisplayed()   // título
        composeTestRule.onNodeWithText("PlayStation 5").assertIsDisplayed()   // plataforma (displayName)
        composeTestRule.onNodeWithText("R$ 299,90").assertIsDisplayed()       // preço em BRL formatado
        composeTestRule.onNodeWithText("faltam 5 dias").assertIsDisplayed()   // countdown para dias = 5
    }

    // verifica que tocar no card dispara o callback onClick
    @Test
    fun gameCard_ao_tocar_dispara_onClick() {
        var clicado = false // vira true quando o onClick é chamado

        composeTestRule.setContent {
            GameCountdownTheme {
                GameCard(game = jogoExemplo, dias = 5L, onClick = { clicado = true })
            }
        }

        // toca no card (localizando-o pelo título) e confirma que o callback foi acionado
        composeTestRule.onNodeWithText("Iron Protocol").performClick()
        assertTrue("o onClick do card deveria ter sido chamado", clicado)
    }
}
