package br.herdeiros.controller;

import br.herdeiros.model.InterfacePartida;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Coordena os comandos da janela da cartela sem acessar objetos internos do Model.
 * A simulacao abaixo serve apenas para verificar a integracao Observer na etapa 2.
 */
public final class ControladorCartela implements ActionListener {

    private final InterfacePartida partida;
    private final javax.swing.JComboBox<String> tiposTributo;

    public ControladorCartela(InterfacePartida partida,
            javax.swing.JComboBox<String> tiposTributo) {
        this.partida = partida;
        this.tiposTributo = tiposTributo;
    }

    @Override
    public void actionPerformed(ActionEvent evento) {
        String tipo = (String) tiposTributo.getSelectedItem();
        if (tipo != null) {
            partida.simularRecebimentoTributo(partida.getIndiceJogadorAtual(), tipo, 1);
        }
    }
}
