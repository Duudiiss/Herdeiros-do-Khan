package br.herdeiros.model;

import static org.junit.Assert.*;

import org.junit.Test;

/** Testa a satisfação de conselheiros por meio da entrega de tesouros. */
public class ConselheiroTest {

    @Test(timeout = 2000)
    public void testeConselheiroComecaInsatisfeito() {
        Conselheiro conselheiro = new Conselheiro("General", 2, 3);

        assertEquals("O nome deve ser preservado", "General", conselheiro.getNome());
        assertEquals("A quantidade de votos deve ser preservada",
                Integer.valueOf(2), conselheiro.getVotosConcedidos());
        assertFalse("O conselheiro deve iniciar insatisfeito", conselheiro.isSatisfeito());
    }

    @Test(timeout = 2000)
    public void testeConselheiroFicaSatisfeitoAposReceberTesourosNecessarios() {
        Conselheiro conselheiro = new Conselheiro("General", 2, 2);

        conselheiro.receberTesouro();
        assertFalse("Uma entrega abaixo do necessário não deve satisfazer o conselheiro",
                conselheiro.isSatisfeito());

        conselheiro.receberTesouro();
        assertTrue("O conselheiro deve ficar satisfeito ao receber o necessário",
                conselheiro.isSatisfeito());
    }

    @Test(timeout = 2000)
    public void testeConselheiroSatisfeitoPermaneceSatisfeito() {
        Conselheiro conselheiro = new Conselheiro("General", 2, 1);
        conselheiro.receberTesouro();
        conselheiro.receberTesouro();

        assertTrue("Entregas adicionais não devem desfazer a satisfação",
                conselheiro.isSatisfeito());
    }
}
