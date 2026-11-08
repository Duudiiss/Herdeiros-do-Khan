package br.herdeiros.model;

import static org.junit.Assert.*;
import org.junit.Test;

/** Testes da tabela oficial de 20 acoes de personagem e moeda virtual. */
public class AcoesOficiaisPersonagensTest {

    /** Objetivo: verificar os icones de Altani, coluna vermelha.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeAltaniVermelha() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(0, Personagem.ALTANI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.ALTANI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 2,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Altani, coluna verde.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeAltaniVerde() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(1, Personagem.ALTANI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.ALTANI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Altani, coluna amarela.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeAltaniAmarela() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(2, Personagem.ALTANI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.ALTANI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 1,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Altani, coluna branca.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeAltaniBranca() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(3, Personagem.ALTANI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.ALTANI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 0,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Jochi, coluna vermelha.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeJochiVermelha() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(0, Personagem.JOCHI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.JOCHI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 1,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Jochi, coluna verde.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeJochiVerde() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(1, Personagem.JOCHI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.JOCHI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 2,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Jochi, coluna amarela.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeJochiAmarela() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(2, Personagem.JOCHI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.JOCHI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Jochi, coluna branca.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeJochiBranca() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(3, Personagem.JOCHI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.JOCHI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 0,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Chagatai, coluna vermelha.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeChagataiVermelha() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(0, Personagem.CHAGATAI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.CHAGATAI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 2,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Chagatai, coluna verde.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeChagataiVerde() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(1, Personagem.CHAGATAI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.CHAGATAI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 1,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Chagatai, coluna amarela.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeChagataiAmarela() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(2, Personagem.CHAGATAI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.CHAGATAI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Chagatai, coluna branca.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeChagataiBranca() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(3, Personagem.CHAGATAI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.CHAGATAI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 0,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Tolui, coluna vermelha.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeToluiVermelha() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(0, Personagem.TOLUI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.TOLUI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 1,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Tolui, coluna verde.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeToluiVerde() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(1, Personagem.TOLUI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.TOLUI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Tolui, coluna amarela.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeToluiAmarela() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(2, Personagem.TOLUI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.TOLUI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 2,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Tolui, coluna branca.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeToluiBranca() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(3, Personagem.TOLUI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.TOLUI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 0,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Ogedei, coluna vermelha.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeOgedeiVermelha() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(0, Personagem.OGEDEI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.OGEDEI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Ogedei, coluna verde.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeOgedeiVerde() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(1, Personagem.OGEDEI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.OGEDEI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 1,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 1,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", false,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Ogedei, coluna amarela.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeOgedeiAmarela() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(2, Personagem.OGEDEI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.OGEDEI);
        assertEquals("Quantidade de cavalos incorreta", 1,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 2,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: verificar os icones de Ogedei, coluna branca.
     * Condicao inicial: cartela sem ativacao ou melhorias adicionais.
     * Resultado esperado: cavalos, punhos, moeda virtual e Khan como na arte. */
    @Test(timeout = 2000)
    public void testeOgedeiBranca() {
        TabuleiroIndividual cartela = new TabuleiroIndividual();
        assertTrue("A ativacao da coluna deve funcionar",
                cartela.ativarColuna(3, Personagem.OGEDEI));
        ResumoAtivacao ativacao = cartela.consultarAtivacao(Personagem.OGEDEI);
        assertEquals("Quantidade de cavalos incorreta", 2,
                ativacao.getMovimentosRestantes());
        assertEquals("Quantidade de punhos incorreta", 0,
                ativacao.getPunhosDaColuna());
        assertEquals("Quantidade de moedas virtuais incorreta", 0,
                ativacao.getMoedasVirtuaisDaColuna());
        assertEquals("Acao do Khan incorreta", true,
                ativacao.temAcaoKhan());
    }

    /** Objetivo: moeda virtual da coluna complementa dinheiro real na compra.
     * Condicao inicial: Tolui com coluna vermelha ativada e uma moeda fisica.
     * Resultado esperado: compra por duas moedas valida e sem saldo real. */
    @Test(timeout = 2000)
    public void testeMoedaVirtualDaColunaPermiteCompra() {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Tolui", "Altani"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.TOLUI, Personagem.ALTANI}, false);
        assertTrue("A coluna vermelha de Tolui deve ativar",
                partida.ativarColunaJogadorAtual(0));
        assertEquals("Tolui deve receber uma moeda virtual", 1,
                partida.consultarAtivacaoJogadorAtual().getMoedasVirtuaisDaColuna());
        assertTrue("Preparacao: recebe uma moeda real",
                partida.simularRecebimentoTributo(0, "MOEDA", 1));
        assertTrue("Uma moeda real + uma virtual devem pagar uma melhoria",
                partida.comprarMelhoriaAtual(0));
        assertEquals("A moeda real deve ser debitada", 0,
                partida.consultarCartelaJogadorAtual().getMoedas());
    }

    /** Objetivo: moeda temporaria nao se mantém depois da rodada do jogador.
     * Condicao inicial: Tolui ativou a coluna vermelha.
     * Resultado esperado: recurso virtual temporario zerado após encerrar turno. */
    @Test(timeout = 2000)
    public void testeMoedaVirtualDaColunaNaoPermaneceAposTurno() {
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Tolui", "Altani"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                new Personagem[] {Personagem.TOLUI, Personagem.ALTANI}, false);
        assertTrue("A coluna vermelha deve ser ativada",
                partida.ativarColunaJogadorAtual(0));
        assertTrue("O turno pode ser encerrado sem acao de Khan",
                partida.tentarAvancarJogador());
        assertTrue("O proximo jogador pode passar sem ativar coluna",
                partida.tentarAvancarJogador());
        assertEquals("A moeda temporaria deve terminar com o turno", 0,
                partida.consultarCartelaJogadorAtual().getMoedasVirtuais());
    }
}
