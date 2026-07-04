package com.almenara.gamecountdown.data.repository // pacote onde vivem as interfaces de repositório

import com.almenara.gamecountdown.data.model.Game // importa a classe Game para uso nas assinaturas dos métodos

// interface: define um contrato de o que pode ser feito com jogos, sem dizer como
// qualquer classe que implementar GameRepository é obrigada a ter todos estes métodos
// isso permite trocar a implementação (mock na Fase 2, API real na Fase 3) sem alterar nada no Service ou na UI
interface GameRepository {
    fun getGames(): List<Game>                    // retorna a lista completa de jogos do catálogo
    fun getGameById(id: String): Game?            // busca um jogo pelo ID; retorna null se não encontrado
    fun searchGames(query: String): List<Game>    // retorna jogos cujo título contém o texto da busca
    fun getWatchedGames(): List<Game>             // retorna apenas os jogos marcados na lista pessoal do usuário
    fun setWatched(id: String, watched: Boolean)  // true adiciona à lista pessoal; false remove

    // registra um callback chamado toda vez que setWatched muda quem está "de olho" — não importa qual tela chamou.
    // é o que permite Catálogo, Lista Pessoal e Detalhes ficarem sincronizados entre si, já que todos compartilham
    // a mesma instância de Repository (via AppContainer). Devolve uma função que CANCELA essa inscrição;
    // quem se inscreve deve chamá-la ao ser destruído, para não vazar callbacks de telas que não existem mais.
    fun observarMudancasWatched(callback: () -> Unit): () -> Unit
}
