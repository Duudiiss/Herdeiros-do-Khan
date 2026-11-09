package br.herdeiros.model;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.Test;

/** Verifica ativação por coluna, prêmio branco e encerramento pela ficha neutra. */
public class MelhoriasColunasEFinalTest {

    private Melhoria melhoria(String codigo, int coluna) {
        return new Melhoria("Melhoria " + codigo, 2, 0, codigo, coluna);
    }

    /** Cavalos verdes nao podem aumentar o deslocamento da coluna amarela. */
    @Test(timeout = 2000)
    public void testeCavalosSaoExclusivosDaColunaAtivada() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoria(melhoria("CAV-V", 1));
        assertTrue(cartela.ativarColuna(2, Personagem.TOLUI));
        assertEquals("A amarela de Tolui tem um cavalo", 1,
                cartela.consultarAtivacao(Personagem.TOLUI).getMovimentosRestantes());
        cartela.encerrarTurno();
        assertTrue(cartela.ativarColuna(1, Personagem.TOLUI));
        assertEquals("A verde de Tolui tem dois cavalos mais dois extras", 4,
                cartela.consultarAtivacao(Personagem.TOLUI).getMovimentosRestantes());
    }

    /** Punhos adicionais tambem so valem na propria coluna. */
    @Test(timeout = 2000)
    public void testePunhoExtraNaoContaminaOutraColuna() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoria(melhoria("MAO-V", 1));
        assertTrue(cartela.ativarColuna(2, Personagem.TOLUI));
        assertEquals(2, cartela.consultarAtivacao(Personagem.TOLUI).getPunhosDaColuna());
        cartela.encerrarTurno();
        assertTrue(cartela.ativarColuna(1, Personagem.TOLUI));
        assertEquals(2, cartela.consultarAtivacao(Personagem.TOLUI).getPunhosDaColuna());
    }

    /** Benefícios adquiridos no próprio turno não aparecem na ativação atual. */
    @Test(timeout = 2000)
    public void testeMelhoriaCompradaDuranteAtivacaoSoValeNoTurnoSeguinte() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue(cartela.ativarColuna(1, Personagem.TOLUI));
        cartela.adicionarMelhoria(melhoria("CAV-V", 1));
        assertEquals("A ativacao atual permanece com os movimentos originais", 2,
                cartela.consultarAtivacao(Personagem.TOLUI).getMovimentosRestantes());
        cartela.encerrarTurno();
        assertTrue(cartela.ativarColuna(3, Personagem.TOLUI));
        cartela.encerrarTurno();
        assertTrue(cartela.ativarColuna(1, Personagem.TOLUI));
        assertEquals(4, cartela.consultarAtivacao(Personagem.TOLUI).getMovimentosRestantes());
    }

    /** Uma linha nova requer três melhorias coloridas, uma em cada coluna. */
    @Test(timeout = 2000)
    public void testeLinhaCompletaLiberaUmaMelhoriaBranca() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoria(melhoria("CAV-R", 0));
        cartela.adicionarMelhoria(melhoria("CAV-V", 1));
        assertEquals(0, cartela.getLinhasBrancasPendentes());
        cartela.adicionarMelhoria(melhoria("CAV-A", 2));
        assertEquals("As três colunas completam uma linha", 1,
                cartela.getLinhasBrancasPendentes());
        assertTrue("A coluna branca aceita melhoria de qualquer cor",
                cartela.receberMelhoriaBranca(melhoria("ESP-V", 1)));
        assertEquals(0, cartela.getLinhasBrancasPendentes());
        assertEquals(1, cartela.contarMelhoriasNaColuna(3));
        assertFalse("Não se deve receber a mesma recompensa duas vezes",
                cartela.receberMelhoriaBranca(melhoria("MAO-R", 0)));
    }

    /** Melhorias na coluna branca só funcionam quando a branca é ativada. */
    @Test(timeout = 2000)
    public void testeMelhoriaBrancaFuncionaSomenteNaBranca() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        cartela.adicionarMelhoria(melhoria("CAV-R", 0));
        cartela.adicionarMelhoria(melhoria("CAV-V", 1));
        cartela.adicionarMelhoria(melhoria("CAV-A", 2));
        assertTrue(cartela.receberMelhoriaBranca(melhoria("MAO-V", 1)));
        assertTrue(cartela.ativarColuna(2, Personagem.TOLUI));
        assertEquals(2, cartela.consultarAtivacao(Personagem.TOLUI).getPunhosDaColuna());
        cartela.encerrarTurno();
        assertTrue(cartela.ativarColuna(3, Personagem.TOLUI));
        assertEquals(1, cartela.consultarAtivacao(Personagem.TOLUI).getPunhosDaColuna());
    }

    /** O Khan soma os votos ganhos por qualquer jogador. */
    @Test(timeout = 2000)
    public void testeKhanAvancaJuntoDaPontuacaoDeTodos() {
        InterfacePartida partida = new InterfacePartida();
        assertTrue(partida.registrarVotosConquistados(2));
        assertEquals(2, partida.consultarVotosJogador(0));
        assertTrue(partida.tentarAvancarJogador());
        assertTrue(partida.registrarVotosConquistados(3));
        assertEquals(3, partida.consultarVotosJogador(1));
        assertEquals(5, partida.consultarPontuacaoKhan());
    }

    /** A marca final dispara as duas passagens de rodada previstas no manual. */
    @Test(timeout = 2000)
    public void testeKhanDisparaFimSemEncerrarImediatamente() {
        InterfacePartida partida = new InterfacePartida();
        assertEquals("Dois jogadores encerram ao atingir a casa 9", 9, partida.consultarCasaFinal());
        assertTrue(partida.registrarVotosConquistados(9));
        assertTrue(partida.isFimDisparado());
        assertFalse(partida.isPartidaEncerrada());
        for (int i = 0; i < 4; i++) {
            assertTrue("Cada jogador ainda pode encerrar sua vez", partida.tentarAvancarJogador());
        }
        assertTrue("A partida deve encerrar após a rodada atual e a extra",
                partida.isPartidaEncerrada());
        assertFalse(partida.tentarAvancarJogador());
    }

    /** Limiares de pontuação são índices zero-based das casas do CSV. */
    @Test(timeout = 2000)
    public void testeLimitesDeFimPorQuantidadeDeJogadores() {
        assertEquals(9, new InterfacePartida().consultarCasaFinal());
        Cor[] cores = new Cor[] {Cor.AZUL, Cor.VERMELHO, Cor.VERDE, Cor.AMARELO, Cor.PRETO};
        for (int quantidade = 3; quantidade <= 5; quantidade++) {
            String[] nomes = new String[quantidade];
            Cor[] selecionadas = new Cor[quantidade];
            for (int i = 0; i < quantidade; i++) {
                nomes[i] = "Jogador " + i;
                selecionadas[i] = cores[i];
            }
            InterfacePartida partida = new InterfacePartida(nomes, selecionadas);
            if (quantidade == 3) {
                assertEquals(13, partida.consultarCasaFinal());
            }
            if (quantidade == 4) {
                assertEquals(15, partida.consultarCasaFinal());
            }
            if (quantidade == 5) {
                assertEquals(16, partida.consultarCasaFinal());
            }
        }
    }
}
