package br.herdeiros.model;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/** Testa distribuição, unicidade, composição regional e esgotamento das cartas. */
public class BaralhoDeCidadesTest {

    private BaralhoDeCidades baralho;

    @Before
    public void preparar() {
        baralho = new BaralhoDeCidades();
    }

    /**
     * Objetivo: Confirmar que a distribuição inicial produz três cartas de cidades válidas e diferentes.
     * Condição inicial: Um baralho recém-criado, ainda sem retiradas.
     * Resultado esperado: A lista contém três cidades não nulas e seus nomes não se repetem.
     */
    @Test(timeout = 2000)
    public void testeDistribuicaoCidadesIniciais() {
        List<Cidade> cidades = baralho.sacarCidadesIniciais();

        assertEquals("testeDistribuicaoCidadesIniciais: cidades.size() deve corresponder ao valor esperado", 3, cidades.size());

        Set<String> nomes = new HashSet<>();

        for (Cidade cidade : cidades) {
            assertNotNull("testeDistribuicaoCidadesIniciais: cidade nao deve ser nulo", cidade);
            nomes.add(cidade.getNome());
        }

        // As três cidades precisam ser diferentes
        assertEquals("testeDistribuicaoCidadesIniciais: nomes.size() deve corresponder ao valor esperado", 3, nomes.size());
    }

    /**
     * Objetivo: Verificar que cada cidade está representada apenas uma vez no baralho completo.
     * Condição inicial: Um baralho novo, com todas as cidades previstas no enum NomeCidade.
     * Resultado esperado: Cada retirada retorna uma cidade existente e a inclusão de seu nome no conjunto é bem-sucedida; ao final, a quantidade de nomes distintos corresponde ao total de cidades.
     */
    @Test(timeout = 2000)
    public void testeBaralhoSemCidadesRepetidas() {
        Set<String> nomes = new HashSet<>();

        for (int i = 0; i < NomeCidade.values().length; i++) {
            Cidade cidade = baralho.sacarCidadeDoTopo();

            assertNotNull("testeBaralhoSemCidadesRepetidas: cidade nao deve ser nulo", cidade);

            // add retorna false se o nome já existir
            assertTrue("testeBaralhoSemCidadesRepetidas: nomes.add(cidade.getNome()) deve ser verdadeiro", nomes.add(cidade.getNome()));
        }

        assertEquals("testeBaralhoSemCidadesRepetidas: nomes.size() deve corresponder ao valor esperado", NomeCidade.values().length, nomes.size());
    }

    /**
     * Objetivo: Conferir a distribuição das cartas pelas três regiões do jogo.
     * Condição inicial: Um baralho completo, independentemente da ordem de embaralhamento.
     * Resultado esperado: As nove retiradas correspondem a três cidades da Rússia, três da China e três da Pérsia.
     */
    @Test(timeout = 2000)
    public void testeQuantidadeDeCidadesPorRegiao() {
        int russia = 0;
        int china = 0;
        int persia = 0;

        for (int i = 0; i < NomeCidade.values().length; i++) {
            Cidade cidade = baralho.sacarCidadeDoTopo();

            assertNotNull("testeQuantidadeDeCidadesPorRegiao: cidade nao deve ser nulo", cidade);

            switch (cidade.getRegiao()) {
                case RUSSIA:
                russia++;
                break;

                case CHINA:
                china++;
                break;

                case PERSIA:
                persia++;
                break;
            }
        }

        assertEquals("testeQuantidadeDeCidadesPorRegiao: russia deve corresponder ao valor esperado", 3, russia);
        assertEquals("testeQuantidadeDeCidadesPorRegiao: china deve corresponder ao valor esperado", 3, china);
        assertEquals("testeQuantidadeDeCidadesPorRegiao: persia deve corresponder ao valor esperado", 3, persia);
    }

    /**
     * Objetivo: Verificar a reação do baralho quando todas as cartas já foram retiradas.
     * Condição inicial: Um baralho completo; o teste realiza uma retirada por cidade cadastrada.
     * Resultado esperado: As retiradas enquanto existem cartas não retornam null; a retirada seguinte retorna null.
     */
    @Test(timeout = 2000)
    public void testeBaralhoVazio() {
        for (int i = 0; i < NomeCidade.values().length; i++) {
            assertNotNull("testeBaralhoVazio: baralho.sacarCidadeDoTopo() nao deve ser nulo", baralho.sacarCidadeDoTopo());
        }

        // Depois de retirar todas as cartas,
        // não deve existir outra cidade disponível
        assertNull("testeBaralhoVazio: baralho.sacarCidadeDoTopo() deve ser nulo", baralho.sacarCidadeDoTopo());
    }

    /**
     * Objetivo: Garantir que distribuir as cartas iniciais realmente as remove do baralho.
     * Condição inicial: Um baralho novo, sem retiradas anteriores.
     * Resultado esperado: Após distribuir três cartas, somente a quantidade restante pode ser retirada; a tentativa posterior retorna null.
     */
    @Test(timeout = 2000)
    public void testeDistribuicaoReduzBaralho() {
        List<Cidade> cidadesIniciais =
        baralho.sacarCidadesIniciais();

        assertEquals("testeDistribuicaoReduzBaralho: cidadesIniciais.size() deve corresponder ao valor esperado", 3, cidadesIniciais.size());

        // Há nove cidades no manual; calculamos o restante a partir do saque inicial.
        int restantes = NomeCidade.values().length - cidadesIniciais.size();
        for (int i = 0; i < restantes; i++) {
            assertNotNull("testeDistribuicaoReduzBaralho: baralho.sacarCidadeDoTopo() nao deve ser nulo", baralho.sacarCidadeDoTopo());
        }

        assertNull("testeDistribuicaoReduzBaralho: baralho.sacarCidadeDoTopo() deve ser nulo", baralho.sacarCidadeDoTopo());
    }

    @Test(timeout = 2000)
    public void testeDistribuicaoInicialComMenosDeTresCidades() {
        for (int i = 0; i < NomeCidade.values().length - 1; i++) {
            assertNotNull("As cidades disponíveis devem ser retiradas",
                    baralho.sacarCidadeDoTopo());
        }

        List<Cidade> cidadesRestantes = baralho.sacarCidadesIniciais();

        assertEquals("Deve distribuir somente as cidades ainda disponíveis",
                1, cidadesRestantes.size());
        assertNotNull("A distribuição não deve incluir cidades nulas",
                cidadesRestantes.get(0));
        assertTrue("Uma segunda distribuição sem cartas deve ser vazia",
                baralho.sacarCidadesIniciais().isEmpty());
    }
}
