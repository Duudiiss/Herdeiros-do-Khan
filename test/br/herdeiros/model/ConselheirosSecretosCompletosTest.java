package br.herdeiros.model;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/** Regressao dos dez objetivos, inclusive deduplicacao e pontuacao final. */
public class ConselheirosSecretosCompletosTest {
    private final Jogador jogador = new Jogador("Teste", Cor.AZUL, 1, Personagem.ALTANI);

    private Tabuleiro criarTabuleiro(int quantidade) {
        Tabuleiro tabuleiro = new Tabuleiro(Collections.<Cidade>emptyList());
        for (int id = 1; id <= quantidade; id++) {
            assertTrue(tabuleiro.adicionarParada(id, false));
        }
        return tabuleiro;
    }

    private List<ResumoMelhoria> melhorias(String... codigos) {
        List<ResumoMelhoria> resultado = new ArrayList<ResumoMelhoria>();
        for (int i = 0; i < codigos.length; i++) {
            resultado.add(new ResumoMelhoria(codigos[i], "Beneficio " + i, i % 4));
        }
        return resultado;
    }

    private Set<Integer> ids(Integer... valores) {
        return new HashSet<Integer>(Arrays.asList(valores));
    }

    private int progresso(CartaConselheiroSecreto carta, Tabuleiro tabuleiro,
            List<ResumoMelhoria> melhorias,
            Map<Integer, Regiao> regioes,
            Map<DestinoKhan, Set<Integer>> acessosKhan,
            Map<NomeCidade, Set<Integer>> acessosCidades,
            List<Integer> entregas) {
        return AvaliadorPontuacaoFinal.progressoConselheiroSecreto(jogador, carta,
                melhorias, tabuleiro, regioes, Collections.<NomeCidade, Regiao>emptyMap(),
                acessosKhan, acessosCidades, entregas);
    }

    private int progresso(CartaConselheiroSecreto carta, Tabuleiro tabuleiro,
            List<ResumoMelhoria> melhorias) {
        return progresso(carta, tabuleiro, melhorias,
                Collections.<Integer, Regiao>emptyMap(),
                Collections.<DestinoKhan, Set<Integer>>emptyMap(),
                Collections.<NomeCidade, Set<Integer>>emptyMap(),
                Collections.<Integer>emptyList());
    }

    @Test(timeout = 2000)
    public void osDezConselheirosPossuemTresPatamaresENaoAcumulamVotos() {
        int[][] metas = {
            {3, 6, 8}, {4, 7, 10}, {4, 6, 7}, {1, 2, 4}, {1, 2, 3},
            {2, 3, 4}, {2, 3, 4}, {3, 6, 8}, {2, 4, 5}, {2, 4, 6}
        };
        CartaConselheiroSecreto[] cartas = CartaConselheiroSecreto.values();
        assertEquals(10, cartas.length);
        for (int i = 0; i < cartas.length; i++) {
            assertEquals(0, AvaliadorPontuacaoFinal.votosPorConselheiro(cartas[i], 0));
            assertEquals(1, AvaliadorPontuacaoFinal.votosPorConselheiro(cartas[i], metas[i][0]));
            assertEquals(2, AvaliadorPontuacaoFinal.votosPorConselheiro(cartas[i], metas[i][1]));
            assertEquals(3, AvaliadorPontuacaoFinal.votosPorConselheiro(cartas[i], metas[i][2]));
            assertEquals(3, AvaliadorPontuacaoFinal.votosPorConselheiro(cartas[i], 99));
        }
    }

    @Test(timeout = 2000)
    public void viajanteContaApenasMaiorRedeMesmoComRamificacoes() {
        Tabuleiro t = criarTabuleiro(7);
        for (int[] aresta : new int[][] {{1,2},{2,3},{2,4},{5,6}}) {
            assertTrue(t.conectarParadas(aresta[0], aresta[1]));
        }
        for (int id : new int[] {1,2,3,4,5,6}) {
            assertTrue(t.construirYurt(id, Cor.AZUL));
        }
        assertEquals(4, progresso(CartaConselheiroSecreto.VIAJANTE, t,
                Collections.<ResumoMelhoria>emptyList()));
    }

    @Test(timeout = 2000)
    public void soldadoContaPecasEIgnoreAcoesIniciais() {
        Tabuleiro t = criarTabuleiro(1);
        assertEquals(0, progresso(CartaConselheiroSecreto.SOLDADO, t, melhorias()));
        assertEquals(4, progresso(CartaConselheiroSecreto.SOLDADO, t,
                melhorias("CAV-R", "CAV-V", "CAV-A", "CAS-R")));
    }

    @Test(timeout = 2000)
    public void colecionadorUsaSimboloNaoDescricaoNemCor() {
        Tabuleiro t = criarTabuleiro(1);
        List<ResumoMelhoria> cartas = melhorias("CAV-R", "CAV-V", "MAO-A", "ESP-R", "MOE-A");
        assertEquals(4, progresso(CartaConselheiroSecreto.COLECIONADOR, t, cartas));
    }

    @Test(timeout = 2000)
    public void sacerdoteCadaPecaParticipaDeUmUnicoPar() {
        Tabuleiro t = criarTabuleiro(1);
        assertEquals(2, progresso(CartaConselheiroSecreto.SACERDOTE, t,
                melhorias("CAV-R", "CAV-V", "CAV-A", "CAV-R", "ESP-A")));
    }

    @Test(timeout = 2000)
    public void governadorExigeYurtsEMelhoriasPorRegiaoSimultaneamente() {
        Tabuleiro t = criarTabuleiro(6);
        Map<Integer, Regiao> regioes = new HashMap<Integer, Regiao>();
        for (int id = 1; id <= 6; id++) {
            assertTrue(t.construirYurt(id, Cor.AZUL));
            regioes.put(id, id <= 2 ? Regiao.CHINA : id <= 4 ? Regiao.PERSIA : Regiao.RUSSIA);
        }
        List<ResumoMelhoria> melhorias = Arrays.asList(
                new ResumoMelhoria("CAV-R", "", 0),
                new ResumoMelhoria("MAO-R", "", 0),
                new ResumoMelhoria("CAV-V", "", 1),
                new ResumoMelhoria("MAO-V", "", 1),
                new ResumoMelhoria("CAV-A", "", 2));
        assertEquals(2, progresso(CartaConselheiroSecreto.GOVERNADOR, t, melhorias,
                regioes, Collections.<DestinoKhan, Set<Integer>>emptyMap(),
                Collections.<NomeCidade, Set<Integer>>emptyMap(),
                Collections.<Integer>emptyList()));
    }

    @Test(timeout = 2000)
    public void bajuladorNaoDuplicaYurtQueAtendeDuasProvincias() {
        Tabuleiro t = criarTabuleiro(3);
        assertTrue(t.construirYurt(1, Cor.AZUL));
        assertTrue(t.construirYurt(2, Cor.AZUL));
        Map<DestinoKhan, Set<Integer>> acessos = new EnumMap<DestinoKhan, Set<Integer>>(DestinoKhan.class);
        DestinoKhan[] destinos = DestinoKhan.values();
        acessos.put(destinos[0], ids(1, 2));
        acessos.put(destinos[1], ids(1, 2));
        assertEquals(2, progresso(CartaConselheiroSecreto.BAJULADOR, t,
                melhorias(), Collections.<Integer, Regiao>emptyMap(), acessos,
                Collections.<NomeCidade, Set<Integer>>emptyMap(),
                Collections.<Integer>emptyList()));
    }

    @Test(timeout = 2000)
    public void prefeitoContaYurtsEnquantoCidadaoContaCidades() {
        Tabuleiro t = criarTabuleiro(3);
        assertTrue(t.construirYurt(1, Cor.AZUL));
        assertTrue(t.construirYurt(2, Cor.AZUL));
        Map<NomeCidade, Set<Integer>> cidades = new EnumMap<NomeCidade, Set<Integer>>(NomeCidade.class);
        NomeCidade[] nomes = NomeCidade.values();
        cidades.put(nomes[0], ids(1, 2));
        cidades.put(nomes[1], ids(1));
        cidades.put(nomes[2], ids(1));
        assertEquals(2, progresso(CartaConselheiroSecreto.PREFEITO, t, melhorias(),
                Collections.<Integer, Regiao>emptyMap(),
                Collections.<DestinoKhan, Set<Integer>>emptyMap(), cidades,
                Collections.<Integer>emptyList()));
        assertEquals(3, progresso(CartaConselheiroSecreto.CIDADAO, t, melhorias(),
                Collections.<Integer, Regiao>emptyMap(),
                Collections.<DestinoKhan, Set<Integer>>emptyMap(), cidades,
                Collections.<Integer>emptyList()));
    }

    @Test(timeout = 2000)
    public void cavaleiroContaApenasMelhoriasComCavalosOuPunhos() {
        Tabuleiro t = criarTabuleiro(1);
        assertEquals(3, progresso(CartaConselheiroSecreto.CAVALEIRO, t,
                melhorias("CAV-R", "MAO-V", "CAV-A", "CAS-R", "MOE-A")));
    }

    @Test(timeout = 2000)
    public void mensageiroContaLocaisDeEntregaAdjacentesIncluindoKarakorum() {
        Tabuleiro t = criarTabuleiro(4);
        t.conectarParadas(0, 1);
        t.conectarParadas(2, 1);
        t.conectarParadas(3, 1);
        assertTrue(t.construirYurt(1, Cor.AZUL));
        assertEquals(3, progresso(CartaConselheiroSecreto.MENSAGEIRO, t,
                melhorias(), Collections.<Integer, Regiao>emptyMap(),
                Collections.<DestinoKhan, Set<Integer>>emptyMap(),
                Collections.<NomeCidade, Set<Integer>>emptyMap(),
                Arrays.asList(0, 2, 2, 3)));
    }

    @Test(timeout = 2000)
    public void jogadorNaoPassaPrimeiroTurnoSemEscolherCarta() {
        InterfacePartida p = new InterfacePartida(2);
        assertFalse(p.tentarAvancarJogador());
        assertTrue(p.consultarOpcoesConselheiroSecretoJogadorAtual().size() > 0);
        assertTrue(p.escolherConselheiroSecretoJogadorAtual(0));
        assertTrue(p.tentarAvancarJogador());
        assertFalse(p.tentarAvancarJogador());
        assertTrue(p.escolherConselheiroSecretoJogadorAtual(0));
    }

    @Test(timeout = 2000)
    public void consultarResultadoDuasVezesNaoAcumulaVotosSecretos() {
        InterfacePartida p = new InterfacePartida(2);
        assertTrue(p.escolherConselheiroSecretoJogadorAtual(0));
        List<ResumoResultadoFinal> primeiro = p.consultarResultadoFinal();
        List<ResumoResultadoFinal> segundo = p.consultarResultadoFinal();
        assertEquals(primeiro.size(), segundo.size());
        for (int i = 0; i < primeiro.size(); i++) {
            assertEquals(primeiro.get(i).getVotosTotais(), segundo.get(i).getVotosTotais());
            assertEquals(primeiro.get(i).getVotosConselheiroSecreto(),
                    segundo.get(i).getVotosConselheiroSecreto());
        }
    }
    @Test(timeout = 5000)
    public void escolhaSecretaSobreviveAoSalvarECarregar() throws java.io.IOException {
        InterfacePartida original = new InterfacePartida(2);
        assertTrue(original.escolherConselheiroSecretoJogadorAtual(0));
        CartaConselheiroSecreto antes = original.consultarConselheiroSecretoJogadorAtual();
        java.nio.file.Path save = java.nio.file.Files.createTempFile("segredo-conselheiro-", ".txt");
        try {
            GerenciadorSave.salvar(save, original);
            InterfacePartida restaurada = GerenciadorSave.carregar(save);
            assertEquals(antes, restaurada.consultarConselheiroSecretoJogadorAtual());
            assertTrue(restaurada.consultarOpcoesConselheiroSecretoJogadorAtual().isEmpty());
            assertFalse(restaurada.escolherConselheiroSecretoJogadorAtual(0));
        } finally {
            java.nio.file.Files.deleteIfExists(save);
        }
    }

}
