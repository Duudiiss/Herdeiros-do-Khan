package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;
import br.herdeiros.mapa.MapaDados;

/** Testes da movimentação em trajetos e da alternância dos jogadores. */
public class MovimentacaoTurnosTest {

    private InterfacePartida criarPartida() {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.20, 0.20, false);
        mapa.adicionar(2, 0.30, 0.30, false);
        mapa.adicionar(3, 0.40, 0.40, true);
        mapa.adicionar(4, 0.50, 0.50, false);
        mapa.conectar(0, 1);
        mapa.conectar(1, 2);
        mapa.conectar(2, 3);
        assertTrue("O mapa de preparação do teste deve carregar", partida.carregarMapa(mapa));
        return partida;
    }

    /** Objetivo: validar trajetos de duas conexões e a alternância da vez.
     * Condição inicial: jogador inicial em Karakorum, conectado à parada 2 por duas arestas.
     * Resultado esperado: peão chega ao destino e a vez passa para o outro jogador. */
    @Test(timeout = 2000)
    public void testeDuasConexoesComDoisMovimentos() {
        InterfacePartida partida = criarPartida();
        assertTrue("Dois movimentos devem permitir alcançar a parada 2",
                partida.movimentarJogadorAtual(2, 2));
        assertEquals("O peão deve terminar na parada 2", 2, partida.getParadaJogador(0));
        assertEquals("Uma jogada válida deve trocar a vez", 1, partida.getIndiceJogadorAtual());
    }

    /** Objetivo: rejeitar trajeto acima do orçamento de movimentos.
     * Condição inicial: um movimento para atingir uma parada distante duas conexões.
     * Resultado esperado: posição e vez inalteradas. */
    @Test(timeout = 2000)
    public void testeDistanciaMaiorQueMovimentosPreservaEstado() {
        InterfacePartida partida = criarPartida();
        assertFalse("A parada 2 exige mais de um movimento",
                partida.movimentarJogadorAtual(2, 1));
        assertEquals("O peão deve continuar em Karakorum", 0, partida.getParadaJogador(0));
        assertEquals("A jogada inválida não pode trocar a vez", 0, partida.getIndiceJogadorAtual());
    }

    /** Objetivo: permitir passar por posição ocupada sem parar nela.
     * Condição inicial: azul termina na parada 1, que é simples; vermelho está em Karakorum.
     * Resultado esperado: vermelho atravessa a parada 1 até a 2. */
    @Test(timeout = 2000)
    public void testeAtravessarParadaOcupada() {
        InterfacePartida partida = criarPartida();
        assertTrue("Azul deve ocupar a parada 1", partida.movimentarJogadorAtual(1, 1));
        assertTrue("Vermelho deve atravessar uma parada ocupada",
                partida.movimentarJogadorAtual(2, 2));
        assertEquals("Vermelho deve alcançar a parada 2", 2, partida.getParadaJogador(1));
    }

    /** Objetivo: impedir que dois peões terminem numa parada simples.
     * Condição inicial: azul já ocupa a parada 1.
     * Resultado esperado: vermelho permanece em Karakorum e não perde a vez. */
    @Test(timeout = 2000)
    public void testeDestinoSimplesOcupadoRejeitado() {
        InterfacePartida partida = criarPartida();
        partida.movimentarJogadorAtual(1, 1);
        assertFalse("Não se pode terminar em uma parada simples ocupada",
                partida.movimentarJogadorAtual(1, 1));
        assertEquals("Peão rejeitado continua na posição anterior", 0, partida.getParadaJogador(1));
        assertEquals("A vez deve continuar com o jogador vermelho", 1, partida.getIndiceJogadorAtual());
    }

    /** Objetivo: preservar o limite de ocupação de paradas duplas.
     * Condição inicial: parada dupla sem ocupantes.
     * Resultado esperado: azul e vermelho podem terminar na mesma parada dupla. */
    @Test(timeout = 2000)
    public void testeParadaDuplaAceitaDoisPeoes() {
        InterfacePartida partida = criarPartida();
        assertTrue("Azul deve chegar à parada dupla", partida.movimentarJogadorAtual(3, 3));
        assertTrue("Vermelho também deve chegar à parada dupla", partida.movimentarJogadorAtual(3, 3));
        assertEquals("A parada dupla deve receber os dois peões", 2, partida.getOcupacao(3));
    }

    /** Objetivo: verificar a passagem da vez e o retorno ao primeiro jogador.
     * Condição inicial: dois participantes e nenhuma movimentação.
     * Resultado esperado: o índice circula de 0 para 1 e volta para 0. */
    @Test(timeout = 2000)
    public void testePassagemDeVezCircular() {
        InterfacePartida partida = criarPartida();
        partida.avancarJogador();
        assertEquals("Após azul deve jogar vermelho", 1, partida.getIndiceJogadorAtual());
        partida.avancarJogador();
        assertEquals("Após vermelho deve jogar azul", 0, partida.getIndiceJogadorAtual());
    }

    /** Objetivo: rejeitar movimento com orçamento zero.
     * Condição inicial: uma parada vizinha e jogador inicial em Karakorum.
     * Resultado esperado: nenhum deslocamento e nenhuma troca de vez. */
    @Test(timeout = 2000)
    public void testeZeroMovimentosNaoAlteraEstado() {
        InterfacePartida partida = criarPartida();
        assertFalse("Não se pode mover sem movimentos disponíveis",
                partida.movimentarJogadorAtual(1, 0));
        assertEquals("A vez não deve ser consumida", 0, partida.getIndiceJogadorAtual());
    }
}
