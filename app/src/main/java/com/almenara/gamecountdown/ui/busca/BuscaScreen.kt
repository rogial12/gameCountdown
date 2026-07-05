package com.almenara.gamecountdown.ui.busca // pacote da feature de busca dedicada

import androidx.compose.foundation.clickable // torna uma linha do histórico tocável
import androidx.compose.foundation.layout.Arrangement // define o espaçamento entre os itens da lista
import androidx.compose.foundation.layout.Box // container para centralizar as mensagens (dica / sem resultados)
import androidx.compose.foundation.layout.PaddingValues // espaçamento ao redor do conteúdo da lista
import androidx.compose.foundation.layout.Row // organiza o cabeçalho "Buscas recentes" + botão "Limpar" lado a lado
import androidx.compose.foundation.layout.WindowInsets // zera os insets do Scaffold interno (o externo cuida deles)
import androidx.compose.foundation.layout.fillMaxSize // ocupa todo o espaço disponível
import androidx.compose.foundation.layout.fillMaxWidth // faz o campo de busca ocupar toda a largura da barra de topo
import androidx.compose.foundation.layout.padding // aplica espaçamento (usado com o padding do Scaffold)
import androidx.compose.foundation.lazy.LazyColumn // lista vertical que só compõe os itens visíveis
import androidx.compose.foundation.lazy.items // gera um item da LazyColumn para cada elemento de uma lista
import androidx.compose.material.icons.Icons // ponto de acesso aos ícones do Material
import androidx.compose.material.icons.filled.Close // ícone de "limpar" (X) do campo de busca
import androidx.compose.material.icons.filled.Search // ícone de lupa (decorativo, no campo)
import androidx.compose.material3.ExperimentalMaterial3Api // a TopAppBar ainda é API experimental do Material 3
import androidx.compose.material3.Icon // desenha um ícone vetorial
import androidx.compose.material3.IconButton // botão que contém só um ícone (o "limpar" do campo)
import androidx.compose.material3.ListItem // linha padrão do Material 3 para cada busca do histórico
import androidx.compose.material3.MaterialTheme // acesso ao tema (cores, tipografia)
import androidx.compose.material3.Scaffold // estrutura básica de tela
import androidx.compose.material3.Text // desenha texto
import androidx.compose.material3.TextButton // botão de texto (o "Limpar" do histórico)
import androidx.compose.material3.TextField // campo de texto editável (a caixa de busca)
import androidx.compose.material3.TextFieldDefaults // cores padrão do TextField, aqui deixadas transparentes
import androidx.compose.material3.TopAppBar // barra de topo (que aqui hospeda o campo de busca)
import androidx.compose.runtime.Composable // marca uma função como componente de UI do Compose
import androidx.compose.runtime.collectAsState // observa um StateFlow e recompõe quando o estado muda
import androidx.compose.runtime.getValue // habilita ler o estado observado com 'by' (delegação)
import androidx.compose.ui.Alignment // centraliza as mensagens / alinha o cabeçalho do histórico
import androidx.compose.ui.Modifier // descreve ajustes de layout/aparência
import androidx.compose.ui.graphics.Color // usado para deixar fundo/linha do campo transparentes
import androidx.compose.ui.tooling.preview.Preview // visualiza o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de distância independente de densidade
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo; usado no @Preview
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros; usado no @Preview
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas; usado no @Preview
import com.almenara.gamecountdown.ui.comum.GameCard // componente que exibe um jogo na lista (Passo 8)

// BuscaScreen: a tela de Busca "com estado" — conecta a UI ao BuscaViewModel.
// o campo de busca fica na barra de topo; o foco (e o teclado) só aparecem quando o usuário toca nele —
// fluxo em duas etapas (toque na aba -> toque no campo), não mais automático ao abrir (item 4 do feedback de Igor).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscaScreen(
    viewModel: BuscaViewModel,             // fonte do estado e da ação de buscar; injetado pela Factory
    onJogoClick: (String) -> Unit = {},    // chamado com o id do jogo tocado (abrir detalhes); padrão = nada
    modifier: Modifier = Modifier          // ajustes externos; padrão = nenhum
) {
    // observa o estado (texto + resultados)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0), // o Scaffold externo (GameCountdownApp) já aplica os insets
        topBar = {
            TopAppBar(
                title = {
                    // o campo de busca ocupa a barra de topo inteira
                    TextField(
                        value = uiState.query,                  // texto atual, vindo do estado
                        onValueChange = viewModel::buscar,      // cada tecla dispara a busca
                        placeholder = { Text("Buscar jogo...") }, // dica quando vazio
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }, // lupa decorativa
                        trailingIcon = {
                            // botão "limpar" só aparece quando há texto
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.buscar("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Limpar busca")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        // fundo e linha transparentes para o campo se integrar à barra de topo
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            )
        }
    ) { innerPadding ->
        BuscaConteudo(
            uiState = uiState,
            onBuscarHistorico = viewModel::buscar,       // toca um item do histórico -> refaz aquela busca
            onLimparHistorico = viewModel::limparHistorico,
            onJogoClick = { id ->
                viewModel.registrarBuscaSelecionada(id) // aquele jogo entra/sobe no histórico
                onJogoClick(id)                          // e navega para os Detalhes, como antes
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// BuscaConteudo: o corpo da tela "sem estado". Mostra o histórico (ou uma dica, se ele estiver vazio),
// "nenhum resultado" ou a lista de resultados — conforme o texto digitado e o que o Service devolveu.
@Composable
private fun BuscaConteudo(
    uiState: BuscaUiState,                     // texto atual + resultados + histórico
    onBuscarHistorico: (String) -> Unit,       // emite a query escolhida no histórico, pra refazer a busca
    onLimparHistorico: () -> Unit,             // pede pra apagar o histórico inteiro
    onJogoClick: (String) -> Unit,             // emite o id do jogo tocado num resultado
    modifier: Modifier = Modifier
) {
    when {
        // ainda não digitou nada: mostra o histórico de buscas, se houver; senão, a dica de sempre
        uiState.query.isBlank() -> {
            if (uiState.historico.isEmpty()) {
                MensagemCentral(texto = "Busque um jogo pelo título", modifier = modifier)
            } else {
                HistoricoBuscas(
                    historico = uiState.historico,
                    onSelecionarJogo = onJogoClick,       // tocar uma tile de jogo -> mesmo caminho de um resultado normal
                    onBuscarFallback = onBuscarHistorico, // tocar um fallback de texto -> refaz aquela busca
                    onLimpar = onLimparHistorico,
                    modifier = modifier
                )
            }
        }
        // digitou, mas nada casou: mostra "nenhum resultado"
        uiState.resultados.isEmpty() -> {
            MensagemCentral(texto = "Nenhum jogo encontrado", modifier = modifier)
        }
        // há resultados: lista de cards
        else -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.resultados, key = { it.game.id }) { item ->
                    GameCard(
                        game = item.game,
                        dias = item.dias,
                        onClick = { onJogoClick(item.game.id) }
                    )
                }
            }
        }
    }
}

// HistoricoBuscas: lista dos jogos selecionados em buscas anteriores, mais recente primeiro — cada um exibido
// como a MESMA tile (GameCard) usada nos resultados normais (decisão de Igor: o histórico é de jogos, não de
// texto). Só quando o jogo referenciado não existe mais no catálogo é que a linha cai no fallback: o termo
// que foi buscado, como texto simples tocável (refaz aquela busca em vez de abrir um jogo que já não existe).
// aparece só quando o campo está vazio E já existe pelo menos uma entrada salva (item 5 do feedback de Igor).
@Composable
private fun HistoricoBuscas(
    historico: List<HistoricoBuscaItem>, // as entradas salvas, na ordem em que devem aparecer
    onSelecionarJogo: (String) -> Unit,   // toca a tile de um jogo -> emite o id (mesmo caminho de um resultado)
    onBuscarFallback: (String) -> Unit,   // toca um fallback de texto -> refaz aquela busca
    onLimpar: () -> Unit,                 // toca "Limpar" -> apaga o histórico inteiro
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // cabeçalho fixo como primeiro item da lista: título + botão de limpar lado a lado
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Buscas recentes", style = MaterialTheme.typography.titleSmall)
                TextButton(onClick = onLimpar) { Text("Limpar") }
            }
        }
        // uma entrada por busca salva: tile do jogo (se ele ainda existir) ou o fallback de texto
        items(historico, key = { it.jogo?.game?.id ?: it.query }) { entrada ->
            val jogo = entrada.jogo
            if (jogo != null) {
                GameCard(
                    game = jogo.game,
                    dias = jogo.dias,
                    onClick = { onSelecionarJogo(jogo.game.id) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            } else {
                // fallback: o jogo não existe mais no catálogo — mostra só o termo que foi buscado
                ListItem(
                    headlineContent = { Text(entrada.query) },
                    leadingContent = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.clickable { onBuscarFallback(entrada.query) }
                )
            }
        }
    }
}

// MensagemCentral: um texto centralizado na área de conteúdo (usado para a dica e para "nenhum resultado")
@Composable
private fun MensagemCentral(texto: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = texto, style = MaterialTheme.typography.bodyLarge)
    }
}

// @Preview: mostra a lista de resultados com dados fictícios, sem precisar de ViewModel.
@Preview
@Composable
private fun BuscaConteudoPreview() {
    val estado = BuscaUiState(
        query = "iron",
        resultados = listOf(
            JogoBusca(
                game = Game(
                    id = "1", title = "Iron Protocol", releaseDate = "2026-07-10",
                    platforms = listOf(Platform.XBOX_SERIES, Platform.PC), genres = listOf(Genre.ACTION),
                    developer = "Apex Studios", synopsis = "", coverUrl = "",
                    priceUsd = 59.99, priceBrl = 299.90, trailerId = null, preSaleDate = null, anticipationScore = 87
                ),
                dias = 5L
            )
        )
    )
    BuscaConteudo(uiState = estado, onBuscarHistorico = {}, onLimparHistorico = {}, onJogoClick = {})
}

// @Preview: mostra o histórico de buscas (campo vazio, mas com entradas salvas), sem precisar de ViewModel.
// inclui um jogo resolvido (mostra a tile) e um fallback (jogo que não existe mais no catálogo).
@Preview
@Composable
private fun BuscaConteudoHistoricoPreview() {
    val estado = BuscaUiState(
        historico = listOf(
            HistoricoBuscaItem(
                query = "hearth",
                jogo = JogoBusca(
                    game = Game(
                        id = "2", title = "Hearthfall", releaseDate = "2026-08-15",
                        platforms = listOf(Platform.PS5, Platform.PC), genres = listOf(Genre.RPG),
                        developer = "Crimson Forge", synopsis = "", coverUrl = "",
                        priceUsd = 69.99, priceBrl = 349.90, trailerId = null, preSaleDate = null, anticipationScore = 92
                    ),
                    dias = 42L
                )
            ),
            HistoricoBuscaItem(query = "jogo removido do catalogo", jogo = null) // fallback: jogo não existe mais
        )
    )
    BuscaConteudo(uiState = estado, onBuscarHistorico = {}, onLimparHistorico = {}, onJogoClick = {})
}
