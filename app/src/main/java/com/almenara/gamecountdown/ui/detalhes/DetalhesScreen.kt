package com.almenara.gamecountdown.ui.detalhes // pacote da feature de detalhes de um jogo

import android.content.Intent // usado para abrir o trailer no YouTube (fora do app)
import android.net.Uri // representa o endereço (URL) do vídeo do YouTube
import androidx.compose.foundation.background // pinta o fundo da tile "+N" (mais mídias na galeria)
import androidx.compose.foundation.clickable // torna as tiles de mídia e a linha de "onde comprar" tocáveis
import androidx.compose.foundation.layout.Arrangement // define o espaçamento entre os elementos empilhados
import androidx.compose.foundation.layout.Box // container para centralizar (capa, tiles de mídia, "não encontrado")
import androidx.compose.foundation.layout.Column // empilha verticalmente todas as seções da tela
import androidx.compose.foundation.layout.FlowRow // linha de plataformas que quebra quando não cabe
import androidx.compose.foundation.layout.Row // organiza itens lado a lado (countdown + switch; rótulo + valor)
import androidx.compose.foundation.layout.Spacer // espaço flexível para empurrar o switch/botão para a direita
import androidx.compose.foundation.layout.WindowInsets // usado para zerar os insets do Scaffold interno (o externo cuida deles)
import androidx.compose.foundation.layout.fillMaxSize // ocupa todo o espaço disponível
import androidx.compose.foundation.layout.fillMaxWidth // ocupa toda a largura disponível
import androidx.compose.foundation.layout.PaddingValues // espaçamento interno do conteúdo da galeria completa
import androidx.compose.foundation.layout.height // define a altura fixa da capa/tiles
import androidx.compose.foundation.layout.heightIn // limita a altura máxima da galeria completa (com rolagem)
import androidx.compose.foundation.layout.padding // aplica espaçamentos
import androidx.compose.foundation.layout.size // define o tamanho do ícone de play sobre a tile de vídeo
import androidx.compose.foundation.layout.width // define uma largura fixa (espaço entre ícone e texto)
import androidx.compose.foundation.lazy.LazyColumn // lista rolável da galeria completa (pode ter várias mídias)
import androidx.compose.foundation.lazy.items // gera um item da LazyColumn por mídia da galeria
import androidx.compose.foundation.rememberScrollState // guarda a posição de rolagem vertical/horizontal da tela
import androidx.compose.foundation.horizontalScroll // permite rolar o carrossel de mídia na horizontal
import androidx.compose.foundation.verticalScroll // permite rolar a coluna verticalmente (conteúdo longo)
import androidx.compose.material.icons.Icons // ponto de acesso aos ícones do Material
import androidx.compose.material.icons.automirrored.filled.ArrowBack // ícone de "voltar"
import androidx.compose.material.icons.filled.Close // ícone de "fechar" no cabeçalho da galeria completa
import androidx.compose.material.icons.filled.PlayArrow // ícone de play sobre a tile de vídeo do carrossel
import androidx.compose.material3.ExperimentalMaterial3Api // a TopAppBar ainda é API experimental do Material 3
import androidx.compose.material3.Icon // desenha um ícone vetorial
import androidx.compose.material3.IconButton // botão que contém só um ícone ("voltar", "fechar" a galeria)
import androidx.compose.material3.MaterialTheme // acesso ao tema (cores, tipografia, formas)
import androidx.compose.material3.Scaffold // estrutura básica de tela (barra de topo + conteúdo)
import androidx.compose.material3.Surface // "caixa" com fundo/forma, usada como base da galeria completa
import androidx.compose.material3.Text // desenha texto na tela
import androidx.compose.material3.TextButton // botão de texto (o "Comprar" de cada loja)
import androidx.compose.material3.TopAppBar // barra de título no topo
import androidx.compose.runtime.Composable // marca uma função como componente de UI do Compose
import androidx.compose.runtime.collectAsState // observa um StateFlow e recompõe quando o estado muda
import androidx.compose.runtime.getValue // habilita ler o estado observado com 'by' (delegação)
import androidx.compose.runtime.mutableStateOf // cria o estado local de "galeria aberta?"
import androidx.compose.runtime.remember // preserva esse estado entre recomposições
import androidx.compose.runtime.setValue // habilita escrever no estado com 'by' (delegação)
import androidx.compose.ui.Alignment // define alinhamento (centralizar)
import androidx.compose.ui.Modifier // descreve ajustes de layout/aparência
import androidx.compose.ui.draw.clip // recorta a capa/tiles em cantos arredondados
import androidx.compose.ui.graphics.Color // cor branca do ícone de play sobre a miniatura do vídeo
import androidx.compose.ui.platform.LocalContext // fornece o Context atual, necessário para abrir o YouTube
import androidx.compose.ui.text.font.FontWeight // controla o peso (negrito) do texto
import androidx.compose.ui.text.style.TextOverflow // reticências quando o título não cabe
import androidx.compose.ui.tooling.preview.Preview // visualiza o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de distância independente de densidade de tela
import androidx.compose.ui.window.Dialog // janela flutuante usada para a galeria completa de mídia
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros; usado no @Preview
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.ui.comum.AddToListSwitch // switch de "de olho" (Passo 14)
import com.almenara.gamecountdown.ui.comum.CountdownBadge // chip de countdown (Passo 7)
import com.almenara.gamecountdown.ui.comum.GameCover // capa/imagem (via Coil ou placeholder da inicial)
import com.almenara.gamecountdown.ui.comum.PlatformBadge // chip de plataforma (Passo 6)
import com.almenara.gamecountdown.ui.comum.PriceTag // chip de preço (Passo 6)

// função de apoio: converte uma data ISO ("2026-07-10") no formato amigável brasileiro ("10/07/2026").
// é lógica pura (texto -> texto), por isso fica fora do @Composable e é 'internal' para ser testável sem emulador.
// se a entrada não estiver no formato esperado (3 partes separadas por "-"), devolve o texto original sem quebrar.
internal fun formatarData(iso: String): String {
    val partes = iso.split("-") // separa em [ano, mês, dia]
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else iso // reordena para dia/mês/ano
}

// enum: o tipo de cada item do carrossel de mídia — determina como a tile é desenhada e o que o toque faz
internal enum class TipoMidia { VIDEO, IMAGEM }

// data class: um item do carrossel de mídia. 'url' significa coisas diferentes conforme o tipo:
// para VIDEO é o trailerId (ID do YouTube); para IMAGEM é a URL da captura de tela.
internal data class Midia(val tipo: TipoMidia, val url: String)

// função de apoio: monta a lista de mídias de um jogo na ordem certa — o vídeo do trailer primeiro
// (se houver), seguido das capturas de tela. É lógica pura (Game -> lista), por isso fica fora do
// @Composable e é 'internal' para ser testável sem emulador. Quem decide quantas mídias CABEM na tela
// (as 3 primeiras + o botão de galeria) é a UI, não esta função — ela só define a ORDEM completa.
internal fun montarMidias(game: Game): List<Midia> {
    val video = game.trailerId?.let { Midia(TipoMidia.VIDEO, it) }
    val imagens = game.screenshotUrls.map { Midia(TipoMidia.IMAGEM, it) }
    return listOfNotNull(video) + imagens
}

// função de apoio: nome fictício da loja de cada plataforma, para a seção "Onde comprar".
// preparação para links de afiliado reais numa fase futura — por ora só o nome, sem link de verdade.
// fica na UI (não no enum Platform) pela mesma razão de rotuloPeriodo/rotuloOrdenacao na FilterBar:
// mantém o enum de modelo livre de texto de apresentação específico de uma tela.
internal fun lojaPara(platform: Platform): String = when (platform) {
    Platform.PS5 -> "PlayStation Store"
    Platform.XBOX_SERIES -> "Xbox Store"
    Platform.PC -> "Steam"
    Platform.SWITCH -> "Nintendo eShop"
    Platform.MOBILE -> "Google Play / App Store"
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
            // estado local de UI (não é regra de negócio): a galeria completa está aberta?
            // recalculado a cada composição a partir do jogo atual — leve o bastante pra não precisar de remember aqui
            var galeriaAberta by remember { mutableStateOf(false) }
            val midias = montarMidias(game)

            // conteúdo rolável: a tela pode ficar mais alta que o visível (sinopse longa etc.)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)                 // respeita a barra de topo
                    .verticalScroll(rememberScrollState())  // habilita a rolagem vertical
                    .padding(16.dp),                        // respiro nas bordas do conteúdo
                verticalArrangement = Arrangement.spacedBy(12.dp) // espaço fixo entre as seções
            ) {
                // carrossel de mídia: vídeo do trailer (se houver) + capturas de tela — substitui a capa estática
                // única de antes (item 8.1 do feedback de Igor). Some quando o jogo não tem mídia nenhuma.
                if (midias.isNotEmpty()) {
                    MediaCarrossel(
                        midias = midias,
                        onAbrirVideo = onAssistirTrailer,
                        onAbrirGaleria = { galeriaAberta = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // título do jogo
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // countdown em destaque: bem maior que os demais tiles da tela (item 8.2 do feedback de Igor)
                CountdownBadge(dias = dias, destaque = true, modifier = Modifier.fillMaxWidth())

                // linha só com o rótulo + o switch de "de olho" — o countdown saiu daqui pro destaque acima,
                // mas o Switch continua (decisão de Igor, item 8.3: manter o Switch no "De Olho" nesta tela)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "De olho", style = MaterialTheme.typography.labelLarge) // rótulo do switch
                    Spacer(modifier = Modifier.weight(1f))     // empurra o switch para a direita
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

                // "Onde comprar": uma linha por plataforma do jogo, com o nome fictício da loja e um botão
                // "Comprar" ainda sem link real — preparação para links de afiliado (item 8.4 do feedback de Igor)
                SecaoOndeComprar(plataformas = game.platforms)
            }

            // a galeria completa é um Dialog por cima da tela; só existe enquanto 'galeriaAberta' for true
            if (galeriaAberta) {
                GaleriaCompleta(
                    midias = midias,
                    onFechar = { galeriaAberta = false },
                    onAbrirVideo = onAssistirTrailer
                )
            }
        }
    }
}

// MediaCarrossel: linha rolável com as 3 primeiras mídias do jogo (vídeo do trailer, se houver, seguido de
// capturas de tela) — cada tile do mesmo tamanho. Se houver mais mídias além dessas 3, uma quarta tile
// "+N" (mesmo formato das outras) abre a galeria completa.
@Composable
private fun MediaCarrossel(
    midias: List<Midia>,               // todas as mídias do jogo, na ordem (vídeo primeiro, depois imagens)
    onAbrirVideo: () -> Unit,          // toca a tile de vídeo -> abre o trailer no YouTube
    onAbrirGaleria: () -> Unit,        // toca uma tile de imagem, ou a tile "+N" -> abre a galeria completa
    modifier: Modifier = Modifier
) {
    val visiveis = midias.take(3)       // no máximo 3 mídias exibidas diretamente no carrossel
    val restantes = midias.size - visiveis.size // quantas mídias sobram fora das 3 exibidas

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        visiveis.forEach { midia ->
            MidiaTile(
                midia = midia,
                onClick = { if (midia.tipo == TipoMidia.VIDEO) onAbrirVideo() else onAbrirGaleria() },
                modifier = Modifier
                    .size(width = 160.dp, height = 100.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }
        // a quarta tile só aparece quando há mídia além das 3 já exibidas — sem ela, "ver galeria" não mostraria nada novo
        if (restantes > 0) {
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 100.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onAbrirGaleria),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$restantes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// MidiaTile: uma única tile do carrossel/galeria. Vídeo mostra a miniatura do YouTube com um ícone de play
// por cima; imagem mostra a captura de tela direto. Reusa o GameCover como carregador de imagem genérico —
// ele já resolve Coil + placeholder, tanto para miniaturas de vídeo quanto para capturas de tela reais.
@Composable
private fun MidiaTile(midia: Midia, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        when (midia.tipo) {
            TipoMidia.VIDEO -> {
                // miniatura oficial do YouTube pelo ID do vídeo — não precisa de API key nem SDK do YouTube
                GameCover(
                    coverUrl = "https://img.youtube.com/vi/${midia.url}/hqdefault.jpg",
                    title = "Trailer",
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Assistir ao trailer",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                )
            }
            TipoMidia.IMAGEM -> {
                GameCover(coverUrl = midia.url, title = "Captura de tela", modifier = Modifier.fillMaxSize())
            }
        }
    }
}

// GaleriaCompleta: janela flutuante (Dialog) com TODAS as mídias do jogo, uma abaixo da outra e roláveis —
// o destino do botão "+N" do carrossel. Tocar a mídia de vídeo abre o trailer; imagens só são exibidas maiores.
@Composable
private fun GaleriaCompleta(
    midias: List<Midia>,          // todas as mídias do jogo (não só as 3 do carrossel)
    onFechar: () -> Unit,         // fecha o Dialog (toque no X ou fora dele)
    onAbrirVideo: () -> Unit      // toca a mídia de vídeo -> abre o trailer no YouTube
) {
    Dialog(onDismissRequest = onFechar) {
        Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 480.dp), // altura máxima; rola se a galeria for grande
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // cabeçalho fixo: título + botão de fechar, lado a lado
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Galeria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = onFechar) {
                            Icon(Icons.Filled.Close, contentDescription = "Fechar")
                        }
                    }
                }
                // uma tile grande por mídia; vídeo abre o trailer, imagem só é exibida (sem outra ação)
                items(midias) { midia ->
                    MidiaTile(
                        midia = midia,
                        onClick = { if (midia.tipo == TipoMidia.VIDEO) onAbrirVideo() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        }
    }
}

// SecaoOndeComprar: uma linha por plataforma do jogo, com o nome da loja e um botão "Comprar" — preparação
// para links de afiliado reais numa fase futura (item 8.4 do feedback de Igor); por ora o botão não faz nada.
@Composable
private fun SecaoOndeComprar(plataformas: List<Platform>) {
    Text(text = "Onde comprar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        plataformas.forEach { plataforma ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = lojaPara(plataforma), style = MaterialTheme.typography.bodyMedium)
                // sem link real ainda — só o texto do botão, pronto para virar um link de afiliado depois
                TextButton(onClick = { }) {
                    Text("Comprar")
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
        trailerId = "dQw4w9WgXcQ",
        // 4 imagens + o trailer = 5 mídias: exercita a tile "+2" do carrossel no preview
        screenshotUrls = listOf("img1", "img2", "img3", "img4"),
        preSaleDate = "2026-06-10", anticipationScore = 87,
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
