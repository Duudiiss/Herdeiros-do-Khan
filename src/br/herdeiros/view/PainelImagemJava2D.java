package br.herdeiros.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/** Suporte Java2D: imagens sempre renderizadas em Graphics2D.drawImage(). */
public class PainelImagemJava2D extends JPanel {
    private static final long serialVersionUID = 1L;
    private BufferedImage imagem;
    private String texto = "";
    private final List<ActionListener> ouvintes = new ArrayList<ActionListener>();

    public PainelImagemJava2D() {
        setOpaque(false);
        setPreferredSize(new Dimension(170, 200));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent evento) {
                if (!isEnabled()) return;
                ActionEvent acao = new ActionEvent(PainelImagemJava2D.this,
                        ActionEvent.ACTION_PERFORMED, "selecionar");
                for (ActionListener ouvinte : new ArrayList<ActionListener>(ouvintes)) {
                    ouvinte.actionPerformed(acao);
                }
            }
        });
    }

    public void setImagem(BufferedImage imagem) { this.imagem = imagem; repaint(); }
    public void setText(String texto) { this.texto = texto == null ? "" : texto; repaint(); }
    public void addActionListener(ActionListener ouvinte) { ouvintes.add(ouvinte); }

    @Override protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        Graphics2D g = (Graphics2D) graficos.create();
        if (imagem != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            double escala = Math.min((getWidth()-8.0)/imagem.getWidth(),
                    (getHeight()-8.0)/imagem.getHeight());
            int largura = Math.max(1, (int)(imagem.getWidth()*escala));
            int altura = Math.max(1, (int)(imagem.getHeight()*escala));
            g.drawImage(imagem, (getWidth()-largura)/2, (getHeight()-altura)/2,
                    largura, altura, null);
        } else if (!texto.isEmpty()) {
            g.setColor(new Color(235, 214, 186));
            g.drawString(texto, 8, getHeight()/2);
        }
        g.dispose();
    }
}
