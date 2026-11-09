package br.herdeiros.view;

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

import javax.imageio.*;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;

//import jdk.internal.vm.VMSupport.IOReader;

public class Janela extends JFrame{
	
	private CardLayout cardLayout;
    private JPanel painelContainer;
    
	public Janela() throws IOException{
		super("Herdeiros de Khan");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 900);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        painelContainer = new JPanel(cardLayout);
        
		JPanel painelInicial = new JanelaInicial(this);
		
		painelContainer.add(painelInicial, "Menu");
		
		setContentPane(painelContainer);
		cardLayout.show(painelContainer, "Menu");
	}
	
	public static void iniciar()
	{
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new Janela().setVisible(true);
                } catch (IOException erro) {
                    erro.printStackTrace();
                }
            }
        });
	}
	
	public void mostrarTabuleiro(int quantidadeJogadores) {
        try {
            painelContainer.add(new JanelaJogo(quantidadeJogadores), "Tabuleiro");
            cardLayout.show(painelContainer, "Tabuleiro");
        } catch (IOException erro) {
            JOptionPane.showMessageDialog(this, "Não foi possível carregar o tabuleiro: "
                    + erro.getMessage(), "Erro ao iniciar partida", JOptionPane.ERROR_MESSAGE);
        }
    }
}


//	@Override
//	public void eventDispatched(AWTEvent event) {
		// TODO Auto-generated method stub

//	}

//}
