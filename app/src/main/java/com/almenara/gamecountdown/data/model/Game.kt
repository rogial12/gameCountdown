package com.almenara.gamecountdown.data.model

data class Game(
    val id: String,
    val title: String,
    val releaseDate: String,        // formato ISO: "2025-03-15"
    val platforms: List<Platform>,
    val genres: List<Genre>,
    val developer: String,
    val synopsis: String,
    val coverUrl: String,
    val priceUsd: Double?,          // null = preço ainda não anunciado
    val priceBrl: Double?,
    val trailerId: String?,         // ID do vídeo no YouTube; null = sem trailer ainda
    val preSaleDate: String?,       // formato ISO; null = sem pré-venda anunciada
    val anticipationScore: Int = 0, // usado para ordenação "mais aguardados"
    val isWatched: Boolean = false  // true = está na lista pessoal do usuário
)
