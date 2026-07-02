package com.almenara.gamecountdown.data.repository.mock

import com.almenara.gamecountdown.data.model.Game
import com.almenara.gamecountdown.data.model.Genre
import com.almenara.gamecountdown.data.model.Platform
import com.almenara.gamecountdown.data.repository.GameRepository

class MockGameRepository : GameRepository {

    // IDs pré-marcados como watched para simular um usuário que já usou o app
    private val watchedIds = mutableSetOf("2", "4")

    private val games = listOf(
        Game(
            id = "1",
            title = "Iron Protocol",
            releaseDate = "2026-07-10",
            platforms = listOf(Platform.XBOX_SERIES, Platform.PC),
            genres = listOf(Genre.ACTION),
            developer = "Apex Studios",
            synopsis = "Thriller de ação em um mundo pós-colapso, onde uma IA controla toda a infraestrutura das cidades.",
            coverUrl = "https://picsum.photos/seed/iron/400/600",
            priceUsd = 59.99,
            priceBrl = 299.90,
            trailerId = "dQw4w9WgXcQ",
            preSaleDate = "2026-06-10",
            anticipationScore = 87
        ),
        Game(
            id = "2",
            title = "Hearthfall",
            releaseDate = "2026-08-15",
            platforms = listOf(Platform.PS5, Platform.PC),
            genres = listOf(Genre.RPG, Genre.ADVENTURE),
            developer = "Crimson Forge",
            synopsis = "RPG de mundo aberto ambientado em um reino em colapso, onde as escolhas do jogador redesenham o mapa político do continente.",
            coverUrl = "https://picsum.photos/seed/hearth/400/600",
            priceUsd = 69.99,
            priceBrl = 349.90,
            trailerId = "dQw4w9WgXcQ",
            preSaleDate = null,
            anticipationScore = 92
        ),
        Game(
            id = "3",
            title = "Verdant Rift",
            releaseDate = "2026-07-28",
            platforms = listOf(Platform.PC),
            genres = listOf(Genre.STRATEGY),
            developer = "Mossy Byte",
            synopsis = "Estratégia por turnos em uma civilização que coexiste com ecossistemas vivos e imprevisíveis.",
            coverUrl = "https://picsum.photos/seed/verdant/400/600",
            priceUsd = 39.99,
            priceBrl = null,
            trailerId = null,
            preSaleDate = null,
            anticipationScore = 74
        ),
        Game(
            id = "4",
            title = "Neon Samurai 2",
            releaseDate = "2026-11-05",
            platforms = listOf(Platform.PS5),
            genres = listOf(Genre.ACTION, Genre.ADVENTURE),
            developer = "Ronin Interactive",
            synopsis = "Sequência do aclamado hack-and-slash: novo protagonista, cidade diferente, mesmo caos neon.",
            coverUrl = "https://picsum.photos/seed/neon/400/600",
            priceUsd = 69.99,
            priceBrl = 349.90,
            trailerId = "dQw4w9WgXcQ",
            preSaleDate = "2026-10-01",
            anticipationScore = 95
        ),
        Game(
            id = "5",
            title = "Starbound Legacy",
            releaseDate = "2026-09-20",
            platforms = listOf(Platform.SWITCH, Platform.PC),
            genres = listOf(Genre.ADVENTURE, Genre.SIMULATION),
            developer = "Orbit Games",
            synopsis = "Explore, construa e sobreviva em um universo procedural com trilhões de planetas gerados dinamicamente.",
            coverUrl = "https://picsum.photos/seed/star/400/600",
            priceUsd = 49.99,
            priceBrl = 249.90,
            trailerId = "dQw4w9WgXcQ",
            preSaleDate = null,
            anticipationScore = 81
        ),
        Game(
            id = "6",
            title = "Project Omega",
            releaseDate = "2027-02-14",
            platforms = listOf(Platform.PS5, Platform.XBOX_SERIES, Platform.PC),
            genres = listOf(Genre.ACTION, Genre.RPG),
            developer = "Phantom Engine",
            synopsis = "Detalhes ainda não revelados. O estúdio confirmou apenas a data e as plataformas.",
            coverUrl = "https://picsum.photos/seed/omega/400/600",
            priceUsd = null,
            priceBrl = null,
            trailerId = null,
            preSaleDate = null,
            anticipationScore = 78
        )
    )

    override fun getGames(): List<Game> =
        games.map { it.copy(isWatched = it.id in watchedIds) }

    override fun getGameById(id: String): Game? =
        games.find { it.id == id }?.copy(isWatched = id in watchedIds)

    override fun searchGames(query: String): List<Game> =
        games.filter { it.title.contains(query, ignoreCase = true) }
            .map { it.copy(isWatched = it.id in watchedIds) }

    override fun getWatchedGames(): List<Game> =
        games.filter { it.id in watchedIds }
            .map { it.copy(isWatched = true) }

    override fun setWatched(id: String, watched: Boolean) {
        if (watched) watchedIds.add(id) else watchedIds.remove(id)
    }
}
