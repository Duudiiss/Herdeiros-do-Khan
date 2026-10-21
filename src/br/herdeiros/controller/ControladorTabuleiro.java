package br.herdeiros.controller;

import br.herdeiros.mapa.MapaDados;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.view.PainelTabuleiro;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

/** Controller da partida: recebe o clique e solicita validação ao Model. */
public class ControladorTabuleiro extends MouseAdapter {
    private final InterfacePartida partida;
    private final MapaDados mapa;
    private PainelTabuleiro painel;
    private JLabel estado;
    private int movimentosDisponiveis = 1;

    public ControladorTabuleiro(InterfacePartida partida, MapaDados mapa) {
        this.partida = partida;
        this.mapa = mapa;
    }

    public void configurar(PainelTabuleiro painel, JLabel estado) {
        this.painel = painel;
        this.estado = estado;
    }

    public void configurarMovimentos(int movimentos) {
        if (movimentos >= 1 && movimentos <= 10) {
            movimentosDisponiveis = movimentos;
        }
    }

    public void passarVez() {
        partida.avancarJogador();
        atualizarEstado();
        painel.repaint();
    }

    public void atualizarEstado() {
        int indice = partida.getIndiceJogadorAtual();
        escrever("Vez de " + partida.getNomeJogador(indice)
                + " | movimentos disponíveis: " + movimentosDisponiveis);
    }

    private void escrever(String mensagem) {
        if (estado != null) {
            estado.setText("<html>" + mensagem + "</html>");
        }
    }

    @Override
    public void mouseClicked(MouseEvent evento) {
        double[] posicao = painel.converterClique(evento.getX(), evento.getY());
        if (posicao != null) {
            int destino = mapa.encontrarParada(posicao[0], posicao[1],
                    painel.getLarguraImagem(), painel.getAlturaImagem());
            if (destino >= 0) {
                if (partida.movimentarJogadorAtual(destino, movimentosDisponiveis)) {
                    atualizarEstado();
                }
                painel.repaint();
            }
        }
    }
}
