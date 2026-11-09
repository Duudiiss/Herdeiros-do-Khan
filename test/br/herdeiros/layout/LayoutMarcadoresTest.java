package br.herdeiros.layout;

import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

/** Coordenadas normalizadas e persistencia dos pontos marcados pelo editor. */
public class LayoutMarcadoresTest {

    /** Objetivo: garantir que o editor preserva coordenadas em diferentes tamanhos.
     * Condicao inicial: layout temporario, sem marcacoes.
     * Resultado esperado: coordenadas iguais apos salvar e recarregar. */
    @Test(timeout = 2000)
    public void testeSalvarRecarregarPontos() throws Exception {
        Path arquivo = Files.createTempFile("khan-marcadores", ".csv");
        try {
            LayoutMarcadores layout = new LayoutMarcadores(arquivo);
            layout.marcar("pontos.0", 0.2512345, 0.8934567);
            layout.salvar();
            LayoutMarcadores recarregado = new LayoutMarcadores(arquivo);
            assertEquals("X precisa permanecer normalizado", 0.2512345,
                    recarregado.consultar("pontos.0").getX(), 0.0000001);
            assertEquals("Y precisa permanecer normalizado", 0.8934567,
                    recarregado.consultar("pontos.0").getY(), 0.0000001);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    /** Objetivo: rejeitar posicoes fora dos limites da imagem.
     * Condicao inicial: layout novo.
     * Resultado esperado: lancamento de IllegalArgumentException. */
    @Test(timeout = 2000, expected = IllegalArgumentException.class)
    public void testeCoordenadasForaDaImagemSaoRecusadas() {
        new LayoutMarcadores.Posicao(-0.2, 0.5);
    }

    /** Objetivo: preservar a independencia de dois arquivos de cartela.
     * Condicao inicial: dois layouts novos.
     * Resultado esperado: cada um conserva seu proprio conjunto de pontos. */
    @Test(timeout = 2000)
    public void testeCartelasMantemMapasIndependentes() throws Exception {
        Path primeiro = Files.createTempFile("khan-layout-a", ".csv");
        Path segundo = Files.createTempFile("khan-layout-b", ".csv");
        try {
            LayoutMarcadores a = new LayoutMarcadores(primeiro);
            LayoutMarcadores b = new LayoutMarcadores(segundo);
            a.marcar("melhoria.0.0", 0.25, 0.42);
            b.marcar("melhoria.0.0", 0.65, 0.72);
            a.salvar();
            b.salvar();
            assertEquals("A primeira cartela nao deve copiar a segunda", 0.25,
                    new LayoutMarcadores(primeiro).consultar("melhoria.0.0").getX(), 0.0000001);
            assertEquals("A segunda cartela deve manter sua propria coordenada", 0.65,
                    new LayoutMarcadores(segundo).consultar("melhoria.0.0").getX(), 0.0000001);
        } finally {
            Files.deleteIfExists(primeiro);
            Files.deleteIfExists(segundo);
        }
    }

    /** Objetivo: oferecer todas as casas editaveis na interface.
     * Condicao inicial: catalogos de pontuacao e cartela.
     * Resultado esperado: campos de placar, mercado e melhorias de cada coluna. */
    @Test(timeout = 2000)
    public void testeEditorPossuiChavesParaTodosOsTipos() {
        assertTrue("Pontuacao precisa ser configuravel",
                LayoutMarcadores.chavesTabuleiro().contains("pontos.8"));
        assertFalse("Nao existe mais mapeamento de progresso separado",
                LayoutMarcadores.chavesTabuleiro().contains("progresso.8"));
        assertFalse("Fim deve ser derivado da pontuacao dos jogadores",
                LayoutMarcadores.chavesTabuleiro().contains("fim"));
        assertTrue("A quinta posicao do mercado precisa ser configuravel",
                LayoutMarcadores.chavesTabuleiro().contains("mercado.4"));
        assertTrue("O editor deve permitir a casa de pontuacao 16",
                LayoutMarcadores.chavesTabuleiro().contains("pontos.16"));
        assertTrue("A quarta melhoria da coluna amarela deve ser mapeavel",
                LayoutMarcadores.chavesCartela().contains("melhoria.2.3"));
        assertTrue("O editor deve aceitar todos os pontos da coluna branca",
                LayoutMarcadores.chavesCartela().contains("melhoria.3.3"));
        assertTrue("A coluna branca tem ponto proprio de ativacao",
                LayoutMarcadores.chavesCartela().contains("ativacao.3"));
    }
}
