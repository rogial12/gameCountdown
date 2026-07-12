# 'date' representa uma data de calendário (ano-mês-dia), sem hora. É o tipo que valida
# o formato "2026-08-15" na entrada e o serializa de volta como esse mesmo texto no JSON.
from datetime import date

# BaseModel: a classe base do Pydantic para "modelos de dados validados" — ela checa os tipos,
# rejeita entradas inválidas e sabe converter o objeto para JSON.
# ConfigDict: o objeto que configura o comportamento de um modelo.
from pydantic import BaseModel, ConfigDict

# to_camel: função pronta do Pydantic que converte um nome snake_case (release_date)
# para camelCase (releaseDate). É o que mantém o código Python idiomático e o JSON fiel ao app.
from pydantic.alias_generators import to_camel

# Importa os enums de plataforma e gênero definidos ao lado, na mesma camada de modelos.
from .enums import Genre, Platform


# Game é o SCHEMA PÚBLICO — o contrato de API que o app Android consome. Espelha o Game.kt do app,
# com duas ausências deliberadas (decididas por Igor no Passo 36):
#   • NÃO tem 'isWatched' — a lista pessoal é local no app; o backend não conhece usuário.
#   • NÃO tem countdown — o app calcula os "dias até o lançamento" a partir da releaseDate.
class Game(BaseModel):
    # model_config ajusta o comportamento do modelo inteiro:
    model_config = ConfigDict(
        # alias_generator: para CADA campo, gera automaticamente um "apelido" em camelCase, usado no JSON.
        # Resultado: o código fica em snake_case (padrão Python) e o JSON sai em camelCase (o que o app espera).
        alias_generator=to_camel,
        # populate_by_name: permite construir o objeto tanto pelo nome Python (release_date=...)
        # quanto pelo apelido (releaseDate=...). Facilita montar o Game a partir do banco e dos testes.
        populate_by_name=True,
    )

    # --- Campos obrigatórios (sempre presentes) ---
    id: str                          # identificador único do jogo (ex.: "gta6")
    title: str                       # título do jogo
    release_date: date               # data de lançamento confirmada → JSON "releaseDate": "2026-08-15"
    platforms: list[Platform]        # plataformas onde o jogo sai (lista de enums Platform)
    genres: list[Genre]              # gêneros do jogo (lista de enums Genre)
    developer: str                   # estúdio que desenvolve o jogo
    synopsis: str                    # descrição para a tela de Detalhes
    cover_url: str                   # URL da imagem de capa → JSON "coverUrl"

    # --- Campos obrigatórios, porém aceitam nulo (o '| None' espelha o 'Type?' do Kotlin) ---
    price_usd: float | None          # preço em dólar; None = não anunciado → JSON "priceUsd"
    price_brl: float | None          # preço em real; None = não anunciado → JSON "priceBrl"
    trailer_id: str | None           # ID do vídeo no YouTube; None = sem trailer → JSON "trailerId"
    pre_sale_date: date | None       # data de pré-venda; None = sem pré-venda → JSON "preSaleDate"

    # --- Campos com valor padrão (podem ser omitidos ao criar o Game) ---
    screenshot_urls: list[str] = []  # capturas para o carrossel; padrão: lista vazia → JSON "screenshotUrls"
    anticipation_score: int = 0      # pontuação de antecipação (ordenação "mais aguardados") → JSON "anticipationScore"
