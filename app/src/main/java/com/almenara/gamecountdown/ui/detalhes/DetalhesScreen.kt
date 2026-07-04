package com.almenara.gamecountdown.ui.detalhes // pacote da feature de detalhes de um jogo

import android.content.Intent // usado para abrir o trailer no YouTube (fora do app)
import android.net.Uri // representa o endereço (URL) do vídeo do YouTube
import androidx.compose.foundation.layout.Arrangement // define o espaçamento entre os elementos empilhados
import androidx.compose.foundation.layout.Box // container para centralizar (capa e mensagem de "não encontrado")
import androidx.compose.foundation.layout.Column // empilha verticalmente todas as seções da tela
import androidx.compose.foundation.layout.FlowRow // linha de plataformas que quebra quando não cabe
import androidx.compose.foundation.layout.Row // organiza itens lado a lado (countdown + switch; rótulo + valor)
import androidx.compose.foundation.layout.Spacer // espaço flexível para empurrar o switch para a direita
import androidx.compose.foundation.layout.WindowInsets // usado para zerar os insets do Scaffold interno (o externo cuida deles)
import androidx.compose.foundation.layout.fillMaxSize // ocupa todo o espaço disponível
import androidx.compose.foundation.layout.fillMaxWidth // ocupa toda a largura disponível
import androidx.compose.foundation.layout.height // define a altura fixa da capa
import androidx.compose.foundation.layout.padding // aplica espaçamentos
import androidx.compose.foundation.layout.width // define uma largura fixa (espaço entre ícone e texto do botão)
import androidx.compose.foundation.rememberScrollState // guarda a posição de rolagem vertical da tela
import androidx.compose.foundation.verticalScroll // permite rolar a coluna verticalmente (conteúdo longo)
import androidx.compose.material.icons.Icons // ponto de acesso aos ícones do Material
import androidx.compose.material.icons.automirrored.filled.ArrowBack // ícone de "voltar"
import androidx.compose.material.icons.filled.PlayArrow // ícone de play no botão de trailer
import androidx.compose.material3.Button // botão preenchido (o "Assistir ao trailer")
import androidx.compose.material3.ExperimentalMaterial3Api // a TopAppBar ainda é API experimental do Material 3
import androidx.compose.material3.Icon // desenha um ícone vetorial
import androidx.compose.material3.IconButton // botão que contém só um ícone (o "voltar")
import androidx.compose.material3.MaterialTheme // acesso ao tema (cores, tipografia, formas)
import androidx.compose.material3.Scaffold // estrutura básica de tela (barra de topo + conteúdo)
import androidx.compose.material3.Text // desenha texto na tela
import androidx.compose.material3.TopAppBar // barra de título no topo
import androidx.compose.runtime.Composable // marca uma função como componente de UI do Compose
import androidx.compose.runtime.collectAsState // observa um StateFlow e recompõe quando o estado muda
import androidx.compose.runtime.getValue // habilita ler o estado observado com 'by' (delegação)
import androidx.compose.ui.Alignment // define alinhamento (centralizar)
import androidx.compose.ui.Modifier // descreve ajustes de layout/aparência
import androidx.compose.ui.draw.clip // recorta a capa em cantos arredondados
import androidx.compose.ui.platform.LocalContext // fornece o Context atual, necessário para abrir o YouTube
import androidx.compose.ui.text.font.FontWeight // controla o peso (negrito) do texto
import androidx.compose.ui.text.style.TextOverflow // reticências quando o título não cabe
import androidx.compose.ui.tooling.preview.Preview // visualiza o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de distância independente de densidade de tela
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros; usado no @Preview
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.ui.comum.AddToListSwitch // switch de "de olho" (Passo 14)
import com.almenara.gamecountdown.ui.comum.CountdownBadge // chip de countdown (Passo 7)
import com.almenara.gamecountdown.ui.comum.GameCover // capa do jogo (imagem via Coil ou placeholder da inicial)
import com.almenara.gamecountdown.ui.comum.PlatformBadge // chip de plataforma (Passo 6)
import com.almenara.gamecountdown.ui.comum.PriceTag // chip de preço (Passo 6)

// função de apoio: converte uma data ISO ("2026-07-10") no formato amigável brasileiro ("10/07/2026").
// é lógica pura (texto -> texto), por isso fica fora do @Composable e é 'internal' para ser testável sem emulador.
// se a entrada não estiver no formato esperado (3 partes separadas por "-"), devolve o texto original sem quebrar.
internal fun formatarData(iso: String): String {
    val partes = iso.split("-") // separa em [ano, mês, dia]
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else iso // reordena para dia/mês/ano
}

// DetalhesScreen: a tela de detalhes "com estado" — conecta a UI ao DetalhesViewModel.
// é aqui que fica o acesso ao Context (para abrir o YouTube), mantendo o conteúdo abaixo livre de dependências do Android.
@Composable
fun DetalhesScreen(
    viewModel: DetalhesViewModel,       // fonte do estado e das ações; injetado pela Factory (com o id do jogo)
    onVoltar: () -> Unit = {},          // chamado ao tocar "voltar" (a navegação tratará isso); padrão = nada
    modifier: Modifier = Modifier       // ajustes externos; padrão = nenhum
) {
    // observa o estado do ViewModel
    val uiState by viewModel.uiState.collectAsState()
    // Context atual, necessário para disparar o Intent que abre o YouTube
    val context = LocalContext.current

    DetalhesConteudo(
        game = uiState.game,
        dias = uiState.dias,
        onVoltar = onVoltar,
        onAlternarWatched = viewModel::alternarWatched, // liga/desliga o "de olho"
        onAssistirTrailer = {
            // monta a URL do YouTube com o trailerId e abre no app do YouTube/navegador (fora do app)
            uiState.game?.trailerId?.let { id ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$id"))
                context.startActivity(intent)
            }
        },
        modifier = modifier
    )
}

// DetalhesConteudo: o corpo da tela "sem estado" — recebe tudo por parâmetro e só desenha + emite callbacks.
// não conhece o ViewModel nem o Context, o que o torna previewável e testável isoladamente.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalhesConteudo(
    game: Game?,                        // o jogo a exibir; null quando o id não existe (mostra "não encontrado")
    dias: Long,                         // countdown já calculado
    onVoltar: () -> Unit,               // emite o pedido de voltar
    onAlternarWatched: () -> Unit,      // emite o pedido de alternar "de olho"
    onAssistirTrailer: () -> Unit,      // emite o pedido de abrir o trailer
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        // zera os insets aqui porque esta tela roda dentro do Scaffold externo (GameCountdownApp), que já os aplica
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                // título da barra: o nome do jogo (ou "Detalhes" enquanto/na ausência de jogo)
                title = {
                    Text(
                        text = game?.title ?: "Detalhes",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    // botão "voltar" à esquerda da barra
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (game == null) {
            // caso de borda: id não encontrado -> mensagem central em vez de tela vazia
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Jogo não encontrado", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // conteúdo rolável: a tela pode ficar mais alta que o visível (sinopse longa etc.)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)                 // respeita a barra de topo
                    .verticalScroll(rememberScrollState())  // habilita a rolagem vertical
                    .padding(16.dp),                        // respiro nas bordas do conteúdo
                verticalArrangement = Arrangement.spacedBy(12.dp) // espaço fixo entre as seções
            ) {
                // capa grande do jogo: carrega a imagem (Coil) ou mostra o placeholder da inicial
                GameCover(
                    coverUrl = game.coverUrl,
                    title = game.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                )

                // título do jogo
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // linha com o countdown à esquerda e o controle de "de olho" à direita
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CountdownBadge(dias = dias)                 // chip de countdown
                    Spacer(modifier = Modifier.weight(1f))     // empurra o que vem depois para a direita
                    Text(text = "De olho", style = MaterialTheme.typography.labelLarge) // rótulo do switch
                    AddToListSwitch(
                        marcado = game.isWatched,                          // reflete o estado atual
                        onMarcarChange = { onAlternarWatched() }           // qualquer alternância inverte o watched
                    )
                }

                // plataformas do jogo (uma por chip; quebram de linha se não couberem)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    game.platforms.forEach { plataforma -> PlatformBadge(platform = plataforma) }
                }

                // preço (mesma regra do card: BRL, senão USD, senão "não anunciado")
                PriceTag(priceUsd = game.priceUsd, priceBrl = game.priceBrl)

                // informações textuais rotuladas
                LinhaInfo(rotulo = "Desenvolvedor", valor = game.developer)
                LinhaInfo(rotulo = "Lançamento", valor = formatarData(game.releaseDate))
                // pré-venda só aparece quando existe
                if (game.preSaleDate != null) {
                    LinhaInfo(rotulo = "Pré-venda", valor = formatarData(game.preSaleDate))
                }

                // sinopse, só quando há texto
                if (game.synopsis.isNotBlank()) {
                    Text(
                        text = "Sinopse",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = game.synopsis, style = MaterialTheme.typography.bodyMedium)
                }

                // botão do trailer, só quando o jogo tem trailerId — abre o YouTube externamente
                if (game.trailerId != null) {
                    Button(onClick = onAssistirTrailer) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null) // ícone de play (decorativo)
                        Spacer(modifier = Modifier.width(8.dp))                  // espaço entre ícone e texto
                        Text(text = "Assistir ao trailer")
                    }
                }
            }
        }
    }
}

// LinhaInfo: uma linha "Rótulo: valor" reutilizada nas informações textuais (desenvolvedor, datas)
@Composable
private fun LinhaInfo(rotulo: String, valor: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "$rotulo:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold // o rótulo em seminegrito destaca-se do valor
        )
        Text(text = valor, style = MaterialTheme.typography.bodyMedium)
    }
}

// @Preview: renderiza o CONTEÚDO da tela (parte sem estado) com um jogo fictício, sem precisar de ViewModel.
@Preview
@Composable
private fun DetalhesConteudoPreview() {
    val exemplo = Game(
        id = "1", title = "Iron Protocol", releaseDate = "2026-07-10",
        platforms = listOf(Platform.XBOX_SERIES, Platform.PC), genres = listOf(Genre.ACTION),
        developer = "Apex Studios",
        synopsis = "Thriller de ação em um mundo pós-colapso, onde uma IA controla toda a infraestrutura das cidades.",
        coverUrl = "", priceUsd = 59.99, priceBrl = 299.90,
        trailerId = "dQw4w9WgXcQ", preSaleDate = "2026-06-10", anticipationScore = 87,
        isWatched = true
    )
    DetalhesConteudo(
        game = exemplo,
        dias = 5L,
        onVoltar = {},
        onAlternarWatched = {},
        onAssistirTrailer = {}
    )
}
