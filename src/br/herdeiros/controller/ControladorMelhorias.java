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
        if (partida.comprarMelhoriaAtual(indice)) {
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
