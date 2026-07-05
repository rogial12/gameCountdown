package com.almenara.gamecountdown.ui.comum // pacote dos componentes visuais compartilhados entre telas

import androidx.compose.foundation.layout.fillMaxWidth // faz a versão em destaque ocupar toda a largura disponível
import androidx.compose.foundation.layout.padding // modifier para dar espaçamento interno ao redor do texto
import androidx.compose.material3.MaterialTheme // acesso ao tema atual (cores, tipografia, formas do Material 3)
import androidx.compose.material3.Surface // "caixa" do Material 3: dá cor de fundo, forma e elevação a um conteúdo
import androidx.compose.material3.Text // componente que desenha um texto na tela
import androidx.compose.runtime.Composable // anotação que marca uma função como componente de UI do Compose
import androidx.compose.ui.Modifier // objeto que descreve ajustes de layout/aparência, repassado de fora pra dentro
import androidx.compose.ui.graphics.Color // tipo que representa uma cor; usado para escolher fundo/texto por estado
import androidx.compose.ui.text.font.FontWeight // permite deixar o texto em negrito no estado iminente
import androidx.compose.ui.text.style.TextAlign // centraliza o texto na versão em destaque
import androidx.compose.ui.tooling.preview.Preview // permite visualizar o componente no editor sem rodar o app
import androidx.compose.ui.unit.dp // unidade de medida de distância independente de densidade de tela

// enum: os estados visuais possíveis do countdown, decididos com Igor (2 níveis + o caso de já lançado)
// é 'internal' para que os testes, dentro do mesmo módulo, possam verificar qual estado a lógica escolheu
internal enum class CountdownEstado {
    NORMAL,      // lançamento a mais de 7 dias — visual discreto
    IMINENTE,    // lançamento em 7 dias ou menos (inclui véspera e o próprio dia) — visual de destaque
    DISPONIVEL   // o jogo já lançou (data no passado) — visual neutro, sem countdown
}

// data class: o resultado da lógica pura do countdown — o texto a exibir e o estado visual correspondente
// separar "o que dizer" (texto) de "como destacar" (estado) deixa a lógica testável sem depender do desenho
internal data class CountdownInfo(
    val texto: String,           // texto já pronto para exibição (ex.: "faltam 42 dias", "LANÇA HOJE")
    val estado: CountdownEstado  // qual dos três estados visuais este countdown representa
)

// função de apoio: converte o número de dias até o lançamento no texto + estado a exibir.
// é lógica pura (dado -> resultado), por isso fica fora do @Composable e é 'internal' para ser testável sem emulador.
// regra decidida com Igor: iminente = 7 dias ou menos (limite inclusivo); negativo = já lançado ("Disponível").
internal fun calcularCountdown(dias: Long): CountdownInfo = when {
    dias < 0L -> CountdownInfo("Disponível", CountdownEstado.DISPONIVEL) // data no passado: jogo já disponível
    dias == 0L -> CountdownInfo("LANÇA HOJE", CountdownEstado.IMINENTE)   // lança no próprio dia
    dias == 1L -> CountdownInfo("LANÇA AMANHÃ", CountdownEstado.IMINENTE) // véspera do lançamento
    dias <= 7L -> CountdownInfo("faltam $dias dias", CountdownEstado.IMINENTE) // dentro da semana (2 a 7 dias)
    else -> CountdownInfo("faltam $dias dias", CountdownEstado.NORMAL)    // mais de 7 dias: countdown normal
}

// Composable = função que descreve um pedaço de UI. Este é o CountdownBadge: um "chip" que mostra o countdown do jogo.
// Recebe apenas o número de dias e delega a decisão de texto/estado à função calcularCountdown acima.
@Composable
fun CountdownBadge(
    dias: Long,                     // dias até o lançamento (vindo de GameService.getDaysUntilRelease); pode ser negativo
    modifier: Modifier = Modifier,  // ajustes vindos de quem chama (ex.: espaçamento externo); padrão = nenhum ajuste
    destaque: Boolean = false       // true = versão grande/centralizada (usada no topo da tela de Detalhes, item 8.2 do feedback)
) {
    // calcula texto + estado uma única vez, antes de desenhar, aplicando a regra de negócio do countdown
    val info = calcularCountdown(dias)

    // escolhe a cor de fundo do chip conforme o estado; cores vêm do tema (acompanham claro/escuro e o Material 3 Expressive)
    val corFundo: Color = when (info.estado) {
        CountdownEstado.IMINENTE -> MaterialTheme.colorScheme.primaryContainer   // destaque: chama atenção para o lançamento próximo
        CountdownEstado.NORMAL -> MaterialTheme.colorScheme.surfaceVariant       // discreto: countdown comum
        CountdownEstado.DISPONIVEL -> MaterialTheme.colorScheme.surfaceVariant   // neutro: já lançado, sem urgência
    }

    // escolhe a cor do texto correspondente ao fundo, garantindo contraste legível em cada estado
    val corTexto: Color = when (info.estado) {
        CountdownEstado.IMINENTE -> MaterialTheme.colorScheme.onPrimaryContainer
        CountdownEstado.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
        CountdownEstado.DISPONIVEL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // no estado iminente o texto fica em negrito para reforçar o destaque; nos demais, peso normal
    val pesoFonte = if (info.estado == CountdownEstado.IMINENTE) FontWeight.Bold else FontWeight.Normal

    // Surface desenha o fundo arredondado e colorido do chip; o Text vai dentro dela.
    // na versão em destaque, ocupa a largura toda e usa cantos maiores — vira um "banner", não mais um chip pequeno.
    Surface(
        modifier = if (destaque) modifier.fillMaxWidth() else modifier,
        shape = if (destaque) MaterialTheme.shapes.medium else MaterialTheme.shapes.small,
        color = corFundo // cor de fundo escolhida conforme o estado do countdown
    ) {
        Text(
            text = info.texto, // texto já formatado pela regra de negócio
            // na versão em destaque a tipografia é bem maior, para ficar mais proeminente que os demais tiles da tela
            style = if (destaque) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.labelMedium,
            color = corTexto,                             // cor do texto que contrasta com o fundo do estado
            fontWeight = pesoFonte,                       // negrito quando iminente, normal caso contrário
            textAlign = if (destaque) TextAlign.Center else TextAlign.Unspecified, // centraliza só na versão grande
            modifier = (if (destaque) Modifier.fillMaxWidth() else Modifier)
                // espaço interno entre o texto e a borda; bem mais generoso na versão em destaque
                .padding(horizontal = if (destaque) 16.dp else 8.dp, vertical = if (destaque) 20.dp else 4.dp)
        )
    }
}

// @Preview: renderiza o componente isolado no painel de preview do Android Studio, sem rodar o app inteiro.
// quatro previews cobrem os estados-chave: normal, iminente (semana), iminente (hoje) e já lançado.
@Preview
@Composable
private fun CountdownBadgeNormalPreview() {
    CountdownBadge(dias = 42L) // mais de 7 dias -> "faltam 42 dias", estado normal
}

@Preview
@Composable
private fun CountdownBadgeIminenteSemanaPreview() {
    CountdownBadge(dias = 5L) // dentro da semana -> "faltam 5 dias", estado iminente
}

@Preview
@Composable
private fun CountdownBadgeHojePreview() {
    CountdownBadge(dias = 0L) // dia do lançamento -> "LANÇA HOJE", estado iminente
}

@Preview
@Composable
private fun CountdownBadgeDisponivelPreview() {
    CountdownBadge(dias = -3L) // data no passado -> "Disponível", estado neutro
}

// preview da versão em destaque (usada no topo da tela de Detalhes), no estado iminente
@Preview
@Composable
private fun CountdownBadgeDestaquePreview() {
    CountdownBadge(dias = 6L, destaque = true) // "faltam 6 dias" — exemplo citado por Igor no feedback
}
