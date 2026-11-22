package br.herdeiros.editor;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Execute esta classe diretamente no Eclipse: nao adiciona botoes no jogo. */
public final class EditorTabuleiroUnificadoMain {
    private EditorTabuleiroUnificadoMain() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new JanelaEditorTabuleiroUnificado().setVisible(true);
            } catch (Exception erro) {
                JOptionPane.showMessageDialog(null, erro.getMessage(), "Erro no editor",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
