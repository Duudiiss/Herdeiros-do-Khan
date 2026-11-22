package br.herdeiros.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.Test;

/** Testes das regras de compra no turno e custo de acesso aos proprios yurts. */
public class CorrecaoCompraMelhoriasYurtTest {

    @Test(timeout = 2000)
    public void comprarMelhoriaNaoImpedeUsarColunaMasAdiaBeneficio() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoria(new Melhoria("Dois cavalos", 2, 0, "CAV-R", 0));
        assertTrue(cartela.consultarAtivacao(Personagem.ALTANI).podeAtivar(0));
        assertTrue(cartela.ativarColuna(0, Personagem.ALTANI));
        assertEquals(1, cartela.consultarAtivacao(Personagem.ALTANI).getMovimentosRestantes());
        cartela.encerrarTurno();
        assertFalse(cartela.ativarColuna(0, Personagem.ALTANI));
        assertTrue(cartela.ativarColuna(3, Personagem.ALTANI));
        cartela.encerrarTurno();
        assertTrue(cartela.ativarColuna(0, Personagem.ALTANI));
        assertEquals(3, cartela.consultarAtivacao(Personagem.ALTANI).getMovimentosRestantes());
    }

    @Test(timeout = 2000)
    public void colunaBrancaContinuaDisponivelAposReceberMelhoria() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoria(new Melhoria("Cavalos", 2, 0, "CAV-R", 0));
        cartela.adicionarMelhoria(new Melhoria("Cavalos", 2, 0, "CAV-V", 1));
        cartela.adicionarMelhoria(new Melhoria("Cavalos", 2, 0, "CAV-A", 2));
        assertEquals(1, cartela.getLinhasBrancasPendentes());
        assertTrue(cartela.receberMelhoriaBranca(new Melhoria("Cavalos", 2, 0, "CAV-R", 0)));
        assertTrue(cartela.consultarAtivacao(Personagem.ALTANI).podeAtivar(3));
        assertTrue(cartela.ativarColuna(3, Personagem.ALTANI));
        assertEquals(2, cartela.consultarAtivacao(Personagem.ALTANI).getMovimentosRestantes());
    }

    @Test(timeout = 2000)
    public void caminhoAteYurtCustaUmMasExigeDoisCavalos() {
        Tabuleiro tabuleiro = new Tabuleiro(new ArrayList<Cidade>());
        assertTrue(tabuleiro.adicionarParada(1, false));
        assertTrue(tabuleiro.adicionarParada(2, false));
        assertTrue(tabuleiro.conectarParadas(0, 1));
        assertTrue(tabuleiro.conectarParadas(1, 2));
        assertTrue(tabuleiro.construirYurt(2, Cor.AZUL));
        Peao peao = new Peao(Cor.AZUL);
        assertTrue(tabuleiro.posicionarPeaoInicial(peao));
        assertEquals(1, tabuleiro.distanciaMinima(peao, 2, Cor.AZUL, 2));
        assertEquals(-1, tabuleiro.distanciaMinima(peao, 2, Cor.AZUL, 1));
        assertFalse(tabuleiro.moverPeaoAte(peao, 2, 1, Cor.AZUL));
        assertTrue(tabuleiro.moverPeaoAte(peao, 2, 2, Cor.AZUL));
    }
}
