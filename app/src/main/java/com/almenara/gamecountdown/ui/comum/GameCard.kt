package com.almenara.gamecountdown.ui.comum // pacote dos componentes visuais compartilhados entre telas

import androidx.compose.foundation.background // modifier que pinta um fundo colorido atrás de um conteúdo
import androidx.compose.foundation.layout.Arrangement // define o espaçamento entre filhos de Row/Column
import androidx.compose.foundation.layout.Box // container que empilha/centraliza um conteúdo (usado na capa placeholder)
import androidx.compose.foundation.layout.Column // organiza filhos verticalmente (título, plataformas, preço/countdown)
import androidx.compose.foundation.layout.FlowRow // como Row, mas quebra para a linha de baixo quando não cabe (plataformas)
import androidx.compose.foundation.layout.Row // organiza filhos horizontalmente (capa à esquerda, infos à direita)
import androidx.compose.foundation.layout.fillMaxWidth // faz o componente ocupar toda a largura disponível
import androidx.compose.foundation.layout.padding // aplica espaçamento ao redor de um conteúdo
import androidx.compose.foundation.layout.size // define largura e altura fixas de um componente
import androidx.compose.material3.Card // "cartão" do Material 3: superfície elevada e clicável que agrupa o conteúdo
import androidx.compose.material3.MaterialTheme // acesso ao tema atual (cores, tipografia, formas do Material 3)
import androidx.compose.material3.Text // componente que desenha um texto na tela
import androidx.compose.runtime.Composable // anotação que marca uma função como componente de UI do Compose
import androidx.compose.ui.Alignment // define alinhamento (ex.: centralizar a inicial na capa)
import androidx.compose.ui.Modifier // objeto que descreve ajustes de layout/aparência, repassado de fora pra dentro
import androidx.compose.ui.draw.clip // recorta um conteúdo no formato de uma forma (cantos arredondados da capa)
import androidx.compose.ui.text.font.FontWeight // controla o peso (negrito) do texto
import androidx.compose.ui.text.style.TextOverflow // define o que fazer quando o texto não cabe (reticências)
import androidx.compose.ui.tooling.preview.Preview // permite visualizar o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de medida de distância independente de densidade de tela
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo — o dado que o card exibe
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros; usado só no @Preview
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas; usado para desenhar um PlatformBadge por plataforma

// função de apoio: extrai a letra inicial (maiúscula) do título, usada na capa placeholder enquanto não há imagem real.
// é 'internal' para ser testável por teste unitário comum; trata título vazio/só espaços devolvendo "?" em vez de quebrar.
internal fun inicialDoTitulo(titulo: String): String =
    titulo.trim().firstOrNull()?.uppercase() ?: "?" // pega o 1º caractere não-espaço; se não houver nenhum, usa "?"

// Composable = função que descreve um pedaço de UI. Este é o GameCard: um item de lista que representa um jogo.
// É um componente de exibição puro — recebe os dados prontos e avisa quando é tocado (onClick), sem chamar Service
// nem saber que dia é hoje. Por isso o countdown chega já calculado, como o parâmetro 'dias'.
@Composable
fun GameCard(
    game: Game,                     // o jogo a exibir (título, plataformas, preços, capa...)
    dias: Long,                     // dias até o lançamento, calculados fora (via GameService.getDaysUntilRelease)
    onClick: () -> Unit,            // função chamada quando o card é tocado (ex.: abrir os detalhes do jogo)
    modifier: Modifier = Modifier,  // ajustes vindos de quem chama (ex.: espaçamento externo); padrão = nenhum ajuste
    trailing: (@Composable () -> Unit)? = null // conteúdo opcional à direita (ex.: o switch de "de olho"); null = nada
) {
    // Card: superfície clicável que agrupa todo o conteúdo do item, com elevação e cantos arredondados do tema
    Card(
        onClick = onClick,                       // repassa o toque ao chamador
        modifier = modifier.fillMaxWidth()       // ocupa toda a largura da lista
    ) {
        // Row: dispõe a capa (esquerda) e a coluna de informações (direita) lado a lado
        Row(
            modifier = Modifier.padding(12.dp),                 // respiro interno entre a borda do card e o conteúdo
            horizontalArrangement = Arrangement.spacedBy(12.dp) // espaço fixo entre a capa e a coluna de infos
        ) {
            // Capa placeholder: caixa colorida com a inicial do título centralizada (substitui a imagem real por ora)
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 96.dp)             // proporção retrato, típica de capa de jogo
                    .clip(MaterialTheme.shapes.small)                // cantos arredondados iguais aos dos badges
                    .background(MaterialTheme.colorScheme.secondaryContainer), // cor de fundo do placeholder, vinda do tema
                contentAlignment = Alignment.Center                  // centraliza a inicial dentro da caixa
            ) {
                Text(
                    text = inicialDoTitulo(game.title),                    // a letra inicial calculada pela função de apoio
                    style = MaterialTheme.typography.headlineMedium,       // fonte grande, já que é o único conteúdo da capa
                    fontWeight = FontWeight.Bold,                          // negrito para destacar a inicial
                    color = MaterialTheme.colorScheme.onSecondaryContainer // cor que contrasta com o fundo do placeholder
                )
            }

            // Column: empilha verticalmente título, plataformas e a linha de preço/countdown
            Column(
                modifier = Modifier.weight(1f),                    // ocupa o espaço restante entre a capa e o 'trailing'
                verticalArrangement = Arrangement.spacedBy(6.dp)   // espaço fixo entre título, plataformas e preço/countdown
            ) {
                // Título do jogo — no máximo 2 linhas; se passar, corta com reticências para não quebrar o layout
                Text(
                    text = game.title,                             // o título do jogo
                    style = MaterialTheme.typography.titleMedium,  // estilo de título de item de lista
                    fontWeight = FontWeight.SemiBold,              // seminegrito para dar destaque
                    maxLines = 2,                                  // limita a 2 linhas
                    overflow = TextOverflow.Ellipsis               // "..." quando o título não cabe em 2 linhas
                )

                // FlowRow: um PlatformBadge por plataforma; quebra para a linha de baixo quando não cabem todos
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp), // espaço horizontal entre badges
                    verticalArrangement = Arrangement.spacedBy(4.dp)    // espaço vertical quando quebra de linha
                ) {
                    // para cada plataforma do jogo, desenha um chip com o nome dela
                    game.platforms.forEach { plataforma ->
                        PlatformBadge(platform = plataforma) // componente do mesmo pacote, criado no Passo 6
                    }
                }

                // Linha final: countdown à esquerda e preço à direita, lado a lado
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp) // espaço fixo entre o countdown e o preço
                ) {
                    CountdownBadge(dias = dias)                                  // chip de countdown (Passo 7)
                    PriceTag(priceUsd = game.priceUsd, priceBrl = game.priceBrl) // chip de preço (Passo 6)
                }
            }

            // conteúdo opcional à direita da coluna de infos (ex.: o AddToListSwitch da Lista Pessoal)
            // só é desenhado quando quem usa o card fornece um 'trailing'; no Catálogo fica ausente
            if (trailing != null) {
                trailing()
            }
        }
    }
}

// @Preview: renderiza o card isolado no painel do Android Studio com um jogo de exemplo, sem rodar o app.
@Preview
@Composable
private fun GameCardPreview() {
    // jogo fictício só para visualização; valores escolhidos para exercitar título, multi-plataforma e preço
    val exemplo = Game(
        id = "1",
        title = "Iron Protocol",
        releaseDate = "2026-07-10",
        platforms = listOf(Platform.XBOX_SERIES, Platform.PC),
        genres = listOf(Genre.ACTION),
        developer = "Apex Studios",
        synopsis = "",
        coverUrl = "",
        priceUsd = 59.99,
        priceBrl = 299.90,
        trailerId = null,
        preSaleDate = null,
        anticipationScore = 87
    )
    GameCard(game = exemplo, dias = 5L, onClick = {}) // dias = 5 -> countdown no estado iminente
}
