# 'date' cria datas para montar jogos de exemplo nos testes.
from datetime import date

# create_engine e sessionmaker: para criar um banco SQLite EM MEMÓRIA, isolado, só para o teste.
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Base traz o conjunto de tabelas conhecidas; Game é o model que estamos testando.
from database import Base
from models.game import Game


def _sessao_em_memoria():
    # Cria um banco SQLite que vive só na memória RAM (some ao fim do teste) — rápido e isolado.
    engine = create_engine("sqlite:///:memory:")
    # Cria, nesse banco, todas as tabelas conhecidas pela Base (inclui 'games', pois importamos Game acima).
    Base.metadata.create_all(engine)
    # Devolve uma fábrica de sessões ligada a esse banco de memória.
    return sessionmaker(bind=engine)


def test_game_persiste_e_le_de_volta():
    # Objetivo: inserir um jogo completo, ler de volta e conferir que todos os campos voltam intactos —
    # em especial as datas (voltam como 'date') e as listas (round-trip pela coluna JSON).
    Sessao = _sessao_em_memoria()
    with Sessao() as s:
        s.add(Game(
            id="gta6",
            title="Grand Theft Auto VI",
            release_date=date(2026, 8, 15),
            platforms=["PS5", "XBOX_SERIES"],
            genres=["ACTION", "ADVENTURE"],
            developer="Rockstar Games",
            synopsis="O retorno a Vice City.",
            cover_url="https://exemplo.com/gta6.jpg",
            price_usd=69.99,
            price_brl=349.90,
            trailer_id="QdBZY2fkU-0",
            pre_sale_date=date(2026, 6, 1),
            screenshot_urls=["https://exemplo.com/s1.jpg"],
            anticipation_score=100,
        ))
        s.commit()  # confirma a inserção no banco

    # Abre uma sessão NOVA para garantir que os dados vieram mesmo do banco, não da memória da sessão anterior.
    with Sessao() as s:
        jogo = s.get(Game, "gta6")           # busca pelo id (chave primária)
        assert jogo is not None              # o jogo tem que existir
        assert jogo.title == "Grand Theft Auto VI"
        assert jogo.release_date == date(2026, 8, 15)          # data volta como 'date', não texto
        assert jogo.platforms == ["PS5", "XBOX_SERIES"]        # lista JSON round-trip
        assert jogo.genres == ["ACTION", "ADVENTURE"]
        assert jogo.price_usd == 69.99
        assert jogo.pre_sale_date == date(2026, 6, 1)


def test_campos_opcionais_e_padroes():
    # Objetivo: um jogo sem preço/trailer/pré-venda deve ser aceito (colunas nuláveis),
    # e os campos com padrão devem assumir seus defaults quando omitidos.
    Sessao = _sessao_em_memoria()
    with Sessao() as s:
        s.add(Game(
            id="x",
            title="Mínimo",
            release_date=date(2027, 1, 1),
            platforms=["PC"],
            genres=["RPG"],
            developer="Estúdio",
            synopsis="...",
            cover_url="https://exemplo.com/x.jpg",
            price_usd=None,
            price_brl=None,
            trailer_id=None,
            pre_sale_date=None,
        ))
        s.commit()

    with Sessao() as s:
        jogo = s.get(Game, "x")
        assert jogo.price_usd is None
        assert jogo.trailer_id is None
        assert jogo.pre_sale_date is None
        assert jogo.screenshot_urls == []    # default: lista vazia
        assert jogo.anticipation_score == 0  # default: zero
