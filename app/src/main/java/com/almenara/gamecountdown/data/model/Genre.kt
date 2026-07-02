package com.almenara.gamecountdown.data.model // pacote onde este arquivo vive na estrutura do projeto

// enum class: tipo que representa um conjunto fixo de gêneros de jogos
// cada gênero carrega um displayName em português para exibição nos filtros e tags da UI
enum class Genre(val displayName: String) {
    ACTION("Ação"),           // jogos com foco em combate em tempo real e reflexo
    RPG("RPG"),               // role-playing games com progressão de personagem e narrativa
    ADVENTURE("Aventura"),    // jogos centrados em exploração e história
    STRATEGY("Estratégia"),   // jogos que exigem planejamento e tomada de decisão
    SPORTS("Esportes"),       // simuladores de modalidades esportivas reais
    SIMULATION("Simulação"),  // jogos que simulam sistemas, atividades ou ambientes reais
    HORROR("Terror"),         // jogos com temática de horror e suspense
    FIGHTING("Luta")          // jogos de combate direto entre personagens
}
