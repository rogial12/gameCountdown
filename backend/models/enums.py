# Importa Enum — a base do Python para "conjuntos fixos e fechados de valores possíveis".
# É o equivalente ao 'enum class' do Kotlin usado em Platform.kt e Genre.kt no app.
from enum import Enum


# Platform herda de (str, Enum): cada membro É, ao mesmo tempo, um texto e um valor de enum.
# Herdar de 'str' faz o Pydantic serializar a plataforma como o seu texto ("PS5") no JSON —
# exatamente o valor que o app espera. Os valores espelham os NOMES do enum Platform.kt do app;
# o 'displayName' em português ("PlayStation 5") é coisa da UI do app e NÃO entra no contrato.
class Platform(str, Enum):
    PS5 = "PS5"                     # Sony PlayStation 5
    XBOX_SERIES = "XBOX_SERIES"     # Microsoft Xbox Series X|S
    PC = "PC"                       # computadores pessoais (Windows, Linux, Mac)
    SWITCH = "SWITCH"               # Nintendo Switch
    MOBILE = "MOBILE"               # dispositivos móveis (Android e iOS)


# Genre segue exatamente o mesmo padrão: os valores espelham os nomes do enum Genre.kt do app.
# O displayName em português ("Ação", "RPG"...) também não entra aqui — o backend só precisa
# transmitir QUAL é o gênero (a identidade), não COMO exibi-lo (responsabilidade da UI do app).
class Genre(str, Enum):
    ACTION = "ACTION"           # Ação
    RPG = "RPG"                 # RPG (role-playing game)
    ADVENTURE = "ADVENTURE"     # Aventura
    STRATEGY = "STRATEGY"       # Estratégia
    SPORTS = "SPORTS"           # Esportes
    SIMULATION = "SIMULATION"   # Simulação
    HORROR = "HORROR"           # Terror
    FIGHTING = "FIGHTING"       # Luta
