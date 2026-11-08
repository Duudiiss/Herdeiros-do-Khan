package br.herdeiros.mapa;

import static org.junit.Assert.*;
import org.junit.Test;
import br.herdeiros.model.Provincia;
import java.nio.file.Files;
import java.nio.file.Path;

/** Verifica o mapeamento separado dos simbolos e das cidades do mapa. */
public class IconesTabuleiroTest {

    /** Objetivo: usar o centro mais proximo, nao os antigos retangulos sobre paradas. */
    @Test(timeout = 2000)
    public void testeIdentificaIconeEmDivisa() throws Exception {
        Path arquivo = Files.createTempFile("khan-icones", ".csv");
        try {
            Files.writeString(arquivo, "T;RUSSIA_OESTE,PERSIA_SUL;MOEDA;0.25;0.30;0.03\n");
            IconesTabuleiro mapa = IconesTabuleiro.carregar(arquivo);
            IconesTabuleiro.IconeTributo icone = mapa.encontrarTributo(0.25, 0.30);
            assertNotNull("O icone de divisa precisa ser encontrado", icone);
            assertEquals("O icone pode ser acessado pelas duas provincias", 2,
                    icone.getProvincias().length);
            assertEquals("A primeira provincia deve ser preservada", Provincia.RUSSIA_OESTE,
                    icone.getProvincias()[0]);
            assertEquals("A segunda provincia deve ser preservada", Provincia.PERSIA_SUL,
                    icone.getProvincias()[1]);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    /** Objetivo: nao consumir clique em parada fora do icone impresso. */
    @Test(timeout = 2000)
    public void testeCliqueForaDoIconeNaoSelecionaTributo() throws Exception {
        Path arquivo = Files.createTempFile("khan-icones", ".csv");
        try {
            Files.writeString(arquivo, "T;PERSIA_SUL;ESPADA;0.25;0.30;0.02\n");
            IconesTabuleiro mapa = IconesTabuleiro.carregar(arquivo);
            assertNull("Um clique distante nao deve selecionar o tributo",
                    mapa.encontrarTributo(0.45, 0.45));
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    /** Objetivo: identificar cidade pelo mapa e nao por lista de janela. */
    @Test(timeout = 2000)
    public void testeIdentificaCidadePeloIcone() throws Exception {
        Path arquivo = Files.createTempFile("khan-icones", ".csv");
        try {
            Files.writeString(arquivo, "C;MOSCOU;-;0.38;0.20;0.04\n");
            IconesTabuleiro mapa = IconesTabuleiro.carregar(arquivo);
            assertEquals("A cidade apontada deve ser Moscou", "MOSCOU",
                    mapa.encontrarCidade(0.38, 0.20).getNome());
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    /** Objetivo: impedir arquivos com coordenadas impossiveis. */
    @Test(timeout = 2000)
    public void testeArquivoComCoordenadaInvalidaERejeitado() throws Exception {
        Path arquivo = Files.createTempFile("khan-icones", ".csv");
        try {
            Files.writeString(arquivo, "T;PERSIA_SUL;ESPADA;1.50;0.30;0.02\n");
            boolean erro = false;
            try {
                IconesTabuleiro.carregar(arquivo);
            } catch (java.io.IOException esperado) {
                erro = true;
            }
            assertTrue("Nao se deve carregar simbolos fora do tabuleiro", erro);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }
}
