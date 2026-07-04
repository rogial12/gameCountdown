package com.almenara.gamecountdown.ui.comum // pacote dos componentes visuais compartilhados entre telas

import androidx.compose.foundation.background // pinta o fundo colorido do placeholder da capa
import androidx.compose.foundation.layout.Box // container que centraliza a inicial no placeholder
import androidx.compose.foundation.layout.fillMaxSize // faz o placeholder preencher a área da capa
import androidx.compose.foundation.layout.size // define tamanho fixo (usado no @Preview)
import androidx.compose.material3.MaterialTheme // acesso ao tema (cores, tipografia)
import androidx.compose.material3.Text // desenha a inicial no placeholder
import androidx.compose.runtime.Composable // marca uma função como componente de UI do Compose
import androidx.compose.ui.Alignment // centraliza a inicial dentro do placeholder
import androidx.compose.ui.Modifier // descreve ajustes de layout/aparência
import androidx.compose.ui.layout.ContentScale // define como a imagem preenche o espaço (recorta mantendo proporção)
import androidx.compose.ui.text.font.FontWeight // deixa a inicial em negrito
import androidx.compose.ui.text.style.TextAlign // centraliza o texto da inicial horizontalmente
import androidx.compose.ui.tooling.preview.Preview // visualiza o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de distância independente de densidade
import coil.compose.SubcomposeAsyncImage // componente do Coil que carrega uma imagem por URL e permite slots de fallback
import coil.request.ImageRequest // configura a requisição da imagem (URL + crossfade)

// GameCover: a capa de um jogo. Se houver coverUrl, carrega a imagem pela rede (Coil);
// enquanto carrega, se falhar, ou se não houver URL, mostra o placeholder da inicial do título.
// centraliza aqui a lógica de "capa ou placeholder" para GameCard, DetalhesScreen e o futuro Calendário reusarem.
@Composable
fun GameCover(
    coverUrl: String,               // endereço da imagem de capa; vazio = sem capa (usa só o placeholder)
    title: String,                  // título do jogo; a inicial dele é o placeholder e a descrição da imagem
    modifier: Modifier = Modifier   // define tamanho/forma da capa; quem chama aplica .size(...).clip(...)
) {
    if (coverUrl.isBlank()) {
        // sem URL: mostra direto o placeholder da inicial
        CoverPlaceholder(title = title, modifier = modifier)
    } else {
        // com URL: o Coil baixa a imagem; loading e error caem no mesmo placeholder da inicial
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(coverUrl)        // a URL a carregar
                .crossfade(true)       // aparição suave da imagem quando termina de carregar
                .build(),
            contentDescription = title, // acessibilidade: descreve a imagem pelo título
            contentScale = ContentScale.Crop, // preenche a área recortando o excesso, sem distorcer
            modifier = modifier,
            loading = { CoverPlaceholder(title = title, modifier = Modifier.fillMaxSize()) }, // enquanto baixa
            error = { CoverPlaceholder(title = title, modifier = Modifier.fillMaxSize()) }    // se falhar
        )
    }
}

// CoverPlaceholder: o "quadrado colorido com a inicial", usado quando não há imagem (ou enquanto ela carrega).
// privado porque é um detalhe interno do GameCover — de fora, todos usam GameCover.
@Composable
private fun CoverPlaceholder(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer), // fundo colorido do tema
        contentAlignment = Alignment.Center // centraliza a inicial
    ) {
        Text(
            text = inicialDoTitulo(title),                         // a letra inicial (função de apoio do GameCard)
            style = MaterialTheme.typography.headlineMedium,       // fonte grande para preencher o espaço
            fontWeight = FontWeight.Bold,                          // negrito para destacar
            textAlign = TextAlign.Center,                          // centralizado horizontalmente
            color = MaterialTheme.colorScheme.onSecondaryContainer // cor que contrasta com o fundo
        )
    }
}

// @Preview: mostra o placeholder (sem URL) no editor
@Preview
@Composable
private fun GameCoverPlaceholderPreview() {
    GameCover(coverUrl = "", title = "Iron Protocol", modifier = Modifier.size(width = 72.dp, height = 96.dp))
}
