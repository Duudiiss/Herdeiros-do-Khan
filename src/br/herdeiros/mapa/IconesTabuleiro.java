package br.herdeiros.mapa;

import br.herdeiros.model.Provincia;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Localizacoes visuais dos icones impressos no tabuleiro, independentes do Model. */
public final class IconesTabuleiro {

    public static final class IconeTributo {
        private final Provincia[] provincias;
        private final String tipo;
        private final double x;
        private final double y;
        private final double raio;

        private IconeTributo(Provincia[] provincias, String tipo, double x, double y, double raio) {
            this.provincias = provincias.clone();
            this.tipo = tipo;
            this.x = x;
            this.y = y;
            this.raio = raio;
        }

        public Provincia[] getProvincias() {
            return provincias.clone();
        }

        public String getTipo() {
            return tipo;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    public static final class IconeCidade {
        private final String nome;
        private final double x;
        private final double y;
        private final double raio;

        private IconeCidade(String nome, double x, double y, double raio) {
            this.nome = nome;
            this.x = x;
            this.y = y;
            this.raio = raio;
        }

        public String getNome() {
            return nome;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    private final List<IconeTributo> tributos = new ArrayList<IconeTributo>();
    private final List<IconeCidade> cidades = new ArrayList<IconeCidade>();

    public static IconesTabuleiro carregar(Path arquivo) throws IOException {
        IconesTabuleiro mapa = new IconesTabuleiro();
        for (String linha : Files.readAllLines(arquivo)) {
            String texto = linha.trim();
            if (texto.isEmpty() || texto.startsWith("#")) {
                continue;
            }
            String[] campos = texto.split(";");
            if (campos.length != 6) {
                throw new IOException("Linha de icone invalida: " + texto);
            }
            try {
                double x = Double.parseDouble(campos[3]);
                double y = Double.parseDouble(campos[4]);
                double raio = Double.parseDouble(campos[5]);
                if (x < 0 || x > 1 || y < 0 || y > 1 || raio <= 0 || raio > 0.10) {
                    throw new IOException("Coordenada ou raio fora do tabuleiro: " + texto);
                }
                if (campos[0].equals("T")) {
                    String[] nomes = campos[1].split(",");
                    Provincia[] provincias = new Provincia[nomes.length];
                    for (int i = 0; i < nomes.length; i++) {
                        provincias[i] = Provincia.valueOf(nomes[i].trim());
                    }
                    String tipo = campos[2].trim();
                    if (!tipo.equals("ESPADA") && !tipo.equals("MOEDA") && !tipo.equals("YURT")) {
                        throw new IOException("Tipo de tributo invalido: " + texto);
                    }
                    mapa.tributos.add(new IconeTributo(provincias, tipo, x, y, raio));
                } else if (campos[0].equals("C")) {
                    mapa.cidades.add(new IconeCidade(campos[1].trim(), x, y, raio));
                } else {
                    throw new IOException("Tipo de icone invalido: " + texto);
                }
            } catch (IllegalArgumentException erro) {
                throw new IOException("Dados invalidos em icone: " + texto, erro);
            }
        }
        return mapa;
    }

    public List<IconeTributo> getTributos() {
        return Collections.unmodifiableList(tributos);
    }

    public List<IconeCidade> getCidades() {
        return Collections.unmodifiableList(cidades);
    }

    /** Retorna sempre o icone mais proximo, inclusive sobre divisas de provincias. */
    public IconeTributo encontrarTributo(double x, double y) {
        IconeTributo melhor = null;
        double distanciaMinima = Double.MAX_VALUE;
        for (IconeTributo icone : tributos) {
            double dx = x - icone.x;
            double dy = y - icone.y;
            double distancia = dx * dx + dy * dy;
            if (distancia <= icone.raio * icone.raio && distancia < distanciaMinima) {
                melhor = icone;
                distanciaMinima = distancia;
            }
        }
        return melhor;
    }

    public IconeCidade encontrarCidade(double x, double y) {
        IconeCidade melhor = null;
        double distanciaMinima = Double.MAX_VALUE;
        for (IconeCidade icone : cidades) {
            double dx = x - icone.x;
            double dy = y - icone.y;
            double distancia = dx * dx + dy * dy;
            if (distancia <= icone.raio * icone.raio && distancia < distanciaMinima) {
                melhor = icone;
                distanciaMinima = distancia;
            }
        }
        return melhor;
    }
}
