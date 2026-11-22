package br.herdeiros.view;

import br.herdeiros.controller.ControladorTabuleiro;
import br.herdeiros.mapa.ArquivoMapa;
import br.herdeiros.mapa.MapaDados;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.GerenciadorPartida;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.Cor;
import br.herdeiros.model.Personagem;
import br.herdeiros.model.DestinoKhan;
import br.herdeiros.model.Provincia;
import br.herdeiros.model.GerenciadorSave;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JanelaJogo extends JPanel implements ObservadorPartida {

    private static final long serialVersionUID = 1L;

    private final int quantidadeJogadores;
    private InterfacePartida partida;
    private MapaDados mapa;
    private ControladorTabuleiro controlador;
    private PainelTabuleiro painel;
    private JLabel indicadorVez;
    private JTextArea estado;
    private JButton passar;
    private JButton salvar;
    private JButton abrirCartela;
    private JButton abrirMelhorias;
    private JButton construirYurt;
    private String ultimaEscolhaAltani; 
    private boolean escolhaInicialJochiAberta;
    private JanelaMelhorias janelaMelhorias;
    private JLabel posicaoKhan;
    private JanelaCartela janelaCartela;
    private JanelaCidades janelaCidades;
    
    private boolean resultadoExibido = false;
    /** Opcao de interface, propositalmente fora do estado persistido. */
    private boolean modoDesenvolvedor;
    private Janela janelaPrincipal;
    private JanelaDesenvolvedor janelaDesenvolvedor;


    public JanelaJogo(int quantidadeJogadores) {
        if (quantidadeJogadores < 2 || quantidadeJogadores > 5) {
            throw new IllegalArgumentException("A partida requer entre 2 e 5 jogadores.");
        }
        this.quantidadeJogadores = quantidadeJogadores;
        configurarInterface();
    }

    public JanelaJogo(int quantidadeJogadores, String[] nomes, Cor[] cores,
                      Personagem[] personagens) throws IOException {
        this(quantidadeJogadores);
        iniciarPartida(quantidadeJogadores, nomes, cores, personagens);
    }
    
    public JanelaJogo(int quantidadeJogadores, String[] nomes, Cor[] cores,
                      Personagem[] personagens, boolean desenvolvedor, Janela janela) throws IOException {
        this(quantidadeJogadores);
        this.modoDesenvolvedor = desenvolvedor;
        this.janelaPrincipal = janela;
        // A interface deve ser reconstruida depois de definir o modo.
        removeAll();
        configurarInterface();
        iniciarPartida(quantidadeJogadores, nomes, cores, personagens);
    }

    public JanelaJogo(InterfacePartida partidaSalva, boolean desenvolvedor, Janela janela)
            throws IOException {
        this(quantidadeDaPartida(partidaSalva));
        this.modoDesenvolvedor = desenvolvedor;
        this.janelaPrincipal = janela;
        removeAll();
        configurarInterface();
        partida = GerenciadorPartida.getInstancia().carregarPartida(partidaSalva);
        configurarPartida(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        EstiloImperial.pintarFundoImperial(g, this, null);
    }

    public JanelaJogo(InterfacePartida partidaSalva) throws IOException {
        this(quantidadeDaPartida(partidaSalva));
        partida = GerenciadorPartida.getInstancia().carregarPartida(partidaSalva);
        configurarPartida(false);
    }

    private static int quantidadeDaPartida(InterfacePartida partida) {
        if (partida == null) {
            throw new IllegalArgumentException("Partida salva inválida.");
        }
        return partida.getQuantidadeJogadores();
    }

    private void configurarInterface() {
        setLayout(new BorderLayout(8, 8));

        indicadorVez = new JLabel();
        indicadorVez.setHorizontalAlignment(JLabel.CENTER);
        indicadorVez.setFont(EstiloImperial.FONTE_SUBTITULO);

        estado = new JTextArea(
            "Configure uma partida para começar."
        );

        estado.setEditable(false);
        estado.setLineWrap(true);
        estado.setWrapStyleWord(true);
        estado.setFont(EstiloImperial.FONTE_TEXTO);
        estado.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        estado.setBackground(new Color(42, 26, 16));
        estado.setRows(4);

        JPanel lateral = new JPanel(new BorderLayout(8, 8)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                EstiloImperial.pintarFundoImperial(g, this, null);
            }
        };

        lateral.setPreferredSize(new Dimension(240, 0));
        lateral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloImperial.COR_DOURADA_IMPERIAL, 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JPanel ferramentas = new JPanel(
            new GridLayout(0, 1, 0, 6)
        );
        ferramentas.setOpaque(false);
        
        JLabel tituloPainel = EstiloImperial.criarLabelComBorda(
                "Ações do Turno",
                EstiloImperial.FONTE_SUBTITULO,
                EstiloImperial.COR_DOURADA_IMPERIAL,
                EstiloImperial.COR_BORDA_TEXTO);
        ferramentas.add(tituloPainel);

        if (modoDesenvolvedor) {
            JButton desenvolvedor = EstiloImperial.criarBotaoImperial("Ferramentas de desenvolvedor");
            desenvolvedor.addActionListener(evento -> abrirFerramentasDesenvolvedor());
            ferramentas.add(desenvolvedor);
        }

        passar = EstiloImperial.criarBotaoEstilizado(
                "Passar turno",
                EstiloImperial.FONTE_BOTAO_MEDIO,
                EstiloImperial.COR_VERMELHO_ESCURO,
                EstiloImperial.COR_VERMELHO_HOVER,
                EstiloImperial.COR_TEXTO_CLARO);

        passar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                if (controlador != null && controlador.passarVez()) {
                    painel.repaint();
                }
            }
        });
        ferramentas.add(passar);

        abrirCartela = EstiloImperial.criarBotaoImperial("Abrir cartela do jogador");
        abrirCartela.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                abrirCartelaDoJogador();
            }
        });
        ferramentas.add(abrirCartela);

        abrirMelhorias = EstiloImperial.criarBotaoImperial("Comprar melhorias");
        abrirMelhorias.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                abrirJanelaMelhorias();
            }
        });
        ferramentas.add(abrirMelhorias);

        construirYurt = EstiloImperial.criarBotaoImperial("Construir yurt");
        construirYurt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                if (controlador != null) {
                    controlador.alternarModoConstrucaoYurt();
                    construirYurt.setText(controlador.isModoConstrucaoYurt()
                            ? "Cancelar yurt" : "Construir yurt");
                }
            }
        });
        ferramentas.add(construirYurt);
        
        JButton abrirConselheiros = EstiloImperial.criarBotaoImperial("Conselheiros e Objetivos");
        abrirConselheiros.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                if (partida != null) {
                    new JanelaConselheiros(partida).setVisible(true);
                }
            }
        });
        ferramentas.add(abrirConselheiros);

        JLabel orientacaoKhan = new JLabel(
                "<html>Khan: clique em uma área destacada<br>no tabuleiro quando a ação estiver ativa.</html>");
        orientacaoKhan.setFont(EstiloImperial.FONTE_TEXTO);
        orientacaoKhan.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        ferramentas.add(orientacaoKhan);

        posicaoKhan = EstiloImperial.criarLabelComBorda(
                "Khan: fora do tabuleiro",
                EstiloImperial.FONTE_TEXTO,
                EstiloImperial.COR_TEXTO_CLARO,
                EstiloImperial.COR_BORDA_TEXTO);
        ferramentas.add(posicaoKhan);

        JLabel instrucoes1 = new JLabel("<html>• Ative uma coluna na cartela para<br> mover.</html>");
        instrucoes1.setFont(EstiloImperial.FONTE_TEXTO);
        instrucoes1.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        JLabel instrucoes2 = new JLabel("<html>• Clique nos ícones com contadores<br> para coletar tributos (1 punho).</html>");
        instrucoes2.setFont(EstiloImperial.FONTE_TEXTO);
        instrucoes2.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        JLabel instrucoes3 = new JLabel("<html>• Clique numa cidade aberta para<br> saquear com espadas.</html>");
        instrucoes3.setFont(EstiloImperial.FONTE_TEXTO);
        instrucoes3.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        
        ferramentas.add(instrucoes1);
        ferramentas.add(instrucoes2);
        ferramentas.add(instrucoes3);

        salvar = EstiloImperial.criarBotaoImperial("Salvar partida");
        salvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                abrirOpcoesSalvar();
            }
        });
        ferramentas.add(salvar);



        lateral.add(indicadorVez, BorderLayout.CENTER);
        lateral.add(ferramentas, BorderLayout.NORTH);
        lateral.add(estado, BorderLayout.SOUTH);

        add(lateral, BorderLayout.EAST);
    }

    /** Jochi escolhe no primeiro turno clicando diretamente em uma das cartas. */
    private void solicitarEscolhaInicialJochi() {
        if (partida == null || escolhaInicialJochiAberta || partida.isPartidaEncerrada()) {
            return;
        }
        java.util.List<br.herdeiros.model.ResumoMelhoria> opcoes =
                partida.consultarOpcoesIniciaisJochi();
        if (opcoes.isEmpty()) {
            return;
        }
        escolhaInicialJochiAberta = true;
        try {
            javax.swing.JDialog escolha = new javax.swing.JDialog(javax.swing.SwingUtilities.getWindowAncestor(this),
                    "Jochi - escolha sua melhoria inicial", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
            JPanel conteudo = EstiloImperial.criarPainelFundo(new BorderLayout(12, 12));
            conteudo.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            JLabel instrucao = new JLabel("Clique diretamente na imagem da melhoria desejada.");
            instrucao.setForeground(EstiloImperial.COR_DOURADA_IMPERIAL);
            instrucao.setFont(EstiloImperial.FONTE_SUBTITULO);
            conteudo.add(instrucao, BorderLayout.NORTH);
            JPanel cartas = EstiloImperial.criarPainelFundo(new GridLayout(1, opcoes.size(), 14, 0));
            for (int i = 0; i < opcoes.size(); i++) {
                final int indice = i;
                br.herdeiros.model.ResumoMelhoria melhoria = opcoes.get(i);
                JPanel carta = EstiloImperial.criarPainelFundo(new BorderLayout(8, 8));
                javax.swing.JButton imagem = new javax.swing.JButton("Imagem indisponivel");
                imagem.setBackground(EstiloImperial.COR_FUNDO_CAMPO);
                imagem.setForeground(EstiloImperial.COR_TEXTO_CLARO);
                imagem.setBorder(BorderFactory.createLineBorder(
                        EstiloImperial.COR_DOURADA_IMPERIAL, 2));
                imagem.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                imagem.setPreferredSize(new Dimension(240, 145));
                try {
                    java.awt.image.BufferedImage arte = javax.imageio.ImageIO.read(
                            Paths.get("assets", "melhorias", melhoria.getCodigo() + ".jpg").toFile());
                    if (arte != null) {
                        imagem.setText("");
                        imagem.setIcon(new javax.swing.ImageIcon(arte.getScaledInstance(
                                230, 135, java.awt.Image.SCALE_SMOOTH)));
                    }
                } catch (IOException erro) {
                    // O beneficio continua visivel se faltar a imagem.
                }
                imagem.addActionListener(evento -> {
                    if (partida.escolherMelhoriaInicialJochi(indice)) {
                        escolha.dispose();
                    }
                });
                JLabel beneficio = new JLabel("<html><center>" + melhoria.getDescricao()
                        + "</center></html>", JLabel.CENTER);
                beneficio.setFont(EstiloImperial.FONTE_TEXTO_NEGRITO);
                beneficio.setForeground(EstiloImperial.COR_DOURADA_IMPERIAL);
                carta.add(imagem, BorderLayout.CENTER);
                carta.add(beneficio, BorderLayout.SOUTH);
                cartas.add(carta);
            }
            conteudo.add(cartas, BorderLayout.CENTER);
            escolha.setContentPane(conteudo);
            escolha.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
            escolha.pack();
            escolha.setSize(Math.max(560, escolha.getWidth()), Math.max(265, escolha.getHeight()));
            escolha.setLocationRelativeTo(this);
            escolha.setVisible(true);
        } finally {
            escolhaInicialJochiAberta = false;
        }
    }

    private void solicitarPeaoAltani() {
        if (partida == null || partida.isPartidaEncerrada()
                || partida.getPersonagemJogador(partida.getIndiceJogadorAtual())
                        != Personagem.ALTANI
                || partida.escolhaPeaoAltaniRealizada()
                || partida.consultarAtivacaoJogadorAtual().getColunaAtiva() >= 0) {
            return;
        }
        String chave = partida.getIndiceJogadorAtual() + ":" + partida.consultarRodadasConcluidas();
        if (chave.equals(ultimaEscolhaAltani)) {
            return;
        }
        ultimaEscolhaAltani = chave;
        Object[] nomes = {"Peão 1", "Peão 2"};
        int escolha = JOptionPane.showOptionDialog(this,
                "Altani: escolha qual dos dois peoes vai agir neste turno.",
                "Escolha do peao", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, nomes, nomes[0]);
        partida.escolherPeaoAtivoAltani(escolha == 1);
    }

    private void executarHabilidadeDoPersonagem() {
        if (partida == null || !partida.habilidadeEspecialDisponivel()) {
            return;
        }
        Personagem personagem = partida.getPersonagemJogador(partida.getIndiceJogadorAtual());
        if (personagem == Personagem.CHAGATAI) {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Trocar 1 cavalo restante por 1 espada?",
                    "Habilidade de Chagatai", JOptionPane.YES_NO_OPTION);
            if (resposta == JOptionPane.YES_OPTION && !partida.usarHabilidadeChagatai()) {
                JOptionPane.showMessageDialog(this, "Ative uma coluna com cavalos restantes.");
            }
        } else if (personagem == Personagem.OGEDEI) {
            if (controlador == null || !controlador.alternarModoHabilidadeOgedei()) {
                JOptionPane.showMessageDialog(this,
                        "A habilidade nao esta disponivel neste turno.");
                return;
            }
            JOptionPane.showMessageDialog(this,
                    "Clique diretamente na ficha de tesouro desejada no tabuleiro.\n"
                    + "Custa 1 moeda e a cidade precisa conservar pelo menos 1 tesouro.",
                    "Habilidade de Ogedei", JOptionPane.INFORMATION_MESSAGE);

        }
    }

    private void abrirFerramentasDesenvolvedor() {
        if (!modoDesenvolvedor || partida == null) {
            return;
        }
        if (janelaDesenvolvedor != null && janelaDesenvolvedor.isDisplayable()) {
            janelaDesenvolvedor.toFront();
            return;
        }
        java.awt.Window dono = SwingUtilities.getWindowAncestor(this);
        janelaDesenvolvedor = new JanelaDesenvolvedor(dono, partida);
        janelaDesenvolvedor.setVisible(true);
    }

    public void fecharJanelasAuxiliares() {
        if (partida != null) {
            partida.removerObservador(this);
            if (painel != null) {
                partida.removerObservador(painel);
            }
        }
        if (janelaDesenvolvedor != null) {
            janelaDesenvolvedor.dispose();
            janelaDesenvolvedor = null;
        }
        if (janelaMelhorias != null) {
            janelaMelhorias.dispose();
            janelaMelhorias = null;
        }
        if (janelaCartela != null) {
            janelaCartela.dispose();
            janelaCartela = null;
        }
        if (janelaCidades != null) {
            janelaCidades.dispose();
            janelaCidades = null;
        }
    }

    private void mostrarResultadoEReiniciar() {
        java.awt.Window dono = SwingUtilities.getWindowAncestor(this);
        JFrame frame = dono instanceof JFrame ? (JFrame) dono : null;
        new JanelaResultadoFinal(frame, partida.consultarResultadoFinal()).setVisible(true);
        int resposta = JOptionPane.showConfirmDialog(frame,
                "Desejam iniciar uma nova partida?", "Fim da partida",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resposta == JOptionPane.YES_OPTION) {
            if (janelaPrincipal != null) {
                janelaPrincipal.voltarAoMenuInicial();
            } else if (frame != null) {
                frame.dispose();
            }
        } else if (frame != null) {
            frame.dispose();
        }
    }

    private void abrirJanelaMelhorias() {
        if (partida == null) {
            return;
        }
        if (janelaMelhorias != null && janelaMelhorias.isDisplayable()) {
            janelaMelhorias.toFront();
            return;
        }
        janelaMelhorias = new JanelaMelhorias(partida);
        janelaMelhorias.setVisible(true);
    }

    private void abrirOpcoesSalvar() {
        if (partida == null || !partida.podeSalvarEntreTurnos()) {
            JOptionPane.showMessageDialog(this,
                    "So e possivel salvar antes de iniciar o turno do jogador.");
            return;
        }
        Object[] opcoes = {"Salvar no slot local", "Exportar para arquivo", "Cancelar"};
        int escolha = JOptionPane.showOptionDialog(this,
                "Escolha onde gravar a partida.",
                "Salvar partida", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
        if (escolha == 0) {
            salvarLocalManualmente();
        } else if (escolha == 1) {
            exportarSave();
        }
    }

    private void salvarLocalManualmente() {
        if (GerenciadorSave.existeSaveLocal()) {
            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "O save local existente será substituído. Deseja continuar?",
                    "Confirmar substituição", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            GerenciadorSave.salvarLocal(partida);
            JOptionPane.showMessageDialog(this,
                    "Partida salva em " + GerenciadorSave.getCaminhoSaveLocal(),
                    "Save concluído", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | IllegalArgumentException erro) {
            JOptionPane.showMessageDialog(this, erro.getMessage(),
                    "Erro ao salvar partida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarAutomaticamente() {
        try {
            GerenciadorSave.salvarLocal(partida);
        } catch (IOException | IllegalArgumentException erro) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível atualizar o autosave local: " + erro.getMessage(),
                    "Erro no salvamento automático", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarSave() {
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Exportar save da partida");
        seletor.setFileFilter(new FileNameExtensionFilter("Partida TXT (*.txt)", "txt"));
        seletor.setSelectedFile(new File("partida.txt"));
        if (seletor.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destino = seletor.getSelectedFile().toPath();
        if (!destino.getFileName().toString().toLowerCase().endsWith(".txt")) {
            destino = destino.resolveSibling(destino.getFileName().toString() + ".txt");
        }
        if (Files.exists(destino)) {
            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "O arquivo selecionado será substituído. Deseja continuar?",
                    "Confirmar substituição", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            GerenciadorSave.salvar(destino, partida);
            JOptionPane.showMessageDialog(this, "Save exportado com sucesso.",
                    "Exportação concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | IllegalArgumentException erro) {
            JOptionPane.showMessageDialog(this, erro.getMessage(),
                    "Erro ao exportar save", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Janela informativa das cidades disponiveis, atualizada via Observer. */
    private void abrirJanelaCidades() {
        if (partida == null) {
            return;
        }
        if (janelaCidades != null && janelaCidades.isDisplayable()) {
            janelaCidades.toFront();
            return;
        }
        janelaCidades = new JanelaCidades(partida);
        janelaCidades.setVisible(true);
    }

    /** Abre uma cartela que acompanha automaticamente o jogador da vez. */
    private void abrirCartelaDoJogador() {
        if (partida == null) {
            return;
        }
        if (janelaCartela != null && janelaCartela.isDisplayable()) {
            janelaCartela.toFront();
            return;
        }
        try {
            janelaCartela = new JanelaCartela(partida,
                    () -> executarHabilidadeDoPersonagem());
            janelaCartela.setVisible(true);
        } catch (IOException erro) {
            JOptionPane.showMessageDialog(this,
                    "Nao foi possivel abrir as imagens das cartelas: " + erro.getMessage(),
                    "Erro ao abrir cartela", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicializa uma nova partida.
     */
    public void iniciarPartida(String[] nomes, Cor[] cores) throws IOException {
        iniciarPartida(quantidadeJogadores, nomes, cores, null, false);
    }

    public void iniciarPartida(String[] nomes, Cor[] cores,
                              Personagem[] personagens) throws IOException {
        iniciarPartida(quantidadeJogadores, nomes, cores, personagens, true);
    }

    public void iniciarPartida(int quantidadeJogadores, String[] nomes, Cor[] cores,
                              Personagem[] personagens) throws IOException {
        iniciarPartida(quantidadeJogadores, nomes, cores, personagens, true);
    }

    private void iniciarPartida(int quantidadeJogadores, String[] nomes, Cor[] cores,
                               Personagem[] personagens, boolean ordemAleatoria) throws IOException {

        if (quantidadeJogadores != this.quantidadeJogadores
                || nomes == null || cores == null
                || quantidadeJogadores != nomes.length
                || quantidadeJogadores != cores.length) {
            throw new IllegalArgumentException(
                    "A quantidade de jogadores deve corresponder aos nomes e cores informados.");
        }

        if (janelaMelhorias != null) {
            janelaMelhorias.dispose();
            janelaMelhorias = null;
        }
        if (janelaCidades != null) {
            janelaCidades.dispose();
            janelaCidades = null;
        }

        if (janelaCartela != null) {
            janelaCartela.dispose();
            janelaCartela = null;
        }

        // Criação do Model
        if (partida != null) {
            partida.removerObservador(this);
            if (painel != null) {
                partida.removerObservador(painel);
                remove(painel);
            }
        }
        partida = GerenciadorPartida.getInstancia()
                .iniciarPartida(nomes, cores, personagens, ordemAleatoria);
        
        this.resultadoExibido = false;
        
        configurarPartida();
    }

    private void configurarPartida() throws IOException {
        configurarPartida(true);
    }

    private void configurarPartida(boolean configurarModelo) throws IOException {
        // Carregamento do mapa usado pela interface e, em partidas novas, pelo Model.
        Path caminho = Paths.get(
            "assets",
            "mapa_paradas.csv"
        );

        // Em partidas novas, o Model inicia com um mapa vazio (apenas Karakorum).
        // Esse mapa nao pode substituir o CSV oficial do tabuleiro.
        // Somente uma partida recuperada deve reutilizar seu mapa persistido.
        MapaDados mapaSalvo = configurarModelo ? null : partida.getMapaDadosSave();
        if (mapaSalvo != null) {
            mapa = mapaSalvo;
        } else if (Files.exists(caminho)) {
            mapa = ArquivoMapa.carregar(caminho);
        } else {
            mapa = new MapaDados();
        }

        if (configurarModelo) {
            if (!partida.carregarMapa(mapa)) {
                throw new IOException("O mapa não pôde ser carregado no Model.");
            }
            carregarAcessosCidades();
            carregarProvincias();
            carregarEstoquesAdicionais();
        } else if (mapaSalvo == null) {
            partida.definirMapaDadosSave(mapa);
        }

        // Criação do Controller
        controlador = new ControladorTabuleiro(
            partida,
            mapa
        );

        // Criação da View do tabuleiro
        painel = new PainelTabuleiro(partida, mapa, controlador);

        controlador.configurar(
            painel,
            estado,
            indicadorVez
        );
        controlador.habilitarAtivacaoReal();

        partida.registrarObservador(this);
        partida.registrarObservador(painel);
        // A construcao conclui dentro do listener do Controller; sincronize o botao apos o clique.
        painel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evento) {
                SwingUtilities.invokeLater(() -> {
                    if (construirYurt != null && controlador != null) {
                        construirYurt.setText(controlador.isModoConstrucaoYurt()
                                ? "Cancelar yurt" : "Construir yurt");
                    }
                });
            }
        });
        atualizar(partida);

        // Adiciona o painel à interface
        add(painel, BorderLayout.CENTER);

        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            solicitarEscolhaInicialJochi();
            solicitarPeaoAltani();
        });
    }

    /** Cadastro dos acessos às cidades: o mapa e a imagem oficial não ficam no Model. */
    private void carregarAcessosCidades() throws IOException {
        Path arquivo = Paths.get("assets", "cidades_paradas.csv");
        if (!Files.exists(arquivo)) {
            throw new IOException("Arquivo de acessos das cidades ausente: " + arquivo);
        }
        java.util.List<String> linhas = Files.readAllLines(arquivo);
        for (String linha : linhas) {
            String limpa = linha.trim();
            if (limpa.length() == 0 || limpa.startsWith("#")) {
                continue;
            }
            String[] partes = limpa.split(";");
            if (partes.length < 2) {
                throw new IOException("Cadastro inválido de cidade: " + linha);
            }
            for (int indice = 1; indice < partes.length; indice++) {
                int parada;
                try {
                    parada = Integer.parseInt(partes[indice].trim());
                } catch (NumberFormatException erro) {
                    throw new IOException("Identificador de parada inválido: " + linha, erro);
                }
                if (!partida.configurarAcessoCidade(partes[0], parada)) {
                    throw new IOException("Cidade ou parada inválida no cadastro: " + linha);
                }
            }
        }
    }

    /** Estoques adicionais por tipo, configurados sem duplicar dados na View. */
    private void carregarEstoquesAdicionais() throws IOException {
        Path arquivo = Paths.get("assets", "estoques_adicionais.csv");
        if (!Files.exists(arquivo)) {
            return;
        }
        for (String linha : Files.readAllLines(arquivo)) {
            String limpa = linha.trim();
            if (limpa.isEmpty() || limpa.startsWith("#")) {
                continue;
            }
            String[] partes = limpa.split(";");
            if (partes.length != 3) {
                throw new IOException("Estoque adicional invalido: " + linha);
            }
            try {
                Provincia provincia = Provincia.valueOf(partes[0].trim());
                int quantidade = Integer.parseInt(partes[2].trim());
                if (!partida.configurarTributoAdicional(provincia, partes[1], quantidade)) {
                    throw new IOException("Estoque adicional recusado: " + linha);
                }
            } catch (IllegalArgumentException erro) {
                throw new IOException("Estoque adicional invalido: " + linha, erro);
            }
        }
    }

    /** As relações visuais estão em CSV, fora do Model e sem números fixos no Java. */
    private void carregarProvincias() throws IOException {
        Path arquivo = Paths.get("assets", "provincias_paradas.csv");
        if (!Files.exists(arquivo)) {
            throw new IOException("Falta o mapa de províncias: " + arquivo);
        }
        java.util.List<String> linhas = Files.readAllLines(arquivo);
        for (String linha : linhas) {
            String limpa = linha.trim();
            if (limpa.length() == 0 || limpa.startsWith("#")) {
                continue;
            }
            String[] campos = limpa.split(";");
            if (campos.length == 4 && campos[0].equals("V")) {
                try {
                    Provincia origem = Provincia.valueOf(campos[1].trim());
                    Provincia primeira = Provincia.valueOf(campos[2].trim());
                    Provincia segunda = Provincia.valueOf(campos[3].trim());
                    if (!partida.configurarVizinhasKhan(origem, primeira, segunda)) {
                        throw new IOException("Vizinhança de Khan inválida: " + linha);
                    }
                    continue;
                } catch (IllegalArgumentException erro) {
                    throw new IOException("Província de Khan inválida: " + linha, erro);
                }
            }
            if (campos.length < 4) {
                throw new IOException("Linha de província incompleta: " + linha);
            }
            try {
                Provincia provincia = Provincia.valueOf(campos[0].trim());
                int estoque = Integer.parseInt(campos[2].trim());
                if (!partida.configurarProvincia(provincia, campos[1], estoque)) {
                    throw new IOException("Província inválida: " + linha);
                }
                for (int i = 3; i < campos.length; i++) {
                    int parada = Integer.parseInt(campos[i].trim());
                    if (!partida.configurarAcessoProvincia(provincia, parada)) {
                        throw new IOException("Parada inválida na província: " + linha);
                    }
                }
            } catch (IllegalArgumentException erro) {
                throw new IOException("Erro no cadastro de província: " + linha, erro);
            }
        }
        for (Provincia provincia : Provincia.values()) {
            if (partida.consultarEstoqueProvincia(provincia) < 0) {
                throw new IOException("Província ausente no arquivo: " + provincia.name());
            }
        }
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        if (controlador != null) {
            controlador.atualizarEstado();
        }
        if (partida != null) {
            SwingUtilities.invokeLater(() -> {
                solicitarEscolhaInicialJochi();
                solicitarPeaoAltani();
            });
            if (salvar != null) {
                salvar.setEnabled(partida.podeSalvarEntreTurnos());
            }
            if (controlador != null && construirYurt != null) {
                construirYurt.setText(controlador.isModoConstrucaoYurt()
                        ? "Cancelar yurt" : "Construir yurt");
            }
            DestinoKhan destino = partida.consultarDestinoKhan();
            if (destino == null) {
                posicaoKhan.setText("Khan: fora do tabuleiro");
            } else {
                posicaoKhan.setText("Khan: " + destino.getDescricao());
            }
        }
        
        if (partida.isPartidaEncerrada() && !resultadoExibido) {
            resultadoExibido = true;
            SwingUtilities.invokeLater(() -> mostrarResultadoEReiniciar());
        }
    }
}
