# 3ª iteração — Ativação de colunas (etapa 4)

## Mudanças

- **TabuleiroIndividual**: registra a peça de ativação sobre cada uma das quatro colunas, a coluna ativa do turno e o saldo de cavalos; impede reativação da mesma coluna antes da recuperação.
- **AcoesColuna** (novo, interno ao Model): descreve os ícones impressos nas cartelas de Altani, Jochi, Chagatai, Tolui e Ogedei. Os valores de cavalos/punhos e os indicadores de Khan/melhoria foram transcritos das cinco imagens oficiais.
- **ResumoAtivacao** (novo, público e somente leitura): fornece estado das colunas e movimentos para View e Controller sem expor arrays mutáveis.
- **InterfacePartida** (Façade): ativar coluna, consultar estado e mover com o saldo calculado dentro do Model; emite notificações Observer somente em alterações válidas.
- **ControladorTabuleiro**: novo modo real, sem seleção arbitrária de cavalos. Os métodos de simulação antiga permanecem apenas por compatibilidade com os testes existentes.
- **JanelaCartela**: quatro botões de ativação e estado das colunas, sem o antigo botão de '+1 tributo (TESTE)'.
- **JanelaJogo**: substitui o seletor de cavalos por instruções para ativar uma coluna na cartela; o botão **Passar turno** continua sendo a única passagem de vez.
- **ControladorAtivacao** (novo): `ActionListener` explícito, sem lambda; `ControladorCartela.java` antigo e não utilizado foi removido.
- **AtivacaoColunasTest** (novo): 13 testes documentados (incluindo teste negativo, recuperação branca, variação por personagem, preservação de saldo, integração Controller e notificação Observer).

## Regras verificadas no manual

- Cada turno ativa **uma** coluna da cartela. A peça fica nessa coluna, impedindo nova ativação enquanto estiver ocupada.
- A coluna **branca** recupera todas as peças, deixando as colunas livres para o **próximo turno**.
- Cada símbolo de cavalo corresponde a um movimento entre paradas adjacentes. O número varia conforme personagem e coluna.
- Um movimento inválido não consome o saldo de cavalos; a passagem de vez exige clicar em **Passar turno**.

## Escopo ainda parcial — não é o jogo completo

- Os ícones de **punho** (coleta de tributos), **Khan** (ação obrigatória) e **melhoria** são identificados, mas as ações correspondentes ainda NÃO foram implementadas nesta etapa. O programa ainda não deve ser considerado conforme a todas as regras de turno.
- A mecânica oficial de coleta depende de províncias adjacentes, das peças presentes e do movimento; o grafo de paradas não identifica ainda todas as áreas e reservas necessárias.
- As melhorias compradas poderão acrescentar ações a uma coluna no futuro; atualmente são exibidas separadamente e NÃO aumentam os ícones da coluna.
- O jogo contém apenas um peão por jogador no Model; algumas cartelas prevêem dois peões. A seleção/alternância dos dois também está pendente.
- `InterfacePartida.simularRecebimentoTributo()` permanece no Model apenas para testes antigos; não há mais botão dessa simulação na interface normal.

## Como executar

1. Importar como projeto Eclipse, configurar JDK 25 e biblioteca JUnit 4.
2. Executar os testes do source folder `test` com **Run As > JUnit Test**.
3. Executar `Main.java`, iniciar a partida, abrir **Cartela do jogador**, clicar em uma coluna livre e mover o peão.
4. Verificar a redução do saldo, clicar **Passar turno**, voltar ao mesmo jogador em turnos futuros e testar coluna ocupada / recuperação branca.

**Validação neste ambiente:** compilação com Java 21, inicialização de `Main` em tela virtual e 105 testes aprovados utilizando um executor temporário compatível com as assertivas de JUnit 4. O executor auxiliar NÃO faz parte do projeto/ZIP. Reexecutar com JUnit 4 real no Eclipse.
