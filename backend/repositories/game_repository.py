# ABC e abstractmethod: as ferramentas do Python para declarar uma "classe base abstrata" —
# um contrato que lista métodos OBRIGATÓRIOS sem implementá-los. Quem herdar desta classe é obrigado
# a implementar cada método marcado com @abstractmethod; se esquecer um, o Python recusa criar o objeto
# (falha explícita, na hora). É a rede de segurança pedida na arquitetura da Fase 1 (abc.ABC, não Protocol).
from abc import ABC, abstractmethod

# Importa o Game do BANCO (ORM), de models/game.py. Decisão de Igor (Passo 39): o repositório trabalha
# com o objeto de domínio/banco; a tradução para o JSON do contrato (o Game Pydantic) acontece lá em cima,
# na rota — o FastAPI faz isso automaticamente. Assim, detalhes de banco não vazam para a camada de API.
from models.game import Game


# GameRepository é o CONTRATO da fonte de dados de jogos: define O QUE dá para fazer, nunca COMO.
# É a fronteira que permite trocar a implementação — dados semente agora, banco/RAWG depois —
# sem mexer no Service nem nas rotas. Espelha o subconjunto de CATÁLOGO do GameRepository.kt do app;
# os métodos de "lista pessoal" (watched) ficam de fora: estado do usuário é local no app (Passo 36).
class GameRepository(ABC):

    # get_games: devolve o catálogo inteiro (todos os jogos).
    # As reticências (...) significam "corpo vazio" — a implementação real vem nas subclasses.
    @abstractmethod
    def get_games(self) -> list[Game]:
        ...

    # get_game_by_id: busca um jogo pelo seu id. Devolve o Game encontrado, ou None se não existir.
    # O '| None' é o equivalente Python ao 'Game?' do Kotlin: pode não haver resultado.
    # (usa-se 'game_id' em vez de 'id' para não colidir com a função embutida 'id' do Python.)
    @abstractmethod
    def get_game_by_id(self, game_id: str) -> Game | None:
        ...

    # search_games: devolve os jogos cujo título casa com o texto buscado (busca por título).
    @abstractmethod
    def search_games(self, query: str) -> list[Game]:
        ...
