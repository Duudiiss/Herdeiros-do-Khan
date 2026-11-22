package br.herdeiros.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Centraliza a identidade visual Mongol/Imperial para todos os componentes Swing. */
public final class EstiloImperial {

    public static final Color COR_DOURADA_IMPERIAL = new Color(219, 161, 121);
    public static final Color COR_MARROM_COURO = new Color(85, 40, 10);
    public static final Color COR_MARROM_HOVER = new Color(115, 58, 18);
    public static final Color COR_VERMELHO_ESCURO = new Color(80, 20, 20);
    public static final Color COR_VERMELHO_HOVER = new Color(110, 30, 30);
    public static final Color COR_TEXTO_CLARO = new Color(245, 240, 230);
    public static final Color COR_BORDA_TEXTO = new Color(112, 84, 63);
    public static final Color COR_FUNDO_CAMPO = new Color(42, 26, 16);
    public static final Color COR_FUNDO_LINHA_ALT = new Color(54, 34, 21);
    public static final Color COR_SELECAO = new Color(130, 72, 28);

    public static final Font FONTE_TITULO = new Font("Serif", Font.BOLD, 36);
    public static final Font FONTE_SUBTITULO = new Font("Serif", Font.BOLD, 18);
    public static final Font FONTE_BOTAO_GRANDE = new Font("Serif", Font.BOLD, 26);
    public static final Font FONTE_BOTAO_MEDIO = new Font("Serif", Font.BOLD, 15);
    public static final Font FONTE_TEXTO = new Font("Serif", Font.PLAIN, 14);
    public static final Font FONTE_TEXTO_NEGRITO = new Font("Serif", Font.BOLD, 14);

    private EstiloImperial() {
    }

    /** Aplica a paleta imperial também aos diálogos padrão do Swing. */
    public static void configurarDialogos() {
        javax.swing.UIDefaults estilo = javax.swing.UIManager.getDefaults();
        String[] fundos = {
            "OptionPane.background", "Panel.background", "Viewport.background",
            "ScrollPane.background", "TextField.background", "TextArea.background",
            "List.background", "ComboBox.background", "Spinner.background",
            "FormattedTextField.background"
        };
        for (String chave : fundos) {
            estilo.put(chave, COR_FUNDO_CAMPO);
        }
        String[] textos = {
            "OptionPane.messageForeground", "Label.foreground", "TextField.foreground",
            "TextArea.foreground", "List.foreground", "ComboBox.foreground",
            "Button.foreground", "Spinner.foreground", "FormattedTextField.foreground"
        };
        for (String chave : textos) {
            estilo.put(chave, COR_TEXTO_CLARO);
        }
        estilo.put("OptionPane.messageFont", FONTE_TEXTO);
        estilo.put("OptionPane.buttonFont", FONTE_TEXTO_NEGRITO);
        estilo.put("Button.background", COR_MARROM_COURO);
        estilo.put("Button.select", COR_MARROM_HOVER);
        estilo.put("ComboBox.selectionBackground", COR_SELECAO);
        estilo.put("ComboBox.selectionForeground", COR_DOURADA_IMPERIAL);
        estilo.put("List.selectionBackground", COR_SELECAO);
        estilo.put("List.selectionForeground", COR_DOURADA_IMPERIAL);
        estilo.put("OptionPane.border", BorderFactory.createLineBorder(COR_DOURADA_IMPERIAL, 2));
    }

    /** Cria um botão arredondado com borda dourada e altura fixa ajustada ao texto. */
    public static JButton criarBotaoEstilizado(String texto, Font fonte,
                                               Color fundoNormal, Color fundoHover,
                                               Color corTexto) {
        final int padHorizontal = 10;
        final int padVertical = 3;

        JButton botao = new JButton(texto) {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                Dimension base = super.getPreferredSize();
                FontMetrics fm = getFontMetrics(getFont());
                int alturaCompacta = fm.getHeight() + (padVertical * 2);
                return new Dimension(base.width, alturaCompacta);
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

    public static JButton criarBotaoImperial(String texto) {
        return criarBotaoEstilizado(texto, FONTE_BOTAO_MEDIO,
                COR_MARROM_COURO, COR_MARROM_HOVER, COR_DOURADA_IMPERIAL);
    }

    /** Cria uma JLabel com contorno desenhado via Graphics2D. */
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
        label.setPreferredSize(new Dimension(fm.stringWidth(texto) + 25, fm.getHeight() + 10));

        return label;
    }

    /** Cria um JPanel com fundo em degradê imperial (madeira/couro escuro). */
    public static JPanel criarPainelFundo(LayoutManager layout) {
        JPanel painel = new JPanel(layout) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                pintarFundoImperial(g, this, null);
            }
        };
        return painel;
    }

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

    /** Cria um JComboBox com fundo escuro, texto claro e seta dourada, eliminando o visual branco padrão. */
    public static <E> JComboBox<E> criarComboBoxEstilizado(E[] itens) {
        JComboBox<E> combo = new JComboBox<E>(itens);
        estilizarComboBox(combo);
        return combo;
    }

    public static <E> void estilizarComboBox(JComboBox<E> combo) {
        combo.setFont(FONTE_TEXTO);
        combo.setBackground(COR_FUNDO_CAMPO);
        combo.setForeground(COR_TEXTO_CLARO);
        combo.setBorder(BorderFactory.createLineBorder(COR_DOURADA_IMPERIAL, 1));
        combo.setOpaque(true);
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        combo.setUI(new BasicComboBoxUI() {
            @Override
            public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds,
                                                    boolean hasFocus) {
                g.setColor(COR_FUNDO_CAMPO);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            protected JButton createArrowButton() {
                JButton seta = new BasicArrowButton(
                        BasicArrowButton.SOUTH,
                        COR_MARROM_COURO,
                        COR_MARROM_HOVER,
                        COR_DOURADA_IMPERIAL,
                        COR_DOURADA_IMPERIAL);
                seta.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COR_DOURADA_IMPERIAL));
                return seta;
            }
        });

        combo.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel item = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                item.setFont(FONTE_TEXTO);
                item.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                item.setOpaque(true);
                if (isSelected && index >= 0) {
                    item.setBackground(COR_SELECAO);
                    item.setForeground(COR_DOURADA_IMPERIAL);
                } else {
                    item.setBackground(COR_FUNDO_CAMPO);
                    item.setForeground(COR_TEXTO_CLARO);
                }
                return item;
            }
        });
        // O valor selecionado tambem e desenhado pelo renderer sobre fundo escuro.
        // A propriedade nao depende da cor de texto do LookAndFeel do Windows.
        combo.setFocusable(false);
    }

    /** Cria uma JTextArea com estética de pergaminho escuro e borda dourada. */
    public static JTextArea criarTextAreaEstilizada(String textoInicial, int linhas, int colunas) {
        JTextArea area = (colunas > 0)
                ? new JTextArea(textoInicial, linhas, colunas)
                : new JTextArea(textoInicial, linhas, 1);
        estilizarTextArea(area);
        return area;
    }

    public static void estilizarTextArea(JTextArea area) {
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(FONTE_TEXTO);
        area.setForeground(COR_TEXTO_CLARO);
        area.setBackground(COR_FUNDO_CAMPO);
        area.setCaretColor(COR_DOURADA_IMPERIAL);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_DOURADA_IMPERIAL, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    /** Estiliza JScrollPane e seu viewport para não exibir fundos brancos nas bordas. */
    public static JScrollPane criarScrollPaneEstilizado(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createLineBorder(COR_DOURADA_IMPERIAL, 1));
        scroll.getViewport().setBackground(COR_FUNDO_CAMPO);
        scroll.setBackground(COR_FUNDO_CAMPO);
        return scroll;
    }

    /** Estiliza uma JTable (cabeçalho marrom/dourado e linhas alternadas em couro escuro). */
    public static void estilizarTabela(JTable tabela) {
        tabela.setFont(FONTE_TEXTO);
        tabela.setForeground(COR_TEXTO_CLARO);
        tabela.setBackground(COR_FUNDO_CAMPO);
        tabela.setSelectionBackground(COR_SELECAO);
        tabela.setSelectionForeground(COR_DOURADA_IMPERIAL);
        tabela.setGridColor(COR_BORDA_TEXTO);
        tabela.setRowHeight(30);
        tabela.setFillsViewportHeight(true);

        DefaultTableCellRenderer renderizadorCelula = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel celula = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                celula.setFont(FONTE_TEXTO);
                celula.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (isSelected) {
                    celula.setBackground(COR_SELECAO);
                    celula.setForeground(COR_DOURADA_IMPERIAL);
                } else {
                    celula.setBackground(row % 2 == 0 ? COR_FUNDO_CAMPO : COR_FUNDO_LINHA_ALT);
                    celula.setForeground(COR_TEXTO_CLARO);
                }
                return celula;
            }
        };

        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderizadorCelula);
        }

        JTableHeader cabecalho = tabela.getTableHeader();
        if (cabecalho != null) {
            cabecalho.setReorderingAllowed(false);
            cabecalho.setDefaultRenderer(new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    JLabel titulo = (JLabel) super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);
                    titulo.setFont(FONTE_TEXTO_NEGRITO);
                    titulo.setHorizontalAlignment(SwingConstants.LEFT);
                    titulo.setBackground(COR_MARROM_COURO);
                    titulo.setForeground(COR_DOURADA_IMPERIAL);
                    titulo.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 1, COR_DOURADA_IMPERIAL),
                            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
                    return titulo;
                }
            });
        }
    }
}