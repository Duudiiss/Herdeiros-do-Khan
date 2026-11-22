package br.herdeiros.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Regressao: melhoria inicial de Jochi so concede beneficio no proximo turno. */
public class JochiMelhoriaInicialTurnoTest {

    @Test(timeout = 2000)
    public void melhoriaInicialDeCavalosNaoValeNoPrimeiroTurno() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoriaInicial(new Melhoria("Dois cavalos", 0, 0, "CAV-R", 3));

        assertTrue(cartela.ativarColuna(3, Personagem.JOCHI));
        assertEquals(2, cartela.consultarAtivacao(Personagem.JOCHI).getMovimentosRestantes());

        cartela.encerrarTurno();
        assertTrue(cartela.ativarColuna(3, Personagem.JOCHI));
        assertEquals(4, cartela.consultarAtivacao(Personagem.JOCHI).getMovimentosRestantes());
    }

    @Test(timeout = 2000)
    public void melhoriaInicialDePunhosNaoValeNoPrimeiroTurno() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoriaInicial(new Melhoria("Punho", 0, 0, "MAO-V", 3));

        assertTrue(cartela.ativarColuna(3, Personagem.JOCHI));
        assertEquals(0, cartela.consultarAtivacao(Personagem.JOCHI).getPunhosDaColuna());

        cartela.encerrarTurno();
        assertTrue(cartela.ativarColuna(3, Personagem.JOCHI));
        assertEquals(1, cartela.consultarAtivacao(Personagem.JOCHI).getPunhosDaColuna());
    }
}
