package br.herdeiros.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/** Centraliza a identidade visual Mongol/Imperial (botões, labels com borda e fundos). */
public final class EstiloImperial {

    public static final Color COR_DOURADA_IMPERIAL = new Color(219, 161, 121);
    public static final Color COR_MARROM_COURO = new Color(85, 40, 10);
    public static final Color COR_MARROM_HOVER = new Color(115, 58, 18);
    public static final Color COR_VERMELHO_ESCURO = new Color(80, 20, 20);
    public static final Color COR_VERMELHO_HOVER = new Color(110, 30, 30);
    public static final Color COR_TEXTO_CLARO = new Color(245, 240, 230);
    public static final Color COR_BORDA_TEXTO = new Color(112, 84, 63);
    public static final Color COR_FUNDO_PAINEL = new Color(36, 26, 20);

    public static final Font FONTE_TITULO = new Font("Serif", Font.BOLD, 36);
    public static final Font FONTE_SUBTITULO = new Font("Serif", Font.BOLD, 18);
    public static final Font FONTE_BOTAO_GRANDE = new Font("Serif", Font.BOLD, 26);
    public static final Font FONTE_BOTAO_MEDIO = new Font("Serif", Font.BOLD, 15);
    public static final Font FONTE_TEXTO = new Font("Serif", Font.PLAIN, 14);

    private EstiloImperial() {
    }

    /** Cria um botão arredondado com borda dourada e altura fixa ajustada ao texto. */
    public static JButton criarBotaoEstilizado(String texto, Font fonte,
                                               Color fundoNormal, Color fundoHover,
                                               Color corTexto) {
        final int padHorizontal = 3;
        final int padVertical = 3;

        JButton botao = new JButton(texto) {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                Dimension base = super.getPreferredSize();
                FontMetrics fm = getFontMetrics(getFont());
                int alturaFixa = fm.getHeight() + (padVertical * 2);
                return new Dimension(base.width, alturaFixa);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, pref.height);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(new Color(65, 58, 52, 180));
                } else if (getModel().isPressed()) {
                    g2.setColor(fundoHover.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(fundoHover);
                } else {
                    g2.setColor(fundoNormal);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(isEnabled()
                        ? new Color(219, 161, 121, 180)
                        : new Color(140, 130, 120, 100));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        botao.setFont(fonte);
        botao.setForeground(corTexto);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setContentAreaFilled(false);
        botao.setOpaque(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setBorder(BorderFactory.createEmptyBorder(padVertical, padHorizontal, padVertical, padHorizontal));

        return botao;
    }

    /** Atalho com as cores padrão do tema imperial (marrom couro e letras douradas). */
    public static JButton criarBotaoImperial(String texto) {
        return criarBotaoEstilizado(texto, FONTE_BOTAO_MEDIO,
                COR_MARROM_COURO, COR_MARROM_HOVER, COR_DOURADA_IMPERIAL);
    }

    /** Cria uma JLabel com contorno desenhado via Graphics2D (mesmo estilo da JanelaInicial). */
    public static JLabel criarLabelComBorda(String texto, Font fonte,
                                            Color corTexto, Color corBorda) {
        int espessuraBorda = 1;
        JLabel label = new JLabel(texto, SwingConstants.CENTER) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setFont(getFont());
                String textoLbl = getText();
                if (textoLbl == null) {
                    g2.dispose();
                    return;
                }
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(textoLbl)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.setColor(corBorda);
                for (int dx = -espessuraBorda; dx <= espessuraBorda; dx++) {
                    for (int dy = -espessuraBorda; dy <= espessuraBorda; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2.drawString(textoLbl, x + dx, y + dy);
                        }
                    }
                }

                g2.setColor(corTexto);
                g2.drawString(textoLbl, x, y);
                g2.dispose();
            }
        };

        label.setFont(fonte);
        label.setOpaque(false);
        FontMetrics fm = label.getFontMetrics(fonte);
        label.setPreferredSize(new Dimension(fm.stringWidth(texto) + 5, fm.getHeight() + 10));

        return label;
    }

    /** Pinta o fundo com imagem (se houver) ou com o degradê imperial padrão. */
    public static void pintarFundoImperial(Graphics g, JComponent componente, Image imagemFundo) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (imagemFundo != null) {
            g2.drawImage(imagemFundo, 0, 0, componente.getWidth(), componente.getHeight(), componente);
        } else {
            g2.setPaint(new GradientPaint(
                    0, 0, new Color(48, 32, 22),
                    0, componente.getHeight(), new Color(22, 14, 10)
            ));
            g2.fillRect(0, 0, componente.getWidth(), componente.getHeight());
        }
        g2.dispose();
    }
}