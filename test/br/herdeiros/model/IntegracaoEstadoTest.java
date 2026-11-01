package br.herdeiros.model;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.Test;

/** Testes de integração contra estados duplicados ou inconsistentes. */
public class IntegracaoEstadoTest {

    /** Alterações no tabuleiro individual devem aparecer no saldo do jogador. */
    @Test(timeout = 2000)
    public void testeJogadorESeuTabuleiroIndividualCompartilhamTributos() {
        Jogador jogador = new Jogador("A", Cor.AZUL, 3);
        jogador.receberTributos(TipoTributo.ESPADA, 2);

        assertEquals("O estoque físico e o saldo devem começar iguais",
                2, jogador.getTabuleiroIndividual().getTributosArmazenados().size());

        jogador.getTabuleiroIndividual().removerTributoPorTipo(TipoTributo.ESPADA);

        assertEquals("O saldo deve refletir a remoção no tabuleiro individual",
                1, jogador.quantidadeTributo(TipoTributo.ESPADA));
    }

    /** A posição exibida deve ser sempre derivada do objeto Parada. */
    @Test(timeout = 2000)
    public void testeLocalizacaoDerivadaDaParada() {
        Tabuleiro tabuleiro = new Tabuleiro(new ArrayList<Cidade>());
        Peao peao = new Peao(Cor.AZUL);

        assertEquals("Peão não registrado está fora do tabuleiro",
                "Fora do tabuleiro", peao.getLocalizacaoAtual());
        assertTrue("Deve registrar o peão inicialmente",
                tabuleiro.posicionarPeaoInicial(peao));
        assertEquals("A posição deve representar Karakorum",
                "Karakorum", peao.getLocalizacaoAtual());

        tabuleiro.adicionarParada(1, false);
        tabuleiro.conectarParadas(0, 1);
        assertTrue("O movimento para a parada vizinha deve ser válido",
                tabuleiro.moverPeao(peao, 1));
        assertEquals("A descrição deve refletir a parada atual",
                "Parada 1", peao.getLocalizacaoAtual());
    }
}
