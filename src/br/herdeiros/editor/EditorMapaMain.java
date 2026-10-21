package br.herdeiros.editor;

import javax.swing.SwingUtilities;

public class EditorMapaMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JanelaEditorMapa().setVisible(true);
            }
        });
    }
}
