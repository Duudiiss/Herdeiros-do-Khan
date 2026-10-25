package br.herdeiros.controller;

import br.herdeiros.mapa.MapaDados;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.view.PainelTabuleiro;
import br.herdeiros.view.CoresJogadores;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextArea;

/** Controller da partida: recebe o clique e solicita validação ao Model. */
public class ControladorTabuleiro extends MouseAdapter {
    private final InterfacePartida partida;
    private final MapaDados mapa;
    private PainelTabuleiro painel;
    private JTextArea estado;
    private JLabel indicadorVez;
    private int movimentosDisponiveis = 1;

    public ControladorTabuleiro(InterfacePartida partida, MapaDados mapa) {
        this.partida = partida;
        this.mapa = mapa;
    }

    public void configurar(PainelTabuleiro painel, JTextArea estado, JLabel indicadorVez) {
        this.painel = painel;
        this.estado = estado;
        this.indicadorVez = indicadorVez;
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
        if (indicadorVez != null) {
            indicadorVez.setText("<html><div style='text-align:center'>VEZ DE<br><b>"
                    + partida.getNomeJogador(indice).toUpperCase() + "</b></div></html>");
            indicadorVez.setBackground(CoresJogadores.obter(partida.getCorJogador(indice)));
            indicadorVez.setForeground(Color.WHITE);
            indicadorVez.setFont(indicadorVez.getFont().deriveFont(Font.BOLD, 20f));
            indicadorVez.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(indicadorVez.getBackground().darker(), 3),
                    BorderFactory.createEmptyBorder(8, 6, 8, 6)));
            indicadorVez.setOpaque(true);
        }
        escrever("Movimentos disponíveis: " + movimentosDisponiveis);
    }

    private void escrever(String mensagem) {
        if (estado != null) {
            estado.setText(mensagem);
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
                } else {
                	escrever("Movimento inválido! Só pode avançar para paradas diretamente adjacentes.");
                }
                painel.repaint();
            }
        }
    }
}
