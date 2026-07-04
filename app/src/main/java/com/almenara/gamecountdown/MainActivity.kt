package com.almenara.gamecountdown // pacote raiz do app

import android.os.Bundle // tipo do estado salvo da Activity, recebido em onCreate
import androidx.activity.ComponentActivity // classe base de Activity compatível com Compose
import androidx.activity.compose.setContent // define a UI da Activity usando Compose
import androidx.activity.enableEdgeToEdge // faz o app desenhar sob as barras de sistema (visual moderno Android)
import com.almenara.gamecountdown.ui.navigation.GameCountdownApp // a raiz navegável do app (barra inferior + telas)
import com.almenara.gamecountdown.ui.theme.GameCountdownTheme // tema Material 3 do app (cores, tipografia)

// MainActivity: ponto de entrada do app. É a primeira tela que abre quando o app é iniciado.
class MainActivity : ComponentActivity() {
    // onCreate roda quando a Activity é criada; é aqui que definimos o que aparece na tela
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // inicialização padrão da Activity
        enableEdgeToEdge()                 // permite o conteúdo desenhar por trás das barras de status/navegação
        setContent {                       // tudo dentro deste bloco é a UI em Compose
            GameCountdownTheme {           // aplica o tema do app a toda a árvore de componentes
                // GameCountdownApp monta a navegação (barra inferior Catálogo/Lista + telas empilhadas como Detalhes)
                GameCountdownApp()
            }
        }
    }
}
