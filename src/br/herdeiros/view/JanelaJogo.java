package br.herdeiros.view;

import br.herdeiros.controller.ControladorTabuleiro;
import br.herdeiros.mapa.ArquivoMapa;
import br.herdeiros.mapa.MapaDados;
import br.herdeiros.model.InterfacePartida;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Jogo normal: carrega o mapa e não oferece comandos para editá-lo. */
public class JanelaJogo extends JFrame {
    private static final long serialVersionUID = 1L;

    public JanelaJogo() throws IOException {
        super("Herdeiros de Khan - Partida");
        InterfacePartida partida = new InterfacePartida();
        MapaDados mapa;
        java.nio.file.Path caminho = Paths.get("assets", "mapa_paradas.csv");
        if (Files.exists(caminho)) {
            mapa = ArquivoMapa.carregar(caminho);
        } else {
            mapa = new MapaDados();
        }
        if (!partida.carregarMapa(mapa)) {
            throw new IOException("O mapa não pôde ser carregado no Model.");
        }
        ControladorTabuleiro controlador = new ControladorTabuleiro(partida, mapa);
        PainelTabuleiro painel = new PainelTabuleiro(partida, mapa, controlador);
        JLabel estado = new JLabel("<html>Selecione uma parada cadastrada. Edite o mapa no EditorMapaMain.</html>");
        controlador.configurar(painel, estado);
        controlador.atualizarEstado();

        JPanel lateral = new JPanel(new BorderLayout(8, 8));
        lateral.setPreferredSize(new Dimension(260, 800));
        JPanel ferramentas = new JPanel(new GridLayout(0, 1, 6, 6));
        ferramentas.add(new JLabel("Movimentos (simulação de cavalos):"));
        final JComboBox<Integer> cavalos = new JComboBox<>(
                new Integer[] {1, 2, 3, 4, 5, 6});
        cavalos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                Integer valor = (Integer) cavalos.getSelectedItem();
                if (valor != null) {
                    controlador.configurarMovimentos(valor);
                    controlador.atualizarEstado();
                }
            }
        });
        ferramentas.add(cavalos);
        JButton passar = new JButton("Passar vez (sem mover)");
        passar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                controlador.passarVez();
            }
        });
        ferramentas.add(passar);
        ferramentas.add(new JLabel("Mapa carregado do arquivo CSV."));
        lateral.add(ferramentas, BorderLayout.NORTH);
        lateral.add(estado, BorderLayout.SOUTH);
        setLayout(new BorderLayout(8, 8));
        add(painel, BorderLayout.CENTER);
        add(lateral, BorderLayout.EAST);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 900);
        setLocationRelativeTo(null);
    }

    public static void iniciar() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new JanelaJogo().setVisible(true);
                } catch (IOException erro) {
                    erro.printStackTrace();
                }
            }
        });
    }
}
