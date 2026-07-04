package com.almenara.gamecountdown.ui.catalogo // mesmo pacote da Factory sendo testada

import androidx.lifecycle.ViewModel // usado para simular uma classe de ViewModel "desconhecida" no teste
import com.almenara.gamecountdown.data.model.Game // modelo de dados de um jogo, usado no fake abaixo
import com.almenara.gamecountdown.data.service.CriterioOrdenacao // enum de ordenação, exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.FiltroCatalogo // agrupador de filtros, exigido pela assinatura do fake
import com.almenara.gamecountdown.data.service.GameService // contrato que o fake abaixo implementa
import org.junit.Assert.assertNotNull // verifica se um valor não é nulo
import org.junit.Assert.assertThrows // verifica se um bloco de código lança a exceção esperada
import org.junit.Test // marca um método como caso de teste

// fake mínimo de GameService, só para não depender do Mock/Impl reais neste teste da Factory
// nome diferente de "FakeGameService" (usado em CatalogoViewModelTest.kt) porque, no mesmo pacote,
// duas classes privadas com o mesmo nome geram arquivos .class conflitantes, mesmo em arquivos separados
private class FakeGameServiceVazio : GameService {
    override fun getGames(filtro: FiltroCatalogo, ordenacao: CriterioOrdenacao): List<Game> = emptyList()
    override fun getGameById(id: String): Game? = null
    override fun searchGames(query: String): List<Game> = emptyList()
    override fun getWatchedGames(): List<Game> = emptyList()
    override fun setWatched(id: String, watched: Boolean) {}
    override fun getDaysUntilRelease(game: Game): Long = 0L
}

// classe de ViewModel "desconhecida", usada só para provar que a Factory rejeita tipos que não sabe criar
private class OutroViewModel : ViewModel()

// classe de testes da CatalogoViewModelFactory
class CatalogoViewModelFactoryTest {

    // pedir um CatalogoViewModel à Factory deve devolver uma instância criada e funcional
    @Test
    fun `create devolve uma instancia de CatalogoViewModel`() {
        val factory = CatalogoViewModelFactory(FakeGameServiceVazio())

        val viewModel = factory.create(CatalogoViewModel::class.java)

        assertNotNull(viewModel)
    }

    // pedir qualquer outro tipo de ViewModel deve falhar de forma explícita, em vez de devolver algo errado
    @Test
    fun `create lanca excecao para uma classe de ViewModel desconhecida`() {
        val factory = CatalogoViewModelFactory(FakeGameServiceVazio())

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(OutroViewModel::class.java)
        }
    }
}
