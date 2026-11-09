package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;
import br.herdeiros.mapa.MapaDados;

/** Verifica saque, historico de deslocamentos e acao obrigatoria do Khan. */
public class SaquesKhanTest {

    private InterfacePartida prepararPartida() {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Ana", "Bruno"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.ALTANI, Personagem.JOCHI}, false);
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.12, 0.17, false);
        mapa.adicionar(2, 0.20, 0.17, false);
        mapa.adicionar(3, 0.30, 0.17, false);
        mapa.conectar(0, 1);
        mapa.conectar(1, 2);
        mapa.conectar(2, 3);
        assertTrue("O mapa de teste deve carregar", partida.carregarMapa(mapa));
        return partida;
    }

    private String primeiraCidade(InterfacePartida partida) {
        return partida.consultarCidadesDisponiveis().get(0).getNome();
    }

    private void darEspadas(InterfacePartida partida, int quantidade) {
        assertTrue("Tributos de preparacao devem ser inseridos", partida.simularRecebimentoTributo(
                partida.getIndiceJogadorAtual(), "ESPADA", quantidade));
    }

    private void chegarNaParada(InterfacePartida partida, int destino, int coluna) {
        assertTrue("A coluna escolhida deve poder ser ativada", partida.ativarColunaJogadorAtual(coluna));
        assertTrue("O movimento permitido deve ser aceito", partida.movimentarJogadorComAtivacao(destino));
    }

    /** Sem visita, o saque nao pode consumir recursos nem retirar tesouros. */
    @Test(timeout = 2000)
    public void testeNaoSaqueiaCidadeDistante() {
        InterfacePartida partida = prepararPartida();
        String cidade = primeiraCidade(partida);
        partida.configurarAcessoCidade(cidade, 1);
        darEspadas(partida, 3);
        int tesouros = partida.consultarCidadesDisponiveis().get(0).getTesourosRestantes();
        assertFalse("Saque sem visita ao acesso da cidade deve falhar", partida.saquearCidadeAtual(cidade));
        assertEquals("Falha nao consome espadas", 3, partida.consultarCartelaJogadorAtual().getEspadas());
        assertEquals("Falha nao altera tesouros", tesouros,
                partida.consultarCidadesDisponiveis().get(0).getTesourosRestantes());
    }

    /** Primeiro saque de uma cidade custa exatamente uma espada. */
    @Test(timeout = 2000)
    public void testePrimeiroSaqueCustaUmaEspada() {
        InterfacePartida partida = prepararPartida();
        String cidade = primeiraCidade(partida);
        partida.configurarAcessoCidade(cidade, 1);
        darEspadas(partida, 3);
        chegarNaParada(partida, 1, 1);
        assertTrue("Primeiro saque deve ser permitido", partida.saquearCidadeAtual(cidade));
        assertEquals("Uma espada deve ser consumida", 2,
                partida.consultarCartelaJogadorAtual().getEspadas());
    }

    /** A mesma cidade custa duas espadas a partir do segundo saque no turno. */
    @Test(timeout = 2000)
    public void testeSegundoSaqueCustaDuasEspadas() {
        InterfacePartida partida = prepararPartida();
        String cidade = primeiraCidade(partida);
        partida.configurarAcessoCidade(cidade, 1);
        darEspadas(partida, 3);
        chegarNaParada(partida, 1, 1);
        assertTrue("Primeiro saque deve ocorrer", partida.saquearCidadeAtual(cidade));
        assertTrue("Segundo saque deve ocorrer", partida.saquearCidadeAtual(cidade));
        assertEquals("Os dois saques gastam tres espadas", 0,
                partida.consultarCartelaJogadorAtual().getEspadas());
    }

    /** Falta de espadas nao permite retirar um tesouro adicional. */
    @Test(timeout = 2000)
    public void testeSaqueInvalidoConservaRecursos() {
        InterfacePartida partida = prepararPartida();
        String cidade = primeiraCidade(partida);
        partida.configurarAcessoCidade(cidade, 1);
        darEspadas(partida, 1);
        chegarNaParada(partida, 1, 1);
        assertTrue("Primeiro saque deve funcionar", partida.saquearCidadeAtual(cidade));
        int tesouros = partida.consultarCidadesDisponiveis().get(0).getTesourosRestantes();
        assertFalse("Segundo saque sem espadas deve falhar", partida.saquearCidadeAtual(cidade));
        assertEquals("Tesouros nao mudam em saque rejeitado", tesouros,
                partida.consultarCidadesDisponiveis().get(0).getTesourosRestantes());
    }

    /** Os trechos intermediarios contam como paradas visitadas no turno. */
    @Test(timeout = 2000)
    public void testePercursoContabilizaParadaIntermediaria() {
        InterfacePartida partida = prepararPartida();
        String cidade = primeiraCidade(partida);
        partida.configurarAcessoCidade(cidade, 1);
        darEspadas(partida, 1);
        chegarNaParada(partida, 2, 1);
        assertTrue("Parada atravessada deve permitir saque", partida.saquearCidadeAtual(cidade));
    }

    /** A conquista retira carta antiga, revela outra e registra o conquistador. */
    @Test(timeout = 2000)
    public void testeConquistaRevelaCidadeSeguinte() {
        InterfacePartida partida = prepararPartida();
        String cidade = primeiraCidade(partida);
        partida.configurarAcessoCidade(cidade, 1);
        darEspadas(partida, 7);
        chegarNaParada(partida, 1, 1);
        for (int i = 0; i < 4; i++) {
            assertTrue("Todos os quatro saques validos devem acontecer", partida.saquearCidadeAtual(cidade));
        }
        assertEquals("A carta conquistada deve ser atribuida ao jogador", Cor.AZUL,
                partida.consultarConquistadorCidade(cidade));
        assertEquals("A cidade conquistada deve dar lugar a outra", 3,
                partida.consultarCidadesDisponiveis().size());
        for (ResumoCidade resumo : partida.consultarCidadesDisponiveis()) {
            assertFalse("Cidade conquistada nao pode permanecer aberta", resumo.getNome().equals(cidade));
        }
    }

    /** Cidades inexistentes nao podem receber acessos nem ser saqueadas. */
    @Test(timeout = 2000)
    public void testeSaqueRejeitaCidadeDesconhecida() {
        InterfacePartida partida = prepararPartida();
        assertFalse("Cidade inexistente nao recebe acesso", partida.configurarAcessoCidade("INEXISTENTE", 1));
        assertFalse("Cidade inexistente nao pode ser saqueada", partida.saquearCidadeAtual("INEXISTENTE"));
    }

    /** Ativar coluna com Khan impede passar turno ate realizar a acao. */
    @Test(timeout = 2000)
    public void testeKhanObrigatorioImpedeFimDoTurno() {
        InterfacePartida partida = prepararPartida();
        assertTrue("Altani vermelha tem acao Khan", partida.ativarColunaJogadorAtual(0));
        assertTrue("A acao do Khan esta pendente", partida.deveMoverKhanJogadorAtual());
        assertFalse("Passar turno com Khan pendente e proibido", partida.tentarAvancarJogador());
        assertEquals("O jogador deve continuar na mesma vez", 0, partida.getIndiceJogadorAtual());
    }

    /** Uma acao Khan valida registra destino e libera a passagem de turno. */
    @Test(timeout = 2000)
    public void testeMoverKhanLiberaProximoTurno() {
        InterfacePartida partida = prepararPartida();
        partida.ativarColunaJogadorAtual(0);
        assertTrue("Khan deve poder ir a uma provincia", partida.realizarAcaoKhan(DestinoKhan.RUSSIA_OESTE));
        assertEquals("Destino do Khan deve estar disponivel para a View", DestinoKhan.RUSSIA_OESTE,
                partida.consultarDestinoKhan());
        assertFalse("A acao ja foi realizada", partida.deveMoverKhanJogadorAtual());
        assertTrue("A vez pode terminar depois da acao", partida.tentarAvancarJogador());
        assertEquals("O proximo jogador deve ficar ativo", 1, partida.getIndiceJogadorAtual());
    }

    /** Nao pode repetir a acao Khan no mesmo turno ou mantê-lo na mesma area. */
    @Test(timeout = 2000)
    public void testeKhanNaoPermiteRepetirDestinoOuAcao() {
        InterfacePartida partida = prepararPartida();
        partida.ativarColunaJogadorAtual(0);
        assertTrue("Primeira movimentacao deve ocorrer", partida.realizarAcaoKhan(DestinoKhan.RUSSIA_OESTE));
        assertFalse("Acao nao pode ser repetida", partida.realizarAcaoKhan(DestinoKhan.CHINA_LESTE));
        partida.tentarAvancarJogador();
        partida.tentarAvancarJogador();
        assertTrue("A coluna branca libera nova acao Khan", partida.ativarColunaJogadorAtual(3));
        assertFalse("Mesmo destino do Khan deve ser recusado", partida.realizarAcaoKhan(DestinoKhan.RUSSIA_OESTE));
    }

    /** Mudancas reais notificam observadores; rejeicoes nao notificam. */
    @Test(timeout = 2000)
    public void testeKhanObserverNaoNotificaOperacaoRejeitada() {
        InterfacePartida partida = prepararPartida();
        class Contador implements ObservadorPartida {
            int numero;
            @Override
            public void atualizar(ObservadoPartida observado) {
                numero++;
            }
        }
        Contador contador = new Contador();
        partida.registrarObservador(contador);
        assertFalse("Sem coluna, Khan nao pode se mover", partida.realizarAcaoKhan(DestinoKhan.RUSSIA_OESTE));
        assertTrue("Ativar a coluna deve funcionar", partida.ativarColunaJogadorAtual(0));
        assertTrue("Mover o Khan deve funcionar", partida.realizarAcaoKhan(DestinoKhan.RUSSIA_OESTE));
        assertEquals("Uma notificacao por alteracao real", 2, contador.numero);
    }
    /** O custo do primeiro saque volta a ser uma espada no turno seguinte. */
    @Test(timeout = 2000)
    public void testeHistoricoDeSaquesReiniciaAoPassarTurno() {
        InterfacePartida partida = prepararPartida();
        String cidade = primeiraCidade(partida);
        partida.configurarAcessoCidade(cidade, 1);
        partida.configurarAcessoCidade(cidade, 2);
        darEspadas(partida, 2);
        chegarNaParada(partida, 1, 1);
        assertTrue("Primeiro turno aceita saque", partida.saquearCidadeAtual(cidade));
        assertTrue("Pode passar sem acao obrigatoria pendente", partida.tentarAvancarJogador());
        assertTrue("Segundo jogador passa", partida.tentarAvancarJogador());
        assertTrue("Altani usa coluna amarela no turno seguinte", partida.ativarColunaJogadorAtual(2));
        assertTrue("O jogador chega a outro acesso da mesma cidade", partida.movimentarJogadorComAtivacao(2));
        assertTrue("No novo turno o saque custa somente uma espada", partida.saquearCidadeAtual(cidade));
        assertEquals("Duas espadas cobrem um saque em cada turno", 0,
                partida.consultarCartelaJogadorAtual().getEspadas());
    }

    /** Observer deve notificar saque valido e nao notificar saque recusado. */
    @Test(timeout = 2000)
    public void testeSaqueNotificaObserverSomenteAoAlterarCidade() {
        InterfacePartida partida = prepararPartida();
        String cidade = primeiraCidade(partida);
        partida.configurarAcessoCidade(cidade, 1);
        darEspadas(partida, 1);
        chegarNaParada(partida, 1, 1);
        class Contador implements ObservadorPartida {
            int numero;
            @Override
            public void atualizar(ObservadoPartida observado) {
                numero++;
            }
        }
        Contador contador = new Contador();
        partida.registrarObservador(contador);
        assertTrue("Um saque autorizado muda a cidade", partida.saquearCidadeAtual(cidade));
        assertFalse("Um saque recusado nao muda nada", partida.saquearCidadeAtual(cidade));
        assertEquals("Apenas o saque validado deve notificar", 1, contador.numero);
    }


}
