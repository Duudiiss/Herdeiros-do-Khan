package br.herdeiros.view;

import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ResumoCartaObjetivo;
import br.herdeiros.model.ResumoCartela;
import br.herdeiros.model.ResumoConselheiro;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/** Janela de Conselheiros, Tesouros, Pecas de Bonus e Cartas de Objetivo (Observer). */
public final class JanelaConselheiros extends JFrame implements ObservadorPartida {

    private static final long serialVersionUID = 1L;
    private final InterfacePartida partida;
    private final JLabel cabecalhoJogador;
    private final DefaultTableModel modeloConselheiros;
    private final JTable tabelaConselheiros;
    private final JTextArea painelObjetivos;
    private final JComboBox<String> seletorBonus;
    private final JComboBox<String> seletorSecreta;
    private final JButton confirmarSecreta;
    private final JButton usarBonus;
    private final JButton entregarTesouros;
    private final JLabel feedback;

    public JanelaConselheiros(InterfacePartida partida) {
        super("Herdeiros de Khan - Conselheiros e Objetivos");
        if (partida == null) {
            throw new IllegalArgumentException("Partida obrigatoria");
        }
        this.partida = partida;

        cabecalhoJogador = new JLabel();
        cabecalhoJogador.setFont(EstiloImperial.FONTE_TEXTO_NEGRITO);
        cabecalhoJogador.setForeground(EstiloImperial.COR_DOURADA_IMPERIAL);

        modeloConselheiros = new DefaultTableModel(
                new Object[] {"Parada", "Local", "Demanda", "Bonus", "Situacao"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaConselheiros = new JTable(modeloConselheiros);
        EstiloImperial.estilizarTabela(tabelaConselheiros);

        painelObjetivos = EstiloImperial.criarTextAreaEstilizada("", 7, 30);
        seletorBonus = EstiloImperial.criarComboBoxEstilizado(new String[] {"Nenhuma peca de bonus"});
        seletorSecreta = EstiloImperial.criarComboBoxEstilizado(new String[] {"Carta secreta definida"});

        confirmarSecreta = EstiloImperial.criarBotaoImperial("Escolher carta secreta");
        confirmarSecreta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = seletorSecreta.getSelectedIndex();
                if (idx >= 0 && JanelaConselheiros.this.partida.escolherConselheiroSecretoJogadorAtual(idx)) {
                    feedback.setText("Carta de conselheiro secreto confirmada!");
                }
            }
        });

        usarBonus = EstiloImperial.criarBotaoImperial("Usar peca de bonus");
        usarBonus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = seletorBonus.getSelectedIndex();
                if (idx >= 0 && JanelaConselheiros.this.partida.usarPecaBonusJogadorAtual(idx)) {
                    feedback.setText("Peca de bonus ativada!");
                } else {
                    feedback.setText("Nao foi possivel usar a peca de bonus selecionada.");
                }
            }
        });

        entregarTesouros = EstiloImperial.criarBotaoImperial("Entregar tesouros ao conselheiro");
        entregarTesouros.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int linha = tabelaConselheiros.getSelectedRow();
                if (linha < 0) {
                    feedback.setText("Selecione um conselheiro na tabela primeiro.");
                    return;
                }
                int linhaModelo = tabelaConselheiros.convertRowIndexToModel(linha);
                int idParada = Integer.parseInt(String.valueOf(modeloConselheiros.getValueAt(linhaModelo, 0)));
                if (JanelaConselheiros.this.partida.entregarTesourosConselheiro(idParada)) {
                    feedback.setText("Entrega realizada! Votos avancados na trilha do Kurultai.");
                } else {
                    feedback.setText("Entrega recusada: visite a parada dupla/Karakorum e tenha os tesouros.");
                }
            }
        });

        feedback = new JLabel("Visite uma parada dupla ou Karakorum para entregar tesouros.");
        feedback.setFont(EstiloImperial.FONTE_TEXTO);
        feedback.setForeground(EstiloImperial.COR_TEXTO_CLARO);

        JPanel fundo = EstiloImperial.criarPainelFundo(new BorderLayout(8, 8));
        fundo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel centro = new JPanel(new GridLayout(1, 2, 10, 0));
        centro.setOpaque(false);
        centro.add(EstiloImperial.criarScrollPaneEstilizado(tabelaConselheiros));
        centro.add(EstiloImperial.criarScrollPaneEstilizado(painelObjetivos));

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        acoes.setOpaque(false);
        acoes.add(entregarTesouros);
        acoes.add(seletorBonus);
        acoes.add(usarBonus);
        acoes.add(seletorSecreta);
        acoes.add(confirmarSecreta);

        JPanel rodape = new JPanel(new BorderLayout(4, 4));
        rodape.setOpaque(false);
        rodape.add(acoes, BorderLayout.NORTH);
        rodape.add(feedback, BorderLayout.SOUTH);

        fundo.add(cabecalhoJogador, BorderLayout.NORTH);
        fundo.add(centro, BorderLayout.CENTER);
        fundo.add(rodape, BorderLayout.SOUTH);
        setContentPane(fundo);

        setSize(new Dimension(980, 460));
        setLocationByPlatform(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                JanelaConselheiros.this.partida.removerObservador(JanelaConselheiros.this);
            }
        });

        partida.registrarObservador(this);
        atualizar(partida);
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        ResumoCartela cartela = partida.consultarCartelaJogadorAtual();
        int votos = partida.consultarVotosJogadorAtual();
        cabecalhoJogador.setText("Jogador da vez: " + cartela.getNomeJogador()
                + " (" + cartela.getCorJogador() + ")  |  Votos Kurultai atuais: " + votos);

        List<ResumoConselheiro> lista = partida.consultarConselheiros();
        modeloConselheiros.setRowCount(0);
        for (ResumoConselheiro c : lista) {
            String situacao = c.isPodeEntregar() ? "Pronto para entregar"
                    : (c.isJogadorPresente() ? "Faltam tesouros" : "Visite a parada");
            modeloConselheiros.addRow(new Object[] {
                    c.getIdParada(),
                    c.getNome(),
                    c.getDemanda(),
                    c.getBonusDisponivel(),
                    situacao
            });
        }

        ResumoCartaObjetivo obj = partida.consultarObjetivosJogadorAtual();
        StringBuilder sb = new StringBuilder();
        sb.append("=== TESOUROS NA MAO ===\n");
        sb.append(obj.getTesourosJogador().isEmpty() ? "Nenhum tesouro saqueado" : obj.getTesourosJogador());
        sb.append("\n\n=== CONSELHEIRO SECRETO ===\n");
        sb.append(obj.getCartaSecretaEscolhida());
        sb.append("\n\n=== CONSELHEIROS ABERTOS (1 VOTO) ===\n");
        for (String aberta : obj.getCartasAbertasDisponiveis()) {
            sb.append("• ").append(aberta).append("\n");
        }
        painelObjetivos.setText(sb.toString());

        seletorBonus.removeAllItems();
        if (obj.getBonusDisponiveisJogador().isEmpty()) {
            seletorBonus.addItem("Sem pecas de bonus");
            usarBonus.setEnabled(false);
        } else {
            for (String b : obj.getBonusDisponiveisJogador()) {
                seletorBonus.addItem(b);
            }
            usarBonus.setEnabled(true);
        }

        seletorSecreta.removeAllItems();
        if (obj.getOpcoesSecretasIniciais().isEmpty()) {
            seletorSecreta.addItem("Carta secreta ja escolhida");
            confirmarSecreta.setEnabled(false);
        } else {
            for (String op : obj.getOpcoesSecretasIniciais()) {
                seletorSecreta.addItem(op);
            }
            confirmarSecreta.setEnabled(true);
        }
    }
}