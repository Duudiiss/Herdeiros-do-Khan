package br.herdeiros.view;

import java.awt.*;
//import java.awt.event.*;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.io.File;

import javax.swing.*;

public class JanelaInicial extends JPanel{

	/**
	 * 
	 */
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

        // Carrega a imagem de fundo.
        File arquivoImg = new File(System.getProperty("user.dir") + "/assets/tela_inicial.jpg");
        if (arquivoImg == null) {
            System.err.println("Arquivo de imagem não encontrado no classpath: /assets/tela_inicial.jpg");
        } else {
            try {
                BackImage = ImageIO.read(arquivoImg);
            } catch (IOException e) {
                System.err.println("Erro ao ler a imagem de fundo: " + e.getMessage());
            }
        }

        // Painel que contém os controles.
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);


        // Texto de configuração.
        JLabel textoJogadores = new JLabel("Número de jogadores");
        textoJogadores.setFont(new Font("SansSerif", Font.BOLD, 20));
        textoJogadores.setForeground(Color.WHITE);
        textoJogadores.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Controles para selecionar jogadores.
        JPanel selecao = new JPanel(new FlowLayout());
        selecao.setOpaque(false);

        Menos_btn = new JButton("-");
        qtdJogadores_lbl = new JLabel(
            String.valueOf(numeroJogadores)
        );
        Mais_btn = new JButton("+");

        qtdJogadores_lbl.setFont(
            new Font("SansSerif", Font.BOLD, 24)
        );
        qtdJogadores_lbl.setForeground(Color.WHITE);

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
        Iniciar_btn = new JButton("Iniciar partida");

        Iniciar_btn.addActionListener(e -> {
            janela.mostrarTabuleiro();
        });

        // Botão para sair.
        Sair_btn = new JButton("Sair");

        Sair_btn.addActionListener(e -> {
            janela.dispose();
        });

        // Montagem do menu.
        painel.add(Box.createVerticalStrut(35));
        painel.add(textoJogadores);
        painel.add(Box.createVerticalStrut(10));
        painel.add(selecao);
        painel.add(Box.createVerticalStrut(25));
        painel.add(Iniciar_btn);
        painel.add(Box.createVerticalStrut(10));
        painel.add(Sair_btn);

        add(painel);
    }

    private void atualizarNumeroJogadores() {
    	qtdJogadores_lbl.setText(
            String.valueOf(numeroJogadores)
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        if (BackImage != null) {
            g2.drawImage(
            		BackImage,
                0,
                0,
                getWidth(),
                getHeight(),
                this
            );
        } else {
            // Fundo alternativo se a imagem não existir.
            g2.setPaint(
                new GradientPaint(
                    0, 0, new Color(30, 70, 100),
                    0, getHeight(), new Color(10, 20, 40)
                )
            );

            g2.fillRect(
                0, 0, getWidth(), getHeight()
            );
        }

        g2.dispose();
	}
	//@Override
	//public void eventDispatched(AWTEvent event) {
		// TODO Auto-generated method stub

	//}

}
