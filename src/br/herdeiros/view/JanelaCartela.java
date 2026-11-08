package br.herdeiros.view;

import br.herdeiros.controller.ControladorCartela;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ResumoCartela;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * View da cartela do jogador da vez. Recebe notificacoes de mudancas pelo Observer
 * e consulta um resumo imutavel dos dados pela Facade do Model.
 */
public final class JanelaCartela extends JFrame implements ObservadorPartida {

    private static final long serialVersionUID = 1L;
    private final InterfacePartida partida;
    private final PainelCartela painelImagem;
    private final JLabel nome;
    private final JLabel tributos;
    private final JTextArea melhorias;

    public JanelaCartela(InterfacePartida partida) throws IOException {
        super("Herdeiros de Khan - Cartela individual");
        if (partida == null) {
            throw new IllegalArgumentException("Partida obrigatoria");
        }
        this.partida = partida;
        this.painelImagem = new PainelCartela();
        this.nome = new JLabel();
        this.tributos = new JLabel();
        this.melhorias = new JTextArea(4, 26);
        melhorias.setEditable(false);
        melhorias.setLineWrap(true);
        melhorias.setWrapStyleWord(true);

        JPanel cabecalho = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cabecalho.add(nome);
        cabecalho.add(tributos);

        JComboBox<String> tipos = new JComboBox<String>(
                new String[] {"ESPADA", "MOEDA", "YURT"});
        JButton simular = new JButton("+1 tributo (TESTE)");
        simular.addActionListener(new ControladorCartela(partida, tipos));

        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controles.add(new JLabel("Simulacao de desenvolvimento:"));
        controles.add(tipos);
        controles.add(simular);
        rodape.add(controles, BorderLayout.NORTH);
        rodape.add(new JScrollPane(melhorias), BorderLayout.CENTER);
        rodape.add(new JLabel("Esta simulacao nao representa uma acao oficial do jogo."),
                BorderLayout.SOUTH);

        setLayout(new BorderLayout(8, 8));
        add(cabecalho, BorderLayout.NORTH);
        add(painelImagem, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
        setSize(new Dimension(1050, 760));
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evento) {
                JanelaCartela.this.partida.removerObservador(JanelaCartela.this);
            }
        });
        partida.registrarObservador(this);
        atualizar(partida);
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        ResumoCartela resumo = partida.consultarCartelaJogadorAtual();
        nome.setText("Jogador: " + resumo.getNomeJogador()
                + " (" + resumo.getCorJogador() + ")  |  ");
        tributos.setText("Espadas: " + resumo.getEspadas()
                + "   Moedas: " + resumo.getMoedas()
                + "   Yurts: " + resumo.getYurts());
        painelImagem.mostrarCartela(partida.getIndiceCartelaJogador(resumo.getIndiceJogador()));

        StringBuilder texto = new StringBuilder("Melhorias adquiridas: ");
        if (resumo.getMelhorias().isEmpty()) {
            texto.append("nenhuma");
        } else {
            for (String descricao : resumo.getMelhorias()) {
                texto.append("\n- ");
                texto.append(descricao);
            }
        }
        melhorias.setText(texto.toString());
    }
}
