package br.herdeiros.editor;

import br.herdeiros.mapa.ArquivoMapa;
import br.herdeiros.mapa.MapaDados;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.SwingUtilities;

/** Aplicativo exclusivo de desenvolvimento, separado da interface do jogo. */
public class JanelaEditorMapa extends JFrame {
    private static final long serialVersionUID = 1L;
    private enum Ferramenta { SELECIONAR, SIMPLES, DUPLA, CONECTAR, DESCONECTAR, EXCLUIR }
    private final Path destino = Paths.get("assets", "mapa_paradas.csv");
    private MapaDados mapa;
    private Ferramenta ferramenta = Ferramenta.SELECIONAR;
    private int selecionado = -1;
    private final JLabel estado = new JLabel("Selecione uma ferramenta para editar o mapa.");
    private final PainelEdicao painel;
    private final JSpinner raioEditor = new JSpinner(new SpinnerNumberModel(3.0, 0.8, 15.0, 0.2));

    public JanelaEditorMapa() {
        super("Editor do mapa - Herdeiros de Khan");
        try {
            mapa = ArquivoMapa.carregar(destino);
        } catch (IOException erro) {
            mapa = new MapaDados();
        }
        painel = new PainelEdicao();
        JPanel lateral = new JPanel(new GridLayout(0, 1, 4, 4));
        lateral.setPreferredSize(new Dimension(245, 800));
        adicionarFerramenta(lateral, "Selecionar / arrastar", Ferramenta.SELECIONAR);
        adicionarFerramenta(lateral, "Criar parada simples", Ferramenta.SIMPLES);
        adicionarFerramenta(lateral, "Criar parada dupla", Ferramenta.DUPLA);
        adicionarFerramenta(lateral, "Conectar (2 cliques)", Ferramenta.CONECTAR);
        adicionarFerramenta(lateral, "Desconectar (2 cliques)", Ferramenta.DESCONECTAR);
        adicionarFerramenta(lateral, "Excluir parada", Ferramenta.EXCLUIR);
        JButton salvar = new JButton("Salvar mapa CSV");
        salvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                try {
                    ArquivoMapa.salvar(mapa, destino);
                    estado.setText("Salvo em " + destino);
                } catch (IOException erro) {
                    estado.setText("Falha ao salvar: " + erro.getMessage());
                }
            }
        });
        lateral.add(salvar);
        lateral.add(new JLabel("Raio clicável da parada selecionada (%):"));
        raioEditor.setToolTipText("Raio relativo à menor dimensão da imagem");
        raioEditor.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evento) {
                if (selecionado >= 0) {
                    double novoRaio = ((Number) raioEditor.getValue()).doubleValue() / 100.0;
                    mapa.alterarRaio(selecionado, novoRaio);
                    painel.repaint();
                }
            }
        });
        lateral.add(raioEditor);
        JButton recarregar = new JButton("Recarregar CSV");
        recarregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                try {
                    mapa = ArquivoMapa.carregar(destino);
                    selecionado = -1;
                    painel.repaint();
                    estado.setText("Mapa recarregado.");
                } catch (IOException erro) {
                    estado.setText("Erro no CSV: " + erro.getMessage());
                }
            }
        });
        lateral.add(recarregar);
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(estado, BorderLayout.CENTER);
        rodape.add(new JLabel("0 = Karakorum (arrastável, não removível)"), BorderLayout.SOUTH);
        setLayout(new BorderLayout(6, 6));
        add(painel, BorderLayout.CENTER);
        add(lateral, BorderLayout.EAST);
        add(rodape, BorderLayout.SOUTH);
        setSize(1120, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void adicionarFerramenta(JPanel lateral, String texto, final Ferramenta nova) {
        JButton botao = new JButton(texto);
        botao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                ferramenta = nova;
                selecionado = -1;
                estado.setText("Ferramenta: " + texto);
            }
        });
        lateral.add(botao);
    }

    private class PainelEdicao extends JPanel {
        private static final long serialVersionUID = 1L;
        private BufferedImage imagem;
        private int left;
        private int top;
        private int width;
        private int height;

        public PainelEdicao() {
            setBackground(new Color(32, 37, 44));
            try {
                imagem = ImageIO.read(new File("assets/tabuleiro.jpg"));
            } catch (IOException erro) {
                estado.setText("Imagem não encontrada em assets/tabuleiro.jpg");
            }
            setPreferredSize(new Dimension(820, 820));
            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent evento) {
                    clicar(evento);
                }

                @Override
                public void mouseDragged(MouseEvent evento) {
                    if (ferramenta == Ferramenta.SELECIONAR && selecionado >= 0) {
                        double[] pos = converter(evento.getX(), evento.getY());
                        if (pos != null) {
                            mapa.mover(selecionado, pos[0], pos[1]);
                            repaint();
                        }
                    }
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private double[] converter(int x, int y) {
            if (width <= 0 || height <= 0 || x < left || y < top
                    || x > left + width || y > top + height) {
                return null;
            }
            return new double[] {(double) (x - left) / width, (double) (y - top) / height};
        }

        private int encontrar(double[] pos) {
            return mapa.encontrarParada(pos[0], pos[1], width, height);
        }

        private void clicar(MouseEvent evento) {
            double[] pos = converter(evento.getX(), evento.getY());
            if (pos == null) {
                return;
            }
            int id = encontrar(pos);
            if (ferramenta == Ferramenta.SIMPLES || ferramenta == Ferramenta.DUPLA) {
                int novoId = mapa.proximoId();
                mapa.adicionar(novoId, pos[0], pos[1], ferramenta == Ferramenta.DUPLA);
                estado.setText("Criada parada " + novoId);
            } else if (ferramenta == Ferramenta.EXCLUIR) {
                if (mapa.remover(id)) {
                    estado.setText("Parada " + id + " removida e conexões limpas.");
                }
            } else if (ferramenta == Ferramenta.SELECIONAR) {
                selecionado = id;
                if (id >= 0) {
                    raioEditor.setValue(mapa.getPonto(id).getRaio() * 100.0);
                    estado.setText("Parada " + id + " selecionada; arraste ou ajuste o raio.");
                }
            } else if (id >= 0) {
                if (selecionado < 0) {
                    selecionado = id;
                    estado.setText("Selecione a segunda parada.");
                } else {
                    boolean alterado;
                    if (ferramenta == Ferramenta.CONECTAR) {
                        alterado = mapa.conectar(selecionado, id);
                    } else {
                        alterado = mapa.desconectar(selecionado, id);
                    }
                    if (alterado) {
                        estado.setText("Conexão alterada.");
                    } else {
                        estado.setText("Conexão não alterada.");
                    }
                    selecionado = -1;
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics desenho) {
            super.paintComponent(desenho);
            if (imagem == null) {
                return;
            }
            Graphics2D g = (Graphics2D) desenho.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double escala = Math.min((double) getWidth() / imagem.getWidth(),
                    (double) getHeight() / imagem.getHeight());
            width = (int) Math.round(imagem.getWidth() * escala);
            height = (int) Math.round(imagem.getHeight() * escala);
            left = (getWidth() - width) / 2;
            top = (getHeight() - height) / 2;
            g.drawImage(imagem, left, top, width, height, null);
            g.setStroke(new BasicStroke(3));
            g.setColor(new Color(240, 188, 41, 190));
            for (MapaDados.Ligacao ligacao : mapa.getLigacoes()) {
                MapaDados.Ponto a = mapa.getPonto(ligacao.getOrigem());
                MapaDados.Ponto b = mapa.getPonto(ligacao.getDestino());
                int ax = left + (int) Math.round(a.getX() * width);
                int ay = top + (int) Math.round(a.getY() * height);
                int bx = left + (int) Math.round(b.getX() * width);
                int by = top + (int) Math.round(b.getY() * height);
                g.drawLine(ax, ay, bx, by);
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            for (MapaDados.Ponto ponto : mapa.getPontos().values()) {
                int x = left + (int) Math.round(ponto.getX() * width);
                int y = top + (int) Math.round(ponto.getY() * height);
                if (ponto.getId() == selecionado) {
                    g.setColor(Color.YELLOW);
                } else if (ponto.getId() == 0) {
                    g.setColor(new Color(250, 190, 30));
                } else if (ponto.isDupla()) {
                    g.setColor(new Color(80, 180, 230));
                } else {
                    g.setColor(Color.WHITE);
                }
                int raioPixels = Math.max(2, (int) Math.round(ponto.getRaio() * Math.min(width, height)));
                g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(),
                        g.getColor().getBlue(), 65));
                g.fillOval(x - raioPixels, y - raioPixels, raioPixels * 2, raioPixels * 2);
                g.setColor(Color.WHITE);
                g.drawOval(x - raioPixels, y - raioPixels, raioPixels * 2, raioPixels * 2);
                g.setColor(Color.BLACK);
                g.fillOval(x - 3, y - 3, 6, 6);
                g.setColor(Color.WHITE);
                g.drawString(String.valueOf(ponto.getId()), x + 5, y - 5);
            }
            g.dispose();
        }
    }
}
