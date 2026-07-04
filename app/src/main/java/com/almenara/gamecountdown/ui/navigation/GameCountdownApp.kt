package com.almenara.gamecountdown.ui.navigation // pacote da navegação (costura as telas do app)

import androidx.compose.foundation.layout.padding // aplica o espaçamento que o Scaffold reserva para a barra inferior
import androidx.compose.material.icons.Icons // ponto de acesso aos ícones do Material
import androidx.compose.material.icons.automirrored.filled.List // ícone (lista) da aba Catálogo (espelha em RTL)
import androidx.compose.material.icons.filled.Favorite // ícone (coração) da aba Lista Pessoal
import androidx.compose.material3.Icon // desenha um ícone vetorial
import androidx.compose.material3.NavigationBar // a barra de navegação inferior do Material 3
import androidx.compose.material3.NavigationBarItem // cada item (aba) dentro da barra inferior
import androidx.compose.material3.Scaffold // estrutura de tela; aqui hospeda a barra inferior + o conteúdo navegável
import androidx.compose.material3.Text // desenha texto (os rótulos das abas)
import androidx.compose.runtime.Composable // marca uma função como componente de UI do Compose
import androidx.compose.runtime.getValue // habilita ler o estado observado com 'by' (delegação)
import androidx.compose.ui.Modifier // descreve ajustes de layout/aparência
import androidx.compose.ui.graphics.vector.ImageVector // tipo do ícone de cada aba
import androidx.lifecycle.viewmodel.compose.viewModel // cria/recupera o ViewModel de cada tela
import androidx.navigation.NavGraph.Companion.findStartDestination // acha a tela inicial (usado ao trocar de aba)
import androidx.navigation.NavType // tipo do argumento de rota (o id do jogo é String)
import androidx.navigation.compose.NavHost // o container que troca a tela conforme a rota atual
import androidx.navigation.compose.composable // registra uma tela (destino) no NavHost
import androidx.navigation.compose.currentBackStackEntryAsState // observa qual rota está ativa agora
import androidx.navigation.compose.rememberNavController // cria o "controle remoto" da navegação
import androidx.navigation.navArgument // declara um argumento de rota (o gameId de Detalhes)
import com.almenara.gamecountdown.ui.catalogo.CatalogoScreen // tela de Catálogo
import com.almenara.gamecountdown.ui.catalogo.CatalogoViewModel // ViewModel do Catálogo
import com.almenara.gamecountdown.ui.catalogo.CatalogoViewModelFactory // Factory do Catálogo
import com.almenara.gamecountdown.ui.detalhes.DetalhesScreen // tela de Detalhes
import com.almenara.gamecountdown.ui.detalhes.DetalhesViewModel // ViewModel de Detalhes
import com.almenara.gamecountdown.ui.detalhes.DetalhesViewModelFactory // Factory de Detalhes (recebe o id)
import com.almenara.gamecountdown.ui.lista_pessoal.ListaPessoalScreen // tela de Lista Pessoal
import com.almenara.gamecountdown.ui.lista_pessoal.ListaPessoalViewModel // ViewModel da Lista Pessoal
import com.almenara.gamecountdown.ui.lista_pessoal.ListaPessoalViewModelFactory // Factory da Lista Pessoal

// Rotas: os "endereços" das telas. Centralizados aqui para evitar strings soltas espalhadas pelo código.
object Rotas {
    const val CATALOGO = "catalogo"              // aba Catálogo
    const val LISTA = "lista"                    // aba Lista Pessoal
    const val DETALHES = "detalhes/{gameId}"     // tela de Detalhes; {gameId} é o argumento (qual jogo)
    fun detalhes(id: String) = "detalhes/$id"    // monta a rota concreta para um jogo específico
}

// dados de uma aba da barra inferior: para qual rota leva, o rótulo e o ícone
private data class AbaInferior(val rota: String, val rotulo: String, val icone: ImageVector)

// as abas exibidas na barra inferior — só Catálogo e Lista Pessoal por ora (Calendário/Busca entram no futuro)
private val abas = listOf(
    AbaInferior(Rotas.CATALOGO, "Catálogo", Icons.AutoMirrored.Filled.List),
    AbaInferior(Rotas.LISTA, "Lista", Icons.Filled.Favorite)
)

// GameCountdownApp: a raiz da UI navegável do app. Monta a barra inferior + o NavHost que troca as telas.
@Composable
fun GameCountdownApp() {
    // o "controle remoto" da navegação: quem manda ir de uma tela para outra e mantém a pilha (back)
    val navController = rememberNavController()

    // observa qual é a rota ativa agora — usado para destacar a aba certa e decidir se a barra inferior aparece
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route

    // a barra inferior só aparece nas telas de aba (Catálogo/Lista); em Detalhes (tela empilhada) ela some
    val mostrarBarra = rotaAtual == Rotas.CATALOGO || rotaAtual == Rotas.LISTA

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    // um item por aba; o item da rota atual fica destacado (selected)
                    abas.forEach { aba ->
                        NavigationBarItem(
                            selected = rotaAtual == aba.rota,
                            onClick = { navegarParaAba(navController, aba.rota) },
                            icon = { Icon(aba.icone, contentDescription = aba.rotulo) },
                            label = { Text(aba.rotulo) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // NavHost: mostra a tela correspondente à rota atual. começa no Catálogo.
        NavHost(
            navController = navController,
            startDestination = Rotas.CATALOGO,
            modifier = Modifier.padding(innerPadding) // respeita o espaço reservado pela barra inferior
        ) {
            // destino: Catálogo
            composable(Rotas.CATALOGO) {
                val vm: CatalogoViewModel = viewModel(factory = CatalogoViewModelFactory())
                CatalogoScreen(
                    viewModel = vm,
                    // ao tocar um card, navega para os Detalhes daquele jogo
                    onJogoClick = { id -> navController.navigate(Rotas.detalhes(id)) }
                )
            }

            // destino: Lista Pessoal
            composable(Rotas.LISTA) {
                val vm: ListaPessoalViewModel = viewModel(factory = ListaPessoalViewModelFactory())
                ListaPessoalScreen(
                    viewModel = vm,
                    onJogoClick = { id -> navController.navigate(Rotas.detalhes(id)) }
                )
            }

            // destino: Detalhes — recebe o gameId pela rota
            composable(
                route = Rotas.DETALHES,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { entry ->
                // lê o id do jogo a partir dos argumentos da rota
                val gameId = entry.arguments?.getString("gameId").orEmpty()
                // cria o ViewModel de Detalhes injetando o id (a Factory repassa ao construtor)
                val vm: DetalhesViewModel = viewModel(factory = DetalhesViewModelFactory(gameId))
                DetalhesScreen(
                    viewModel = vm,
                    onVoltar = { navController.popBackStack() } // "voltar" remove Detalhes da pilha
                )
            }
        }
    }
}

// navega para uma aba da barra inferior seguindo o padrão recomendado do Android:
// - popUpTo(startDestination) com saveState: evita empilhar abas infinitamente e guarda o estado da aba deixada
// - launchSingleTop: não recria a aba se ela já é a atual
// - restoreState: ao voltar para uma aba, restaura o estado (posição de rolagem etc.) que ela tinha
private fun navegarParaAba(
    navController: androidx.navigation.NavHostController,
    rota: String
) {
    navController.navigate(rota) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
