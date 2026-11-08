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
import javax.swing.JComboBox;
import javax.swing.JTextArea;

/** Controller da partida: recebe o clique e solicita validação ao Model. */
public class ControladorTabuleiro extends MouseAdapter {
    private final InterfacePartida partida;
    private final MapaDados mapa;
    private PainelTabuleiro painel;
    private JTextArea estado;
    private JLabel indicadorVez;
    private JComboBox<Integer> seletorMovimentos;
    private int movimentosDisponiveis = 1;
    private int movimentosRestantes = 1;
    private boolean iniciouMovimento = false;

	public ControladorTabuleiro(InterfacePartida partida, MapaDados mapa) {
        this.partida = partida;
        this.mapa = mapa;
    }

    public void configurar(PainelTabuleiro painel, JTextArea estado, JLabel indicadorVez) {
        configurar(painel, estado, indicadorVez, null);
    }

    public void configurar(PainelTabuleiro painel, JTextArea estado,
                          JLabel indicadorVez, JComboBox<Integer> seletorMovimentos) {
        this.painel = painel;
        this.estado = estado;
        this.indicadorVez = indicadorVez;
        this.seletorMovimentos = seletorMovimentos;
    }

    public void configurarMovimentos(int movimentos) {
        if (!iniciouMovimento && movimentos >= 1 && movimentos <= 10) {
            movimentosDisponiveis = movimentos;
            movimentosRestantes = movimentos;
        }
    }

    /** Apenas este comando encerra o turno, mesmo apos uma movimentacao. */
    public void passarVez() {
        partida.avancarJogador();
        iniciouMovimento = false;
        movimentosRestantes = movimentosDisponiveis;
        if (seletorMovimentos != null) {
            seletorMovimentos.setEnabled(true);
        }
        atualizarEstado();
    }

    public int getMovimentosRestantes() {
        return movimentosRestantes;
    }

    public void atualizarEstado() {
        int indice = partida.getIndiceJogadorAtual();
        if (indicadorVez != null) {
            String personagem = "";
            if (partida.getPersonagemJogador(indice) != null) {
                personagem = "<br>" + partida.getPersonagemJogador(indice).getNomeExibicao();
            }
            indicadorVez.setText("<html><div style='text-align:center'>VEZ DE<br><b>"
                    + partida.getNomeJogador(indice).toUpperCase()
                    + "</b>" + personagem + "</div></html>");
            indicadorVez.setBackground(CoresJogadores.obter(partida.getCorJogador(indice)));
            indicadorVez.setForeground(Color.WHITE);
            indicadorVez.setFont(indicadorVez.getFont().deriveFont(Font.BOLD, 20f));
            indicadorVez.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(indicadorVez.getBackground().darker(), 3),
                    BorderFactory.createEmptyBorder(8, 6, 8, 6)));
            indicadorVez.setOpaque(true);
        }
        escrever("Movimentos restantes neste turno: " + movimentosRestantes
                + " de " + movimentosDisponiveis + ". Finalize em Passar turno.");
    }

    private void escrever(String mensagem) {
        if (estado != null) {
            estado.setText(mensagem);
        }
    }

    /**
     * Processa um destino usando o saldo de movimentos do turno.
     * Pode ser exercitado por testes sem abrir a interface gráfica.
     */
    public boolean tentarMoverPara(int destino) {
        int custo = partida.consultarCustoMovimentoAtual(destino);
        if (custo <= 0 || custo > movimentosRestantes) {
            escrever("Destino invalido ou exige mais movimentos que os restantes: "
                    + movimentosRestantes);
            return false;
        }
        if (!partida.movimentarJogadorAtual(destino, movimentosRestantes)) {
            escrever("O movimento foi recusado pelo Model.");
            return false;
        }
        movimentosRestantes -= custo;
        iniciouMovimento = true;
        if (seletorMovimentos != null) {
            seletorMovimentos.setEnabled(false);
        }
        atualizarEstado();
        return true;
    }

    @Override
    public void mouseClicked(MouseEvent evento) {
        double[] posicao = painel.converterClique(evento.getX(), evento.getY());
        if (posicao != null) {
            int destino = mapa.encontrarParada(posicao[0], posicao[1],
                    painel.getLarguraImagem(), painel.getAlturaImagem());
            if (destino >= 0) {
                tentarMoverPara(destino);
            }
        }
    }
}
