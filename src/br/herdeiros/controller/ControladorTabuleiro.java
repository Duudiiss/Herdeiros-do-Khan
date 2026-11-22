package br.herdeiros.controller;

import br.herdeiros.mapa.MapaDados;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ResumoAtivacao;
import br.herdeiros.model.DestinoKhan;
import br.herdeiros.model.TipoTesouro;
import javax.swing.JOptionPane;
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
    private boolean modoConstrucaoYurt = false;
    private boolean modoHabilidadeOgedei = false;

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
    public boolean passarVez() {
        if (partida.consultarMelhoriasBrancasPendentes() > 0) {
            escrever("Complete a recompensa: abra a cartela e escolha a melhoria branca antes de passar o turno.");
            return false;
        }
        if (partida.isPartidaEncerrada()) {
            escrever("A partida foi encerrada. O Khan atingiu o limite e as rodadas finais terminaram.");
            return false;
        }
        if (!partida.tentarAvancarJogador()) {
            if (partida.consultarConselheiroSecretoJogadorAtual() == null) {
                escrever("Escolha seu conselheiro secreto na cartela antes de passar o primeiro turno.");
            } else {
                escrever("Antes de passar o turno, conclua as acoes obrigatorias do Khan.");
            }
            return false;
        }
        iniciouMovimento = false;
        modoConstrucaoYurt = false;
        modoHabilidadeOgedei = false;
        movimentosRestantes = movimentosDisponiveis;
        if (seletorMovimentos != null) {
            seletorMovimentos.setEnabled(true);
        }
        atualizarEstado();
        return true;
    }

    /** Habilidade de Ogedei: escolher no tabuleiro uma ficha de cidade. */
    public boolean alternarModoHabilidadeOgedei() {
        if (!modoHabilidadeOgedei && !partida.habilidadeEspecialDisponivel()) {
            return false;
        }
        modoHabilidadeOgedei = !modoHabilidadeOgedei;
        escrever(modoHabilidadeOgedei
                ? "Ogedei: clique na ficha de tesouro que deseja comprar por 1 moeda. "
                  + "A cidade precisa manter pelo menos 1 tesouro."
                : "Selecao de tesouro de Ogedei cancelada.");
        return true;
    }

    public boolean isModoHabilidadeOgedei() {
        return modoHabilidadeOgedei;
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
            String conteudoVez;
            if (partida.isPartidaEncerrada()) {
                java.util.List<String> vencedores = partida.consultarNomesVencedores();
                String tituloResultado = vencedores.size() == 1 ? "VENCEDOR" : "EMPATE";
                conteudoVez = "<b>PARTIDA ENCERRADA</b><br>" + tituloResultado
                        + "<br><b>" + String.join(", ", vencedores).toUpperCase() + "</b>";
            } else {
                conteudoVez = "<b>Vez de " + partida.getNomeJogador(indice).toUpperCase() + "</b>"
                        + personagem;
            }
            indicadorVez.setText("<html><div style='text-align:center'>"
                    + conteudoVez + "</div></html>");
            indicadorVez.setBackground(CoresJogadores.obter(partida.getCorJogador(indice)));
            indicadorVez.setForeground(Color.WHITE);
            indicadorVez.setFont(indicadorVez.getFont().deriveFont(Font.BOLD, 21f));
            indicadorVez.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(indicadorVez.getBackground().darker(), 3),
                    BorderFactory.createEmptyBorder(8, 6, 8, 6)));
            indicadorVez.setOpaque(true);
        }
        if (partida.isPartidaEncerrada()) {
            java.util.List<String> vencedores = partida.consultarNomesVencedores();
            if (vencedores.size() == 1) {
                escrever("FIM DE JOGO! Vencedor: " + vencedores.get(0)
                        + ". A rodada final terminou.");
            } else {
                escrever("FIM DE JOGO! Empate entre: " + String.join(", ", vencedores)
                        + ". A rodada final terminou.");
            }
            return;
        }
        if (partida.isFimDisparado()) {
            escrever("O Khan chegou à casa final! Complete esta rodada e a última rodada da partida.");
            return;
        }
        if (ativacaoReal) {
            ResumoAtivacao ativacao = partida.consultarAtivacaoJogadorAtual();
            if (ativacao.getColunaAtiva() < 0) {
                escrever("1. Abra a cartela e ative uma coluna.\n"
                        + "2. Mova o peão e conclua as ações indicadas.\n"
                        + "3. Clique em Passar turno.");
            } else {
                String acaoKhan;
                if (ativacao.temAcaoKhan()) {
                    acaoKhan = "OBRIGATÓRIA";
                } else {
                    acaoKhan = "não necessária";
                }
                escrever("Continue sua jogada.\n"
                        + "Movimentos restantes: " + ativacao.getMovimentosRestantes()
                        + "  |  Punhos: " + ativacao.getPunhosDaColuna() + "\n"
                        + "Ação do Khan: " + acaoKhan + ".\n"
                        + "Quando terminar, clique em Passar turno.");
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

    public void alternarModoConstrucaoYurt() {
        modoConstrucaoYurt = !modoConstrucaoYurt;
        if (modoConstrucaoYurt) {
            escrever("Modo yurt ativado: clique numa parada visitada neste turno para construir 1 yurt.");
        } else {
            atualizarEstado();
        }
    }

    public boolean isModoConstrucaoYurt() {
        return modoConstrucaoYurt;
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
        if (custo < 0 || custo > movimentosRestantes) {
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
        // A ação do Khan tem prioridade sobre cartas, recursos e cidades sobrepostos.
        if (partida.deveMoverKhanJogadorAtual() && !partida.isPartidaEncerrada()) {
            double[] local = painel.converterClique(evento.getX(), evento.getY());
            if (local != null) {
                for (DestinoKhan destino : DestinoKhan.values()) {
                    if (destino != partida.consultarDestinoKhan()
                            && painel.contemDestinoKhan(destino, local[0], local[1])) {
                        if (partida.realizarAcaoKhan(destino)) {
                            escrever("Khan deslocado para " + destino.getDescricao()
                                    + ". Agora você pode passar o turno.");
                        } else {
                            escrever("Destino recusado para o Khan.");
                        }
                        return;
                    }
                }
            }
        }
        if (modoHabilidadeOgedei) {
            PainelTabuleiro.SelecaoTesouroCidade fichaOgedei =
                    painel.identificarFichaCidadeClicada(evento.getX(), evento.getY());
            if (fichaOgedei == null) {
                escrever("Ogedei: clique diretamente em uma ficha de tesouro de cidade.");
                return;
            }
            if (partida.usarHabilidadeOgedei(fichaOgedei.getNomeCidade(),
                    fichaOgedei.getTipo())) {
                modoHabilidadeOgedei = false;
                escrever("Ogedei comprou " + fichaOgedei.getTipo().getDescricao()
                        + " em " + fichaOgedei.getNomeCidade() + " por 1 moeda.");
            } else {
                escrever("Ogedei: precisa ter 1 moeda, a habilidade livre e "
                        + "a cidade precisa manter pelo menos 1 tesouro.");
            }
            return;
        }
        int cartaMercado = painel.identificarMelhoriaMercadoClicada(evento.getX(), evento.getY());
        if (cartaMercado >= 0) {
            int resposta = javax.swing.JOptionPane.showConfirmDialog(painel,
                    "Comprar a melhoria da posicao " + (cartaMercado + 1) + " do mercado?",
                    "Mercado de melhorias", javax.swing.JOptionPane.YES_NO_OPTION);
            if (resposta == javax.swing.JOptionPane.YES_OPTION) {
                int coluna = new ControladorMelhorias(partida, null)
                        .escolherColunaJochi(painel);
                if (coluna == -2) {
                    return;
                }
                if (partida.comprarMelhoriaAtual(cartaMercado, coluna)) {
                    escrever("Melhoria adquirida. Abra a cartela para conferir.");
                    if (partida.consultarMelhoriasBrancasPendentes() > 0) {
                        br.herdeiros.view.EscolhaMelhoriaBranca.mostrar(
                                javax.swing.SwingUtilities.getWindowAncestor(painel), partida);
                    }
                } else {
                    escrever("Compra recusada: verifique recursos e espaco na coluna.");
                }
            }
            return;
        }
        PainelTabuleiro.SelecaoTesouroCidade ficha =
                painel.identificarFichaCidadeClicada(evento.getX(), evento.getY());
        if (ficha != null) {
            if (partida.saquearCidadeAtual(ficha.getNomeCidade(), ficha.getTipo())) {
                escrever("Tesouro " + ficha.getTipo().getDescricao()
                        + " saqueado em " + ficha.getNomeCidade() + ".");
            } else {
                escrever("Saque recusado: verifique acesso, espadas e turno atual.");
            }
            return;
        }
        PainelTabuleiro.SelecaoTributo tributo = painel.identificarTributoClicado(
                evento.getX(), evento.getY());
        if (tributo != null) {
            boolean coletado = partida.coletarTributoDoIcone(
                    tributo.getIndice(), tributo.getProvincias(), tributo.getTipo());
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
            java.util.List<TipoTesouro> fichas = partida.consultarTesourosCidade(cidade);
            if (fichas.isEmpty()) {
                escrever("A cidade nao possui tesouros disponiveis.");
                return;
            }
            TipoTesouro[] opcoes = fichas.stream().distinct().toArray(TipoTesouro[]::new);
            TipoTesouro escolhido = (TipoTesouro) JOptionPane.showInputDialog(
                    painel, "Selecione um dos tesouros expostos em " + cidade + ":",
                    "Saquear cidade", JOptionPane.QUESTION_MESSAGE,
                    null, opcoes, opcoes[0]);
            if (escolhido == null) {
                return;
            }
            if (partida.saquearCidadeAtual(cidade, escolhido)) {
                escrever("Tesouro " + escolhido.getDescricao() + " saqueado em " + cidade + ".");
            } else {
                escrever("Saque recusado em " + cidade
                        + ": verifique acesso a cidade e espadas disponiveis.");
            }
            return;
        }
        double[] posicao = painel.converterClique(evento.getX(), evento.getY());
        if (posicao != null) {
            int destino = mapa.encontrarParada(posicao[0], posicao[1],
                    painel.getLarguraImagem(), painel.getAlturaImagem());
            if (destino >= 0) {
                if (modoConstrucaoYurt) {
                    if (partida.construirYurtAtual(destino)) {
                        modoConstrucaoYurt = false;
                        escrever("Yurt construida na parada " + destino + ". Ela ja pode ser usada em saltos futuros.");
                    } else {
                        escrever("Construcao recusada: verifique a parada, seus yurts disponiveis e o limite maximo de 12 yurts por jogador.");
                    }
                } else {
                    tentarMoverPara(destino);
                }
            }
        }
    }
}
