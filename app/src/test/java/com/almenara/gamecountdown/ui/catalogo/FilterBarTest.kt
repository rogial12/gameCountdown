package com.almenara.gamecountdown.ui.catalogo // mesmo pacote das funções testadas, para acessar a visibilidade 'internal'

import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação, vem do Service
import com.almenara.gamecountdown.data.service.PeriodoLancamento // enum de janelas de lançamento, vem do Service
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertTrue // verifica se uma condição é verdadeira
import org.junit.Test // marca um método como caso de teste

// testes dos rótulos amigáveis da FilterBar (rotuloPeriodo e rotuloOrdenacao)
// não testam o desenho da barra (isso exigiria teste instrumentado), só a lógica pura de mapeamento enum -> texto.
// o objetivo é garantir que nenhum valor fique sem rótulo, com rótulo vazio, ou com rótulo repetido de outro valor.
class FilterBarTest {

    // cada período deve ter um rótulo não-vazio (nenhum valor esquecido ou em branco)
    @Test
    fun `todo periodo tem rotulo nao vazio`() {
        PeriodoLancamento.entries.forEach { periodo ->
            assertTrue("Período $periodo com rótulo vazio", rotuloPeriodo(periodo).isNotBlank())
        }
    }

    // os rótulos de período devem ser todos distintos entre si (nenhum copiado por engano)
    @Test
    fun `rotulos de periodo sao todos distintos`() {
        val rotulos = PeriodoLancamento.entries.map { rotuloPeriodo(it) }
        assertEquals("Há rótulos de período repetidos", rotulos.size, rotulos.distinct().size)
    }

    // cada critério de ordenação deve ter um rótulo não-vazio
    @Test
    fun `toda ordenacao tem rotulo nao vazio`() {
        CriterioOrdenacao.entries.forEach { ordenacao ->
            assertTrue("Ordenação $ordenacao com rótulo vazio", rotuloOrdenacao(ordenacao).isNotBlank())
        }
    }

    // os rótulos de ordenação devem ser todos distintos entre si
    @Test
    fun `rotulos de ordenacao sao todos distintos`() {
        val rotulos = CriterioOrdenacao.entries.map { rotuloOrdenacao(it) }
        assertEquals("Há rótulos de ordenação repetidos", rotulos.size, rotulos.distinct().size)
    }
}
