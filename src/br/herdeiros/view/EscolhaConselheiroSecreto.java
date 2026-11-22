package br.herdeiros.view;

import br.herdeiros.model.CartaConselheiroSecreto;
import br.herdeiros.model.InterfacePartida;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

/** Escolha visual de uma entre as cartas secretas oferecidas ao jogador da vez. */
final class EscolhaConselheiroSecreto {
    private EscolhaConselheiroSecreto() {
    }

    static void mostrar(JFrame dono, InterfacePartida partida) {
        List<CartaConselheiroSecreto> opcoes =
                partida.consultarOpcoesConselheiroSecretoJogadorAtual();
        if (opcoes.isEmpty()) {
            return;
        }
        JDialog dialogo = new JDialog(dono, "Escolha seu conselheiro secreto", true);
        JPanel conteudo = EstiloImperial.criarPainelFundo(new BorderLayout(12, 12));
        conteudo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel instrucao = new JLabel("Clique na imagem da carta que deseja manter em segredo.");
        instrucao.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        conteudo.add(instrucao, BorderLayout.NORTH);

        JPanel cartas = EstiloImperial.criarPainelFundo(
                new GridLayout(1, opcoes.size(), 12, 0));
        for (int i = 0; i < opcoes.size(); i++) {
            final int indice = i;
            CartaConselheiroSecreto carta = opcoes.get(i);
            JPanel painel = EstiloImperial.criarPainelFundo(new BorderLayout(4, 6));
            // O botao estilizado padrao fixa a altura ao texto; para cartas
            // usamos JButton normal com tamanho proporcional a ilustracao.
            PainelImagemJava2D imagem = new PainelImagemJava2D();
            imagem.setImagem(ImagensConselheirosSecretos.imagem(carta, 175, 270));
            imagem.setPreferredSize(new java.awt.Dimension(188, 282));
            imagem.setBorder(BorderFactory.createLineBorder(
                    EstiloImperial.COR_DOURADA_IMPERIAL, 2));
            
            imagem.setCursor(java.awt.Cursor.getPredefinedCursor(
                    java.awt.Cursor.HAND_CURSOR));
            imagem.setToolTipText(carta.getDescricao());
            imagem.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent evento) { if (partida.escolherConselheiroSecretoJogadorAtual(indice)) {
                    dialogo.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialogo,
                            "Nao foi possivel selecionar esta carta.");
                } } });
            JLabel descricao = new JLabel("<html><center>"
                    + carta.getDescricao() + "</center></html>");
            descricao.setForeground(EstiloImperial.COR_TEXTO_CLARO);
            painel.add(imagem, BorderLayout.CENTER);
            painel.add(descricao, BorderLayout.SOUTH);
            cartas.add(painel);
        }
        conteudo.add(cartas, BorderLayout.CENTER);
        JPanel rodape = EstiloImperial.criarPainelFundo(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelar = EstiloImperial.criarBotaoImperial("Decidir depois");
        cancelar.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent evento) { dialogo.dispose(); } });
        rodape.add(cancelar);
        conteudo.add(rodape, BorderLayout.SOUTH);
        dialogo.setContentPane(conteudo);
        dialogo.pack();
        dialogo.setSize(Math.max(430, 220 * opcoes.size()), 455);
        dialogo.setLocationRelativeTo(dono);
        dialogo.setVisible(true);
    }
}
