package com.almenara.gamecountdown.ui.comum // mesmo pacote da função testada, para acessar a visibilidade 'internal'

import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Test // marca um método como caso de teste

// testes da lógica do countdown (função calcularCountdown)
// aqui NÃO se testa o desenho da tela (isso exigiria teste instrumentado com ComposeTestRule),
// só a regra pura: qual texto e qual estado visual correspondem a cada quantidade de dias.
// os testes focam nos LIMITES de cada faixa, onde erros de "≤ vs <" costumam se esconder.
class CountdownBadgeTest {

    // dia do lançamento (0 dias): texto especial e estado iminente
    @Test
    fun `zero dias lanca hoje e e iminente`() {
        val info = calcularCountdown(0L)
        assertEquals("LANÇA HOJE", info.texto)
        assertEquals(CountdownEstado.IMINENTE, info.estado)
    }

    // véspera (1 dia): texto especial e estado iminente
    @Test
    fun `um dia lanca amanha e e iminente`() {
        val info = calcularCountdown(1L)
        assertEquals("LANÇA AMANHÃ", info.texto)
        assertEquals(CountdownEstado.IMINENTE, info.estado)
    }

    // 2 dias: início da faixa "faltam N dias" que ainda é iminente
    @Test
    fun `dois dias mostra contagem e ainda e iminente`() {
        val info = calcularCountdown(2L)
        assertEquals("faltam 2 dias", info.texto)
        assertEquals(CountdownEstado.IMINENTE, info.estado)
    }

    // 7 dias: limite superior INCLUSIVO do estado iminente (ainda é iminente)
    @Test
    fun `sete dias e o limite inclusivo do iminente`() {
        val info = calcularCountdown(7L)
        assertEquals("faltam 7 dias", info.texto)
        assertEquals(CountdownEstado.IMINENTE, info.estado)
    }

    // 8 dias: primeiro dia FORA do iminente — vira estado normal (testa o limite pela borda de cima)
    @Test
    fun `oito dias ja e normal`() {
        val info = calcularCountdown(8L)
        assertEquals("faltam 8 dias", info.texto)
        assertEquals(CountdownEstado.NORMAL, info.estado)
    }

    // lançamento distante: continua normal, com a contagem de dias correta
    @Test
    fun `muitos dias e normal`() {
        val info = calcularCountdown(42L)
        assertEquals("faltam 42 dias", info.texto)
        assertEquals(CountdownEstado.NORMAL, info.estado)
    }

    // data no passado (dias negativos): jogo já lançado, estado neutro "Disponível"
    @Test
    fun `dias negativos indicam jogo ja disponivel`() {
        val info = calcularCountdown(-3L)
        assertEquals("Disponível", info.texto)
        assertEquals(CountdownEstado.DISPONIVEL, info.estado)
    }
}
