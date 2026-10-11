package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;

/** Regras de custo de espada e preservação do estado em falhas. */
public class JogadorTest {

    /**
     * Objetivo: Verificar o custo em espadas do primeiro saque de uma cidade durante o turno.
     * Condição inicial: Um jogador recebe três espadas e uma cidade possui sua quantidade inicial de tesouros; o contador de saques do turno é zero.
     * Resultado esperado: A ação é aceita; o saldo de espadas e o número de tesouros diminuem exatamente uma unidade.
     */
    @Test(timeout = 2000)
    public void testeEspadasConsumidasNoPrimeiroSaque() {
        Jogador j = new Jogador("A", Cor.AZUL, 3);
        Cidade c = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
        j.receberTributos(TipoTributo.ESPADA, 3);
        int antes = j.quantidadeTributo(TipoTributo.ESPADA);
        int tesouros = c.getQuantidadeTesouros();
        assertTrue("testeEspadasConsumidasNoPrimeiroSaque: j.saquearCidade(c, 0) deve ser verdadeiro", j.saquearCidade(c, 0));
        assertEquals("testeEspadasConsumidasNoPrimeiroSaque: j.quantidadeTributo(TipoTributo.ESPADA) deve corresponder ao valor esperado", antes - 1, j.quantidadeTributo(TipoTributo.ESPADA));
        assertEquals("testeEspadasConsumidasNoPrimeiroSaque: c.getQuantidadeTesouros() deve corresponder ao valor esperado", tesouros - 1, c.getQuantidadeTesouros());
    }

    /**
     * Objetivo: Verificar a diferença entre os custos do primeiro e do segundo saques da mesma cidade.
     * Condição inicial: Um jogador com três espadas, uma cidade disponível e duas chamadas sequenciais com contadores de turno zero e um.
     * Resultado esperado: Os dois saques são aceitos e o saldo de espadas chega a zero, correspondendo aos custos de uma e duas espadas.
     */
    @Test(timeout = 2000)
    public void testeSaqueSeguinteCustaDuasEspadas() {
        Jogador j = new Jogador("A", Cor.AZUL, 3);
        Cidade c = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
        j.receberTributos(TipoTributo.ESPADA, 3);
        assertTrue("testeSaqueSeguinteCustaDuasEspadas: j.saquearCidade(c, 0) deve ser verdadeiro", j.saquearCidade(c, 0));
        assertTrue("testeSaqueSeguinteCustaDuasEspadas: j.saquearCidade(c, 1) deve ser verdadeiro", j.saquearCidade(c, 1));
        assertEquals("testeSaqueSeguinteCustaDuasEspadas: j.quantidadeTributo(TipoTributo.ESPADA) deve corresponder ao valor esperado", 0, j.quantidadeTributo(TipoTributo.ESPADA));
    }

    /**
     * Objetivo: Verificar a rejeição de um saque cujo custo ultrapassa as espadas disponíveis.
     * Condição inicial: Um jogador possui uma espada e tenta um saque informado como posterior ao primeiro, com custo de duas espadas.
     * Resultado esperado: A ação retorna false; a quantidade de tesouros e o saldo de espadas permanecem inalterados.
     */
    @Test(timeout = 2000)
    public void testeRecursosInsuficientesNaoAlteramEstado() {
        Jogador j = new Jogador("A", Cor.AZUL, 3);
        Cidade c = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
        j.receberTributos(TipoTributo.ESPADA, 1);
        int antes = c.getQuantidadeTesouros();
        assertFalse("testeRecursosInsuficientesNaoAlteramEstado: j.saquearCidade(c, 1) deve ser falso", j.saquearCidade(c, 1));
        assertEquals("testeRecursosInsuficientesNaoAlteramEstado: c.getQuantidadeTesouros() deve corresponder ao valor esperado", antes, c.getQuantidadeTesouros());
        assertEquals("testeRecursosInsuficientesNaoAlteramEstado: j.quantidadeTributo(TipoTributo.ESPADA) deve corresponder ao valor esperado", 1, j.quantidadeTributo(TipoTributo.ESPADA));
    }

    /**
     * Objetivo: Garantir que quantidades não positivas de tributos não sejam recebidas.
     * Condição inicial: Um jogador recém-criado e seu saldo inicial de espadas registrado.
     * Resultado esperado: Recebimentos de quantidade negativa e zero são recusados e não alteram o saldo.
     */
    @Test(timeout = 2000)
    public void testeRecursosNaoAceitamValoresInvalidos() {
        Jogador j = new Jogador("A", Cor.AZUL, 3);
        int antes = j.quantidadeTributo(TipoTributo.ESPADA);
        assertFalse("testeRecursosNaoAceitamValoresInvalidos: j.receberTributos(TipoTributo.ESPADA, -2) deve ser falso", j.receberTributos(TipoTributo.ESPADA, -2));
        assertFalse("testeRecursosNaoAceitamValoresInvalidos: j.receberTributos(TipoTributo.ESPADA, 0) deve ser falso", j.receberTributos(TipoTributo.ESPADA, 0));
        assertEquals("testeRecursosNaoAceitamValoresInvalidos: j.quantidadeTributo(TipoTributo.ESPADA) deve corresponder ao valor esperado", antes, j.quantidadeTributo(TipoTributo.ESPADA));
    }
}
