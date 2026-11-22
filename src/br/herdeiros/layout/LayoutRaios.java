package br.herdeiros.layout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Raios normalizados dos circulos, editaveis independentemente dos centros. */
public final class LayoutRaios {
    private final Path arquivo;
    private final Map<String, Double> raios = new LinkedHashMap<>();

    public LayoutRaios(Path arquivo) throws IOException {
        this.arquivo = arquivo;
        recarregar();
    }

    public static Path caminhoTabuleiro() {
        return Paths.get("assets", "layout", "raios_tabuleiro.csv");
    }

    public synchronized void definir(String id, double raio) {
        if (id == null || !id.matches("[A-Za-z0-9_.-]+")
                || !Double.isFinite(raio) || raio <= 0 || raio > 0.35) {
            throw new IllegalArgumentException("Identificador ou raio invalido");
        }
        raios.put(id, raio);
    }

    public synchronized double consultar(String id, double padrao) {
        Double raio = raios.get(id);
        return raio == null ? padrao : raio;
    }

    public synchronized void recarregar() throws IOException {
        raios.clear();
        if (!Files.exists(arquivo)) {
            return;
        }
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        for (String linha : linhas) {
            if (linha.isBlank() || linha.startsWith("#") || linha.startsWith("id,")) {
                continue;
            }
            String[] colunas = linha.trim().split(",", -1);
            if (colunas.length != 2) {
                throw new IOException("Linha invalida no arquivo de raios: " + linha);
            }
            try {
                definir(colunas[0], Double.parseDouble(colunas[1]));
            } catch (IllegalArgumentException erro) {
                throw new IOException("Raio invalido: " + linha, erro);
            }
        }
    }

    public synchronized void salvar() throws IOException {
        Files.createDirectories(arquivo.toAbsolutePath().getParent());
        StringBuilder conteudo = new StringBuilder("id,raio\n");
        for (Map.Entry<String, Double> entrada : raios.entrySet()) {
            conteudo.append(String.format(Locale.ROOT, "%s,%.7f%n",
                    entrada.getKey(), entrada.getValue()));
        }
        Files.writeString(arquivo, conteudo.toString(), StandardCharsets.UTF_8);
    }
}
