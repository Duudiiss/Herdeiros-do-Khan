package br.herdeiros.view;

import br.herdeiros.controller.ControladorAtivacao;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ResumoCartela;
import br.herdeiros.model.ResumoAtivacao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JButton;
import br.herdeiros.model.Personagem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import br.herdeiros.model.CartaConselheiroSecreto;

/**
 * View da cartela do jogador da vez. Recebe notificacoes de mudancas pelo Observer
 * e consulta um resumo imutavel dos dados pela Facade do Model.
 */
public final class JanelaCartela extends JFrame implements ObservadorPartida {

    private static final long serialVersionUID = 1L;
    private final InterfacePartida partida;
    private final PainelCartela painelImagem;
    private final PainelBonusJogador painelBonus;
    private final JLabel avisoBonus;
    private final JLabel nome;
    private final JLabel tributos;
    private final JLabel resumoAtivacao;
    private final JLabel recursosRodada;
    private final JLabel colunasUtilizadas;
    private final JLabel tesourosVirtuaisLabel;
    private final JButton botaoHabilidade;
    private final Runnable acaoHabilidade;
    private final JLabel conselheiroSecreto;
    private final PainelImagemJava2D imagemConselheiroSecreto;
    private final JLabel detalhesConselheiroSecreto;
    private final JButton confirmarSecreta;
    private final JButton revelarSecreta;
    private boolean cartaRevelada;
    private int ultimoJogadorExibido = -1;

    public JanelaCartela(InterfacePartida partida) throws IOException {
        this(partida, null);
    }

    public JanelaCartela(InterfacePartida partida, Runnable acaoHabilidade) throws IOException {
        super("Herdeiros de Khan - Cartela individual");
        if (partida == null) {
            throw new IllegalArgumentException("Partida obrigatoria");
        }
        this.partida = partida;
        this.acaoHabilidade = acaoHabilidade;
        this.painelImagem = new PainelCartela();
        this.painelBonus = new PainelBonusJogador();
        this.avisoBonus = new JLabel(" ");
        avisoBonus.setForeground(EstiloImperial.COR_DOURADA_IMPERIAL);
        painelBonus.definirAcaoClique(new java.util.function.IntConsumer() {
            @Override public void accept(int indice) { ativarBonus(indice); }
        });
        this.nome = EstiloImperial.criarLabelComBorda(
                "",
                EstiloImperial.FONTE_SUBTITULO,
                EstiloImperial.COR_DOURADA_IMPERIAL,
                EstiloImperial.COR_BORDA_TEXTO);
        this.tributos = new JLabel();
        this.tributos.setFont(EstiloImperial.FONTE_TEXTO);
        this.tributos.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        this.tesourosVirtuaisLabel = new JLabel();
        tesourosVirtuaisLabel.setFont(EstiloImperial.FONTE_TEXTO_NEGRITO);
        tesourosVirtuaisLabel.setForeground(EstiloImperial.COR_DOURADA_IMPERIAL);
        this.colunasUtilizadas = new JLabel();
        this.colunasUtilizadas.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        this.colunasUtilizadas.setFont(EstiloImperial.FONTE_TEXTO);
        this.resumoAtivacao = new JLabel();
        this.resumoAtivacao.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        this.resumoAtivacao.setFont(EstiloImperial.FONTE_TEXTO);
        this.recursosRodada = new JLabel("Recursos da rodada: aguardando ativacao");
        recursosRodada.setForeground(EstiloImperial.COR_DOURADA_IMPERIAL);
        recursosRodada.setFont(EstiloImperial.FONTE_TEXTO_NEGRITO);
        painelImagem.definirAcaoDeClique(new java.util.function.IntConsumer() {
            @Override public void accept(int coluna) {
                new ControladorAtivacao(JanelaCartela.this.partida, coluna).actionPerformed(null);
            }
        });

        JPanel cabecalho = EstiloImperial.criarPainelFundo(new GridLayout(5, 1, 3, 3));
        cabecalho.add(nome);
        cabecalho.add(tributos);
        cabecalho.add(recursosRodada);
        cabecalho.add(colunasUtilizadas);
        cabecalho.add(tesourosVirtuaisLabel);

        conselheiroSecreto = new JLabel("Conselheiro secreto: nao escolhido");
        conselheiroSecreto.setFont(EstiloImperial.FONTE_TEXTO_NEGRITO);
        conselheiroSecreto.setForeground(EstiloImperial.COR_DOURADA_IMPERIAL);
        imagemConselheiroSecreto = new PainelImagemJava2D();
        imagemConselheiroSecreto.setPreferredSize(new Dimension(175, 245));
        imagemConselheiroSecreto.setMinimumSize(new Dimension(175, 245));
        imagemConselheiroSecreto.setText("Carta oculta");

        detalhesConselheiroSecreto = new JLabel();
        detalhesConselheiroSecreto.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        detalhesConselheiroSecreto.setVerticalAlignment(JLabel.TOP);
        confirmarSecreta = EstiloImperial.criarBotaoImperial("Escolher conselheiro secreto");
        confirmarSecreta.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evento) {
                EscolhaConselheiroSecreto.mostrar(JanelaCartela.this, partida);
            }
        });
        revelarSecreta = EstiloImperial.criarBotaoImperial("Revelar carta");
        revelarSecreta.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evento) {
                cartaRevelada = !cartaRevelada;
                atualizar(partida);
            }
        });
        JPanel areaSecreta = EstiloImperial.criarPainelFundo(new java.awt.BorderLayout(8, 8));
        areaSecreta.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(EstiloImperial.COR_DOURADA_IMPERIAL),
                "Conselheiro secreto", 0, 0, EstiloImperial.FONTE_TEXTO_NEGRITO,
                EstiloImperial.COR_DOURADA_IMPERIAL));
        areaSecreta.setPreferredSize(new Dimension(0, 305));
        areaSecreta.add(conselheiroSecreto, java.awt.BorderLayout.NORTH);

        JPanel botoesSecreta = EstiloImperial.criarPainelFundo(new java.awt.GridLayout(2, 1, 6, 6));
        botoesSecreta.add(confirmarSecreta);
        botoesSecreta.add(revelarSecreta);
        JPanel esquerdaSecreta = EstiloImperial.criarPainelFundo(new java.awt.BorderLayout(6, 6));
        esquerdaSecreta.add(imagemConselheiroSecreto, java.awt.BorderLayout.CENTER);
        esquerdaSecreta.add(botoesSecreta, java.awt.BorderLayout.SOUTH);

        areaSecreta.add(esquerdaSecreta, java.awt.BorderLayout.WEST);
        javax.swing.JScrollPane rolagemDetalhes = new javax.swing.JScrollPane(detalhesConselheiroSecreto,
                javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rolagemDetalhes.setBorder(null);
        rolagemDetalhes.getViewport().setOpaque(false);
        rolagemDetalhes.setOpaque(false);
        areaSecreta.add(rolagemDetalhes, java.awt.BorderLayout.CENTER);

        JPanel areaHabilidade = EstiloImperial.criarPainelFundo(new BorderLayout(4, 4));
        areaHabilidade.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(EstiloImperial.COR_DOURADA_IMPERIAL),
                "Habilidade especial", 0, 0, EstiloImperial.FONTE_TEXTO_NEGRITO,
                EstiloImperial.COR_DOURADA_IMPERIAL));
        botaoHabilidade = EstiloImperial.criarBotaoImperial("Usar habilidade do personagem");
        botaoHabilidade.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evento) {
                if (JanelaCartela.this.acaoHabilidade != null) {
                    JanelaCartela.this.acaoHabilidade.run();
                }
            }
        });
        areaHabilidade.add(botaoHabilidade, BorderLayout.CENTER);

        setLayout(new BorderLayout(8, 8));
        add(cabecalho, BorderLayout.NORTH);

        JPanel tabuleiroPessoal = EstiloImperial.criarPainelFundo(new BorderLayout(6, 6));
        tabuleiroPessoal.add(painelImagem, BorderLayout.CENTER);

        JPanel zonaBonus = EstiloImperial.criarPainelFundo(new BorderLayout(4, 4));
        zonaBonus.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(EstiloImperial.COR_DOURADA_IMPERIAL),
                "Peças de bônus", 0, 0, EstiloImperial.FONTE_TEXTO_NEGRITO,
                EstiloImperial.COR_DOURADA_IMPERIAL));
        javax.swing.JScrollPane rolagemBonus = new javax.swing.JScrollPane(painelBonus,
                javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rolagemBonus.setBorder(null);
        zonaBonus.add(rolagemBonus, BorderLayout.CENTER);
        JPanel estadoBonus = EstiloImperial.criarPainelFundo(new BorderLayout(4, 4));
        estadoBonus.add(avisoBonus, BorderLayout.CENTER);
        zonaBonus.add(estadoBonus, BorderLayout.SOUTH);
        zonaBonus.setPreferredSize(new Dimension(0, 195));

        JPanel painelInferior = EstiloImperial.criarPainelFundo(new BorderLayout());
        painelInferior.setLayout(new javax.swing.BoxLayout(painelInferior, javax.swing.BoxLayout.Y_AXIS));
        areaHabilidade.setAlignmentX(LEFT_ALIGNMENT);
        areaSecreta.setAlignmentX(LEFT_ALIGNMENT);
        zonaBonus.setAlignmentX(LEFT_ALIGNMENT);
        painelInferior.add(areaHabilidade);
        painelInferior.add(javax.swing.Box.createVerticalStrut(8));
        painelInferior.add(areaSecreta);
        painelInferior.add(javax.swing.Box.createVerticalStrut(8));
        painelInferior.add(zonaBonus);
        painelInferior.setPreferredSize(new Dimension(0, 560));

        tabuleiroPessoal.add(painelInferior, BorderLayout.SOUTH);

        javax.swing.JScrollPane rolagemCartela = new javax.swing.JScrollPane(tabuleiroPessoal,
                javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rolagemCartela.getVerticalScrollBar().setUnitIncrement(20);
        add(rolagemCartela, BorderLayout.CENTER);
        setSize(new Dimension(1320, 1040));
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(new Color(48, 32, 22));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evento) {
                JanelaCartela.this.partida.removerObservador(JanelaCartela.this);
            }
        });
        partida.registrarObservador(this);
        atualizar(partida);
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        ResumoCartela resumo = partida.consultarCartelaJogadorAtual();
        int jogadorDaVez = partida.getIndiceJogadorAtual();
        if (jogadorDaVez != ultimoJogadorExibido) {
            cartaRevelada = false;
            ultimoJogadorExibido = jogadorDaVez;
        }
        CartaConselheiroSecreto cartaEscolhida =
                partida.consultarConselheiroSecretoJogadorAtual();
        boolean faltaEscolher = cartaEscolhida == null;
        confirmarSecreta.setVisible(faltaEscolher);
        confirmarSecreta.setEnabled(faltaEscolher);
        revelarSecreta.setVisible(!faltaEscolher);
        revelarSecreta.setText(cartaRevelada ? "Ocultar carta" : "Revelar carta");
        if (faltaEscolher) {
            conselheiroSecreto.setText("Conselheiro secreto: escolha obrigatoria no 1o turno");
            imagemConselheiroSecreto.setImagem(null);
            imagemConselheiroSecreto.setText("Ainda nao escolhido");
            detalhesConselheiroSecreto.setText("");
        } else if (!cartaRevelada) {
            conselheiroSecreto.setText("Conselheiro secreto: oculto");
            imagemConselheiroSecreto.setImagem(null);
            imagemConselheiroSecreto.setText("Carta oculta");
            detalhesConselheiroSecreto.setText("");
        } else {
            conselheiroSecreto.setText("Conselheiro secreto: " + cartaEscolhida.name());
            imagemConselheiroSecreto.setText("");
            imagemConselheiroSecreto.setImagem(
                    ImagensConselheirosSecretos.imagem(cartaEscolhida, 165, 235));
            int progresso = partida.consultarProgressoSecretoJogadorAtual();
            int[] metas = cartaEscolhida.getMetas();
            StringBuilder detalhes = new StringBuilder("<html><div style='width:480px'>");
            detalhes.append("<b>Objetivo:</b> ").append(cartaEscolhida.getDescricao());
            detalhes.append("<br><b>Progresso atual:</b> ").append(progresso);
            for (int i = 0; i < metas.length; i++) {
                detalhes.append("<br>").append(progresso >= metas[i] ? "&#10003;" : "&#9675;")
                        .append(" <b>").append(i + 1).append(" voto(s)</b>: ")
                        .append(metas[i]).append(" necessarios");
            }
            detalhes.append("</div></html>");
            detalhesConselheiroSecreto.setText(detalhes.toString());
        }
        Personagem personagem = partida.getPersonagemJogador(partida.getIndiceJogadorAtual());
        boolean possuiHabilidadeManual = personagem == Personagem.CHAGATAI
                || personagem == Personagem.OGEDEI;
        botaoHabilidade.setVisible(possuiHabilidadeManual && acaoHabilidade != null);
        botaoHabilidade.setEnabled(possuiHabilidadeManual
                && acaoHabilidade != null && partida.habilidadeEspecialDisponivel());
        nome.setText("Jogador: " + resumo.getNomeJogador()
                + " (" + resumo.getCorJogador() + ")");
        tributos.setText("Tesouros: " + partida.consultarQuantidadeTesourosJogadorAtual()
                + "   Espadas: " + resumo.getEspadas()
                + formatarVirtual(resumo.getEspadasVirtuais())
                + "   Moedas: " + resumo.getMoedas()
                + formatarVirtual(resumo.getMoedasVirtuais())
                + "   Yurts: " + resumo.getYurts()
                + formatarVirtual(resumo.getYurtsVirtuais()));
        java.util.Map<br.herdeiros.model.TipoTesouro, Integer> individuais =
                partida.consultarTesourosPorTipoJogadorAtual();
        java.util.Map<br.herdeiros.model.TipoTesouro, Integer> virtuais =
                partida.consultarTesourosVirtuaisJogadorAtual();
        StringBuilder listaTesouros = new StringBuilder("Tesouros por tipo: ");
        for (br.herdeiros.model.TipoTesouro tipo : br.herdeiros.model.TipoTesouro.values()) {
            if (tipo != br.herdeiros.model.TipoTesouro.PELES) {
                listaTesouros.append("  |  ");
            }
            listaTesouros.append(tipo.getDescricao()).append(": ")
                    .append(individuais.getOrDefault(tipo, 0))
                    .append(formatarVirtual(virtuais.getOrDefault(tipo, 0)));
        }
        ResumoAtivacao ativacao = partida.consultarAtivacaoJogadorAtual();
        recursosRodada.setText("RECURSOS DA RODADA  |  Cavalos (movimentos): "
                + partida.consultarCavalosRestantesJogadorAtual()
                + "   Punhos (coletas): " + partida.consultarPunhosRestantesJogadorAtual()
                + "   Moedas virtuais: " + ativacao.getMoedasVirtuaisDaColuna()
                + "   Khan: " + (ativacao.temAcaoKhan() ? "Disponível" : "Não")
                + "   Melhoria: " + (ativacao.temAcaoMelhoria() ? "Disponível" : "Não"));
        StringBuilder tesourosVirtuais = new StringBuilder("  |  Virtuais: ");
        boolean primeiro = true;
        for (br.herdeiros.model.TipoTesouro tipo : br.herdeiros.model.TipoTesouro.values()) {
            int quantidade = virtuais.getOrDefault(tipo, 0);
            if (quantidade > 0) {
                if (!primeiro) tesourosVirtuais.append(", ");
                tesourosVirtuais.append(tipo.getDescricao()).append(": ").append(quantidade);
                primeiro = false;
            }
        }
        if (primeiro) tesourosVirtuais.append("nenhum");
        tesourosVirtuaisLabel.setText("TESOUROS VIRTUAIS DA RODADA: " +
                tesourosVirtuais.substring("  |  Virtuais: ".length()));
        colunasUtilizadas.setText(listaTesouros.toString());
        java.util.List<br.herdeiros.model.PecaBonus> pecas = partida.consultarBonusJogadorAtual();
        java.util.List<Boolean> estadosBonus = new java.util.ArrayList<Boolean>();
        for (int i = 0; i < pecas.size(); i++) {
            estadosBonus.add(partida.isBonusUtilizadoJogadorAtual(i));
        }
        painelBonus.atualizarPecas(pecas, estadosBonus);
        painelImagem.mostrarCartela(
                partida.getIndiceCartelaJogador(resumo.getIndiceJogador()),
                ativacao, partida.consultarVotosJogadorAtual(), resumo.getCorJogador(),
                resumo.getMelhoriasDetalhadas());

        StringBuilder estadoColunas = new StringBuilder("Colunas indisponiveis: ");
        String[] nomesColunasEstado = {"Vermelha", "Verde", "Amarela", "Branca"};
        boolean alguma = false;
        for (int coluna = 0; coluna < 4; coluna++) {
            if (ativacao.estaOcupada(coluna)) {
                if (alguma) {
                    estadoColunas.append(", ");
                }
                estadoColunas.append(nomesColunasEstado[coluna]);
                alguma = true;
            }
        }
        if (!alguma) {
            estadoColunas.append("nenhuma");
        }
        colunasUtilizadas.setToolTipText(estadoColunas.toString());
        painelImagem.mostrarColunasIndisponiveis(ativacao);
        if (ativacao.getColunaAtiva() < 0) {
            resumoAtivacao.setText("");
        } else {
            resumoAtivacao.setText("");
        }

    }

    private void ativarBonus(int indice) {
        java.util.List<br.herdeiros.model.PecaBonus> pecas = partida.consultarBonusJogadorAtual();
        if (indice < 0 || indice >= pecas.size()) return;
        br.herdeiros.model.PecaBonus peca = pecas.get(indice);
        if (partida.isBonusUtilizadoJogadorAtual(indice)) {
            avisoBonus.setText("Esta peca ja foi utilizada.");
            return;
        }
        if (!partida.consultarEscolhaTesourosBonusPendente().isEmpty()) {
            escolherTesouroDoBonus();
            return;
        }
        if (partida.usarPecaBonusJogadorAtual(indice)) {
            avisoBonus.setText("Bonus ativado: " + peca.getDescricao());
            if (!partida.consultarEscolhaTesourosBonusPendente().isEmpty()) {
                escolherTesouroDoBonus();
            }
        } else {
            avisoBonus.setText("Nao foi possivel ativar esta peca agora.");
        }
    }

    /** O bonus de tres tesouros depende da escolha explicita do jogador. */
    private void escolherTesouroDoBonus() {
        java.util.List<br.herdeiros.model.TipoTesouro> opcoes =
                partida.consultarEscolhaTesourosBonusPendente();
        if (opcoes.isEmpty()) return;
        Object[] alternativas = new Object[opcoes.size()];
        for (int i = 0; i < opcoes.size(); i++) {
            alternativas[i] = (i + 1) + " - " + opcoes.get(i).getDescricao();
        }
        Object escolha = javax.swing.JOptionPane.showInputDialog(this,
                "Escolha 1 tesouro para manter. Os demais serao descartados.",
                "Bonus: tres tesouros", javax.swing.JOptionPane.QUESTION_MESSAGE,
                null, alternativas, alternativas[0]);
        if (escolha == null) {
            avisoBonus.setText("Escolha pendente: clique novamente na peca para concluir.");
            return;
        }
        for (int i = 0; i < alternativas.length; i++) {
            if (alternativas[i].equals(escolha)) {
                if (partida.escolherTesouroBonus(i)) {
                    avisoBonus.setText("Tesouro selecionado; demais descartados.");
                }
                return;
            }
        }
    }

    private String formatarVirtual(int quantidade) {
        if (quantidade <= 0) {
            return "";
        }
        return " (+" + quantidade + " virtual)";
    }
}
