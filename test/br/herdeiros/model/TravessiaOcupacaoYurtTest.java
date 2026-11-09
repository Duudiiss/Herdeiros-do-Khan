package br.herdeiros.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Test;

/** Testes de regressao para travessia de paradas ocupadas apos implementar yurts. */
public class TravessiaOcupacaoYurtTest {

    /**
     * Objetivo: verificar se a busca de caminhos permite atravessar uma parada simples ocupada.
     * Condicao inicial: dois peoes em Karakorum; o primeiro ocupa a parada 1 de uma cadeia 0-1-2.
     * Resultado esperado: o segundo pode chegar a parada 2, mas nao pode parar na parada 1.
     */
    @Test(timeout = 2000)
    public void testeParadaIntermediariaOcupadaNaoBloqueiaTravessia() {
        Tabuleiro tabuleiro = new Tabuleiro(new ArrayList<Cidade>());
        assertTrue("Parada 1 deve ser criada", tabuleiro.adicionarParada(1, false));
        assertTrue("Parada 2 deve ser criada", tabuleiro.adicionarParada(2, false));
        assertTrue("Karakorum deve conectar a parada 1", tabuleiro.conectarParadas(0, 1));
        assertTrue("A parada 1 deve conectar a parada 2", tabuleiro.conectarParadas(1, 2));

        Peao primeiro = new Peao(Cor.AZUL);
        Peao segundo = new Peao(Cor.VERMELHO);
        assertTrue("Primeiro peao cadastrado", tabuleiro.posicionarPeaoInicial(primeiro));
        assertTrue("Segundo peao cadastrado", tabuleiro.posicionarPeaoInicial(segundo));
        assertTrue("Primeiro peao ocupa a parada 1", tabuleiro.moverPeaoAte(primeiro, 1, 1));

        assertFalse("Nao e permitido terminar na parada simples ocupada",
                tabuleiro.moverPeaoAte(segundo, 1, 1));
        assertEquals("A distancia ate a parada 2 deve ser de duas conexoes", 2,
                tabuleiro.distanciaMinima(segundo, 2, Cor.VERMELHO));
        assertTrue("O segundo peao deve conseguir atravessar a parada ocupada",
                tabuleiro.moverPeaoAte(segundo, 2, 2, Cor.VERMELHO));
        assertEquals("O segundo peao terminou no destino correto", 2,
                segundo.getParadaAtual().getId());
    }
}
