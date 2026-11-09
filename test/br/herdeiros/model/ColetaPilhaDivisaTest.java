package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;
import br.herdeiros.mapa.MapaDados;

/** Testa a coleta de duas peças visiveis em uma unica pilha de divisa. */
public class ColetaPilhaDivisaTest {

    private InterfacePartida prepararPartida(boolean comAcesso) {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Altani", "Jochi"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.ALTANI, Personagem.JOCHI}, false);
        MapaDados mapa = new MapaDados();
        mapa.adicionar(1, 0.2, 0.2, false);
        mapa.conectar(0, 1);
        assertTrue("Mapa valido", partida.carregarMapa(mapa));
        assertTrue("Primeira reserva configurada",
                partida.configurarProvincia(Provincia.PERSIA_SUL, "MOEDA", 1));
        assertTrue("Segunda reserva configurada",
                partida.configurarProvincia(Provincia.RUSSIA_OESTE, "MOEDA", 1));
        assertTrue("Acesso configurado para Persia",
                partida.configurarAcessoProvincia(Provincia.PERSIA_SUL, 1));
        assertTrue("Coluna com punhos ativada", partida.ativarColunaJogadorAtual(0));
        if (comAcesso) {
            assertTrue("Peao alcança a parada de acesso",
                    partida.movimentarJogadorComAtivacao(1));
        }
        return partida;
    }

    /**
     * Objetivo: permitir duas coletas consecutivas da mesma pilha numa divisa.
     * Condicao inicial: dois punhos, acesso a Persia, uma moeda em cada uma
     * das duas provincias indicadas pelo mesmo icone.
     * Resultado esperado: duas moedas recebidas, estoques zerados e sem punhos.
     */
    @Test(timeout = 2000)
    public void testeDuasColetasDaMesmaPilhaDeDivisa() {
        InterfacePartida partida = prepararPartida(true);
        Provincia[] pilha = {Provincia.PERSIA_SUL, Provincia.RUSSIA_OESTE};
        int saldoInicial = partida.consultarCartelaJogadorAtual().getMoedas();

        assertTrue("O primeiro clique coleta uma moeda", partida.coletarTributoDaPilha(pilha, "MOEDA"));
        assertTrue("O segundo clique coleta a moeda restante da mesma pilha",
                partida.coletarTributoDaPilha(pilha, "MOEDA"));
        assertEquals("O jogador recebe as duas moedas", saldoInicial + 2,
                partida.consultarCartelaJogadorAtual().getMoedas());
        assertEquals("O primeiro estoque fica vazio", 0,
                partida.consultarQuantidadeTributoProvincia(Provincia.PERSIA_SUL, "MOEDA"));
        assertEquals("O segundo estoque fica vazio", 0,
                partida.consultarQuantidadeTributoProvincia(Provincia.RUSSIA_OESTE, "MOEDA"));
        assertEquals("Dois punhos foram consumidos", 0,
                partida.consultarAtivacaoJogadorAtual().getPunhosDaColuna());
    }

    /**
     * Objetivo: exigir acesso a pelo menos uma provincia da pilha.
     * Condicao inicial: estoques e punhos disponiveis, mas peao ainda distante.
     * Resultado esperado: nenhuma coleta nem consumo de punhos.
     */
    @Test(timeout = 2000)
    public void testePilhaInacessivelNaoPermiteColeta() {
        InterfacePartida partida = prepararPartida(false);
        Provincia[] pilha = {Provincia.PERSIA_SUL, Provincia.RUSSIA_OESTE};
        int punhosAntes = partida.consultarAtivacaoJogadorAtual().getPunhosDaColuna();

        assertFalse("Nao pode coletar uma pilha inacessivel",
                partida.coletarTributoDaPilha(pilha, "MOEDA"));
        assertEquals("Nao consome punhos em uma coleta recusada", punhosAntes,
                partida.consultarAtivacaoJogadorAtual().getPunhosDaColuna());
    }

    /**
     * Objetivo: impedir nova coleta quando o saldo de punhos acabou.
     * Condicao inicial: duas coletas aceitas; terceira peca adicionada por reposicao
     * nao e necessaria para este cenario, pois o estoque ja chegou a zero.
     * Resultado esperado: o terceiro clique e recusado sem alterar o saldo.
     */
    @Test(timeout = 2000)
    public void testeTerceiroCliqueNaoGeraTributo() {
        InterfacePartida partida = prepararPartida(true);
        Provincia[] pilha = {Provincia.PERSIA_SUL, Provincia.RUSSIA_OESTE};
        partida.coletarTributoDaPilha(pilha, "MOEDA");
        partida.coletarTributoDaPilha(pilha, "MOEDA");
        int saldoAnterior = partida.consultarCartelaJogadorAtual().getMoedas();

        assertFalse("Nao pode retirar uma terceira moeda da pilha esgotada",
                partida.coletarTributoDaPilha(pilha, "MOEDA"));
        assertEquals("O saldo permanece inalterado", saldoAnterior,
                partida.consultarCartelaJogadorAtual().getMoedas());
    }
}
