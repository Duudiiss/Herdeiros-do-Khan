# 3ª iteração — Consulta de cidades e cartela

## Alterações

- Retirada a informação redundante **Colunas: 4** do cabeçalho da cartela. O atributo e seu getter continuam no Model para a futura implementação das ativações.
- Nova janela **Consultar cidades abertas**, com nome, região, tesouros restantes e estado de conquista das três cidades inicialmente sorteadas.
- `InterfacePartida.consultarCidadesDisponiveis()` retorna uma lista imutável de `ResumoCidade`, evitando acesso da View às classes internas.
- `JanelaCidades` implementa `ObservadorPartida`, registra-se na Façade e remove seu registro ao ser fechada.
- Quatro testes JUnit 4 novos em `ResumoCidadesTest.java`.

## Limitações

- A janela é uma consulta ao estado das **cidades abertas**, não uma representação das cartas particulares dos jogadores.
- Ainda não há uma ação oficial de saque integrada ao Controller; nenhuma regra ou localização de cidade foi inventada.
- O botão **+1 tributo (TESTE)** permanece apenas para demonstrar o Observer e não corresponde a uma ação válida do jogo.
- As regras de ativação das quatro colunas e a associação correta de cartelas a personagens ainda precisam ser implementadas.

## Verificação no Eclipse

1. Execute todos os testes como **JUnit 4**.
2. Execute `Main.java` e inicie a partida.
3. Abra **Abrir cartela do jogador**: a quantidade fixa de colunas não aparece mais no cabeçalho.
4. Abra **Consultar cidades abertas**: devem surgir as três cidades sorteadas.
5. Feche e reabra a janela das cidades, e troque a vez para conferir que a janela permanece funcional.
