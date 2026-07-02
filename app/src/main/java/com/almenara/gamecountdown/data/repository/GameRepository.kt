package com.almenara.gamecountdown.data.repository

import com.almenara.gamecountdown.data.model.Game

interface GameRepository {
    fun getGames(): List<Game>
    fun getGameById(id: String): Game?
    fun searchGames(query: String): List<Game>
    fun getWatchedGames(): List<Game>
    fun setWatched(id: String, watched: Boolean)
}
