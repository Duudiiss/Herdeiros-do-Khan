package br.herdeiros.view;

import br.herdeiros.model.DestinoKhan;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.ObservadorPartida;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** Ferramentas opcionais para testes manuais, sem alterar o formato dos saves. */
public final class JanelaDesenvolvedor extends JDialog implements ObservadorPartida {
    private static final long serialVersionUID = 1L;

    private final InterfacePartida partida;
    private final JLabel situacao = new JLabel();
    private final JComboBox<String> jogadores;
    private final JComboBox<String> recursos = new JComboBox<String>(
            new String[] {"MOEDA", "ESPADA", "YURT", "CAVALO", "PUNHO",
                    "MOEDA_VIRTUAL", "ESPADA_VIRTUAL", "YURT_VIRTUAL",
                    "CAVALO_VIRTUAL", "PUNHO_VIRTUAL",
                    "TESOURO_ESPECIARIAS", "TESOURO_SEDA", "TESOURO_PELES",
                    "TESOURO_FERRO", "TESOURO_GADO",
                    "BONUS_VOTO_IMEDIATO", "BONUS_DUAS_ESPADAS",
                    "BONUS_DUAS_MOEDAS", "BONUS_DOIS_YURTS",
                    "BONUS_TRES_MOVIMENTOS_UM_PUNHO", "BONUS_DOIS_PUNHOS",
                    "BONUS_ESCOLHER_TESOURO"});
    private final JSpinner variacao = new JSpinner(new SpinnerNumberModel(1, -999, 999, 1));
    private final JSpinner votos = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JSpinner pontosKhan = new JSpinner(new SpinnerNumberModel(1, -999, 999, 1));
    private final JComboBox<DestinoKhan> destinos = new JComboBox<DestinoKhan>(DestinoKhan.values());

    public JanelaDesenvolvedor(Window dono, InterfacePartida partida) {
        super(dono, "Ferramentas de desenvolvedor", ModalityType.MODELESS);
        if (partida == null) {
            throw new IllegalArgumentException("Partida obrigatoria");
        }
        this.partida = partida;
        String[] nomes = new String[partida.getQuantidadeJogadores()];
        for (int i = 0; i < nomes.length; i++) {
            nomes[i] = (i + 1) + " - " + partida.getNomeJogador(i);
        }
        jogadores = new JComboBox<String>(nomes);
        jogadores.setSelectedIndex(partida.getIndiceJogadorAtual());

        JPanel conteudo = new JPanel(new BorderLayout(8, 8));
        conteudo.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JPanel secoes = new JPanel(new GridLayout(0, 1, 6, 10));
        JPanel escolha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        escolha.add(new JLabel("Jogador:"));
        escolha.add(jogadores);
        secoes.add(escolha);

        JPanel tributos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tributos.setBorder(BorderFactory.createTitledBorder("Recursos do jogador"));
        tributos.add(recursos);
        tributos.add(new JLabel("Variacao (+/-):"));
        tributos.add(variacao);
        JButton ajustarRecurso = new JButton("Aplicar recurso");
        ajustarRecurso.addActionListener(evento -> {
            String escolhido = (String) recursos.getSelectedItem();
            int quantidade = (Integer) variacao.getValue();
            if ("MOEDA".equals(escolhido) || "ESPADA".equals(escolhido) || "YURT".equals(escolhido)) {
                executar(partida.desenvolvimentoAjustarTributo(jogadores.getSelectedIndex(),
                        escolhido, quantidade));
            } else if (escolhido.startsWith("TESOURO_") || escolhido.startsWith("BONUS_")) {
                if ("TESOURO_ESPECIARIAS".equals(escolhido)) {
                    escolhido = "TESOURO_PELES";
                } else if ("TESOURO_PELES".equals(escolhido)) {
                    escolhido = "TESOURO_PORCELANA";
                }
                executar(partida.desenvolvimentoAjustarItemEspecial(jogadores.getSelectedIndex(),
                        escolhido, quantidade));
            } else {
                executar(partida.desenvolvimentoAjustarRecursoEspecial(jogadores.getSelectedIndex(),
                        escolhido, quantidade));
            }
        });
        tributos.add(ajustarRecurso);
        secoes.add(tributos);

        JPanel votosJogador = new JPanel(new FlowLayout(FlowLayout.LEFT));
        votosJogador.setBorder(BorderFactory.createTitledBorder("Pontuacao do jogador"));
        votosJogador.add(new JLabel("Adicionar votos:"));
        votosJogador.add(votos);
        JButton adicionarVotos = new JButton("Conceder votos");
        adicionarVotos.addActionListener(evento -> executar(
                partida.desenvolvimentoAlterarVotos(jogadores.getSelectedIndex(),
                        (Integer) votos.getValue())));
        votosJogador.add(adicionarVotos);
        secoes.add(votosJogador);

        JPanel khan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        khan.setBorder(BorderFactory.createTitledBorder("Khan: destino e trilha"));
        khan.add(destinos);
        JButton moverKhan = new JButton("Posicionar Khan");
        moverKhan.addActionListener(evento -> executar(
                partida.desenvolvimentoPosicionarKhan((DestinoKhan) destinos.getSelectedItem())));
        khan.add(moverKhan);
        khan.add(new JLabel("Votos Khan (+/-):"));
        khan.add(pontosKhan);
        JButton alterarKhan = new JButton("Ajustar trilha");
        alterarKhan.addActionListener(evento -> executar(
                partida.desenvolvimentoAjustarPontuacaoKhan((Integer) pontosKhan.getValue())));
        khan.add(alterarKhan);
        secoes.add(khan);

        JPanel encerramento = new JPanel(new FlowLayout(FlowLayout.LEFT));
        encerramento.setBorder(BorderFactory.createTitledBorder("Testes de encerramento"));
        JButton terminar = new JButton("Encerrar partida e apurar vencedor");
        terminar.addActionListener(evento -> {
            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "Encerrar imediatamente e calcular os resultados?",
                    "Confirmar teste", JOptionPane.YES_NO_OPTION);
            if (confirmacao == JOptionPane.YES_OPTION) {
                executar(partida.desenvolvimentoEncerrarPartida());
            }
        });
        encerramento.add(terminar);
        secoes.add(encerramento);

        conteudo.add(secoes, BorderLayout.CENTER);
        conteudo.add(situacao, BorderLayout.SOUTH);
        setContentPane(conteudo);
        aplicarVisualPadrao(conteudo);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(750, 460);
        setLocationRelativeTo(dono);
        partida.registrarObservador(this);
        atualizar(partida);
    }

    /** Visual normal do Swing apenas nesta janela, sem alterar o tema do resto do jogo. */
    private void aplicarVisualPadrao(java.awt.Component componente) {
        java.awt.Color fundo = new java.awt.Color(240, 240, 240);
        java.awt.Color texto = java.awt.Color.BLACK;
        componente.setForeground(texto);
        if (componente instanceof javax.swing.JComboBox<?>) {
            componente.setBackground(java.awt.Color.WHITE);
        } else if (componente instanceof JSpinner) {
            componente.setBackground(java.awt.Color.WHITE);
            JSpinner spinner = (JSpinner) componente;
            if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
                javax.swing.JFormattedTextField campo =
                        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
                campo.setBackground(java.awt.Color.WHITE);
                campo.setForeground(texto);
            }
        } else if (componente instanceof JButton) {
            componente.setBackground(fundo);
            if (componente instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) componente).setOpaque(true);
            }
        } else if (componente instanceof JPanel || componente instanceof JLabel) {
            componente.setBackground(fundo);
        }
        if (componente instanceof java.awt.Container) {
            for (java.awt.Component filho : ((java.awt.Container) componente).getComponents()) {
                aplicarVisualPadrao(filho);
            }
        }
    }

    private void executar(boolean sucesso) {
        if (!sucesso) {
            JOptionPane.showMessageDialog(this, "Alteracao nao permitida para este estado.",
                    "Ferramenta de teste", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        if (partida != null) {
            situacao.setText("Vez: " + partida.getNomeJogador(partida.getIndiceJogadorAtual())
                    + "    |    Khan: " + partida.consultarPontuacaoKhan()
                    + "/" + partida.consultarCasaFinal());
        }
    }

    @Override
    public void dispose() {
        partida.removerObservador(this);
        super.dispose();
    }
}
