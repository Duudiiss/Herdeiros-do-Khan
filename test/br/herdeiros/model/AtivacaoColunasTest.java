package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;
import br.herdeiros.mapa.MapaDados;

/** Testes do registro das pecas de ativacao e uso de cavalos do manual. */
public class AtivacaoColunasTest {

    private InterfacePartida criarPartidaComMapa() {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Altani", "Jochi"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.ALTANI, Personagem.JOCHI}, false);
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.15, 0.15, false);
        mapa.adicionar(2, 0.20, 0.20, false);
        mapa.adicionar(3, 0.30, 0.30, false);
        mapa.conectar(0, 1);
        mapa.conectar(1, 2);
        mapa.conectar(2, 3);
        assertTrue("Mapa de teste deve carregar", partida.carregarMapa(mapa));
        return partida;
    }

    /** Objetivo: verificar que antes de ativar uma coluna nao existem movimentos.
     * Condicao inicial: primeiro turno de uma partida valida.
     * Resultado esperado: tentativa de mover e recusada sem alterar o estado. */
    @Test(timeout = 2000)
    public void testeMovimentoAntesDaAtivacaoERecusado() {
        InterfacePartida partida = criarPartidaComMapa();
        assertFalse("Mover sem ativar uma coluna deve falhar",
                partida.movimentarJogadorComAtivacao(1));
        assertEquals("O peao permanece em Karakorum", 0,
                partida.getParadaJogador(0));
    }

    /** Objetivo: conferir os cavalos presentes na cartela de Altani.
     * Condicao inicial: jogador Altani sem coluna ativada.
     * Resultado esperado: a coluna verde libera dois movimentos. */
    @Test(timeout = 2000)
    public void testeAltaniVerdeLiberaDoisCavalos() {
        InterfacePartida partida = criarPartidaComMapa();
        assertTrue("Coluna verde de Altani deve ser ativada",
                partida.ativarColunaJogadorAtual(1));
        assertEquals("A coluna verde possui dois cavalos", 2,
                partida.consultarAtivacaoJogadorAtual().getMovimentosRestantes());
    }

    /** Objetivo: consumir a distancia real de um deslocamento.
     * Condicao inicial: coluna verde ativada, destino a dois passos.
     * Resultado esperado: movimento aceito e saldo reduzido a zero. */
    @Test(timeout = 2000)
    public void testeMovimentacaoConsomeDoisCavalos() {
        InterfacePartida partida = criarPartidaComMapa();
        partida.ativarColunaJogadorAtual(1);
        assertTrue("Dois passos devem ser permitidos", partida.movimentarJogadorComAtivacao(2));
        assertEquals("O saldo deve chegar a zero", 0,
                partida.consultarAtivacaoJogadorAtual().getMovimentosRestantes());
        assertEquals("O peao termina no destino", 2, partida.getParadaJogador(0));
    }

    /** Objetivo: proteger o saldo de cavalos em operacoes invalidas.
     * Condicao inicial: coluna com um cavalo, destino a dois passos.
     * Resultado esperado: posicao e saldo preservados. */
    @Test(timeout = 2000)
    public void testeMovimentoInvalidoNaoDescontaCavalos() {
        InterfacePartida partida = criarPartidaComMapa();
        partida.ativarColunaJogadorAtual(0);
        assertFalse("Dois passos exigem mais cavalos que a coluna vermelha",
                partida.movimentarJogadorComAtivacao(2));
        assertEquals("O movimento recusado nao consome cavalos", 1,
                partida.consultarAtivacaoJogadorAtual().getMovimentosRestantes());
        assertEquals("A posicao permanece inalterada", 0, partida.getParadaJogador(0));
    }

    /** Objetivo: impedir ativacao repetida no mesmo turno.
     * Condicao inicial: jogador ja ativou a coluna vermelha.
     * Resultado esperado: ativar outra coluna e recusado sem mudar o saldo. */
    @Test(timeout = 2000)
    public void testeUmaAtivacaoPorTurno() {
        InterfacePartida partida = criarPartidaComMapa();
        partida.ativarColunaJogadorAtual(0);
        assertFalse("Nao pode ativar a coluna verde no mesmo turno",
                partida.ativarColunaJogadorAtual(1));
        assertEquals("Movimentos devem permanecer inalterados", 1,
                partida.consultarAtivacaoJogadorAtual().getMovimentosRestantes());
    }

    /** Objetivo: impedir reutilizar coluna ocupada apos passar o turno.
     * Condicao inicial: Altani usou vermelha, Jochi jogou e a vez voltou.
     * Resultado esperado: a coluna vermelha continua indisponivel. */
    @Test(timeout = 2000)
    public void testeColunaOcupadaPermaneceIndisponivelNaProximaRodada() {
        InterfacePartida partida = criarPartidaComMapa();
        partida.ativarColunaJogadorAtual(0);
        partida.avancarJogador();
        partida.avancarJogador();
        assertFalse("A coluna vermelha ainda tem uma peca de ativacao",
                partida.ativarColunaJogadorAtual(0));
        assertTrue("A coluna verde continua livre", partida.ativarColunaJogadorAtual(1));
    }

    /** Objetivo: recuperar pecas de ativacao usando a coluna branca.
     * Condicao inicial: Altani utilizou a vermelha, depois a branca.
     * Resultado esperado: no proximo turno a vermelha pode ser ativada novamente. */
    @Test(timeout = 2000)
    public void testeBrancaRecuperaPecasParaProximoTurno() {
        InterfacePartida partida = criarPartidaComMapa();
        partida.ativarColunaJogadorAtual(0);
        partida.avancarJogador();
        partida.avancarJogador();
        assertTrue("A branca deve ser ativavel", partida.ativarColunaJogadorAtual(3));
        partida.avancarJogador();
        partida.avancarJogador();
        assertTrue("A vermelha deve estar disponivel apos recuperacao",
                partida.ativarColunaJogadorAtual(0));
    }

    /** Objetivo: impedir ativacao de colunas fora da cartela.
     * Condicao inicial: nenhuma coluna ativada.
     * Resultado esperado: indices invalidos sao recusados sem efeitos. */
    @Test(timeout = 2000)
    public void testeIndicesInvalidosNaoAtivam() {
        InterfacePartida partida = criarPartidaComMapa();
        assertFalse("Indice negativo deve ser recusado", partida.ativarColunaJogadorAtual(-1));
        assertFalse("Nao existe quinta coluna", partida.ativarColunaJogadorAtual(4));
        assertEquals("Nenhuma coluna fica ativa", -1,
                partida.consultarAtivacaoJogadorAtual().getColunaAtiva());
    }

    /** Objetivo: impedir exposicao das colecoes internas de ativacao.
     * Condicao inicial: coluna vermelha usada e resumo consultado.
     * Resultado esperado: consulta nao fornece acesso mutavel as flags. */
    @Test(timeout = 2000)
    public void testeResumoDeAtivacaoNaoExpoeEstadoInterno() {
        InterfacePartida partida = criarPartidaComMapa();
        partida.ativarColunaJogadorAtual(0);
        ResumoAtivacao resumo = partida.consultarAtivacaoJogadorAtual();
        partida.avancarJogador();
        partida.avancarJogador();
        assertTrue("O resumo registra a coluna ocupada", resumo.estaOcupada(0));
        assertFalse("A coluna original continua ocupada no Model",
                partida.consultarAtivacaoJogadorAtual().podeAtivar(0));
    }

    /** Objetivo: verificar diferencas de acoes entre personagens.
     * Condicao inicial: Jochi tem a coluna amarela livre.
     * Resultado esperado: amarela de Jochi concede dois cavalos. */
    @Test(timeout = 2000)
    public void testeJochiAmarelaPossuiDoisCavalos() {
        InterfacePartida partida = criarPartidaComMapa();
        partida.avancarJogador();
        assertTrue("Jochi pode ativar a coluna amarela", partida.ativarColunaJogadorAtual(2));
        assertEquals("Jochi amarela tem dois cavalos", 2,
                partida.consultarAtivacaoJogadorAtual().getMovimentosRestantes());
    }

    /** Objetivo: verificar notificacoes da interface apos ativacao.
     * Condicao inicial: observador registrado e nenhuma ativacao.
     * Resultado esperado: uma notificacao, nenhuma quando a ativacao falha. */
    @Test(timeout = 2000)
    public void testeAtivacaoNotificaObserverSomenteAoAlterarEstado() {
        InterfacePartida partida = criarPartidaComMapa();
        class Contador implements ObservadorPartida {
            int contagem;
            @Override
            public void atualizar(ObservadoPartida observado) {
                contagem++;
            }
        }
        Contador contador = new Contador();
        partida.registrarObservador(contador);
        assertTrue("A coluna pode ser ativada", partida.ativarColunaJogadorAtual(0));
        assertFalse("Nao pode ativar outra coluna no mesmo turno",
                partida.ativarColunaJogadorAtual(1));
        assertEquals("Somente a alteracao valida deve notificar", 1, contador.contagem);
    }

    /** Objetivo: garantir que a interface normal nao use mais o seletor antigo.
     * Condicao inicial: Controller em modo de ativacao real sem coluna escolhida.
     * Resultado esperado: configurar movimentos manuais nao libera deslocamentos. */
    @Test(timeout = 2000)
    public void testeControllerIgnoraSimulacaoEmModoReal() {
        InterfacePartida partida = criarPartidaComMapa();
        br.herdeiros.controller.ControladorTabuleiro controller =
                new br.herdeiros.controller.ControladorTabuleiro(partida, new MapaDados());
        controller.habilitarAtivacaoReal();
        controller.configurarMovimentos(6);
        assertFalse("A simulacao nao pode contornar a ativacao",
                controller.tentarMoverPara(1));
        assertEquals("Sem ativacao, o peao continua na origem", 0,
                partida.getParadaJogador(0));
    }

    /** Objetivo: verificar a integracao da ativacao com o Controller.
     * Condicao inicial: Altani ativa verde e tenta percorrer duas conexoes.
     * Resultado esperado: deslocamento aceito, saldo consumido, vez preservada. */
    @Test(timeout = 2000)
    public void testeControllerUsaSaldoRealDoModel() {
        InterfacePartida partida = criarPartidaComMapa();
        br.herdeiros.controller.ControladorTabuleiro controller =
                new br.herdeiros.controller.ControladorTabuleiro(partida, new MapaDados());
        controller.habilitarAtivacaoReal();
        assertTrue("Ativacao deve liberar dois movimentos",
                partida.ativarColunaJogadorAtual(1));
        assertTrue("Controller deve aceitar trajeto de dois passos",
                controller.tentarMoverPara(2));
        assertEquals("O saldo oficial no Model chega a zero", 0,
                partida.consultarAtivacaoJogadorAtual().getMovimentosRestantes());
        assertEquals("So o botao Passar turno muda a vez", 0,
                partida.getIndiceJogadorAtual());
    }
}
