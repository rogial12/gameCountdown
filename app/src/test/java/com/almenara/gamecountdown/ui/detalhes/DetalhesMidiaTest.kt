package com.almenara.gamecountdown.ui.detalhes // mesmo pacote das funções testadas, para acessar a visibilidade 'internal'

import com.almenara.gamecountdown.data.model.Game     // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre    // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertTrue   // verifica se uma condição é verdadeira
import org.junit.Test                // marca um método como caso de teste

// testes das funções puras de mídia/loja usadas na tela de Detalhes (montarMidias e lojaPara).
// não testam o desenho do carrossel (isso exigiria teste instrumentado), só a lógica pura.
class DetalhesMidiaTest {

    // jogo com trailer e imagens: o vídeo deve vir primeiro, seguido das imagens na ordem em que estão no Game
    @Test
    fun `montarMidias coloca o video primeiro e depois as imagens em ordem`() {
        val jogo = gameFake(trailerId = "abc123", screenshotUrls = listOf("img1", "img2"))

        val midias = montarMidias(jogo)

        assertEquals(
            listOf(
                Midia(TipoMidia.VIDEO, "abc123"),
                Midia(TipoMidia.IMAGEM, "img1"),
                Midia(TipoMidia.IMAGEM, "img2")
            ),
            midias
        )
    }

    // jogo sem trailer: a lista deve conter só as imagens, sem nenhum item de vídeo
    @Test
    fun `montarMidias sem trailer traz so as imagens`() {
        val jogo = gameFake(trailerId = null, screenshotUrls = listOf("img1", "img2"))

        val midias = montarMidias(jogo)

        assertEquals(listOf(Midia(TipoMidia.IMAGEM, "img1"), Midia(TipoMidia.IMAGEM, "img2")), midias)
    }

    // jogo sem imagens: a lista deve conter só o vídeo
    @Test
    fun `montarMidias sem imagens traz so o video`() {
        val jogo = gameFake(trailerId = "abc123", screenshotUrls = emptyList())

        val midias = montarMidias(jogo)

        assertEquals(listOf(Midia(TipoMidia.VIDEO, "abc123")), midias)
    }

    // jogo sem trailer nem imagens: a lista deve vir vazia, sem quebrar
    @Test
    fun `montarMidias sem trailer e sem imagens retorna lista vazia`() {
        val jogo = gameFake(trailerId = null, screenshotUrls = emptyList())

        assertTrue(montarMidias(jogo).isEmpty())
    }

    // toda plataforma deve ter uma loja associada, com nome não-vazio
    @Test
    fun `toda plataforma tem loja com nome nao vazio`() {
        Platform.entries.forEach { plataforma ->
            assertTrue("Plataforma $plataforma sem nome de loja", lojaPara(plataforma).isNotBlank())
        }
    }

    // as lojas devem ser todas distintas entre si (nenhuma plataforma copiada por engano)
    @Test
    fun `lojas sao todas distintas`() {
        val lojas = Platform.entries.map { lojaPara(it) }
        assertEquals("Há lojas repetidas entre plataformas", lojas.size, lojas.distinct().size)
    }
}

// função de apoio: cria um Game fictício preenchendo só o essencial para estes testes
private fun gameFake(trailerId: String?, screenshotUrls: List<String>): Game = Game(
    id = "1",
    title = "Jogo Fake",
    releaseDate = "2026-08-15",
    platforms = listOf(Platform.PC),
    genres = listOf(Genre.ACTION),
    developer = "Fake Studio",
    synopsis = "",
    coverUrl = "",
    priceUsd = null,
    priceBrl = null,
    trailerId = trailerId,
    screenshotUrls = screenshotUrls,
    preSaleDate = null
)
