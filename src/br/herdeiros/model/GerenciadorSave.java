package br.herdeiros.model;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

/** Le e grava partidas completas em saves locais. */
public final class GerenciadorSave {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private GerenciadorSave() {
    }

    public static Path getCaminhoSaveLocal() {
        return getPastaSaves().resolve("partida.txt");
    }

    public static Path getPastaSaves() {
        return Paths.get(System.getProperty("user.dir"), "saves").toAbsolutePath().normalize();
    }

    public static boolean existeSaveLocal() {
        return Files.isRegularFile(getCaminhoSaveLocal());
    }

    public static List<String> listarSaves() throws IOException {
        Path pasta = getPastaSaves();
        if (!Files.isDirectory(pasta)) {
            return new ArrayList<>();
        }

        List<String> nomes = new ArrayList<>();
        try (Stream<Path> arquivos = Files.list(pasta)) {
            java.util.Iterator<Path> iterador = arquivos.iterator();
            while (iterador.hasNext()) {
                Path arquivo = iterador.next();
                if (Files.isRegularFile(arquivo)
                        && arquivo.getFileName().toString().toLowerCase().endsWith(".txt")) {
                    nomes.add(arquivo.getFileName().toString());
                }
            }
        }
        java.util.Collections.sort(nomes, String.CASE_INSENSITIVE_ORDER);
        return nomes;
    }

    public static Path getCaminhoSave(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do save é obrigatório.");
        }

        String nomeArquivo = nome.trim();
        if (!nomeArquivo.toLowerCase().endsWith(".txt")) {
            nomeArquivo += ".txt";
        }
        if (nomeArquivo.indexOf('/') >= 0 || nomeArquivo.indexOf('\\') >= 0
                || nomeArquivo.equals("..") || nomeArquivo.equals(".")) {
            throw new IllegalArgumentException("Nome de save inválido.");
        }
        return getPastaSaves().resolve(nomeArquivo);
    }

    public static void salvarNomeado(String nome, InterfacePartida partida) throws IOException {
        salvar(getCaminhoSave(nome), partida);
    }

    public static InterfacePartida carregarNomeado(String nome) throws IOException {
        return carregar(getCaminhoSave(nome));
    }

    public static void salvarLocal(InterfacePartida partida) throws IOException {
        salvar(getCaminhoSaveLocal(), partida);
    }

    public static InterfacePartida carregarLocal() throws IOException {
        return carregar(getCaminhoSaveLocal());
    }

    public static void salvar(Path destino, InterfacePartida partida) throws IOException {
        if (destino == null || partida == null || !partida.podeSalvarEntreTurnos()) {
            throw new IllegalArgumentException("Partida ou destino de save inválido.");
        }

        Path absoluto = destino.toAbsolutePath();
        Path diretorio = absoluto.getParent();
        if (diretorio == null) {
            throw new IOException("Não foi possível determinar a pasta do save.");
        }
        Files.createDirectories(diretorio);
        Path temporario = Files.createTempFile(diretorio, "partida-", ".tmp");
        try {
            SavePartidaData dados = partida.criarDadosSave();
            Files.write(temporario, converterParaAscii(GSON.toJson(dados)).getBytes(StandardCharsets.US_ASCII));
            try {
                Files.move(temporario, absoluto, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException | java.nio.file.FileAlreadyExistsException erro) {
                Files.move(temporario, absoluto, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    public static InterfacePartida carregar(Path origem) throws IOException {
        if (origem == null || !Files.isRegularFile(origem)) {
            throw new IOException("O arquivo de save não foi encontrado.");
        }

        try {
            byte[] conteudo = Files.readAllBytes(origem);
            String texto = new String(conteudo, StandardCharsets.UTF_8).trim();
            if (texto.startsWith("{")) {
                JsonObject json = JsonParser.parseString(texto).getAsJsonObject();
                if (json.has("payload")) {
                    if (!json.has("formato")
                            || !"herdeiros-save".equals(json.get("formato").getAsString())) {
                        throw new IOException("O save legado não tem um formato reconhecido.");
                    }
                    return carregarObjeto(Base64.getDecoder().decode(
                            json.get("payload").getAsString()));
                }
                SavePartidaData dados = GSON.fromJson(json, SavePartidaData.class);
                InterfacePartida partida = InterfacePartida.restaurarDadosSave(dados);
                if (!partida.estadoValidoParaSave()) {
                    throw new IOException("O estado da partida salva é inválido.");
                }
                return partida;
            }
            return carregarObjeto(conteudo);
        } catch (JsonParseException | IllegalStateException | IllegalArgumentException
                 | NullPointerException erro) {
            throw new IOException("Não foi possível carregar esse arquivo de save.", erro);
        }
    }

    /** JSON legivel em arquivo TXT, escapando os caracteres fora da faixa ASCII. */
    private static String converterParaAscii(String texto) {
        StringBuilder resultado = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char caractere = texto.charAt(i);
            if (caractere > 127) {
                resultado.append(String.format(java.util.Locale.ROOT, "\\u%04x", (int) caractere));
            } else {
                resultado.append(caractere);
            }
        }
        return resultado.toString();
    }

    private static InterfacePartida carregarObjeto(byte[] conteudo) throws IOException {
        try (ObjectInputStream entrada =
                     new ObjectInputStream(new ByteArrayInputStream(conteudo))) {
            Object objeto = entrada.readObject();
            if (!(objeto instanceof InterfacePartida)) {
                throw new IOException("O arquivo não contém uma partida salva válida.");
            }
            InterfacePartida partida = (InterfacePartida) objeto;
            if (!partida.estadoValidoParaSave()) {
                throw new IOException("O estado da partida salva é inválido.");
            }
            return partida;
        } catch (ClassNotFoundException | RuntimeException erro) {
            throw new IOException("Não foi possível carregar esse arquivo de save.", erro);
        }
    }
}
