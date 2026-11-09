package br.herdeiros.mapa;

import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Test;

/** Confere os simbolos de coleta identificados no tabuleiro oficial. */
public class IconesOficiaisCorrigidosTest {

    /**
     * Objetivo: reconhecer o yurt impresso junto ao caminho no centro superior.
     * Condicao inicial: arquivo oficial de coordenadas atualizado.
     * Resultado esperado: o clique identifica um yurt, nao uma parada.
     */
    @Test(timeout = 2000)
    public void testeYurtCentralTemAreaDeClique() throws IOException {
        IconesTabuleiro mapa = IconesTabuleiro.carregar(Paths.get("assets", "icones_tabuleiro.csv"));
        IconesTabuleiro.IconeTributo icone = mapa.encontrarTributo(0.425, 0.303);
        assertNotNull("O yurt central deve ser clicavel", icone);
        assertEquals("O simbolo central deve ser reconhecido como yurt", "YURT", icone.getTipo());
    }

    /**
     * Objetivo: reconhecer a moeda impressa abaixo de Sarai.
     * Condicao inicial: arquivo oficial de coordenadas atualizado.
     * Resultado esperado: a selecao identifica uma moeda.
     */
    @Test(timeout = 2000)
    public void testeMoedaAbaixoDeSaraiTemAreaDeClique() throws IOException {
        IconesTabuleiro mapa = IconesTabuleiro.carregar(Paths.get("assets", "icones_tabuleiro.csv"));
        IconesTabuleiro.IconeTributo icone = mapa.encontrarTributo(0.665, 0.239);
        assertNotNull("A moeda abaixo de Sarai deve ser clicavel", icone);
        assertEquals("O simbolo abaixo de Sarai deve ser uma moeda", "MOEDA", icone.getTipo());
    }

    /**
     * Objetivo: preservar os simbolos ja mapeados no noroeste do tabuleiro.
     * Condicao inicial: arquivo oficial de coordenadas atualizado.
     * Resultado esperado: yurt, espada e moeda continuam clicaveis.
     */
    @Test(timeout = 2000)
    public void testeTresSimbolosDoNoroesteContinuamClicaveis() throws IOException {
        IconesTabuleiro mapa = IconesTabuleiro.carregar(Paths.get("assets", "icones_tabuleiro.csv"));
        assertEquals("O yurt proximo a Kiev deve ser reconhecido", "YURT",
                mapa.encontrarTributo(0.22365, 0.135).getTipo());
        assertEquals("A espada proxima a Kiev deve ser reconhecida", "ESPADA",
                mapa.encontrarTributo(0.30148, 0.119).getTipo());
        assertEquals("A moeda de divisa deve ser reconhecida", "MOEDA",
                mapa.encontrarTributo(0.27586, 0.315).getTipo());
    }

    /**
     * Objetivo: nao transformar os desenhos decorativos da cartela impressa em tributos.
     * Condicao inicial: dois antigos marcadores incorretos foram retirados do CSV.
     * Resultado esperado: os cliques nessa cartela nao encontram simbolos de coleta.
     */
    @Test(timeout = 2000)
    public void testeIconesDecorativosDaCartelaNaoGeramContadores() throws IOException {
        IconesTabuleiro mapa = IconesTabuleiro.carregar(Paths.get("assets", "icones_tabuleiro.csv"));
        assertNull("A figura superior da cartela nao e recurso coletavel",
                mapa.encontrarTributo(0.72611, 0.078));
        assertNull("A figura inferior da cartela nao e recurso coletavel",
                mapa.encontrarTributo(0.72414, 0.114));
    }
}
