package br.herdeiros.mapa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dados do desenho do mapa; não contém regras de movimento do jogo. */
public class MapaDados {
    public static class Ponto {
        private final int id;
        private double x;
        private double y;
        private final boolean dupla;
        private double raio;
        private double raioVertical;

        public Ponto(int id, double x, double y, boolean dupla) {
            if (id < 0 || !Double.isFinite(x) || !Double.isFinite(y)
                    || x < 0 || x > 1 || y < 0 || y > 1) {
                throw new IllegalArgumentException("Parada com coordenadas inválidas");
            }
            this.id = id;
            this.x = x;
            this.y = y;
            this.dupla = dupla;
            this.raio = raioPadrao(id, dupla);
            this.raioVertical = this.raio;
        }

        public int getId() {
            return id;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public boolean isDupla() {
            return dupla;
        }

        public double getRaio() {
            return raio;
        }

        public double getRaioVertical() {
            return raioVertical;
        }

        private void alterarDimensoes(double raioHorizontal, double raioY) {
            validarRaio(raioHorizontal);
            validarRaio(raioY);
            raio = raioHorizontal;
            raioVertical = raioY;
        }

        private void validarRaio(double novoRaio) {
            if (!Double.isFinite(novoRaio) || novoRaio < 0.008 || novoRaio > 0.15) {
                throw new IllegalArgumentException("Raio fora do intervalo permitido");
            }
        }

        private void atualizar(double novoX, double novoY) {
            if (!Double.isFinite(novoX) || !Double.isFinite(novoY)
                    || novoX < 0 || novoX > 1 || novoY < 0 || novoY > 1) {
                throw new IllegalArgumentException("Coordenadas fora da imagem");
            }
            x = novoX;
            y = novoY;
        }
    }

    public static class Ligacao {
        private final int origem;
        private final int destino;

        public Ligacao(int primeiro, int segundo) {
            if (primeiro == segundo) {
                throw new IllegalArgumentException("Uma parada não pode ligar-se a si mesma");
            }
            origem = Math.min(primeiro, segundo);
            destino = Math.max(primeiro, segundo);
        }

        public int getOrigem() {
            return origem;
        }

        public int getDestino() {
            return destino;
        }
    }

    /** Raio normalizado pela menor dimensão do tabuleiro desenhado. */
    public static double raioPadrao(int id, boolean dupla) {
        if (id == 0) {
            return 0.055;
        }
        if (dupla) {
            return 0.042;
        }
        return 0.027;
    }

    /** Encontra a parada mais próxima dentro da área clicável, na escala atual. */
    public int encontrarParada(double x, double y, int largura, int altura) {
        if (largura <= 0 || altura <= 0) {
            return -1;
        }
        int encontrada = -1;
        double menorDistancia = Double.POSITIVE_INFINITY;
        double dimensao = Math.min(largura, altura);
        for (Ponto ponto : pontos.values()) {
            double dx = (ponto.getX() - x) * largura;
            double dy = (ponto.getY() - y) * altura;
            double raioX = ponto.getRaio() * dimensao;
            double raioY = ponto.getRaioVertical() * dimensao;
            double distanciaNormalizada = (dx * dx) / (raioX * raioX)
                    + (dy * dy) / (raioY * raioY);
            if (distanciaNormalizada <= 1.0
                    && distanciaNormalizada < menorDistancia) {
                menorDistancia = distanciaNormalizada;
                encontrada = ponto.getId();
            }
        }
        return encontrada;
    }

    private final LinkedHashMap<Integer, Ponto> pontos = new LinkedHashMap<>();
    private final List<Ligacao> ligacoes = new ArrayList<>();

    public MapaDados() {
        pontos.put(0, new Ponto(0, 0.50, 0.43, false));
    }

    public Map<Integer, Ponto> getPontos() {
        return Collections.unmodifiableMap(pontos);
    }

    public List<Ligacao> getLigacoes() {
        return Collections.unmodifiableList(ligacoes);
    }

    public Ponto getPonto(int id) {
        return pontos.get(id);
    }

    public int proximoId() {
        int id = 1;
        while (pontos.containsKey(id)) {
            id++;
        }
        return id;
    }

    public boolean adicionar(int id, double x, double y, boolean dupla) {
        if (pontos.containsKey(id) || id <= 0) {
            return false;
        }
        pontos.put(id, new Ponto(id, x, y, dupla));
        return true;
    }

    public boolean alterarRaio(int id, double raio) {
        Ponto ponto = pontos.get(id);
        if (ponto == null) {
            return false;
        }
        ponto.alterarDimensoes(raio, raio);
        return true;
    }

    /** Os dois semieixos sao relativos a menor dimensao da imagem. */
    public boolean alterarElipse(int id, double horizontal, double vertical) {
        Ponto ponto = pontos.get(id);
        if (ponto == null) {
            return false;
        }
        ponto.alterarDimensoes(horizontal, vertical);
        return true;
    }

    public boolean mover(int id, double x, double y) {
        Ponto ponto = pontos.get(id);
        if (ponto == null) {
            return false;
        }
        ponto.atualizar(x, y);
        return true;
    }

    public boolean remover(int id) {
        if (id == 0 || pontos.remove(id) == null) {
            return false;
        }
        for (int i = ligacoes.size() - 1; i >= 0; i--) {
            Ligacao ligacao = ligacoes.get(i);
            if (ligacao.getOrigem() == id || ligacao.getDestino() == id) {
                ligacoes.remove(i);
            }
        }
        return true;
    }

    public boolean conectar(int origem, int destino) {
        if (origem == destino || !pontos.containsKey(origem) || !pontos.containsKey(destino)) {
            return false;
        }
        Ligacao nova = new Ligacao(origem, destino);
        for (Ligacao existente : ligacoes) {
            if (existente.getOrigem() == nova.getOrigem()
                    && existente.getDestino() == nova.getDestino()) {
                return false;
            }
        }
        ligacoes.add(nova);
        return true;
    }

    public boolean desconectar(int origem, int destino) {
        if (origem == destino) {
            return false;
        }
        Ligacao alvo = new Ligacao(origem, destino);
        for (int i = 0; i < ligacoes.size(); i++) {
            Ligacao ligacao = ligacoes.get(i);
            if (ligacao.getOrigem() == alvo.getOrigem()
                    && ligacao.getDestino() == alvo.getDestino()) {
                ligacoes.remove(i);
                return true;
            }
        }
        return false;
    }
}
