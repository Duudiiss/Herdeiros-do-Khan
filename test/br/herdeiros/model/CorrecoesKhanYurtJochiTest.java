package br.herdeiros.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import org.junit.Test;
import br.herdeiros.mapa.MapaDados;

/** Regressao das regras introduzidas na quarta iteracao. */
public class CorrecoesKhanYurtJochiTest {

    @Test(timeout = 3000)
    public void limiteDeDozeYurtsIncluiYurtsConquistadas() {
        Tabuleiro tabuleiro = new Tabuleiro(Collections.<Cidade>emptyList());
        assertTrue(tabuleiro.registrarYurtConquista(NomeCidade.KIEV, Cor.AZUL));
        for (int parada = 1; parada <= 11; parada++) {
            assertTrue(tabuleiro.adicionarParada(parada, false));
            assertTrue(tabuleiro.construirYurt(parada, Cor.AZUL));
        }
        assertEquals(12, tabuleiro.contarTotalYurtsNoMapa(Cor.AZUL));
        assertTrue(tabuleiro.adicionarParada(12, false));
        assertFalse(tabuleiro.construirYurt(12, Cor.AZUL));
        assertFalse(tabuleiro.registrarYurtConquista(NomeCidade.MOSCOU, Cor.AZUL));
        assertTrue(tabuleiro.construirYurt(12, Cor.VERMELHO));
    }

    @Test(timeout = 3000)
    public void paradaAdjacenteComYurtPropriaNaoGastaCavalos() {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Primeiro", "Segundo"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.CHAGATAI, Personagem.ALTANI}, false);
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.25, 0.25, false);
        assertTrue(mapa.conectar(0, 1));
        assertTrue(partida.carregarMapa(mapa));
        assertTrue(partida.desenvolvimentoAjustarTributo(0, "YURT", 1));
        assertTrue(partida.movimentarJogadorAtual(1, 1));
        assertTrue(partida.construirYurtAtual(1));
        assertTrue(partida.movimentarJogador(0, 0));
        assertTrue(partida.ativarColunaJogadorAtual(0));
        int cavalosAntes = partida.consultarCavalosRestantesJogadorAtual();
        assertTrue(cavalosAntes >= 1);
        assertEquals(0, partida.consultarCustoMovimentoAtual(1));
        assertTrue(partida.movimentarJogadorComAtivacao(1));
        assertEquals(cavalosAntes, partida.consultarCavalosRestantesJogadorAtual());
        assertEquals(1, partida.getParadaJogador(0));
    }

    @Test(timeout = 3000)
    public void reposicaoDoKhanAfetaSomenteSeusTresIcones() {
        InterfacePartida partida = new InterfacePartida(2);
        assertTrue(partida.desenvolvimentoPosicionarKhan(DestinoKhan.RUSSIA_OESTE));
        for (int i = 0; i < 12; i++) {
            assertEquals("Apenas os tres icones mapeados podem ser repostos",
                    i < 3 ? 2 : 1, partida.consultarQuantidadeTributoIcone(i));
        }
        assertTrue(partida.desenvolvimentoPosicionarKhan(DestinoKhan.CHINA_LESTE));
        for (int i = 0; i < 12; i++) {
            assertEquals("A segunda provincia nao pode alterar outras areas",
                    i < 3 || i >= 9 ? 2 : 1,
                    partida.consultarQuantidadeTributoIcone(i));
        }
    }

    @Test(timeout = 3000)
    public void jochiSoOfereceEscolhaNoSeuTurno() {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Primeiro", "Jochi"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.CHAGATAI, Personagem.JOCHI}, false);
        assertTrue(partida.consultarOpcoesIniciaisJochi().isEmpty());
        assertFalse(partida.escolherMelhoriaInicialJochi(0));
        assertTrue(TestesConselheirosAux.avancar(partida));
        assertEquals(2, partida.consultarOpcoesIniciaisJochi().size());
        assertTrue(partida.escolherMelhoriaInicialJochi(0));
        assertTrue(partida.consultarOpcoesIniciaisJochi().isEmpty());
    }
}
