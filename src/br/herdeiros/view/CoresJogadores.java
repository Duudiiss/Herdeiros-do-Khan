package br.herdeiros.view;

import br.herdeiros.model.Cor;
import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

/** Converte as cores do modelo para suas cores de exibição. */
public final class CoresJogadores {
    private static final Map<Cor, Color> PALETA = new EnumMap<>(Cor.class);

    static {
        PALETA.put(Cor.AZUL, new Color(30, 95, 215));
        PALETA.put(Cor.VERMELHO, new Color(195, 35, 55));
        PALETA.put(Cor.AMARELO, new Color(230, 175, 25));
        PALETA.put(Cor.VERDE, new Color(35, 150, 80));
        PALETA.put(Cor.LARANJA, new Color(230, 110, 25));
        PALETA.put(Cor.PRETO, new Color(45, 45, 50));
    }

    private CoresJogadores() {
    }

    public static Color obter(Cor cor) {
        if (cor == null || cor == Cor.BRANCO) {
            return Color.GRAY;
        }
        Color definida = PALETA.get(cor);
        if (definida != null) {
            return definida;
        }
        float matiz = (float) ((cor.ordinal() * 0.61803398875) % 1);
        return Color.getHSBColor(matiz, 0.72f, 0.88f);
    }
}
