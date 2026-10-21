package br.herdeiros.model;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

/** Testa cadastro, conexões, movimentação, ocupação, proteção de listas e yurts. */
public class TabuleiroTest {

    private Tabuleiro tabuleiro;

    @Before
    public void preparar() {
        tabuleiro = new Tabuleiro(new ArrayList<Cidade>());
    }

    // Cada teste utiliza um tabuleiro novo criado em @Before.

    /**
     * Objetivo: Verificar que o cadastro de paradas novas altera a quantidade armazenada.
     * Condição inicial: Um tabuleiro recém-criado e sua quantidade inicial de paradas registrada.
     * Resultado esperado: Duas inserções válidas retornam true e aumentam a contagem em duas, independentemente das paradas preexistentes.
     */
    @Test(timeout = 2000)
    public void testeAdicionarParadas() {
        int quantidadeInicial = tabuleiro.getQuantidadeParadas();

        assertTrue("testeAdicionarParadas: tabuleiro.adicionarParada(1, false) deve ser verdadeiro", tabuleiro.adicionarParada(1, false));
        assertTrue("testeAdicionarParadas: tabuleiro.adicionarParada(2, true) deve ser verdadeiro", tabuleiro.adicionarParada(2, true));

        assertEquals("testeAdicionarParadas: tabuleiro.getQuantidadeParadas() deve corresponder ao valor esperado", 
        quantidadeInicial + 2,
        tabuleiro.getQuantidadeParadas()
        );
    }

    /**
     * Objetivo: Verificar a proteção contra identificadores repetidos.
     * Condição inicial: Uma parada cadastrada e a quantidade atual de paradas registrada.
     * Resultado esperado: A segunda inserção com o mesmo ID retorna false e mantém a contagem.
     */
    @Test(timeout = 2000)
    public void testeNaoPermitirParadasDuplicadas() {
        assertTrue("testeNaoPermitirParadasDuplicadas: tabuleiro.adicionarParada(1, false) deve ser verdadeiro", tabuleiro.adicionarParada(1, false));

        int quantidadeAnterior = tabuleiro.getQuantidadeParadas();

        assertFalse("testeNaoPermitirParadasDuplicadas: tabuleiro.adicionarParada(1, true) deve ser falso", tabuleiro.adicionarParada(1, true));

        assertEquals("testeNaoPermitirParadasDuplicadas: tabuleiro.getQuantidadeParadas() deve corresponder ao valor esperado", 
        quantidadeAnterior,
        tabuleiro.getQuantidadeParadas()
        );
    }

    /**
     * Objetivo: Verificar a criação recíproca de rotas entre duas paradas existentes.
     * Condição inicial: Duas paradas cadastradas e ainda não conectadas pelo teste.
     * Resultado esperado: O cadastro da conexão tem sucesso e a adjacência é reconhecida em ambos os sentidos.
     */
    @Test(timeout = 2000)
    public void testeConexaoBidirecional() {
        tabuleiro.adicionarParada(1, false);
        tabuleiro.adicionarParada(2, false);

        assertTrue("testeConexaoBidirecional: tabuleiro.conectarParadas(1, 2) deve ser verdadeiro", tabuleiro.conectarParadas(1, 2));

        assertTrue("testeConexaoBidirecional: tabuleiro.saoAdjacentes(1, 2) deve ser verdadeiro", tabuleiro.saoAdjacentes(1, 2));
        assertTrue("testeConexaoBidirecional: tabuleiro.saoAdjacentes(2, 1) deve ser verdadeiro", tabuleiro.saoAdjacentes(2, 1));
    }

    /**
     * Objetivo: Verificar a rejeição de conexões inválidas.
     * Condição inicial: Uma parada existente, outra inexistente e a quantidade de paradas registrada.
     * Resultado esperado: Tentativas inválidas retornam false, sem criar uma conexão nem modificar a quantidade de paradas.
     */
    @Test(timeout = 2000)
    public void testeNaoConectarParadasInexistentes() {
        tabuleiro.adicionarParada(1, false);
        int quantidadeAntes = tabuleiro.getQuantidadeParadas();

        assertFalse("testeNaoConectarParadasInexistentes: tabuleiro.conectarParadas(1, 2) deve ser falso", tabuleiro.conectarParadas(1, 2));
        assertEquals("testeNaoConectarParadasInexistentes: tabuleiro.getQuantidadeParadas() deve corresponder ao valor esperado", quantidadeAntes, tabuleiro.getQuantidadeParadas());
        assertFalse("testeNaoConectarParadasInexistentes: tabuleiro.saoAdjacentes(1, 2) deve ser falso", tabuleiro.saoAdjacentes(1, 2));
    }

    /**
     * Objetivo: Verificar um deslocamento permitido por uma conexão direta.
     * Condição inicial: Um peão registrado em Karakorum e uma parada de destino conectada e disponível.
     * Resultado esperado: A operação retorna true e a localização do peão passa a ser a parada de destino.
     */
    @Test(timeout = 2000)
    public void testeMovimentacaoValida() {

        tabuleiro.adicionarParada(1, false);
        tabuleiro.conectarParadas(0, 1);

        Peao peao = new Peao(Cor.AZUL);

        assertTrue("testeMovimentacaoValida: tabuleiro.posicionarPeaoInicial(peao) deve ser verdadeiro", tabuleiro.posicionarPeaoInicial(peao));
        assertTrue("testeMovimentacaoValida: tabuleiro.moverPeao(peao, 1) deve ser verdadeiro", tabuleiro.moverPeao(peao, 1));

        assertEquals("testeMovimentacaoValida: peao.getParadaAtual().getId() deve corresponder ao valor esperado", 1, peao.getParadaAtual().getId());
    }

    /**
     * Objetivo: Verificar que não é possível ir diretamente a uma parada sem ligação com a origem.
     * Condição inicial: Um peão registrado em Karakorum e um destino alcançável apenas por uma parada intermediária.
     * Resultado esperado: A operação retorna false e a localização permanece em Karakorum.
     */
    @Test(timeout = 2000)
    public void testeMovimentacaoNaoAdjacente() {

        tabuleiro.adicionarParada(1, false);
        tabuleiro.adicionarParada(2, false);

        tabuleiro.conectarParadas(0, 1);
        tabuleiro.conectarParadas(1, 2);

        Peao peao = new Peao(Cor.AZUL);

        tabuleiro.posicionarPeaoInicial(peao);

        assertFalse("testeMovimentacaoNaoAdjacente: tabuleiro.moverPeao(peao, 2) deve ser falso", tabuleiro.moverPeao(peao, 2));
        assertEquals("Karakorum", peao.getLocalizacaoAtual());

        // O peão deve continuar em Karakorum
        assertEquals("testeMovimentacaoNaoAdjacente: peao.getParadaAtual().getId() deve corresponder ao valor esperado", 0, peao.getParadaAtual().getId());
    }

    /**
     * Objetivo: Verificar a proteção da lista interna de cidades do tabuleiro.
     * Condição inicial: Um tabuleiro criado com uma lista de cidades fornecida externamente.
     * Resultado esperado: Alterações na lista de entrada e na lista retornada pelo getter não mudam a lista mantida pelo tabuleiro.
     */
    @Test(timeout = 2000)
    public void testeListaDeCidadesNaoPodeSerAlteradaExternamente() {
        Cidade cidade = new Cidade(NomeCidade.MOSCOU, Regiao.RUSSIA);
        java.util.List<Cidade> iniciais = new ArrayList<>();
        iniciais.add(cidade);
        Tabuleiro outroTabuleiro = new Tabuleiro(iniciais);

        iniciais.clear();
        assertEquals("testeListaDeCidadesNaoPodeSerAlteradaExternamente: outroTabuleiro.getCidadesDisponiveis().size() deve corresponder ao valor esperado", 1, outroTabuleiro.getCidadesDisponiveis().size());

        outroTabuleiro.getCidadesDisponiveis().clear();
        assertEquals("testeListaDeCidadesNaoPodeSerAlteradaExternamente: outroTabuleiro.getCidadesDisponiveis().size() deve corresponder ao valor esperado", 1, outroTabuleiro.getCidadesDisponiveis().size());
    }

    /**
     * Objetivo: Verificar a capacidade de uma parada simples e a preservação de posições após recusa.
     * Condição inicial: Dois peões partem de Karakorum e uma parada simples de destino é ocupada pelo primeiro.
     * Resultado esperado: A entrada do segundo é rejeitada; as posições dos dois peões continuam coerentes.
     */
    @Test(timeout = 2000)
    public void testeNaoEntrarEmParadaSimplesOcupada() {
        tabuleiro.adicionarParada(1, false);
        tabuleiro.conectarParadas(0, 1);
        Peao a = new Peao(Cor.AZUL);
        Peao b = new Peao(Cor.VERDE);
        assertTrue("testeNaoEntrarEmParadaSimplesOcupada: tabuleiro.posicionarPeaoInicial(a) deve ser verdadeiro", tabuleiro.posicionarPeaoInicial(a));
        assertTrue("testeNaoEntrarEmParadaSimplesOcupada: tabuleiro.posicionarPeaoInicial(b) deve ser verdadeiro", tabuleiro.posicionarPeaoInicial(b));
        assertTrue("testeNaoEntrarEmParadaSimplesOcupada: tabuleiro.moverPeao(a, 1) deve ser verdadeiro", tabuleiro.moverPeao(a, 1));
        int ocupacaoAntes = tabuleiro.getOcupacao(1);
        assertFalse("testeNaoEntrarEmParadaSimplesOcupada: tabuleiro.moverPeao(b, 1) deve ser falso", tabuleiro.moverPeao(b, 1));
        assertEquals("testeNaoEntrarEmParadaSimplesOcupada: tabuleiro.getOcupacao(1) deve corresponder ao valor esperado", ocupacaoAntes, tabuleiro.getOcupacao(1));
        assertEquals("testeNaoEntrarEmParadaSimplesOcupada: b.getParadaAtual().getId() deve corresponder ao valor esperado", 0, b.getParadaAtual().getId());
        assertEquals("testeNaoEntrarEmParadaSimplesOcupada: a.getParadaAtual().getId() deve corresponder ao valor esperado", 1, a.getParadaAtual().getId());
    }

    /**
     * Objetivo: Verificar o limite de ocupação de uma parada dupla.
     * Condição inicial: Três peões registrados em Karakorum e uma parada dupla adjacente disponível.
     * Resultado esperado: Os dois primeiros movimentos são aceitos, o terceiro é recusado e a ocupação respeita o limite.
     */
    @Test(timeout = 2000)
    public void testeCapacidadeDaParadaDupla() {
        tabuleiro.adicionarParada(1, true);
        tabuleiro.conectarParadas(0, 1);
        Peao a = new Peao(Cor.AZUL);
        Peao b = new Peao(Cor.VERDE);
        Peao c = new Peao(Cor.PRETO);
        for (Peao peao : new Peao[] { a, b, c }) {
            assertTrue("testeCapacidadeDaParadaDupla: tabuleiro.posicionarPeaoInicial(peao) deve ser verdadeiro", tabuleiro.posicionarPeaoInicial(peao));
        }
        assertTrue("testeCapacidadeDaParadaDupla: tabuleiro.moverPeao(a, 1) deve ser verdadeiro", tabuleiro.moverPeao(a, 1));
        assertTrue("testeCapacidadeDaParadaDupla: tabuleiro.moverPeao(b, 1) deve ser verdadeiro", tabuleiro.moverPeao(b, 1));
        assertFalse("testeCapacidadeDaParadaDupla: tabuleiro.moverPeao(c, 1) deve ser falso", tabuleiro.moverPeao(c, 1));
        assertEquals("testeCapacidadeDaParadaDupla: tabuleiro.getOcupacao(1) deve corresponder ao valor esperado", 2, tabuleiro.getOcupacao(1));
    }

    /**
     * Objetivo: Confirmar que a posição inicial comporta simultaneamente os cinco jogadores.
     * Condição inicial: Um tabuleiro recém-criado, com a ocupação inicial registrada.
     * Resultado esperado: Os cinco peões são registrados em Karakorum e a ocupação aumenta na quantidade de registros efetuados.
     */
    @Test(timeout = 2000)
    public void testeKarakorumSemLimiteDeOcupacaoDosJogadores() {
        int inicial = tabuleiro.getOcupacao(0);
        for (Cor cor : new Cor[]{Cor.AZUL, Cor.VERDE, Cor.PRETO, Cor.AMARELO, Cor.LARANJA}) {
            assertTrue("testeKarakorumSemLimiteDeOcupacaoDosJogadores: tabuleiro.posicionarPeaoInicial(new Peao(cor, \"Karakorum\")) deve ser verdadeiro", tabuleiro.posicionarPeaoInicial(new Peao(cor)));
        }
        assertEquals("testeKarakorumSemLimiteDeOcupacaoDosJogadores: tabuleiro.getOcupacao(0) deve corresponder ao valor esperado", inicial + 5, tabuleiro.getOcupacao(0));
    }

    /**
     * Objetivo: Verificar a entrada do Khan diante de destinos com capacidades diferentes.
     * Condição inicial: Uma parada simples ocupada e outra parada disponível no tabuleiro.
     * Resultado esperado: O Khan não entra na parada ocupada, mas pode entrar na parada livre, respeitando a capacidade.
     */
    @Test(timeout = 2000)
    public void testeEntradaDoKhanEProtecaoDaCapacidade() {
        tabuleiro.adicionarParada(1, false);
        tabuleiro.adicionarParada(2, false);
        tabuleiro.conectarParadas(1, 2);
        Peao jogador = new Peao(Cor.AZUL);
        tabuleiro.posicionarPeaoInicial(jogador);
        tabuleiro.conectarParadas(0, 1);
        assertTrue("testeEntradaDoKhanEProtecaoDaCapacidade: tabuleiro.moverPeao(jogador, 1) deve ser verdadeiro", tabuleiro.moverPeao(jogador, 1));
        assertFalse("testeEntradaDoKhanEProtecaoDaCapacidade: tabuleiro.moverKhan(1) deve ser falso", tabuleiro.moverKhan(1));
        assertNull("testeEntradaDoKhanEProtecaoDaCapacidade: tabuleiro.getPeaoDoKhan().getParadaAtual() deve ser nulo", tabuleiro.getPeaoDoKhan().getParadaAtual());
        assertTrue("testeEntradaDoKhanEProtecaoDaCapacidade: tabuleiro.moverKhan(2) deve ser verdadeiro", tabuleiro.moverKhan(2));
        assertFalse("testeEntradaDoKhanEProtecaoDaCapacidade: tabuleiro.moverKhan(1) deve ser falso", tabuleiro.moverKhan(1));
        assertEquals("testeEntradaDoKhanEProtecaoDaCapacidade: tabuleiro.getPeaoDoKhan().getParadaAtual().getId() deve corresponder ao valor esperado", 2, tabuleiro.getPeaoDoKhan().getParadaAtual().getId());
    }

    /**
     * Objetivo: Verificar restrições básicas ao registro de yurts.
     * Condição inicial: Karakorum e uma parada comum cadastradas.
     * Resultado esperado: Não é possível construir yurt na capital nem duplicar uma yurt já registrada na mesma parada.
     */
    @Test(timeout = 2000)
    public void testeYurtNaoPodeSerDuplicadoNemEstarEmKarakorum() {
        tabuleiro.adicionarParada(1, false);
        assertFalse("testeYurtNaoPodeSerDuplicadoNemEstarEmKarakorum: tabuleiro.construirYurt(0, Cor.AZUL) deve ser falso", tabuleiro.construirYurt(0, Cor.AZUL));
        assertFalse("testeYurtNaoPodeSerDuplicadoNemEstarEmKarakorum: tabuleiro.construirYurt(999, Cor.AZUL) deve ser falso", tabuleiro.construirYurt(999, Cor.AZUL));
        assertTrue("testeYurtNaoPodeSerDuplicadoNemEstarEmKarakorum: tabuleiro.construirYurt(1, Cor.AZUL) deve ser verdadeiro", tabuleiro.construirYurt(1, Cor.AZUL));
        assertFalse("testeYurtNaoPodeSerDuplicadoNemEstarEmKarakorum: tabuleiro.construirYurt(1, Cor.VERDE) deve ser falso", tabuleiro.construirYurt(1, Cor.VERDE));
        assertEquals("testeYurtNaoPodeSerDuplicadoNemEstarEmKarakorum: tabuleiro.getDonoYurt(1) deve corresponder ao valor esperado", Cor.AZUL, tabuleiro.getDonoYurt(1));
    }

    @Test(timeout = 2000)
    public void testeSubstituirCidadeConquistada() {
        Cidade antiga = new Cidade(NomeCidade.MOSCOU, Regiao.RUSSIA);
        Cidade nova = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
        Tabuleiro tabuleiroComCidade = new Tabuleiro(java.util.Arrays.asList(antiga));

        tabuleiroComCidade.substituirCidadeConquistada(antiga, nova);

        assertEquals("A cidade conquistada deve ser substituída",
                1, tabuleiroComCidade.getCidadesDisponiveis().size());
        assertSame("A nova cidade deve ficar disponível",
                nova, tabuleiroComCidade.getCidadesDisponiveis().get(0));
    }

    @Test(timeout = 2000)
    public void testeSubstituicaoDeCidadeAusenteNaoAlteraLista() {
        Cidade existente = new Cidade(NomeCidade.MOSCOU, Regiao.RUSSIA);
        Cidade ausente = new Cidade(NomeCidade.KAZAN, Regiao.RUSSIA);
        Cidade substituta = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
        Tabuleiro tabuleiroComCidade = new Tabuleiro(java.util.Arrays.asList(existente));

        tabuleiroComCidade.substituirCidadeConquistada(ausente, substituta);

        assertEquals("Cidade ausente não deve ser substituída",
                1, tabuleiroComCidade.getCidadesDisponiveis().size());
        assertSame("A cidade original deve permanecer disponível",
                existente, tabuleiroComCidade.getCidadesDisponiveis().get(0));
    }

}
