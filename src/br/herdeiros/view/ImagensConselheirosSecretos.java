package br.herdeiros.view;

import br.herdeiros.model.CartaConselheiroSecreto;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/** Imagens oficiais fornecidas pelo usuario e indicacao explicita das ausentes. */
final class ImagensConselheirosSecretos {
    private ImagensConselheirosSecretos() {
    }

    static ImageIcon imagem(CartaConselheiroSecreto carta, int largura, int altura) {
        if (carta == null) {
            return null;
        }
        File arquivo = new File("assets/conselheiros_secretos/" + carta.name() + ".jpg");
        try {
            if (arquivo.isFile()) {
                BufferedImage imagem = ImageIO.read(arquivo);
                if (imagem != null) {
                    return new ImageIcon(imagem.getScaledInstance(
                            largura, altura, java.awt.Image.SCALE_SMOOTH));
                }
            }
        } catch (IOException erro) {
            // A carta continua selecionavel, mas sem ilustracao oficial.
        }
        BufferedImage ausente = new BufferedImage(largura, altura,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ausente.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(96, 31, 71));
        g.fillRoundRect(0, 0, largura - 1, altura - 1, 14, 14);
        g.setColor(new Color(231, 187, 124));
        g.drawRoundRect(2, 2, largura - 5, altura - 5, 14, 14);
        g.setFont(new Font("Serif", Font.BOLD, 17));
        String nome = carta.name();
        int texto = g.getFontMetrics().stringWidth(nome);
        g.drawString(nome, Math.max(4, (largura - texto) / 2), altura / 2 - 8);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("Imagem nao fornecida", 12, altura / 2 + 16);
        g.dispose();
        return new ImageIcon(ausente);
    }
}
