package br.herdeiros.view;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class JanelaInicial extends JPanel {

    private static final long serialVersionUID = -3262712126867002261L;

    private int numeroJogadores = 2;
    
    private JButton Iniciar_btn;
    private JButton Sair_btn;
    private JLabel qtdJogadores_lbl;
    private JButton Mais_btn;
    private JButton Menos_btn;
    
    private Image BackImage;
    
    public JanelaInicial(Janela janela) throws IOException {

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

        // Painel que contém os controles.
        JPanel painelBackground = new JPanel();
        painelBackground.setLayout(new BoxLayout(painelBackground, BoxLayout.Y_AXIS));
        painelBackground.setOpaque(false);
        
        // Painel H (Textos e Seleção)
        JPanel painelH = new JPanel();
        painelH.setLayout(new BoxLayout(painelH, BoxLayout.Y_AXIS));
        painelH.setOpaque(false);
        
        // Painel V (Botões de Ação)
        JPanel painelV = new JPanel();
        painelV.setLayout(new BoxLayout(painelV, BoxLayout.X_AXIS));
        painelV.setOpaque(false);

        // --- ESTILO ÉPICO / MONGOL (Fontes e Cores) ---
        // 'Serif' robusto e elegante (garantido em qualquer Java/SO). Se preferir, substitua por "Pangolin" caso esteja instalada.
        Font fonteEpicaTitulo = new Font("Serif", Font.BOLD, 40);
        Font fonteEpicaBotoes = new Font("Serif", Font.BOLD, 30);
        
        // Cores temáticas (Dourado imperial, Marrom/Couro escuro e Branco marfim)
        Color corDouradaImperial = new Color(219, 161, 121); 
        Color corMarromCouro = new Color(85, 40, 10);
        Color corMarromHover = new Color(90, 45, 15);
        Color corTextoClaro = new Color(245, 240, 230);
        Color corBordaTexto = new Color(112, 84, 63);
        
        //-------------------------------------------------------------------------
        // Texto de configuração com borda estilizada
        JLabel textoJogadores = criarLabelComBorda(
                "Número de jogadores", 
                fonteEpicaTitulo, 
                corDouradaImperial, 
                corBordaTexto,
                1   
            );
        textoJogadores.setAlignmentX(Component.CENTER_ALIGNMENT);
        textoJogadores.setOpaque(false);
        
        // Controles para selecionar jogadores.
        JPanel selecao = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        selecao.setOpaque(false);

        Menos_btn = criarBotaoEstilizado("-", fonteEpicaBotoes, corMarromCouro, corMarromHover, corDouradaImperial);
        qtdJogadores_lbl = new JLabel(String.valueOf(numeroJogadores));
        Mais_btn = criarBotaoEstilizado("+", fonteEpicaBotoes, corMarromCouro, corMarromHover, corDouradaImperial);

        qtdJogadores_lbl.setFont(new Font("Serif", Font.BOLD, 32));
        qtdJogadores_lbl.setForeground(corTextoClaro);

        Menos_btn.addActionListener(e -> {
            if (numeroJogadores > 2) {
                numeroJogadores--;
                atualizarNumeroJogadores();
            }
        });

        Mais_btn.addActionListener(e -> {
            if (numeroJogadores < 6) {
                numeroJogadores++;
                atualizarNumeroJogadores();
            }
        });

        selecao.add(Menos_btn);
        selecao.add(qtdJogadores_lbl);
        selecao.add(Mais_btn);

        // Botão para iniciar a partida.
        Iniciar_btn = criarBotaoEstilizado("Iniciar partida", fonteEpicaBotoes, corMarromCouro, corMarromHover, corDouradaImperial);
        Iniciar_btn.addActionListener(e -> janela.mostrarTabuleiro());

        // Botão para sair.
        Sair_btn = criarBotaoEstilizado("Sair", fonteEpicaBotoes, new Color(80, 20, 20), new Color(110, 30, 30), Color.WHITE);
        Sair_btn.addActionListener(e -> janela.dispose());

        // Montagem do menu.
        painelH.add(Box.createVerticalStrut(35));
        painelH.add(textoJogadores);
        painelH.add(Box.createVerticalStrut(10));
        painelH.add(selecao);
        painelH.add(Box.createVerticalStrut(25));
        
        painelV.add(Iniciar_btn);
        painelV.add(Box.createRigidArea(new Dimension(15, 0))); // Espaço horizontal entre os botões
        painelV.add(Sair_btn);
        
        painelBackground.add(Box.createVerticalStrut(15)); // Margem superior interna
        painelBackground.add(painelH);
        painelBackground.add(painelV);
        painelBackground.add(Box.createVerticalStrut(15));
        
        // --- POSICIONAMENTO: CENTRO-ESQUERDO (Inalterado) ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST; 
        gbc.insets = new Insets(0, 75, 0, 0); 

        add(painelBackground, gbc);
    }
    
    // Método auxiliar para criar botões com visual rústico/temático sem o cinza padrão do Swing
    private JButton criarBotaoEstilizado(String texto, Font fonte, Color fundoNormal, Color fundoHover, Color corTexto) {
        JButton botao = new JButton(texto) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Muda a cor se o botão estiver pressionado ou com o mouse em cima
                if (getModel().isPressed()) {
                    g2.setColor(fundoHover.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(fundoHover);
                } else {
                    g2.setColor(fundoNormal);
                }
                
                // Desenha o fundo arredondado estilo placa de madeira/metal antigo
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Borda dourada ou detalhe
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
        botao.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16)); // Margem interna do botão
        
        return botao;
    }
    
    // Método auxiliar que cria a label já com a borda estilizada
    private JLabel criarLabelComBorda(String texto, Font fonte, Color corTexto, Color corBorda, int espessuraBorda) {
    	JLabel label = new JLabel(texto, SwingConstants.CENTER) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                String texto = getText();
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(texto)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.setFont(fonte);

                g2.setColor(corBorda); // Cor da borda
                int espessuraBorda = 1;   // Grossura da borda em pixels
                
                for (int dx = -espessuraBorda; dx <= espessuraBorda; dx++) {
                    for (int dy = -espessuraBorda; dy <= espessuraBorda; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2.drawString(texto, x + dx, y + dy);
                        }
                    }
                }

                // --- 2. DESENHA O TEXTO PRINCIPAL POR CIMA ---
                g2.setColor(corTexto);
                g2.drawString(texto, x, y);

                g2.dispose();
            }
        };
        // Aplica a fonte diretamente no JLabel para o Swing calcular a geometria certa
        label.setFont(fonte);
        
        // Calcula dinamicamente o tamanho necessário com folga para a borda não cortar
        FontMetrics fm = label.getFontMetrics(fonte);
        int larguraDesejada = fm.stringWidth(texto) + (espessuraBorda * 4) + 20;
        int alturaDesejada = fm.getHeight() + (espessuraBorda * 4) + 10;
        label.setPreferredSize(new Dimension(larguraDesejada, alturaDesejada));

        return label;
    }

    private void atualizarNumeroJogadores() {
        qtdJogadores_lbl.setText(String.valueOf(numeroJogadores));
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