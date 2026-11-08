package br.herdeiros.view;

import br.herdeiros.controller.ControladorAtivacao;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ResumoCartela;
import br.herdeiros.model.ResumoAtivacao;
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
    private final JLabel resumoAtivacao;
    private final JButton[] botoesAtivacao;

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
        this.resumoAtivacao = new JLabel();
        this.botoesAtivacao = new JButton[4];
        melhorias.setEditable(false);
        melhorias.setLineWrap(true);
        melhorias.setWrapStyleWord(true);

        JPanel cabecalho = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cabecalho.add(nome);
        cabecalho.add(tributos);

        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] nomesColunas = {"Vermelha", "Verde", "Amarela", "Branca"};

        for (int i = 0; i < botoesAtivacao.length; i++) {
            botoesAtivacao[i] = new JButton("Ativar " + nomesColunas[i]);
            botoesAtivacao[i].addActionListener(
                    new ControladorAtivacao(partida, i));
            controles.add(botoesAtivacao[i]);
        }

        JPanel areaAtivacao = new JPanel(new BorderLayout(4, 4));
        areaAtivacao.add(controles, BorderLayout.NORTH);
        areaAtivacao.add(resumoAtivacao, BorderLayout.SOUTH);
        rodape.add(areaAtivacao, BorderLayout.NORTH);
        rodape.add(new JScrollPane(melhorias), BorderLayout.CENTER);

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

        ResumoAtivacao ativacao = partida.consultarAtivacaoJogadorAtual();
        String[] nomesColunas = {"Vermelha", "Verde", "Amarela", "Branca"};
        for (int i = 0; i < botoesAtivacao.length; i++) {
            botoesAtivacao[i].setEnabled(ativacao.podeAtivar(i));
            botoesAtivacao[i].setText("Ativar " + nomesColunas[i]
                    + " (" + ativacao.getCavalosDaColuna(i) + " cavalos)"
                    + (ativacao.estaOcupada(i) ? " - usada" : ""));
        }
        if (ativacao.getColunaAtiva() < 0) {
            resumoAtivacao.setText("Ative uma coluna para liberar os movimentos do turno.");
        } else {
            resumoAtivacao.setText("Coluna ativa: "
                    + nomesColunas[ativacao.getColunaAtiva()]
                    + ". Cavalos restantes: " + ativacao.getMovimentosRestantes()
                    + ". Punhos: " + ativacao.getPunhosDaColuna()
                    + ". Acoes Khan/melhoria: pendentes de integracao.");
        }

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
