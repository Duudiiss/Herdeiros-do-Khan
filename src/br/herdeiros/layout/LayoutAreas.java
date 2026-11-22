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

/** Retangulos normalizados compartilhados pelo editor e pelo desenho/hitbox das cartas. */
public final class LayoutAreas {
    public static final class Area {
        public final double x;
        public final double y;
        public final double largura;
        public final double altura;

        public Area(double x, double y, double largura, double altura) {
            if (!Double.isFinite(x) || !Double.isFinite(y)
                    || !Double.isFinite(largura) || !Double.isFinite(altura)
                    || x < 0 || x > 1 || y < 0 || y > 1
                    || largura <= 0 || largura > 1 || altura <= 0 || altura > 1) {
                throw new IllegalArgumentException("Area invalida");
            }
            this.x = x;
            this.y = y;
            this.largura = largura;
            this.altura = altura;
        }

        public boolean contem(double pontoX, double pontoY) {
            return Math.abs(pontoX - x) <= largura / 2
                    && Math.abs(pontoY - y) <= altura / 2;
        }
    }

    private final Path arquivo;
    private final Map<String, Area> areas = new LinkedHashMap<>();

    public LayoutAreas(Path arquivo) throws IOException {
        this.arquivo = arquivo;
        recarregar();
    }

    public static Path caminhoTabuleiro() {
        return Paths.get("assets", "layout", "areas_tabuleiro.csv");
    }

    public static Path caminhoCartela(int indice) {
        if (indice < 1 || indice > 5) {
            throw new IllegalArgumentException("Cartela invalida");
        }
        return Paths.get("assets", "layout", "areas_cartela" + indice + ".csv");
    }

    public synchronized void marcar(String id, Area area) {
        if (id == null || !id.matches("[A-Za-z0-9_.-]+")) {
            throw new IllegalArgumentException("ID de area invalido");
        }
        areas.put(id, area);
    }

    public synchronized Area consultar(String id) {
        return areas.get(id);
    }

    public synchronized Map<String, Area> consultarTodas() {
        return new LinkedHashMap<>(areas);
    }

    public synchronized void recarregar() throws IOException {
        areas.clear();
        if (!Files.exists(arquivo)) {
            return;
        }
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        int linha = 0;
        for (String conteudo : linhas) {
            linha++;
            if (conteudo.isBlank() || conteudo.startsWith("#") || conteudo.startsWith("id,")) {
                continue;
            }
            String[] partes = conteudo.trim().split(",", -1);
            if (partes.length != 5) {
                throw new IOException("Area invalida na linha " + linha + ": " + arquivo);
            }
            try {
                marcar(partes[0], new Area(Double.parseDouble(partes[1]),
                        Double.parseDouble(partes[2]), Double.parseDouble(partes[3]),
                        Double.parseDouble(partes[4])));
            } catch (IllegalArgumentException erro) {
                throw new IOException("Area invalida na linha " + linha + ": " + erro.getMessage(), erro);
            }
        }
    }

    public synchronized void salvar() throws IOException {
        Files.createDirectories(arquivo.toAbsolutePath().getParent());
        StringBuilder texto = new StringBuilder("id,x,y,largura,altura\n");
        for (Map.Entry<String, Area> entrada : areas.entrySet()) {
            Area area = entrada.getValue();
            texto.append(String.format(Locale.ROOT, "%s,%.7f,%.7f,%.7f,%.7f%n",
                    entrada.getKey(), area.x, area.y, area.largura, area.altura));
        }
        Files.writeString(arquivo, texto.toString(), StandardCharsets.UTF_8);
    }
}
