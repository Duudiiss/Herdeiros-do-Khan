package br.herdeiros.controller;

import br.herdeiros.model.InterfacePartida;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

/** Trata a escolha de uma melhoria sem colocar regras na View. */
public final class ControladorMelhorias extends MouseAdapter {
    private final InterfacePartida partida;
    private final JLabel mensagem;

    public ControladorMelhorias(InterfacePartida partida, JLabel mensagem) {
        this.partida = partida;
        this.mensagem = mensagem;
    }

    /** A cor original da carta nao restringe a coluna comprada por Jochi. */
    public int escolherColunaJochi(java.awt.Component dono) {
        if (partida.getPersonagemJogador(partida.getIndiceJogadorAtual())
                != br.herdeiros.model.Personagem.JOCHI) {
            return -1;
        }
        String[] nomes = {"Vermelha", "Verde", "Amarela", "Branca"};
        int escolhida = javax.swing.JOptionPane.showOptionDialog(dono,
                "Jochi: em qual coluna colocar esta melhoria?",
                "Escolher coluna", javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE, null, nomes, nomes[0]);
        return escolhida < 0 ? -2 : escolhida;
    }

    @Override
    public void mouseClicked(MouseEvent evento) {
        int largura = evento.getComponent().getWidth() / partida.consultarMercadoMelhorias().size();
        if (largura <= 0) {
            return;
        }
        int indice = evento.getX() / largura;
        if (indice < 0 || indice >= partida.consultarMercadoMelhorias().size()) {
            return;
        }
        int coluna = escolherColunaJochi(evento.getComponent());
        if (coluna == -2) {
            return;
        }
        if (partida.comprarMelhoriaAtual(indice, coluna)) {
            mensagem.setText("Melhoria comprada. Os efeitos só podem ser ativados a partir do próximo turno.");
            if (partida.consultarMelhoriasBrancasPendentes() > 0) {
                br.herdeiros.view.EscolhaMelhoriaBranca.mostrar(
                        javax.swing.SwingUtilities.getWindowAncestor(evento.getComponent()), partida);
            }
        } else {
            mensagem.setText("Compra recusada: verifique moedas, vagas na coluna e estoque.");
        }
    }
}
