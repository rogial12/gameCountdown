package com.almenara.gamecountdown.data.repository.mock // pacote das implementações falsas usadas no protótipo

import com.almenara.gamecountdown.data.model.Game     // modelo de dados de um jogo
import com.almenara.gamecountdown.data.model.Genre    // enum de gêneros
import com.almenara.gamecountdown.data.model.Platform // enum de plataformas
import com.almenara.gamecountdown.data.repository.GameRepository // interface que esta classe implementa

// implementação falsa do repositório para uso no protótipo (Fase 2)
// substitui uma chamada real à API sem depender de internet ou backend
class MockGameRepository : GameRepository { // : GameRepository significa que esta classe implementa a interface

    // conjunto mutável de IDs dos jogos que o usuário marcou como watched
    // mutableSetOf: coleção que não admite duplicatas e pode ser alterada em tempo de execução
    // pré-populado com "2" e "4" para simular um usuário que já usou o app
    private val watchedIds = mutableSetOf("2", "4")

    // lista imutável de jogos fictícios que representa o catálogo
    // listOf: cria uma lista que não pode ser alterada após a criação
    private val games = listOf(

        // jogo 1: lançamento iminente (daqui a dias), sem pré-venda, Xbox e PC
        Game(
            id = "1",                                  // identificador único
            title = "Iron Protocol",                   // título
            releaseDate = "2026-07-10",                // data próxima — serve para testar o estado "iminente"
            platforms = listOf(Platform.XBOX_SERIES, Platform.PC), // disponível em Xbox Series e PC
            genres = listOf(Genre.ACTION),             // gênero: ação
            developer = "Apex Studios",                // estúdio desenvolvedor
            synopsis = "Thriller de ação em um mundo pós-colapso, onde uma IA controla toda a infraestrutura das cidades.",
            coverUrl = "https://picsum.photos/seed/iron/400/600", // imagem placeholder para o protótipo
            priceUsd = 59.99,                          // preço em dólar anunciado
            priceBrl = 299.90,                         // preço em real anunciado
            trailerId = "dQw4w9WgXcQ",                // ID de um vídeo no YouTube para embed
            preSaleDate = "2026-06-10",                // pré-venda já encerrada (antes da data atual)
            anticipationScore = 87                     // pontuação de antecipação
        ),

        // jogo 2: lançamento em agosto, watched por padrão (id "2" está em watchedIds), PS5 e PC
        Game(
            id = "2",
            title = "Hearthfall",
            releaseDate = "2026-08-15",
            platforms = listOf(Platform.PS5, Platform.PC),
            genres = listOf(Genre.RPG, Genre.ADVENTURE), // dois gêneros: RPG e aventura
            developer = "Crimson Forge",
            synopsis = "RPG de mundo aberto ambientado em um reino em colapso, onde as escolhas do jogador redesenham o mapa político do continente.",
            coverUrl = "https://picsum.photos/seed/hearth/400/600",
            priceUsd = 69.99,
            priceBrl = 349.90,
            trailerId = "dQw4w9WgXcQ",
            preSaleDate = null,                        // null: sem pré-venda anunciada
            anticipationScore = 92
        ),

        // jogo 3: exclusivo de PC, sem preço em BRL, sem trailer — testa campos opcionais ausentes
        Game(
            id = "3",
            title = "Verdant Rift",
            releaseDate = "2026-07-28",
            platforms = listOf(Platform.PC),           // exclusivo de PC
            genres = listOf(Genre.STRATEGY),
            developer = "Mossy Byte",
            synopsis = "Estratégia por turnos em uma civilização que coexiste com ecossistemas vivos e imprevisíveis.",
            coverUrl = "https://picsum.photos/seed/verdant/400/600",
            priceUsd = 39.99,
            priceBrl = null,                           // null: preço em BRL não disponível
            trailerId = null,                          // null: trailer ainda não lançado
            preSaleDate = null,
            anticipationScore = 74
        ),

        // jogo 4: exclusivo PS5, watched por padrão (id "4" está em watchedIds), lançamento distante
        Game(
            id = "4",
            title = "Neon Samurai 2",
            releaseDate = "2026-11-05",
            platforms = listOf(Platform.PS5),          // exclusivo PlayStation 5
            genres = listOf(Genre.ACTION, Genre.ADVENTURE),
            developer = "Ronin Interactive",
            synopsis = "Sequência do aclamado hack-and-slash: novo protagonista, cidade diferente, mesmo caos neon.",
            coverUrl = "https://picsum.photos/seed/neon/400/600",
            priceUsd = 69.99,
            priceBrl = 349.90,
            trailerId = "dQw4w9WgXcQ",
            preSaleDate = "2026-10-01",                // pré-venda futura
            anticipationScore = 95                     // maior pontuação do catálogo mock
        ),

        // jogo 5: Nintendo Switch e PC, sem pré-venda
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

        // jogo 6: lançamento em 2027, sem preço nem trailer — simula anúncio incompleto
        Game(
            id = "6",
            title = "Project Omega",
            releaseDate = "2027-02-14",
            platforms = listOf(Platform.PS5, Platform.XBOX_SERIES, Platform.PC), // multi-plataforma
            genres = listOf(Genre.ACTION, Genre.RPG),
            developer = "Phantom Engine",
            synopsis = "Detalhes ainda não revelados. O estúdio confirmou apenas a data e as plataformas.",
            coverUrl = "https://picsum.photos/seed/omega/400/600",
            priceUsd = null,                           // null: preço ainda não anunciado
            priceBrl = null,
            trailerId = null,                          // null: sem trailer ainda
            preSaleDate = null,
            anticipationScore = 78
        )
    )

    // retorna todos os jogos, mesclando o status watched de cada um com o conjunto watchedIds
    // .map transforma cada item da lista aplicando a função entre chaves
    // .copy() cria uma cópia do objeto com apenas o campo isWatched alterado — os demais ficam iguais
    // "it.id in watchedIds" verifica se o ID do jogo está no conjunto de watched; retorna true ou false
    override fun getGames(): List<Game> =
        games.map { it.copy(isWatched = it.id in watchedIds) }

    // busca um único jogo pelo ID; retorna null se nenhum jogo tiver aquele ID
    // .find retorna o primeiro item que satisfaz a condição, ou null se não encontrar nenhum
    // ?. (operador de chamada segura) só executa .copy() se o resultado do .find não for null
    override fun getGameById(id: String): Game? =
        games.find { it.id == id }?.copy(isWatched = id in watchedIds)

    // filtra jogos pelo título, sem diferenciar maiúsculas de minúsculas
    // .filter mantém apenas os itens para os quais a condição retorna true
    // .contains(query, ignoreCase = true) verifica se o título contém o texto da busca
    override fun searchGames(query: String): List<Game> =
        games.filter { it.title.contains(query, ignoreCase = true) }
            .map { it.copy(isWatched = it.id in watchedIds) }

    // retorna apenas os jogos cujos IDs estão em watchedIds
    // .filter + "it.id in watchedIds" seleciona somente os jogos da lista pessoal
    override fun getWatchedGames(): List<Game> =
        games.filter { it.id in watchedIds }
            .map { it.copy(isWatched = true) } // isWatched é sempre true aqui por definição

    // adiciona ou remove um ID do conjunto watchedIds
    // watched = true → .add() insere o ID no conjunto
    // watched = false → .remove() retira o ID do conjunto
    override fun setWatched(id: String, watched: Boolean) {
        if (watched) watchedIds.add(id) else watchedIds.remove(id)
    }
}
