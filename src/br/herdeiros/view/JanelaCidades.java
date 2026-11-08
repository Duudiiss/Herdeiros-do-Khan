package br.herdeiros.view;

import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ResumoCidade;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Janela de consulta das cidades abertas no mapa. Nao representa a mao de
 * cartas do jogador; essas cartas ainda nao foram integradas ao Model.
 */
public final class JanelaCidades extends JFrame implements ObservadorPartida {

    private static final long serialVersionUID = 1L;
    private final InterfacePartida partida;
    private final DefaultTableModel modelo;

    public JanelaCidades(InterfacePartida partida) {
        super("Herdeiros de Khan - Cidades disponiveis");
        if (partida == null) {
            throw new IllegalArgumentException("Partida obrigatoria");
        }
        this.partida = partida;

        modelo = new DefaultTableModel(
                new Object[] {"Cidade", "Regiao", "Tesouros", "Conquistada"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };

        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.setFillsViewportHeight(true);

        setLayout(new BorderLayout(8, 8));
        add(new JLabel("Cidades abertas na partida (consulta):"), BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        setSize(new Dimension(610, 320));
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evento) {
                JanelaCidades.this.partida.removerObservador(JanelaCidades.this);
            }
        });

        partida.registrarObservador(this);
        atualizar(partida);
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        List<ResumoCidade> cidades = partida.consultarCidadesDisponiveis();
        modelo.setRowCount(0);

        for (ResumoCidade cidade : cidades) {
            String conquistada;
            if (cidade.isConquistada()) {
                conquistada = "Sim";
            } else {
                conquistada = "Nao";
            }

            modelo.addRow(new Object[] {
                    cidade.getNome(),
                    cidade.getRegiao(),
                    Integer.valueOf(cidade.getTesourosRestantes()),
                    conquistada
            });
        }
    }
}
