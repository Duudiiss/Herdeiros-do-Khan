package br.herdeiros.model;

import java.io.Serializable;
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
import br.herdeiros.mapa.MapaDados;

/** Armazena as posições, conexões e peças e verifica movimentação e ocupação. */
class Tabuleiro implements Serializable {
    private static final long serialVersionUID = 1L;

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

    static Tabuleiro restaurar(List<Cidade> cidades, MapaDados mapa,
                               List<Jogador> jogadores, Map<Cor, Integer> posicoes,
                               SavePartidaData.Tabuleiro estado) {
        if (cidades == null || mapa == null || jogadores == null || posicoes == null || estado == null
                || mapa.getPonto(0) == null) {
            throw new IllegalArgumentException("Dados do tabuleiro incompletos");
        }
        Tabuleiro tabuleiro = new Tabuleiro(cidades);
        for (MapaDados.Ponto ponto : mapa.getPontos().values()) {
            if (ponto.getId() != 0 && !tabuleiro.adicionarParada(ponto.getId(), ponto.isDupla())) {
                throw new IllegalArgumentException("Parada duplicada no mapa");
            }
        }
        for (MapaDados.Ligacao ligacao : mapa.getLigacoes()) {
            if (!tabuleiro.conectarParadas(ligacao.getOrigem(), ligacao.getDestino())) {
                throw new IllegalArgumentException("Conexão inválida no mapa");
            }
        }
        for (Jogador jogador : jogadores) {
            Integer idParada = posicoes.get(jogador.getCor());
            Parada parada = idParada == null ? null : tabuleiro.paradas.get(idParada);
            if (parada == null) {
                throw new IllegalArgumentException("Posição de jogador inválida");
            }
            if (!tabuleiro.posicionarPeaoInicial(jogador.getMeuPeao())) {
                throw new IllegalArgumentException("Peão de jogador inválido");
            }
            jogador.getMeuPeao().posicionarEm(parada);
        }
        if (estado.paradaKhan >= 0) {
            Parada paradaKhan = tabuleiro.paradas.get(estado.paradaKhan);
            if (paradaKhan == null) {
                throw new IllegalArgumentException("Posição do Khan inválida");
            }
            tabuleiro.peaoDoKhan.posicionarEm(paradaKhan);
        }
        for (Parada parada : tabuleiro.paradas.values()) {
            if (tabuleiro.ocupacao(parada, null) > parada.capacidade()) {
                throw new IllegalArgumentException("Capacidade de parada excedida");
            }
        }
        if (estado.destinoKhan != null) {
            try {
                tabuleiro.destinoKhan = DestinoKhan.valueOf(estado.destinoKhan);
            } catch (IllegalArgumentException erro) {
                throw new IllegalArgumentException("Destino do Khan inválido", erro);
            }
        }
        if (estado.yurts == null) {
            throw new IllegalArgumentException("Lista de yurts ausente");
        }
        for (SavePartidaData.Yurt yurt : estado.yurts) {
            if (yurt == null || yurt.cor == null) {
                throw new IllegalArgumentException("Yurt inválido");
            }
            Cor cor;
            try {
                cor = Cor.valueOf(yurt.cor);
            } catch (IllegalArgumentException erro) {
                throw new IllegalArgumentException("Cor de yurt inválida", erro);
            }
            if (!tabuleiro.construirYurt(yurt.parada, cor)) {
                throw new IllegalArgumentException("Posição de yurt inválida");
            }
        }
        if (estado.yurtToluiKarakorum != null) {
            if (!tabuleiro.posicionarYurtInicialTolui(Cor.valueOf(estado.yurtToluiKarakorum))) {
                throw new IllegalArgumentException("Yurt inicial de Tolui invalido.");
            }
        }
        if (estado.yurtsConquistaCidade != null) {
            for (Map.Entry<String, String> entrada : estado.yurtsConquistaCidade.entrySet()) {
                if (!tabuleiro.registrarYurtConquista(
                        NomeCidade.valueOf(entrada.getKey()), Cor.valueOf(entrada.getValue()))) {
                    throw new IllegalArgumentException("Yurt de cidade conquistada invalido.");
                }
            }
        }
        return tabuleiro;
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

    List<Parada> getParadas() {
        return new ArrayList<Parada>(paradas.values());
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

    /**
     * Cada aresta tem custo 1, exceto entrar numa parada com yurt proprio.
     * Essa entrada custa zero, mas exige ao menos um cavalo ainda disponivel.
     * A busca usa estado (parada, cavalos gastos), pois um caminho de custo
     * igual pode deixar recursos diferentes nas etapas intermediarias.
     */
    private static final class EstadoCaminho {
        private final Parada parada;
        private final int gasto;
        private final EstadoCaminho anterior;

        private EstadoCaminho(Parada parada, int gasto, EstadoCaminho anterior) {
            this.parada = parada;
            this.gasto = gasto;
            this.anterior = anterior;
        }
    }

    private EstadoCaminho encontrarRota(Peao peao, int destinoId, Cor cor, int cavalos) {
        if (peao == null || !peoes.contains(peao) || cavalos < 0) {
            return null;
        }
        Parada origem = peao.getParadaAtual();
        Parada destino = paradas.get(destinoId);
        if (origem == null || destino == null || origem == destino
                || ocupacao(destino, peao) >= destino.capacidade()) {
            return null;
        }
        int limite = Math.min(cavalos, paradas.size());
        java.util.PriorityQueue<EstadoCaminho> fila = new java.util.PriorityQueue<>(
                java.util.Comparator.comparingInt(estado -> estado.gasto));
        Map<Integer, Set<Integer>> visitados = new HashMap<>();
        fila.add(new EstadoCaminho(origem, 0, null));
        while (!fila.isEmpty()) {
            EstadoCaminho atual = fila.remove();
            Set<Integer> gastosNaParada = visitados.computeIfAbsent(
                    atual.parada.getId(), ignorada -> new HashSet<Integer>());
            if (!gastosNaParada.add(atual.gasto)) {
                continue;
            }
            if (atual.parada == destino) {
                return atual;
            }
            for (Parada vizinha : atual.parada.getAdjacentes()) {
                boolean gratuito = cor != null && temYurtDoJogador(vizinha.getId(), cor);
                // Nao se pode iniciar um movimento gratuito sem um cavalo na reserva.
                if (gratuito && atual.gasto >= cavalos) {
                    continue;
                }
                int novoGasto = atual.gasto + (gratuito ? 0 : 1);
                if (novoGasto > limite) {
                    continue;
                }
                Set<Integer> gastosConhecidos = visitados.get(vizinha.getId());
                if (gastosConhecidos == null || !gastosConhecidos.contains(novoGasto)) {
                    fila.add(new EstadoCaminho(vizinha, novoGasto, atual));
                }
            }
        }
        return null;
    }

    public boolean podeMoverAte(Peao peao, int destino, int cavalos) {
        return podeMoverAte(peao, destino, cavalos, null);
    }

    public boolean podeMoverAte(Peao peao, int destino, int cavalos, Cor cor) {
        return encontrarRota(peao, destino, cor, cavalos) != null;
    }

    public int distanciaMinima(Peao peao, int destino) {
        return distanciaMinima(peao, destino, null);
    }

    public int distanciaMinima(Peao peao, int destino, Cor cor) {
        // Sem restricao de saldo: custo minimo teorico.
        return distanciaMinima(peao, destino, cor, paradas.size());
    }

    public int distanciaMinima(Peao peao, int destino, Cor cor, int cavalos) {
        EstadoCaminho rota = encontrarRota(peao, destino, cor, cavalos);
        return rota == null ? -1 : rota.gasto;
    }

    public List<Integer> consultarCaminho(Peao peao, int destino) {
        return consultarCaminho(peao, destino, null);
    }

    public List<Integer> consultarCaminho(Peao peao, int destino, Cor cor) {
        return consultarCaminho(peao, destino, cor, paradas.size());
    }

    public List<Integer> consultarCaminho(Peao peao, int destino, Cor cor, int cavalos) {
        List<Integer> caminho = new ArrayList<Integer>();
        EstadoCaminho rota = encontrarRota(peao, destino, cor, cavalos);
        while (rota != null && rota.anterior != null) {
            caminho.add(0, rota.parada.getId());
            rota = rota.anterior;
        }
        return caminho;
    }

    public boolean moverPeaoAte(Peao peao, int destino, int cavalos) {
        return moverPeaoAte(peao, destino, cavalos, null);
    }

    public boolean moverPeaoAte(Peao peao, int destino, int cavalos, Cor cor) {
        if (!podeMoverAte(peao, destino, cavalos, cor)) {
            return false;
        }
        peao.posicionarEm(paradas.get(destino));
        return true;
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
        if (parada == null || cor == null || cor == Cor.BRANCO
                || contarTotalYurtsNoMapa(cor) >= 12) {
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
        if (cor == null || cor == Cor.BRANCO || yurtToluiKarakorum != null
                || contarTotalYurtsNoMapa(cor) >= 12) {
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
        if (cidade == null || cor == null || cor == Cor.BRANCO
                || yurtsConquistaCidade.containsKey(cidade)
                || contarTotalYurtsNoMapa(cor) >= 12) {
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

    /** Apenas yurts colocados em paradas normais da regiao, sem cidades ou Karakorum. */
    public int contarYurtsNormaisNaRegiao(Cor cor, Regiao regiao,
                                         Map<Integer, Regiao> regioes) {
        if (cor == null || regiao == null || regioes == null) {
            return 0;
        }
        int total = 0;
        for (Map.Entry<Integer, ArrayList<Cor>> item : yurts.entrySet()) {
            if (item.getKey() != 0 && regioes.get(item.getKey()) == regiao) {
                for (Cor dono : item.getValue()) {
                    if (dono == cor) {
                        total++;
                    }
                }
            }
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

    /** Conta yurts distintos presentes nas paradas de acesso aos icones do Khan. */
    public int contarYurtsAdjacentesProvinciasKhan(Cor cor,
            Map<DestinoKhan, Set<Integer>> acessos) {
        if (cor == null || acessos == null) {
            return 0;
        }
        Set<Integer> paradasUnicas = new HashSet<Integer>();
        for (Set<Integer> ids : acessos.values()) {
            if (ids != null) {
                paradasUnicas.addAll(ids);
            }
        }
        int total = 0;
        for (Integer id : paradasUnicas) {
            total += contarYurtsDoJogadorNaParada(id, cor);
        }
        return total;
    }

    /** Conta yurts distintos adjacentes a uma ou mais cidades, sem duplicacao. */
    public int contarYurtsAdjacentesCidades(Cor cor,
            Map<NomeCidade, Set<Integer>> acessos) {
        if (cor == null || acessos == null) {
            return 0;
        }
        Set<Integer> paradasUnicas = new HashSet<Integer>();
        for (Set<Integer> ids : acessos.values()) {
            if (ids != null) {
                paradasUnicas.addAll(ids);
            }
        }
        int total = 0;
        for (Integer id : paradasUnicas) {
            total += contarYurtsDoJogadorNaParada(id, cor);
        }
        return total;
    }

    /** Conta locais de entrega distintos que possuem yurt em uma parada vizinha. */
    public int contarPontosEntregaComYurtAdjacente(Cor cor, Collection<Integer> pontosEntrega) {
        if (cor == null || pontosEntrega == null) {
            return 0;
        }
        int total = 0;
        Set<Integer> locaisVisitados = new HashSet<Integer>();
        for (Integer id : pontosEntrega) {
            if (id == null || !locaisVisitados.add(id)) {
                continue;
            }
            Parada local = paradas.get(id);
            if (local == null) {
                continue;
            }
            for (Parada vizinha : local.getAdjacentes()) {
                if (temYurtDoJogador(vizinha.getId(), cor)) {
                    total++;
                    break;
                }
            }
        }
        return total;
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