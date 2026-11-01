package br.herdeiros.model;

import static org.junit.Assert.*;

import org.junit.Test;

/** Testa a associação entre um tributo e seu tipo. */
public class TributoTest {

    @Test(timeout = 2000)
    public void testeTributoPreservaTipo() {
        for (TipoTributo tipo : TipoTributo.values()) {
            Tributo tributo = new Tributo(tipo);
            assertSame("O tributo deve preservar o tipo recebido",
                    tipo, tributo.getTipo());
        }
    }
}
