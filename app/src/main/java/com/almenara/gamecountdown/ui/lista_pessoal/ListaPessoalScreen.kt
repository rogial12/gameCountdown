package com.almenara.gamecountdown.ui.lista_pessoal // pacote da feature "Jogos que estou de olho"

import androidx.compose.foundation.layout.Arrangement // define o espaçamento entre os itens da lista
import androidx.compose.foundation.layout.Box // container usado para centralizar a mensagem de "lista vazia"
import androidx.compose.foundation.layout.PaddingValues // espaçamento ao redor do conteúdo da lista
import androidx.compose.foundation.layout.WindowInsets // usado para zerar os insets do Scaffold interno (o externo cuida deles)
import androidx.compose.foundation.layout.fillMaxSize // faz um componente ocupar todo o espaço disponível
import androidx.compose.foundation.layout.padding // aplica espaçamento (usado com o padding do Scaffold)
import androidx.compose.foundation.lazy.LazyColumn // lista vertical que só compõe os itens visíveis (eficiente na rolagem)
import androidx.compose.foundation.lazy.items // gera um item da LazyColumn para cada elemento de uma lista
import androidx.compose.material.icons.Icons // ponto de acesso aos ícones do Material
import androidx.compose.material.icons.automirrored.filled.List // ícone (lista) para voltar da grade à lista
import androidx.compose.material.icons.filled.DateRange // ícone (calendário) para alternar para a grade
import androidx.compose.material3.ExperimentalMaterial3Api // a TopAppBar ainda é marcada como API experimental do Material 3
import androidx.compose.material3.Icon // desenha um ícone vetorial
import androidx.compose.material3.IconButton // botão clicável que contém só um ícone (o alternador de visão)
import androidx.compose.material3.MaterialTheme // acesso ao tema atual (cores, tipografia)
import androidx.compose.material3.Scaffold // estrutura básica de tela (barra de topo + conteúdo + área de snackbar)
import androidx.compose.material3.SnackbarDuration // por quanto tempo o snackbar fica na tela
import androidx.compose.material3.SnackbarHost // área onde o snackbar é exibido
import androidx.compose.material3.SnackbarHostState // guarda/estado do snackbar; usado para exibi-lo por código
import androidx.compose.material3.SnackbarResult // resultado do snackbar: dispensado ou ação ("Desfazer") tocada
import androidx.compose.material3.Text // componente que desenha um texto na tela
import androidx.compose.material3.TopAppBar // barra de título no topo da tela
import androidx.compose.runtime.Composable // anotação que marca uma função como componente de UI do Compose
import androidx.compose.runtime.collectAsState // observa um StateFlow e recompõe a UI quando o estado muda
import androidx.compose.runtime.getValue // habilita ler o estado observado com 'by' (delegação)
import androidx.compose.runtime.mutableStateOf // cria o estado local do modo de visão (lista x grade)
import androidx.compose.runtime.remember // preserva um valor entre recomposições (o estado do snackbar)
import androidx.compose.runtime.rememberCoroutineScope // fornece um escopo de corrotina ligado à tela (para exibir o snackbar)
import androidx.compose.runtime.saveable.rememberSaveable // preserva o modo de visão mesmo após rotação de tela
import androidx.compose.runtime.setValue // habilita escrever no estado com 'by' (delegação)
import androidx.compose.ui.Alignment // define alinhamento (centralizar a mensagem de lista vazia)
import androidx.compose.ui.Modifier // objeto que descreve ajustes de layout/aparência
import androidx.compose.ui.tooling.preview.Preview // permite visualizar o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de medida de distância independente de densidade de tela
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo; usado no @Preview
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros; usado no @Preview
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas; usado no @Preview
import com.almenara.gamecountdown.ui.comum.AddToListSwitch // switch de "de olho" (Passo 14)
import com.almenara.gamecountdown.ui.comum.Calendario // visão de grade mensal (Passo 21)
import com.almenara.gamecountdown.ui.comum.GameCard // componente que exibe um jogo na lista (Passo 8)
import kotlinx.coroutines.launch // inicia uma corrotina para exibir o snackbar sem travar a UI

// ListaPessoalScreen: a tela "Jogos que estou de olho" com estado — conecta a UI ao ListaPessoalViewModel.
// observa o estado e trata a remoção com um snackbar de "Desfazer" (decisão de produto de Igor).
@OptIn(ExperimentalMaterial3Api::class) // exigido para usar a TopAppBar, ainda experimental no Material 3
@Composable
fun ListaPessoalScreen(
    viewModel: ListaPessoalViewModel,      // fonte do estado e das ações; injetado pela Factory
    onJogoClick: (String) -> Unit = {},    // chamado com o id do jogo tocado (abrir detalhes no futuro); padrão = nada
    modifier: Modifier = Modifier          // ajustes externos; padrão = nenhum
) {
    // observa o StateFlow do ViewModel: recompõe quando a lista muda
    val uiState by viewModel.uiState.collectAsState()

    // estado do snackbar e escopo de corrotina para exibi-lo (o snackbar é assíncrono: aparece e espera resposta)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // modo de visão: false = lista (padrão), true = grade de calendário. rememberSaveable sobrevive à rotação.
    var modoGrade by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        // zera os insets aqui porque esta tela roda dentro do Scaffold externo (GameCountdownApp), que já os aplica
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Jogos que estou de olho") }, // título da tela
                actions = {
                    // botão que alterna entre lista e grade de calendário (mesma UX do Catálogo)
                    IconButton(onClick = { modoGrade = !modoGrade }) {
                        if (modoGrade) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Ver em lista")
                        } else {
                            Icon(Icons.Filled.DateRange, contentDescription = "Ver em calendário")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // onde o snackbar de "Desfazer" será exibido
    ) { innerPadding ->
        ListaPessoalConteudo(
            jogos = uiState.jogos,                       // jogos da lista pessoal (com dias)
            modoGrade = modoGrade,                       // se true, mostra a grade de calendário em vez da lista
            onRemover = { id ->
                // remove na hora e exibe um snackbar com a opção de desfazer
                viewModel.removerDaLista(id)
                scope.launch {
                    val resultado = snackbarHostState.showSnackbar(
                        message = "Removido da lista",       // texto do snackbar
                        actionLabel = "Desfazer",            // botão de ação
                        duration = SnackbarDuration.Short    // some sozinho após alguns segundos
                    )
                    // se o usuário tocou "Desfazer", reinsere o jogo na lista
                    if (resultado == SnackbarResult.ActionPerformed) {
                        viewModel.desfazerRemocao(id)
                    }
                }
            },
            onJogoClick = onJogoClick,                   // repassa o toque num card
            modifier = Modifier.padding(innerPadding)    // aplica o espaço da barra de topo
        )
    }
}

// ListaPessoalConteudo: o corpo da tela "sem estado" — recebe tudo por parâmetro e só desenha + emite callbacks.
// não conhece o ViewModel, o que o torna fácil de visualizar no @Preview e testar isoladamente.
@Composable
private fun ListaPessoalConteudo(
    jogos: List<JogoLista>,            // jogos observados, com os dias já calculados
    modoGrade: Boolean,                // true = grade de calendário; false = lista
    onRemover: (String) -> Unit,       // emite o id do jogo a remover (switch desligado)
    onJogoClick: (String) -> Unit,     // emite o id do jogo tocado
    modifier: Modifier = Modifier      // ajustes externos (aqui: o padding da barra de topo)
) {
    when {
        // visão em grade: o Calendário da Lista Pessoal mostra só os jogos "de olho"
        modoGrade -> {
            Calendario(
                jogos = jogos.map { it.game },
                onJogoClick = onJogoClick,
                modifier = modifier
            )
        }
        // visão em lista, sem nada: mostra a mensagem de vazio
        jogos.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),      // ocupa todo o espaço da área de conteúdo
                contentAlignment = Alignment.Center     // centraliza a mensagem
            ) {
                Text(
                    text = "Você ainda não está de olho em nenhum jogo", // texto do estado vazio
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        // visão em lista com jogos: a LazyColumn de cards com o switch de remover
        else -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(12.dp),              // respiro ao redor da lista
                verticalArrangement = Arrangement.spacedBy(12.dp)   // espaço fixo entre os cards
            ) {
                // um GameCard por jogo; a 'key' pelo id ajuda o Compose a reaproveitar itens ao remover
                items(jogos, key = { it.game.id }) { item ->
                    GameCard(
                        game = item.game,                          // o jogo a exibir
                        dias = item.dias,                          // o countdown já calculado
                        onClick = { onJogoClick(item.game.id) },   // ao tocar o card, avisa com o id
                        trailing = {
                            // switch sempre ligado aqui (todo jogo da lista está "de olho");
                            // ao desligar (marcado = false), pede a remoção do jogo
                            AddToListSwitch(
                                marcado = true,
                                onMarcarChange = { marcado ->
                                    if (!marcado) onRemover(item.game.id)
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

// @Preview: renderiza o CONTEÚDO da tela (a parte sem estado) com dados fictícios, sem precisar de ViewModel.
@Preview
@Composable
private fun ListaPessoalConteudoPreview() {
    val exemplos = listOf(
        JogoLista(
            game = Game(
                id = "2", title = "Hearthfall", releaseDate = "2026-08-15",
                platforms = listOf(Platform.PS5, Platform.PC), genres = listOf(Genre.RPG),
                developer = "Crimson Forge", synopsis = "", coverUrl = "",
                priceUsd = 69.99, priceBrl = 349.90, trailerId = null, preSaleDate = null, anticipationScore = 92
            ),
            dias = 12L
        ),
        JogoLista(
            game = Game(
                id = "4", title = "Neon Samurai 2", releaseDate = "2026-11-05",
                platforms = listOf(Platform.PS5), genres = listOf(Genre.ACTION),
                developer = "Ronin Interactive", synopsis = "", coverUrl = "",
                priceUsd = 69.99, priceBrl = 349.90, trailerId = null, preSaleDate = null, anticipationScore = 95
            ),
            dias = 120L
        )
    )
    ListaPessoalConteudo(jogos = exemplos, modoGrade = false, onRemover = {}, onJogoClick = {})
}
