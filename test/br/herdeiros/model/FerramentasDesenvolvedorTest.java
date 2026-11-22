package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;

/** Testes da API de diagnostico, independentes da interface grafica. */
public class FerramentasDesenvolvedorTest {

    private InterfacePartida partida() {
        return new InterfacePartida(new String[] {"Ana", "Beto"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.ALTANI, Personagem.JOCHI}, false);
    }

    @Test(timeout = 2000)
    public void testeAdicionarERemoverRecursosPorJogador() {
        InterfacePartida p = partida();
        assertTrue(p.desenvolvimentoAjustarTributo(1, "ESPADA", 3));
        assertEquals(3, p.consultarCartela(1).getEspadas());
        assertTrue(p.desenvolvimentoAjustarTributo(1, "ESPADA", -2));
        assertEquals(1, p.consultarCartela(1).getEspadas());
        assertFalse(p.desenvolvimentoAjustarTributo(1, "ESPADA", -2));
        assertEquals(1, p.consultarCartela(1).getEspadas());
        assertFalse(p.desenvolvimentoAjustarTributo(4, "MOEDA", 1));
    }

    @Test(timeout = 2000)
    public void testeDestinoKhanEPontuacao() {
        InterfacePartida p = partida();
        assertTrue(p.desenvolvimentoPosicionarKhan(DestinoKhan.CHINA_LESTE));
        assertEquals(DestinoKhan.CHINA_LESTE, p.consultarDestinoKhan());
        assertFalse(p.desenvolvimentoPosicionarKhan(DestinoKhan.CHINA_LESTE));
        assertTrue(p.desenvolvimentoAlterarVotos(0, 2));
        assertEquals(2, p.consultarVotosJogador(0));
        assertEquals(2, p.consultarPontuacaoKhan());
        assertTrue(p.desenvolvimentoAjustarPontuacaoKhan(3));
        assertEquals(5, p.consultarPontuacaoKhan());
        assertTrue(p.desenvolvimentoAjustarPontuacaoKhan(-3));
        assertEquals(2, p.consultarPontuacaoKhan());
    }

    @Test(timeout = 2000)
    public void testeEncerramentoManualBloqueiaOutrasAlteracoes() {
        InterfacePartida p = partida();
        assertTrue(p.desenvolvimentoEncerrarPartida());
        assertTrue(p.isPartidaEncerrada());
        assertFalse(p.desenvolvimentoEncerrarPartida());
        assertFalse(p.desenvolvimentoAjustarTributo(0, "YURT", 1));
        assertFalse(p.desenvolvimentoAlterarVotos(0, 1));
        assertFalse(p.desenvolvimentoPosicionarKhan(DestinoKhan.MELHORIAS));
    }
}
