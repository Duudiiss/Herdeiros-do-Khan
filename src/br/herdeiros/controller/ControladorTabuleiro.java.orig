package br.herdeiros.controller;

import br.herdeiros.mapa.MapaDados;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ResumoAtivacao;
import br.herdeiros.model.DestinoKhan;
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
    private boolean ativacaoReal = false;

	public ControladorTabuleiro(InterfacePartida partida, MapaDados mapa) {
        this.partida = partida;
        this.mapa = mapa;
    }

    public void configurar(PainelTabuleiro painel, JTextArea estado,
                          JLabel indicadorVez, JComboBox<Integer> seletorMovimentos) {
        this.painel = painel;
        this.estado = estado;
        this.indicadorVez = indicadorVez;
        this.seletorMovimentos = seletorMovimentos;
    }

    /** A interface normal usa apenas cavalos obtidos de uma coluna ativada. */
    public void habilitarAtivacaoReal() {
        ativacaoReal = true;
        seletorMovimentos = null;
    }

    public void configurarMovimentos(int movimentos) {
        if (ativacaoReal) {
            return;
        }
        if (!iniciouMovimento && movimentos >= 1 && movimentos <= 10) {
            movimentosDisponiveis = movimentos;
            movimentosRestantes = movimentos;
        }
    }

    /** Apenas este comando encerra o turno, mesmo apos uma movimentacao. */
    public void passarVez() {
        if (!partida.tentarAvancarJogador()) {
            escrever("Antes de passar o turno, mova o Khan: esta ação é obrigatória.");
            return;
        }
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
        if (ativacaoReal) {
            ResumoAtivacao ativacao = partida.consultarAtivacaoJogadorAtual();
            if (ativacao.getColunaAtiva() < 0) {
                escrever("Abra a cartela e ative uma coluna antes de mover. "
                        + "Uma coluna e ativada por turno. Finalize em Passar turno.");
            } else {
                String acaoKhan;
            if (ativacao.temAcaoKhan()) {
                acaoKhan = "ação obrigatória pendente";
            } else {
                acaoKhan = "sem ação pendente";
            }
            escrever("Movimentos restantes da coluna: "
                        + ativacao.getMovimentosRestantes()
                        + ". Punhos: " + ativacao.getPunhosDaColuna()
                        + ". Khan: " + acaoKhan
                        + ". Finalize em Passar turno.");
            }
        } else {
            escrever("Movimentos restantes neste turno: " + movimentosRestantes
                    + " de " + movimentosDisponiveis + ". Finalize em Passar turno.");
        }
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
        if (ativacaoReal) {
            boolean realizado = partida.movimentarJogadorComAtivacao(destino);
            if (!realizado) {
                escrever("Movimento recusado: ative uma coluna ou escolha um destino "
                        + "alcancavel com os cavalos restantes.");
                return false;
            }
            atualizarEstado();
            return true;
        }

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
        PainelTabuleiro.SelecaoTributo tributo = painel.identificarTributoClicado(
                evento.getX(), evento.getY());
        if (tributo != null) {
            boolean coletado = partida.coletarTributoDaPilha(
                    tributo.getProvincias(), tributo.getTipo());
            if (coletado) {
                escrever("Coletado 1 " + tributo.getTipo()
                        + ". O estoque do icone diminuiu e a cartela recebeu 1 tributo.");
            } else {
                escrever("Coleta recusada: verifique acesso, estoque e punhos da coluna.");
            }
            return;
        }
        String cidade = painel.identificarCidadeClicada(evento.getX(), evento.getY());
        if (cidade != null) {
            if (partida.saquearCidadeAtual(cidade)) {
                escrever("Saque realizado em " + cidade
                        + ". O tesouro restante da cidade foi atualizado.");
            } else {
                escrever("Saque recusado em " + cidade
                        + ": cidade fechada, parada nao visitada ou falta de espadas.");
            }
            return;
        }
        double[] posicao = painel.converterClique(evento.getX(), evento.getY());
        if (posicao != null) {
            if (partida.deveMoverKhanJogadorAtual()) {
                for (DestinoKhan destinoKhan : DestinoKhan.values()) {
                    double dx = posicao[0] - destinoKhan.getX();
                    double dy = posicao[1] - destinoKhan.getY();
                    if (dx * dx + dy * dy < 0.0030) {
                        if (partida.realizarAcaoKhan(destinoKhan)) {
                            escrever("Khan deslocado para: " + destinoKhan.getDescricao());
                        } else {
                            escrever("Destino recusado para o Khan.");
                        }
                        return;
                    }
                }
            }
            int destino = mapa.encontrarParada(posicao[0], posicao[1],
                    painel.getLarguraImagem(), painel.getAlturaImagem());
            if (destino >= 0) {
                tentarMoverPara(destino);
            }
        }
    }
}
