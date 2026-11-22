package br.herdeiros.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayDeque;
import java.util.Queue;

/** Armazena as posições, conexões e peças e verifica movimentação e ocupação. */
class Tabuleiro {
    private final Peao peaoDoKhan;
    private final List<Cidade> cidadesDisponiveis;
    private final HashMap<Integer, Parada> paradas = new HashMap<Integer, Parada>();
    private final Set<Peao> peoes = Collections.newSetFromMap(new IdentityHashMap<Peao, Boolean>());
    private final HashMap<Integer, ArrayList<Cor>> yurts = new HashMap<Integer, ArrayList<Cor>>();
    private final HashMap<NomeCidade, Cor> yurtsConquistaCidade = new HashMap<NomeCidade, Cor>();
    private Cor yurtToluiKarakorum;
    private DestinoKhan destinoKhan;

    public Tabuleiro(List<Cidade> cidadesIniciaisSorteadas) {
        if (cidadesIniciaisSorteadas == null) {
            throw new IllegalArgumentException("Lista de cidades obrigatória");
        }

        this.peaoDoKhan = new Peao(Cor.BRANCO);
        this.cidadesDisponiveis = new ArrayList<Cidade>(cidadesIniciaisSorteadas);
        this.paradas.put(0, new Parada(0, TipoParada.KARAKORUM));
    }

    public boolean adicionarParada(int id, boolean dupla) {
        if (id <= 0 || paradas.containsKey(id)) {
            return false;
        }

        paradas.put(id, new Parada(id, dupla));
        return true;
    }

    public boolean conectarParadas(int id1, int id2) {
        Parada primeira = paradas.get(id1);
        Parada segunda = paradas.get(id2);

        if (primeira == null || segunda == null || primeira == segunda) {
            return false;
        }

        primeira.adicionarAdjacente(segunda);
        segunda.adicionarAdjacente(primeira);
        return true;
    }

    public boolean saoAdjacentes(int id1, int id2) {
        Parada primeira = paradas.get(id1);
        Parada segunda = paradas.get(id2);

        if (primeira == null || segunda == null) {
            return false;
        }

        return primeira.ehAdjacente(segunda);
    }

    public void substituirCidadeConquistada(Cidade cidadeAntiga, Cidade novaCidade) {
        if (cidadeAntiga != null) {
            boolean removida = cidadesDisponiveis.remove(cidadeAntiga);
            if (removida && novaCidade != null) {
                cidadesDisponiveis.add(novaCidade);
            }
        }
    }

    public Peao getPeaoDoKhan() {
        return peaoDoKhan;
    }

    public List<Cidade> getCidadesDisponiveis() {
        return new ArrayList<Cidade>(cidadesDisponiveis);
    }

    public int getQuantidadeParadas() {
        return paradas.size();
    }

    /** Registra o peão de um jogador em Karakorum somente uma vez. */
    public boolean posicionarPeaoInicial(Peao peao) {
        if (peao == null || peao == peaoDoKhan || peao.getCor() == Cor.BRANCO || peoes.contains(peao)) {
            return false;
        }

        peoes.add(peao);
        peao.posicionarEm(paradas.get(0));
        return true;
    }

    private int ocupacao(Parada parada, Peao ignorar) {
        int quantidade = 0;

        for (Peao peao : peoes) {
            if (peao != ignorar && peao.getParadaAtual() == parada) {
                quantidade++;
            }
        }

        if (peaoDoKhan != ignorar && peaoDoKhan.getParadaAtual() == parada) {
            quantidade++;
        }

        return quantidade;
    }

    public int getOcupacao(int id) {
        Parada parada = paradas.get(id);
        if (parada == null) {
            return -1;
        }

        return ocupacao(parada, null);
    }

    /** Executa um único movimento entre paradas conectadas, sem gastar ativações. */
    public boolean moverPeao(Peao peao, int idDestino) {
        if (peao == null || !peoes.contains(peao)) {
            return false;
        }

        Parada origem = peao.getParadaAtual();
        Parada destino = paradas.get(idDestino);
        if (origem == null || destino == null) {
            return false;
        }

        if (!origem.ehAdjacente(destino)) {
            return false;
        }

        if (ocupacao(destino, peao) >= destino.capacidade()) {
            return false;
        }

        peao.posicionarEm(destino);
        return true;
    }

    /** Calcula um trajeto usando a regra opcional de saltar yurts do proprio jogador. */
    public boolean podeMoverAte(Peao peao, int idDestino, int movimentosDisponiveis) {
        return podeMoverAte(peao, idDestino, movimentosDisponiveis, null);
    }

    public boolean podeMoverAte(Peao peao, int idDestino, int movimentosDisponiveis, Cor corDoJogador) {
        int custo = distanciaMinima(peao, idDestino, corDoJogador);
        if (custo <= 0) {
            return false;
        }
        return custo <= movimentosDisponiveis;
    }

    /** Busca em largura: devolve a quantidade minima de conexoes/saltos ate o destino. */
    public int distanciaMinima(Peao peao, int idDestino) {
        return distanciaMinima(peao, idDestino, null);
    }

    public int distanciaMinima(Peao peao, int idDestino, Cor corDoJogador) {
        if (peao == null || !peoes.contains(peao)) {
            return -1;
        }
        Parada origem = peao.getParadaAtual();
        Parada destino = paradas.get(idDestino);
        if (origem == null || destino == null || origem == destino) {
            return -1;
        }
        if (ocupacao(destino, peao) >= destino.capacidade()) {
            return -1;
        }

        Queue<Parada> fila = new ArrayDeque<Parada>();
        HashMap<Integer, Integer> distancias = new HashMap<Integer, Integer>();
        fila.add(origem);
        distancias.put(origem.getId(), 0);

        while (!fila.isEmpty()) {
            Parada atual = fila.remove();
            int distancia = distancias.get(atual.getId());
            for (Parada proxima : destinosEmUmPasso(atual, peao, corDoJogador)) {
                if (!distancias.containsKey(proxima.getId())) {
                    int proximaDistancia = distancia + 1;
                    if (proxima == destino) {
                        return proximaDistancia;
                    }
                    distancias.put(proxima.getId(), proximaDistancia);
                    fila.add(proxima);
                }
            }
        }
        return -1;
    }

    /** Retorna a sequencia de paradas realmente ocupadas, excluindo a origem. */
    public List<Integer> consultarCaminho(Peao peao, int idDestino) {
        return consultarCaminho(peao, idDestino, null);
    }

    public List<Integer> consultarCaminho(Peao peao, int idDestino, Cor corDoJogador) {
        List<Integer> vazio = new ArrayList<Integer>();
        if (peao == null || !peoes.contains(peao)) {
            return vazio;
        }
        Parada origem = peao.getParadaAtual();
        Parada destino = paradas.get(idDestino);
        if (origem == null || destino == null || origem == destino) {
            return vazio;
        }
        if (ocupacao(destino, peao) >= destino.capacidade()) {
            return vazio;
        }
        Queue<Parada> fila = new ArrayDeque<Parada>();
        HashMap<Integer, Integer> anterior = new HashMap<Integer, Integer>();
        fila.add(origem);
        anterior.put(origem.getId(), -1);
        while (!fila.isEmpty()) {
            Parada atual = fila.remove();
            for (Parada proxima : destinosEmUmPasso(atual, peao, corDoJogador)) {
                if (!anterior.containsKey(proxima.getId())) {
                    anterior.put(proxima.getId(), atual.getId());
                    fila.add(proxima);
                }
            }
        }
        if (!anterior.containsKey(idDestino)) {
            return vazio;
        }
        int atual = idDestino;
        while (atual != origem.getId()) {
            vazio.add(0, atual);
            atual = anterior.get(atual);
        }
        return vazio;
    }

    /** Valida a rota completa antes de alterar a posição do peão. */
    public boolean moverPeaoAte(Peao peao, int idDestino, int movimentosDisponiveis) {
        return moverPeaoAte(peao, idDestino, movimentosDisponiveis, null);
    }

    public boolean moverPeaoAte(Peao peao, int idDestino, int movimentosDisponiveis, Cor corDoJogador) {
        boolean permitido = podeMoverAte(peao, idDestino, movimentosDisponiveis, corDoJogador);
        if (permitido) {
            peao.posicionarEm(paradas.get(idDestino));
        }
        return permitido;
    }

    private List<Parada> destinosEmUmPasso(Parada origem, Peao peao, Cor corDoJogador) {
        LinkedHashSet<Parada> destinos = new LinkedHashSet<Parada>();
        for (Parada vizinha : origem.getAdjacentes()) {
            adicionarDestinoSeValido(destinos, vizinha, peao);
            if (corDoJogador != null && temYurtDoJogador(vizinha.getId(), corDoJogador)) {
                for (Parada aposSalto : vizinha.getAdjacentes()) {
                    if (aposSalto != origem) {
                        adicionarDestinoSeValido(destinos, aposSalto, peao);
                    }
                }
            }
        }
        return new ArrayList<Parada>(destinos);
    }

    /**
     * Paradas ocupadas continuam transitaveis. A capacidade impede apenas que
     * um peao termine o deslocamento nelas, como verificado pelos metodos
     * distanciaMinima e consultarCaminho antes da busca.
     */
    private void adicionarDestinoSeValido(Set<Parada> destinos, Parada destino, Peao peao) {
        if (destino != null) {
            destinos.add(destino);
        }
    }

    /** O Khan pode entrar a partir de fora; movimentos seguintes exigem conexão. */
    public boolean moverKhan(int idDestino) {
        Parada destino = paradas.get(idDestino);
        if (destino == null) {
            return false;
        }

        Parada origem = peaoDoKhan.getParadaAtual();
        if (origem != null && !origem.ehAdjacente(destino)) {
            return false;
        }

        if (ocupacao(destino, peaoDoKhan) >= destino.capacidade()) {
            return false;
        }

        peaoDoKhan.posicionarEm(destino);
        return true;
    }

    /** Registra yurts: parada simples recebe 1; parada dupla pode receber 2; Karakorum nao recebe. */
    public boolean construirYurt(int idParada, Cor cor) {
        Parada parada = paradas.get(idParada);
        if (parada == null || cor == null || cor == Cor.BRANCO) {
            return false;
        }

        if (parada.getTipo() == TipoParada.KARAKORUM) {
            return false;
        }

        ArrayList<Cor> donos = yurts.get(idParada);
        if (donos == null) {
            donos = new ArrayList<Cor>();
            yurts.put(idParada, donos);
        }

        int capacidade = parada.isDupla() ? 2 : 1;
        if (donos.size() >= capacidade) {
            return false;
        }

        donos.add(cor);
        return true;
    }

    /** Habilidade especial de Tolui: posiciona 1 yurt inicial em Karakorum. */
    public boolean posicionarYurtInicialTolui(Cor cor) {
        if (cor == null || cor == Cor.BRANCO || yurtToluiKarakorum != null) {
            return false;
        }
        yurtToluiKarakorum = cor;
        return true;
    }

    public Cor getYurtToluiKarakorum() {
        return yurtToluiKarakorum;
    }

    /** Posiciona 1 yurt no centro de uma cidade conquistada (conta para influencia regional). */
    public boolean registrarYurtConquista(NomeCidade cidade, Cor cor) {
        if (cidade == null || cor == null || cor == Cor.BRANCO || yurtsConquistaCidade.containsKey(cidade)) {
            return false;
        }
        yurtsConquistaCidade.put(cidade, cor);
        return true;
    }

    public Cor getDonoYurtConquista(NomeCidade cidade) {
        return yurtsConquistaCidade.get(cidade);
    }

    public Map<NomeCidade, Cor> getYurtsConquistaCidade() {
        return Collections.unmodifiableMap(yurtsConquistaCidade);
    }

    public DestinoKhan getDestinoKhan() {
        return destinoKhan;
    }

    public boolean deslocarKhan(DestinoKhan destino) {
        if (destino == null || destino == destinoKhan) {
            return false;
        }
        destinoKhan = destino;
        return true;
    }

    public Cor getDonoYurt(int idParada) {
        if (idParada == 0 && yurtToluiKarakorum != null) {
            return yurtToluiKarakorum;
        }
        ArrayList<Cor> donos = yurts.get(idParada);
        if (donos == null || donos.isEmpty()) {
            return null;
        }
        return donos.get(0);
    }

    public int getQuantidadeYurts(int idParada) {
        if (idParada == 0) {
            return yurtToluiKarakorum != null ? 1 : 0;
        }
        ArrayList<Cor> donos = yurts.get(idParada);
        if (donos == null) {
            return 0;
        }
        return donos.size();
    }

    public List<Cor> getDonosYurt(int idParada) {
        if (idParada == 0 && yurtToluiKarakorum != null) {
            return Collections.singletonList(yurtToluiKarakorum);
        }
        ArrayList<Cor> donos = yurts.get(idParada);
        if (donos == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Cor>(donos);
    }

    public boolean temYurtDoJogador(int idParada, Cor cor) {
        if (cor == null) {
            return false;
        }
        if (idParada == 0 && yurtToluiKarakorum == cor) {
            return true;
        }
        ArrayList<Cor> donos = yurts.get(idParada);
        return donos != null && donos.contains(cor);
    }

    // =========================================================================
    // CONSULTAS ESPACIAIS E DE GRAFO (CONSELHEIROS, INFLUENCIA E DESEMPATE)
    // =========================================================================

    /** Total de yurts do jogador no mapa (paradas + conquista em cidades + Tolui em Karakorum). */
    public int contarTotalYurtsNoMapa(Cor cor) {
        if (cor == null) {
            return 0;
        }
        int total = 0;
        for (ArrayList<Cor> donos : yurts.values()) {
            for (Cor dono : donos) {
                if (dono == cor) {
                    total++;
                }
            }
        }
        for (Cor dono : yurtsConquistaCidade.values()) {
            if (dono == cor) {
                total++;
            }
        }
        if (yurtToluiKarakorum == cor) {
            total++;
        }
        return total;
    }

    /** Conta yurts para maioria regional (inclui cidades conquistadas; exclui Karakorum). */
    public int contarYurtsJogadorNaRegiao(Cor cor, Regiao regiao,
                                         Map<Integer, Regiao> regiaoPorParada,
                                         Map<NomeCidade, Regiao> regiaoPorCidade) {
        if (cor == null || regiao == null) {
            return 0;
        }
        int total = 0;
        if (regiaoPorParada != null) {
            for (Map.Entry<Integer, ArrayList<Cor>> entrada : yurts.entrySet()) {
                int idParada = entrada.getKey();
                if (idParada == 0) {
                    continue; // Yurt de Karakorum nao pertence a nenhuma regiao
                }
                if (regiaoPorParada.get(idParada) == regiao) {
                    for (Cor dono : entrada.getValue()) {
                        if (dono == cor) {
                            total++;
                        }
                    }
                }
            }
        }
        if (regiaoPorCidade != null) {
            for (Map.Entry<NomeCidade, Cor> entrada : yurtsConquistaCidade.entrySet()) {
                if (entrada.getValue() == cor && regiaoPorCidade.get(entrada.getKey()) == regiao) {
                    total++;
                }
            }
        }
        return total;
    }

    /** Menor numero de conexoes ate Karakorum (id 0) para criterio de desempate final. */
    public int distanciaMinimaParaKarakorum(int idParadaOrigem) {
        if (idParadaOrigem == 0) {
            return 0;
        }
        Parada origem = paradas.get(idParadaOrigem);
        Parada karakorum = paradas.get(0);
        if (origem == null || karakorum == null) {
            return Integer.MAX_VALUE;
        }

        Queue<Parada> fila = new ArrayDeque<Parada>();
        HashMap<Integer, Integer> distancias = new HashMap<Integer, Integer>();
        fila.add(origem);
        distancias.put(origem.getId(), 0);

        while (!fila.isEmpty()) {
            Parada atual = fila.remove();
            int dist = distancias.get(atual.getId());
            for (Parada vizinha : atual.getAdjacentes()) {
                if (!distancias.containsKey(vizinha.getId())) {
                    int proximaDist = dist + 1;
                    if (vizinha == karakorum) {
                        return proximaDist;
                    }
                    distancias.put(vizinha.getId(), proximaDist);
                    fila.add(vizinha);
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    /** Carta Secreta Viajante: maior grupo conectado de yurts do jogador (inclui ramificacoes). */
    public int maiorRedeConectadaYurts(Cor cor) {
        if (cor == null) {
            return 0;
        }
        Set<Integer> visitados = new HashSet<Integer>();
        int maiorGrupo = 0;

        for (Parada inicial : paradas.values()) {
            int idInicial = inicial.getId();
            if (!visitados.contains(idInicial) && temYurtDoJogador(idInicial, cor)) {
                int tamanhoGrupo = 0;
                Queue<Parada> fila = new ArrayDeque<Parada>();
                fila.add(inicial);
                visitados.add(idInicial);

                while (!fila.isEmpty()) {
                    Parada atual = fila.remove();
                    tamanhoGrupo += contarYurtsDoJogadorNaParada(atual.getId(), cor);

                    for (Parada vizinha : atual.getAdjacentes()) {
                        int idViz = vizinha.getId();
                        if (!visitados.contains(idViz) && temYurtDoJogador(idViz, cor)) {
                            visitados.add(idViz);
                            fila.add(vizinha);
                        }
                    }
                }
                if (tamanhoGrupo > maiorGrupo) {
                    maiorGrupo = tamanhoGrupo;
                }
            }
        }
        return maiorGrupo;
    }

    /** Carta Secreta Mensageiro: paradas duplas (exceto Karakorum) com pelo menos 1 yurt do jogador. */
    public int contarParadasDuplasComYurt(Cor cor) {
        if (cor == null) {
            return 0;
        }
        int total = 0;
        for (Parada parada : paradas.values()) {
            if (parada.isDupla()
                    && parada.getTipo() != TipoParada.KARAKORUM
                    && temYurtDoJogador(parada.getId(), cor)) {
                total++;
            }
        }
        return total;
    }

    /** Carta Secreta Bajulador: provincias com icone de Khan que possuem >= 2 yurts adjacentes. */
    public int contarProvinciasKhanComDoisYurts(Cor cor, Map<DestinoKhan, Set<Integer>> paradasPorProvinciaKhan) {
        if (cor == null || paradasPorProvinciaKhan == null) {
            return 0;
        }
        int provinciasAtendidas = 0;
        for (Set<Integer> paradasAdjacentes : paradasPorProvinciaKhan.values()) {
            int somaYurts = 0;
            for (Integer idParada : paradasAdjacentes) {
                somaYurts += contarYurtsDoJogadorNaParada(idParada, cor);
            }
            if (somaYurts >= 2) {
                provinciasAtendidas++;
            }
        }
        return provinciasAtendidas;
    }

    /** Cartas Secretas Prefeito/Cidadao: cidades com >= minimoYurts nas paradas adjacentes. */
    public int contarCidadesComYurtsAdjacentes(Cor cor, Collection<Set<Integer>> paradasPorCidade,
                                               int minimoYurtsPorCidade) {
        if (cor == null || paradasPorCidade == null) {
            return 0;
        }
        int cidadesAtendidas = 0;
        for (Set<Integer> paradasAdjacentes : paradasPorCidade) {
            int somaYurts = 0;
            for (Integer idParada : paradasAdjacentes) {
                somaYurts += contarYurtsDoJogadorNaParada(idParada, cor);
            }
            if (somaYurts >= minimoYurtsPorCidade) {
                cidadesAtendidas++;
            }
        }
        return cidadesAtendidas;
    }

    private int contarYurtsDoJogadorNaParada(int idParada, Cor cor) {
        if (idParada == 0) {
            return yurtToluiKarakorum == cor ? 1 : 0;
        }
        ArrayList<Cor> donos = yurts.get(idParada);
        if (donos == null) {
            return 0;
        }
        int qtd = 0;
        for (Cor dono : donos) {
            if (dono == cor) {
                qtd++;
            }
        }
        return qtd;
    }
}