package com.almenara.gamecountdown.data.service // mesmo pacote da interface GameService

import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo
import com.almenara.gamecountdown.data.repository.GameRepository // interface da camada de dados, injetada aqui
import java.time.Clock // relógio abstrato: permite "congelar" a data atual nos testes
import java.time.LocalDate // representa uma data (ano-mês-dia), sem hora, do java.time
import java.time.temporal.ChronoUnit // fornece a unidade DAYS para calcular diferença entre datas

// implementação real das regras de negócio, usando o GameRepository injetado no construtor
// GameServiceImpl : GameService diz que esta classe cumpre o contrato definido na interface
class GameServiceImpl(
    private val repository: GameRepository, // fonte dos dados; pode ser o Mock (Fase 2) ou a API real (Fase 3+)
    private val clock: Clock = Clock.systemDefaultZone() // relógio usado para saber "hoje";
    // tem um padrão (relógio real do sistema) para uso normal do app,
    // mas pode ser substituído por um relógio fixo nos testes, para que o resultado não dependa da data em que o teste roda
) : GameService {

    // retorna o catálogo já filtrado e ordenado
    override fun getGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> =
        aplicarFiltroEOrdenacao(repository.getGames(), filtro, ordenacao) // começa do catálogo completo do Repository

    // retorna a lista pessoal já filtrada e ordenada — mesma regra do catálogo, aplicada só sobre os jogos "de olho"
    override fun getWatchedGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> =
        aplicarFiltroEOrdenacao(repository.getWatchedGames(), filtro, ordenacao) // começa só dos jogos observados

    // função privada compartilhada: aplica filtro de plataforma/gênero/período e a ordenação escolhida
    // sobre QUALQUER lista de jogos recebida — extraída para não duplicar a regra entre getGames e getWatchedGames
    private fun aplicarFiltroEOrdenacao(
        jogos: List<Game>,
        filtro: FiltroCatalogo,
        ordenacao: CriterioOrdenacao
    ): List<Game> {
        var resultado = jogos

        // se um filtro de plataforma foi definido, mantém só os jogos que a contêm na lista de plataformas
        filtro.plataforma?.let { plataforma ->
            resultado = resultado.filter { plataforma in it.platforms }
        }

        // se um filtro de gênero foi definido, mantém só os jogos que o contêm na lista de gêneros
        filtro.genero?.let { genero ->
            resultado = resultado.filter { genero in it.genres }
        }

        // se um filtro de período foi definido, mantém só os jogos que lançam entre hoje e o limite da janela
        filtro.periodoLancamento?.let { periodo ->
            val hoje = LocalDate.now(clock) // data de hoje, segundo o relógio injetado
            val limite = hoje.plusDays(periodo.emDias()) // data limite da janela (ex.: hoje + 7 dias para SEMANA)
            resultado = resultado.filter { jogo ->
                val dataLancamento = LocalDate.parse(jogo.releaseDate) // converte a String ISO em LocalDate
                // !isBefore(hoje): exclui jogos já lançados; !isAfter(limite): exclui jogos além da janela
                !dataLancamento.isBefore(hoje) && !dataLancamento.isAfter(limite)
            }
        }

        // aplica a ordenação escolhida por último, depois de todos os filtros
        resultado = when (ordenacao) {
            CriterioOrdenacao.MAIS_AGUARDADOS -> resultado.sortedByDescending { it.anticipationScore } // maior score primeiro
            CriterioOrdenacao.MAIS_PROXIMOS -> resultado.sortedBy { LocalDate.parse(it.releaseDate) }  // data mais próxima primeiro
            CriterioOrdenacao.ALFABETICA -> resultado.sortedBy { it.title }                            // ordem alfabética do título
        }

        return resultado
    }

    // os três métodos abaixo não têm regra de negócio própria — apenas repassam a chamada ao Repository
    // eles existem no Service (em vez da UI chamar o Repository direto) para manter a UI desacoplada da fonte de dados
    override fun getGameById(id: String): Game? = repository.getGameById(id)

    override fun searchGames(query: String): List<Game> = repository.searchGames(query)

    override fun setWatched(id: String, watched: Boolean) = repository.setWatched(id, watched)

    // calcula quantos dias faltam entre hoje e a data de lançamento do jogo (pode ser negativo se já lançou)
    override fun getDaysUntilRelease(game: Game): Long {
        val hoje = LocalDate.now(clock) // data de hoje, segundo o relógio injetado
        val lancamento = LocalDate.parse(game.releaseDate) // converte a String ISO em LocalDate
        return ChronoUnit.DAYS.between(hoje, lancamento) // diferença em dias entre as duas datas
    }

    // função privada de extensão: converte cada período nomeado em uma quantidade de dias
    // fica encapsulada aqui porque é um detalhe de implementação do filtro, não parte do contrato da interface
    private fun PeriodoLancamento.emDias(): Long = when (this) {
        PeriodoLancamento.SEMANA -> 7L
        PeriodoLancamento.MES -> 30L
        PeriodoLancamento.TRIMESTRE -> 90L
        PeriodoLancamento.SEMESTRE -> 180L
        PeriodoLancamento.ANO -> 365L
    }
}
