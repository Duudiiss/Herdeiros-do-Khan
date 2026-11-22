package br.herdeiros.model;

import br.herdeiros.layout.LayoutAreas;
import br.herdeiros.mapa.ArquivoMapa;
import br.herdeiros.mapa.MapaDados;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import static org.junit.Assert.*;

/** Garante compatibilidade CSV e fidelidade dos limites clicaveis. */
public class HitboxesEditorUnificadoTest {
    @Test
    public void hitboxElipticaRejeitaPontoForaDoEixoVertical() {
        MapaDados mapa = new MapaDados();
        assertTrue(mapa.adicionar(1, 0.5, 0.5, false));
        assertTrue(mapa.alterarElipse(1, 0.10, 0.02));
        assertEquals(1, mapa.encontrarParada(0.58, 0.5, 1000, 1000));
        assertNotEquals(1, mapa.encontrarParada(0.5, 0.55, 1000, 1000));
    }

    @Test
    public void arquivoMapaPreservaRaiosIndependentes() throws Exception {
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.35, 0.67, false);
        mapa.alterarElipse(1, 0.085, 0.023);
        Path arquivo = Files.createTempFile("mapa-ellipse", ".csv");
        try {
            ArquivoMapa.salvar(mapa, arquivo);
            MapaDados recarregado = ArquivoMapa.carregar(arquivo);
            assertEquals(0.085, recarregado.getPonto(1).getRaio(), 0.000001);
            assertEquals(0.023, recarregado.getPonto(1).getRaioVertical(), 0.000001);
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test
    public void retangulosSaoSalvosEUsadosNaDeteccao() throws Exception {
        Path arquivo = Files.createTempFile("areas-mercado", ".csv");
        try {
            LayoutAreas layout = new LayoutAreas(arquivo);
            layout.marcar("mercado.0", new LayoutAreas.Area(0.25, 0.7, 0.10, 0.04));
            layout.salvar();
            LayoutAreas recarregado = new LayoutAreas(arquivo);
            LayoutAreas.Area area = recarregado.consultar("mercado.0");
            assertNotNull(area);
            assertTrue(area.contem(0.29, 0.71));
            assertFalse(area.contem(0.31, 0.71));
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }
}
