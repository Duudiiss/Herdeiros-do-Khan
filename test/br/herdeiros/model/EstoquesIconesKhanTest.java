package br.herdeiros.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

/** Verifica que cada posicao de tributo possui estoque proprio e persistente. */
public class EstoquesIconesKhanTest {

    @Test(timeout = 3000)
    public void todosIconesComecamComUmaFicha() {
        InterfacePartida partida = new InterfacePartida(2);
        for (int indice = 0; indice < 12; indice++) {
            assertEquals("Cada icone comeca com uma ficha", 1,
                    partida.consultarQuantidadeTributoIcone(indice));
        }
    }

    @Test(timeout = 3000)
    public void khanRepoeCadaIconeAssociadoUmaVez() {
        InterfacePartida partida = new InterfacePartida(2);
        assertTrue(partida.desenvolvimentoPosicionarKhan(DestinoKhan.RUSSIA_OESTE));
        assertEquals(2, partida.consultarQuantidadeTributoIcone(0));
        assertEquals(2, partida.consultarQuantidadeTributoIcone(1));
    }

    @Test(timeout = 3000)
    public void salvaERecuperaEstoqueDosIcones() throws IOException {
        InterfacePartida original = new InterfacePartida(2);
        assertTrue(original.desenvolvimentoPosicionarKhan(DestinoKhan.RUSSIA_OESTE));
        Path destino = Files.createTempFile("khan-icones-", ".txt");
        try {
            GerenciadorSave.salvar(destino, original);
            InterfacePartida recuperada = GerenciadorSave.carregar(destino);
            assertEquals(original.consultarQuantidadeTributoIcone(0),
                    recuperada.consultarQuantidadeTributoIcone(0));
            assertEquals(original.consultarQuantidadeTributoIcone(11),
                    recuperada.consultarQuantidadeTributoIcone(11));
        } finally {
            Files.deleteIfExists(destino);
        }
    }
}
