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
import br.herdeiros.model.ResumoCartaObjetivo;

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
    private final javax.swing.JCheckBox karakorumEspecial;
    private final JLabel nome;
    private final JLabel tributos;
    private final JLabel resumoAtivacao;
    private final JLabel colunasUtilizadas;
    private final JButton botaoHabilidade;
    private final Runnable acaoHabilidade;
    private final JLabel conselheiroSecreto;
    private final JLabel imagemConselheiroSecreto;
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
        karakorumEspecial = new javax.swing.JCheckBox("Karakorum especial (antes da partida)");
        karakorumEspecial.setOpaque(false);
        karakorumEspecial.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        karakorumEspecial.addActionListener(evento -> {
            if (!this.partida.configurarKarakorumEspecial(karakorumEspecial.isSelected())) {
                karakorumEspecial.setSelected(this.partida.isKarakorumEspecial());
                avisoBonus.setText("Karakorum especial so pode ser alterada na preparacao.");
            }
        });
        painelBonus.definirAcaoClique(indice -> ativarBonus(indice));
        this.nome = EstiloImperial.criarLabelComBorda(
                "",
                EstiloImperial.FONTE_SUBTITULO,
                EstiloImperial.COR_DOURADA_IMPERIAL,
                EstiloImperial.COR_BORDA_TEXTO);
        this.tributos = new JLabel();
        this.tributos.setFont(EstiloImperial.FONTE_TEXTO);
        this.tributos.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        this.colunasUtilizadas = new JLabel();
        this.colunasUtilizadas.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        this.colunasUtilizadas.setFont(EstiloImperial.FONTE_TEXTO);
        this.resumoAtivacao = new JLabel();
        this.resumoAtivacao.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        this.resumoAtivacao.setFont(EstiloImperial.FONTE_TEXTO);
        painelImagem.definirAcaoDeClique(coluna ->
                new ControladorAtivacao(JanelaCartela.this.partida, coluna).actionPerformed(null));

        JPanel cabecalho = EstiloImperial.criarPainelFundo(new GridLayout(3, 1, 3, 3));
        cabecalho.add(nome);
        cabecalho.add(tributos);
        cabecalho.add(colunasUtilizadas);

        conselheiroSecreto = new JLabel("Conselheiro secreto: nao escolhido");
        conselheiroSecreto.setFont(EstiloImperial.FONTE_TEXTO_NEGRITO);
        conselheiroSecreto.setForeground(EstiloImperial.COR_DOURADA_IMPERIAL);
        imagemConselheiroSecreto = new JLabel("Carta oculta");
        imagemConselheiroSecreto.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        detalhesConselheiroSecreto = new JLabel();
        detalhesConselheiroSecreto.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        detalhesConselheiroSecreto.setVerticalAlignment(JLabel.TOP);
        confirmarSecreta = EstiloImperial.criarBotaoImperial("Escolher conselheiro secreto");
        confirmarSecreta.addActionListener(evento ->
                EscolhaConselheiroSecreto.mostrar(JanelaCartela.this, partida));
        revelarSecreta = EstiloImperial.criarBotaoImperial("Revelar carta");
        revelarSecreta.addActionListener(evento -> {
            cartaRevelada = !cartaRevelada;
            atualizar(partida);
        });
        JPanel areaSecreta = EstiloImperial.criarPainelFundo(new java.awt.BorderLayout(6, 6));
        areaSecreta.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(EstiloImperial.COR_DOURADA_IMPERIAL),
                "Conselheiro secreto", 0, 0, EstiloImperial.FONTE_TEXTO_NEGRITO,
                EstiloImperial.COR_DOURADA_IMPERIAL));
        areaSecreta.add(conselheiroSecreto, java.awt.BorderLayout.NORTH);
        JPanel escolhaSecreta = EstiloImperial.criarPainelFundo(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        escolhaSecreta.add(imagemConselheiroSecreto);
        escolhaSecreta.add(confirmarSecreta);
        escolhaSecreta.add(revelarSecreta);
        areaSecreta.add(escolhaSecreta, java.awt.BorderLayout.WEST);
        areaSecreta.add(detalhesConselheiroSecreto, java.awt.BorderLayout.CENTER);

        JPanel rodape = EstiloImperial.criarPainelFundo(new BorderLayout(8, 8));
        botaoHabilidade = EstiloImperial.criarBotaoImperial("Usar habilidade do personagem");
        botaoHabilidade.addActionListener(evento -> {
            if (this.acaoHabilidade != null) {
                this.acaoHabilidade.run();
            }
        });
        rodape.add(areaSecreta, BorderLayout.NORTH);
        JPanel acoesRodape = EstiloImperial.criarPainelFundo(new BorderLayout(4, 4));
        acoesRodape.add(botaoHabilidade, BorderLayout.NORTH);
        acoesRodape.add(resumoAtivacao, BorderLayout.SOUTH);
        rodape.add(acoesRodape, BorderLayout.SOUTH);

        setLayout(new BorderLayout(8, 8));
        add(cabecalho, BorderLayout.NORTH);
        JPanel tabuleiroPessoal = EstiloImperial.criarPainelFundo(new BorderLayout(4, 4));
        tabuleiroPessoal.add(painelImagem, BorderLayout.CENTER);
        JPanel zonaBonus = EstiloImperial.criarPainelFundo(new BorderLayout(2, 2));
        zonaBonus.add(new javax.swing.JScrollPane(painelBonus,
                javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel estadoBonus = EstiloImperial.criarPainelFundo(new BorderLayout(4, 4));
        estadoBonus.add(avisoBonus, BorderLayout.CENTER);
        estadoBonus.add(karakorumEspecial, BorderLayout.EAST);
        zonaBonus.add(estadoBonus, BorderLayout.SOUTH);
        zonaBonus.setPreferredSize(new Dimension(0, 155));
        tabuleiroPessoal.add(zonaBonus, BorderLayout.SOUTH);
        add(tabuleiroPessoal, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
        setSize(new Dimension(1080, 850));
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
            imagemConselheiroSecreto.setIcon(null);
            imagemConselheiroSecreto.setText("Ainda nao escolhido");
            detalhesConselheiroSecreto.setText("");
        } else if (!cartaRevelada) {
            conselheiroSecreto.setText("Conselheiro secreto: oculto");
            imagemConselheiroSecreto.setIcon(null);
            imagemConselheiroSecreto.setText("Carta oculta");
            detalhesConselheiroSecreto.setText("");
        } else {
            conselheiroSecreto.setText("Conselheiro secreto: " + cartaEscolhida.name());
            imagemConselheiroSecreto.setText("");
            imagemConselheiroSecreto.setIcon(
                    ImagensConselheirosSecretos.imagem(cartaEscolhida, 135, 195));
            int progresso = partida.consultarProgressoSecretoJogadorAtual();
            int[] metas = cartaEscolhida.getMetas();
            StringBuilder detalhes = new StringBuilder("<html><div style='width:360px'>");
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
        StringBuilder listaTesouros = new StringBuilder("Tesouros por tipo: ");
        for (br.herdeiros.model.TipoTesouro tipo : br.herdeiros.model.TipoTesouro.values()) {
            if (tipo != br.herdeiros.model.TipoTesouro.PELES) {
                listaTesouros.append("  |  ");
            }
            listaTesouros.append(tipo.getDescricao()).append(": ")
                    .append(individuais.getOrDefault(tipo, 0));
        }
        ResumoAtivacao ativacao = partida.consultarAtivacaoJogadorAtual();
        colunasUtilizadas.setText(listaTesouros.toString());
        java.util.List<br.herdeiros.model.PecaBonus> pecas = partida.consultarBonusJogadorAtual();
        java.util.List<Boolean> estadosBonus = new java.util.ArrayList<Boolean>();
        for (int i = 0; i < pecas.size(); i++) {
            estadosBonus.add(partida.isBonusUtilizadoJogadorAtual(i));
        }
        painelBonus.atualizarPecas(pecas, estadosBonus);
        karakorumEspecial.setSelected(partida.isKarakorumEspecial());
        painelImagem.mostrarCartela(
                partida.getIndiceCartelaJogador(resumo.getIndiceJogador()),
                ativacao, partida.consultarVotosJogadorAtual(), resumo.getCorJogador(),
                resumo.getMelhoriasDetalhadas());

        String[] nomesColunas = {"Vermelha", "Verde", "Amarela", "Branca"};
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
            resumoAtivacao.setText("Clique na coluna desejada na cartela para ativar sua acao.");
        } else {
            resumoAtivacao.setText("Coluna ativa: "
                    + nomesColunas[ativacao.getColunaAtiva()]
                    + ". Cavalos restantes: " + partida.consultarCavalosRestantesJogadorAtual()
                    + ". Punhos restantes: " + partida.consultarPunhosRestantesJogadorAtual()
                    + ". Moedas virtuais da coluna: " + ativacao.getMoedasVirtuaisDaColuna()
                    + ". Acao do Khan: " + (ativacao.temAcaoKhan() ? "pendente" : "nao pendente") + ".");
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