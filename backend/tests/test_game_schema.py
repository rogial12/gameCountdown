# 'date' cria datas para montar um Game de exemplo nos testes.
from datetime import date

# pytest: o framework de testes. 'ValidationError' é o erro que o Pydantic levanta
# quando uma entrada não passa na validação (ex.: uma data mal formada).
import pytest
from pydantic import ValidationError

# Importa exatamente o que vamos testar: o schema Game e os enums do contrato.
from models.enums import Genre, Platform
from models.schemas import Game


# Conjunto EXATO de chaves que o JSON do contrato deve ter — em camelCase, espelhando o Game.kt do app.
# 'isWatched' está FORA de propósito (decisão de Igor: a lista pessoal é local no app).
CHAVES_CONTRATO = {
    "id", "title", "releaseDate", "platforms", "genres", "developer",
    "synopsis", "coverUrl", "priceUsd", "priceBrl", "trailerId",
    "screenshotUrls", "preSaleDate", "anticipationScore",
}


def _game_exemplo() -> Game:
    # Monta um Game completo e válido, reaproveitado por vários testes.
    return Game(
        id="gta6",
        title="Grand Theft Auto VI",
        release_date=date(2026, 8, 15),
        platforms=[Platform.PS5, Platform.XBOX_SERIES],
        genres=[Genre.ACTION, Genre.ADVENTURE],
        developer="Rockstar Games",
        synopsis="O retorno a Vice City.",
        cover_url="https://exemplo.com/gta6.jpg",
        price_usd=69.99,
        price_brl=349.90,
        trailer_id="QdBZY2fkU-0",
        pre_sale_date=date(2026, 6, 1),
        screenshot_urls=["https://exemplo.com/s1.jpg"],
        anticipation_score=100,
    )


def test_json_tem_exatamente_as_chaves_do_contrato():
    # Serializa com by_alias=True para obter as chaves em camelCase (como o app recebe).
    dados = _game_exemplo().model_dump(by_alias=True)
    # As chaves do JSON devem ser EXATAMENTE as do contrato — nem faltando, nem sobrando.
    assert set(dados.keys()) == CHAVES_CONTRATO


def test_is_watched_nao_existe_no_schema():
    # Garante explicitamente que o campo de estado do usuário não vazou para o contrato público.
    dados = _game_exemplo().model_dump(by_alias=True)
    assert "isWatched" not in dados   # nem o nome em camelCase
    assert "is_watched" not in dados  # nem o nome em snake_case


def test_datas_viram_texto_iso_no_json():
    # A data deve sair como texto "AAAA-MM-DD" no JSON — o formato que o app entende.
    dados = _game_exemplo().model_dump(by_alias=True, mode="json")
    assert dados["releaseDate"] == "2026-08-15"
    assert dados["preSaleDate"] == "2026-06-01"


def test_enums_viram_seus_nomes_no_json():
    # Plataformas e gêneros devem sair como os textos fixos do contrato (os nomes do enum).
    dados = _game_exemplo().model_dump(by_alias=True, mode="json")
    assert dados["platforms"] == ["PS5", "XBOX_SERIES"]
    assert dados["genres"] == ["ACTION", "ADVENTURE"]


def test_campos_nulos_e_padroes_sao_aceitos():
    # Um jogo sem preço, sem trailer e sem pré-venda deve ser válido (campos nulos permitidos),
    # e os campos com padrão (screenshots, pontuação) devem assumir seus valores default quando omitidos.
    jogo = Game(
        id="x",
        title="Sem Extras",
        release_date=date(2027, 1, 1),
        platforms=[Platform.PC],
        genres=[Genre.STRATEGY],
        developer="Estúdio",
        synopsis="...",
        cover_url="https://exemplo.com/x.jpg",
        price_usd=None,
        price_brl=None,
        trailer_id=None,
        pre_sale_date=None,
    )
    dados = jogo.model_dump(by_alias=True, mode="json")
    assert dados["priceUsd"] is None
    assert dados["trailerId"] is None
    assert dados["preSaleDate"] is None
    assert dados["screenshotUrls"] == []   # padrão: lista vazia
    assert dados["anticipationScore"] == 0  # padrão: zero


def test_data_invalida_e_rejeitada():
    # A validação de data deve barrar um texto que não é data — este é o ganho concreto
    # da opção idiomática escolhida (datas como tipo 'date', não como texto solto).
    with pytest.raises(ValidationError):
        Game(
            id="x",
            title="Data Ruim",
            release_date="banana",  # não é uma data → deve falhar
            platforms=[Platform.PC],
            genres=[Genre.RPG],
            developer="E",
            synopsis="...",
            cover_url="c",
            price_usd=None,
            price_brl=None,
            trailer_id=None,
            pre_sale_date=None,
        )
