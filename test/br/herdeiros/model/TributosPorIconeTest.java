package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;
import br.herdeiros.mapa.MapaDados;

/** Testa a coleta de um recurso especifico exibido em um icone do tabuleiro. */
public class TributosPorIconeTest {

    /**
     * Objetivo: uma provincia pode guardar espadas e moedas sem exceder 3 pecas.
     * Condicao inicial: duas espadas cadastradas, um lugar disponivel.
     * Resultado esperado: aceita uma moeda, mas nao aceita mais uma.
     */
    @Test(timeout = 2000)
    public void testeLimiteTotalDoEstoqueCompartilhado() {
        InterfacePartida partida = new InterfacePartida();
        assertTrue("Configura estoque original", partida.configurarProvincia(Provincia.PERSIA_SUL, "ESPADA", 2));
        assertTrue("Aceita uma moeda no espaco restante",
                partida.configurarTributoAdicional(Provincia.PERSIA_SUL, "MOEDA", 1));
        assertFalse("Nao ultrapassa tres tributos na provincia",
                partida.configurarTributoAdicional(Provincia.PERSIA_SUL, "YURT", 1));
        assertEquals("A moeda permanece cadastrada", 1,
                partida.consultarQuantidadeTributoProvincia(Provincia.PERSIA_SUL, "MOEDA"));
    }

    /**
     * Objetivo: o tipo clicado deve ser debitado do estoque e creditado ao jogador.
     * Condicao inicial: provincia visitada, coluna com punhos e moedas disponiveis.
     * Resultado esperado: a provincia perde uma moeda e o jogador ganha uma.
     */
    @Test(timeout = 2000)
    public void testeColetaDeMoedaAtualizaEstoqueEJogador() {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.20, 0.20, false);
        mapa.conectar(0, 1);
        assertTrue("O mapa deve carregar", partida.carregarMapa(mapa));
        assertTrue("Configura estoque", partida.configurarProvincia(Provincia.PERSIA_SUL, "ESPADA", 1));
        assertTrue("Inclui moeda na mesma provincia",
                partida.configurarTributoAdicional(Provincia.PERSIA_SUL, "MOEDA", 1));
        assertTrue("Permite acesso a partir da parada 1",
                partida.configurarAcessoProvincia(Provincia.PERSIA_SUL, 1));
        assertTrue("A coluna deve ativar", partida.ativarColunaJogadorAtual(0));
        assertTrue("A parada de acesso deve ser alcancada", partida.movimentarJogadorComAtivacao(1));
        int anterior = partida.consultarCartelaJogadorAtual().getMoedas();
        assertTrue("O clique de coleta de moeda deve funcionar",
                partida.coletarTributo(Provincia.PERSIA_SUL, "MOEDA"));
        assertEquals("O estoque de moedas diminui uma unidade", 0,
                partida.consultarQuantidadeTributoProvincia(Provincia.PERSIA_SUL, "MOEDA"));
        assertEquals("O jogador recebe a moeda recolhida", anterior + 1,
                partida.consultarCartelaJogadorAtual().getMoedas());
    }
}
