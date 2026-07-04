package com.almenara.gamecountdown.ui.comum // pacote dos componentes visuais compartilhados entre telas

import androidx.compose.foundation.background // pinta fundo (scrim do número, selo "+N")
import androidx.compose.foundation.border // desenha a moldura do dia atual
import androidx.compose.foundation.clickable // torna um dia com lançamento tocável
import androidx.compose.foundation.layout.Arrangement // espaçamento entre elementos
import androidx.compose.foundation.layout.Box // empilha/centraliza (célula do dia, sobreposições)
import androidx.compose.foundation.layout.Column // empilha cabeçalho + grade verticalmente
import androidx.compose.foundation.layout.Row // organiza os 7 dias da semana lado a lado
import androidx.compose.foundation.layout.Spacer // espaço fixo entre seções
import androidx.compose.foundation.layout.aspectRatio // deixa a célula do dia quadrada
import androidx.compose.foundation.layout.fillMaxSize // preenche a célula
import androidx.compose.foundation.layout.fillMaxWidth // ocupa a largura toda
import androidx.compose.foundation.layout.height // altura de espaçadores
import androidx.compose.foundation.layout.padding // espaçamentos internos
import androidx.compose.foundation.shape.CircleShape // recorte circular da capa e da moldura de hoje
import androidx.compose.material.icons.Icons // ícones do Material
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft // seta "mês anterior"
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight // seta "próximo mês"
import androidx.compose.material3.ExperimentalMaterial3Api // ModalBottomSheet é API experimental do Material 3
import androidx.compose.material3.Icon // desenha um ícone
import androidx.compose.material3.IconButton // botão só com ícone (as setas)
import androidx.compose.material3.MaterialTheme // tema (cores, tipografia)
import androidx.compose.material3.ModalBottomSheet // painel inferior que sobe ao tocar um dia
import androidx.compose.material3.Text // desenha texto
import androidx.compose.runtime.Composable // marca função como componente de UI
import androidx.compose.runtime.getValue // ler estado com 'by'
import androidx.compose.runtime.mutableStateOf // cria estado observável (mês exibido, dia selecionado)
import androidx.compose.runtime.remember // preserva estado entre recomposições
import androidx.compose.runtime.setValue // escrever estado com 'by'
import androidx.compose.ui.Alignment // alinhamentos
import androidx.compose.ui.Modifier // ajustes de layout/aparência
import androidx.compose.ui.draw.clip // recorta conteúdo numa forma
import androidx.compose.ui.graphics.Color // cores fixas (branco do número, scrim)
import androidx.compose.ui.text.font.FontWeight // negrito
import androidx.compose.ui.text.style.TextAlign // centralização de texto
import androidx.compose.ui.tooling.preview.Preview // preview no editor
import androidx.compose.ui.unit.dp // unidade de distância
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre // usado no @Preview
import com.almenara.gamecountdown.data.model.Platform // usado no @Preview
import java.time.LocalDate // data de hoje e das capas
import java.time.YearMonth // mês exibido no calendário
import java.time.temporal.ChronoUnit // calcula os dias até o lançamento (para o card do bottom sheet)

// nomes dos meses em português, para o cabeçalho (ex.: "Julho 2026")
private val mesesPt = arrayOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
)

// Calendario: a visão de grade mensal, compartilhada por Catálogo e Lista Pessoal.
// recebe a lista de jogos JÁ FILTRADA pela tela; agrupa por dia (via jogosPorDiaDoMes) e desenha a grade do mês.
// guarda internamente o mês exibido e o dia selecionado (estado de UI, não de negócio).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendario(
    jogos: List<Game>,                 // jogos a posicionar no calendário (já respeitando os filtros da tela)
    onJogoClick: (String) -> Unit,     // chamado com o id do jogo tocado no bottom sheet do dia
    modifier: Modifier = Modifier,     // ajustes externos
    hoje: LocalDate = LocalDate.now()  // data de hoje (para destacar o dia atual); parametrizável para testes/preview
) {
    // mês atualmente exibido; começa no mês de "hoje"
    var mes by remember { mutableStateOf(YearMonth.from(hoje)) }
    // dia atualmente selecionado (abre o bottom sheet); null = nenhum
    var diaSelecionado by remember { mutableStateOf<Int?>(null) }

    // agrupa os jogos por dia do mês exibido (lógica pura já testada no Passo 20)
    val porDia = jogosPorDiaDoMes(jogos, mes)

    Column(modifier = modifier.fillMaxWidth().padding(12.dp)) {
        // cabeçalho: setas de navegação + "Mês Ano"
        CabecalhoMes(
            mes = mes,
            onAnterior = { mes = mes.minusMonths(1); diaSelecionado = null }, // volta um mês, fecha seleção
            onProximo = { mes = mes.plusMonths(1); diaSelecionado = null }     // avança um mês
        )
        Spacer(Modifier.height(8.dp))
        // a grade de dias em si
        GradeMes(mes = mes, porDia = porDia, hoje = hoje, onDiaClick = { dia -> diaSelecionado = dia })
    }

    // se um dia com lançamentos foi tocado, mostra o bottom sheet com os jogos daquele dia
    val dia = diaSelecionado
    if (dia != null) {
        ModalBottomSheet(onDismissRequest = { diaSelecionado = null }) {
            SheetDoDia(
                mes = mes,
                dia = dia,
                jogos = porDia[dia].orEmpty(),
                hoje = hoje,
                onJogoClick = { id -> diaSelecionado = null; onJogoClick(id) } // fecha o sheet e navega
            )
        }
    }
}

// cabeçalho do calendário: seta anterior, "Mês Ano" centralizado, seta próximo
@Composable
private fun CabecalhoMes(mes: YearMonth, onAnterior: () -> Unit, onProximo: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onAnterior) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
        }
        Text(
            text = "${mesesPt[mes.monthValue - 1]} ${mes.year}", // ex.: "Julho 2026"
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onProximo) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês")
        }
    }
}

// a grade do mês: cabeçalho dos dias da semana (domingo primeiro) + as semanas com as células de cada dia
@Composable
private fun GradeMes(mes: YearMonth, porDia: Map<Int, List<Game>>, hoje: LocalDate, onDiaClick: (Int) -> Unit) {
    val diasSemana = listOf("D", "S", "T", "Q", "Q", "S", "S") // domingo -> sábado
    // quantos "vazios" antes do dia 1 (para o dia 1 cair na coluna certa). domingo = 0
    val offset = mes.atDay(1).dayOfWeek.value % 7
    val totalDias = mes.lengthOfMonth()
    // monta a lista de células: vazios iniciais + dias 1..N + vazios finais até fechar a última semana
    val celulas = buildList<Int?> {
        repeat(offset) { add(null) }
        for (d in 1..totalDias) add(d)
        while (size % 7 != 0) add(null)
    }
    val semanas = celulas.chunked(7)          // divide em semanas de 7 dias
    val ehMesAtual = YearMonth.from(hoje) == mes // só destaca "hoje" se o mês exibido é o corrente

    Column {
        // cabeçalho com as iniciais dos dias da semana
        Row(Modifier.fillMaxWidth()) {
            diasSemana.forEach { inicial ->
                Text(
                    text = inicial,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        // uma Row por semana
        semanas.forEach { semana ->
            Row(Modifier.fillMaxWidth()) {
                semana.forEach { dia ->
                    CelulaDia(
                        modifier = Modifier.weight(1f),
                        dia = dia,
                        jogosDoDia = dia?.let { porDia[it] },
                        ehHoje = ehMesAtual && dia == hoje.dayOfMonth,
                        onClick = { if (dia != null && porDia[dia] != null) onDiaClick(dia) }
                    )
                }
            }
        }
    }
}

// uma célula de dia. Três casos: vazia (fora do mês), dia sem lançamento (só o número),
// e dia com lançamento (capa em círculo + número + selo "+N"). "Hoje" ganha uma moldura.
@Composable
private fun CelulaDia(
    modifier: Modifier,
    dia: Int?,
    jogosDoDia: List<Game>?,
    ehHoje: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = modifier.aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
        if (dia == null) return@Box // célula vazia (dias de preenchimento antes/depois do mês)

        // moldura aplicada quando é o dia de hoje
        val molduraHoje = if (ehHoje) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier

        if (jogosDoDia != null && jogosDoDia.isNotEmpty()) {
            val destaque = jogosDoDia.first() // maior interesse do dia (já ordenado)
            Box(
                modifier = Modifier.fillMaxSize().clip(CircleShape).then(molduraHoje).clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                // capa do jogo em destaque, recortada em círculo
                GameCover(
                    coverUrl = destaque.coverUrl,
                    title = destaque.title,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
                // número do dia no topo, com um pequeno fundo escuro para ficar legível sobre a imagem
                Text(
                    text = dia.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .padding(horizontal = 5.dp)
                )
                // selo "+N" quando há mais de um lançamento no dia
                if (jogosDoDia.size > 1) {
                    Text(
                        text = "+${jogosDoDia.size - 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        } else {
            // dia sem lançamento: só o número, mais apagado, sem interação
            Box(modifier = Modifier.fillMaxSize().then(molduraHoje), contentAlignment = Alignment.Center) {
                Text(
                    text = dia.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// conteúdo do bottom sheet: título do dia + a lista de jogos daquele dia (como GameCard)
@Composable
private fun SheetDoDia(mes: YearMonth, dia: Int, jogos: List<Game>, hoje: LocalDate, onJogoClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "$dia de ${mesesPt[mes.monthValue - 1]}", // ex.: "10 de Julho"
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        jogos.forEach { game ->
            // calcula os dias até o lançamento (a data já é válida — veio do agrupamento)
            val dias = ChronoUnit.DAYS.between(hoje, LocalDate.parse(game.releaseDate))
            GameCard(game = game, dias = dias, onClick = { onJogoClick(game.id) })
        }
    }
}

// @Preview: mês fixo (julho/2026) com alguns jogos, para visualizar a grade no editor
@Preview
@Composable
private fun CalendarioPreview() {
    val jogos = listOf(
        gameFakePreview("1", "Iron Protocol", "2026-07-10", 87),
        gameFakePreview("2", "Verdant Rift", "2026-07-28", 74),
        gameFakePreview("3", "Outro Jogo", "2026-07-10", 50) // mesmo dia do "1" -> gera "+1"
    )
    Calendario(jogos = jogos, onJogoClick = {}, hoje = LocalDate.of(2026, 7, 15))
}

// jogo fictício só para o @Preview
private fun gameFakePreview(id: String, title: String, releaseDate: String, score: Int): Game = Game(
    id = id, title = title, releaseDate = releaseDate,
    platforms = listOf(Platform.PC), genres = listOf(Genre.ACTION),
    developer = "Studio", synopsis = "", coverUrl = "",
    priceUsd = null, priceBrl = null, trailerId = null, preSaleDate = null, anticipationScore = score
)
