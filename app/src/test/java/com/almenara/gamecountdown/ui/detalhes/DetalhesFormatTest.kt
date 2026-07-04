package com.almenara.gamecountdown.ui.detalhes // mesmo pacote da função testada, para acessar a visibilidade 'internal'

import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Test // marca um método como caso de teste

// testes da função pura de formatação de data (formatarData) usada na tela de Detalhes.
// não testa o desenho da tela (isso exigiria teste instrumentado), só a conversão texto -> texto.
class DetalhesFormatTest {

    // caso normal: data ISO vira o formato brasileiro dia/mês/ano
    @Test
    fun `data ISO vira formato brasileiro`() {
        assertEquals("10/07/2026", formatarData("2026-07-10"))
    }

    // mantém os zeros à esquerda de dia e mês (não vira "1/7/2026")
    @Test
    fun `mantem zeros a esquerda de dia e mes`() {
        assertEquals("05/01/2027", formatarData("2027-01-05"))
    }

    // entrada fora do formato esperado (sem os dois hifens) é devolvida como veio, sem quebrar
    @Test
    fun `entrada malformada retorna o texto original`() {
        assertEquals("data-invalida", formatarData("data-invalida"))
    }

    // string vazia também é devolvida como está
    @Test
    fun `entrada vazia retorna vazio`() {
        assertEquals("", formatarData(""))
    }
}
