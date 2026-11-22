package br.herdeiros.view;

import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ResumoCartaObjetivo;
import br.herdeiros.model.ResumoCartela;
import br.herdeiros.model.ResumoConselheiro;
import br.herdeiros.model.TipoTesouro;
import br.herdeiros.model.CartaConselheiroAberto;
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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/** Janela de Conselheiros, entregas e Cartas de Objetivo (Observer). */
public final class JanelaConselheiros extends JFrame implements ObservadorPartida {

    private static final long serialVersionUID = 1L;
    private final InterfacePartida partida;
    private final JLabel cabecalhoJogador;
    private final DefaultTableModel modeloConselheiros;
    private final JTable tabelaConselheiros;
    private final java.util.List<Integer> idsConselheiros = new java.util.ArrayList<Integer>();
    private final JPanel painelObjetivos;
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
                new Object[] {"Conselheiro", "Exigencia"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaConselheiros = new JTable(modeloConselheiros);
        EstiloImperial.estilizarTabela(tabelaConselheiros);

        painelObjetivos = EstiloImperial.criarPainelFundo(new GridLayout(1, 4, 10, 5));
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
                int idParada = idsConselheiros.get(linhaModelo);
                java.util.List<TipoTesouro> escolhidos = null;
                if (idParada == 23 || idParada == 33) {
                    escolhidos = escolherTresTesouros();
                    if (escolhidos == null) {
                        return;
                    }
                }
                if (JanelaConselheiros.this.partida.entregarTesourosConselheiro(idParada, escolhidos)) {
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

        JPanel centro = EstiloImperial.criarPainelFundo(new BorderLayout(0, 10));
        JPanel painelTabela = EstiloImperial.criarPainelFundo(new BorderLayout());
        painelTabela.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(EstiloImperial.COR_DOURADA_IMPERIAL),
                "Conselheiros de troca", 0, 0,
                EstiloImperial.FONTE_SUBTITULO, EstiloImperial.COR_DOURADA_IMPERIAL));
        painelTabela.add(EstiloImperial.criarScrollPaneEstilizado(tabelaConselheiros));
        centro.add(painelTabela, BorderLayout.CENTER);

        JPanel painelAbertos = EstiloImperial.criarPainelFundo(new BorderLayout());
        painelAbertos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(EstiloImperial.COR_DOURADA_IMPERIAL),
                "Objetivos abertos (1 voto cada)", 0, 0,
                EstiloImperial.FONTE_SUBTITULO, EstiloImperial.COR_DOURADA_IMPERIAL));
        painelAbertos.add(EstiloImperial.criarScrollPaneEstilizado(painelObjetivos));
        painelAbertos.setPreferredSize(new Dimension(0, 265));
        // Objetivos serao colocados abaixo dos controles de entrega de tesouros.

        // Nome do conselheiro destacado sem alterar as demais colunas.
        tabelaConselheiros.getColumnModel().getColumn(0).setCellRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public java.awt.Component getTableCellRendererComponent(
                            JTable tabela, Object valor, boolean selecionado,
                            boolean foco, int linha, int coluna) {
                        javax.swing.JLabel label = (javax.swing.JLabel) super.getTableCellRendererComponent(
                                tabela, valor, selecionado, foco, linha, coluna);
                        boolean ativo = valor != null && valor.toString().endsWith(" (Ativo)");
                        label.setForeground(ativo ? new java.awt.Color(255, 214, 85)
                                : EstiloImperial.COR_TEXTO_CLARO);
                        label.setBackground(selecionado
                                ? EstiloImperial.COR_SELECAO
                                : EstiloImperial.COR_FUNDO_CAMPO);
                        label.setFont(EstiloImperial.FONTE_TEXTO_NEGRITO);
                        return label;
                    }
                });
        tabelaConselheiros.getColumnModel().getColumn(0).setPreferredWidth(220);
        tabelaConselheiros.getColumnModel().getColumn(1).setPreferredWidth(300);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        acoes.setOpaque(false);
        acoes.add(entregarTesouros);

        JPanel rodape = new JPanel(new BorderLayout(4, 4));
        rodape.setOpaque(false);
        rodape.add(acoes, BorderLayout.NORTH);
        rodape.add(feedback, BorderLayout.SOUTH);

        JPanel inferior = EstiloImperial.criarPainelFundo(new BorderLayout(0, 8));
        JPanel painelEntrega = EstiloImperial.criarPainelFundo(new BorderLayout(4, 4));
        painelEntrega.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(EstiloImperial.COR_DOURADA_IMPERIAL),
                "Entrega de tesouros", 0, 0,
                EstiloImperial.FONTE_SUBTITULO, EstiloImperial.COR_DOURADA_IMPERIAL));
        painelEntrega.add(rodape, BorderLayout.CENTER);
        inferior.add(painelEntrega, BorderLayout.NORTH);
        inferior.add(painelAbertos, BorderLayout.CENTER);
        inferior.setPreferredSize(new Dimension(0, 435));
        centro.add(inferior, BorderLayout.SOUTH);
        fundo.add(cabecalhoJogador, BorderLayout.NORTH);
        fundo.add(centro, BorderLayout.CENTER);
        setContentPane(fundo);

        setSize(new Dimension(1080, 880));
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

    /** Uma unica janela com todas as quantidades; nao permite gastar o que nao possui. */
    private java.util.List<TipoTesouro> escolherTresTesouros() {
        java.util.Map<TipoTesouro, Integer> estoque =
                partida.consultarTesourosPorTipoJogadorAtual();
        java.util.Map<TipoTesouro, Integer> virtuais =
                partida.consultarTesourosVirtuaisJogadorAtual();
        int total = 0;
        for (TipoTesouro tipo : TipoTesouro.values()) {
            total += estoque.getOrDefault(tipo, 0) + virtuais.getOrDefault(tipo, 0);
        }
        if (total < 3) {
            JOptionPane.showMessageDialog(this,
                    "Voce precisa possuir pelo menos 3 tesouros para esta entrega.",
                    "Tesouros insuficientes", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        JPanel formulario = new JPanel(new BorderLayout(6, 8));
        formulario.add(new JLabel("Escolha exatamente 3 tesouros para entregar:"),
                BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(0, 2, 10, 6));
        java.util.Map<TipoTesouro, JSpinner> controles =
                new java.util.EnumMap<TipoTesouro, JSpinner>(TipoTesouro.class);
        for (TipoTesouro tipo : TipoTesouro.values()) {
            int fisicos = estoque.getOrDefault(tipo, 0);
            int virtuaisAtivos = virtuais.getOrDefault(tipo, 0);
            int disponiveis = fisicos + virtuaisAtivos;
            campos.add(new JLabel(tipo.getDescricao() + " (" + fisicos
                    + " reais + " + virtuaisAtivos + " virtuais):"));
            JSpinner contador = new JSpinner(new SpinnerNumberModel(0, 0, disponiveis, 1));
            controles.put(tipo, contador);
            campos.add(contador);
        }
        formulario.add(campos, BorderLayout.CENTER);
        JLabel validacao = new JLabel("Total selecionado: 0 de 3");
        formulario.add(validacao, BorderLayout.SOUTH);
        for (JSpinner contador : controles.values()) {
            contador.addChangeListener(new javax.swing.event.ChangeListener() { @Override public void stateChanged(javax.swing.event.ChangeEvent evento) { int selecionados = 0;
                for (JSpinner c : controles.values()) {
                    selecionados += (Integer) c.getValue();
                }
                validacao.setText("Total selecionado: " + selecionados + " de 3"); } });
        }

        while (true) {
            int resposta = JOptionPane.showConfirmDialog(this, formulario,
                    "Entrega de 3 tesouros", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (resposta != JOptionPane.OK_OPTION) {
                return null;
            }
            java.util.List<TipoTesouro> escolhidos =
                    new java.util.ArrayList<TipoTesouro>();
            for (TipoTesouro tipo : TipoTesouro.values()) {
                int quantidade = (Integer) controles.get(tipo).getValue();
                for (int i = 0; i < quantidade; i++) {
                    escolhidos.add(tipo);
                }
            }
            if (escolhidos.size() == 3) {
                return escolhidos;
            }
            validacao.setText("Selecione exatamente 3 tesouros (atual: "
                    + escolhidos.size() + ").");
            JOptionPane.showMessageDialog(this,
                    "Escolha exatamente 3 tesouros antes de confirmar.",
                    "Quantidade incorreta", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Quatro cartas publicas originais, na proporcao natural da arte. */
    private void atualizarImagensCartasAbertas() {
        painelObjetivos.removeAll();
        for (CartaConselheiroAberto carta : partida.consultarCartasAbertasAtivas()) {
            JPanel coluna = EstiloImperial.criarPainelFundo(new BorderLayout(2, 4));
            PainelImagemJava2D figura = new PainelImagemJava2D();
            figura.setPreferredSize(new Dimension(165, 215));
            
            figura.setToolTipText(carta.getDescricao());
            java.io.File arquivo = new java.io.File("assets/conselheiros_abertos/"
                    + carta.getArquivoImagem());
            try {
                java.awt.image.BufferedImage imagem = javax.imageio.ImageIO.read(arquivo);
                if (imagem != null) {
                    figura.setImagem(imagem);
                }
            } catch (java.io.IOException erro) {
                figura.setText("Imagem indisponivel");
            }
            JLabel descricao = new JLabel("<html><center>" + carta.getDescricao()
                    + "</center></html>", JLabel.CENTER);
            descricao.setForeground(EstiloImperial.COR_TEXTO_CLARO);
            coluna.add(figura, BorderLayout.CENTER);
            coluna.add(descricao, BorderLayout.SOUTH);
            painelObjetivos.add(coluna);
        }
        painelObjetivos.revalidate();
        painelObjetivos.repaint();
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        ResumoCartela cartela = partida.consultarCartelaJogadorAtual();
        int votos = partida.consultarVotosJogadorAtual();
        cabecalhoJogador.setText("Jogador da vez: " + cartela.getNomeJogador()
                + " (" + cartela.getCorJogador() + ")  |  Votos Kurultai atuais: " + votos);

        List<ResumoConselheiro> lista = partida.consultarConselheiros();
        modeloConselheiros.setRowCount(0);
        idsConselheiros.clear();
        java.util.List<ResumoConselheiro> ordenados = new java.util.ArrayList<>(lista);
        ordenados.sort(new java.util.Comparator<ResumoConselheiro>() {
            @Override public int compare(ResumoConselheiro a, ResumoConselheiro b) {
                return Boolean.compare(b.isJogadorPresente(), a.isJogadorPresente());
            }
        });
        for (ResumoConselheiro c : ordenados) {
            idsConselheiros.add(c.getIdParada());
            modeloConselheiros.addRow(new Object[] {
                    c.getNome() + (c.isJogadorPresente() ? " (Ativo)" : ""),
                    c.getDemanda()
            });
        }

        atualizarImagensCartasAbertas();

    }
}
