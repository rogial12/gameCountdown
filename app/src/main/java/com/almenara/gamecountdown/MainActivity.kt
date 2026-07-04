package com.almenara.gamecountdown // pacote raiz do app

import android.os.Bundle // tipo do estado salvo da Activity, recebido em onCreate
import androidx.activity.ComponentActivity // classe base de Activity compatível com Compose
import androidx.activity.compose.setContent // define a UI da Activity usando Compose
import androidx.activity.enableEdgeToEdge // faz o app desenhar sob as barras de sistema (visual moderno Android)
import androidx.lifecycle.viewmodel.compose.viewModel // função que cria/recupera um ViewModel dentro de um Composable
import com.almenara.gamecountdown.ui.catalogo.CatalogoScreen // a tela de Catálogo, que vira o conteúdo do app
import com.almenara.gamecountdown.ui.catalogo.CatalogoViewModel // o ViewModel que a tela observa
import com.almenara.gamecountdown.ui.catalogo.CatalogoViewModelFactory // ensina o Android a criar o CatalogoViewModel
import com.almenara.gamecountdown.ui.theme.GameCountdownTheme // tema Material 3 do app (cores, tipografia)

// MainActivity: ponto de entrada do app. É a primeira tela que abre quando o app é iniciado.
class MainActivity : ComponentActivity() {
    // onCreate roda quando a Activity é criada; é aqui que definimos o que aparece na tela
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // inicialização padrão da Activity
        enableEdgeToEdge()                 // permite o conteúdo desenhar por trás das barras de status/navegação
        setContent {                       // tudo dentro deste bloco é a UI em Compose
            GameCountdownTheme {           // aplica o tema do app a toda a árvore de componentes
                // cria (ou recupera, se já existir) o CatalogoViewModel usando a Factory —
                // a Factory é quem monta a cadeia real GameServiceImpl(MockGameRepository()) por baixo.
                // usar viewModel() em vez de instanciar direto faz o ViewModel sobreviver à rotação de tela.
                val catalogoViewModel: CatalogoViewModel = viewModel(factory = CatalogoViewModelFactory())

                // exibe a tela de Catálogo, ligada ao ViewModel; ela já traz seu próprio Scaffold e barra de topo
                CatalogoScreen(viewModel = catalogoViewModel)
            }
        }
    }
}
