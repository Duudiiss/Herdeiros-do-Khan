package br.herdeiros.view;

import br.herdeiros.model.Cor;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.Personagem;

import br.herdeiros.view.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.AWTEventListener;

import java.io.*;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.Flushable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputFilter;
import java.io.ObjectInputFilter.FilterInfo;
import java.io.ObjectInputValidation;
import java.io.ObjectOutput;
import java.io.ObjectStreamConstants;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.*;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;

//import jdk.internal.vm.VMSupport.IOReader;

public class Janela extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CardLayout cardLayout;
    private JPanel painelContainer;
    
    private JanelaJogo janelaJogo;
    private JanelaInicial janelaInicial;
    
	public Janela() throws IOException{
		super("Herdeiros de Khan");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 900);
        setLocationRelativeTo(null);
        
        
        cardLayout = new CardLayout();
        painelContainer = new JPanel(cardLayout);
        
        janelaInicial = new JanelaInicial(this);
		
		painelContainer.add(janelaInicial, "Menu");
		
		setContentPane(painelContainer);
		cardLayout.show(painelContainer, "Menu");
	}
	
	public static void iniciar()
	{
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    EstiloImperial.configurarDialogos();
                    new Janela().setVisible(true);
                } catch (IOException erro) {
                    erro.printStackTrace();
                }
            }
        });
	}
	
	private void mostrarTabuleiro() {
        cardLayout.show(painelContainer, "Tabuleiro");
    }

    public void iniciarPartida(int quantidadeJogadores, String[] nomes, Cor[] cores,
                              Personagem[] personagens) throws IOException {
        iniciarPartida(quantidadeJogadores, nomes, cores, personagens, false);
    }

    public void iniciarPartida(int quantidadeJogadores, String[] nomes, Cor[] cores,
                              Personagem[] personagens, boolean desenvolvedor) throws IOException {
        exibirPartida(new JanelaJogo(quantidadeJogadores, nomes, cores, personagens, desenvolvedor, this));
    }

    public void carregarPartida(InterfacePartida partida) throws IOException {
        carregarPartida(partida, false);
    }

    public void carregarPartida(InterfacePartida partida, boolean desenvolvedor) throws IOException {
        exibirPartida(new JanelaJogo(partida, desenvolvedor, this));
    }

    /** Volta ao menu com uma nova tela e sem reaproveitar configuracoes da partida anterior. */
    public void voltarAoMenuInicial() {
        if (janelaJogo != null) {
            janelaJogo.fecharJanelasAuxiliares();
            painelContainer.remove(janelaJogo);
            janelaJogo = null;
        }
        painelContainer.remove(janelaInicial);
        try {
            janelaInicial = new JanelaInicial(this);
        } catch (IOException erro) {
            JOptionPane.showMessageDialog(this, erro.getMessage(), "Erro no menu", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        painelContainer.add(janelaInicial, "Menu");
        cardLayout.show(painelContainer, "Menu");
        painelContainer.revalidate();
        painelContainer.repaint();
    }

    private void exibirPartida(JanelaJogo novaJanela) {
        if (janelaJogo != null) {
            janelaJogo.fecharJanelasAuxiliares();
            painelContainer.remove(janelaJogo);
        }
        janelaJogo = novaJanela;
        painelContainer.add(janelaJogo, "Tabuleiro");
        mostrarTabuleiro();
    }
}


//	@Override
//	public void eventDispatched(AWTEvent event) {
		// TODO Auto-generated method stub

//	}

//}
