package com.almenara.gamecountdown.ui.comum // mesmo pacote da função testada, para acessar a visibilidade 'internal'

import com.almenara.gamecountdown.data.model.Game     // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre    // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertTrue   // verifica se uma condição é verdadeira
import org.junit.Test                // marca um método como caso de teste
import java.time.YearMonth           // define o mês usado nos testes

// testes da lógica pura de agrupamento do Calendário (função jogosPorDiaDoMes)
class CalendarioAgrupamentoTest {

    private val julho2026 = YearMonth.of(2026, 7) // mês de referência dos testes

    // agrupa os jogos do mês pelo dia do lançamento
    @Test
    fun `agrupa os jogos do mes por dia`() {
        val jogos = listOf(
            gameFake("1", "2026-07-10"),
            gameFake("2", "2026-07-28")
        )

        val porDia = jogosPorDiaDoMes(jogos, julho2026)

        assertEquals(setOf(10, 28), porDia.keys)                 // dois dias com lançamento
        assertEquals(listOf("1"), porDia[10]?.map { it.id })     // dia 10 tem o jogo "1"
        assertEquals(listOf("2"), porDia[28]?.map { it.id })     // dia 28 tem o jogo "2"
    }

    // jogos de outros meses não entram no resultado
    @Test
    fun `ignora jogos de outros meses`() {
        val jogos = listOf(
            gameFake("1", "2026-07-10"), // julho -> entra
            gameFake("2", "2026-08-15"), // agosto -> fora
            gameFake("3", "2027-07-10")  // julho, mas de 2027 -> fora
        )

        val porDia = jogosPorDiaDoMes(jogos, julho2026)

        assertEquals(setOf(10), porDia.keys) // só o jogo "1" de julho/2026
    }

    // no mesmo dia, a lista vem ordenada por interesse (maior anticipationScore primeiro = destaque)
    @Test
    fun `mesmo dia ordena por interesse com o destaque primeiro`() {
        val jogos = listOf(
            gameFake("baixo", "2026-07-10", anticipation = 40),
            gameFake("alto", "2026-07-10", anticipation = 95),
            gameFake("medio", "2026-07-10", anticipation = 70)
        )

        val doDia = jogosPorDiaDoMes(jogos, julho2026)[10]

        assertEquals(listOf("alto", "medio", "baixo"), doDia?.map { it.id }) // ordem decrescente de interesse
    }

    // data malformada não quebra: o jogo é simplesmente ignorado
    @Test
    fun `data malformada e ignorada`() {
        val jogos = listOf(
            gameFake("ok", "2026-07-10"),
            gameFake("ruim", "data-invalida")
        )

        val porDia = jogosPorDiaDoMes(jogos, julho2026)

        assertEquals(setOf(10), porDia.keys)                 // só o jogo com data válida
        assertEquals(listOf("ok"), porDia[10]?.map { it.id })
    }

    // mês sem nenhum lançamento devolve um mapa vazio
    @Test
    fun `mes sem jogos devolve vazio`() {
        val jogos = listOf(gameFake("1", "2026-08-15")) // agosto

        val porDia = jogosPorDiaDoMes(jogos, julho2026)

        assertTrue(porDia.isEmpty())
    }
}

// função de apoio: cria um Game fictício preenchendo só o essencial para estes testes
private fun gameFake(id: String, releaseDate: String, anticipation: Int = 0): Game = Game(
    id = id,
    title = "Jogo $id",
    releaseDate = releaseDate,
    platforms = listOf(Platform.PC),
    genres = listOf(Genre.ACTION),
    developer = "Fake Studio",
    synopsis = "",
    coverUrl = "",
    priceUsd = null,
    priceBrl = null,
    trailerId = null,
    preSaleDate = null,
    anticipationScore = anticipation
)
