package com.almenara.gamecountdown.data.model // pacote onde este arquivo vive na estrutura do projeto

// enum class: tipo que representa um conjunto fixo e fechado de valores possíveis
// cada valor de plataforma carrega um displayName — o nome legível exibido na interface do app
enum class Platform(val displayName: String) {
    PS5("PlayStation 5"),           // Sony PlayStation 5
    XBOX_SERIES("Xbox Series X|S"), // Microsoft Xbox Series X e Xbox Series S
    PC("PC"),                       // computadores pessoais (Windows, Linux, Mac)
    SWITCH("Nintendo Switch"),      // Nintendo Switch (todas as versões)
    MOBILE("Mobile")                // dispositivos móveis (Android e iOS)
}
