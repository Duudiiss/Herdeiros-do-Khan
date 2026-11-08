# Terceira iteração — turnos e personagens

## Alterações principais
- Corrigida a movimentação simulada: cada deslocamento consome o número mínimo de conexões percorridas (BFS). O jogador pode realizar vários cliques válidos com seu saldo restante.
- A mudança de jogador acontece **somente** ao pressionar **Passar turno**, mesmo após um movimento válido ou após gastar todos os movimentos.
- O seletor de movimentos é bloqueado após o primeiro deslocamento do turno e reativado no turno seguinte, impedindo aumentar o orçamento no meio da jogada.
- No menu inicial, cada participante escolhe uma cor e um personagem de forma independente: Altani, Jochi, Chagatai, Tolui e Ogedei, ou **Aleatorio**. Personagens não podem repetir; opções aleatórias usam apenas os disponíveis.
- A ordem dos jogadores é embaralhada ao iniciar uma partida pela interface gráfica, sem alterar a associação entre nome, cor e personagem.
- A imagem exibida na janela da cartela é selecionada pelo personagem, não pela posição na ordem da partida.
- Compatibilidade preservada: construtores antigos de `InterfacePartida` continuam deterministas para os testes existentes.

## Arquivos modificados / criados
- Model: `Personagem.java` (novo), `Jogador.java`, `GerenciadorPartida.java`, `InterfacePartida.java`, `Tabuleiro.java`.
- Controller: `ControladorTabuleiro.java`.
- View: `JanelaInicial.java`, `Janela.java`, `JanelaJogo.java`, `JanelaCartela.java`.
- Testes: `PersonagensTurnosTest.java` (novo), com oito casos documentados.

## Testar no Eclipse
1. `Project → Clean`; executar os testes do projeto com **JUnit 4**.
2. Abrir `Main.java`; iniciar com dois ou mais participantes, escolhendo personagens diferentes ou Aleatorio.
3. Verificar que a ordem pode mudar e que o nome, a cor e o personagem de cada participante permanecem ligados.
4. Selecionar 3 movimentos e clicar em um destino a duas conexões: deve restar um movimento. Realizar outro clique sem mudar a vez.
5. Pressionar **Passar turno**: a vez muda e o orçamento de movimentos é restaurado.
6. Abrir a cartela individual e verificar se ela corresponde ao personagem escolhido.

**Limitação:** a quantidade de cavalos ainda é um recurso *de simulação*, pois as regras completas de ativação das colunas da cartela não estão integradas.
