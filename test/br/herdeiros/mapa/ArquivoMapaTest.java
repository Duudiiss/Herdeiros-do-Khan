package br.herdeiros.mapa;

import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

/** Testes de persistência CSV e rejeição de arquivos incorretos. */
public class ArquivoMapaTest {
    /** Objetivo: preservar pontos, tipos e conexões em uma gravação completa.
     * Condição inicial: mapa com duas paradas e uma ligação; resultado esperado: dados equivalentes. */
    @Test(timeout = 2000)
    public void testeSalvarECarregarMapa() throws IOException {
        Path arquivo = Files.createTempFile("khan-mapa", ".csv");
        try {
            MapaDados original = new MapaDados();
            original.adicionar(1, 0.25, 0.75, true);
            original.adicionar(2, 0.60, 0.30, false);
            original.conectar(1, 2);
            ArquivoMapa.salvar(original, arquivo);
            MapaDados carregado = ArquivoMapa.carregar(arquivo);
            assertEquals("Todas as paradas devem ser recuperadas", original.getPontos().size(), carregado.getPontos().size());
            assertEquals("Todas as conexões devem ser recuperadas", original.getLigacoes().size(), carregado.getLigacoes().size());
            assertTrue("Tipo dupla deve ser preservado", carregado.getPonto(1).isDupla());
            assertEquals("Coordenada deve ser preservada", 0.25, carregado.getPonto(1).getX(), 0.00001);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    /** Objetivo: rejeitar rotas para paradas inexistentes.
     * Estado inicial: CSV com conexão inválida; resultado esperado: IOException. */
    @Test(timeout = 2000, expected = IOException.class)
    public void testeNaoCarregarLigacaoInexistente() throws IOException {
        Path arquivo = Files.createTempFile("khan-invalido", ".csv");
        try {
            Files.writeString(arquivo, "P;0;0.5;0.43;false\nC;0;99\n", StandardCharsets.UTF_8);
            ArquivoMapa.carregar(arquivo);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    /** Objetivo: aceitar conexões declaradas antes das paradas.
     * Resultado esperado: parser resolve conexões depois do cadastro completo. */
    @Test(timeout = 2000)
    public void testeConexaoAntesDaDefinicaoDePontos() throws IOException {
        Path arquivo = Files.createTempFile("khan-ordem", ".csv");
        try {
            Files.writeString(arquivo, "C;0;2\nP;2;0.7;0.7;false\n", StandardCharsets.UTF_8);
            MapaDados mapa = ArquivoMapa.carregar(arquivo);
            assertEquals("Conexão deve ser carregada", 1, mapa.getLigacoes().size());
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }
}
