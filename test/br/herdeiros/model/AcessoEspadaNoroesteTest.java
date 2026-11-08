package br.herdeiros.model;

import static org.junit.Assert.*;
import br.herdeiros.mapa.MapaDados;
import org.junit.Test;

/** Verifica o ponto de acesso à espada a oeste de Moscou (parada 2). */
public class AcessoEspadaNoroesteTest {

    /** A parada junto ao símbolo da espada pode coletar, desde que haja punhos. */
    @Test(timeout = 2000)
    public void testeParada2DaAcessoAoTributoDeEspadaNaRussiaOeste() {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        mapa.adicionar(2, 0.4379, 0.1689, false);
        mapa.conectar(0, 2);
        assertTrue("O mapa precisa carregar", partida.carregarMapa(mapa));
        assertTrue("A província deve ser configurada com espadas", 
                partida.configurarProvincia(Provincia.RUSSIA_OESTE, "ESPADA", 2));
        assertTrue("A parada 2 deve constar como acesso ao ícone",
                partida.configurarAcessoProvincia(Provincia.RUSSIA_OESTE, 2));
        assertTrue("A coluna deve ser ativada", partida.ativarColunaJogadorAtual(0));
        assertTrue("O jogador deve alcançar a parada 2", 
                partida.movimentarJogadorComAtivacao(2));
        assertTrue("A espada adjacente deve ser coletável",
                partida.coletarTributoDaPilha(new Provincia[] {Provincia.RUSSIA_OESTE}, "ESPADA"));
        assertEquals("O jogador recebeu uma espada", 1,
                partida.consultarCartelaJogadorAtual().getEspadas());
    }
}
