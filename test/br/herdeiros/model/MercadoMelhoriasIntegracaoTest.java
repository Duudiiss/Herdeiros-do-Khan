package br.herdeiros.model;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;

/** Regras de mercado, compra, reposicao e Observer com melhoria oficial. */
public class MercadoMelhoriasIntegracaoTest {

    private InterfacePartida novaPartida() {
        return new InterfacePartida();
    }

    @Test(timeout = 2000)
    public void testeMercadoIniciaComCincoMelhoriasOficiais() {
        InterfacePartida partida = novaPartida();
        List<ResumoMelhoria> mercado = partida.consultarMercadoMelhorias();
        assertEquals("O mercado possui cinco espacos", 5, mercado.size());
        HashSet<String> codigos = new HashSet<String>();
        for (ResumoMelhoria melhoria : mercado) {
            assertNotNull("Os cinco espacos iniciais contem melhorias", melhoria);
            assertTrue("Cada imagem oficial deve ser unica", codigos.add(melhoria.getCodigo()));
            assertTrue("A coluna deve ser colorida", melhoria.getColuna() >= 0 && melhoria.getColuna() <= 2);
        }
    }

    @Test(timeout = 2000)
    public void testeCompraRejeitadaSemMoedaConservaMercado() {
        InterfacePartida partida = novaPartida();
        String primeira = partida.consultarMercadoMelhorias().get(0).getCodigo();
        assertFalse("Nao se pode comprar sem duas moedas", partida.comprarMelhoriaAtual(0));
        assertEquals("Compra rejeitada nao remove melhoria", primeira,
                partida.consultarMercadoMelhorias().get(0).getCodigo());
    }

    @Test(timeout = 2000)
    public void testeCompraConsomeDuasMoedasERegistraMelhoriaNaCartela() {
        InterfacePartida partida = novaPartida();
        String descricao = partida.consultarMercadoMelhorias().get(0).getDescricao();
        assertTrue("A preparacao permite receber duas moedas", partida.simularRecebimentoTributo(0, "MOEDA", 2));
        assertTrue("Compra valida utiliza duas moedas", partida.comprarMelhoriaAtual(0));
        assertEquals("O saldo de moedas deve zerar", 0, partida.consultarCartelaJogadorAtual().getMoedas());
        assertTrue("A cartela passa a possuir a melhoria", partida.consultarCartelaJogadorAtual()
                .getMelhorias().contains(descricao));
    }

    @Test(timeout = 2000)
    public void testeCompraDeslocaAsRestantesParaDireitaSemRepor() {
        InterfacePartida partida = novaPartida();
        assertTrue("Preparacao das moedas deve funcionar", partida.simularRecebimentoTributo(0, "MOEDA", 2));
        assertTrue("Compra deve ser aceita", partida.comprarMelhoriaAtual(1));
        List<ResumoMelhoria> mercado = partida.consultarMercadoMelhorias();
        assertNull("A primeira vaga deve ficar vazia ate o Khan", mercado.get(0));
        assertEquals("Devem restar quatro melhorias expostas", 4, quantidadeCartas(mercado));
    }

    private int quantidadeCartas(List<ResumoMelhoria> mercado) {
        int quantidade = 0;
        for (ResumoMelhoria melhoria : mercado) {
            if (melhoria != null) {
                quantidade++;
            }
        }
        return quantidade;
    }

    @Test(timeout = 2000)
    public void testeKhanAreaMelhoriasPreencheEspacoVazio() {
        InterfacePartida partida = novaPartida();
        partida.simularRecebimentoTributo(0, "MOEDA", 2);
        assertTrue("Compra abre vaga", partida.comprarMelhoriaAtual(1));
        assertEquals("Vaga vazia antes da reposicao", 4,
                quantidadeCartas(partida.consultarMercadoMelhorias()));
        assertTrue("Coluna vermelha de Altani ativa o Khan", partida.ativarColunaJogadorAtual(0));
        assertTrue("Khan pode ser enviado a area de melhorias",
                partida.realizarAcaoKhan(DestinoKhan.MELHORIAS));
        assertEquals("Khan revela uma melhoria no espaco vazio", 5,
                quantidadeCartas(partida.consultarMercadoMelhorias()));
    }

    @Test(timeout = 2000)
    public void testeKhanEmMelhoriasRecebeUmaMoedaEUmaEspada() {
        InterfacePartida partida = novaPartida();
        int moedas = partida.consultarCartelaJogadorAtual().getMoedas();
        int espadas = partida.consultarCartelaJogadorAtual().getEspadas();
        assertTrue("Coluna com Khan ativa", partida.ativarColunaJogadorAtual(0));
        assertTrue("Khan chega a area de melhorias", partida.realizarAcaoKhan(DestinoKhan.MELHORIAS));
        assertEquals("Bonus de moeda na area", moedas + 1,
                partida.consultarCartelaJogadorAtual().getMoedas());
        assertEquals("Bonus de espada na area", espadas + 1,
                partida.consultarCartelaJogadorAtual().getEspadas());
    }

    /**
     * Objetivo: comprovar que uma moeda virtual complementa uma moeda real na compra.
     * Condicao inicial: mercado com sorteio reproduzivel, uma melhoria MOE acessivel
     * e duas moedas reais para adquiri-la na preparacao do teste.
     * Resultado esperado: a melhoria nao oferece moedas no turno da compra;
     * ao ativar sua propria coluna em turno posterior, complementa a moeda real.
     */
    @Test(timeout = 2000)
    public void testeMoedaVirtualCompletaCompraDeMelhoria() {
        MercadoMelhorias mercado = new MercadoMelhorias(new java.util.Random(1));
        InterfacePartida partida = new InterfacePartida(
                new String[] {"Azul", "Vermelho"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO},
                null, false, mercado);

        int indiceMoedaVirtual = -1;
        for (int i = 0; i < 5; i++) {
            ResumoMelhoria melhoria = partida.consultarMercadoMelhorias().get(i);
            if (melhoria != null && melhoria.getCodigo().startsWith("MOE")) {
                indiceMoedaVirtual = i;
                break;
            }
        }
        assertTrue("O mercado preparado deve conter uma melhoria de moeda virtual",
                indiceMoedaVirtual >= 0);

        assertTrue("A preparacao deve fornecer duas moedas reais",
                partida.simularRecebimentoTributo(0, "MOEDA", 2));
        assertTrue("A primeira compra deve adquirir a moeda virtual",
                partida.comprarMelhoriaAtual(indiceMoedaVirtual));
        assertEquals("A melhoria comprada nao funciona no mesmo turno", 0,
                partida.consultarCartelaJogadorAtual().getMoedasVirtuais());
        assertEquals("A primeira compra consome as moedas reais", 0,
                partida.consultarCartelaJogadorAtual().getMoedas());

        assertTrue("Jogador A pode passar o turno", partida.tentarAvancarJogador());
        assertTrue("Jogador B pode passar o turno", partida.tentarAvancarJogador());
        int colunaMelhoria = mercado.consultarResumos().size();
        for (ResumoMelhoria adquirida : partida.consultarCartelaJogadorAtual().getMelhoriasDetalhadas()) {
            if (adquirida.getCodigo().startsWith("MOE")) {
                colunaMelhoria = adquirida.getColuna();
                break;
            }
        }
        assertTrue("A coluna da melhoria deve existir", colunaMelhoria >= 0 && colunaMelhoria <= 2);
        assertTrue("A coluna beneficiada deve ser ativada", partida.ativarColunaJogadorAtual(colunaMelhoria));
        assertEquals("A moeda virtual aparece ao ativar a coluna correta", 1,
                partida.consultarCartelaJogadorAtual().getMoedasVirtuais());

        int proximaMelhoria = -1;
        for (int i = 0; i < 5; i++) {
            if (partida.consultarMercadoMelhorias().get(i) != null) {
                proximaMelhoria = i;
                break;
            }
        }
        assertTrue("Deve restar pelo menos uma melhoria disponivel", proximaMelhoria >= 0);
        assertTrue("A preparacao deve fornecer uma moeda real",
                partida.simularRecebimentoTributo(0, "MOEDA", 1));
        assertTrue("Uma moeda real e uma virtual devem permitir uma nova compra",
                partida.comprarMelhoriaAtual(proximaMelhoria));
        assertEquals("A moeda real complementar deve ter sido consumida", 0,
                partida.consultarCartelaJogadorAtual().getMoedas());
    }

    @Test(timeout = 2000)
    public void testePosicaoInexistenteNaoConsomeMoedas() {
        InterfacePartida partida = novaPartida();
        partida.simularRecebimentoTributo(0, "MOEDA", 3);
        assertFalse("Posicao inexistente nao pode ser comprada", partida.comprarMelhoriaAtual(-1));
        assertFalse("Posicao acima do limite deve ser rejeitada", partida.comprarMelhoriaAtual(5));
        assertEquals("Compra invalida preserva saldo", 3,
                partida.consultarCartelaJogadorAtual().getMoedas());
    }

    @Test(timeout = 2000)
    public void testeMercadoNaoExpoeColecaoMutavel() {
        InterfacePartida partida = novaPartida();
        List<ResumoMelhoria> mercado = partida.consultarMercadoMelhorias();
        try {
            mercado.clear();
            fail("A lista de consulta deve ser imutavel");
        } catch (UnsupportedOperationException esperado) {
            assertEquals("O mercado real continua intacto", 5,
                    partida.consultarMercadoMelhorias().size());
        }
    }

    @Test(timeout = 2000)
    public void testeObserverSomenteNotificaCompraValida() {
        InterfacePartida partida = novaPartida();
        class Contador implements ObservadorPartida {
            int total;
            @Override
            public void atualizar(ObservadoPartida observado) {
                total++;
            }
        }
        Contador contador = new Contador();
        partida.registrarObservador(contador);
        assertFalse("Compra sem moedas falha", partida.comprarMelhoriaAtual(0));
        assertEquals("Nao deve haver notificacao em falha", 0, contador.total);
        partida.simularRecebimentoTributo(0, "MOEDA", 2);
        assertTrue("Compra com moedas funciona", partida.comprarMelhoriaAtual(0));
        assertEquals("Recebimento e compra notificam", 2, contador.total);
    }
}
