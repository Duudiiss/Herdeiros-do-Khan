package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;

import br.herdeiros.model.Cidade;
import br.herdeiros.model.NomeCidade;
import br.herdeiros.model.Regiao;

/** Testa retirada de tesouros, conquista e rejeição de saques após esgotamento. */
public class CidadeTest {

    /**
     * Objetivo: Verificar que um saque inicial bem-sucedido diminui a reserva de tesouros.
     * Condição inicial: Uma cidade recém-criada, ainda não conquistada, com quatro tesouros.
     * Resultado esperado: O saque é aceito e a quantidade de tesouros passa de quatro para três.
     */
    @Test(timeout = 2000)
    public void testSaquearTesouroPelaPrimeiraVez() {
        // Cidade é criada com 4 tesouros iniciais
        Cidade cidade = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);

        boolean resultado = cidade.saquearTesouro();

        assertTrue("Saquear uma cidade pela primeira vez deveria retornar true", resultado);
        assertEquals("Saquear uma cidade pela primeira vez ainda deve ter 3 tesouros", 3, cidade.getQuantidadeTesouros());

    }

    /**
     * Objetivo: Confirmar a transição da cidade para o estado de conquistada após esgotar os tesouros.
     * Condição inicial: Uma cidade criada com quatro tesouros; três saques são executados antes do último.
     * Resultado esperado: O saque final é bem-sucedido e a cidade passa a ter zero tesouros e a estar conquistada.
     */
    @Test(timeout = 2000)
    public void testSaquearUltimoTesouro() {
        Cidade cidade = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
        //Saqueando a mesma cidade 3 vezes
        int i = 0;
        while (i < 3) {
            cidade.saquearTesouro();
            i++;
        }
        boolean resultado = cidade.saquearTesouro();

        assertTrue("O saque do último tesouro deveria retornar true", resultado);
        assertTrue("Ao saquear o último tesouro a cidade deveria ser conquistada", cidade.isConquistada());
    }

    /**
     * Objetivo: Testar a rejeição de um saque após o esgotamento da cidade.
     * Condição inicial: Uma cidade da qual todos os quatro tesouros já foram retirados.
     * Resultado esperado: A tentativa adicional é recusada, sem permitir retirada de novos tesouros.
     */
    @Test(timeout = 2000)
    public void testSaquearTesouroSemSucesso() {
        Cidade cidade = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
        //Saqueando a mesma cidade 3 vezes
        int i = 0;
        while (i < 4) {
            cidade.saquearTesouro();
            i++;
        }
        boolean resultado = cidade.saquearTesouro();

        assertFalse("Tentar saquear uma cidade sem tesouros deve retornar false", resultado);
    }

}
