package br.herdeiros.model;

import static org.junit.Assert.*;

import org.junit.Test;

/** Testa o armazenamento das características de uma melhoria. */
public class MelhoriaTest {

    @Test(timeout = 2000)
    public void testeMelhoriaPreservaDescricaoCustoEBonus() {
        Melhoria melhoria = new Melhoria("Estrada comercial", 3, 2);

        assertEquals("A descrição deve ser preservada",
                "Estrada comercial", melhoria.getDescdescricao());
        assertEquals("O custo deve ser preservado",
                Integer.valueOf(3), melhoria.getCustoEmMoedas());
        assertEquals("O bônus de votos deve ser preservado",
                Integer.valueOf(2), melhoria.getVotosBonus());
    }
}
