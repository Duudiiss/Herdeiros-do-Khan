package br.herdeiros.view;

import br.herdeiros.model.PecaBonus;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** Pecas coletadas desenhadas no tabuleiro individual via Java2D, sem JLabel de imagens. */
public final class PainelBonusJogador extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int LARGURA = 92;
    private static final int ALTURA = 115;
    private static final int MARGEM = 12;
    private final Map<String, BufferedImage> imagens = new HashMap<String, BufferedImage>();
    private List<PecaBonus> pecas = new ArrayList<PecaBonus>();
    private List<Boolean> utilizadas = new ArrayList<Boolean>();
    private IntConsumer aoClicar;

    public PainelBonusJogador() {
        setOpaque(false);
        setPreferredSize(new Dimension(800, ALTURA + 12));
        setToolTipText("Clique em uma peca disponivel para utilizar seu efeito.");
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent evento) {
                int indice = (evento.getX() - MARGEM) / LARGURA;
                if (evento.getX() < MARGEM || indice < 0 || indice >= pecas.size()
                        || evento.getY() < 24 || evento.getY() >= ALTURA + 3) return;
                if (aoClicar != null) aoClicar.accept(indice);
            }
        });
    }

    public void definirAcaoClique(IntConsumer acao) { aoClicar = acao; }

    public void atualizarPecas(List<PecaBonus> novas, List<Boolean> estados) {
        pecas = new ArrayList<PecaBonus>(novas);
        utilizadas = new ArrayList<Boolean>(estados);
        setPreferredSize(new Dimension(Math.max(800, MARGEM * 2 + pecas.size() * LARGURA), ALTURA + 12));
        revalidate();
        repaint();
    }

    @Override protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(240, 210, 140));
        g.drawString("PECAS DE BONUS DO JOGADOR  |  Clique para usar", MARGEM, 16);
        if (pecas.isEmpty()) {
            g.setColor(new Color(225, 220, 205));
            g.drawString("Nenhuma peca recebida. Entregue tesouros a um conselheiro.", MARGEM, 67);
        }
        for (int i = 0; i < pecas.size(); i++) {
            PecaBonus peca = pecas.get(i);
            int x = MARGEM + i * LARGURA;
            int y = 25;
            g.setColor(new Color(245, 210, 133));
            g.drawRoundRect(x, y, 80, 78, 6, 6);
            BufferedImage imagem = carregar(peca.getArquivoImagem());
            if (imagem != null) {
                double escala = Math.min(74.0 / imagem.getWidth(), 72.0 / imagem.getHeight());
                int w = Math.max(1, (int) Math.round(imagem.getWidth() * escala));
                int h = Math.max(1, (int) Math.round(imagem.getHeight() * escala));
                g.drawImage(imagem, x + (80 - w)/2, y + (78 - h)/2, w, h, null);
            }
            boolean utilizada = utilizadas.get(i);
            if (utilizada) {
                g.setColor(new Color(12, 12, 12, 178));
                g.fillRoundRect(x + 1, y + 1, 78, 76, 6, 6);
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.setColor(utilizada ? new Color(210, 210, 210) : new Color(180, 240, 175));
            g.drawString(utilizada ? "UTILIZADA" : "DISPONIVEL", x + 4, y + 90);
        }
        g.dispose();
    }

    private BufferedImage carregar(String arquivo) {
        if (!imagens.containsKey(arquivo)) {
            BufferedImage imagem = null;
            try { imagem = ImageIO.read(new File("assets/cartas_bonus/" + arquivo)); }
            catch (IOException erro) { /* Ausencia de imagem nao impede uso da peca. */ }
            imagens.put(arquivo, imagem);
        }
        return imagens.get(arquivo);
    }
}
