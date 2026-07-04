package com.almenara.gamecountdown.ui.comum // pacote dos componentes visuais compartilhados entre telas

import androidx.compose.foundation.layout.padding // modifier para dar espaçamento interno ao redor do texto
import androidx.compose.material3.MaterialTheme // acesso ao tema atual (cores, tipografia, formas do Material 3)
import androidx.compose.material3.Surface // "caixa" do Material 3: dá cor de fundo, forma e elevação a um conteúdo
import androidx.compose.material3.Text // componente que desenha um texto na tela
import androidx.compose.runtime.Composable // anotação que marca uma função como componente de UI do Compose
import androidx.compose.ui.Modifier // objeto que descreve ajustes de layout/aparência, repassado de fora pra dentro
import androidx.compose.ui.tooling.preview.Preview // permite visualizar o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de medida de distância independente de densidade de tela
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas; a fonte do texto exibido no badge

// Composable = função que descreve um pedaço de UI. Este é o PlatformBadge: um "chip" que mostra o nome de uma plataforma.
// Ele é puro: recebe uma Platform e só a desenha — não busca dados, não guarda estado, não decide nada de negócio.
@Composable
fun PlatformBadge(
    platform: Platform,             // a plataforma a exibir; o componente lê o displayName dela (ex.: "PlayStation 5")
    modifier: Modifier = Modifier   // ajustes vindos de quem chama (ex.: espaçamento externo); padrão = nenhum ajuste
) {
    // Surface desenha o fundo arredondado e colorido do chip; o Text vai dentro dela
    Surface(
        modifier = modifier,                                     // repassa os ajustes externos recebidos
        shape = MaterialTheme.shapes.small,                      // cantos levemente arredondados, vindos do tema
        color = MaterialTheme.colorScheme.secondaryContainer     // cor de fundo do chip, definida pelo tema (contraste suave)
    ) {
        Text(
            text = platform.displayName,                         // o texto exibido é o nome legível da plataforma
            style = MaterialTheme.typography.labelSmall,         // estilo tipográfico pequeno, adequado a um rótulo/chip
            color = MaterialTheme.colorScheme.onSecondaryContainer, // cor do texto que contrasta com o fundo do chip
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp) // espaço interno entre o texto e a borda do chip
        )
    }
}

// @Preview: renderiza o componente isolado no painel de preview do Android Studio, sem precisar rodar o app inteiro
// serve só para desenvolvimento/visualização — não faz parte do app final
@Preview
@Composable
private fun PlatformBadgePreview() {
    PlatformBadge(platform = Platform.PS5) // exemplo com uma plataforma qualquer só para ver o resultado
}
