package br.herdeiros.model;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;
import br.herdeiros.mapa.MapaDados;
import org.junit.Test;

/** Testa a ligação do mapa persistido com as regras do Model. */
public class InterfacePartidaMapaTest {
    @Test(timeout = 2000)
    public void testePartidaCriaPeoesParaCincoJogadores() {
        InterfacePartida partida = new InterfacePartida(5);
        Set<Cor> cores = new HashSet<>();

        assertEquals(5, partida.getQuantidadeJogadores());
        assertEquals(5, partida.getOcupacao(0));
        for (int i = 0; i < partida.getQuantidadeJogadores(); i++) {
            assertEquals(0, partida.getParadaJogador(i));
            assertTrue(cores.add(partida.getCorJogador(i)));
        }
    }

    @Test(timeout = 2000)
    public void testePartidaRejeitaMaisDeCincoJogadores() {
        try {
            new InterfacePartida(6);
            fail("Partida com seis jogadores deveria ser rejeitada");
        } catch (IllegalArgumentException esperado) {
            // A partida permite no máximo cinco jogadores.
        }
    }

    /** Objetivo: garantir que o mapa seja incorporado ao tabuleiro do jogo.
     * Condição inicial: paradas conectadas a Karakorum; resultado esperado: movimento aceito. */
    @Test(timeout = 2000)
    public void testeMapaCarregadoPermiteMovimentoAdjacente() {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.4, 0.4, false);
        mapa.conectar(0, 1);
        assertTrue("Mapa deve ser aceito", partida.carregarMapa(mapa));
        assertTrue("Peão deve se mover pela ligação cadastrada", partida.movimentarJogador(0, 1));
    }

    /** Objetivo: impedir o uso de uma parada sem conexão.
     * Condição inicial: parada sem rota com a posição inicial; resultado esperado: peão permanece em 0. */
    @Test(timeout = 2000)
    public void testeMapaSemConexaoNaoPermiteMovimento() {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.4, 0.4, false);
        partida.carregarMapa(mapa);
        assertFalse("Não pode deslocar por rota inexistente", partida.movimentarJogador(0, 1));
        assertEquals("Peão deve permanecer em Karakorum", 0, partida.getParadaJogador(0));
    }
}
