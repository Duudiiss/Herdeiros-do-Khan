package br.herdeiros.model;

import static org.junit.Assert.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/** Regressao de sorteio, verificacao, reposicao, recompensa e persistencia. */
public class ConselheirosAbertosCompletosTest {

    @SuppressWarnings("unchecked")
    private static <T> T campo(Object alvo, String nome) throws Exception {
        Field campo = alvo.getClass().getDeclaredField(nome);
        campo.setAccessible(true);
        return (T) campo.get(alvo);
    }

    private boolean cumpre(InterfacePartida partida, CartaConselheiroAberto carta,
                           Jogador jogador) throws Exception {
        Method metodo = InterfacePartida.class.getDeclaredMethod(
                "cumpreCartaAberta", CartaConselheiroAberto.class, Jogador.class);
        metodo.setAccessible(true);
        return (Boolean) metodo.invoke(partida, carta, jogador);
    }

    @Test(timeout = 3000)
    public void quatorzeCartasPossuemImagensOriginaisUnicas() {
        assertEquals(14, CartaConselheiroAberto.values().length);
        Set<String> arquivos = new HashSet<String>();
        for (CartaConselheiroAberto carta : CartaConselheiroAberto.values()) {
            assertTrue(arquivos.add(carta.getArquivoImagem()));
            assertTrue(new File("assets/conselheiros_abertos", carta.getArquivoImagem()).isFile());
        }
    }

    @Test(timeout = 3000)
    public void partidaInicialTemQuatroReveladasEDezSemRepeticoes() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        List<CartaConselheiroAberto> abertas = partida.consultarCartasAbertasAtivas();
        List<CartaConselheiroAberto> pilha = campo(partida, "pilhaCartasAbertas");
        assertEquals(4, abertas.size());
        assertEquals(10, pilha.size());
        Set<CartaConselheiroAberto> distintas = new HashSet<CartaConselheiroAberto>(abertas);
        distintas.addAll(pilha);
        assertEquals(14, distintas.size());
    }

    @Test(timeout = 3000)
    public void quatroEspadasRecompensaUmaVezRepoeEAvancaKhan() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        List<CartaConselheiroAberto> abertas = campo(partida, "cartasAbertasAtivas");
        List<CartaConselheiroAberto> pilha = campo(partida, "pilhaCartasAbertas");
        Map<CartaConselheiroAberto, Set<Integer>> elegiveis = campo(partida, "elegiveisAbertas");
        abertas.clear();
        pilha.clear();
        elegiveis.clear();
        abertas.add(CartaConselheiroAberto.QUATRO_ESPADAS);
        pilha.add(CartaConselheiroAberto.QUATRO_TESOUROS);
        elegiveis.put(CartaConselheiroAberto.QUATRO_ESPADAS,
                new HashSet<Integer>(Arrays.asList(0, 1)));
        int votos = partida.consultarVotosJogador(0);
        int khan = partida.consultarPontuacaoKhan();
        assertTrue(partida.simularRecebimentoTributo(0, "ESPADA", 4));
        assertEquals(votos + 1, partida.consultarVotosJogador(0));
        assertEquals(khan + 1, partida.consultarPontuacaoKhan());
        assertEquals(Arrays.asList(CartaConselheiroAberto.QUATRO_TESOUROS),
                partida.consultarCartasAbertasAtivas());
        assertEquals(0, partida.consultarQuantidadeCartasAbertasNaPilha());
        assertTrue(partida.simularRecebimentoTributo(0, "ESPADA", 1));
        assertEquals(votos + 1, partida.consultarVotosJogador(0));
    }

    @Test(timeout = 3000)
    public void cincoTiposNaoSaoSubstituidosPorCincoIguais() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        Jogador jogador = partidaJogador(partida, 0);
        List<TipoTesouro> mao = campo(partida, "tesourosPorJogador") instanceof Map
                ? ((Map<Jogador, List<TipoTesouro>>) campo(partida, "tesourosPorJogador")).get(jogador)
                : new ArrayList<TipoTesouro>();
        for (int i = 0; i < 5; i++) { mao.add(TipoTesouro.GADO); }
        assertFalse(cumpre(partida, CartaConselheiroAberto.CINCO_TESOUROS_DIFERENTES, jogador));
        mao.clear();
        mao.addAll(Arrays.asList(TipoTesouro.values()));
        assertTrue(cumpre(partida, CartaConselheiroAberto.CINCO_TESOUROS_DIFERENTES, jogador));
    }

    @SuppressWarnings("unchecked")
    private Jogador partidaJogador(InterfacePartida partida, int indice) throws Exception {
        return ((List<Jogador>) campo(partida, "participantes")).get(indice);
    }

    @Test(timeout = 3000)
    public void bonusUsadoContinuaContandoNoObjetivo() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        Jogador jogador = partidaJogador(partida, 0);
        Map<Jogador, Integer> recebidos = campo(partida, "bonusRecebidosPorJogador");
        recebidos.put(jogador, 3);
        assertTrue(cumpre(partida, CartaConselheiroAberto.DOIS_BONUS, jogador));
        assertTrue(cumpre(partida, CartaConselheiroAberto.TRES_BONUS, jogador));
        assertTrue(((Map<Jogador, List<PecaBonus>>)campo(partida, "bonusPorJogador")).get(jogador).isEmpty());
    }

    @Test(timeout = 3000)
    public void cartasNovasReconhecemConquistasJaAlcancadas() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        assertTrue(partida.simularRecebimentoTributo(0, "ESPADA", 4));
        Method revelar = InterfacePartida.class.getDeclaredMethod(
                "revelarCartaAberta", CartaConselheiroAberto.class);
        revelar.setAccessible(true);
        List<CartaConselheiroAberto> abertas = campo(partida, "cartasAbertasAtivas");
        Map<CartaConselheiroAberto, Set<Integer>> elegiveis = campo(partida, "elegiveisAbertas");
        abertas.clear();
        elegiveis.clear();
        revelar.invoke(partida, CartaConselheiroAberto.QUATRO_ESPADAS);
        assertTrue(elegiveis.get(CartaConselheiroAberto.QUATRO_ESPADAS).contains(0));
        int votos = partida.consultarVotosJogador(0);
        assertTrue(partida.simularRecebimentoTributo(0, "ESPADA", 1));
        assertEquals(votos + 1, partida.consultarVotosJogador(0));
    }

    @Test(timeout = 3000)
    public void saveMantemOrdemECondicoesDeCartasAbertas() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        Path arquivo = Files.createTempFile("khan-cartas-abertas", ".txt");
        try {
            GerenciadorSave.salvar(arquivo, partida);
            InterfacePartida restaurada = GerenciadorSave.carregar(arquivo);
            assertEquals(partida.consultarCartasAbertasAtivas(),
                    restaurada.consultarCartasAbertasAtivas());
            assertEquals(partida.consultarQuantidadeCartasAbertasNaPilha(),
                    restaurada.consultarQuantidadeCartasAbertasNaPilha());
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }
    @Test(timeout = 3000)
    public void metasDeMelhoriasRespeitamColunasEContamSomentePecas() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        Jogador jogador = partidaJogador(partida, 0);
        TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
        assertFalse(cumpre(partida, CartaConselheiroAberto.UMA_LINHA_MELHORIAS, jogador));
        cartela.adicionarMelhoria(MercadoMelhorias.criarPorCodigo("CAV-R", 0));
        cartela.adicionarMelhoria(MercadoMelhorias.criarPorCodigo("CAV-V", 1));
        cartela.adicionarMelhoria(MercadoMelhorias.criarPorCodigo("CAV-A", 2));
        assertTrue(cumpre(partida, CartaConselheiroAberto.UMA_LINHA_MELHORIAS, jogador));
        assertFalse(cumpre(partida, CartaConselheiroAberto.DUAS_COLUNAS_DUAS_MELHORIAS, jogador));
        cartela.adicionarMelhoria(MercadoMelhorias.criarPorCodigo("MOE-R", 0));
        cartela.adicionarMelhoria(MercadoMelhorias.criarPorCodigo("MOE-V", 1));
        assertTrue(cumpre(partida, CartaConselheiroAberto.DUAS_COLUNAS_DUAS_MELHORIAS, jogador));
        assertFalse(cumpre(partida, CartaConselheiroAberto.QUATRO_MELHORIAS_COLUNA, jogador));
        cartela.adicionarMelhoria(MercadoMelhorias.criarPorCodigo("ESP-R", 0));
        cartela.adicionarMelhoria(MercadoMelhorias.criarPorCodigo("MAO-R", 0));
        assertTrue(cumpre(partida, CartaConselheiroAberto.QUATRO_MELHORIAS_COLUNA, jogador));
    }

    @Test(timeout = 3000)
    public void contagemDeCidadesExigeConquistaRealRegistrada() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        Jogador jogador = partidaJogador(partida, 0);
        Map<Jogador, Integer> conquistas = campo(partida, "cidadesConquistadasPorJogador");
        assertFalse(cumpre(partida, CartaConselheiroAberto.DUAS_CIDADES, jogador));
        conquistas.put(jogador, 2);
        assertTrue(cumpre(partida, CartaConselheiroAberto.DUAS_CIDADES, jogador));
        assertFalse(cumpre(partida, CartaConselheiroAberto.TRES_CIDADES, jogador));
        conquistas.put(jogador, 3);
        assertTrue(cumpre(partida, CartaConselheiroAberto.TRES_CIDADES, jogador));
    }

    @Test(timeout = 3000)
    public void seisEDezYurtsSaoMetasDistintas() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        Jogador jogador = partidaJogador(partida, 0);
        Jogo jogo = campo(partida, "jogo");
        Tabuleiro tabuleiro = jogo.getTabuleiro();
        for (int id = 100; id < 110; id++) {
            assertTrue(tabuleiro.adicionarParada(id, false));
            assertTrue(tabuleiro.construirYurt(id, jogador.getCor()));
            if (id == 105) {
                assertTrue(cumpre(partida, CartaConselheiroAberto.SEIS_YURTS, jogador));
                assertFalse(cumpre(partida, CartaConselheiroAberto.DEZ_YURTS, jogador));
            }
        }
        assertTrue(cumpre(partida, CartaConselheiroAberto.DEZ_YURTS, jogador));
    }

    @Test(timeout = 3000)
    public void regiaoExigeCadaUmaDasTresSeparadamente() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        Jogador jogador = partidaJogador(partida, 0);
        Jogo jogo = campo(partida, "jogo");
        Map<Integer, Regiao> regioes = campo(partida, "regiaoPorParada");
        int id = 120;
        for (Regiao regiao : new Regiao[] {Regiao.CHINA, Regiao.RUSSIA, Regiao.PERSIA}) {
            for (int i = 0; i < 2; i++) {
                assertTrue(jogo.getTabuleiro().adicionarParada(id, false));
                assertTrue(jogo.getTabuleiro().construirYurt(id, jogador.getCor()));
                regioes.put(id, regiao);
                id++;
                if (regiao == Regiao.RUSSIA && i == 0) {
                    assertFalse(cumpre(partida, CartaConselheiroAberto.UM_YURT_CADA_REGIAO, jogador));
                }
            }
        }
        assertTrue(cumpre(partida, CartaConselheiroAberto.UM_YURT_CADA_REGIAO, jogador));
        assertTrue(cumpre(partida, CartaConselheiroAberto.DOIS_YURTS_CADA_REGIAO, jogador));
    }

    @Test(timeout = 3000)
    public void duasCartasCumpridasNaMesmaAcaoPremiamDuasVezes() throws Exception {
        InterfacePartida partida = new InterfacePartida(2);
        List<CartaConselheiroAberto> abertas = campo(partida, "cartasAbertasAtivas");
        List<CartaConselheiroAberto> pilha = campo(partida, "pilhaCartasAbertas");
        Map<CartaConselheiroAberto, Set<Integer>> elegiveis = campo(partida, "elegiveisAbertas");
        abertas.clear();
        pilha.clear();
        elegiveis.clear();
        abertas.add(CartaConselheiroAberto.QUATRO_ESPADAS);
        abertas.add(CartaConselheiroAberto.QUATRO_TESOUROS);
        elegiveis.put(CartaConselheiroAberto.QUATRO_ESPADAS,
                new HashSet<Integer>(Arrays.asList(0, 1)));
        elegiveis.put(CartaConselheiroAberto.QUATRO_TESOUROS,
                new HashSet<Integer>(Arrays.asList(0, 1)));
        @SuppressWarnings("unchecked")
        List<TipoTesouro> mao = ((Map<Jogador, List<TipoTesouro>>)campo(partida,
                "tesourosPorJogador")).get(partidaJogador(partida, 0));
        mao.addAll(Arrays.asList(TipoTesouro.GADO, TipoTesouro.GADO,
                TipoTesouro.GADO, TipoTesouro.GADO));
        assertTrue(partida.simularRecebimentoTributo(0, "ESPADA", 4));
        assertEquals(2, partida.consultarVotosJogador(0));
        assertEquals(2, partida.consultarPontuacaoKhan());
        assertTrue(partida.consultarCartasAbertasAtivas().isEmpty());
    }

}
