package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import br.herdeiros.controller.ControladorTabuleiro;
import br.herdeiros.mapa.MapaDados;

/** Verifica escolha independente de personagem, sorteio da ordem e movimentacao por turno. */
public class PersonagensTurnosTest {

    private InterfacePartida criarPartidaComMapa() {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.15, 0.15, false);
        mapa.adicionar(2, 0.25, 0.25, false);
        mapa.adicionar(3, 0.35, 0.35, false);
        mapa.conectar(0, 1);
        mapa.conectar(1, 2);
        mapa.conectar(2, 3);
        assertTrue("Mapa de teste deve ser valido", partida.carregarMapa(mapa));
        return partida;
    }

    /** Objetivo: permitir dois cliques validos no mesmo turno.
     * Condicao inicial: dois movimentos e cadeia de tres paradas.
     * Resultado esperado: peao avanca duas vezes e a vez permanece com o mesmo jogador. */
    @Test(timeout = 2000)
    public void testeDoisMovimentosNoMesmoTurno() {
        InterfacePartida partida = criarPartidaComMapa();
        ControladorTabuleiro controller = new ControladorTabuleiro(partida, new MapaDados());
        controller.configurarMovimentos(2);
        assertTrue("Deve chegar a primeira parada", controller.tentarMoverPara(1));
        assertTrue("Deve continuar ate a segunda parada", controller.tentarMoverPara(2));
        assertEquals("Os dois movimentos foram gastos", 0, controller.getMovimentosRestantes());
        assertEquals("Somente Passar turno muda o jogador", 0, partida.getIndiceJogadorAtual());
        assertEquals("O peao terminou na segunda parada", 2, partida.getParadaJogador(0));
    }

    /** Objetivo: descontar duas conexoes numa unica escolha de destino.
     * Condicao inicial: tres movimentos e destino a duas conexoes.
     * Resultado esperado: resta um movimento no mesmo turno. */
    @Test(timeout = 2000)
    public void testeDestinoDistanteConsomeDistanciaReal() {
        InterfacePartida partida = criarPartidaComMapa();
        ControladorTabuleiro controller = new ControladorTabuleiro(partida, new MapaDados());
        controller.configurarMovimentos(3);
        assertTrue("Dois passos devem ser aceitos", controller.tentarMoverPara(2));
        assertEquals("Deve restar um movimento", 1, controller.getMovimentosRestantes());
        assertTrue("O jogador pode gastar o movimento restante", controller.tentarMoverPara(3));
        assertEquals("A vez continua a mesma apos se mover", 0, partida.getIndiceJogadorAtual());
    }

    /** Objetivo: rejeitar destino caro sem modificar estado.
     * Condicao inicial: limite de um movimento e destino a duas conexoes.
     * Resultado esperado: a posicao, o saldo e o turno permanecem intactos. */
    @Test(timeout = 2000)
    public void testeMovimentoInvalidoNaoConsomeOrcamento() {
        InterfacePartida partida = criarPartidaComMapa();
        ControladorTabuleiro controller = new ControladorTabuleiro(partida, new MapaDados());
        assertFalse("Nao e possivel percorrer duas conexoes com um movimento", controller.tentarMoverPara(2));
        assertEquals("A posicao deve continuar em Karakorum", 0, partida.getParadaJogador(0));
        assertEquals("Saldo nao pode ser consumido em recusa", 1, controller.getMovimentosRestantes());
        assertEquals("Jogador da vez nao muda", 0, partida.getIndiceJogadorAtual());
    }

    /** Objetivo: passar turno somente quando o comando explicito e usado.
     * Condicao inicial: primeiro jogador ja gastou um movimento.
     * Resultado esperado: a vez avanca e o saldo do turno seguinte e restaurado. */
    @Test(timeout = 2000)
    public void testeBotaoLogicoPassarTurnoRestauraSaldo() {
        InterfacePartida partida = criarPartidaComMapa();
        ControladorTabuleiro controller = new ControladorTabuleiro(partida, new MapaDados());
        controller.configurarMovimentos(3);
        assertTrue("Primeiro deslocamento deve funcionar", controller.tentarMoverPara(1));
        controller.passarVez();
        assertEquals("A vez deve ser do proximo jogador", 1, partida.getIndiceJogadorAtual());
        assertEquals("Novo turno recebe o limite completo", 3, controller.getMovimentosRestantes());
        assertTrue("O proximo jogador pode atravessar a parada ocupada", controller.tentarMoverPara(2));
    }

    /** Objetivo: garantir escolha de personagem independente da cor.
     * Condicao inicial: dois jogadores com personagens explicitamente selecionados.
     * Resultado esperado: os personagens correspondem as escolhas, nao as cores. */
    @Test(timeout = 2000)
    public void testePersonagemIndependenteDaCor() {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Jogador A", "Jogador B"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.OGEDEI, Personagem.ALTANI}, false);
        assertEquals("Azul escolheu Ogedei", Personagem.OGEDEI, partida.getPersonagemJogador(0));
        assertEquals("Vermelho escolheu Altani", Personagem.ALTANI, partida.getPersonagemJogador(1));
        assertEquals("Imagem de Ogedei e cartela5", 4, partida.getIndiceCartelaJogador(0));
        assertEquals("Imagem de Altani e cartela1", 0, partida.getIndiceCartelaJogador(1));
    }

    /** Objetivo: impedir personagem repetido na mesma partida.
     * Condicao inicial: duas escolhas iguais.
     * Resultado esperado: a criacao rejeita o cadastro. */
    @Test(timeout = 2000)
    public void testePersonagensDuplicadosSaoRecusados() {
        try {
            new InterfacePartida(new String[] {"A", "B"},
                    new Cor[] {Cor.AZUL, Cor.VERMELHO},
                    new Personagem[] {Personagem.JOCHI, Personagem.JOCHI}, true);
            fail("Personagem repetido deve causar IllegalArgumentException");
        } catch (IllegalArgumentException esperado) {
            assertNotNull("Deve fornecer mensagem explicativa", esperado.getMessage());
        }
    }

    /** Objetivo: testar sorteio sem depender de uma permutacao fixa.
     * Condicao inicial: cinco participantes validos, ordem aleatoria habilitada.
     * Resultado esperado: todos aparecem uma vez, com cor e personagem preservados. */
    @Test(timeout = 2000)
    public void testeSorteioDaOrdemPreservaOsParticipantes() {
        String[] nomes = {"A", "B", "C", "D", "E"};
        Cor[] cores = {Cor.AZUL, Cor.VERMELHO, Cor.VERDE, Cor.AMARELO, Cor.PRETO};
        Personagem[] personagens = Personagem.values();
        InterfacePartida partida = new InterfacePartida(nomes, cores, personagens, true);
        Set<String> encontrados = new HashSet<String>();
        for (int i = 0; i < partida.getQuantidadeJogadores(); i++) {
            String nome = partida.getNomeJogador(i);
            assertTrue("O jogador nao pode se repetir no sorteio", encontrados.add(nome));
            int posicaoOriginal = -1;
            for (int j = 0; j < nomes.length; j++) {
                if (nomes[j].equals(nome)) {
                    posicaoOriginal = j;
                }
            }
            assertTrue("O participante sorteado deve pertencer ao cadastro", posicaoOriginal >= 0);
            assertEquals("A cor pertence ao participante", cores[posicaoOriginal], partida.getCorJogador(i));
            assertEquals("O personagem pertence ao participante", personagens[posicaoOriginal], partida.getPersonagemJogador(i));
        }
        assertEquals("Todos os cinco participantes continuam presentes", nomes.length, encontrados.size());
    }
    /** Objetivo: impedir alterar a quantidade de cavalos durante o movimento.
     * Condicao inicial: dois movimentos configurados; um movimento ja consumido.
     * Resultado esperado: tentar selecionar seis movimentos nao aumenta o saldo. */
    @Test(timeout = 2000)
    public void testeNaoPermiteAumentarCavalosDuranteTurno() {
        InterfacePartida partida = criarPartidaComMapa();
        ControladorTabuleiro controller = new ControladorTabuleiro(partida, new MapaDados());
        controller.configurarMovimentos(2);
        assertTrue("O primeiro movimento deve ser aceito", controller.tentarMoverPara(1));
        controller.configurarMovimentos(6);
        assertEquals("Nao pode aumentar o saldo apos se movimentar", 1,
                controller.getMovimentosRestantes());
    }

}
