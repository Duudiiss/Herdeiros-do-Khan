package br.herdeiros.mapa;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/** Testa a estrutura editável do mapa, separadamente das regras da partida. */
public class MapaDadosTest {
    private MapaDados mapa;

    @Before
    public void preparar() {
        mapa = new MapaDados();
    }

    /** Objetivo: impedir IDs repetidos. Estado inicial: somente Karakorum.
     * Resultado esperado: segunda inclusão falha e a quantidade permanece estável. */
    @Test(timeout = 2000)
    public void testeIdDuplicadoNaoAlteraMapa() {
        assertTrue("Primeiro cadastro deve funcionar", mapa.adicionar(1, 0.2, 0.4, false));
        int anterior = mapa.getPontos().size();
        assertFalse("ID duplicado não deve ser inserido", mapa.adicionar(1, 0.6, 0.7, true));
        assertEquals("Quantidade deve ficar igual", anterior, mapa.getPontos().size());
    }

    /** Objetivo: confirmar que a conexão é não direcionada.
     * Estado inicial: duas paradas novas; resultado esperado: apenas uma ligação. */
    @Test(timeout = 2000)
    public void testeLigacaoUnicaMesmoInvertida() {
        mapa.adicionar(1, 0.2, 0.2, false);
        mapa.adicionar(2, 0.3, 0.3, true);
        assertTrue("Ligação inicial válida", mapa.conectar(1, 2));
        assertFalse("Ligação invertida não deve duplicar", mapa.conectar(2, 1));
        assertEquals("Somente uma ligação deve existir", 1, mapa.getLigacoes().size());
    }

    /** Objetivo: remover conexões órfãs ao excluir uma parada.
     * Estado inicial: uma ligação com a parada 2; resultado esperado: ligação removida. */
    @Test(timeout = 2000)
    public void testeExcluirParadaRemoveLigacoes() {
        mapa.adicionar(1, 0.2, 0.2, false);
        mapa.adicionar(2, 0.3, 0.3, true);
        mapa.conectar(1, 2);
        assertTrue("Parada deve ser removida", mapa.remover(2));
        assertEquals("Remoção deve limpar conexões", 0, mapa.getLigacoes().size());
    }

    /** Objetivo: manter a posição de Karakorum editável e impedir sua remoção.
     * Resultado esperado: movimento permitido, remoção rejeitada. */
    @Test(timeout = 2000)
    public void testeKarakorumPodeMoverMasNaoExcluir() {
        assertTrue("Karakorum pode ser reposicionada", mapa.mover(0, 0.4, 0.5));
        assertFalse("Karakorum não pode ser apagada", mapa.remover(0));
        assertEquals("Coordenada deve ser preservada", 0.4, mapa.getPonto(0).getX(), 0.00001);
    }

    /** Objetivo: rejeitar pontos fora da imagem.
     * Estado inicial: mapa sem novas paradas; resultado esperado: exceção. */
    @Test(timeout = 2000, expected = IllegalArgumentException.class)
    public void testeCoordenadaForaDaImagemRejeitada() {
        mapa.adicionar(1, 1.2, 0.3, false);
    }
}
