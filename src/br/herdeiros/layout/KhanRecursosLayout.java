package br.herdeiros.layout;

import br.herdeiros.model.DestinoKhan;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Editor persiste que icones de recurso sao repostos por cada destino do Khan. */
public final class KhanRecursosLayout {
    private final Map<DestinoKhan, Set<Integer>> associados =
            new EnumMap<DestinoKhan, Set<Integer>>(DestinoKhan.class);

    public static Path caminho() {
        return Paths.get("assets", "layout", "recursos_khan.csv");
    }

    public static KhanRecursosLayout carregar() throws IOException {
        return carregar(caminho());
    }

    public static KhanRecursosLayout carregar(Path arquivo) throws IOException {
        if (arquivo == null) {
            throw new IllegalArgumentException("Arquivo obrigatorio");
        }
        KhanRecursosLayout layout = new KhanRecursosLayout();
        if (!Files.exists(arquivo)) {
            return layout;
        }
        for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
            String texto = linha.trim();
            if (texto.isEmpty() || texto.startsWith("#")) {
                continue;
            }
            String[] campos = texto.split(";", -1);
            if (campos.length != 2) {
                throw new IOException("Mapeamento do Khan invalido: " + texto);
            }
            try {
                DestinoKhan destino = DestinoKhan.valueOf(campos[0].trim());
                Set<Integer> indices = new LinkedHashSet<Integer>();
                if (!campos[1].trim().isEmpty()) {
                    for (String indice : campos[1].split(",")) {
                        int valor = Integer.parseInt(indice.trim());
                        if (valor < 0 || !indices.add(valor)) {
                            throw new IOException("Indice de recurso do Khan invalido: " + texto);
                        }
                    }
                }
                layout.definir(destino, indices);
            } catch (IllegalArgumentException erro) {
                throw new IOException("Destino ou indice do Khan invalido: " + texto, erro);
            }
        }
        return layout;
    }

    public boolean configurado(DestinoKhan destino) {
        return associados.containsKey(destino);
    }

    public Set<Integer> consultar(DestinoKhan destino) {
        if (!configurado(destino)) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(associados.get(destino));
    }

    public void definir(DestinoKhan destino, Set<Integer> indices) {
        if (destino == null || indices == null || indices.contains(null)) {
            throw new IllegalArgumentException("Mapeamento do Khan invalido");
        }
        for (Integer indice : indices) {
            if (indice < 0) {
                throw new IllegalArgumentException("Indice negativo");
            }
        }
        associados.put(destino, new LinkedHashSet<Integer>(indices));
    }

    public void salvar() throws IOException {
        salvar(caminho());
    }

    public void salvar(Path arquivo) throws IOException {
        if (arquivo == null) {
            throw new IllegalArgumentException("Arquivo obrigatorio");
        }
        List<String> linhas = new ArrayList<String>();
        linhas.add("# destino;indices dos icones na ordem de icones_tabuleiro.csv");
        for (DestinoKhan destino : DestinoKhan.values()) {
            if (!configurado(destino)) {
                continue;
            }
            StringBuilder linha = new StringBuilder(destino.name()).append(';');
            boolean primeiro = true;
            for (Integer indice : associados.get(destino)) {
                if (!primeiro) {
                    linha.append(',');
                }
                linha.append(indice);
                primeiro = false;
            }
            linhas.add(linha.toString());
        }
        if (arquivo.getParent() != null) {
            Files.createDirectories(arquivo.getParent());
        }
        Files.write(arquivo, linhas, StandardCharsets.UTF_8);
    }
}
