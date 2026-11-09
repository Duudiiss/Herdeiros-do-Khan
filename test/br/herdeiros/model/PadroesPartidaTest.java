package br.herdeiros.model;

import static org.junit.Assert.*;

import org.junit.Test;
import br.herdeiros.mapa.MapaDados;

/** Verifica os contratos Singleton, Facade e Observer da terceira iteracao. */
public class PadroesPartidaTest {

    private static class ObservadorDeTeste implements ObservadorPartida {

        private int notificacoes;
        private int ultimaParada;

        @Override
        public void atualizar(ObservadoPartida observado) {
            notificacoes++;
            ultimaParada = observado.getParadaJogador(0);
        }
    }

    /** Objetivo: garantir uma unica instancia do gerenciador.
     * Condicao inicial: duas consultas estaticas sem criacao manual.
     * Resultado esperado: as duas referencias apontam para o mesmo objeto. */
    @Test(timeout = 2000)
    public void testeGerenciadorSingletonRetornaMesmaInstancia() {
        GerenciadorPartida primeiro = GerenciadorPartida.getInstancia();
        GerenciadorPartida segundo = GerenciadorPartida.getInstancia();
        assertSame("O gerenciador da partida deve ser unico", primeiro, segundo);
    }

    /** Objetivo: iniciar uma nova partida sem criar um novo Singleton.
     * Condicao inicial: a instancia do gerenciador ja existe.
     * Resultado esperado: partida ativa e substituida e o gerenciador e preservado. */
    @Test(timeout = 2000)
    public void testeNovaPartidaSubstituiPartidaAtiva() {
        GerenciadorPartida gerenciador = GerenciadorPartida.getInstancia();
        InterfacePartida antiga = gerenciador.iniciarPartida(
                new String[] {"Ana", "Bia"}, new Cor[] {Cor.AZUL, Cor.VERDE});
        InterfacePartida nova = gerenciador.iniciarPartida(
                new String[] {"Caio", "Duda"}, new Cor[] {Cor.PRETO, Cor.VERMELHO});
        assertNotSame("Uma nova partida deve substituir a anterior", antiga, nova);
        assertSame("O gerenciador deve conter a partida mais recente", nova,
                gerenciador.getPartidaAtual());
    }

    /** Objetivo: confirmar atualizacao automatica dos observadores apos um movimento valido.
     * Condicao inicial: partida com destino adjacente livre e observador registrado.
     * Resultado esperado: notificacao e leitura do estado atualizado. */
    @Test(timeout = 2000)
    public void testeObservadorRecebeMovimentacaoValida() {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.25, 0.25, false);
        mapa.conectar(0, 1);
        assertTrue("O mapa precisa estar carregado", partida.carregarMapa(mapa));

        ObservadorDeTeste observador = new ObservadorDeTeste();
        partida.registrarObservador(observador);
        assertTrue("O movimento precisa ser valido", partida.movimentarJogadorAtual(1, 1));
        assertEquals("O observador deve receber exatamente uma notificacao", 1,
                observador.notificacoes);
        assertEquals("O observador deve consultar a posicao atualizada", 1,
                observador.ultimaParada);
    }

    /** Objetivo: impedir notificacoes quando uma jogada nao modifica o estado.
     * Condicao inicial: nao existe caminho ate o destino.
     * Resultado esperado: o observador nao recebe notificacao. */
    @Test(timeout = 2000)
    public void testeMovimentoInvalidoNaoNotificaObservador() {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.25, 0.25, false);
        assertTrue("O mapa deve carregar sem conexoes", partida.carregarMapa(mapa));
        ObservadorDeTeste observador = new ObservadorDeTeste();
        partida.registrarObservador(observador);
        assertFalse("Movimento impossivel nao pode ser aceito", partida.movimentarJogadorAtual(1, 1));
        assertEquals("Nao houve mudanca: nao deve haver notificacao", 0,
                observador.notificacoes);
        assertEquals("A vez nao muda com a tentativa invalida", 0,
                partida.getIndiceJogadorAtual());
    }

    /** Objetivo: confirmar que observadores podem ser removidos.
     * Condicao inicial: observador registrado e depois removido.
     * Resultado esperado: nenhuma notificacao na passagem de vez. */
    @Test(timeout = 2000)
    public void testeObservadorRemovidoNaoRecebeEventos() {
        InterfacePartida partida = new InterfacePartida();
        ObservadorDeTeste observador = new ObservadorDeTeste();
        partida.registrarObservador(observador);
        partida.removerObservador(observador);
        partida.avancarJogador();
        assertEquals("Observador removido nao pode receber eventos", 0,
                observador.notificacoes);
    }

    /** Objetivo: impedir observadores duplicados na mesma partida.
     * Condicao inicial: mesmo observador registrado duas vezes.
     * Resultado esperado: somente uma notificacao para cada mudanca. */
    @Test(timeout = 2000)
    public void testeRegistroDuplicadoNaoDuplicaNotificacao() {
        InterfacePartida partida = new InterfacePartida();
        ObservadorDeTeste observador = new ObservadorDeTeste();
        partida.registrarObservador(observador);
        partida.registrarObservador(observador);
        partida.avancarJogador();
        assertEquals("Registro repetido nao deve duplicar notificacoes", 1,
                observador.notificacoes);
    }
}
