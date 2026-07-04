package com.almenara.gamecountdown.ui.comum // mesmo pacote da função testada, para acessar a visibilidade 'internal'

import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Test // marca um método como caso de teste

// testes da lógica de formatação de preço do PriceTag (função formatarPreco)
// aqui NÃO se testa o desenho da tela (isso exigiria teste instrumentado com ComposeTestRule),
// só a regra pura de negócio: qual moeda tem prioridade e como o número é formatado
class PriceTagTest {

    // caso 1 da regra: havendo preço em BRL, ele tem prioridade e é exibido em reais,
    // mesmo que também exista preço em USD (BRL em destaque para o usuário brasileiro)
    @Test
    fun `com BRL e USD mostra o preco em reais`() {
        val resultado = formatarPreco(priceUsd = 69.99, priceBrl = 349.90)
        assertEquals("R$ 349,90", resultado) // vírgula como separador decimal, padrão do pt-BR
    }

    // ainda no caso 1: mesmo sem USD, se houver BRL, mostra em reais
    @Test
    fun `apenas com BRL mostra o preco em reais`() {
        val resultado = formatarPreco(priceUsd = null, priceBrl = 199.90)
        assertEquals("R$ 199,90", resultado)
    }

    // caso 2 da regra: sem BRL, cai no fallback e exibe o preço em dólar
    @Test
    fun `apenas com USD mostra o preco em dolar`() {
        val resultado = formatarPreco(priceUsd = 39.99, priceBrl = null)
        assertEquals("US$ 39.99", resultado) // ponto como separador decimal, padrão do dólar
    }

    // caso 3 da regra: sem nenhum dos dois preços, mostra o texto neutro de "não anunciado"
    @Test
    fun `sem nenhum preco mostra texto de nao anunciado`() {
        val resultado = formatarPreco(priceUsd = null, priceBrl = null)
        assertEquals("Preço não anunciado", resultado)
    }

    // verifica o separador de milhar: em pt-BR o milhar usa ponto e o decimal usa vírgula (ex.: 1.349,90)
    @Test
    fun `preco em BRL acima de mil usa ponto no milhar e virgula no decimal`() {
        val resultado = formatarPreco(priceUsd = null, priceBrl = 1349.90)
        assertEquals("R$ 1.349,90", resultado)
    }

    // verifica o separador de milhar no dólar: em US o milhar usa vírgula e o decimal usa ponto (ex.: 1,349.99)
    @Test
    fun `preco em USD acima de mil usa virgula no milhar e ponto no decimal`() {
        val resultado = formatarPreco(priceUsd = 1349.99, priceBrl = null)
        assertEquals("US$ 1,349.99", resultado)
    }
}
