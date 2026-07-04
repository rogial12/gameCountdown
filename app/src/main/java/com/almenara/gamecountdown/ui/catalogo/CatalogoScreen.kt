package com.almenara.gamecountdown.ui.catalogo // pacote da feature de catálogo, dentro de ui/

import androidx.compose.foundation.layout.Arrangement // define o espaçamento entre os itens da lista
import androidx.compose.foundation.layout.Box // container usado para centralizar a mensagem de "lista vazia"
import androidx.compose.foundation.layout.Column // empilha a FilterBar sobre o conteúdo (lista ou vazio)
import androidx.compose.foundation.layout.PaddingValues // espaçamento ao redor do conteúdo da lista
import androidx.compose.foundation.layout.WindowInsets // usado para zerar os insets do Scaffold interno (o externo cuida deles)
import androidx.compose.foundation.layout.fillMaxSize // faz um componente ocupar todo o espaço disponível
import androidx.compose.foundation.layout.padding // aplica espaçamento (usado com o padding do Scaffold)
import androidx.compose.foundation.lazy.LazyColumn // lista vertical que só compõe os itens visíveis (eficiente para rolagem)
import androidx.compose.foundation.lazy.items // gera um item da LazyColumn para cada elemento de uma lista
import androidx.compose.material.icons.Icons // ponto de acesso aos ícones do Material
import androidx.compose.material.icons.automirrored.filled.List // ícone (lista) para voltar da grade à lista
import androidx.compose.material.icons.filled.Add // ícone (+) do botão de adicionar à Lista Pessoal
import androidx.compose.material.icons.filled.Check // ícone (✓) do botão quando o jogo já está na Lista Pessoal
import androidx.compose.material.icons.filled.DateRange // ícone (calendário) para alternar para a grade
import androidx.compose.material3.ExperimentalMaterial3Api // a TopAppBar ainda é marcada como API experimental do Material 3
import androidx.compose.material3.Icon // desenha um ícone vetorial
import androidx.compose.material3.IconButton // botão clicável que contém só um ícone (o alternador de visão)
import androidx.compose.material3.MaterialTheme // acesso ao tema atual (cores, tipografia)
import androidx.compose.material3.Scaffold // estrutura básica de tela do Material 3 (barra de topo + conteúdo)
import androidx.compose.material3.Text // componente que desenha um texto na tela
import androidx.compose.material3.TopAppBar // barra de título no topo da tela
import androidx.compose.runtime.Composable // anotação que marca uma função como componente de UI do Compose
import androidx.compose.runtime.collectAsState // observa um StateFlow e recompõe a UI quando o estado muda
import androidx.compose.runtime.getValue // habilita ler o estado observado com 'by' (delegação)
import androidx.compose.runtime.mutableStateOf // cria o estado local do modo de visão (lista x grade)
import androidx.compose.runtime.saveable.rememberSaveable // preserva o modo de visão mesmo após rotação de tela
import androidx.compose.runtime.setValue // habilita escrever no estado com 'by' (delegação)
import androidx.compose.ui.Alignment // define alinhamento (centralizar a mensagem de lista vazia)
import androidx.compose.ui.Modifier // objeto que descreve ajustes de layout/aparência
import androidx.compose.ui.tooling.preview.Preview // permite visualizar o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de medida de distância independente de densidade de tela
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo; usado no @Preview
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros; usado no @Preview
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas; usado no @Preview
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação; parte da assinatura do conteúdo
import com.almenara.gamecountdown.data.service.FiltroCatalogo // agrupador de filtros; parte da assinatura do conteúdo
import com.almenara.gamecountdown.ui.comum.Calendario // visão de grade mensal (Passo 20/3b)
import com.almenara.gamecountdown.ui.comum.FilterBar // barra de filtros/ordenação, compartilhada com a Lista Pessoal
import com.almenara.gamecountdown.ui.comum.GameCard // componente que exibe um jogo na lista (Passo 8)

// CatalogoScreen: a tela de Catálogo "com estado" — é o ponto onde a UI se conecta ao ViewModel.
// Ela observa o CatalogoUiState e repassa os dados e os callbacks para o conteúdo (sem estado).
// A busca deixou de morar aqui: virou uma aba própria (BuscaScreen); por isso a barra de topo é só o título.
@OptIn(ExperimentalMaterial3Api::class) // exigido para usar a TopAppBar, ainda experimental no Material 3
@Composable
fun CatalogoScreen(
    viewModel: CatalogoViewModel,          // fonte do estado e das ações; injetado pela Factory quando a tela é criada
    onJogoClick: (String) -> Unit = {},    // chamado com o id do jogo tocado (abrir detalhes); padrão = nada
    modifier: Modifier = Modifier          // ajustes externos; padrão = nenhum
) {
    // observa o StateFlow do ViewModel: sempre que o estado muda, esta função é recomposta com o novo valor
    val uiState by viewModel.uiState.collectAsState()

    // modo de visão: false = lista (padrão), true = grade de calendário. rememberSaveable sobrevive à rotação.
    var modoGrade by rememberSaveable { mutableStateOf(false) }

    // Scaffold monta a estrutura da tela: barra de topo fixa + área de conteúdo abaixo dela
    Scaffold(
        modifier = modifier,
        // zera os insets aqui porque esta tela roda dentro do Scaffold externo (GameCountdownApp), que já os aplica
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Catálogo") }, // barra com o título da tela
                actions = {
                    // botão que alterna entre lista e grade de calendário (no canto superior direito)
                    IconButton(onClick = { modoGrade = !modoGrade }) {
                        if (modoGrade) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Ver em lista")
                        } else {
                            Icon(Icons.Filled.DateRange, contentDescription = "Ver em calendário")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        // innerPadding é o espaço reservado pela barra de topo; é repassado ao conteúdo para ele não ficar atrás dela
        CatalogoConteudo(
            jogos = uiState.jogos,                          // lista de jogos (com dias) a exibir
            modoGrade = modoGrade,                          // se true, mostra a grade de calendário em vez da lista
            filtro = uiState.filtro,                        // filtro atual, para a FilterBar refletir o estado
            ordenacao = uiState.ordenacao,                  // ordenação atual, idem
            onFiltroChange = viewModel::aplicarFiltro,      // ao mudar um filtro, chama o método do ViewModel
            onOrdenacaoChange = viewModel::aplicarOrdenacao, // ao mudar a ordenação, idem
            onAlternarWatched = viewModel::alternarWatched, // toca o "+"/"✓" da tile: adiciona ou remove da Lista Pessoal
            onJogoClick = onJogoClick,                      // repassa o toque num card para quem chamou a tela
            modifier = Modifier.padding(innerPadding)       // aplica o espaço da barra de topo
        )
    }
}

// CatalogoConteudo: o corpo da tela "sem estado" — recebe tudo por parâmetro e só desenha + emite callbacks.
// não conhece o ViewModel, o que o torna fácil de visualizar no @Preview e de testar isoladamente.
@Composable
private fun CatalogoConteudo(
    jogos: List<JogoCatalogo>,                       // jogos já filtrados/ordenados, com os dias
    modoGrade: Boolean,                              // true = grade de calendário; false = lista
    filtro: FiltroCatalogo,                          // filtro atual (para a FilterBar)
    ordenacao: CriterioOrdenacao,                    // ordenação atual (para a FilterBar)
    onFiltroChange: (FiltroCatalogo) -> Unit,        // emite o novo filtro
    onOrdenacaoChange: (CriterioOrdenacao) -> Unit,  // emite a nova ordenação
    onAlternarWatched: (String) -> Unit,             // emite o id do jogo cujo "+"/"✓" foi tocado
    onJogoClick: (String) -> Unit,                   // emite o id do jogo tocado
    modifier: Modifier = Modifier                    // ajustes externos (aqui: o padding da barra de topo)
) {
    // Column empilha a barra de filtros no topo e, abaixo, a visão escolhida (grade ou lista)
    Column(modifier = modifier) {
        // barra de filtros/ordenação; vale para as DUAS visões (o calendário também respeita os filtros)
        FilterBar(
            filtro = filtro,
            ordenacao = ordenacao,
            onFiltroChange = onFiltroChange,
            onOrdenacaoChange = onOrdenacaoChange
        )

        when {
            // visão em grade: passa os jogos (só o Game, sem os dias) para o Calendário
            modoGrade -> {
                Calendario(
                    jogos = jogos.map { it.game },
                    onJogoClick = onJogoClick
                )
            }
            // visão em lista, sem nada: mostra a mensagem de vazio
            jogos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),      // ocupa todo o espaço restante abaixo da barra
                    contentAlignment = Alignment.Center     // centraliza a mensagem
                ) {
                    Text(
                        text = "Nenhum jogo encontrado",              // texto do estado vazio
                        style = MaterialTheme.typography.bodyLarge    // estilo de corpo de texto
                    )
                }
            }
            // visão em lista com jogos: a LazyColumn de cards
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),              // respiro ao redor da lista
                    verticalArrangement = Arrangement.spacedBy(12.dp)   // espaço fixo entre os cards
                ) {
                    // um GameCard por jogo; a 'key' pelo id ajuda o Compose a reaproveitar itens ao reordenar/filtrar
                    items(jogos, key = { it.game.id }) { item ->
                        GameCard(
                            game = item.game,                          // o jogo a exibir
                            dias = item.dias,                          // o countdown já calculado (vindo do estado)
                            onClick = { onJogoClick(item.game.id) },   // ao tocar, avisa com o id do jogo
                            trailing = {
                                // "+" para adicionar à Lista Pessoal sem abrir Detalhes; vira "✓" quando já está lá
                                // (item 6 do feedback de Igor); reusa alternarWatched, que já existe no ViewModel
                                IconButton(onClick = { onAlternarWatched(item.game.id) }) {
                                    if (item.game.isWatched) {
                                        Icon(Icons.Filled.Check, contentDescription = "Já está na lista pessoal")
                                    } else {
                                        Icon(Icons.Filled.Add, contentDescription = "Adicionar à lista pessoal")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// @Preview: renderiza o CONTEÚDO da tela (a parte sem estado) com dados fictícios, sem precisar de ViewModel.
@Preview
@Composable
private fun CatalogoConteudoPreview() {
    // dois jogos fictícios só para visualizar a lista no editor
    val exemplos = listOf(
        JogoCatalogo(
            game = Game(
                id = "1", title = "Iron Protocol", releaseDate = "2026-07-10",
                platforms = listOf(Platform.XBOX_SERIES, Platform.PC), genres = listOf(Genre.ACTION),
                developer = "Apex Studios", synopsis = "", coverUrl = "",
                priceUsd = 59.99, priceBrl = 299.90, trailerId = null, preSaleDate = null, anticipationScore = 87
            ),
            dias = 5L // countdown iminente
        ),
        JogoCatalogo(
            game = Game(
                id = "2", title = "Hearthfall", releaseDate = "2026-08-15",
                platforms = listOf(Platform.PS5, Platform.PC), genres = listOf(Genre.RPG),
                developer = "Crimson Forge", synopsis = "", coverUrl = "",
                priceUsd = 69.99, priceBrl = 349.90, trailerId = null, preSaleDate = null, anticipationScore = 92,
                isWatched = true // já está na Lista Pessoal — exercita o ícone "✓" no preview
            ),
            dias = 42L // countdown normal
        )
    )
    CatalogoConteudo(
        jogos = exemplos,
        modoGrade = false, // preview na visão de lista
        filtro = FiltroCatalogo(),
        ordenacao = CriterioOrdenacao.MAIS_PROXIMOS,
        onFiltroChange = {},
        onOrdenacaoChange = {},
        onAlternarWatched = {},
        onJogoClick = {}
    )
}
