package com.almenara.gamecountdown.ui.comum // pacote dos componentes visuais compartilhados entre telas

import androidx.compose.material3.Switch // interruptor do Material 3 (liga/desliga), usado como controle de "de olho"
import androidx.compose.runtime.Composable // anotação que marca uma função como componente de UI do Compose
import androidx.compose.ui.Modifier // objeto que descreve ajustes de layout/aparência, repassado de fora pra dentro
import androidx.compose.ui.tooling.preview.Preview // permite visualizar o componente no editor sem rodar o app

// AddToListSwitch: o interruptor de "estou de olho neste jogo".
// componente puro e genérico: recebe o estado atual (marcado) e avisa quando o usuário o alterna (onMarcarChange).
// é reutilizável — na Lista Pessoal serve para REMOVER (ligado -> desligado); no Catálogo, no futuro, para ADICIONAR.
@Composable
fun AddToListSwitch(
    marcado: Boolean,                    // true = jogo está na lista pessoal (switch ligado)
    onMarcarChange: (Boolean) -> Unit,   // chamado com o novo valor quando o usuário alterna o switch
    modifier: Modifier = Modifier        // ajustes vindos de quem chama; padrão = nenhum
) {
    // Switch do Material 3: desenha o interruptor e dispara onMarcarChange quando tocado
    Switch(
        checked = marcado,               // reflete o estado atual
        onCheckedChange = onMarcarChange, // repassa a mudança ao chamador (que decide o que fazer)
        modifier = modifier
    )
}

// @Preview: mostra o switch nos dois estados, sem rodar o app
@Preview
@Composable
private fun AddToListSwitchMarcadoPreview() {
    AddToListSwitch(marcado = true, onMarcarChange = {}) // estado ligado (jogo na lista)
}

@Preview
@Composable
private fun AddToListSwitchDesmarcadoPreview() {
    AddToListSwitch(marcado = false, onMarcarChange = {}) // estado desligado (jogo fora da lista)
}
