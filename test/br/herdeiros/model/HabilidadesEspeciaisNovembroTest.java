package br.herdeiros.model;

import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import br.herdeiros.mapa.MapaDados;

/** Testes das habilidades individuais e da selecao de tres tesouros. */
public class HabilidadesEspeciaisNovembroTest {
    private InterfacePartida criar(Personagem primeiro) {
        return new InterfacePartida(new String[] {"Um", "Dois"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {primeiro,
                        primeiro == Personagem.ALTANI ? Personagem.TOLUI : Personagem.ALTANI}, false);
    }

    @Test(timeout = 2000)
    public void jochiEscolheUmaDasDuasMelhoriasIniciaisParaBranca() {
        InterfacePartida partida = criar(Personagem.JOCHI);
        List<ResumoMelhoria> opcoes = partida.consultarOpcoesIniciaisJochi();
        assertEquals(2, opcoes.size());
        assertFalse(partida.ativarColunaJogadorAtual(0));
        assertTrue(partida.escolherMelhoriaInicialJochi(1));
        assertTrue(partida.consultarOpcoesIniciaisJochi().isEmpty());
        assertEquals(1, partida.consultarCartelaJogadorAtual().getMelhoriasDetalhadas().size());
        assertEquals(3, partida.consultarCartelaJogadorAtual()
                .getMelhoriasDetalhadas().get(0).getColuna());
        assertEquals(opcoes.get(1).getCodigo(), partida.consultarCartelaJogadorAtual()
                .getMelhoriasDetalhadas().get(0).getCodigo());
    }

    @Test(timeout = 2000)
    public void jochiPodeComprarMelhoriaDeQualquerCorNaColunaBranca() {
        InterfacePartida partida = criar(Personagem.JOCHI);
        assertTrue(partida.escolherMelhoriaInicialJochi(0));
        assertTrue(partida.desenvolvimentoAjustarTributo(0, "MOEDA", 4));
        assertTrue(partida.comprarMelhoriaAtual(0, 3));
        assertEquals(2, partida.consultarCartelaJogadorAtual().getMelhoriasDetalhadas().size());
        assertFalse(partida.comprarMelhoriaAtual(1, 4));
    }

    @Test(timeout = 2000)
    public void chagataiTrocaUmCavaloPorUmaEspadaApenasUmaVez() {
        InterfacePartida partida = criar(Personagem.CHAGATAI);
        assertFalse(partida.usarHabilidadeChagatai());
        assertTrue(partida.ativarColunaJogadorAtual(0));
        int cavalos = partida.consultarCavalosRestantesJogadorAtual();
        int espadas = partida.consultarCartelaJogadorAtual().getEspadas();
        assertTrue(partida.usarHabilidadeChagatai());
        assertEquals(cavalos - 1, partida.consultarCavalosRestantesJogadorAtual());
        assertEquals(espadas + 1, partida.consultarCartelaJogadorAtual().getEspadas());
        assertFalse(partida.usarHabilidadeChagatai());
    }

    @Test(timeout = 2000)
    public void ogedeiRetiraTesouroDeCidadeDistanteSemConsumirOUltimo() {
        InterfacePartida partida = criar(Personagem.OGEDEI);
        String nome = partida.consultarCidadesDisponiveis().get(0).getNome();
        TipoTesouro tipo = partida.consultarTesourosCidade(nome).get(0);
        int antes = partida.consultarTesourosCidade(nome).size();
        assertTrue(antes > 1);
        assertTrue(partida.desenvolvimentoAjustarTributo(0, "MOEDA", 1));
        assertTrue(partida.usarHabilidadeOgedei(nome, tipo));
        assertEquals(antes - 1, partida.consultarTesourosCidade(nome).size());
        assertEquals(1, partida.consultarQuantidadeTesourosJogadorAtual());
        assertFalse(partida.usarHabilidadeOgedei(nome, tipo));
    }

    @Test(timeout = 2000)
    public void altaniSelecionaUmDosDoisPeoesESomenteEleMove() {
        InterfacePartida partida = criar(Personagem.ALTANI);
        MapaDados mapa = new MapaDados();
        mapa.adicionar(2, 0.38, 0.38, false);
        assertTrue(mapa.conectar(0, 2));
        assertTrue(partida.carregarMapa(mapa));
        assertEquals(0, partida.consultarParadaPeaoPrincipal(0));
        assertEquals(0, partida.consultarParadaSegundoPeaoAltani(0));
        assertTrue(partida.escolherPeaoAtivoAltani(true));
        assertFalse(partida.escolherPeaoAtivoAltani(false));
        assertTrue(partida.movimentarJogador(0, 2));
        assertEquals(0, partida.consultarParadaPeaoPrincipal(0));
        assertEquals(2, partida.consultarParadaSegundoPeaoAltani(0));
    }

    @Test(timeout = 2000)
    public void toluiConstroiEmParadaAdjacenteSemVisita() {
        InterfacePartida partida = criar(Personagem.TOLUI);
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.38, 0.38, false);
        mapa.adicionar(2, 0.52, 0.45, false);
        assertTrue(mapa.conectar(0, 1));
        assertTrue(mapa.conectar(1, 2));
        assertTrue(partida.carregarMapa(mapa));
        assertTrue(partida.movimentarJogador(0, 1));
        assertTrue(partida.desenvolvimentoAjustarTributo(0, "YURT", 1));
        assertTrue(partida.construirYurtAtual(2));
        assertEquals(1, partida.consultarQuantidadeYurtsNaParada(2));
        assertEquals(1, partida.consultarQuantidadeYurtsNaParada(0));
    }

    @Test(timeout = 2000)
    public void conselheiroDaParada33ConsomeOsTresTesourosEscolhidos() {
        InterfacePartida partida = criar(Personagem.CHAGATAI);
        MapaDados mapa = new MapaDados();
        mapa.adicionar(33, 0.4, 0.4, true);
        assertTrue(mapa.conectar(0, 33));
        assertTrue(partida.carregarMapa(mapa));
        assertTrue(partida.movimentarJogador(0, 33));
        assertTrue(partida.desenvolvimentoAjustarItemEspecial(0, "TESOURO_FERRO", 2));
        assertTrue(partida.desenvolvimentoAjustarItemEspecial(0, "TESOURO_GADO", 2));
        int votosAntes = partida.consultarVotosJogadorAtual();
        int votosKhanAntes = partida.consultarPontuacaoKhan();
        assertFalse(partida.entregarTesourosConselheiro(33));
        assertFalse(partida.entregarTesourosConselheiro(33,
                Arrays.asList(TipoTesouro.FERRO, TipoTesouro.FERRO, TipoTesouro.FERRO)));
        assertEquals(votosAntes, partida.consultarVotosJogadorAtual());
        assertTrue(partida.entregarTesourosConselheiro(33,
                Arrays.asList(TipoTesouro.GADO, TipoTesouro.FERRO, TipoTesouro.GADO)));
        assertEquals(votosAntes + 1, partida.consultarVotosJogadorAtual());
        assertEquals(votosKhanAntes + 1, partida.consultarPontuacaoKhan());
        Map<TipoTesouro, Integer> estoque = partida.consultarTesourosPorTipoJogadorAtual();
        assertEquals(1, estoque.get(TipoTesouro.FERRO).intValue());
        assertEquals(0, estoque.get(TipoTesouro.GADO).intValue());
    }

    @Test(timeout = 3000)
    public void savePreservaOpcoesDeJochiAntesDaEscolha() throws Exception {
        InterfacePartida partida = criar(Personagem.JOCHI);
        List<ResumoMelhoria> antes = partida.consultarOpcoesIniciaisJochi();
        Path arquivo = Files.createTempFile("khan-jochi-", ".txt");
        try {
            GerenciadorSave.salvar(arquivo, partida);
            InterfacePartida recuperada = GerenciadorSave.carregar(arquivo);
            List<ResumoMelhoria> depois = recuperada.consultarOpcoesIniciaisJochi();
            assertEquals(2, depois.size());
            assertEquals(antes.get(0).getCodigo(), depois.get(0).getCodigo());
            assertTrue(recuperada.escolherMelhoriaInicialJochi(0));
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }
}
