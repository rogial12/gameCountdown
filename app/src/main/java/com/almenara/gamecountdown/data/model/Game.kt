package com.almenara.gamecountdown.data.model // pacote onde este arquivo vive na estrutura do projeto

// data class: tipo especial do Kotlin para objetos que só carregam dados, sem lógica
// o compilador gera automaticamente: comparação entre objetos, cópia e conversão para texto
data class Game(
    val id: String,                     // identificador único do jogo (ex: "1", "gta6")
    val title: String,                  // título do jogo (ex: "Neon Samurai 2")
    val releaseDate: String,            // data de lançamento no formato ISO: "2026-08-15"
    val platforms: List<Platform>,      // lista das plataformas onde o jogo será lançado
    val genres: List<Genre>,            // lista dos gêneros do jogo (pode ter mais de um)
    val developer: String,              // nome do estúdio que desenvolve o jogo
    val synopsis: String,               // descrição/sinopse do jogo para exibição na tela de detalhes
    val coverUrl: String,               // endereço da imagem de capa para carregamento na UI
    val priceUsd: Double?,              // preço em dólar; o ? significa que pode ser null (preço não anunciado)
    val priceBrl: Double?,              // preço em real; null quando o valor em BRL não está disponível
    val trailerId: String?,             // ID do vídeo no YouTube para embed; null quando não há trailer
    val preSaleDate: String?,           // data de pré-venda no formato ISO; null quando não há pré-venda
    val anticipationScore: Int = 0,     // pontuação de antecipação para ordenação "mais aguardados"; padrão 0
    val isWatched: Boolean = false      // true quando o usuário adicionou o jogo à lista pessoal; padrão false
)
