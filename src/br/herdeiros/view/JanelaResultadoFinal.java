package br.herdeiros.view;

import br.herdeiros.model.ResumoResultadoFinal;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Modal de encerramento da partida com pontuacao detalhada e vencedores. */
public final class JanelaResultadoFinal extends JDialog {

    private static final long serialVersionUID = 1L;

    public JanelaResultadoFinal(JFrame dono, List<ResumoResultadoFinal> resultados) {
        super(dono, "Herdeiros de Khan - Fim de Jogo", true);

        JPanel fundo = EstiloImperial.criarPainelFundo(new BorderLayout(10, 10));
        fundo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        StringBuilder campeoes = new StringBuilder("Novo Grande Khan: ");
        boolean primeiro = true;
        for (ResumoResultadoFinal r : resultados) {
            if (r.isVencedor()) {
                if (!primeiro) {
                    campeoes.append(" & ");
                }
                campeoes.append(r.getNomeJogador()).append(" (").append(r.getPersonagem()).append(")");
                primeiro = false;
            }
        }

        JLabel titulo = EstiloImperial.criarLabelComBorda(
                campeoes.toString(),
                EstiloImperial.FONTE_SUBTITULO,
                EstiloImperial.COR_DOURADA_IMPERIAL,
                EstiloImperial.COR_BORDA_TEXTO);

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[] {"Pos", "Jogador", "Herdeiro", "Trilha", "Regioes",
                        "Secreto", "Total", "Tesouros", "Dist. Karakorum", "Conselheiro secreto", "Progresso"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int pos = 1;
        for (ResumoResultadoFinal r : resultados) {
            modelo.addRow(new Object[] {
                    r.isVencedor() ? "1º ★" : (pos + "º"),
                    r.getNomeJogador(),
                    r.getPersonagem(),
                    r.getVotosTrilha(),
                    r.getVotosRegioes(),
                    r.getVotosConselheiroSecreto(),
                    r.getVotosTotais(),
                    r.getTesourosNaoEntregues(),
                    r.getDistanciaKarakorum(),
                    r.getNomeConselheiro(),
                    r.getProgressoConselheiro()
            });
            pos++;
        }

        JTable tabela = new JTable(modelo);
        EstiloImperial.estilizarTabela(tabela);

        JButton fechar = EstiloImperial.criarBotaoImperial("Encerrar Apuracao");
        fechar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        fundo.add(titulo, BorderLayout.NORTH);
        fundo.add(EstiloImperial.criarScrollPaneEstilizado(tabela), BorderLayout.CENTER);
        fundo.add(fechar, BorderLayout.SOUTH);

        setContentPane(fundo);
        setSize(new Dimension(1150, 390));
        setLocationRelativeTo(dono);
    }
}