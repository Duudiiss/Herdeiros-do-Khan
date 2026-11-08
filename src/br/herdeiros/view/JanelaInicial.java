package br.herdeiros.view;

import br.herdeiros.model.Cor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JanelaInicial extends JPanel {

    private static final long serialVersionUID = -3262712126867002261L;

    private int numeroJogadores = 2;
    
    private JButton Iniciar_btn;
    private JButton Sair_btn;
    private JLabel qtdJogadores_lbl;
    private JButton Mais_btn;
    private JButton Menos_btn;
    
    // Listas para armazenar os campos dinâmicos de cada jogador
    private List<JTextField> camposNomes;
    private List<JComboBox<String>> seletoresCores;
    
    // Painel interno que vai guardar a lista dinâmica de inputs dos jogadores
    private JPanel painelInputsJogadores;
    
    private Image BackImage;
    
    private Janela janelaPrincipal; // Referência guardada para usar ao iniciar

    public JanelaInicial(Janela janela) throws IOException {
        this.janelaPrincipal = janela;
        this.camposNomes = new ArrayList<>();
        this.seletoresCores = new ArrayList<>();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Carrega a imagem de fundo.
        File arquivoImg = new File(System.getProperty("user.dir") + "/assets/tela_inicial.jpg");
        if (!arquivoImg.exists()) {
            System.err.println("Arquivo de imagem não encontrado no classpath: /assets/tela_inicial.jpg");
        } else {
            try {
                BackImage = ImageIO.read(arquivoImg);
            } catch (IOException e) {
                System.err.println("Erro ao ler a imagem de fundo: " + e.getMessage());
            }
        }

        JPanel painelBackgroundH = new JPanel();
        painelBackgroundH.setLayout(new BoxLayout(painelBackgroundH, BoxLayout.X_AXIS));
        painelBackgroundH.setAlignmentY(Component.TOP_ALIGNMENT);
        painelBackgroundH.setOpaque(false);
        
        // Painel que contém os controles.
        JPanel painelBackground = new JPanel();
        painelBackground.setLayout(new BoxLayout(painelBackground, BoxLayout.Y_AXIS));
        painelBackground.setOpaque(false);
        painelBackground.setAlignmentY(Component.TOP_ALIGNMENT);
        
        // Painel H (Textos e Seleção de Quantidade)
        JPanel painelH = new JPanel();
        painelH.setLayout(new BoxLayout(painelH, BoxLayout.Y_AXIS));
        painelH.setOpaque(false);
        
        // Painel central para os inputs dinâmicos de nomes e cores
        painelInputsJogadores = new JPanel();
        painelInputsJogadores.setLayout(new BoxLayout(painelInputsJogadores, BoxLayout.Y_AXIS));
        painelInputsJogadores.setOpaque(false);

        // Painel V (Botões de Ação)
        JPanel painelV = new JPanel();
        painelV.setLayout(new BoxLayout(painelV, BoxLayout.X_AXIS));
        painelV.setOpaque(false);

        // --- ESTILO ÉPICO / MONGOL ---
        Font fonteEpicaTitulo = new Font("Serif", Font.BOLD, 36);
        Font fonteEpicaBotoes = new Font("Serif", Font.BOLD, 26);
        
        Color corDouradaImperial = new Color(219, 161, 121); 
        Color corMarromCouro = new Color(85, 40, 10);
        Color corMarromHover = new Color(90, 45, 15);
        Color corTextoClaro = new Color(245, 240, 230);
        Color corBordaTexto = new Color(112, 84, 63);
        
        // Texto de configuração
        JLabel textoJogadores = criarLabelComBorda("Número de jogadores", fonteEpicaTitulo, corDouradaImperial, corBordaTexto);
        textoJogadores.setAlignmentX(Component.CENTER_ALIGNMENT);
        textoJogadores.setOpaque(false);

        // Controles para selecionar quantidade de jogadores
        JPanel selecao = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        selecao.setOpaque(false);

        Menos_btn = criarBotaoEstilizado("-", fonteEpicaBotoes, corMarromCouro, corMarromHover, corDouradaImperial);
        qtdJogadores_lbl = new JLabel(String.valueOf(numeroJogadores));
        Mais_btn = criarBotaoEstilizado("+", fonteEpicaBotoes, corMarromCouro, corMarromHover, corDouradaImperial);

        qtdJogadores_lbl.setFont(new Font("Serif", Font.BOLD, 30));
        qtdJogadores_lbl.setForeground(corTextoClaro);

        Menos_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                if (numeroJogadores > 2) {
                    numeroJogadores--;
                    atualizarNumeroJogadores();
                }
            }
        });

        Mais_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                if (numeroJogadores < 5) {
                    numeroJogadores++;
                    atualizarNumeroJogadores();
                }
            }
        });

        selecao.add(Menos_btn);
        selecao.add(qtdJogadores_lbl);
        selecao.add(Mais_btn);

        // Constrói os inputs iniciais (para 2 jogadores)
        atualizarNumeroJogadores();

        // Botão para iniciar a partida (Coleta os dados e envia para a JanelaJogo)
        Iniciar_btn = criarBotaoEstilizado("Iniciar partida", fonteEpicaBotoes, corMarromCouro, corMarromHover, corDouradaImperial);
        Iniciar_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
            List<String> nomes = new ArrayList<>();
            List<Cor> cores = new ArrayList<>();

            String[] opcoesCoresNomes = {"Vermelho", "Azul", "Verde", "Amarelo", "Preto", "Laranja"};

            for (int i = 0; i < camposNomes.size(); i++) {
                String nome = camposNomes.get(i).getText().trim();
                if (nome.isEmpty()) {
                    nome = "Jogador " + (i + 1); // Nome padrão se estiver vazio
                }
                nomes.add(nome);

                // Mapeia a cor selecionada no JComboBox para um objeto java.awt.Color
                String corStr = (String) seletoresCores.get(i).getSelectedItem();
                cores.add(converterStringParaColor(corStr));
            }
            
            if (new java.util.HashSet<Cor>(cores).size() != cores.size()) {
                JOptionPane.showMessageDialog(JanelaInicial.this,
                    "Escolha uma cor diferente para cada jogador.",
                    "Cores repetidas", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //mandar essas informações para JanelaJogo
            String[] nomesArray = nomes.toArray(new String[0]);
            Cor[] coresArray = cores.toArray(new Cor[0]);
            try {
                janela.iniciarPartida(nomesArray, coresArray);
            } catch (IOException erro) {
                JOptionPane.showMessageDialog(JanelaInicial.this,
                    "Nao foi possivel carregar o mapa: " + erro.getMessage(),
                    "Erro na partida", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException erro) {
                JOptionPane.showMessageDialog(JanelaInicial.this,
                    erro.getMessage(), "Configuracao invalida",
                    JOptionPane.WARNING_MESSAGE);
            }
            }
        });

        // Botão para sair
        Sair_btn = criarBotaoEstilizado("Sair", fonteEpicaBotoes, new Color(80, 20, 20), new Color(110, 30, 30), Color.WHITE);
        Sair_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                janela.dispose();
            }
        });

        // Montagem do menu principal
        painelH.add(textoJogadores);
        painelH.add(Box.createVerticalStrut(5));
        painelH.add(selecao);
        painelH.add(Box.createVerticalStrut(15));
        
        painelV.add(Iniciar_btn);
        painelV.add(Box.createRigidArea(new Dimension(15, 0)));
        painelV.add(Sair_btn);
        
        painelBackground.add(Box.createVerticalStrut(10));
        painelBackground.add(painelH);
        painelBackground.add(painelV);
        painelBackground.add(Box.createVerticalStrut(10));
        
        painelBackgroundH.add(painelBackground);
        painelBackgroundH.add(painelInputsJogadores); // Adiciona a área dinâmica de nomes/cores
        
        // --- POSICIONAMENTO: CENTRO-ESQUERDO ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST; 
        gbc.insets = new Insets(0, 75, 0, 0); 

        add(painelBackgroundH, gbc);
    }

    // Método que reconstrói os campos de texto e seleção de cor dinamicamente
    private void atualizarNumeroJogadores() {
        if (qtdJogadores_lbl != null) {
            qtdJogadores_lbl.setText(String.valueOf(numeroJogadores));
        }

        if (painelInputsJogadores != null) {
            painelInputsJogadores.removeAll();
            camposNomes.clear();
            seletoresCores.clear();

            String[] coresDisponiveis = {"Vermelho", "Azul", "Verde", "Amarelo", "Preto", "Laranja"};

            painelInputsJogadores.add(Box.createVerticalStrut(20));
            
            for (int i = 1; i <= numeroJogadores; i++) {
                JPanel linhaJogador = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
                linhaJogador.setOpaque(false);

                JTextField campoNome = new JTextField("Jogador " + i, 10);
                campoNome.setFont(new Font("Serif", Font.PLAIN, 16));

                JComboBox<String> seletorCor = new JComboBox<>(coresDisponiveis);
                // Define cores padrão diferentes para cada jogador para evitar repetição inicial
                seletorCor.setSelectedIndex((i - 1) % coresDisponiveis.length);
                seletorCor.setFont(new Font("Serif", Font.PLAIN, 14));

                camposNomes.add(campoNome);
                seletoresCores.add(seletorCor);

                linhaJogador.add(campoNome);
                linhaJogador.add(seletorCor);

                painelInputsJogadores.add(linhaJogador);
            }
            painelInputsJogadores.setAlignmentY(Component.TOP_ALIGNMENT);
            painelInputsJogadores.setMaximumSize(new Dimension(200, 20+(numeroJogadores*28)));
            painelInputsJogadores.revalidate();
            painelInputsJogadores.repaint();
        }
    }

    // Auxiliar para converter texto do JComboBox em java.awt.Color
    private Cor converterStringParaColor(String nomeCor) {
        switch (nomeCor.toLowerCase()) {
            case "vermelho": return Cor.VERMELHO;
            case "azul": return Cor.AZUL;
            case "verde": return Cor.VERDE;
            case "amarelo": return Cor.AMARELO;
            case "preto": return Cor.PRETO;
            case "branco": return Cor.BRANCO;
            case "laranja": return Cor.LARANJA;
            default: return Cor.BRANCO;
        }
    }

    // Método auxiliar para criar botões customizados
    private JButton criarBotaoEstilizado(String texto, Font fonte, Color fundoNormal, Color fundoHover, Color corTexto) {
        JButton botao = new JButton(texto) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(fundoHover.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(fundoHover);
                } else {
                    g2.setColor(fundoNormal);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(219, 161, 121, 180));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        botao.setFont(fonte);
        botao.setForeground(corTexto);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setContentAreaFilled(false);
        botao.setOpaque(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        return botao;
    }
    
    // Método auxiliar que cria a label com borda
    private JLabel criarLabelComBorda(String texto, Font fonte, Color corTexto, Color corBorda) {
        int espessuraBorda = 1;
        JLabel label = new JLabel(texto, SwingConstants.CENTER) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setFont(getFont());
                String textoLbl = getText();
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(textoLbl)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.setColor(corBorda);
                for (int dx = -espessuraBorda; dx <= espessuraBorda; dx++) {
                    for (int dy = -espessuraBorda; dy <= espessuraBorda; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2.drawString(textoLbl, x + dx, y + dy);
                        }
                    }
                }

                g2.setColor(corTexto);
                g2.drawString(textoLbl, x, y);
                g2.dispose();
            }
        };
        
        label.setFont(fonte);
        FontMetrics fm = label.getFontMetrics(fonte);
        label.setPreferredSize(new Dimension(fm.stringWidth(texto) + 25, fm.getHeight() + 10));

        return label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (BackImage != null) {
            g2.drawImage(BackImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setPaint(new GradientPaint(
                0, 0, new Color(30, 70, 100),
                0, getHeight(), new Color(10, 20, 40)
            ));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }
}