package br.herdeiros.editor;

import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Permite iniciar o editor separadamente, sem carregar uma partida. */
public final class EditorMarcadoresMain {
    private EditorMarcadoresMain() {
    }

    public static void main(String[] argumentos) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new JanelaEditorMarcadores(null).setVisible(true);
                } catch (IOException erro) {
                    JOptionPane.showMessageDialog(null, erro.getMessage());
                }
            }
        });
    }
}
