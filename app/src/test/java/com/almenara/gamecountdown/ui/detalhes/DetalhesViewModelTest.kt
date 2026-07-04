package com.almenara.gamecountdown.ui.detalhes // mesmo pacote do código sendo testado

import com.almenara.gamecountdown.data.model.Game     // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre    // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.FiltroCatalogo    // exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.GameService       // interface que o fake abaixo implementa
import org.junit.Assert.assertEquals // verifica se dois valores são iguais
import org.junit.Assert.assertNull   // verifica se um valor é nulo
import org.junit.Assert.assertTrue   // verifica se uma condição é verdadeira
import org.junit.Assert.assertFalse  // verifica se uma condição é falsa
import org.junit.Test                // marca um método como caso de teste

// fake do GameService só para estes testes: guarda os jogos e um conjunto de "watched" em memória.
// devolve dias = id do jogo, e conta as chamadas de setWatched para os testes inspecionarem.
private class FakeGameService(private val jogos: List<Game>) : GameService {

    private val watched = mutableSetOf<String>() // ids observados
    private val listenersWatched = mutableListOf<() -> Unit>() // callbacks inscritos via observarMudancasWatched
    var chamadasSetWatched = 0                    // quantas vezes setWatched foi chamado

    override fun getGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> = jogos
    override fun searchGames(query: String): List<Game> = emptyList()
    override fun getWatchedGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> =
        jogos.filter { it.id in watched }

    // devolve o jogo pelo id, refletindo o estado atual de "de olho"; null se não existir
    override fun getGameById(id: String): Game? =
        jogos.find { it.id == id }?.copy(isWatched = id in watched)

    override fun setWatched(id: String, watched: Boolean) {
        chamadasSetWatched++
        if (watched) this.watched.add(id) else this.watched.remove(id)
        listenersWatched.forEach { it() }
    }

    override fun observarMudancasWatched(callback: () -> Unit): () -> Unit {
        listenersWatched.add(callback)
        return { listenersWatched.remove(callback) }
    }

    override fun getDaysUntilRelease(game: Game): Long = game.id.toLong() // dias derivados do id
}

// classe de testes do DetalhesViewModel
class DetalhesViewModelTest {

    private val jogos = listOf(
        gameFake(id = "5", title = "Starbound Legacy"),
        gameFake(id = "7", title = "Iron Protocol")
    )

    // ao criar apontando para um id existente, o estado deve conter aquele jogo e os dias calculados
    @Test
    fun `init carrega o jogo pelo id com dias`() {
        val vm = DetalhesViewModel(FakeGameService(jogos), gameId = "7")

        val estado = vm.uiState.value
        assertEquals("7", estado.game?.id)   // carregou o jogo certo
        assertEquals("Iron Protocol", estado.game?.title)
        assertEquals(7L, estado.dias)        // dias = id (segundo o fake)
    }

    // ao apontar para um id que não existe, o jogo fica nulo (a tela mostrará "não encontrado")
    @Test
    fun `init com id inexistente deixa o jogo nulo`() {
        val vm = DetalhesViewModel(FakeGameService(jogos), gameId = "999")

        val estado = vm.uiState.value
        assertNull(estado.game) // nenhum jogo com esse id
    }

    // alternarWatched deve marcar o jogo como observado e refletir isso no estado recarregado
    @Test
    fun `alternarWatched marca o jogo como observado`() {
        val vm = DetalhesViewModel(FakeGameService(jogos), gameId = "5")

        vm.alternarWatched()

        assertTrue(vm.uiState.value.game?.isWatched == true)
    }

    // chamar alternarWatched duas vezes deve voltar ao estado inicial (não observado)
    @Test
    fun `alternarWatched duas vezes desfaz a marcacao`() {
        val vm = DetalhesViewModel(FakeGameService(jogos), gameId = "5")

        vm.alternarWatched()
        vm.alternarWatched()

        assertFalse(vm.uiState.value.game?.isWatched == true)
    }

    // se o jogo não existe, alternarWatched não deve chamar o Service nem quebrar
    @Test
    fun `alternarWatched sem jogo carregado nao chama o service`() {
        val fake = FakeGameService(jogos)
        val vm = DetalhesViewModel(fake, gameId = "999") // id inexistente -> game nulo

        vm.alternarWatched()

        assertEquals(0, fake.chamadasSetWatched) // não tentou marcar nada
    }

    // reproduz o bug relatado por Igor: se o watched mudar por FORA deste ViewModel (ex.: a Lista Pessoal,
    // que compartilha a mesma instância de GameService via AppContainer), os Detalhes devem recarregar sozinhos
    @Test
    fun `detalhes recarrega sozinho quando outra tela muda o watched`() {
        val fake = FakeGameService(jogos)
        val vm = DetalhesViewModel(fake, gameId = "5")

        fake.setWatched("5", true) // simula outra tela marcando o mesmo jogo, sem passar por este ViewModel

        assertTrue(vm.uiState.value.game?.isWatched == true)
    }

    // onCleared deve cancelar a inscrição — depois disso, mudanças externas não devem mais mexer no estado
    @Test
    fun `onCleared cancela a inscricao no service`() {
        val fake = FakeGameService(jogos)
        val vm = DetalhesViewModel(fake, gameId = "5")

        vm.onCleared()
        fake.setWatched("5", true) // mudança externa após o ViewModel ter sido destruído

        assertFalse(vm.uiState.value.game?.isWatched == true) // estado ficou congelado; não recarregou
    }
}

// função de apoio: cria um Game fictício preenchendo só o essencial para estes testes
private fun gameFake(id: String, title: String): Game = Game(
    id = id,
    title = title,
    releaseDate = "2026-09-20",
    platforms = listOf(Platform.PC),
    genres = listOf(Genre.ADVENTURE),
    developer = "Fake Studio",
    synopsis = "",
    coverUrl = "",
    priceUsd = null,
    priceBrl = null,
    trailerId = null,
    preSaleDate = null
)
