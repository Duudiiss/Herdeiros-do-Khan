package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;
import br.herdeiros.mapa.MapaDados;

/** Verifica a coleta por punhos e a reposição de tributos do Khan. */
public class ColetaTributosKhanTest {

    private InterfacePartida partida() {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Altani", "Jochi"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.ALTANI, Personagem.JOCHI}, false);
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.2, 0.2, false);
        mapa.adicionar(2, 0.4, 0.4, false);
        mapa.conectar(0, 1);
        mapa.conectar(1, 2);
        assertTrue("O mapa mínimo deve carregar", partida.carregarMapa(mapa));
        assertTrue("A província deve ser configurada", partida.configurarProvincia(Provincia.PERSIA_SUL, "ESPADA", 2));
        assertTrue("A parada 1 deve dar acesso à província", partida.configurarAcessoProvincia(Provincia.PERSIA_SUL, 1));
        assertTrue("A segunda província deve ser configurada", partida.configurarProvincia(Provincia.RUSSIA_OESTE, "MOEDA", 1));
        assertTrue("A terceira província deve ser configurada", partida.configurarProvincia(Provincia.RUSSIA_LESTE, "YURT", 3));
        assertTrue("A quarta província deve ser configurada", partida.configurarProvincia(Provincia.CHINA_LESTE, "ESPADA", 2));
        return partida;
    }

    /** Objetivo: impedir coletar sem coluna. Condição inicial: nenhuma ativação.
     * Resultado esperado: estado e estoque preservados. */
    @Test(timeout = 2000)
    public void testeColetaSemAtivacaoFalha() {
        InterfacePartida partida = partida();
        assertFalse("Sem ativação, não pode coletar", partida.coletarTributo(Provincia.PERSIA_SUL));
        assertEquals("Estoque permanece igual", 2, partida.consultarEstoqueProvincia(Provincia.PERSIA_SUL));
    }

    /** Objetivo: exigir parada visitada. Condição inicial: Altani em Karakorum.
     * Resultado esperado: coleta recusada mesmo com punhos. */
    @Test(timeout = 2000)
    public void testeColetaDistanteFalha() {
        InterfacePartida partida = partida();
        assertTrue("Ativação aceita", partida.ativarColunaJogadorAtual(0));
        assertFalse("Ainda não visitou a parada 1", partida.coletarTributo(Provincia.PERSIA_SUL));
    }

    /** Objetivo: usar um punho para coletar espada após visitar a província.
     * Resultado esperado: saldo +1, estoque -1 e punhos -1. */
    @Test(timeout = 2000)
    public void testeColetaEspadaReal() {
        InterfacePartida partida = partida();
        assertTrue("Ativação aceita", partida.ativarColunaJogadorAtual(0));
        assertTrue("Movimento até acesso permitido", partida.movimentarJogadorComAtivacao(1));
        int estoque = partida.consultarEstoqueProvincia(Provincia.PERSIA_SUL);
        int espadas = partida.consultarCartelaJogadorAtual().getEspadas();
        int punhos = partida.consultarAtivacaoJogadorAtual().getPunhosDaColuna();
        assertTrue("Coleta deve funcionar", partida.coletarTributo(Provincia.PERSIA_SUL));
        assertEquals("Uma espada foi recebida", espadas + 1, partida.consultarCartelaJogadorAtual().getEspadas());
        assertEquals("Uma peça foi retirada da província", estoque - 1, partida.consultarEstoqueProvincia(Provincia.PERSIA_SUL));
        assertEquals("Um punho foi gasto", punhos - 1, partida.consultarAtivacaoJogadorAtual().getPunhosDaColuna());
    }

    /** Objetivo: não permitir gastar mais punhos que a coluna oferece.
     * Resultado esperado: terceira coleta sem punhos é recusada. */
    @Test(timeout = 2000)
    public void testeNaoColetarSemPunhosRestantes() {
        InterfacePartida partida = partida();
        partida.ativarColunaJogadorAtual(0);
        partida.movimentarJogadorComAtivacao(1);
        assertTrue("Primeira coleta", partida.coletarTributo(Provincia.PERSIA_SUL));
        assertTrue("Segunda coleta", partida.coletarTributo(Provincia.PERSIA_SUL));
        assertFalse("Não existe terceiro punho", partida.coletarTributo(Provincia.PERSIA_SUL));
    }

    /** Objetivo: validar estoque finito.
     * Resultado esperado: terceira coleta não cria tributo inexistente. */
    @Test(timeout = 2000)
    public void testeEstoqueVazioNaoCriaTributos() {
        InterfacePartida partida = partida();
        partida.ativarColunaJogadorAtual(0);
        partida.movimentarJogadorComAtivacao(1);
        partida.coletarTributo(Provincia.PERSIA_SUL);
        partida.coletarTributo(Provincia.PERSIA_SUL);
        assertEquals("Estoque chegou a zero", 0, partida.consultarEstoqueProvincia(Provincia.PERSIA_SUL));
        assertFalse("Estoque vazio não fornece peças", partida.coletarTributo(Provincia.PERSIA_SUL));
    }

    /** Objetivo: movimentação do Khan repõe até três províncias.
     * Resultado esperado: escolhida e duas vizinhas aumentam, respeitando máximo 3. */
    @Test(timeout = 2000)
    public void testeKhanReabasteceProvinciaEVizinhas() {
        InterfacePartida partida = partida();
        partida.ativarColunaJogadorAtual(0);
        assertTrue("Khan é movido", partida.realizarAcaoKhan(DestinoKhan.PERSIA_SUL));
        assertEquals("Província escolhida recebe uma peça", 3, partida.consultarEstoqueProvincia(Provincia.PERSIA_SUL));
        assertEquals("Primeira vizinha recebe uma peça", 2, partida.consultarEstoqueProvincia(Provincia.RUSSIA_OESTE));
        assertEquals("Província cheia mantém o máximo", 3, partida.consultarEstoqueProvincia(Provincia.RUSSIA_LESTE));
    }

    /** Objetivo: impedir alterar reserva além do teto de três unidades. */
    @Test(timeout = 2000)
    public void testeReservaNaoAceitaMaisDeTres() {
        InterfacePartida partida = partida();
        assertFalse("Quantidade quatro deve ser recusada", partida.configurarProvincia(Provincia.PERSIA_SUL, "ESPADA", 4));
        assertEquals("Reserva previamente configurada não muda", 2, partida.consultarEstoqueProvincia(Provincia.PERSIA_SUL));
    }

    /** Objetivo: testar cinco e somente cinco destinos permitidos ao Khan. */
    @Test(timeout = 2000)
    public void testeExistemCincoDestinosDoKhan() {
        assertEquals("Quatro províncias e a área de melhorias", 5, DestinoKhan.values().length);
        assertEquals("São quatro províncias", 4, Provincia.values().length);
    }

    /** Objetivo: impedir coleta de um lugar sem configuração de acesso. */
    @Test(timeout = 2000)
    public void testeColetaNaoAutorizadaEmProvinciaNaoVisitada() {
        InterfacePartida partida = partida();
        partida.ativarColunaJogadorAtual(0);
        partida.movimentarJogadorComAtivacao(1);
        assertFalse("A província russa não foi acessada", partida.coletarTributo(Provincia.RUSSIA_OESTE));
    }

    /** Objetivo: o Observer informa alterações reais, não operações recusadas. */
    @Test(timeout = 2000)
    public void testeObserverDaColeta() {
        InterfacePartida partida = partida();
        class Contador implements ObservadorPartida {
            int total;
            @Override
            public void atualizar(ObservadoPartida observado) {
                total++;
            }
        }
        Contador observador = new Contador();
        partida.registrarObservador(observador);
        assertFalse("Coleta recusada", partida.coletarTributo(Provincia.PERSIA_SUL));
        assertEquals("Sem notificação para recusa", 0, observador.total);
        partida.ativarColunaJogadorAtual(0);
        partida.movimentarJogadorComAtivacao(1);
        int antes = observador.total;
        assertTrue("Coleta aceita", partida.coletarTributo(Provincia.PERSIA_SUL));
        assertEquals("Notificação após coleta", antes + 1, observador.total);
    }

    /**
     * Objetivo: impedir coletar uma peça que nao existe na provincia.
     * Condicao inicial: a provincia possui espadas e o jogador tem punhos.
     * Resultado esperado: saldo, estoque e punhos permanecem inalterados.
     */
    @Test(timeout = 2000)
    public void testeEscolherTributoInexistenteNaoGastaPunho() {
        InterfacePartida partida = partida();
        assertTrue("Ativacao aceita", partida.ativarColunaJogadorAtual(0));
        assertTrue("Acesso a provincia", partida.movimentarJogadorComAtivacao(1));
        int estoqueAnterior = partida.consultarEstoqueProvincia(Provincia.PERSIA_SUL);
        int punhosAnteriores = partida.consultarAtivacaoJogadorAtual().getPunhosDaColuna();
        int espadasAnteriores = partida.consultarCartelaJogadorAtual().getEspadas();

        assertFalse("A provincia nao tem moedas", partida.coletarTributo(Provincia.PERSIA_SUL, "MOEDA"));
        assertEquals("O estoque nao deve mudar", estoqueAnterior,
                partida.consultarEstoqueProvincia(Provincia.PERSIA_SUL));
        assertEquals("Nenhum punho deve ser gasto", punhosAnteriores,
                partida.consultarAtivacaoJogadorAtual().getPunhosDaColuna());
        assertEquals("Nenhuma espada deve ser recebida", espadasAnteriores,
                partida.consultarCartelaJogadorAtual().getEspadas());
    }

    /**
     * Objetivo: permitir a coleta do tipo escolhido pelo jogador.
     * Condicao inicial: jogador acessou provincia com espadas e tem punhos.
     * Resultado esperado: ganha uma espada e consome um punho e uma peca do estoque.
     */
    @Test(timeout = 2000)
    public void testeColetaSelecionandoTipoDisponivel() {
        InterfacePartida partida = partida();
        partida.ativarColunaJogadorAtual(0);
        partida.movimentarJogadorComAtivacao(1);
        int espadasAnteriores = partida.consultarCartelaJogadorAtual().getEspadas();
        int punhosAnteriores = partida.consultarAtivacaoJogadorAtual().getPunhosDaColuna();

        assertTrue("A coleta de espada deve ser permitida",
                partida.coletarTributo(Provincia.PERSIA_SUL, "ESPADA"));
        assertEquals("Uma espada adicional", espadasAnteriores + 1,
                partida.consultarCartelaJogadorAtual().getEspadas());
        assertEquals("Um punho utilizado", punhosAnteriores - 1,
                partida.consultarAtivacaoJogadorAtual().getPunhosDaColuna());
    }

    /**
     * Objetivo: expor o estoque individual sem modificar as reservas.
     * Condicao inicial: duas espadas na provincia persa.
     * Resultado esperado: duas espadas, zero moedas e zero yurts.
     */
    @Test(timeout = 2000)
    public void testeEstoqueSeparadoPorTipoDeTributo() {
        InterfacePartida partida = partida();
        assertEquals("Quantidade de espadas", 2,
                partida.consultarQuantidadeTributoProvincia(Provincia.PERSIA_SUL, "ESPADA"));
        assertEquals("A provincia nao possui moedas", 0,
                partida.consultarQuantidadeTributoProvincia(Provincia.PERSIA_SUL, "MOEDA"));
        assertEquals("A provincia nao possui yurts", 0,
                partida.consultarQuantidadeTributoProvincia(Provincia.PERSIA_SUL, "YURT"));
    }

    /**
     * Objetivo: rejeitar nomes invalidos sem provocar excecao ou mutacao.
     * Condicao inicial: reserva configurada com duas espadas.
     * Resultado esperado: as consultas e coletas com tipo invalido nao tem efeito.
     */
    @Test(timeout = 2000)
    public void testeTipoInvalidoNaoAlteraProvincia() {
        InterfacePartida partida = partida();
        assertEquals("Consulta de tipo desconhecido resulta em zero", 0,
                partida.consultarQuantidadeTributoProvincia(Provincia.PERSIA_SUL, "DESCONHECIDO"));
        assertFalse("Coleta com tipo invalido deve ser recusada",
                partida.coletarTributo(Provincia.PERSIA_SUL, "DESCONHECIDO"));
        assertEquals("A quantidade de tributos deve ser preservada", 2,
                partida.consultarEstoqueProvincia(Provincia.PERSIA_SUL));
    }
}
