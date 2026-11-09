package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;

/** Testes da consulta das cartelas e das notificacoes Observer via Facade. */
public class CartelaObserverTest {

    private static class ObservadorContador implements ObservadorPartida {
        private int notificacoes;
        private int espadasObservadas;

        @Override
        public void atualizar(ObservadoPartida observado) {
            notificacoes++;
            espadasObservadas = ((InterfacePartida) observado)
                    .consultarCartelaJogadorAtual().getEspadas();
        }
    }

    /** Objetivo: ler uma cartela sem modificar o Model.
     * Condicao inicial: partida com dois jogadores recem-criada.
     * Resultado esperado: nome, recursos e colunas com valores iniciais coerentes. */
    @Test(timeout = 2000)
    public void testeCartelaInicialRepresentaJogadorAtual() {
        InterfacePartida partida = new InterfacePartida();
        ResumoCartela cartela = partida.consultarCartelaJogadorAtual();
        assertEquals("A cartela deve pertencer ao jogador atual", "Azul",
                cartela.getNomeJogador());
        assertEquals("As espadas devem iniciar zeradas", 0, cartela.getEspadas());
        assertEquals("As quatro colunas devem aparecer na cartela", 4,
                cartela.getQuantidadeColunas());
    }

    /** Objetivo: verificar troca de cartela por notificacao de passagem de turno.
     * Condicao inicial: dois jogadores distintos iniciam a partida.
     * Resultado esperado: a consulta ao jogador da vez reflete a troca. */
    @Test(timeout = 2000)
    public void testeCartelaAtualMudaComJogadorDaVez() {
        InterfacePartida partida = new InterfacePartida();
        partida.avancarJogador();
        assertEquals("Depois de passar a vez, deve aparecer o outro jogador",
                "Vermelho", partida.consultarCartelaJogadorAtual().getNomeJogador());
    }

    /** Objetivo: proteger a colecao de melhorias disponibilizada a View.
     * Condicao inicial: um resumo de cartela foi consultado.
     * Resultado esperado: a lista do resumo nao aceita modificacoes. */
    @Test(timeout = 2000)
    public void testeResumoNaoPermiteAlterarMelhorias() {
        InterfacePartida partida = new InterfacePartida();
        ResumoCartela cartela = partida.consultarCartelaJogadorAtual();
        boolean bloqueou = false;
        try {
            cartela.getMelhorias().add("Alteracao externa");
        } catch (UnsupportedOperationException erro) {
            bloqueou = true;
        }
        assertTrue("A View nao pode modificar as melhorias pelo resumo", bloqueou);
    }

    /** Objetivo: observar mudanca real de tributos pela Facade.
     * Condicao inicial: observador registrado, espadas inicialmente zeradas.
     * Resultado esperado: o observador ve o saldo atualizado na notificacao. */
    @Test(timeout = 2000)
    public void testeRecebimentoDeTributosNotificaObservador() {
        InterfacePartida partida = new InterfacePartida();
        ObservadorContador observador = new ObservadorContador();
        partida.registrarObservador(observador);
        assertTrue("Recebimento de teste deve ser aceito",
                partida.simularRecebimentoTributo(0, "ESPADA", 2));
        assertEquals("A alteracao deve notificar somente uma vez", 1,
                observador.notificacoes);
        assertEquals("O observador deve consultar o novo saldo", 2,
                observador.espadasObservadas);
    }

    /** Objetivo: impedir que entradas invalidas alterem a partida.
     * Condicao inicial: um observador registrado, sem eventos anteriores.
     * Resultado esperado: nao ha alteracao de recursos nem notificacao. */
    @Test(timeout = 2000)
    public void testeTributoInvalidoNaoNotificaObservador() {
        InterfacePartida partida = new InterfacePartida();
        ObservadorContador observador = new ObservadorContador();
        partida.registrarObservador(observador);
        assertFalse("Tipo inexistente deve ser rejeitado",
                partida.simularRecebimentoTributo(0, "DESCONHECIDO", 1));
        assertEquals("Nenhuma modificacao deve ser notificada", 0,
                observador.notificacoes);
        assertEquals("O saldo deve continuar igual ao inicial", 0,
                partida.consultarCartela(0).getEspadas());
    }

    /** Objetivo: impedir consulta a jogador inexistente.
     * Condicao inicial: partida com dois jogadores.
     * Resultado esperado: a Facade rejeita indices invalidos. */
    @Test(timeout = 2000)
    public void testeIndiceDeCartelaInvalido() {
        InterfacePartida partida = new InterfacePartida();
        boolean rejeitou = false;
        try {
            partida.consultarCartela(8);
        } catch (IllegalArgumentException erro) {
            rejeitou = true;
        }
        assertTrue("Indice fora da partida precisa ser rejeitado", rejeitou);
    }
}
