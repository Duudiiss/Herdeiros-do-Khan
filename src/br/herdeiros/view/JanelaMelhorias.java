package br.herdeiros.view;

import br.herdeiros.model.InterfacePartida;
import br.herdeiros.controller.ControladorMelhorias;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.ResumoMelhoria;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Mostra as pecas oficiais de melhorias por Java2D e compra com clique. */
public final class JanelaMelhorias extends JFrame implements ObservadorPartida {
    private static final long serialVersionUID = 1L;
    private final InterfacePartida partida;
    private final JLabel mensagem;
    private final PainelMercado painel;

    public JanelaMelhorias(InterfacePartida partida) {
        super("Herdeiros de Khan - Melhorias");
        this.partida = partida;
        mensagem = new JLabel("Clique numa melhoria disponivel para comprar por 2 moedas.");
        mensagem.setHorizontalAlignment(SwingConstants.CENTER);
        painel = new PainelMercado();
        setLayout(new BorderLayout());
        add(mensagem, BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);
        setSize(840, 300);
        setLocationByPlatform(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evento) {
                JanelaMelhorias.this.partida.removerObservador(JanelaMelhorias.this);
            }
        });
        partida.registrarObservador(this);
        atualizar(partida);
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        painel.repaint();
    }

    private final class PainelMercado extends JPanel {
        private static final long serialVersionUID = 1L;

        PainelMercado() {
            setPreferredSize(new Dimension(800, 220));
            addMouseListener(new ControladorMelhorias(partida, mensagem));
        }

        @Override
        protected void paintComponent(Graphics grafico) {
            super.paintComponent(grafico);
            Graphics2D g = (Graphics2D) grafico.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            List<ResumoMelhoria> lista = partida.consultarMercadoMelhorias();
            int largura = getWidth() / lista.size();
            for (int i = 0; i < lista.size(); i++) {
                int x = i * largura;
                g.setColor(new Color(230, 225, 210));
                g.fillRect(x + 4, 3, largura - 8, getHeight() - 8);
                ResumoMelhoria melhoria = lista.get(i);
                if (melhoria == null) {
                    g.setColor(Color.DARK_GRAY);
                    g.drawString("Vazio - aguarda Khan", x + 12, getHeight() / 2);
                    continue;
                }
                try {
                    BufferedImage imagem = ImageIO.read(Paths.get("assets", "melhorias",
                            melhoria.getCodigo() + ".jpg").toFile());
                    if (imagem != null) {
                        g.drawImage(imagem, x + 10, 10, largura - 20,
                                getHeight() - 65, null);
                    }
                } catch (IOException erro) {
                    g.setColor(Color.DARK_GRAY);
                    g.drawString(melhoria.getCodigo(), x + 12, getHeight() / 2);
                }
                g.setColor(Color.BLACK);
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g.drawString(melhoria.getDescricao(), x + 9, getHeight() - 35);
                g.drawString("2 moedas | " + nomeColuna(melhoria.getColuna()),
                        x + 9, getHeight() - 16);
            }
            g.dispose();
        }

        private String nomeColuna(int coluna) {
            if (coluna == 0) {
                return "vermelha";
            }
            if (coluna == 1) {
                return "verde";
            }
            return "amarela";
        }
    }
}
