package br.herdeiros.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Estrutura JSON versionada usada para persistir uma partida sem serializar objetos Java. */
public final class SavePartidaData {
    public Meta meta = new Meta();
    public EstadoPartida partida = new EstadoPartida();
    public Mapa mapa = new Mapa();
    public List<Jogador> jogadores = new ArrayList<>();
    public Tabuleiro tabuleiro = new Tabuleiro();
    public Baralhos baralhos = new Baralhos();
    public List<Provincia> provincias = new ArrayList<>();

    public static final class Meta {
        public String formato = "herdeiros-save";
        public int versao = 1;
        public String tipo = "partida";
        public String descricao = "Save automático";
    }

    public static final class EstadoPartida {
        public int rodadasConcluidas;
        public int jogadorAtual;
        public int pontuacaoKhan;
        public int rodadasNoDisparoFim = -1;
        public boolean encerrada;
    }

    public static final class Mapa {
        public List<Ponto> paradas = new ArrayList<>();
        public List<Ligacao> conexoes = new ArrayList<>();
        public Map<String, List<Integer>> acessosCidades = new LinkedHashMap<>();
        public Map<String, List<String>> vizinhasKhan = new LinkedHashMap<>();
    }

    public static final class Ponto {
        public int id;
        public double x;
        public double y;
        public boolean dupla;
        public Double raio;
    }

    public static final class Ligacao {
        public int origem;
        public int destino;
    }

    public static final class Jogador {
        public String nome;
        public String cor;
        public String personagem;
        public int votos;
        public int descendentes;
        public int parada;
        public Map<String, Integer> tributos = new LinkedHashMap<>();
        public List<Melhoria> melhorias = new ArrayList<>();
        public boolean[] colunasOcupadas;
    }

    public static final class Melhoria {
        public String codigo;
        public int coluna;
    }

    public static final class Tabuleiro {
        public int paradaKhan = -1;
        public String destinoKhan;
        public List<Yurt> yurts = new ArrayList<>();
        public List<Cidade> cidadesDisponiveis = new ArrayList<>();
        public Map<String, String> cidadesConquistadas = new LinkedHashMap<>();
    }

    public static final class Yurt {
        public int parada;
        public String cor;
    }

    public static final class Cidade {
        public String nome;
        public String regiao;
        public int tesouros;
        public boolean conquistada;
    }

    public static final class Baralhos {
        public List<Cidade> cidadesRestantes = new ArrayList<>();
        public List<Melhoria> melhoriasDisponiveis = new ArrayList<>();
        public List<Melhoria> melhoriasRestantes = new ArrayList<>();
    }

    public static final class Provincia {
        public String nome;
        public String tipoPrincipal;
        public Map<String, Integer> quantidades = new LinkedHashMap<>();
        public List<Integer> acessos = new ArrayList<>();
    }
}
