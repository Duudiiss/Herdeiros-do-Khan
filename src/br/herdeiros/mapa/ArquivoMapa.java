package br.herdeiros.mapa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistência de paradas e conexões no formato CSV P;... e C;.... */
public class ArquivoMapa {
    public static void salvar(MapaDados mapa, Path caminho) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(caminho, StandardCharsets.UTF_8)) {
            for (MapaDados.Ponto ponto : mapa.getPontos().values()) {
                escritor.write("P;" + ponto.getId() + ";" + ponto.getX() + ";"
                        + ponto.getY() + ";" + ponto.isDupla() + ";" + ponto.getRaio());
                escritor.newLine();
            }
            for (MapaDados.Ligacao ligacao : mapa.getLigacoes()) {
                escritor.write("C;" + ligacao.getOrigem() + ";" + ligacao.getDestino());
                escritor.newLine();
            }
        }
    }

    public static MapaDados carregar(Path caminho) throws IOException {
        MapaDados mapa = new MapaDados();
        if (!Files.exists(caminho)) {
            throw new IOException("Arquivo de mapa não encontrado: " + caminho);
        }
        java.util.List<String[]> conexoes = new java.util.ArrayList<>();
        int linhaAtual = 0;
        boolean karakorumLida = false;
        try (BufferedReader leitor = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                linhaAtual++;
                if (linha.isBlank() || linha.startsWith("#")) {
                    continue;
                }
                String[] campos = linha.split(";", -1);
                try {
                    if ((campos.length == 5 || campos.length == 6) && campos[0].equals("P")) {
                        int id = Integer.parseInt(campos[1]);
                        double x = Double.parseDouble(campos[2]);
                        double y = Double.parseDouble(campos[3]);
                        if (!campos[4].equals("true") && !campos[4].equals("false")) {
                            throw new IllegalArgumentException("Tipo de parada inválido");
                        }
                        boolean dupla = Boolean.parseBoolean(campos[4]);
                        if (id == 0) {
                            if (karakorumLida || dupla) {
                                throw new IllegalArgumentException("Karakorum inválida ou duplicada");
                            }
                            mapa.mover(0, x, y);
                            karakorumLida = true;
                        } else if (!mapa.adicionar(id, x, y, dupla)) {
                            throw new IllegalArgumentException("ID duplicado ou inválido");
                        }
                        if (campos.length == 6) {
                            mapa.alterarRaio(id, Double.parseDouble(campos[5]));
                        }
                    } else if (campos.length == 3 && campos[0].equals("C")) {
                        conexoes.add(campos);
                    } else {
                        throw new IllegalArgumentException("Registro desconhecido");
                    }
                } catch (IllegalArgumentException erro) {
                    throw new IOException("Mapa inválido na linha " + linhaAtual + ": " + erro.getMessage(), erro);
                }
            }
        }
        for (String[] campos : conexoes) {
            try {
                int origem = Integer.parseInt(campos[1]);
                int destino = Integer.parseInt(campos[2]);
                if (!mapa.conectar(origem, destino)) {
                    throw new IllegalArgumentException("Conexão inválida ou duplicada");
                }
            } catch (IllegalArgumentException erro) {
                throw new IOException("Conexão inválida: " + String.join(";", campos), erro);
            }
        }
        return mapa;
    }
}
