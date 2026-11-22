package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;

/** Regressao: recursos virtuais so podem pagar uma vez por ativacao. */
public class ConsumoRecursosVirtuaisFinalTest {
    @Test
    public void moedaVirtualPodeSerGastaUmaVez() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue(cartela.desenvolvimentoAjustarVirtual("MOEDA_VIRTUAL", 1));
        assertTrue(cartela.ativarColuna(0, Personagem.OGEDEI));
        assertEquals(1, cartela.getQuantidadeMoedasVirtuais());
        assertTrue(cartela.gastarTributoComVirtuais(TipoTributo.MOEDA, 1));
        assertEquals(0, cartela.getQuantidadeMoedasVirtuais());
        assertFalse(cartela.gastarTributoComVirtuais(TipoTributo.MOEDA, 1));
    }

    @Test
    public void pagamentoMistoDescontaAmbosEImpedeReuso() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarTributo(new Tributo(TipoTributo.MOEDA));
        assertTrue(cartela.desenvolvimentoAjustarVirtual("MOEDA_VIRTUAL", 1));
        assertTrue(cartela.ativarColuna(0, Personagem.OGEDEI));
        assertTrue(cartela.gastarTributoComVirtuais(TipoTributo.MOEDA, 2));
        assertEquals(0, cartela.getQuantidadeMoedasVirtuais());
        assertEquals(0, cartela.getQuantidadeTributoDoTipo(TipoTributo.MOEDA));
        assertFalse(cartela.gastarTributoComVirtuais(TipoTributo.MOEDA, 1));
    }

    @Test
    public void gastosInvalidosNaoAlteramSaldo() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarTributo(new Tributo(TipoTributo.ESPADA));
        assertTrue(cartela.desenvolvimentoAjustarVirtual("ESPADA_VIRTUAL", 1));
        assertTrue(cartela.ativarColuna(0, Personagem.CHAGATAI));
        assertFalse(cartela.gastarTributoComVirtuais(TipoTributo.ESPADA, 3));
        assertEquals(1, cartela.getQuantidadeEspadasVirtuais());
        assertEquals(1, cartela.getQuantidadeTributoDoTipo(TipoTributo.ESPADA));
        assertTrue(cartela.gastarTributoComVirtuais(TipoTributo.ESPADA, 2));
        assertEquals(0, cartela.getQuantidadeEspadasVirtuais());
        assertEquals(0, cartela.getQuantidadeTributoDoTipo(TipoTributo.ESPADA));
    }

    @Test
    public void yurtVirtualConsumidoEExpiraNoFimDoTurno() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue(cartela.desenvolvimentoAjustarVirtual("YURT_VIRTUAL", 2));
        assertTrue(cartela.ativarColuna(0, Personagem.TOLUI));
        assertTrue(cartela.gastarTributoComVirtuais(TipoTributo.YURT, 1));
        assertEquals(1, cartela.getQuantidadeYurtsVirtuais());
        cartela.encerrarTurno();
        assertEquals(0, cartela.getQuantidadeYurtsVirtuais());
    }
}
