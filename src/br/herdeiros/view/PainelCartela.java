package br.herdeiros.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Usa JPanel apenas como superficie de pintura. A imagem da cartela e sempre
 * apresentada por Graphics2D.drawImage(), conforme o enunciado.
 */
public final class PainelCartela extends JPanel {

    private static final long serialVersionUID = 1L;
    private final BufferedImage[] imagens;
    private int indiceCartela;

    public PainelCartela() throws IOException {
        imagens = new BufferedImage[5];
        for (int i = 0; i < imagens.length; i++) {
            String caminho = "assets/cartelas/cartela" + (i + 1) + ".jpg";
            imagens[i] = ImageIO.read(new File(caminho));
            if (imagens[i] == null) {
                throw new IOException("Imagem de cartela invalida: " + caminho);
            }
        }
        indiceCartela = 0;
        setPreferredSize(new Dimension(900, 520));
    }

    public void mostrarCartela(int indice) {
        if (indice < 0 || indice >= imagens.length) {
            throw new IllegalArgumentException("Cartela inexistente");
        }
        indiceCartela = indice;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        BufferedImage imagem = imagens[indiceCartela];
        Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double escalaX = (double) getWidth() / imagem.getWidth();
        double escalaY = (double) getHeight() / imagem.getHeight();
        double escala = Math.min(escalaX, escalaY);
        int largura = (int) Math.round(imagem.getWidth() * escala);
        int altura = (int) Math.round(imagem.getHeight() * escala);
        int x = (getWidth() - largura) / 2;
        int y = (getHeight() - altura) / 2;

        g.drawImage(imagem, x, y, largura, altura, null);
        g.dispose();
    }
}
