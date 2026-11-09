package br.herdeiros.model;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

/** Verifica a consulta de cidades pela Facade, sem expor o estado interno. */
public class ResumoCidadesTest {

    /**
     * Objetivo: garantir que a tela receba as cidades abertas no inicio.
     * Condicao inicial: partida nova, com as tres cidades sorteadas.
     * Resultado esperado: tres cidades distintas e quatro tesouros em cada uma.
     */
    @Test(timeout = 2000)
    public void testeConsultaInicialRetornaTresCidadesDistintas() {
        InterfacePartida partida = new InterfacePartida();
        List<ResumoCidade> cidades = partida.consultarCidadesDisponiveis();
        java.util.Set<String> nomes = new java.util.HashSet<String>();

        for (ResumoCidade cidade : cidades) {
            nomes.add(cidade.getNome());
            assertEquals("Cada cidade comeca com quatro tesouros", 4,
                    cidade.getTesourosRestantes());
            assertFalse("Uma cidade inicial nao esta conquistada", cidade.isConquistada());
        }

        assertEquals("O jogo distribui tres cidades abertas", 3, cidades.size());
        assertEquals("As cidades abertas nao podem se repetir", cidades.size(), nomes.size());
    }

    /**
     * Objetivo: impedir que a View edite a lista devolvida pela Facade.
     * Condicao inicial: lista consultada em uma partida nova.
     * Resultado esperado: a tentativa de remover um item e rejeitada.
     */
    @Test(timeout = 2000)
    public void testeListaDasCidadesNaoPermiteRemocaoExterna() {
        InterfacePartida partida = new InterfacePartida();
        List<ResumoCidade> cidades = partida.consultarCidadesDisponiveis();
        boolean bloqueou = false;

        try {
            cidades.clear();
        } catch (UnsupportedOperationException erro) {
            bloqueou = true;
        }

        assertTrue("A View nao pode modificar a lista de cidades", bloqueou);
        assertEquals("As cidades do Model devem permanecer intactas", 3,
                partida.consultarCidadesDisponiveis().size());
    }

    /**
     * Objetivo: confirmar que consultas repetidas nao mudam os objetos do Model.
     * Condicao inicial: mesma partida consultada duas vezes.
     * Resultado esperado: dados iguais, mas objetos de resumo diferentes.
     */
    @Test(timeout = 2000)
    public void testeConsultasProduzemResumosIndependentes() {
        InterfacePartida partida = new InterfacePartida();
        List<ResumoCidade> primeira = partida.consultarCidadesDisponiveis();
        List<ResumoCidade> segunda = partida.consultarCidadesDisponiveis();

        assertNotSame("As consultas nao devem retornar a mesma lista", primeira, segunda);
        assertEquals("A quantidade de cidades deve permanecer igual",
                primeira.size(), segunda.size());
        for (int i = 0; i < primeira.size(); i++) {
            assertNotSame("A View recebe um retrato, nao o objeto do Model",
                    primeira.get(i), segunda.get(i));
            assertEquals("A ordem da mesma partida deve permanecer estavel",
                    primeira.get(i).getNome(), segunda.get(i).getNome());
        }
    }

    /**
     * Objetivo: testar a validacao de estados ilegais do objeto de consulta.
     * Condicao inicial: criacao de um resumo com tesouros negativos.
     * Resultado esperado: IllegalArgumentException.
     */
    @Test(timeout = 2000)
    public void testeResumoRejeitaQuantidadeNegativa() {
        boolean rejeitou = false;
        try {
            new ResumoCidade("Teste", "PERSIA", -1, false);
        } catch (IllegalArgumentException erro) {
            rejeitou = true;
        }
        assertTrue("Tesouros negativos nao podem ser exibidos", rejeitou);
    }
}
