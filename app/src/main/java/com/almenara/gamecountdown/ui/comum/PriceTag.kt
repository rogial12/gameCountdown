package com.almenara.gamecountdown.ui.comum // pacote dos componentes visuais compartilhados entre telas

import androidx.compose.foundation.layout.padding // modifier para dar espaçamento interno ao redor do texto
import androidx.compose.material3.MaterialTheme // acesso ao tema atual (cores, tipografia, formas do Material 3)
import androidx.compose.material3.Surface // "caixa" do Material 3: dá cor de fundo, forma e elevação a um conteúdo
import androidx.compose.material3.Text // componente que desenha um texto na tela
import androidx.compose.runtime.Composable // anotação que marca uma função como componente de UI do Compose
import androidx.compose.ui.Modifier // objeto que descreve ajustes de layout/aparência, repassado de fora pra dentro
import androidx.compose.ui.tooling.preview.Preview // permite visualizar o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de medida de distância independente de densidade de tela
import java.util.Locale // define a formatação numérica por região (separador decimal, agrupamento de milhar)

// função de apoio: decide QUAL texto de preço mostrar, seguindo a regra de negócio decidida:
// 1) se houver preço em BRL, mostra em reais (moeda do usuário brasileiro em destaque);
// 2) senão, se houver preço em USD, mostra em dólar como fallback;
// 3) se não houver nenhum dos dois, mostra "Preço não anunciado".
// fica fora do @Composable porque é lógica pura (dado -> texto), não desenho de tela — mais fácil de ler e raciocinar.
// é 'internal' (visível dentro do módulo do app, incluindo os testes) em vez de 'private' para poder ser testada
// por um teste unitário comum, sem precisar de emulador — diferente do desenho da tela, que exigiria teste instrumentado.
internal fun formatarPreco(priceUsd: Double?, priceBrl: Double?): String = when {
    // Locale.forLanguageTag("pt-BR") faz o %,.2f usar vírgula como separador decimal e ponto no milhar (ex.: 1.349,90)
    priceBrl != null -> String.format(Locale.forLanguageTag("pt-BR"), "R$ %,.2f", priceBrl)
    // Locale.US faz o %,.2f usar ponto como separador decimal (ex.: 1,349.99) — formato usual do dólar
    priceUsd != null -> String.format(Locale.US, "US$ %,.2f", priceUsd)
    // nenhum preço disponível: texto neutro indicando que o valor ainda será anunciado
    else -> "Preço não anunciado"
}

// Composable = função que descreve um pedaço de UI. Este é o PriceTag: um "chip" que mostra o preço de um jogo.
// Recebe os dois preços possíveis (ambos podem ser null) e delega a decisão de exibição à função formatarPreco acima.
@Composable
fun PriceTag(
    priceUsd: Double?,              // preço em dólar; null quando não anunciado
    priceBrl: Double?,              // preço em real; null quando não disponível em BRL
    modifier: Modifier = Modifier   // ajustes vindos de quem chama (ex.: espaçamento externo); padrão = nenhum ajuste
) {
    // calcula o texto uma única vez, antes de desenhar, aplicando a regra de prioridade BRL -> USD -> "não anunciado"
    val textoPreco = formatarPreco(priceUsd, priceBrl)

    // Surface desenha o fundo arredondado e colorido do chip; o Text vai dentro dela
    Surface(
        modifier = modifier,                                 // repassa os ajustes externos recebidos
        shape = MaterialTheme.shapes.small,                  // cantos levemente arredondados, vindos do tema
        color = MaterialTheme.colorScheme.tertiaryContainer  // cor de fundo do chip de preço, distinta da do PlatformBadge
    ) {
        Text(
            text = textoPreco,                                   // texto já formatado pela regra de negócio
            style = MaterialTheme.typography.labelMedium,        // estilo tipográfico de rótulo, um pouco maior que o do badge de plataforma
            color = MaterialTheme.colorScheme.onTertiaryContainer, // cor do texto que contrasta com o fundo do chip
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp) // espaço interno entre o texto e a borda do chip
        )
    }
}

// @Preview: renderiza o componente isolado no painel de preview do Android Studio, sem rodar o app inteiro.
// três previews cobrem os três casos da regra: com BRL, só com USD, e sem preço nenhum.
@Preview
@Composable
private fun PriceTagComBrlPreview() {
    PriceTag(priceUsd = 69.99, priceBrl = 349.90) // caso 1: tem BRL -> mostra "R$ 349,90"
}

@Preview
@Composable
private fun PriceTagSoUsdPreview() {
    PriceTag(priceUsd = 39.99, priceBrl = null) // caso 2: só USD -> mostra "US$ 39.99"
}

@Preview
@Composable
private fun PriceTagSemPrecoPreview() {
    PriceTag(priceUsd = null, priceBrl = null) // caso 3: nenhum preço -> mostra "Preço não anunciado"
}
