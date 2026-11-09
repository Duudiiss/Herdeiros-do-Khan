package br.herdeiros.editor;

import br.herdeiros.layout.LayoutMarcadores;
import br.herdeiros.layout.LayoutMarcadores.Posicao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Editor de pontos sobre imagens; salva as coordenadas em arquivos CSV. */
public final class JanelaEditorMarcadores extends JFrame {
    private static final long serialVersionUID = 1L;
    private final JComboBox<String> seletorTela;
    private final JComboBox<String> seletorPonto;
    private final JLabel mensagem;
    private final PainelEditor painel;
    private final Runnable aoSalvar;
    private BufferedImage imagem;
    private LayoutMarcadores layout;
    private boolean alterado;
    private boolean restaurandoSelecao;
    private int indiceCarregado = -1;

    public JanelaEditorMarcadores(Runnable aoSalvar) throws IOException {
        super("Herdeiros de Khan - Editor de marcadores");
        this.aoSalvar = aoSalvar;
        seletorTela = new JComboBox<String>(new String[] {
            "Tabuleiro", "Altani", "Jochi", "Chagatai", "Tolui", "Ogedei"
        });
        seletorPonto = new JComboBox<String>();
        mensagem = new JLabel("Escolha o ponto, clique na imagem e salve.");
        mensagem.setHorizontalAlignment(SwingConstants.CENTER);
        painel = new PainelEditor();
        JPanel comandos = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel superior = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JPanel inferior = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        superior.add(new JLabel("Imagem:"));
        superior.add(seletorTela);
        superior.add(new JLabel("Posicao:"));
        superior.add(seletorPonto);
        comandos.add(superior);

        JButton proximo = new JButton("Proximo ponto");
        proximo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                avancarSelecao();
            }
        });
        inferior.add(proximo);
        JButton remover = new JButton("Remover ponto");
        remover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                String chave = (String) seletorPonto.getSelectedItem();
                if (chave != null) {
                    layout.remover(chave);
                    alterado = true;
                    painel.repaint();
                    mensagem.setText("Ponto removido. Clique em Salvar para confirmar.");
                }
            }
        });
        inferior.add(remover);
        JButton salvar = new JButton("Salvar CSV");
        salvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                salvarLayout();
            }
        });
        inferior.add(salvar);
        comandos.add(inferior);

        setLayout(new BorderLayout(5, 5));
        add(comandos, BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);
        add(mensagem, BorderLayout.SOUTH);
        setSize(new Dimension(1100, 900));
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        seletorTela.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                trocarImagem();
            }
        });
        seletorPonto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                painel.repaint();
            }
        });
        trocarImagem();
    }

    private void trocarImagem() {
        if (restaurandoSelecao) {
            return;
        }
        if (alterado) {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Ha marcacoes nao salvas. Salvar antes de mudar de imagem?",
                    "Marcacoes pendentes", JOptionPane.YES_NO_CANCEL_OPTION);
            if (resposta == JOptionPane.CANCEL_OPTION
                    || resposta == JOptionPane.CLOSED_OPTION) {
                if (indiceCarregado >= 0) {
                    restaurandoSelecao = true;
                    seletorTela.setSelectedIndex(indiceCarregado);
                    restaurandoSelecao = false;
                }
                return;
            }
            if (resposta == JOptionPane.YES_OPTION) {
                salvarLayout();
            }
        }
        int indice = seletorTela.getSelectedIndex();
        try {
            if (indice == 0) {
                layout = new LayoutMarcadores(LayoutMarcadores.caminhoTabuleiro());
                imagem = ImageIO.read(new File("assets/tabuleiro.jpg"));
            } else {
                layout = new LayoutMarcadores(LayoutMarcadores.caminhoCartela(indice));
                imagem = ImageIO.read(new File("assets/cartelas/cartela" + indice + ".jpg"));
            }
            seletorPonto.removeAllItems();
            List<String> chaves = indice == 0
                    ? LayoutMarcadores.chavesTabuleiro() : LayoutMarcadores.chavesCartela();
            for (String chave : chaves) {
                seletorPonto.addItem(chave);
            }
            alterado = false;
            indiceCarregado = indice;
            painel.repaint();
            mensagem.setText("Escolha um ponto e clique na posicao exata na imagem. Depois salve o CSV.");
        } catch (IOException erro) {
            JOptionPane.showMessageDialog(this, "Nao foi possivel carregar a imagem: "
                    + erro.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void avancarSelecao() {
        int atual = seletorPonto.getSelectedIndex();
        if (atual >= 0 && atual < seletorPonto.getItemCount() - 1) {
            seletorPonto.setSelectedIndex(atual + 1);
        }
    }

    private void salvarLayout() {
        if (layout == null) {
            return;
        }
        try {
            layout.salvar();
            alterado = false;
            mensagem.setText("CSV salvo. As posicoes serao usadas na proxima atualizacao das janelas.");
            if (aoSalvar != null) {
                aoSalvar.run();
            }
        } catch (IOException erro) {
            JOptionPane.showMessageDialog(this, "Falha ao salvar CSV: " + erro.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private final class PainelEditor extends JPanel {
        private static final long serialVersionUID = 1L;
        private int origemX;
        private int origemY;
        private int largura;
        private int altura;

        PainelEditor() {
            setBackground(new Color(38, 38, 42));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evento) {
                    if (imagem == null || layout == null || largura <= 0 || altura <= 0) {
                        return;
                    }
                    String chave = (String) seletorPonto.getSelectedItem();
                    if (chave == null) {
                        return;
                    }
                    double x = (evento.getX() - origemX) / (double) largura;
                    double y = (evento.getY() - origemY) / (double) altura;
                    if (x < 0 || x > 1 || y < 0 || y > 1) {
                        return;
                    }
                    layout.marcar(chave, x, y);
                    alterado = true;
                    mensagem.setText("Marcado " + chave + ". Salve ou marque o proximo ponto.");
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graficos) {
            super.paintComponent(graficos);
            if (imagem == null) {
                return;
            }
            Graphics2D g = (Graphics2D) graficos.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            double escala = Math.min(getWidth() / (double) imagem.getWidth(),
                    getHeight() / (double) imagem.getHeight());
            largura = (int) Math.round(imagem.getWidth() * escala);
            altura = (int) Math.round(imagem.getHeight() * escala);
            origemX = (getWidth() - largura) / 2;
            origemY = (getHeight() - altura) / 2;
            g.drawImage(imagem, origemX, origemY, largura, altura, null);
            String selecionada = (String) seletorPonto.getSelectedItem();
            if (layout != null) {
                for (Map.Entry<String, Posicao> item : layout.consultarTodas().entrySet()) {
                    Posicao ponto = item.getValue();
                    int x = origemX + (int) Math.round(ponto.getX() * largura);
                    int y = origemY + (int) Math.round(ponto.getY() * altura);
                    boolean destaque = item.getKey().equals(selecionada);
                    int raio = destaque ? 9 : 5;
                    g.setColor(destaque ? new Color(255, 70, 50) : new Color(30, 200, 80));
                    g.fillOval(x - raio, y - raio, raio * 2, raio * 2);
                    g.setColor(Color.WHITE);
                    g.drawOval(x - raio, y - raio, raio * 2, raio * 2);
                    if (destaque) {
                        g.setFont(new Font("SansSerif", Font.BOLD, 13));
                        g.drawString(item.getKey(), x + 12, y - 8);
                    }
                }
            }
            g.dispose();
        }
    }
}
