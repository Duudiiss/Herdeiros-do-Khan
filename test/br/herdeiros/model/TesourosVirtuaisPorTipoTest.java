package br.herdeiros.model;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/** Testes de saldos temporarios por tipo, diferentes de fichas reais. */
public class TesourosVirtuaisPorTipoTest {
    @Test public void cartasVirtuaisIdentificamOsCincoTipos() {
        assertEquals("Um tesouro virtual: Gado", MercadoMelhorias.criarPorCodigo("BOI-A", 2).getDescdescricao());
        assertEquals("Um tesouro virtual: Especiarias", MercadoMelhorias.criarPorCodigo("CUI-A", 2).getDescdescricao());
        assertEquals("Um tesouro virtual: Peles", MercadoMelhorias.criarPorCodigo("MOP-V", 1).getDescdescricao());
        assertEquals("Um tesouro virtual: Seda", MercadoMelhorias.criarPorCodigo("PER-V", 1).getDescdescricao());
        assertEquals("Um tesouro virtual: Ferro", MercadoMelhorias.criarPorCodigo("MOB-R", 0).getDescdescricao());
    }

    @Test public void melhoriaSoConcedeTesouroQuandoColunaEAtivada() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoria(MercadoMelhorias.criarPorCodigo("BOI-A", 2));
        assertTrue(cartela.consultarTesourosVirtuais().isEmpty() ||
                cartela.consultarTesourosVirtuais().getOrDefault(TipoTesouro.GADO,0) == 0);
        cartela.encerrarTurno(); // encerra turno da compra
        assertTrue(cartela.ativarColuna(2, Personagem.ALTANI));
        assertEquals(Integer.valueOf(1), cartela.consultarTesourosVirtuais().get(TipoTesouro.GADO));
        assertEquals(Integer.valueOf(0), cartela.consultarTesourosVirtuais().get(TipoTesouro.SEDA));
        cartela.consumirTesourosVirtuais(Collections.singletonMap(TipoTesouro.GADO,1));
        assertEquals(Integer.valueOf(0), cartela.consultarTesourosVirtuais().get(TipoTesouro.GADO));
        cartela.encerrarTurno();
        assertEquals(Integer.valueOf(0), cartela.consultarTesourosVirtuais().get(TipoTesouro.GADO));
    }

    @Test public void tipoErradoNaoCompletaDemanda() {
        InterfacePartida partida = new InterfacePartida();
        LocalConselheiro local = new LocalConselheiro(5,"Teste",new TipoTesouro[] {TipoTesouro.FERRO,TipoTesouro.FERRO},1,null);
        java.util.List<TipoTesouro> fisicos = Arrays.asList(TipoTesouro.FERRO);
        try {
            java.lang.reflect.Method metodo = InterfacePartida.class.getDeclaredMethod(
                "podePagarTesouros", LocalConselheiro.class, java.util.List.class, java.util.Map.class);
            metodo.setAccessible(true);
            java.util.Map<TipoTesouro,Integer> virtuais = new java.util.EnumMap<>(TipoTesouro.class);
            virtuais.put(TipoTesouro.SEDA,1);
            assertFalse((Boolean)metodo.invoke(partida,local,fisicos,virtuais));
            virtuais.put(TipoTesouro.FERRO,1);
            assertTrue((Boolean)metodo.invoke(partida,local,fisicos,virtuais));
        } catch (ReflectiveOperationException erro) {
            throw new AssertionError(erro);
        }
    }
}
