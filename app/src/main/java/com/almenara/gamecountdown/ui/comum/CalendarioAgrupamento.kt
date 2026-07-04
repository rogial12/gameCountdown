package com.almenara.gamecountdown.ui.comum // pacote dos componentes visuais compartilhados entre telas

import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo
import java.time.LocalDate // representa uma data (ano-mês-dia); usada para ler o dia de cada lançamento
import java.time.YearMonth // representa um mês de um ano (ex.: julho/2026); define qual mês o calendário mostra

// função pura de apoio ao Calendário: agrupa os jogos por DIA do mês informado.
// para cada dia com lançamento, devolve a lista de jogos daquele dia JÁ ORDENADA por interesse público
// (anticipationScore, do maior para o menor) — assim o primeiro da lista é o "destaque" que a grade mostra,
// e o tamanho da lista diz quantos lançamentos há naquele dia (para o selo "+N").
// é 'internal' para ser testável por teste unitário comum, sem emulador.
internal fun jogosPorDiaDoMes(jogos: List<Game>, mes: YearMonth): Map<Int, List<Game>> {
    return jogos
        // 1) para cada jogo, tenta ler a data; mantém só os que caem no mês pedido, guardando (dia, jogo)
        .mapNotNull { game ->
            // runCatching: se a releaseDate estiver malformada, LocalDate.parse lançaria — aqui vira null (ignorado)
            val data = runCatching { LocalDate.parse(game.releaseDate) }.getOrNull()
            // só interessa se a data existe E pertence ao mês/ano exibido
            if (data != null && YearMonth.from(data) == mes) data.dayOfMonth to game else null
        }
        // 2) agrupa os pares pelo dia do mês: dia -> lista de jogos daquele dia
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
        // 3) ordena cada lista por interesse (maior anticipationScore primeiro = destaque do dia)
        .mapValues { (_, lista) -> lista.sortedByDescending { it.anticipationScore } }
}
