# Terceira iteracao — Etapa 2: cartelas e Observer

## Mudancas

- `JanelaCartela` e `PainelCartela`: exibem as imagens oficiais das cartelas com `Graphics2D.drawImage()`; janela acompanha o jogador da vez via `ObservadorPartida`.
- `ResumoCartela`: objeto de consulta imutavel; a View nao recebe listas internas do Model.
- `InterfacePartida`: consulta da cartela pela Facade e notificacoes quando ha mudanca de tributos.
- `ControladorCartela`: coordena a demonstracao de recebimento de tributos por `ActionListener`, sem lambda.
- `JanelaJogo`: botao para abrir cartela e fechamento correto da janela anterior ao iniciar nova partida.
- `CartelaObserverTest`: seis novos testes JUnit 4.

## Como executar

1. Ajustar JDK 25 e JUnit 4 no Eclipse, se necessario.
2. Executar todos os testes do source folder `test`.
3. Executar `Main.java` e iniciar partida de 2 a 5 jogadores.
4. Clicar em `Abrir cartela do jogador`.
5. Testar o botao `+1 tributo (TESTE)` e observar os valores serem atualizados automaticamente.
6. Passar a vez na tela principal e observar a cartela alternar para o proximo jogador.

## Limitacoes documentadas

- As imagens das cinco cartelas sao associadas, por enquanto, a ordem dos participantes (indices 0 a 4). Uma selecao de personagem, baseada nas cartas e nas regras, ainda precisa substituir essa associacao provisoria.
- O botao `+1 tributo (TESTE)` **nao representa uma acao real do jogo**, nao consome recursos e serve somente para verificar a integracao visual. Nao deve ser apresentado como regra completa.
- As melhorias sao listadas a partir do Model, mas sua compra/ativacao ainda nao esta conectada a interface.
- As cartelas nao representam visualmente cada tributo posicionado em coluna: mostram a imagem oficial e os totais textuais do Model.
- A janela de cartas e a sequencia completa de ativacao de colunas ainda sao pendentes.
- O salvamento e recuperacao permanecem reservados para a quarta iteracao.
