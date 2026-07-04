package com.almenara.gamecountdown.ui.comum // mesmo pacote da função testada, para acessar a visibilidade 'internal'

import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Test // marca um método como caso de teste

// testes da lógica de apoio do GameCard (função inicialDoTitulo)
// não testa o desenho do card (isso exigiria teste instrumentado), só a extração da inicial da capa placeholder
class GameCardTest {

    // caso comum: retorna a primeira letra em maiúscula
    @Test
    fun `titulo normal retorna a primeira letra em maiuscula`() {
        assertEquals("I", inicialDoTitulo("Iron Protocol"))
    }

    // título já em maiúscula continua correto (não altera nada além do que precisa)
    @Test
    fun `titulo minusculo e convertido para maiuscula`() {
        assertEquals("H", inicialDoTitulo("hearthfall"))
    }

    // título com espaços à esquerda: ignora o espaço e pega a primeira letra de verdade
    @Test
    fun `espacos a esquerda sao ignorados`() {
        assertEquals("V", inicialDoTitulo("   Verdant Rift"))
    }

    // caso de borda: título vazio não deve quebrar — retorna "?" como marcador neutro
    @Test
    fun `titulo vazio retorna interrogacao`() {
        assertEquals("?", inicialDoTitulo(""))
    }

    // caso de borda: título só com espaços também retorna "?" (não há inicial de verdade)
    @Test
    fun `titulo so com espacos retorna interrogacao`() {
        assertEquals("?", inicialDoTitulo("   "))
    }
}
