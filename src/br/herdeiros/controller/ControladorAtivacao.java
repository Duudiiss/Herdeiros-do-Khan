package br.herdeiros.controller;

import br.herdeiros.model.InterfacePartida;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Recebe o clique na cartela e solicita a ativacao oficial ao Model. */
public final class ControladorAtivacao implements ActionListener {
    private final InterfacePartida partida;
    private final int indiceColuna;

    public ControladorAtivacao(InterfacePartida partida, int indiceColuna) {
        this.partida = partida;
        this.indiceColuna = indiceColuna;
    }

    @Override
    public void actionPerformed(ActionEvent evento) {
        partida.ativarColunaJogadorAtual(indiceColuna);
    }
}
