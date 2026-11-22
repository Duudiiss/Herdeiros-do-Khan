package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;
import br.herdeiros.mapa.MapaDados;

/** Garante que tributo.5 nao herde todas as paradas da provincia Russia Leste. */
public class AcessoEspadaLesteTest {
    private InterfacePartida criarPartidaNaParada(int idParada) {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Azul", "Vermelho"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.ALTANI, Personagem.JOCHI}, false);
        MapaDados mapa = new MapaDados();
        assertTrue(mapa.adicionar(3, 0.537267, 0.230690, false));
        assertTrue(mapa.adicionar(24, 0.737342, 0.430221, false));
        assertTrue(mapa.conectar(0, 3));
        assertTrue(mapa.conectar(0, 24));
        assertTrue(partida.carregarMapa(mapa));
        assertTrue(partida.configurarProvincia(Provincia.RUSSIA_LESTE, "YURT", 1));
        assertTrue(partida.configurarProvincia(Provincia.CHINA_LESTE, "ESPADA", 1));
        assertTrue(partida.configurarAcessoProvincia(Provincia.RUSSIA_LESTE, 3));
        assertTrue(partida.configurarAcessoProvincia(Provincia.CHINA_LESTE, 24));
        assertTrue(partida.ativarColunaJogadorAtual(0));
        assertTrue(partida.movimentarJogadorComAtivacao(idParada));
        return partida;
    }

    @Test(timeout = 3000)
    public void paradaTresNaoPodeColetarEspadaDistante() {
        InterfacePartida partida = criarPartidaNaParada(3);
        int estoque = partida.consultarQuantidadeTributoIcone(5);
        assertFalse(partida.coletarTributoDoIcone(5,
                new Provincia[] {Provincia.RUSSIA_LESTE, Provincia.CHINA_LESTE}, "ESPADA"));
        assertEquals(estoque, partida.consultarQuantidadeTributoIcone(5));
        assertEquals(0, partida.consultarCartelaJogadorAtual().getEspadas());
    }

    @Test(timeout = 3000)
    public void paradaVinteQuatroPodeColetarEspadaProxima() {
        InterfacePartida partida = criarPartidaNaParada(24);
        int estoque = partida.consultarQuantidadeTributoIcone(5);
        int espadas = partida.consultarCartelaJogadorAtual().getEspadas();
        assertTrue(partida.coletarTributoDoIcone(5,
                new Provincia[] {Provincia.RUSSIA_LESTE, Provincia.CHINA_LESTE}, "ESPADA"));
        assertEquals(estoque - 1, partida.consultarQuantidadeTributoIcone(5));
        assertEquals(espadas + 1, partida.consultarCartelaJogadorAtual().getEspadas());
    }
}
