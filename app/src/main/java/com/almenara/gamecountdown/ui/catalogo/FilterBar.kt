package com.almenara.gamecountdown.ui.catalogo // pacote da feature de catálogo (a FilterBar é específica desta tela)

import androidx.compose.foundation.horizontalScroll // permite rolar a linha de botões na horizontal quando não cabem
import androidx.compose.foundation.layout.Arrangement // define o espaçamento entre os botões da barra
import androidx.compose.foundation.layout.Row // organiza os botões de filtro/ordenação lado a lado
import androidx.compose.foundation.layout.padding // aplica espaçamento ao redor da barra
import androidx.compose.foundation.rememberScrollState // guarda a posição de rolagem horizontal entre recomposições
import androidx.compose.material3.DropdownMenu // menu suspenso que aparece ao tocar um botão
import androidx.compose.material3.DropdownMenuItem // cada opção clicável dentro do menu suspenso
import androidx.compose.material3.OutlinedButton // botão com contorno, usado como "gatilho" de cada menu
import androidx.compose.material3.Text // componente que desenha um texto na tela
import androidx.compose.runtime.Composable // anotação que marca uma função como componente de UI do Compose
import androidx.compose.runtime.getValue // habilita ler a variável de estado com 'by' (delegação)
import androidx.compose.runtime.mutableStateOf // cria um estado observável (aqui: menu aberto/fechado)
import androidx.compose.runtime.remember // preserva um valor entre recomposições (o estado do menu)
import androidx.compose.runtime.setValue // habilita escrever na variável de estado com 'by' (delegação)
import androidx.compose.ui.Modifier // objeto que descreve ajustes de layout/aparência, repassado de fora pra dentro
import androidx.compose.ui.tooling.preview.Preview // permite visualizar o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de medida de distância independente de densidade de tela
import com.almenara.gamecountdown.data.model.Genre // enum de gêneros; usa o displayName próprio nos itens do menu
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas; usa o displayName próprio nos itens do menu
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação, vem do Service
import com.almenara.gamecountdown.data.service.FiltroCatalogo // agrupador de filtros, vem do Service
import com.almenara.gamecountdown.data.service.PeriodoLancamento // enum de janelas de lançamento, vem do Service

// função de apoio: traduz cada janela de período no rótulo amigável exibido no menu.
// fica na UI (não no enum) por decisão de Igor — mantém os enums do Service livres de texto de apresentação.
// é 'internal' para ser testável; o 'when' exaustivo garante, em tempo de compilação, que todo valor tem rótulo.
internal fun rotuloPeriodo(periodo: PeriodoLancamento): String = when (periodo) {
    PeriodoLancamento.SEMANA -> "Esta semana"        // próximos 7 dias
    PeriodoLancamento.MES -> "Este mês"              // próximos 30 dias
    PeriodoLancamento.TRIMESTRE -> "Próximos 3 meses" // próximos 90 dias
    PeriodoLancamento.SEMESTRE -> "Próximos 6 meses"  // próximos 180 dias
    PeriodoLancamento.ANO -> "Este ano"              // próximos 365 dias
}

// função de apoio: traduz cada critério de ordenação no rótulo amigável exibido no menu (mesma justificativa acima)
internal fun rotuloOrdenacao(ordenacao: CriterioOrdenacao): String = when (ordenacao) {
    CriterioOrdenacao.MAIS_AGUARDADOS -> "Mais aguardados" // ordena pelo anticipationScore
    CriterioOrdenacao.MAIS_PROXIMOS -> "Mais próximos"     // ordena pela data de lançamento
    CriterioOrdenacao.ALFABETICA -> "A–Z"                  // ordena pelo título
}

// componente genérico e privado: um botão que abre um menu suspenso de opções.
// <T> = o tipo do valor de cada opção (Platform?, Genre?, PeriodoLancamento? ou CriterioOrdenacao).
// é reutilizado pelos quatro controles da barra, evitando repetir a mesma estrutura de botão+menu quatro vezes.
@Composable
private fun <T> FilterDropdown(
    textoBotao: String,                 // texto no botão: o valor selecionado, ou o nome da dimensão se nada selecionado
    opcoes: List<Pair<T, String>>,      // lista de opções; cada uma é (valor, rótulo a exibir)
    onSelecionar: (T) -> Unit,          // chamado com o valor escolhido quando o usuário toca uma opção
    modifier: Modifier = Modifier       // ajustes externos; padrão = nenhum
) {
    // estado local de UI: o menu está aberto ou fechado? não é regra de negócio, então vive no próprio componente
    var aberto by remember { mutableStateOf(false) }

    // IMPORTANTE: o botão e o menu precisam ficar juntos DENTRO de um Box.
    // é o Box que serve de âncora — o DropdownMenu aparece logo abaixo dele (do botão).
    // sem o Box, os dois viravam filhos soltos da Row da barra: o menu não ancorava no botão
    // e o espaçamento entre os filtros mudava ao abrir (cada menu contava como um item extra na Row).
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { aberto = true } // tocar o botão abre o menu
        ) {
            Text(text = "$textoBotao ▾") // o "▾" indica visualmente que o botão abre um menu
        }

        // o menu só é composto/exibido quando 'aberto' é true; ancora no Box acima (abaixo do botão)
        DropdownMenu(
            expanded = aberto,             // controla a visibilidade pelo estado local
            onDismissRequest = { aberto = false } // tocar fora fecha o menu sem escolher nada
        ) {
            // cria um item clicável para cada opção recebida
            opcoes.forEach { (valor, rotulo) ->
                DropdownMenuItem(
                    text = { Text(rotulo) },          // o texto exibido na linha do menu
                    onClick = {
                        onSelecionar(valor)           // avisa o chamador do valor escolhido
                        aberto = false                // e fecha o menu
                    }
                )
            }
        }
    }
}

// FilterBar: a barra de filtros e ordenação da tela de Catálogo.
// É "sem estado de negócio": recebe o filtro/ordenação atuais e apenas EMITE o novo valor via callback —
// quem guarda e aplica o estado é o CatalogoViewModel. Isso mantém a barra reutilizável e previsível.
@Composable
fun FilterBar(
    filtro: FiltroCatalogo,                          // filtro atualmente aplicado (vindo do CatalogoUiState)
    ordenacao: CriterioOrdenacao,                    // ordenação atualmente aplicada (vindo do CatalogoUiState)
    onFiltroChange: (FiltroCatalogo) -> Unit,        // chamado com o novo filtro quando o usuário muda algum menu
    onOrdenacaoChange: (CriterioOrdenacao) -> Unit,  // chamado com a nova ordenação quando o usuário a troca
    modifier: Modifier = Modifier                    // ajustes externos; padrão = nenhum
) {
    // Row rolável na horizontal: em telas estreitas os quatro botões podem não caber, então permitem rolagem
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())        // habilita a rolagem horizontal
            .padding(horizontal = 12.dp, vertical = 8.dp),  // respiro nas bordas da barra
        horizontalArrangement = Arrangement.spacedBy(8.dp)  // espaço fixo entre os botões
    ) {
        // 1) Filtro de PLATAFORMA — opção "Todas" (null) limpa o filtro; demais aplicam a plataforma escolhida
        FilterDropdown(
            textoBotao = filtro.plataforma?.displayName ?: "Plataforma", // mostra a selecionada, ou o nome da dimensão
            // lista de opções: primeiro "Todas" (valor null), depois uma por plataforma com seu displayName
            opcoes = buildList {
                add(null to "Todas")                                    // limpa o filtro de plataforma
                Platform.entries.forEach { add(it to it.displayName) }  // uma opção por plataforma
            },
            onSelecionar = { plataforma -> onFiltroChange(filtro.copy(plataforma = plataforma)) } // troca só a plataforma
        )

        // 2) Filtro de GÊNERO — "Todos" (null) limpa; demais aplicam o gênero escolhido
        FilterDropdown(
            textoBotao = filtro.genero?.displayName ?: "Gênero",
            opcoes = buildList {
                add(null to "Todos")
                Genre.entries.forEach { add(it to it.displayName) }
            },
            onSelecionar = { genero -> onFiltroChange(filtro.copy(genero = genero)) }
        )

        // 3) Filtro de PERÍODO — "Qualquer" (null) limpa; demais aplicam a janela de lançamento escolhida
        FilterDropdown(
            textoBotao = filtro.periodoLancamento?.let { rotuloPeriodo(it) } ?: "Período",
            opcoes = buildList {
                add(null to "Qualquer")
                PeriodoLancamento.entries.forEach { add(it to rotuloPeriodo(it)) } // usa o rótulo amigável, não o nome do enum
            },
            onSelecionar = { periodo -> onFiltroChange(filtro.copy(periodoLancamento = periodo)) }
        )

        // 4) ORDENAÇÃO — sempre há um valor ativo, então o botão mostra o critério atual (sem opção "nenhum")
        FilterDropdown(
            textoBotao = rotuloOrdenacao(ordenacao),
            opcoes = CriterioOrdenacao.entries.map { it to rotuloOrdenacao(it) }, // uma opção por critério
            onSelecionar = { novaOrdenacao -> onOrdenacaoChange(novaOrdenacao) }
        )
    }
}

// @Preview: renderiza a barra isolada no Android Studio, com filtro vazio e ordenação padrão, sem rodar o app
@Preview
@Composable
private fun FilterBarPreview() {
    FilterBar(
        filtro = FiltroCatalogo(),                       // nenhum filtro aplicado
        ordenacao = CriterioOrdenacao.MAIS_PROXIMOS,     // ordenação padrão
        onFiltroChange = {},                             // no preview os callbacks não fazem nada
        onOrdenacaoChange = {}
    )
}
