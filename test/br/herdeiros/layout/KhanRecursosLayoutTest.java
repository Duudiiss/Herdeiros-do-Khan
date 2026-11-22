package br.herdeiros.layout;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;
import org.junit.Test;
import br.herdeiros.model.DestinoKhan;

public class KhanRecursosLayoutTest {
    @Test(timeout = 2000)
    public void associacoesSaoRecuperadasDoCsv() throws Exception {
        java.nio.file.Path arquivo = java.nio.file.Files.createTempFile("khan-recursos-", ".csv");
        try {
            KhanRecursosLayout original = new KhanRecursosLayout();
            original.definir(DestinoKhan.RUSSIA_LESTE,
                    new java.util.LinkedHashSet<Integer>(Arrays.asList(2, 5, 9)));
            original.definir(DestinoKhan.CHINA_LESTE, Collections.<Integer>emptySet());
            original.salvar(arquivo);
            KhanRecursosLayout restaurado = KhanRecursosLayout.carregar(arquivo);
            assertEquals(original.consultar(DestinoKhan.RUSSIA_LESTE),
                    restaurado.consultar(DestinoKhan.RUSSIA_LESTE));
            assertTrue(restaurado.configurado(DestinoKhan.CHINA_LESTE));
            assertFalse(restaurado.configurado(DestinoKhan.PERSIA_SUL));
        } finally {
            java.nio.file.Files.deleteIfExists(arquivo);
        }
    }

    @Test(timeout = 2000)
    public void mapeamentoDeRecursosEhEspecificoPorDestino() {
        KhanRecursosLayout layout = new KhanRecursosLayout();
        assertFalse(layout.configurado(DestinoKhan.RUSSIA_OESTE));
        layout.definir(DestinoKhan.RUSSIA_OESTE,
                new HashSet<Integer>(Arrays.asList(1, 3, 7)));
        assertTrue(layout.configurado(DestinoKhan.RUSSIA_OESTE));
        assertTrue(layout.consultar(DestinoKhan.RUSSIA_OESTE).contains(3));
        assertTrue(layout.consultar(DestinoKhan.CHINA_LESTE).isEmpty());
        layout.definir(DestinoKhan.CHINA_LESTE, Collections.<Integer>emptySet());
        assertTrue(layout.configurado(DestinoKhan.CHINA_LESTE));
    }
}
