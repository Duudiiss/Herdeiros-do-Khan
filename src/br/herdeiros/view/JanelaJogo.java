
package br.herdeiros.view;

import br.herdeiros.controller.ControladorTabuleiro;
import br.herdeiros.mapa.ArquivoMapa;
import br.herdeiros.mapa.MapaDados;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.GerenciadorPartida;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.Cor;
import br.herdeiros.model.Personagem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class JanelaJogo extends JPanel implements ObservadorPartida {

    private static final long serialVersionUID = 1L;

    // Componentes da partida
    private InterfacePartida partida;
    private MapaDados mapa;
    private ControladorTabuleiro controlador;
    private PainelTabuleiro painel;

    // Componentes da interface
    private JLabel indicadorVez;
    private JTextArea estado;


    private JButton passar;
    private JButton abrirCartela;
    private JButton abrirCidades;
    private JanelaCartela janelaCartela;
    private JanelaCidades janelaCidades;

    public JanelaJogo() {

        configurarInterface();
    }

    /**
     * Prepara a estrutura visual da janela.
     */
    private void configurarInterface() {

        setLayout(new BorderLayout(8, 8));

        indicadorVez = new JLabel();
        indicadorVez.setHorizontalAlignment(JLabel.CENTER);

        estado = new JTextArea(
            "Configure uma partida para começar."
        );

        estado.setEditable(false);
        estado.setLineWrap(true);
        estado.setWrapStyleWord(true);
        estado.setOpaque(false);
        estado.setRows(3);

        JPanel lateral = new JPanel(
            new BorderLayout(8, 8)
        );

        lateral.setPreferredSize(new Dimension(240, 0));

        JPanel ferramentas = new JPanel(
            new GridLayout(0, 1, 6, 6)
        );

        ferramentas.add(new JLabel(
            "<html>Movimentacao: ative uma<br>coluna na cartela individual.</html>"
        ));

        passar = new JButton("Passar turno");

        passar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {

                if (controlador != null) {
                    controlador.passarVez();
                }
            }
        });

        ferramentas.add(passar);

        abrirCartela = new JButton("Abrir cartela do jogador");
        abrirCartela.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                abrirCartelaDoJogador();
            }
        });
        ferramentas.add(abrirCartela);

        abrirCidades = new JButton("Consultar cidades abertas");
        abrirCidades.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                abrirJanelaCidades();
            }
        });
        ferramentas.add(abrirCidades);

        ferramentas.add(new JLabel(
            "<html>Mapa carregado do<br>arquivo CSV.</html>"
        ));

        lateral.add(indicadorVez, BorderLayout.CENTER);
        lateral.add(ferramentas, BorderLayout.NORTH);
        lateral.add(estado, BorderLayout.SOUTH);

        add(lateral, BorderLayout.EAST);
    }

    /** Janela informativa das cidades disponiveis, atualizada via Observer. */
    private void abrirJanelaCidades() {
        if (partida == null) {
            return;
        }
        if (janelaCidades != null && janelaCidades.isDisplayable()) {
            janelaCidades.toFront();
            return;
        }
        janelaCidades = new JanelaCidades(partida);
        janelaCidades.setVisible(true);
    }

    /** Abre uma cartela que acompanha automaticamente o jogador da vez. */
    private void abrirCartelaDoJogador() {
        if (partida == null) {
            return;
        }
        if (janelaCartela != null && janelaCartela.isDisplayable()) {
            janelaCartela.toFront();
            return;
        }
        try {
            janelaCartela = new JanelaCartela(partida);
            janelaCartela.setVisible(true);
        } catch (IOException erro) {
            JOptionPane.showMessageDialog(this,
                    "Nao foi possivel abrir as imagens das cartelas: " + erro.getMessage(),
                    "Erro ao abrir cartela", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicializa uma nova partida.
     */
    public void iniciarPartida(String[] nomes, Cor[] cores) throws IOException {
        iniciarPartida(nomes, cores, null, false);
    }

    public void iniciarPartida(String[] nomes, Cor[] cores,
                              Personagem[] personagens) throws IOException {
        iniciarPartida(nomes, cores, personagens, true);
    }

    private void iniciarPartida(String[] nomes, Cor[] cores,
                               Personagem[] personagens, boolean ordemAleatoria) throws IOException {

        if (janelaCidades != null) {
            janelaCidades.dispose();
            janelaCidades = null;
        }

        if (janelaCartela != null) {
            janelaCartela.dispose();
            janelaCartela = null;
        }

        // Criação do Model
        if (partida != null) {
            partida.removerObservador(this);
            if (painel != null) {
                partida.removerObservador(painel);
                remove(painel);
            }
        }
        partida = GerenciadorPartida.getInstancia()
                .iniciarPartida(nomes, cores, personagens, ordemAleatoria);

        // Carregamento do mapa
        Path caminho = Paths.get(
            "assets",
            "mapa_paradas.csv"
        );

        if (Files.exists(caminho)) {
            mapa = ArquivoMapa.carregar(caminho);
        } else {
            mapa = new MapaDados();
        }

        if (!partida.carregarMapa(mapa)) {
            throw new IOException(
                "O mapa não pôde ser carregado no Model."
            );
        }

        // Criação do Controller
        controlador = new ControladorTabuleiro(
            partida,
            mapa
        );

        // Criação da View do tabuleiro
        painel = new PainelTabuleiro(partida, mapa, controlador);

        controlador.configurar(
            painel,
            estado,
            indicadorVez
        );
        controlador.habilitarAtivacaoReal();

        partida.registrarObservador(this);
        partida.registrarObservador(painel);
        controlador.atualizarEstado();

        // Adiciona o painel à interface
        add(painel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        if (controlador != null) {
            controlador.atualizarEstado();
        }
    }
}
