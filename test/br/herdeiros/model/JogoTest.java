package br.herdeiros.model;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** Testes de preparação da partida e de empate na pontuação disponível. */
public class JogoTest {
    private Jogador novo(String nome, Cor cor) {
        return new Jogador(nome, cor, 2);
    }

    /**
     * Objetivo: Verificar a preparação de uma partida válida e seu estado inicial.
     * Condição inicial: Dois jogadores distintos com cores diferentes, ainda sem partida associada.
     * Resultado esperado: A partida registra os dois participantes, coloca ambos os peões em Karakorum e disponibiliza três cidades iniciais.
     */
    @Test(timeout = 2000)
    public void testePreparacaoPartidaComDoisJogadores() {
        Jogador a = novo("A", Cor.AZUL);
        Jogador b = novo("B", Cor.VERDE);
        Jogo jogo = new Jogo(Arrays.asList(a, b));
        assertEquals("testePreparacaoPartidaComDoisJogadores: jogo.getQuantidadeJogadores() deve corresponder ao valor esperado", 2, jogo.getQuantidadeJogadores());
        assertEquals("testePreparacaoPartidaComDoisJogadores: a.getMeuPeao().getParadaAtual().getId() deve corresponder ao valor esperado", 0, a.getMeuPeao().getParadaAtual().getId());
        assertEquals("testePreparacaoPartidaComDoisJogadores: b.getMeuPeao().getParadaAtual().getId() deve corresponder ao valor esperado", 0, b.getMeuPeao().getParadaAtual().getId());
        assertEquals("testePreparacaoPartidaComDoisJogadores: jogo.getTabuleiro().getCidadesDisponiveis().size() deve corresponder ao valor esperado", 3, jogo.getTabuleiro().getCidadesDisponiveis().size());
    }

    /**
     * Objetivo: Verificar a validação de cores duplicadas durante a criação da partida.
     * Condição inicial: Uma tentativa de criação com dois jogadores da mesma cor.
     * Resultado esperado: O construtor lança IllegalArgumentException; o teste falha se a exceção não for lançada.
     */
    @Test(timeout = 2000)
    public void testeRejeicaoDeCorRepetida() {
        try {
            new Jogo(Arrays.asList(novo("A", Cor.AZUL), novo("B", Cor.AZUL)));
            fail("Cor duplicada deveria ser rejeitada");
        } catch (IllegalArgumentException esperado) {
            // Exceção esperada para os dados inválidos do teste.
        }
    }

    /**
     * Objetivo: Verificar o limite mínimo de participantes na criação da partida.
     * Condição inicial: Uma lista contendo somente um jogador.
     * Resultado esperado: O construtor lança IllegalArgumentException; o teste falha se a exceção não ocorrer.
     */
    @Test(timeout = 2000)
    public void testePartidaComNumeroInvalidoDeJogadores() {
        List<Jogador> um = Arrays.asList(novo("A", Cor.AZUL));
        try {
            new Jogo(um);
            fail("Partida com um jogador deveria ser rejeitada");
        } catch (IllegalArgumentException esperado) {
            // Exceção esperada: o jogo exige pelo menos dois participantes.
        }
    }

    /**
     * Objetivo: Verificar a identificação dos líderes e a preservação de empates na consulta de votos.
     * Condição inicial: Dois jogadores iniciam empatados e recebem votos sucessivamente.
     * Resultado esperado: A consulta retorna os dois no empate inicial, apenas o líder quando ele se destaca e novamente os dois após o empate ser restabelecido.
     */
    @Test(timeout = 2000)
    public void testeMaisVotosEConservacaoDeEmpates() {
        Jogador a = novo("A", Cor.AZUL);
        Jogador b = novo("B", Cor.VERDE);
        Jogo jogo = new Jogo(Arrays.asList(a, b));
        assertEquals("testeMaisVotosEConservacaoDeEmpates: jogo.jogadoresComMaisVotos().size() deve corresponder ao valor esperado", 2, jogo.jogadoresComMaisVotos().size());
        a.ganharVotos(3);
        assertEquals("testeMaisVotosEConservacaoDeEmpates: jogo.jogadoresComMaisVotos().size() deve corresponder ao valor esperado", 1, jogo.jogadoresComMaisVotos().size());
        assertTrue("testeMaisVotosEConservacaoDeEmpates: jogo.jogadoresComMaisVotos().contains(a) deve ser verdadeiro", jogo.jogadoresComMaisVotos().contains(a));
        b.ganharVotos(3);
        assertEquals("testeMaisVotosEConservacaoDeEmpates: jogo.jogadoresComMaisVotos().size() deve corresponder ao valor esperado", 2, jogo.jogadoresComMaisVotos().size());
    }
}
