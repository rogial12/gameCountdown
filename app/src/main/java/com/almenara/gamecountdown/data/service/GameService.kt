package com.almenara.gamecountdown.data.service // pacote onde vive a camada de regras de negócio (Service)

import com.almenara.gamecountdown.data.model.Game // importa Game para uso nas assinaturas dos métodos
import com.almenara.gamecountdown.data.model.Genre // importa Genre para uso no filtro de catálogo
import com.almenara.gamecountdown.data.model.Platform // importa Platform para uso no filtro de catálogo

// enum: janelas de tempo usadas no filtro "lançamento iminente" do catálogo (spec: semana/mês/trimestre/semestre/ano)
enum class PeriodoLancamento {
    SEMANA,     // jogos que lançam nos próximos 7 dias
    MES,        // jogos que lançam nos próximos 30 dias
    TRIMESTRE,  // jogos que lançam nos próximos 90 dias
    SEMESTRE,   // jogos que lançam nos próximos 180 dias
    ANO         // jogos que lançam nos próximos 365 dias
}

// enum: critérios de ordenação da lista de jogos, conforme definido na spec da Fase 1
enum class CriterioOrdenacao {
    MAIS_AGUARDADOS, // ordena pelo anticipationScore, do maior para o menor
    MAIS_PROXIMOS,   // ordena pela releaseDate, da mais próxima para a mais distante
    ALFABETICA       // ordena pelo title, em ordem alfabética
}

// data class: agrupa os filtros opcionais do catálogo num único parâmetro
// todos nulos por padrão = nenhum filtro aplicado, retorna o catálogo inteiro
data class FiltroCatalogo(
    val plataforma: Platform? = null,           // filtra por uma plataforma específica; null = todas
    val genero: Genre? = null,                  // filtra por um gênero específico; null = todos
    val periodoLancamento: PeriodoLancamento? = null // filtra por janela de lançamento; null = qualquer data
)

// interface: define o contrato da camada de regras de negócio
// a UI/ViewModel só conhece este contrato, nunca o Repository diretamente —
// isso mantém a lógica de filtro/ordenação/countdown fora da UI e fora do Repository
interface GameService {

    // retorna o catálogo filtrado e ordenado conforme os parâmetros recebidos
    fun getGames(
        filtro: FiltroCatalogo = FiltroCatalogo(),           // filtros a aplicar; padrão = nenhum
        ordenacao: CriterioOrdenacao = CriterioOrdenacao.MAIS_PROXIMOS // critério de ordenação; padrão = mais próximos
    ): List<Game>

    fun getGameById(id: String): Game?            // busca um jogo específico pelo ID; repassa ao Repository

    fun searchGames(query: String): List<Game>    // busca por texto no título; repassa ao Repository

    // retorna a lista pessoal ("de olho") do usuário, já filtrada e ordenada conforme os parâmetros recebidos
    // (mesmos filtro/ordenação do catálogo — a Lista Pessoal também precisa desses controles, ver Igor)
    fun getWatchedGames(
        filtro: FiltroCatalogo = FiltroCatalogo(),
        ordenacao: CriterioOrdenacao = CriterioOrdenacao.MAIS_PROXIMOS
    ): List<Game>

    fun setWatched(id: String, watched: Boolean)  // adiciona ou remove um jogo da lista pessoal

    fun getDaysUntilRelease(game: Game): Long      // calcula quantos dias faltam para o lançamento (o "countdown")
}
