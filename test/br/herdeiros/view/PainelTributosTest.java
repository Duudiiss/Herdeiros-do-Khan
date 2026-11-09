package br.herdeiros.view;

import static org.junit.Assert.*;
import org.junit.Test;

import br.herdeiros.controller.ControladorTabuleiro;
import br.herdeiros.mapa.MapaDados;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.Provincia;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** Testes da selecao visual dos tributos diretamente na imagem do tabuleiro. */
public class PainelTributosTest {

    /**
     * Objetivo: confirmar a escolha de um tipo por clique na regiao da provincia.
     * Condicao inicial: painel desenhado em tamanho de referencia.
     * Resultado esperado: o clique identifica provincia e tipo corretos.
     */
    @Test(timeout = 2000)
    public void testeCliqueEmTributoIdentificaProvinciaETipo() throws IOException {
        PainelTabuleiro painel = criarPainel(900, 900);
        double[] coordenadas = obterCoordenadasTela(painel, 280 * 900.0 / 1015.0, 315 * 900.0 / 1000.0);
        PainelTabuleiro.SelecaoTributo escolha = painel.identificarTributoClicado(
                (int) coordenadas[0], (int) coordenadas[1]);
        assertNotNull("O recurso deve ser identificado", escolha);
        assertEquals("A provincia clicada e a russa oeste", Provincia.RUSSIA_OESTE,
                escolha.getProvincia());
        assertEquals("O icone impresso na divisa representa moeda", "MOEDA", escolha.getTipo());
        assertEquals("O icone da divisa tem duas provincias", 2, escolha.getProvincias().length);
    }

    /**
     * Objetivo: validar o mapeamento apos redimensionar o painel Swing.
     * Condicao inicial: painel com medidas diferentes do sistema virtual de 900.
     * Resultado esperado: mesmas coordenadas proporcionais identificam o mesmo tributo.
     */
    @Test(timeout = 2000)
    public void testeCliqueEscaladoEmPainelRedimensionado() throws IOException {
        PainelTabuleiro painel = criarPainel(620, 780);
        double[] coordenadas = obterCoordenadasTela(painel, 280 * 900.0 / 1015.0, 315 * 900.0 / 1000.0);
        PainelTabuleiro.SelecaoTributo escolha = painel.identificarTributoClicado(
                (int) coordenadas[0], (int) coordenadas[1]);
        assertNotNull("O clique ajustado pela escala encontra o item", escolha);
        assertEquals("A provincia nao muda com a escala", Provincia.RUSSIA_OESTE,
                escolha.getProvincia());
        assertEquals("O tipo nao muda com a escala", "MOEDA", escolha.getTipo());
    }

    /**
     * Objetivo: preservar cliques normais do tabuleiro fora dos botoes de tributos.
     * Condicao inicial: painel desenhado e nenhuma opcao de tributo na posicao.
     * Resultado esperado: nenhuma selecao de tributo e devolvida.
     */
    @Test(timeout = 2000)
    public void testeCliqueForaDosTributosNaoInterfereNoMapa() throws IOException {
        PainelTabuleiro painel = criarPainel(900, 900);
        double[] coordenadas = obterCoordenadasTela(painel, 450, 450);
        assertNull("O centro do tabuleiro nao e um botao de tributo",
                painel.identificarTributoClicado((int) coordenadas[0], (int) coordenadas[1]));
    }

    /** Objetivo: verificar a posicao do Khan na extremidade esquerda da trilha. */
    @Test(timeout = 2000)
    public void testeAreaDeMelhoriasDoKhanEstaNoInicioDaTrilha() {
        assertTrue("O marcador fica na margem esquerda da trilha",
                br.herdeiros.model.DestinoKhan.MELHORIAS.getX() < 0.08);
        assertTrue("O marcador fica na faixa inferior do tabuleiro",
                br.herdeiros.model.DestinoKhan.MELHORIAS.getY() > 0.90);
    }


    /**
     * Objetivo: reconhecer o simbolo original da espada sem bloquear as paradas.
     * Condicao inicial: painel com a imagem carregada no tamanho de referencia.
     * Resultado esperado: clique no desenho da espada identifica seu tipo.
     */
    @Test(timeout = 2000)
    public void testeCliqueNoIconeDeEspadaDoMapa() throws IOException {
        PainelTabuleiro painel = criarPainel(900, 900);
        double[] xy = obterCoordenadasTela(painel, 306 * 900.0 / 1015.0, 119 * 900.0 / 1000.0);
        PainelTabuleiro.SelecaoTributo escolha = painel.identificarTributoClicado(
                (int) xy[0], (int) xy[1]);
        assertNotNull("Clicar na espada desenhada precisa identificar o tributo", escolha);
        assertEquals("O tipo escolhido deve ser uma espada", "ESPADA", escolha.getTipo());
    }

    /**
     * Objetivo: permitir saquear uma cidade clicando no proprio tabuleiro.
     * Condicao inicial: painel carregado, sem abrir uma janela de cidades.
     * Resultado esperado: o local do desenho da cidade resolve seu nome.
     */
    @Test(timeout = 2000)
    public void testeCliqueNaCidadeIdentificaDestinoDeSaque() throws IOException {
        PainelTabuleiro painel = criarPainel(900, 900);
        double[] xy = obterCoordenadasTela(painel, 375 * 900.0 / 1015.0, 200 * 900.0 / 1000.0);
        assertEquals("O clique em Moscou deve oferecer saque da cidade", "MOSCOU",
                painel.identificarCidadeClicada((int) xy[0], (int) xy[1]));
    }

    /**
     * Objetivo: impedir que cliques fora dos icones sejam tratados como saques.
     * Condicao inicial: painel de jogo pronto.
     * Resultado esperado: nao ha cidade selecionada no centro de Karakorum.
     */
    @Test(timeout = 2000)
    public void testeCliqueForaDeCidadesNaoIniciaSaque() throws IOException {
        PainelTabuleiro painel = criarPainel(900, 900);
        double[] xy = obterCoordenadasTela(painel, 515 * 900.0 / 1015.0, 400 * 900.0 / 1000.0);
        assertNull("O clique fora de cidade nao pode iniciar saque",
                painel.identificarCidadeClicada((int) xy[0], (int) xy[1]));
    }

    private PainelTabuleiro criarPainel(int largura, int altura) throws IOException {
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa = new MapaDados();
        ControladorTabuleiro controlador = new ControladorTabuleiro(partida, mapa);
        PainelTabuleiro painel = new PainelTabuleiro(partida, mapa, controlador);
        painel.setSize(largura, altura);
        BufferedImage buffer = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graficos = buffer.createGraphics();
        painel.paint(graficos);
        graficos.dispose();
        return painel;
    }

    private double[] obterCoordenadasTela(PainelTabuleiro painel, double xVirtual, double yVirtual) {
        // Como a imagem e centralizada, a margem pode ser recuperada pelo proprio tamanho do painel.
        double largura = painel.getLarguraImagem();
        double altura = painel.getAlturaImagem();
        double xMargem = (painel.getWidth() - largura) / 2.0;
        double yMargem = (painel.getHeight() - altura) / 2.0;
        return new double[] {xMargem + largura * xVirtual / 900.0,
                yMargem + altura * yVirtual / 900.0};
    }
}
