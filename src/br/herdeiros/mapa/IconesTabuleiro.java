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
        private final java.util.Set<Integer> paradasPermitidas = new java.util.LinkedHashSet<Integer>();
        private double x;
        private double y;
        private double raio;

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

        /** Restricao especifica do icone; vazia preserva o acesso tradicional por provincia. */
        public java.util.Set<Integer> getParadasPermitidas() {
            return java.util.Collections.unmodifiableSet(paradasPermitidas);
        }

        public void definirParadasPermitidas(java.util.Collection<Integer> paradas) {
            paradasPermitidas.clear();
            if (paradas != null) {
                for (Integer parada : paradas) {
                    if (parada == null || parada.intValue() < 0) {
                        throw new IllegalArgumentException("Parada invalida");
                    }
                    paradasPermitidas.add(parada);
                }
            }
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getRaio() {
            return raio;
        }

        public void posicionar(double novoX, double novoY, double novoRaio) {
            validar(novoX, novoY, novoRaio);
            x = novoX;
            y = novoY;
            raio = novoRaio;
        }
    }

    public static final class IconeCidade {
        private final String nome;
        private double x;
        private double y;
        private double raio;

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

        public double getRaio() {
            return raio;
        }

        public void posicionar(double novoX, double novoY, double novoRaio) {
            validar(novoX, novoY, novoRaio);
            x = novoX;
            y = novoY;
            raio = novoRaio;
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
            if (campos.length != 6 && !(campos[0].equals("T") && campos.length == 7)) {
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
                    IconeTributo tributo = new IconeTributo(provincias, tipo, x, y, raio);
                    if (campos.length == 7 && !campos[6].trim().isEmpty()) {
                        java.util.Set<Integer> paradas = new java.util.LinkedHashSet<Integer>();
                        for (String id : campos[6].split(",")) {
                            paradas.add(Integer.valueOf(id.trim()));
                        }
                        tributo.definirParadasPermitidas(paradas);
                    }
                    mapa.tributos.add(tributo);
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

    private static void validar(double x, double y, double raio) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(raio)
                || x < 0 || x > 1 || y < 0 || y > 1 || raio <= 0 || raio > 0.10) {
            throw new IllegalArgumentException("Coordenada ou raio de icone invalido");
        }
    }

    public void salvar(Path arquivo) throws IOException {
        StringBuilder conteudo = new StringBuilder(
                "# T;PROVINCIAS;TIPO;x;y;raio;PARADAS_PERMITIDAS(opcional) | C;NOME;TIPO;x;y;raio\n");
        for (IconeTributo icone : tributos) {
            StringBuilder nomes = new StringBuilder();
            for (Provincia provincia : icone.provincias) {
                if (nomes.length() > 0) {
                    nomes.append(',');
                }
                nomes.append(provincia.name());
            }
            StringBuilder paradas = new StringBuilder();
            for (Integer parada : icone.paradasPermitidas) {
                if (paradas.length() > 0) paradas.append(',');
                paradas.append(parada);
            }
            conteudo.append(String.format(java.util.Locale.ROOT, "T;%s;%s;%.7f;%.7f;%.7f;%s%n",
                    nomes, icone.tipo, icone.x, icone.y, icone.raio, paradas));
        }
        for (IconeCidade icone : cidades) {
            conteudo.append(String.format(java.util.Locale.ROOT, "C;%s;CIDADE;%.7f;%.7f;%.7f%n",
                    icone.nome, icone.x, icone.y, icone.raio));
        }
        Files.writeString(arquivo, conteudo.toString(), java.nio.charset.StandardCharsets.UTF_8);
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
