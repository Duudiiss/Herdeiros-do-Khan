package br.herdeiros.view;

import br.herdeiros.controller.ControladorTabuleiro;
import br.herdeiros.model.InterfacePartida;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import br.herdeiros.mapa.MapaDados;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** O JPanel é somente a superfície: imagens são desenhadas com Graphics2D.drawImage. */
public class PainelTabuleiro extends JPanel {
    private final BufferedImage imagem;
    private final BufferedImage[] imagemPeoes;
    private final MapaDados mapa;
    private final InterfacePartida partida;
    private final ControladorTabuleiro controlador;
    private int xImagem;
    private int yImagem;
    private int larguraImagem;
    private int alturaImagem;

    public PainelTabuleiro(InterfacePartida partida, MapaDados mapa,
                          ControladorTabuleiro controlador) throws IOException {
        this.partida = partida;
        this.mapa = mapa;
        this.controlador = controlador;
        imagem = ImageIO.read(new File("assets/tabuleiro.jpg"));
        imagemPeoes = new BufferedImage[partida.getQuantidadeJogadores()];
        for (int i = 0; i < imagemPeoes.length; i++) {
            imagemPeoes[i] = criarImagemPeao(CoresJogadores.obter(partida.getCorJogador(i)));
        }
        setPreferredSize(new Dimension(820, 820));
        setBackground(new Color(45, 43, 51));
        addMouseListener(controlador);
    }

    private BufferedImage criarImagemPeao(Color cor) {
        BufferedImage token = new BufferedImage(32, 36, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = token.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(30, 24, 17, 210));
        g.fillOval(5, 27, 22, 7);
        g.setColor(cor);
        g.fillOval(9, 2, 14, 14);
        g.fillRoundRect(7, 15, 18, 17, 5, 5);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(9, 2, 14, 14);
        g.dispose();
        return token;
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double escala = Math.min((double) getWidth() / imagem.getWidth(),
                                 (double) getHeight() / imagem.getHeight());
        larguraImagem = (int) Math.round(imagem.getWidth() * escala);
        alturaImagem = (int) Math.round(imagem.getHeight() * escala);
        xImagem = (getWidth() - larguraImagem) / 2;
        yImagem = (getHeight() - alturaImagem) / 2;
        g.drawImage(imagem, xImagem, yImagem, larguraImagem, alturaImagem, null);
        for (int i = 0; i < partida.getQuantidadeJogadores(); i++) {
            int id = partida.getParadaJogador(i);
            MapaDados.Ponto ponto = mapa.getPonto(id);
            if (ponto != null) {
                int x = xImagem + (int) Math.round(ponto.getX() * larguraImagem);
                int y = yImagem + (int) Math.round(ponto.getY() * alturaImagem);
                int deslocamento = (int) Math.round((i - (imagemPeoes.length - 1) / 2.0) * 23);
                g.drawImage(imagemPeoes[i], x + deslocamento - 16, y - 47, 32, 36, null);
            }
        }
        g.dispose();
    }

    public int getLarguraImagem() {
        return larguraImagem;
    }

    public int getAlturaImagem() {
        return alturaImagem;
    }

    public double[] converterClique(int x, int y) {
        if (larguraImagem <= 0 || alturaImagem <= 0 || x < xImagem || y < yImagem
                || x > xImagem + larguraImagem || y > yImagem + alturaImagem) {
            return null;
        }
        return new double[] {(double) (x - xImagem) / larguraImagem,
                             (double) (y - yImagem) / alturaImagem};
    }
}
