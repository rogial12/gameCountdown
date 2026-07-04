package com.almenara.gamecountdown.ui.busca // pacote da feature de busca dedicada

import androidx.compose.foundation.layout.Arrangement // define o espaçamento entre os itens da lista
import androidx.compose.foundation.layout.Box // container para centralizar as mensagens (dica / sem resultados)
import androidx.compose.foundation.layout.PaddingValues // espaçamento ao redor do conteúdo da lista
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
import androidx.compose.material3.IconButton // botão que contém só um ícone (o "limpar")
import androidx.compose.material3.MaterialTheme // acesso ao tema (cores, tipografia)
import androidx.compose.material3.Scaffold // estrutura básica de tela
import androidx.compose.material3.Text // desenha texto
import androidx.compose.material3.TextField // campo de texto editável (a caixa de busca)
import androidx.compose.material3.TextFieldDefaults // cores padrão do TextField, aqui deixadas transparentes
import androidx.compose.material3.TopAppBar // barra de topo (que aqui hospeda o campo de busca)
import androidx.compose.runtime.Composable // marca uma função como componente de UI do Compose
import androidx.compose.runtime.LaunchedEffect // roda um efeito uma vez ao entrar na tela (focar o campo)
import androidx.compose.runtime.collectAsState // observa um StateFlow e recompõe quando o estado muda
import androidx.compose.runtime.getValue // habilita ler o estado observado com 'by' (delegação)
import androidx.compose.runtime.remember // preserva o FocusRequester entre recomposições
import androidx.compose.ui.Alignment // centraliza as mensagens
import androidx.compose.ui.Modifier // descreve ajustes de layout/aparência
import androidx.compose.ui.focus.FocusRequester // permite pedir o foco para o campo ao abrir a tela
import androidx.compose.ui.focus.focusRequester // modifier que liga o FocusRequester ao campo
import androidx.compose.ui.graphics.Color // usado para deixar fundo/linha do campo transparentes
import androidx.compose.ui.tooling.preview.Preview // visualiza o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de distância independente de densidade
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo; usado no @Preview
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros; usado no @Preview
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas; usado no @Preview
import com.almenara.gamecountdown.ui.comum.GameCard // componente que exibe um jogo na lista (Passo 8)

// BuscaScreen: a tela de Busca "com estado" — conecta a UI ao BuscaViewModel.
// o campo de busca fica na barra de topo (foca automaticamente ao abrir a aba); os resultados vêm abaixo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscaScreen(
    viewModel: BuscaViewModel,             // fonte do estado e da ação de buscar; injetado pela Factory
    onJogoClick: (String) -> Unit = {},    // chamado com o id do jogo tocado (abrir detalhes); padrão = nada
    modifier: Modifier = Modifier          // ajustes externos; padrão = nenhum
) {
    // observa o estado (texto + resultados)
    val uiState by viewModel.uiState.collectAsState()

    // ao abrir a aba, foca o campo de busca (já sobe o teclado)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),    // liga ao pedido de foco automático
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
            onJogoClick = onJogoClick,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// BuscaConteudo: o corpo da tela "sem estado". Mostra uma dica (sem texto), "nenhum resultado" ou a lista.
@Composable
private fun BuscaConteudo(
    uiState: BuscaUiState,             // texto atual + resultados
    onJogoClick: (String) -> Unit,     // emite o id do jogo tocado
    modifier: Modifier = Modifier
) {
    when {
        // ainda não digitou nada: mostra uma dica central
        uiState.query.isBlank() -> {
            MensagemCentral(texto = "Busque um jogo pelo título", modifier = modifier)
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
    BuscaConteudo(uiState = estado, onJogoClick = {})
}
