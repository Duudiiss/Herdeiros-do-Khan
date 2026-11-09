package br.herdeiros.model;

import static org.junit.Assert.*;

import org.junit.Test;

import br.herdeiros.mapa.MapaDados;

/** Exercita construção de yurts, consumo do tributo e saltos de movimento. */
public class YurtFuncionalidadeTest {

    @Test(timeout = 2000)
    public void testeConstruirYurtConsomeTributoERegistraNoTabuleiro() {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.20, 0.20, false);
        mapa.conectar(0, 1);
        assertTrue("O mapa simples deve carregar", partida.carregarMapa(mapa));

        assertTrue("O jogador precisa receber um yurt de teste",
                partida.simularRecebimentoTributo(0, "YURT", 1));
        assertTrue("O jogador deve visitar a parada 1",
                partida.movimentarJogadorAtual(1, 1));
        assertTrue("A construção deve ser permitida numa parada visitada",
                partida.construirYurtAtual(1));

        assertEquals("Após construir, o tabuleiro deve registrar um yurt na parada", 1,
                partida.consultarQuantidadeYurtsNaParada(1));
        assertEquals("O tributo de yurt deve ser consumido", 0,
                partida.consultarCartelaJogadorAtual().getYurts());
    }

    @Test(timeout = 2000)
    public void testeYurtPermiteSaltarUmaParadaComUmMovimento() {
        Tabuleiro tabuleiro = new Tabuleiro(new java.util.ArrayList<Cidade>());
        tabuleiro.adicionarParada(1, false);
        tabuleiro.adicionarParada(2, false);
        tabuleiro.adicionarParada(3, false);
        tabuleiro.conectarParadas(0, 1);
        tabuleiro.conectarParadas(1, 2);
        tabuleiro.conectarParadas(2, 3);

        Peao peao = new Peao(Cor.AZUL);
        assertTrue("O peão precisa ser registrado", tabuleiro.posicionarPeaoInicial(peao));
        assertTrue("Preparação do teste: o peão deve chegar à parada 1",
                tabuleiro.moverPeaoAte(peao, 1, 1, Cor.AZUL));
        assertTrue("A yurt intermediária deve ser construída", tabuleiro.construirYurt(2, Cor.AZUL));

        assertEquals("Com yurt na parada 2, a distância de 1 para 3 deve cair para 1", 1,
                tabuleiro.distanciaMinima(peao, 3, Cor.AZUL));
        assertTrue("O salto deve ser aceito com um único movimento",
                tabuleiro.moverPeaoAte(peao, 3, 1, Cor.AZUL));
        assertEquals("O peão deve terminar na parada 3", 3, peao.getParadaAtual().getId());
    }

    @Test(timeout = 2000)
    public void testeParadaDuplaAceitaDoisYurts() {
        Tabuleiro tabuleiro = new Tabuleiro(new java.util.ArrayList<Cidade>());
        assertTrue(tabuleiro.adicionarParada(1, true));

        assertTrue("Primeiro yurt deve ser aceito", tabuleiro.construirYurt(1, Cor.AZUL));
        assertTrue("Segundo yurt na parada dupla também deve ser aceito",
                tabuleiro.construirYurt(1, Cor.VERMELHO));
        assertEquals("A parada dupla deve guardar os dois yurts", 2,
                tabuleiro.getQuantidadeYurts(1));
        assertFalse("A terceira construção deve ser recusada pela capacidade",
                tabuleiro.construirYurt(1, Cor.VERDE));
    }
}
