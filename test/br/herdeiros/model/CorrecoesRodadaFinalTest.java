package br.herdeiros.model;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/** Regressao das regras e ferramentas ajustadas na quarta iteracao. */
public class CorrecoesRodadaFinalTest {

    private InterfacePartida criarPartida() {
        return new InterfacePartida(new String[] {"Ana", "Beto"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.ALTANI, Personagem.JOCHI}, false);
    }

    @Test(timeout = 2000)
    public void pontuacaoDoKhanEncerraEImpedeNovosVotos() {
        InterfacePartida partida = criarPartida();
        assertTrue(partida.desenvolvimentoAlterarVotos(0, 100));
        assertEquals(9, partida.consultarPontuacaoKhan());
        assertEquals(9, partida.consultarVotosJogador(0));
        assertTrue(partida.isPartidaEncerrada());
        assertFalse(partida.desenvolvimentoAlterarVotos(1, 1));
        assertFalse(TestesConselheirosAux.avancar(partida));
    }

    @Test(timeout = 2000)
    public void reposicaoDoMercadoPeloKhanNoModoDesenvolvedor() {
        InterfacePartida partida = criarPartida();
        assertTrue(partida.desenvolvimentoAjustarTributo(0, "MOEDA", 10));
        assertTrue(partida.comprarMelhoriaAtual(0));
        long antes = partida.consultarMercadoMelhorias().stream()
                .filter(carta -> carta != null).count();
        assertEquals(4, antes);
        assertTrue(partida.desenvolvimentoPosicionarKhan(DestinoKhan.MELHORIAS));
        long depois = partida.consultarMercadoMelhorias().stream()
                .filter(carta -> carta != null).count();
        assertEquals(5, depois);
    }

    @Test(timeout = 2000)
    public void recursosVirtuaisDeOutroJogadorEntramNaAtivacao() {
        InterfacePartida partida = criarPartida();
        assertTrue(partida.desenvolvimentoAjustarRecursoEspecial(1, "CAVALO_VIRTUAL", 3));
        assertTrue(partida.desenvolvimentoAjustarRecursoEspecial(1, "PUNHO_VIRTUAL", 2));
        assertTrue(partida.desenvolvimentoAjustarRecursoEspecial(1, "MOEDA_VIRTUAL", 1));
        assertTrue(TestesConselheirosAux.avancar(partida));
        assertTrue(partida.escolherMelhoriaInicialJochi(0));
        assertTrue(partida.ativarColunaJogadorAtual(0));
        assertEquals(4, partida.consultarCavalosRestantesJogadorAtual());
        assertEquals(3, partida.consultarPunhosRestantesJogadorAtual());
        assertEquals(2, partida.consultarAtivacaoJogadorAtual().getMoedasVirtuaisDaColuna());
    }

    @Test(timeout = 2000)
    public void baralhoDeCidadesNaoRepeteCidade() {
        BaralhoDeCidades baralho = new BaralhoDeCidades();
        Set<NomeCidade> vistos = new HashSet<NomeCidade>();
        Cidade cidade;
        while ((cidade = baralho.sacarCidadeDoTopo()) != null) {
            assertTrue("Cidade repetida no baralho", vistos.add(cidade.getNomeCidade()));
        }
        assertEquals(9, vistos.size());
    }

    @Test(timeout = 2000)
    public void yurtDaCidadeConquistadaTemDonoEContagemSeparada() {
        Tabuleiro tabuleiro = new Tabuleiro(java.util.Collections.<Cidade>emptyList());
        assertTrue(tabuleiro.registrarYurtConquista(NomeCidade.KIEV, Cor.AZUL));
        assertEquals(Cor.AZUL, tabuleiro.getDonoYurtConquista(NomeCidade.KIEV));
        assertFalse(tabuleiro.registrarYurtConquista(NomeCidade.KIEV, Cor.VERMELHO));
        assertEquals(0, tabuleiro.getQuantidadeYurts(0));
    }

    @Test(timeout = 2000)
    public void ferramentasDeTesourosAlteramAMaoDoJogador() {
        InterfacePartida partida = criarPartida();
        assertTrue(partida.desenvolvimentoAjustarItemEspecial(0, "TESOURO_SEDA", 2));
        assertEquals(2, partida.consultarQuantidadeTesourosJogadorAtual());
        assertTrue(partida.desenvolvimentoAjustarItemEspecial(0, "TESOURO_SEDA", -1));
        assertEquals(1, partida.consultarQuantidadeTesourosJogadorAtual());
        assertFalse(partida.desenvolvimentoAjustarItemEspecial(0, "TESOURO_SEDA", -2));
    }
}
