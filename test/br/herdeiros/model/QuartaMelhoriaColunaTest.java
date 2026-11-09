package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;

/** Testes para o quarto espaco de melhoria nas colunas coloridas. */
public class QuartaMelhoriaColunaTest {

    /**
     * Objetivo: confirmar que quatro melhorias sao aceitas por coluna colorida.
     * Condicao inicial: cartela vazia com melhorias distintas da coluna vermelha.
     * Resultado esperado: quarto espaco disponivel; quinto rejeitado por capacidade.
     */
    @Test(timeout = 2000)
    public void testeColunaColoridaComportaQuatroMelhorias() {
        TabuleiroIndividual tabuleiro = new TabuleiroIndividual();
        for (int i = 0; i < 4; i++) {
            assertTrue("A coluna deve aceitar a melhoria de numero " + (i + 1),
                    tabuleiro.podeAdicionarMelhoria(0));
            tabuleiro.adicionarMelhoria(new Melhoria("Melhoria " + i, 2, 0, "TST-" + i, 0));
        }
        assertFalse("A quinta melhoria da mesma coluna deve ser recusada",
                tabuleiro.podeAdicionarMelhoria(0));
        assertEquals("O armazenamento deve preservar as quatro melhorias", 4,
                tabuleiro.contarMelhoriasNaColuna(0));
    }
}
