# Alterações — Herdeiros de Khan

Comparação entre **`Herdeiros-do-Khan-main(2).zip` (original)** e **`Herdeiros-do-Khan-Testes-JUnit4-Revisados.zip` (versão atual)**. Para `CidadeTest.java`, a referência é o código enviado separadamente na conversa.

## Mudanças gerais

- Classes e enums internos do Model passaram de `public` para **visibilidade de pacote**: `BaralhoDeCidades`, `Cidade`, `Cor`, `Jogador`, `NomeCidade`, `Peao`, `Regiao`, `Tabuleiro` e `TipoTributo`.
- Uso de coleções Java (`HashMap`, `List`, `Set`) para recursos, participantes, paradas, conexões e ocupação.
- Adicionadas validações de dados e operações inválidas, protegendo o estado dos objetos.
- Criados **28 testes JUnit 4**, com `timeout`, comentários de objetivo/condição inicial/resultado esperado e mensagens nas assertivas.
- Padronização da formatação, com blocos explícitos e sem expressões ternárias ou `if` de uma linha nos trechos novos.

## Mudanças por classe

### `BaralhoDeCidades.java`
- Alterada apenas a visibilidade da classe: deixou de ser `public`.

### `Cidade.java`
- Alterada apenas a visibilidade da classe: deixou de ser `public`.

### `Cor.java`, `NomeCidade.java`, `Regiao.java` e `TipoTributo.java`
- Enums passaram a ter visibilidade de pacote, sem mudanças nos valores.

### `Jogador.java`
- **Construtor:** valida nome, cor e quantidade de descendentes; impede a cor branca; inicializa recursos; atributos principais passaram a ser `final`.
- **`quantidadeTributo` / `receberTributos`:** adicionados para consultar e receber recursos por tipo, com validação de quantidades.
- **`saquearCidade`:** adicionado custo de espadas (1 no primeiro saque da cidade no turno e 2 nos posteriores); impede saques sem recursos e evita consumo em caso de falha.
- **`ganharVotos` e getters:** preservados, com ajustes de formatação.

### `Peao.java`
- **Construtor:** rejeita cor nula; cor passou a ser `final`.
- **`getParadaAtual` / `posicionarEm`:** adicionados para registrar a parada e manter a localização textual sincronizada.
- Removido o método de movimentação direta por texto (`mover`); a movimentação passa a ser validada pelo `Tabuleiro`.

### `Parada.java` — nova
- Representa uma posição do mapa, com identificador, tipo e conexões em `HashMap`.
- **`adicionarAdjacente` / `ehAdjacente`:** permitem cadastrar e consultar ligações.
- **`capacidade`:** determina o limite de ocupantes conforme o tipo de parada.

### `TipoParada.java` — novo
- Enum com `SIMPLES`, `DUPLA` e `KARAKORUM`.

### `Tabuleiro.java`
- **Construtor:** valida a lista de cidades, faz cópia dela e cadastra Karakorum como parada inicial (ID 0).
- **`adicionarParada` / `conectarParadas` / `saoAdjacentes`:** cadastram paradas e conexões bidirecionais e verificam adjacência.
- **`posicionarPeaoInicial` / `getOcupacao`:** registram os peões em Karakorum e consultam ocupação.
- **`moverPeao`:** valida registro do peão, existência do destino, adjacência e capacidade antes de mover.
- **`moverKhan`:** permite entrada do Khan no tabuleiro e valida movimentos seguintes.
- **`construirYurt` / `getDonoYurt`:** registram e consultam yurts; impedem duplicatas e construção em Karakorum.
- **`substituirCidadeConquistada`:** só adiciona substituta se a cidade anterior foi realmente removida.
- **`getCidadesDisponiveis`:** retorna cópia da lista, evitando alteração direta da coleção interna.

### `Jogo.java` — nova
- **Construtor:** valida de 2 a 5 participantes e cores diferentes; cria baralho e tabuleiro, distribui cidades iniciais e posiciona os jogadores em Karakorum.
- **Getters:** consultam tabuleiro, quantidade de jogadores e jogador por cor.
- **`jogadoresComMaisVotos`:** identifica a maior pontuação registrada, incluindo empates.

### `Main.java`
- Sem alterações.

## Arquivos de teste

- **`BaralhoDeCidadesTest.java` — novo:** 5 testes de distribuição, repetição de cidades, regiões, esgotamento e quantidade restante.
- **`TabuleiroTest.java` — novo:** 12 testes de paradas, conexões, movimentação, ocupação, Khan, yurts e proteção da lista de cidades.
- **`JogadorTest.java` — novo:** 4 testes de custo dos saques, recursos insuficientes e quantidades inválidas.
- **`JogoTest.java` — novo:** 4 testes de preparação, validação de jogadores e apuração de empates.
- **`CidadeTest.java` — comparado com o código fornecido:** preservados os **3 cenários**, os **nomes dos métodos** e as **assertivas com suas mensagens**; alterado o pacote de `br.herdeiros.model.test` para `br.herdeiros.model`, permitindo acessar `Cidade`, `NomeCidade` e `Regiao` não públicos. Adicionados `timeout = 2000` e comentários formais com objetivo, condição inicial e resultado esperado; ajustada apenas a formatação do código.

**Observação:** esta versão amplia o Model, mas não implementa todas as regras do jogo nem a futura API Singleton/Façade.
