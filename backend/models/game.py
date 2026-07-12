# 'date' é o tipo de data de calendário (ano-mês-dia), usado nas colunas de datas.
from datetime import date

# Tipos de coluna do SQLAlchemy: String (texto), Integer (inteiro), Float (decimal),
# Date (data) e JSON (guarda listas/estruturas — usado para as listas de plataformas/gêneros).
from sqlalchemy import Date, Float, Integer, JSON, String

# Mapped e mapped_column: a forma moderna (SQLAlchemy 2.0) de declarar colunas com tipo explícito,
# o que dá autocompletar e checagem de tipo, além de deixar o model legível.
from sqlalchemy.orm import Mapped, mapped_column

# Base: a classe-mãe de todas as tabelas, definida na infraestrutura de banco (database.py).
from database import Base


# Game é o model SQLAlchemy — o MESMO jogo do schema Pydantic, agora como TABELA de banco.
# É o "domínio fundido ao ORM" da arquitetura da Fase 1: esta classe é, ao mesmo tempo, o objeto
# de domínio e o mapeamento da tabela. O schema Pydantic (models/schemas.py) é que fica separado,
# como contrato de API. Note: NÃO há 'isWatched' aqui — a lista pessoal é estado do usuário, local no app,
# não do catálogo; o backend não a persiste (decisão do Passo 36).
class Game(Base):
    # __tablename__ define o nome da tabela no banco.
    __tablename__ = "games"

    # --- Colunas obrigatórias ---
    id: Mapped[str] = mapped_column(String, primary_key=True)          # chave primária: o id textual do jogo (ex.: "gta6")
    title: Mapped[str] = mapped_column(String, nullable=False)         # título do jogo
    release_date: Mapped[date] = mapped_column(Date, nullable=False)   # data de lançamento confirmada
    platforms: Mapped[list[str]] = mapped_column(JSON, nullable=False) # lista de plataformas, guardada como JSON (ex.: ["PS5","PC"])
    genres: Mapped[list[str]] = mapped_column(JSON, nullable=False)    # lista de gêneros, guardada como JSON (ex.: ["ACTION"])
    developer: Mapped[str] = mapped_column(String, nullable=False)     # estúdio desenvolvedor
    synopsis: Mapped[str] = mapped_column(String, nullable=False)      # descrição para a tela de Detalhes
    cover_url: Mapped[str] = mapped_column(String, nullable=False)     # URL da capa

    # --- Colunas que aceitam nulo (o 'nullable=True' espelha o 'Type?' do Kotlin) ---
    price_usd: Mapped[float | None] = mapped_column(Float, nullable=True)   # preço em dólar; nulo = não anunciado
    price_brl: Mapped[float | None] = mapped_column(Float, nullable=True)   # preço em real; nulo = não anunciado
    trailer_id: Mapped[str | None] = mapped_column(String, nullable=True)   # ID do trailer no YouTube; nulo = sem trailer
    pre_sale_date: Mapped[date | None] = mapped_column(Date, nullable=True) # data de pré-venda; nula = sem pré-venda

    # --- Colunas com valor padrão (podem ser omitidas ao criar o jogo) ---
    # default=list gera uma lista vazia nova a cada inserção; default=0 zera a pontuação por padrão.
    screenshot_urls: Mapped[list[str]] = mapped_column(JSON, nullable=False, default=list) # capturas para o carrossel
    anticipation_score: Mapped[int] = mapped_column(Integer, nullable=False, default=0)    # pontuação de antecipação
