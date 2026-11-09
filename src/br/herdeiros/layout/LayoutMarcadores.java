package br.herdeiros.layout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Mapeia marcadores sobre a arte por coordenadas normalizadas de 0 a 1.
 * Arquivos separados evitam misturar o tabuleiro com as cinco cartelas.
 */
public final class LayoutMarcadores {
    public static final class Posicao {
        private final double x;
        private final double y;

        public Posicao(double x, double y) {
            if (!Double.isFinite(x) || !Double.isFinite(y)
                    || x < 0 || x > 1 || y < 0 || y > 1) {
                throw new IllegalArgumentException("Coordenadas devem estar entre zero e um");
            }
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    private final Path arquivo;
    private final LinkedHashMap<String, Posicao> posicoes = new LinkedHashMap<String, Posicao>();
    private long ultimaAlteracao = -2;
    private long ultimoTamanho = -2;

    public LayoutMarcadores(Path arquivo) throws IOException {
        if (arquivo == null) {
            throw new IllegalArgumentException("Arquivo obrigatorio");
        }
        this.arquivo = arquivo;
        recarregar();
    }

    public static Path caminhoTabuleiro() {
        return java.nio.file.Paths.get("assets", "layout", "tabuleiro.csv");
    }

    public static Path caminhoCartela(int indice) {
        if (indice < 1 || indice > 5) {
            throw new IllegalArgumentException("Cartela deve estar entre 1 e 5");
        }
        return java.nio.file.Paths.get("assets", "layout", "cartela" + indice + ".csv");
    }

    public static List<String> chavesTabuleiro() {
        ArrayList<String> chaves = new ArrayList<String>();
        for (int i = 0; i <= 16; i++) {
            chaves.add("pontos." + i);
        }
        for (int i = 0; i < 5; i++) {
            chaves.add("mercado." + i);
        }
        return Collections.unmodifiableList(chaves);
    }

    public static List<String> chavesCartela() {
        ArrayList<String> chaves = new ArrayList<String>();
        for (int coluna = 0; coluna < 4; coluna++) {
            for (int linha = 0; linha < 4; linha++) {
                chaves.add("melhoria." + coluna + "." + linha);
            }
        }
        for (int coluna = 0; coluna < 4; coluna++) {
            chaves.add("ativacao." + coluna);
        }
        return Collections.unmodifiableList(chaves);
    }

    public synchronized Posicao consultar(String chave) {
        if (chave == null) {
            return null;
        }
        try {
            recarregarSeAlterado();
        } catch (IOException erro) {
            // Conserva as coordenadas validas anteriores ate salvar um novo arquivo.
        }
        return posicoes.get(chave);
    }

    public synchronized Map<String, Posicao> consultarTodas() {
        try {
            recarregarSeAlterado();
        } catch (IOException erro) {
            // Conserva o ultimo conjunto carregado.
        }
        return Collections.unmodifiableMap(new LinkedHashMap<String, Posicao>(posicoes));
    }

    public synchronized void marcar(String chave, double x, double y) {
        if (chave == null || chave.trim().isEmpty() || chave.contains(",")
                || chave.contains("\n")) {
            throw new IllegalArgumentException("Identificador invalido");
        }
        posicoes.put(chave.trim(), new Posicao(x, y));
    }

    public synchronized void remover(String chave) {
        posicoes.remove(chave);
    }

    public synchronized void recarregar() throws IOException {
        LinkedHashMap<String, Posicao> novas = new LinkedHashMap<String, Posicao>();
        if (Files.isRegularFile(arquivo)) {
            BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8);
            try {
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    String limpa = linha.trim();
                    if (limpa.isEmpty() || limpa.startsWith("#") || limpa.startsWith("id,")) {
                        continue;
                    }
                    String[] partes = limpa.split(",", -1);
                    if (partes.length != 3) {
                        continue;
                    }
                    try {
                        novas.put(partes[0].trim(), new Posicao(
                                Double.parseDouble(partes[1].trim()),
                                Double.parseDouble(partes[2].trim())));
                    } catch (IllegalArgumentException erro) {
                        // Uma linha invalida nao impede as demais de carregar.
                    }
                }
            } finally {
                leitor.close();
            }
        }
        posicoes.clear();
        posicoes.putAll(novas);
        atualizarMarcaArquivo();
    }

    private void recarregarSeAlterado() throws IOException {
        long alteracao = Files.isRegularFile(arquivo)
                ? Files.getLastModifiedTime(arquivo).toMillis() : -1;
        long tamanho = Files.isRegularFile(arquivo) ? Files.size(arquivo) : -1;
        if (alteracao != ultimaAlteracao || tamanho != ultimoTamanho) {
            recarregar();
        }
    }

    public synchronized void salvar() throws IOException {
        Path pasta = arquivo.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        Path temporario = arquivo.resolveSibling(arquivo.getFileName().toString() + ".tmp");
        BufferedWriter escritor = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8);
        try {
            escritor.write("id,x,y");
            escritor.newLine();
            for (Map.Entry<String, Posicao> entrada : posicoes.entrySet()) {
                escritor.write(String.format(Locale.ROOT, "%s,%.7f,%.7f",
                        entrada.getKey(), entrada.getValue().getX(), entrada.getValue().getY()));
                escritor.newLine();
            }
        } finally {
            escritor.close();
        }
        try {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException erro) {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        }
        atualizarMarcaArquivo();
    }

    private void atualizarMarcaArquivo() throws IOException {
        if (Files.isRegularFile(arquivo)) {
            ultimaAlteracao = Files.getLastModifiedTime(arquivo).toMillis();
            ultimoTamanho = Files.size(arquivo);
        } else {
            ultimaAlteracao = -1;
            ultimoTamanho = -1;
        }
    }
}
