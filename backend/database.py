# 'os' dá acesso às variáveis de ambiente do sistema (para ler a URL do banco de fora do código).
import os

# create_engine cria o "engine" — o objeto que gerencia a conexão real com o banco de dados.
from sqlalchemy import create_engine

# DeclarativeBase: a classe-mãe de todos os models (tabelas). sessionmaker: a fábrica de sessões.
from sqlalchemy.orm import DeclarativeBase, sessionmaker

# URL do banco: lê da variável de ambiente DATABASE_URL; se ela não existir, cai no SQLite local
# (um arquivo 'gamecountdown.db' na pasta backend/). É AQUI que a migração para outro banco acontece:
# basta definir DATABASE_URL apontando para um Postgres — sem mudar nenhuma linha de model, repositório ou rota.
DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./gamecountdown.db")

# connect_args extra só faz sentido no SQLite: por padrão ele proíbe usar a mesma conexão em threads
# diferentes, o que um servidor web faz o tempo todo. check_same_thread=False libera isso.
# Para qualquer outro banco (Postgres etc.), o dicionário fica vazio e nada muda.
connect_args = {"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {}

# O engine em si — a ponte com o banco, criada a partir da URL e dos argumentos acima.
engine = create_engine(DATABASE_URL, connect_args=connect_args)

# SessionLocal: fábrica de "sessões". Cada sessão é uma conversa curta com o banco (consultas, inserções).
# autoflush/autocommit desligados = controle explícito de quando os dados vão para o banco (via commit()).
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)


# Base é a classe da qual todos os models herdam. É por ela que o SQLAlchemy conhece o conjunto
# de tabelas do projeto (Base.metadata) — usado depois para criar as tabelas no banco.
class Base(DeclarativeBase):
    pass


# get_db: dependência do FastAPI (será usada com Depends() nas rotas). Abre uma sessão para atender
# a requisição e GARANTE o fechamento no fim, mesmo se ocorrer um erro no meio. Ainda não é usada
# por nenhuma rota — fica pronta para quando o repositório e as rotas chegarem.
def get_db():
    db = SessionLocal()   # abre a sessão
    try:
        yield db          # entrega a sessão para quem pediu (a rota) e pausa aqui
    finally:
        db.close()        # ao terminar a requisição, fecha a sessão (libera a conexão)
