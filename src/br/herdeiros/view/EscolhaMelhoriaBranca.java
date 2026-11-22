package br.herdeiros.view;

import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.ResumoMelhoria;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Escolha da carta da coluna branca diretamente pela imagem, sem codigos internos. */
public final class EscolhaMelhoriaBranca {

    private EscolhaMelhoriaBranca() {
    }

    public static void mostrar(Component origem, InterfacePartida partida) {
        while (partida.consultarMelhoriasBrancasPendentes() > 0) {
            Integer escolha = exibirEscolha(origem, partida.consultarMercadoMelhorias());
            if (escolha == null) {
                return;
            }
            boolean sucesso = escolha.intValue() == -1
                    ? partida.escolherMelhoriaBrancaAleatoria()
                    : partida.escolherMelhoriaBrancaDoMercado(escolha.intValue());
            if (!sucesso) {
                JOptionPane.showMessageDialog(origem,
                        "Nao foi possivel obter a melhoria. Confira a disponibilidade.",
                        "Melhoria branca", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
    }

    private static Integer exibirEscolha(Component origem, List<ResumoMelhoria> mercado) {
        Window dona = origem == null ? null : SwingUtilities.getWindowAncestor(origem);
        JDialog janela = new JDialog(dona, "Escolher melhoria da coluna branca",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        janela.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel conteudo = EstiloImperial.criarPainelFundo(new BorderLayout(12, 12));
        conteudo.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel instrucao = new JLabel(
                "Linha completa! Clique na imagem para escolher a melhoria gratuita.");
        instrucao.setFont(EstiloImperial.FONTE_TEXTO_NEGRITO);
        instrucao.setForeground(EstiloImperial.COR_TEXTO_CLARO);
        conteudo.add(instrucao, BorderLayout.NORTH);

        JPanel cartas = EstiloImperial.criarPainelFundo(new GridLayout(1, 0, 10, 8));
        final Integer[] selecionada = new Integer[1];
        for (int i = 0; i < mercado.size(); i++) {
            ResumoMelhoria melhoria = mercado.get(i);
            if (melhoria == null) {
                continue;
            }
            final int posicao = i;
            JPanel carta = EstiloImperial.criarPainelFundo(new BorderLayout(4, 5));
            carta.setBorder(BorderFactory.createLineBorder(
                    EstiloImperial.COR_DOURADA_IMPERIAL, 1));
            PainelImagemJava2D botaoImagem = new PainelImagemJava2D();
            botaoImagem.setPreferredSize(new Dimension(155, 110));
            botaoImagem.setBackground(EstiloImperial.COR_FUNDO_CAMPO);
            botaoImagem.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            botaoImagem.setToolTipText(melhoria.getDescricao());
            try {
                BufferedImage imagem = ImageIO.read(Paths.get("assets", "melhorias",
                        melhoria.getCodigo() + ".jpg").toFile());
                if (imagem != null) {
                    botaoImagem.setImagem(imagem);
                }
            } catch (IOException erro) {
                botaoImagem.setText("Ver beneficio");
            }
            botaoImagem.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent evento) { selecionada[0] = Integer.valueOf(posicao);
                janela.dispose(); } });
            JLabel beneficio = new JLabel(
                    "<html><div style='text-align:center;width:135px'>"
                    + escaparHtml(melhoria.getDescricao()) + "</div></html>",
                    SwingConstants.CENTER);
            beneficio.setForeground(EstiloImperial.COR_TEXTO_CLARO);
            beneficio.setFont(EstiloImperial.FONTE_TEXTO);
            carta.add(botaoImagem, BorderLayout.CENTER);
            carta.add(beneficio, BorderLayout.SOUTH);
            cartas.add(carta);
        }
        conteudo.add(cartas, BorderLayout.CENTER);
        JPanel rodape = EstiloImperial.criarPainelFundo(new FlowLayout(FlowLayout.CENTER));
        JButton aleatoria = EstiloImperial.criarBotaoImperial("Escolher melhoria aleatoria");
        aleatoria.addActionListener(new java.awt.event.ActionListener() { @Override public void actionPerformed(java.awt.event.ActionEvent evento) { selecionada[0] = Integer.valueOf(-1);
            janela.dispose(); } });
        rodape.add(aleatoria);
        conteudo.add(rodape, BorderLayout.SOUTH);
        janela.setContentPane(conteudo);
        janela.setMinimumSize(new Dimension(620, 280));
        janela.pack();
        janela.setLocationRelativeTo(origem);
        janela.setVisible(true);
        return selecionada[0];
    }

    private static String escaparHtml(String texto) {
        if (texto == null) {
            return "Melhoria";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
