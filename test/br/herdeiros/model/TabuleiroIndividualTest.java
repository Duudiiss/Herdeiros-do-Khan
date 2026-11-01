package br.herdeiros.model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

/** Testa o armazenamento de tributos e melhorias do tabuleiro individual. */
public class TabuleiroIndividualTest {

    @Test(timeout = 2000)
    public void testeTabuleiroIndividualComecaSemItens() {
        TabuleiroIndividual tabuleiro = new TabuleiroIndividual();

        assertTrue("O tabuleiro deve começar sem tributos",
                tabuleiro.getTributosArmazenados().isEmpty());
        assertTrue("O tabuleiro deve começar sem melhorias",
                tabuleiro.getMelhoriasAdquiridas().isEmpty());
    }

    @Test(timeout = 2000)
    public void testeAdicionarEremoverTributosPorTipo() {
        TabuleiroIndividual tabuleiro = new TabuleiroIndividual();
        Tributo espada = new Tributo(TipoTributo.ESPADA);
        Tributo moeda = new Tributo(TipoTributo.MOEDA);
        tabuleiro.adicionarTributo(espada);
        tabuleiro.adicionarTributo(moeda);

        assertEquals("Os dois tributos devem ser armazenados",
                2, tabuleiro.getTributosArmazenados().size());
        assertTrue("O tributo do tipo espada deve ser removido",
                tabuleiro.removerTributoPorTipo(TipoTributo.ESPADA));
        assertEquals("A remoção deve preservar o tributo de outro tipo",
                1, tabuleiro.getTributosArmazenados().size());
        assertSame("O tributo restante deve ser a moeda",
                moeda, tabuleiro.getTributosArmazenados().get(0));
        assertFalse("Um tipo sem tributo correspondente não deve ser removido",
                tabuleiro.removerTributoPorTipo(TipoTributo.YURT));
    }

    @Test(timeout = 2000)
    public void testeAdicionarMelhorias() {
        TabuleiroIndividual tabuleiro = new TabuleiroIndividual();
        Melhoria melhoria = new Melhoria("Estrada comercial", 3, 2);

        tabuleiro.adicionarMelhoria(melhoria);

        assertEquals("A melhoria deve ser armazenada",
                1, tabuleiro.getMelhoriasAdquiridas().size());
        assertSame("A melhoria armazenada deve ser a mesma recebida",
                melhoria, tabuleiro.getMelhoriasAdquiridas().get(0));
    }

    @Test(timeout = 2000)
    public void testeGettersNaoExibemAsListasInternas() {
        TabuleiroIndividual tabuleiro = new TabuleiroIndividual();
        tabuleiro.adicionarTributo(new Tributo(TipoTributo.ESPADA));
        tabuleiro.adicionarMelhoria(new Melhoria("Estrada comercial", 3, 2));

        List<Tributo> tributos = tabuleiro.getTributosArmazenados();
        List<Melhoria> melhorias = tabuleiro.getMelhoriasAdquiridas();
        tributos.clear();
        melhorias.clear();

        assertEquals("Alterar a lista retornada não deve remover tributos internos",
                1, tabuleiro.getTributosArmazenados().size());
        assertEquals("Alterar a lista retornada não deve remover melhorias internas",
                1, tabuleiro.getMelhoriasAdquiridas().size());
    }
}
