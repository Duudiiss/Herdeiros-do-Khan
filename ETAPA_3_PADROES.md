# Terceira iteracao - etapa 1: arquitetura

## Padroes
- **Singleton**: `GerenciadorPartida` possui construtor privado e `getInstancia()`; cria/substitui a partida ativa.
- **Facade**: `InterfacePartida` e a API publica do Model; nao expoe `Jogo`, `Tabuleiro` ou `Peao` para o Controller.
- **Observer**: `ObservadoPartida` e `ObservadorPartida` sao interfaces; `InterfacePartida` notifica, `PainelTabuleiro` redesenha e `JanelaJogo` atualiza a indicacao da vez.
- **Controller**: `ControladorTabuleiro` decide quando avancar a vez. Movimento invalido nao avanca.

## Outras correcoes
- Lambdas de `ActionListener` removidas de `JanelaInicial`.
- A tela inicial limita jogadores a cinco, sem oferecer a cor branca do Khan.
- Novo teste de Singleton/Observer; testes de movimentacao adaptados ao papel do Controller.
- Mantida a separacao entre `src` e `test` e o JUnit 4.

## Ainda falta nesta iteracao
- Integrar cartelas, cartas, melhorias e saques a interface grafica e ao Observer.
- Validar o mapeamento completo e as regras especiais de movimento; seletor de cavalos e provisório.
- O salvamento/carregamento e escopo da quarta iteracao.

## Verificacao
- Fontes compiladas com Java 21 na verificacao local.
- 74 metodos de teste validados por um executor temporario compatível com a API das assertivas JUnit 4; **o JUnit 4 real ainda precisa ser executado no Eclipse/JDK 25**.
- Uma assertiva antiga de `TabuleiroTest` comparava enum com objeto `Parada`; foi corrigida para consultar `getTipo()`.
- Os testes existentes de movimentacao foram alterados para refletir que o Controller avancara a vez.
- Nao foram introduzidas bibliotecas auxiliares no projeto entregue.
