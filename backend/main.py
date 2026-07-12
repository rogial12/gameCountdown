# Importa a classe FastAPI — o objeto central que representa a aplicação web inteira.
# É nele que registramos as rotas (endpoints) e é ele que o servidor (uvicorn) executa.
from fastapi import FastAPI

# Cria a instância da aplicação. O parâmetro title aparece na documentação
# automática que o FastAPI gera (a página /docs) — ajuda a identificar a API.
app = FastAPI(title="GameCountdown API")


# O decorador @app.get("/") registra a função abaixo como resposta para
# requisições HTTP GET no caminho raiz ("/"). Quando alguém acessar esse
# endereço, o FastAPI chama a função e devolve o que ela retornar como JSON.
@app.get("/")
def health_check() -> dict[str, str]:
    # Retorna um dicionário Python; o FastAPI o converte automaticamente para JSON.
    # Serve apenas como "sinal de vida": se isto responde, a base está de pé.
    return {"status": "ok"}
