package br.herdeiros.view;

import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ResumoMelhoria;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/** Janela de selecao da melhoria branca concedida por linha completa. */
public final class EscolhaMelhoriaBranca {

    private EscolhaMelhoriaBranca() {
    }

    public static void mostrar(Component origem, InterfacePartida partida) {
        while (partida.consultarMelhoriasBrancasPendentes() > 0) {
            List<ResumoMelhoria> mercado = partida.consultarMercadoMelhorias();
            List<String> opcoes = new ArrayList<String>();
            List<Integer> posicoes = new ArrayList<Integer>();
            opcoes.add("Aleatória - topo da pilha");
            posicoes.add(-1);

            for (int i = 0; i < mercado.size(); i++) {
                ResumoMelhoria melhoria = mercado.get(i);
                if (melhoria != null) {
                    opcoes.add("Espaço " + (i + 1) + ": " + melhoria.getCodigo()
                            + " - " + melhoria.getDescricao());
                    posicoes.add(i);
                }
            }

            if (opcoes.isEmpty()) {
                return;
            }
            Object escolha = JOptionPane.showInputDialog(origem,
                    "Linha de três melhorias concluída! Escolha uma melhoria gratuita "
                            + "para a coluna branca (qualquer cor).",
                    "Melhoria branca", JOptionPane.QUESTION_MESSAGE,
                    null, opcoes.toArray(new String[0]), opcoes.get(0));
            if (escolha == null) {
                return;
            }
            int indiceEscolha = opcoes.indexOf(escolha.toString());
            if (indiceEscolha < 0) {
                return;
            }
            int posicao = posicoes.get(indiceEscolha);
            boolean sucesso;
            if (posicao < 0) {
                sucesso = partida.escolherMelhoriaBrancaAleatoria();
            } else {
                sucesso = partida.escolherMelhoriaBrancaDoMercado(posicao);
            }
            if (!sucesso) {
                JOptionPane.showMessageDialog(origem,
                        "Não foi possível obter essa melhoria; verifique a disponibilidade.");
                return;
            }
        }
    }
}
