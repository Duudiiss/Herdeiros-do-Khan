package br.herdeiros.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import br.herdeiros.mapa.MapaDados;

/** API mínima para a interface da segunda iteração; não expõe as classes internas. */
public class InterfacePartida implements ObservadoPartida, Serializable {
    private static final long serialVersionUID = 1L;

    private final Jogo jogo;
    private final MercadoMelhorias mercadoMelhorias;
    private final List<Jogador> participantes;
    private transient List<ObservadorPartida> observadores = new ArrayList<>();
    private final Map<String, Set<Integer>> acessosCidades = new HashMap<String, Set<Integer>>();
    private final Set<Integer> paradasVisitadasTurno = new HashSet<Integer>();
    private final Map<String, Integer> saquesNaCidadeNoTurno = new HashMap<String, Integer>();
    private final Map<String, Cor> cidadesConquistadas = new HashMap<String, Cor>();
    private final Map<Provincia, ReservaProvincia> reservas = new HashMap<Provincia, ReservaProvincia>();
    private final Map<Provincia, Provincia[]> vizinhasDoKhan = new HashMap<Provincia, Provincia[]>();

    /** Estruturas de Conselheiros, Tesouros, Bonus, Herdeiros e Pontuacao Final */
    private final List<LocalConselheiro> locaisConselheiros = new ArrayList<LocalConselheiro>();
    private final List<CartaConselheiroAberto> cartasAbertasAtivas = new ArrayList<CartaConselheiroAberto>();
    private final List<TipoTesouro> pilhaTesouros = new ArrayList<TipoTesouro>();
    private final List<TipoTesouro> descarteTesouros = new ArrayList<TipoTesouro>();
    private final List<PecaBonus> poolPecasBonus = new ArrayList<PecaBonus>();
    private final Map<Jogador, List<TipoTesouro>> tesourosPorJogador = new HashMap<Jogador, List<TipoTesouro>>();
    private final Map<Jogador, List<PecaBonus>> bonusPorJogador = new HashMap<Jogador, List<PecaBonus>>();
    private final Map<Jogador, List<CartaConselheiroSecreto>> opcoesSecretasPorJogador =
            new HashMap<Jogador, List<CartaConselheiroSecreto>>();
    private final Map<Jogador, CartaConselheiroSecreto> secretaPorJogador =
            new HashMap<Jogador, CartaConselheiroSecreto>();
    private final Map<Jogador, Integer> cidadesConquistadasPorJogador = new HashMap<Jogador, Integer>();
    private final Map<Jogador, Peao> peaoExtraAltaniPorJogador = new HashMap<Jogador, Peao>();

    /**  Mapeamentos espaciais para influencia regional e cartas secretas*/
    private final Map<Integer, Regiao> regiaoPorParada = new HashMap<Integer, Regiao>();
    private final Map<NomeCidade, Regiao> regiaoPorCidade = new HashMap<NomeCidade, Regiao>();
    private final Map<DestinoKhan, Set<Integer>> paradasPorProvinciaKhan =
            new HashMap<DestinoKhan, Set<Integer>>();
    private final Map<NomeCidade, Set<Integer>> paradasPorCidade = new HashMap<NomeCidade, Set<Integer>>();

    private int indiceAtual = 0;
    private transient MapaDados mapaDados = new MapaDados();
    private transient SavePartidaData.Meta metaSave = new SavePartidaData.Meta();
    private int rodadasConcluidas;
    private int pontuacaoKhan;
    private int rodadasNoDisparoFim = -1;
    private boolean partidaEncerrada;
    private boolean usandoPeaoExtraAltani;
    private boolean acaoRealizadaNoTurno;
    private int bonusMovimentosTurno;
    private int bonusPunhosTurno;

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        observadores = new ArrayList<>();
    }

    boolean estadoValidoParaSave() {
        if (jogo == null || mercadoMelhorias == null || participantes == null
                || participantes.size() < 2 || participantes.size() > 5
                || indiceAtual < 0 || indiceAtual >= participantes.size()
                || jogo.getTabuleiro() == null) {
            return false;
        }
        for (Jogador participante : participantes) {
            if (participante == null || participante.getNome() == null
                    || participante.getCor() == null || participante.getPersonagem() == null) {
                return false;
            }
        }
        return true;
    }

    boolean podeSalvarEntreTurnos() {
        return estadoValidoParaSave()
                && participantes.get(indiceAtual).getTabuleiroIndividual().podeSalvarEntreTurnos();
    }

    SavePartidaData criarDadosSave() {
        if (!podeSalvarEntreTurnos() || mapaDados == null) {
            throw new IllegalArgumentException(
                    "O save exige uma partida válida, mapa carregado e turno não iniciado.");
        }

        SavePartidaData dados = new SavePartidaData();
        dados.meta = copiarMeta(metaSave);
        dados.partida.rodadasConcluidas = rodadasConcluidas;
        dados.partida.jogadorAtual = indiceAtual;
        dados.partida.pontuacaoKhan = pontuacaoKhan;
        dados.partida.rodadasNoDisparoFim = rodadasNoDisparoFim;
        dados.partida.encerrada = partidaEncerrada;

        for (MapaDados.Ponto ponto : mapaDados.getPontos().values()) {
            SavePartidaData.Ponto salvo = new SavePartidaData.Ponto();
            salvo.id = ponto.getId();
            salvo.x = ponto.getX();
            salvo.y = ponto.getY();
            salvo.dupla = ponto.isDupla();
            salvo.raio = ponto.getRaio();
            dados.mapa.paradas.add(salvo);
        }
        for (MapaDados.Ligacao ligacao : mapaDados.getLigacoes()) {
            SavePartidaData.Ligacao salva = new SavePartidaData.Ligacao();
            salva.origem = ligacao.getOrigem();
            salva.destino = ligacao.getDestino();
            dados.mapa.conexoes.add(salva);
        }
        for (Map.Entry<String, Set<Integer>> acesso : acessosCidades.entrySet()) {
            dados.mapa.acessosCidades.put(acesso.getKey(), new ArrayList<Integer>(acesso.getValue()));
        }
        for (Map.Entry<Provincia, Provincia[]> vizinhas : vizinhasDoKhan.entrySet()) {
            List<String> nomes = new ArrayList<String>();
            for (Provincia provincia : vizinhas.getValue()) {
                nomes.add(provincia.name());
            }
            dados.mapa.vizinhasKhan.put(vizinhas.getKey().name(), nomes);
        }

        for (Jogador jogador : participantes) {
            SavePartidaData.Jogador salvo = new SavePartidaData.Jogador();
            salvo.nome = jogador.getNome();
            salvo.cor = jogador.getCor().name();
            salvo.personagem = jogador.getPersonagem().name();
            salvo.votos = jogador.getVotosKurultai();
            salvo.descendentes = jogador.getQuantidadeDescendentes();
            Parada parada = jogador.getMeuPeao().getParadaAtual();
            if (parada == null) {
                throw new IllegalArgumentException("Jogador sem posição no tabuleiro.");
            }
            salvo.parada = parada.getId();
            TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
            salvo.colunasOcupadas = cartela.getColunasOcupadas();
            for (TipoTributo tipo : TipoTributo.values()) {
                int quantidade = cartela.getQuantidadeTributoDoTipo(tipo);
                if (quantidade > 0) {
                    salvo.tributos.put(tipo.name(), quantidade);
                }
            }
            for (Melhoria melhoria : cartela.getMelhoriasAdquiridas()) {
                salvo.melhorias.add(criarDadosMelhoria(melhoria));
            }
            dados.jogadores.add(salvo);
        }

        Tabuleiro tabuleiro = jogo.getTabuleiro();
        Parada paradaKhan = tabuleiro.getPeaoDoKhan().getParadaAtual();
        dados.tabuleiro.paradaKhan = paradaKhan == null ? -1 : paradaKhan.getId();
        dados.tabuleiro.destinoKhan = tabuleiro.getDestinoKhan() == null
                ? null : tabuleiro.getDestinoKhan().name();
        for (Parada parada : tabuleiro.getParadas()) {
            for (Cor cor : tabuleiro.getDonosYurt(parada.getId())) {
                SavePartidaData.Yurt yurt = new SavePartidaData.Yurt();
                yurt.parada = parada.getId();
                yurt.cor = cor.name();
                dados.tabuleiro.yurts.add(yurt);
            }
        }
        for (Cidade cidade : tabuleiro.getCidadesDisponiveis()) {
            dados.tabuleiro.cidadesDisponiveis.add(criarDadosCidade(cidade));
        }
        for (Map.Entry<String, Cor> conquistada : cidadesConquistadas.entrySet()) {
            dados.tabuleiro.cidadesConquistadas.put(conquistada.getKey(), conquistada.getValue().name());
        }

        for (Cidade cidade : jogo.getBaralho().getOrdemRestante()) {
            dados.baralhos.cidadesRestantes.add(criarDadosCidade(cidade));
        }
        for (Melhoria melhoria : mercadoMelhorias.getMelhoriasDisponiveis()) {
            dados.baralhos.melhoriasDisponiveis.add(
                    melhoria == null ? null : criarDadosMelhoria(melhoria));
        }
        for (Melhoria melhoria : mercadoMelhorias.getMelhoriasRestantes()) {
            dados.baralhos.melhoriasRestantes.add(criarDadosMelhoria(melhoria));
        }

        for (Map.Entry<Provincia, ReservaProvincia> entrada : reservas.entrySet()) {
            SavePartidaData.Provincia provincia = new SavePartidaData.Provincia();
            provincia.nome = entrada.getKey().name();
            provincia.tipoPrincipal = entrada.getValue().getTipo().name();
            for (Map.Entry<TipoTributo, Integer> quantidade
                    : entrada.getValue().getQuantidades().entrySet()) {
                if (quantidade.getValue() > 0) {
                    provincia.quantidades.put(quantidade.getKey().name(), quantidade.getValue());
                }
            }
            provincia.acessos.addAll(entrada.getValue().getAcessos());
            dados.provincias.add(provincia);
        }
        return dados;
    }

    static InterfacePartida restaurarDadosSave(SavePartidaData dados) {
        if (dados == null || dados.meta == null || !"herdeiros-save".equals(dados.meta.formato)
                || dados.meta.versao != 1
                || !("partida".equals(dados.meta.tipo) || "cenario".equals(dados.meta.tipo))
                || dados.meta.descricao == null || dados.partida == null || dados.mapa == null
                || dados.jogadores == null || dados.jogadores.size() < 2
                || dados.jogadores.size() > 5 || dados.tabuleiro == null
                || dados.baralhos == null || dados.provincias == null) {
            throw new IllegalArgumentException("Estrutura do save JSON inválida ou incompatível.");
        }
        if (dados.partida.jogadorAtual < 0
                || dados.partida.jogadorAtual >= dados.jogadores.size()
                || dados.partida.rodadasConcluidas < 0 || dados.partida.pontuacaoKhan < 0
                || dados.partida.rodadasNoDisparoFim < -1) {
            throw new IllegalArgumentException("Estado da partida inválido.");
        }

        MapaDados mapa = restaurarMapa(dados.mapa);
        List<Jogador> jogadores = new ArrayList<Jogador>();
        Map<Cor, Integer> posicoes = new HashMap<Cor, Integer>();
        Set<Cor> cores = new HashSet<Cor>();
        Set<Personagem> personagens = new HashSet<Personagem>();
        for (SavePartidaData.Jogador salvo : dados.jogadores) {
            if (salvo == null || salvo.nome == null || salvo.cor == null
                    || salvo.personagem == null || salvo.tributos == null
                    || salvo.melhorias == null || salvo.colunasOcupadas == null) {
                throw new IllegalArgumentException("Dados de jogador incompletos.");
            }
            Cor cor = Cor.valueOf(salvo.cor);
            Personagem personagem = Personagem.valueOf(salvo.personagem);
            if (cor == Cor.BRANCO || !cores.add(cor) || !personagens.add(personagem)) {
                throw new IllegalArgumentException("Cores ou personagens de jogador repetidos.");
            }
            TabuleiroIndividual cartela = new TabuleiroIndividual();
            List<Tributo> tributos = new ArrayList<Tributo>();
            for (Map.Entry<String, Integer> quantidade : salvo.tributos.entrySet()) {
                TipoTributo tipo = TipoTributo.valueOf(quantidade.getKey());
                if (quantidade.getValue() == null || quantidade.getValue() < 0
                        || quantidade.getValue() > 100) {
                    throw new IllegalArgumentException("Quantidade de tributo inválida.");
                }
                for (int i = 0; i < quantidade.getValue(); i++) {
                    tributos.add(new Tributo(tipo));
                }
            }
            List<Melhoria> melhorias = new ArrayList<Melhoria>();
            for (SavePartidaData.Melhoria melhoria : salvo.melhorias) {
                melhorias.add(restaurarMelhoria(melhoria));
            }
            cartela.restaurarEstado(tributos, melhorias, salvo.colunasOcupadas);
            jogadores.add(new Jogador(salvo.nome, cor, salvo.descendentes, personagem,
                    cartela, salvo.votos));
            posicoes.put(cor, salvo.parada);
        }

        List<Cidade> cidadesDisponiveis = restaurarCidades(dados.tabuleiro.cidadesDisponiveis);
        List<Cidade> cidadesRestantes = restaurarCidades(dados.baralhos.cidadesRestantes);
        BaralhoDeCidades baralho = new BaralhoDeCidades(cidadesRestantes);
        Tabuleiro tabuleiro = Tabuleiro.restaurar(cidadesDisponiveis, mapa,
                jogadores, posicoes, dados.tabuleiro);

        if (dados.baralhos.melhoriasDisponiveis == null
                || dados.baralhos.melhoriasDisponiveis.size() != 5
                || dados.baralhos.melhoriasRestantes == null) {
            throw new IllegalArgumentException("Estado do mercado de melhorias inválido.");
        }
        List<Melhoria> disponiveis = restaurarMelhoriasComNulos(
                dados.baralhos.melhoriasDisponiveis);
        List<Melhoria> pilhaMelhorias = restaurarMelhorias(dados.baralhos.melhoriasRestantes);
        MercadoMelhorias mercado = new MercadoMelhorias(pilhaMelhorias, disponiveis);
        Jogo jogo = new Jogo(jogadores, baralho, tabuleiro);
        return new InterfacePartida(dados, mapa, jogo, mercado, jogadores);
    }

    private InterfacePartida(SavePartidaData dados, MapaDados mapa, Jogo jogo,
                             MercadoMelhorias mercado, List<Jogador> jogadores) {
        this.mapaDados = mapa;
        this.metaSave = copiarMeta(dados.meta);
        this.jogo = jogo;
        this.mercadoMelhorias = mercado;
        this.participantes = new ArrayList<Jogador>(jogadores);
        this.indiceAtual = dados.partida.jogadorAtual;
        this.rodadasConcluidas = dados.partida.rodadasConcluidas;
        this.pontuacaoKhan = dados.partida.pontuacaoKhan;
        this.rodadasNoDisparoFim = dados.partida.rodadasNoDisparoFim;
        this.partidaEncerrada = dados.partida.encerrada;
        for (Map.Entry<String, List<Integer>> acesso : dados.mapa.acessosCidades.entrySet()) {
            NomeCidade.valueOf(acesso.getKey());
            if (acesso.getValue() == null || acesso.getValue().isEmpty()) {
                throw new IllegalArgumentException("Acesso de cidade inválido.");
            }
            for (Integer parada : acesso.getValue()) {
                if (parada == null || mapa.getPonto(parada) == null) {
                    throw new IllegalArgumentException("Parada de acesso de cidade inexistente.");
                }
            }
            this.acessosCidades.put(acesso.getKey(), new HashSet<Integer>(acesso.getValue()));
        }
        for (SavePartidaData.Provincia salvo : dados.provincias) {
            if (salvo == null || salvo.nome == null || salvo.tipoPrincipal == null
                    || salvo.quantidades == null || salvo.acessos == null) {
                throw new IllegalArgumentException("Dados de província incompletos.");
            }
            Provincia nome = Provincia.valueOf(salvo.nome);
            TipoTributo tipo = TipoTributo.valueOf(salvo.tipoPrincipal);
            for (Integer acesso : salvo.acessos) {
                if (acesso == null || mapa.getPonto(acesso) == null) {
                    throw new IllegalArgumentException("Parada de acesso de província inexistente.");
                }
            }
            EnumMap<TipoTributo, Integer> quantidades =
                    new EnumMap<TipoTributo, Integer>(TipoTributo.class);
            for (TipoTributo opcao : TipoTributo.values()) {
                quantidades.put(opcao, 0);
            }
            for (Map.Entry<String, Integer> quantidade : salvo.quantidades.entrySet()) {
                quantidades.put(TipoTributo.valueOf(quantidade.getKey()), quantidade.getValue());
            }
            if (this.reservas.put(nome, ReservaProvincia.restaurar(
                    tipo, quantidades, new HashSet<Integer>(salvo.acessos))) != null) {
                throw new IllegalArgumentException("Província duplicada no save.");
            }
        }
        for (Map.Entry<String, List<String>> entrada : dados.mapa.vizinhasKhan.entrySet()) {
            if (entrada.getValue() == null || entrada.getValue().size() != 2) {
                throw new IllegalArgumentException("Vizinhança do Khan inválida.");
            }
            Provincia origem = Provincia.valueOf(entrada.getKey());
            Provincia primeira = Provincia.valueOf(entrada.getValue().get(0));
            Provincia segunda = Provincia.valueOf(entrada.getValue().get(1));
            if (origem == primeira || origem == segunda || primeira == segunda
                    || this.vizinhasDoKhan.put(origem,
                    new Provincia[] {primeira, segunda}) != null) {
                throw new IllegalArgumentException("Vizinhança do Khan inválida.");
            }
        }
        for (Map.Entry<String, String> entrada : dados.tabuleiro.cidadesConquistadas.entrySet()) {
            NomeCidade.valueOf(entrada.getKey());
            Cor cor = Cor.valueOf(entrada.getValue());
            boolean jogadorExiste = false;
            for (Jogador jogador : this.participantes) {
                if (jogador.getCor() == cor) {
                    jogadorExiste = true;
                    break;
                }
            }
            if (cor == Cor.BRANCO || !jogadorExiste) {
                throw new IllegalArgumentException("Conquistador de cidade inválido.");
            }
            this.cidadesConquistadas.put(entrada.getKey(), cor);
        }
        if (!estadoValidoParaSave()) {
            throw new IllegalArgumentException("O estado reconstruído da partida é inválido.");
        }
        if (!partidaEncerrada) {
            registrarParadaAtualNoTurno();
        }
    }

    private static SavePartidaData.Meta copiarMeta(SavePartidaData.Meta original) {
        SavePartidaData.Meta copia = new SavePartidaData.Meta();
        if (original != null) {
            copia.formato = original.formato;
            copia.versao = original.versao;
            copia.tipo = original.tipo;
            copia.descricao = original.descricao;
        }
        return copia;
    }

    private static MapaDados restaurarMapa(SavePartidaData.Mapa dados) {
        if (dados.paradas == null || dados.conexoes == null
                || dados.acessosCidades == null || dados.vizinhasKhan == null) {
            throw new IllegalArgumentException("Mapa incompleto no save.");
        }
        MapaDados mapa = new MapaDados();
        Set<Integer> ids = new HashSet<Integer>();
        for (SavePartidaData.Ponto ponto : dados.paradas) {
            if (ponto == null || !ids.add(ponto.id)) {
                throw new IllegalArgumentException("Parada duplicada ou inválida no mapa.");
            }
            if (ponto.id == 0) {
                if (ponto.dupla || !mapa.mover(0, ponto.x, ponto.y)) {
                    throw new IllegalArgumentException("Karakorum inválido no mapa.");
                }
            } else if (!mapa.adicionar(ponto.id, ponto.x, ponto.y, ponto.dupla)) {
                throw new IllegalArgumentException("Coordenadas de parada inválidas.");
            }
            double raio = ponto.raio == null
                    ? MapaDados.raioPadrao(ponto.id, ponto.dupla) : ponto.raio;
            if (!mapa.alterarRaio(ponto.id, raio)) {
                throw new IllegalArgumentException("Raio de parada inválido.");
            }
        }
        if (!ids.contains(0)) {
            throw new IllegalArgumentException("O mapa não contém Karakorum.");
        }
        for (SavePartidaData.Ligacao ligacao : dados.conexoes) {
            if (ligacao == null || !mapa.conectar(ligacao.origem, ligacao.destino)) {
                throw new IllegalArgumentException("Conexão inválida no mapa.");
            }
        }
        return mapa;
    }

    private static List<Cidade> restaurarCidades(List<SavePartidaData.Cidade> cidades) {
        if (cidades == null) {
            throw new IllegalArgumentException("Baralho de cidades incompleto.");
        }
        List<Cidade> restauradas = new ArrayList<Cidade>();
        for (SavePartidaData.Cidade cidade : cidades) {
            if (cidade == null || cidade.nome == null || cidade.regiao == null) {
                throw new IllegalArgumentException("Carta de cidade inválida.");
            }
            restauradas.add(new Cidade(NomeCidade.valueOf(cidade.nome),
                    Regiao.valueOf(cidade.regiao), cidade.tesouros, cidade.conquistada));
        }
        return restauradas;
    }

    private static List<Melhoria> restaurarMelhorias(
            List<SavePartidaData.Melhoria> melhorias) {
        if (melhorias == null) {
            throw new IllegalArgumentException("Pilha de melhorias incompleta.");
        }
        List<Melhoria> restauradas = new ArrayList<Melhoria>();
        for (SavePartidaData.Melhoria melhoria : melhorias) {
            restauradas.add(restaurarMelhoria(melhoria));
        }
        return restauradas;
    }

    private static List<Melhoria> restaurarMelhoriasComNulos(
            List<SavePartidaData.Melhoria> melhorias) {
        List<Melhoria> restauradas = new ArrayList<Melhoria>();
        for (SavePartidaData.Melhoria melhoria : melhorias) {
            restauradas.add(melhoria == null ? null : restaurarMelhoria(melhoria));
        }
        return restauradas;
    }

    private static Melhoria restaurarMelhoria(SavePartidaData.Melhoria melhoria) {
        if (melhoria == null || melhoria.codigo == null) {
            throw new IllegalArgumentException("Código de melhoria ausente.");
        }
        return MercadoMelhorias.criarPorCodigo(melhoria.codigo, melhoria.coluna);
    }

    private static SavePartidaData.Melhoria criarDadosMelhoria(Melhoria melhoria) {
        if (melhoria == null || melhoria.getCodigo() == null) {
            throw new IllegalArgumentException("Melhoria sem código não pode ser salva.");
        }
        SavePartidaData.Melhoria dados = new SavePartidaData.Melhoria();
        dados.codigo = melhoria.getCodigo();
        dados.coluna = melhoria.getColuna();
        return dados;
    }

    private static SavePartidaData.Cidade criarDadosCidade(Cidade cidade) {
        SavePartidaData.Cidade dados = new SavePartidaData.Cidade();
        dados.nome = cidade.getNomeCidade().name();
        dados.regiao = cidade.getRegiao().name();
        dados.tesouros = cidade.getQuantidadeTesouros();
        dados.conquistada = cidade.isConquistada();
        return dados;
    }

    private MapaDados copiarMapa(MapaDados origem) {
        MapaDados copia = new MapaDados();
        for (MapaDados.Ponto ponto : origem.getPontos().values()) {
            if (ponto.getId() == 0) {
                copia.mover(0, ponto.getX(), ponto.getY());
            } else {
                copia.adicionar(ponto.getId(), ponto.getX(), ponto.getY(), ponto.isDupla());
            }
            copia.alterarRaio(ponto.getId(), ponto.getRaio());
        }
        for (MapaDados.Ligacao ligacao : origem.getLigacoes()) {
            copia.conectar(ligacao.getOrigem(), ligacao.getDestino());
        }
        return copia;
    }

    public MapaDados getMapaDadosSave() {
        return mapaDados == null ? null : copiarMapa(mapaDados);
    }

    public void definirMapaDadosSave(MapaDados mapa) {
        if (mapa == null) {
            throw new IllegalArgumentException("Mapa obrigatório");
        }
        mapaDados = copiarMapa(mapa);
    }

    /** Construtor padrao usado pelos testes de integracao do Model. */
    public InterfacePartida() {
        this(new String[] {"Azul", "Vermelho"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO});
    }

    public InterfacePartida(int quantidadeJogadores) {
        this(criarNomesJogadores(quantidadeJogadores), criarCoresJogadores(quantidadeJogadores));
    }

    public InterfacePartida(String[] nomes, Cor[] cores) {
        this(nomes, cores, null, false);
    }

    /**
     * A ordem aleatoria e solicitada pela tela inicial. Os construtores
     * antigos permanecem deterministas para preservar cenarios de testes.
     */
    public InterfacePartida(String[] nomes, Cor[] cores,
                            Personagem[] personagens, boolean ordemAleatoria) {
        this(nomes, cores, personagens, ordemAleatoria, new MercadoMelhorias());
    }

    /** Construtor de apoio aos testes de integracao, com mercado reproduzivel. */
    InterfacePartida(String[] nomes, Cor[] cores,
                     Personagem[] personagens, boolean ordemAleatoria,
                     MercadoMelhorias mercado) {
        if (mercado == null) {
            throw new IllegalArgumentException("Mercado obrigatorio");
        }
        this.mercadoMelhorias = mercado;
        if (nomes == null || cores == null || nomes.length < 2
                || nomes.length > 5 || nomes.length != cores.length) {
            throw new IllegalArgumentException("A partida requer entre 2 e 5 nomes e cores");
        }
        if (personagens != null && personagens.length != nomes.length) {
            throw new IllegalArgumentException("A quantidade de personagens nao corresponde aos jogadores");
        }

        HashSet<Personagem> escolhidos = new HashSet<Personagem>();
        participantes = new ArrayList<Jogador>();
        for (int i = 0; i < nomes.length; i++) {
            Personagem personagem;
            if (personagens == null) {
                personagem = Personagem.values()[i];
            } else {
                personagem = personagens[i];
            }
            if (personagem == null || !escolhidos.add(personagem)) {
                throw new IllegalArgumentException("Os personagens devem ser unicos e validos");
            }
            participantes.add(new Jogador(nomes[i], cores[i], 0, personagem));
        }
        if (ordemAleatoria) {
            Collections.shuffle(participantes);
        }
        jogo = new Jogo(participantes);
        inicializarMecanicasCompletas();
    }

    private void inicializarMecanicasCompletas() {
        // 1. Registra regioes das cidades disponiveis e habilidades iniciais dos herdeiros
        for (Cidade c : jogo.getTabuleiro().getCidadesDisponiveis()) {
            if (c != null && c.getNomeCidade() != null && c.getRegiao() != null) {
                regiaoPorCidade.put(c.getNomeCidade(), c.getRegiao());
            }
        }
        for (Jogador j : participantes) {
            tesourosPorJogador.put(j, new ArrayList<TipoTesouro>());
            bonusPorJogador.put(j, new ArrayList<PecaBonus>());
            cidadesConquistadasPorJogador.put(j, 0);

            if (j.getPersonagem() == Personagem.ALTANI) {
                Peao segundoPeao = new Peao(j.getCor());
                jogo.getTabuleiro().posicionarPeaoInicial(segundoPeao);
                peaoExtraAltaniPorJogador.put(j, segundoPeao);
            } else if (j.getPersonagem() == Personagem.TOLUI) {
                jogo.getTabuleiro().posicionarYurtInicialTolui(j.getCor());
            }
        }

        // 2. Baralho de 40 tesouros (8 de cada um dos 5 tipos)
        for (TipoTesouro tipo : TipoTesouro.values()) {
            for (int i = 0; i < 8; i++) {
                pilhaTesouros.add(tipo);
            }
        }
        Collections.shuffle(pilhaTesouros);

        // 3. Embaralha as 7 pecas de bonus e cria o local de Karakorum (id 0)
        poolPecasBonus.addAll(Arrays.asList(PecaBonus.values()));
        Collections.shuffle(poolPecasBonus);

        List<TipoTesouro> tiposKarakorum = new ArrayList<TipoTesouro>(Arrays.asList(TipoTesouro.values()));
        Collections.shuffle(tiposKarakorum);
        TipoTesouro[] demandaKarakorum = {
            tiposKarakorum.get(0), tiposKarakorum.get(1), tiposKarakorum.get(2)
        };
        PecaBonus bonusKarakorum = poolPecasBonus.isEmpty() ? null : poolPecasBonus.remove(0);
        locaisConselheiros.add(new LocalConselheiro(0, "Karakorum", demandaKarakorum, 2, bonusKarakorum));

        // 4. Sorteia 4 cartas de Conselheiro Aberto
        List<CartaConselheiroAberto> todasAbertas =
                new ArrayList<CartaConselheiroAberto>(Arrays.asList(CartaConselheiroAberto.values()));
        Collections.shuffle(todasAbertas);
        for (int i = 0; i < 4 && i < todasAbertas.size(); i++) {
            cartasAbertasAtivas.add(todasAbertas.get(i));
        }

        // 5. Distribui 2 opcoes de Conselheiro Secreto para cada jogador
        List<CartaConselheiroSecreto> todasSecretas =
                new ArrayList<CartaConselheiroSecreto>(Arrays.asList(CartaConselheiroSecreto.values()));
        Collections.shuffle(todasSecretas);
        int cursor = 0;
        for (Jogador j : participantes) {
            List<CartaConselheiroSecreto> opcoes = new ArrayList<CartaConselheiroSecreto>();
            if (cursor + 1 < todasSecretas.size()) {
                opcoes.add(todasSecretas.get(cursor++));
                opcoes.add(todasSecretas.get(cursor++));
            }
            opcoesSecretasPorJogador.put(j, opcoes);
        }
    }

    private void registrarParadaDuplaConselheiro(int idParada) {
        for (LocalConselheiro existente : locaisConselheiros) {
            if (existente.getIdParada() == idParada) {
                return;
            }
        }
        int indiceDupla = locaisConselheiros.size() - 1; // descontando Karakorum
        if (indiceDupla >= 6) {
            return;
        }
        TipoTesouro[][] demandas = {
            {TipoTesouro.PELES, TipoTesouro.PELES},
            {TipoTesouro.SEDA, TipoTesouro.SEDA},
            {TipoTesouro.PORCELANA, TipoTesouro.PORCELANA},
            {TipoTesouro.FERRO, TipoTesouro.FERRO},
            {TipoTesouro.GADO, TipoTesouro.GADO},
            null // 6a parada dupla: 3 tesouros quaisquer
        };
        PecaBonus bonus = poolPecasBonus.isEmpty() ? null : poolPecasBonus.remove(0);
        locaisConselheiros.add(new LocalConselheiro(
                idParada, "Conselheiro (Parada " + idParada + ")", demandas[indiceDupla], 1, bonus));
    }

    private static String[] criarNomesJogadores(int quantidadeJogadores) {
        if (quantidadeJogadores < 2 || quantidadeJogadores > 5) {
            throw new IllegalArgumentException("A partida requer de 2 a 5 jogadores");
        }

        String[] nomes = new String[quantidadeJogadores];
        for (int i = 0; i < nomes.length; i++) {
            nomes[i] = "Jogador " + (i + 1);
        }
        return nomes;
    }

    private static Cor[] criarCoresJogadores(int quantidadeJogadores) {
        Cor[] coresDisponiveis = {Cor.AZUL, Cor.VERMELHO, Cor.AMARELO,
                Cor.VERDE, Cor.LARANJA};
        Cor[] cores = new Cor[quantidadeJogadores];
        System.arraycopy(coresDisponiveis, 0, cores, 0, quantidadeJogadores);
        return cores;
    }

    /** Carrega um mapa de uma só vez; não deixa um tabuleiro parcialmente configurado. */
    public boolean carregarMapa(MapaDados mapa) {
        if (mapa == null || jogo.getTabuleiro().getQuantidadeParadas() != 1) {
            return false;
        }
        for (MapaDados.Ponto ponto : mapa.getPontos().values()) {
            if (ponto.getId() != 0) {
                if (!jogo.getTabuleiro().adicionarParada(ponto.getId(), ponto.isDupla())) {
                    return false;
                }
                if (ponto.isDupla()) {
                    registrarParadaDuplaConselheiro(ponto.getId());
                }
            }
        }
        for (MapaDados.Ligacao ligacao : mapa.getLigacoes()) {
            if (!jogo.getTabuleiro().conectarParadas(ligacao.getOrigem(), ligacao.getDestino())) {
                return false;
            }
        }
        mapaDados = copiarMapa(mapa);
        registrarParadaAtualNoTurno();
        notificarObservadores();
        return true;
    }

    public boolean cadastrarParada(int id, boolean dupla) {
        boolean adicionado = jogo.getTabuleiro().adicionarParada(id, dupla);
        if (adicionado) {
            if (dupla && id != 0) {
                registrarParadaDuplaConselheiro(id);
            }
            notificarObservadores();
        }
        return adicionado;
    }

    public boolean conectarParadas(int origem, int destino) {
        boolean conectado = jogo.getTabuleiro().conectarParadas(origem, destino);
        if (conectado) {
            notificarObservadores();
        }
        return conectado;
    }

    /** Configura explicitamente a regiao de uma parada para contagem de influencia final. */
    public boolean configurarRegiaoParada(int idParada, Regiao regiao) {
        if (regiao == null || jogo.getTabuleiro().getOcupacao(idParada) < 0) {
            return false;
        }
        regiaoPorParada.put(idParada, regiao);
        return true;
    }

    private Peao obterPeaoAtivo(Jogador jogador) {
        if (jogador.getPersonagem() == Personagem.ALTANI
                && jogador == participantes.get(indiceAtual)
                && usandoPeaoExtraAltani
                && peaoExtraAltaniPorJogador.containsKey(jogador)) {
            return peaoExtraAltaniPorJogador.get(jogador);
        }
        return jogador.getMeuPeao();
    }

    /** Habilidade de Altani: alterna entre os 2 peoes enquanto nao executou acoes no turno. */
    public boolean alternarPeaoAtivoAltani() {
        if (partidaEncerrada) {
            return false;
        }
        Jogador atual = participantes.get(indiceAtual);
        if (atual.getPersonagem() != Personagem.ALTANI || acaoRealizadaNoTurno) {
            return false;
        }
        usandoPeaoExtraAltani = !usandoPeaoExtraAltani;
        paradasVisitadasTurno.clear();
        registrarParadaAtualNoTurno();
        notificarObservadores();
        return true;
    }

    public boolean movimentarJogador(int indice, int destino) {
        if (indice < 0 || indice >= participantes.size()) {
            return false;
        }
        Jogador alvo = participantes.get(indice);
        boolean realizado = jogo.getTabuleiro().moverPeao(obterPeaoAtivo(alvo), destino);
        if (realizado) {
            notificarObservadores();
        }
        return realizado;
    }

    /** Consulta o custo minimo do destino para o jogador da vez, sem movimentar. */
    public int consultarCustoMovimentoAtual(int destino) {
        Jogador jogador = participantes.get(indiceAtual);
        return jogo.getTabuleiro().distanciaMinima(
                obterPeaoAtivo(jogador), destino, jogador.getCor());
    }

    /** Jogador ativo da rodada visual. */
    public int getIndiceJogadorAtual() {
        return indiceAtual;
    }

    public boolean movimentarJogadorAtual(int destino, int movimentosDisponiveis) {
        Jogador jogador = participantes.get(indiceAtual);
        Peao peao = obterPeaoAtivo(jogador);
        List<Integer> percurso = jogo.getTabuleiro().consultarCaminho(peao, destino, jogador.getCor());
        boolean realizado = jogo.getTabuleiro().moverPeaoAte(
                peao, destino, movimentosDisponiveis, jogador.getCor());
        if (realizado) {
            paradasVisitadasTurno.addAll(percurso);
            notificarObservadores();
        }
        return realizado;
    }

    /** Ativacao oficial da cartela: uma coluna por turno, sem repetir ocupadas. */
    public boolean ativarColunaJogadorAtual(int coluna) {
        if (partidaEncerrada) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        boolean ativada = jogador.getTabuleiroIndividual().ativarColuna(
                coluna, jogador.getPersonagem());
        if (ativada) {
            notificarObservadores();
        }
        return ativada;
    }

    public ResumoAtivacao consultarAtivacaoJogadorAtual() {
        Jogador jogador = participantes.get(indiceAtual);
        return jogador.getTabuleiroIndividual().consultarAtivacao(jogador.getPersonagem());
    }

    /** Movimentacao cujo saldo e calculado e debitado no proprio Model (inclui bonus). */
    public boolean movimentarJogadorComAtivacao(int destino) {
        if (partidaEncerrada) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
        ResumoAtivacao ativacao = cartela.consultarAtivacao(jogador.getPersonagem());
        int movimentosTotais = ativacao.getMovimentosRestantes() + bonusMovimentosTurno;
        int custo = consultarCustoMovimentoAtual(destino);

        if (custo <= 0 || custo > movimentosTotais) {
            return false;
        }
        Peao peao = obterPeaoAtivo(jogador);
        List<Integer> percurso = jogo.getTabuleiro().consultarCaminho(
                peao, destino, jogador.getCor());
        boolean realizado = jogo.getTabuleiro().moverPeaoAte(
                peao, destino, movimentosTotais, jogador.getCor());
        if (!realizado) {
            return false;
        }

        paradasVisitadasTurno.addAll(percurso);
        int usadoDoBonus = Math.min(custo, bonusMovimentosTurno);
        bonusMovimentosTurno -= usadoDoBonus;
        int restanteCartela = custo - usadoDoBonus;
        if (restanteCartela > 0) {
            cartela.consumirMovimentos(restanteCartela);
        }
        notificarObservadores();
        return true;
    }

    /** Encerra o turno somente apos a acao obrigatoria do Khan, quando houver. */
    public boolean tentarAvancarJogador() {
        if (partidaEncerrada) {
            return false;
        }
        TabuleiroIndividual cartela = participantes.get(indiceAtual).getTabuleiroIndividual();
        if (cartela.getLinhasBrancasPendentes() > 0) {
            return false;
        }
        if (cartela.deveMoverKhan()) {
            return false;
        }
        cartela.encerrarTurno();
        paradasVisitadasTurno.clear();
        saquesNaCidadeNoTurno.clear();
        usandoPeaoExtraAltani = false;
        acaoRealizadaNoTurno = false;
        bonusMovimentosTurno = 0;
        bonusPunhosTurno = 0;

        indiceAtual++;
        if (indiceAtual >= participantes.size()) {
            indiceAtual = 0;
            rodadasConcluidas++;
            if (rodadasNoDisparoFim >= 0
                    && rodadasConcluidas >= rodadasNoDisparoFim + 2) {
                partidaEncerrada = true;
            }
        }
        if (!partidaEncerrada) {
            registrarParadaAtualNoTurno();
        }
        notificarObservadores();
        return true;
    }

    /** Mantem a assinatura antiga para as telas e testes anteriores. */
    public void avancarJogador() {
        tentarAvancarJogador();
    }

    public int getParadaJogador(int indice) {
        if (indice < 0 || indice >= participantes.size()) {
            return -1;
        }
        Parada parada = obterPeaoAtivo(participantes.get(indice)).getParadaAtual();
        if (parada == null) {
            return -1;
        }
        return parada.getId();
    }

    public int getQuantidadeJogadores() {
        return participantes.size();
    }

    public String getNomeJogador(int indice) {
        return participantes.get(indice).getNome();
    }

    public Personagem getPersonagemJogador(int indice) {
        if (indice < 0 || indice >= participantes.size()) {
            return null;
        }
        return participantes.get(indice).getPersonagem();
    }

    public int getIndiceCartelaJogador(int indice) {
        Personagem personagem = getPersonagemJogador(indice);
        if (personagem == null) {
            return -1;
        }
        return personagem.getIndiceCartela();
    }

    public Cor getCorJogador(int indice) {
        if (indice < 0 || indice >= participantes.size()) {
            return null;
        }
        return participantes.get(indice).getCor();
    }

    @Override
    public void registrarObservador(ObservadorPartida observador) {
        if (observador == null) {
            throw new IllegalArgumentException("Observador obrigatorio");
        }
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public void removerObservador(ObservadorPartida observador) {
        observadores.remove(observador);
    }

    /** Notifica uma copia da lista para permitir remocoes durante a notificacao. */
    private void notificarObservadores() {
        List<ObservadorPartida> copia = new ArrayList<>(observadores);
        for (ObservadorPartida observador : copia) {
            observador.atualizar(this);
        }
    }

    /**
     * Devolve um retrato dos recursos da cartela do participante solicitado.
     * A View nunca recebe as listas internas de TabuleiroIndividual.
     */
    public ResumoCartela consultarCartela(int indiceJogador) {
        if (indiceJogador < 0 || indiceJogador >= participantes.size()) {
            throw new IllegalArgumentException("Indice de jogador invalido");
        }

        Jogador jogador = participantes.get(indiceJogador);
        TabuleiroIndividual tabuleiro = jogador.getTabuleiroIndividual();
        List<String> descricoes = new ArrayList<String>();
        List<ResumoMelhoria> melhoriasDetalhadas = new ArrayList<ResumoMelhoria>();

        for (Melhoria melhoria : tabuleiro.getMelhoriasAdquiridas()) {
            if (melhoria != null) {
                descricoes.add(melhoria.getDescdescricao());
                melhoriasDetalhadas.add(new ResumoMelhoria(
                        melhoria.getCodigo(),
                        melhoria.getDescdescricao(),
                        melhoria.getColuna()));
            }
        }

        return new ResumoCartela(
                jogador.getNome(),
                jogador.getCor(),
                indiceJogador,
                tabuleiro.getQuantidadeColunas(),
                jogador.quantidadeTributo(TipoTributo.ESPADA),
                jogador.quantidadeTributo(TipoTributo.MOEDA),
                jogador.quantidadeTributo(TipoTributo.YURT),
                tabuleiro.getQuantidadeEspadasVirtuais(),
                tabuleiro.getQuantidadeMoedasVirtuais(),
                tabuleiro.getQuantidadeYurtsVirtuais(),
                descricoes,
                melhoriasDetalhadas
        );
    }

    public ResumoCartela consultarCartelaJogadorAtual() {
        return consultarCartela(indiceAtual);
    }

    public int consultarVotosJogadorAtual() {
        return participantes.get(indiceAtual).getVotosKurultai();
    }

    public int consultarVotosJogador(int indiceJogador) {
        if (indiceJogador < 0 || indiceJogador >= participantes.size()) {
            return 0;
        }
        return participantes.get(indiceJogador).getVotosKurultai();
    }

    public int consultarPontuacaoKhan() {
        return pontuacaoKhan;
    }

    public List<String> consultarNomesVencedores() {
        List<String> nomes = new ArrayList<String>();
        for (Jogador jogador : jogo.jogadoresComMaisVotos()) {
            nomes.add(jogador.getNome());
        }
        Collections.sort(nomes, String.CASE_INSENSITIVE_ORDER);
        return Collections.unmodifiableList(nomes);
    }

    /** A ficha branca avanca sempre que qualquer jogador ganha votos. */
    public boolean registrarVotosConquistados(int quantidade) {
        if (quantidade <= 0 || partidaEncerrada) {
            return false;
        }
        concederVotosAoJogador(participantes.get(indiceAtual), quantidade);
        notificarObservadores();
        return true;
    }

    private void concederVotosAoJogador(Jogador jogador, int quantidade) {
        if (jogador == null || quantidade <= 0) {
            return;
        }
        jogador.ganharVotos(quantidade);
        pontuacaoKhan += quantidade;
        if (rodadasNoDisparoFim < 0
                && pontuacaoKhan >= consultarCasaFinal()) {
            rodadasNoDisparoFim = rodadasConcluidas;
        }
    }

    public int consultarCasaFinal() {
        if (participantes.size() == 2) {
            return 9;
        }
        if (participantes.size() == 3) {
            return 13;
        }
        if (participantes.size() == 4) {
            return 15;
        }
        return 16;
    }

    public boolean isFimDisparado() {
        return rodadasNoDisparoFim >= 0;
    }

    public boolean isPartidaEncerrada() {
        return partidaEncerrada;
    }

    public int consultarQuantidadeYurtsNaParada(int idParada) {
        return jogo.getTabuleiro().getQuantidadeYurts(idParada);
    }

    public List<Cor> consultarDonosYurtNaParada(int idParada) {
        return jogo.getTabuleiro().getDonosYurt(idParada);
    }

    public boolean construirYurtAtual(int idParada) {
        if (partidaEncerrada) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        boolean alcanceValido = paradasVisitadasTurno.contains(idParada);
        if (!alcanceValido && jogador.getPersonagem() == Personagem.TOLUI) {
            for (Integer visitada : paradasVisitadasTurno) {
                if (jogo.getTabuleiro().saoAdjacentes(visitada, idParada)) {
                    alcanceValido = true;
                    break;
                }
            }
        }
        if (!alcanceValido) {
            return false;
        }
        TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
        int yurtsVirtuais = cartela.getQuantidadeYurtsVirtuais();
        int yurtsDisponiveis = jogador.quantidadeTributo(TipoTributo.YURT) + yurtsVirtuais;
        if (yurtsDisponiveis < 1) {
            return false;
        }
        if (!jogo.getTabuleiro().construirYurt(idParada, jogador.getCor())) {
            return false;
        }
        int yurtsReaisNecessarios = 1 - yurtsVirtuais;
        if (yurtsReaisNecessarios < 0) {
            yurtsReaisNecessarios = 0;
        }
        if (yurtsReaisNecessarios > 0) {
            jogador.getTabuleiroIndividual().removerTributosPorTipo(TipoTributo.YURT,
                    yurtsReaisNecessarios);
        }
        acaoRealizadaNoTurno = true;
        verificarCartasConselheiroAberto(jogador);
        notificarObservadores();
        return true;
    }

    /**
     * Recurso restrito a demonstracao e testes da interface.
     * Nao implementa uma regra de ganho de tributos durante uma jogada.
     */
    public boolean simularRecebimentoTributo(int indiceJogador, String tipo, int quantidade) {
        if (indiceJogador < 0 || indiceJogador >= participantes.size()
         || tipo == null || quantidade <= 0) {
            return false;
        }

        TipoTributo tipoTributo;
        try {
            tipoTributo = TipoTributo.valueOf(tipo.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException erro) {
            return false;
        }

        boolean recebido = participantes.get(indiceJogador)
                .receberTributos(tipoTributo, quantidade);
        if (recebido) {
            verificarCartasConselheiroAberto(participantes.get(indiceJogador));
            notificarObservadores();
        }
        return recebido;
    }

    /**
     * Consulta o estado das cidades abertas, sem expor objetos Cidade.
     * Uma nova consulta deve ser feita apos uma notificacao do Observer.
     */
    public List<ResumoCidade> consultarCidadesDisponiveis() {
        List<ResumoCidade> resumos = new ArrayList<ResumoCidade>();
        List<Cidade> cidades = jogo.getTabuleiro().getCidadesDisponiveis();

        for (Cidade cidade : cidades) {
            if (cidade.getNomeCidade() != null && cidade.getRegiao() != null) {
                regiaoPorCidade.put(cidade.getNomeCidade(), cidade.getRegiao());
            }
            ResumoCidade resumo = new ResumoCidade(
                    cidade.getNome(),
                    cidade.getRegiao().name(),
                    cidade.getQuantidadeTesouros(),
                    cidade.isConquistada()
            );
            resumos.add(resumo);
        }
        return java.util.Collections.unmodifiableList(resumos);
    }

    /** Associa uma cidade do mapa a uma parada usada para atacá-la. */
    public boolean configurarAcessoCidade(String nomeCidade, int idParada) {
        NomeCidade nome = interpretarCidade(nomeCidade);
        if (nome == null || idParada < 0 || jogo.getTabuleiro().getOcupacao(idParada) < 0) {
            return false;
        }
        String chave = nome.name();
        if (!acessosCidades.containsKey(chave)) {
            acessosCidades.put(chave, new HashSet<Integer>());
        }
        if (!paradasPorCidade.containsKey(nome)) {
            paradasPorCidade.put(nome, new HashSet<Integer>());
        }
        paradasPorCidade.get(nome).add(idParada);
        return acessosCidades.get(chave).add(idParada);
    }

    private NomeCidade interpretarCidade(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = texto.trim().toUpperCase(Locale.ROOT);
        for (NomeCidade cidade : NomeCidade.values()) {
            if (cidade.name().equals(normalizado)
                    || cidade.getNomeFormatado().toUpperCase(Locale.ROOT).equals(normalizado)) {
                return cidade;
            }
        }
        return null;
    }

    /** A cidade deve estar aberta e ter sido alcancada por uma parada neste turno. */
    public boolean podeSaquearCidadeAtual(String nomeCidade) {
        NomeCidade nome = interpretarCidade(nomeCidade);
        if (nome == null) {
            return false;
        }
        Cidade cidade = encontrarCidadeDisponivel(nome);
        if (cidade == null || cidade.isConquistada() || cidade.getQuantidadeTesouros() <= 0) {
            return false;
        }
        Set<Integer> acessos = acessosCidades.get(nome.name());
        if (acessos == null) {
            return false;
        }
        boolean passouPerto = false;
        for (Integer parada : acessos) {
            if (paradasVisitadasTurno.contains(parada)) {
                passouPerto = true;
            }
        }
        if (!passouPerto) {
            return false;
        }
        int anteriores = 0;
        if (saquesNaCidadeNoTurno.containsKey(nome.name())) {
            anteriores = saquesNaCidadeNoTurno.get(nome.name());
        }
        int custo = (anteriores > 0) ? 2 : 1;
        Jogador jogadorAtual = participantes.get(indiceAtual);
        int espadas = jogadorAtual.quantidadeTributo(TipoTributo.ESPADA)
                + jogadorAtual.getTabuleiroIndividual().getQuantidadeEspadasVirtuais();
        if (espadas >= custo) {
            return true;
        }
        // Habilidade de Ogedei: pode gastar 1 moeda no lugar de espada no primeiro saque
        if (anteriores == 0 && jogadorAtual.getPersonagem() == Personagem.OGEDEI) {
            int moedas = jogadorAtual.quantidadeTributo(TipoTributo.MOEDA)
                    + jogadorAtual.getTabuleiroIndividual().getQuantidadeMoedasVirtuais();
            return moedas >= 1;
        }
        return false;
    }

    private Cidade encontrarCidadeDisponivel(NomeCidade nome) {
        for (Cidade cidade : jogo.getTabuleiro().getCidadesDisponiveis()) {
            if (cidade.getNomeCidade() == nome) {
                return cidade;
            }
        }
        return null;
    }

    /** Saque completo: valida visita, gasta espadas/moedas (Ogedei), entrega tesouro e revela nova cidade. */
    public boolean saquearCidadeAtual(String nomeCidade) {
        if (partidaEncerrada || !podeSaquearCidadeAtual(nomeCidade)) {
            return false;
        }
        NomeCidade nome = interpretarCidade(nomeCidade);
        Cidade cidade = encontrarCidadeDisponivel(nome);
        if (cidade.getRegiao() != null) {
            regiaoPorCidade.put(nome, cidade.getRegiao());
        }
        String chave = nome.name();
        int anteriores = 0;
        if (saquesNaCidadeNoTurno.containsKey(chave)) {
            anteriores = saquesNaCidadeNoTurno.get(chave);
        }
        Jogador jogadorAtual = participantes.get(indiceAtual);
        boolean saqueOk = jogadorAtual.saquearCidade(cidade, anteriores);
        if (!saqueOk && anteriores == 0 && jogadorAtual.getPersonagem() == Personagem.OGEDEI) {
            int moedasVirtuais = jogadorAtual.getTabuleiroIndividual().getQuantidadeMoedasVirtuais();
            int moedasReais = Math.max(0, 1 - moedasVirtuais);
            if (moedasReais == 0
                    || jogadorAtual.getTabuleiroIndividual().removerTributosPorTipo(TipoTributo.MOEDA, moedasReais)) {
                saqueOk = cidade.saquearTesouro();
            }
        }
        if (!saqueOk) {
            return false;
        }

        acaoRealizadaNoTurno = true;
        saquesNaCidadeNoTurno.put(chave, anteriores + 1);
        TipoTesouro tesouroSaqueado = comprarTesouroDaPilha();
        if (tesouroSaqueado != null) {
            tesourosPorJogador.get(jogadorAtual).add(tesouroSaqueado);
        }

        if (cidade.isConquistada()) {
            cidadesConquistadas.put(chave, jogadorAtual.getCor());
            jogo.getTabuleiro().registrarYurtConquista(nome, jogadorAtual.getCor());
            cidadesConquistadasPorJogador.put(
                    jogadorAtual, cidadesConquistadasPorJogador.get(jogadorAtual) + 1);

            Cidade proxima = jogo.revelarProximaCidade();
            if (proxima != null && proxima.getNomeCidade() != null && proxima.getRegiao() != null) {
                regiaoPorCidade.put(proxima.getNomeCidade(), proxima.getRegiao());
            }
            jogo.getTabuleiro().substituirCidadeConquistada(cidade, proxima);

            if (rodadasNoDisparoFim < 0 && cidadesConquistadas.size() >= 9) {
                rodadasNoDisparoFim = rodadasConcluidas;
            }
        }
        verificarCartasConselheiroAberto(jogadorAtual);
        notificarObservadores();
        return true;
    }

    private TipoTesouro comprarTesouroDaPilha() {
        if (pilhaTesouros.isEmpty() && !descarteTesouros.isEmpty()) {
            pilhaTesouros.addAll(descarteTesouros);
            descarteTesouros.clear();
            Collections.shuffle(pilhaTesouros);
        }
        if (pilhaTesouros.isEmpty()) {
            return null;
        }
        return pilhaTesouros.remove(0);
    }

    public Cor consultarConquistadorCidade(String nomeCidade) {
        NomeCidade nome = interpretarCidade(nomeCidade);
        if (nome == null) {
            return null;
        }
        return cidadesConquistadas.get(nome.name());
    }

    public boolean deveMoverKhanJogadorAtual() {
        return participantes.get(indiceAtual).getTabuleiroIndividual().deveMoverKhan();
    }

    public DestinoKhan consultarDestinoKhan() {
        return jogo.getTabuleiro().getDestinoKhan();
    }

    public boolean coletarTributo(Provincia provincia, String tipoEscolhido) {
        if (provincia == null || tipoEscolhido == null) {
            return false;
        }
        TipoTributo tipo;
        try {
            tipo = TipoTributo.valueOf(tipoEscolhido.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException erro) {
            return false;
        }
        return coletarTributoDaReserva(provincia, tipo);
    }

    public int consultarQuantidadeTributoProvincia(Provincia provincia, String tipoConsultado) {
        if (provincia == null || tipoConsultado == null) {
            return 0;
        }
        ReservaProvincia reserva = reservas.get(provincia);
        if (reserva == null) {
            return 0;
        }
        try {
            TipoTributo tipo = TipoTributo.valueOf(tipoConsultado.trim().toUpperCase(Locale.ROOT));
            return reserva.quantidade(tipo);
        } catch (IllegalArgumentException erro) {
            return 0;
        }
    }

    public boolean coletarTributoDaPilha(Provincia[] provincias, String tipoEscolhido) {
        if (partidaEncerrada || provincias == null || provincias.length == 0 || tipoEscolhido == null) {
            return false;
        }

        TipoTributo tipo;
        try {
            tipo = TipoTributo.valueOf(tipoEscolhido.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException erro) {
            return false;
        }

        boolean temAcesso = false;
        ReservaProvincia reservaComEstoque = null;
        Set<Provincia> examinadas = new HashSet<Provincia>();

        for (Provincia provincia : provincias) {
            if (provincia == null || !examinadas.add(provincia)) {
                return false;
            }

            ReservaProvincia reserva = reservas.get(provincia);
            if (reserva == null) {
                continue;
            }

            if (reserva.acessivelPelas(paradasVisitadasTurno)) {
                temAcesso = true;
            }

            if (reservaComEstoque == null && reserva.quantidade(tipo) > 0) {
                reservaComEstoque = reserva;
            }
        }

        if (!temAcesso || reservaComEstoque == null) {
            return false;
        }

        Jogador jogador = participantes.get(indiceAtual);
        TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
        if (!cartela.podeGastarPunho() && bonusPunhosTurno <= 0) {
            return false;
        }

        if (!reservaComEstoque.retirar(tipo)) {
            return false;
        }

        jogador.receberTributos(tipo, 1);
        if (bonusPunhosTurno > 0) {
            bonusPunhosTurno--;
        } else {
            cartela.gastarPunho();
        }
        verificarCartasConselheiroAberto(jogador);
        notificarObservadores();
        return true;
    }

    /** Compatibilidade: coleta do tipo original configurado na provincia. */
    public boolean coletarTributo(Provincia provincia) {
        if (provincia == null) {
            return false;
        }
        ReservaProvincia reserva = reservas.get(provincia);
        if (reserva == null) {
            return false;
        }
        return coletarTributoDaReserva(provincia, reserva.getTipo());
    }

    private boolean coletarTributoDaReserva(Provincia provincia, TipoTributo tipo) {
        if (partidaEncerrada) {
            return false;
        }
        ReservaProvincia reserva = reservas.get(provincia);
        if (reserva == null || !provinciaAcessivelNoTurno(reserva)) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
        if ((!cartela.podeGastarPunho() && bonusPunhosTurno <= 0) || reserva.quantidade(tipo) <= 0) {
            return false;
        }
        if (!reserva.retirar(tipo)) {
            return false;
        }
        jogador.receberTributos(tipo, 1);
        if (bonusPunhosTurno > 0) {
            bonusPunhosTurno--;
        } else {
            cartela.gastarPunho();
        }
        verificarCartasConselheiroAberto(jogador);
        notificarObservadores();
        return true;
    }

    /** Cadastra uma província e seu estoque; o CSV guarda os acessos ao mapa. */
    public boolean configurarProvincia(Provincia provincia, String tipo, int quantidade) {
        if (provincia == null || tipo == null || reservas.containsKey(provincia)) {
            return false;
        }
        TipoTributo tributo;
        try {
            tributo = TipoTributo.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException erro) {
            return false;
        }
        if (quantidade < 0 || quantidade > 3) {
            return false;
        }
        reservas.put(provincia, new ReservaProvincia(tributo, quantidade));
        return true;
    }

    /** Acrescenta outro tipo ao estoque, respeitando o limite total de 3 pecas. */
    public boolean configurarTributoAdicional(Provincia provincia, String tipo, int quantidade) {
        ReservaProvincia reserva = reservas.get(provincia);
        if (reserva == null || tipo == null) {
            return false;
        }
        try {
            return reserva.adicionarTipo(TipoTributo.valueOf(tipo.trim().toUpperCase(Locale.ROOT)),
                    quantidade);
        } catch (IllegalArgumentException erro) {
            return false;
        }
    }

    public boolean configurarAcessoProvincia(Provincia provincia, int idParada) {
        ReservaProvincia reserva = reservas.get(provincia);
        if (reserva == null || jogo.getTabuleiro().getOcupacao(idParada) < 0) {
            return false;
        }
        try {
            DestinoKhan destino = DestinoKhan.valueOf(provincia.name());
            if (!paradasPorProvinciaKhan.containsKey(destino)) {
                paradasPorProvinciaKhan.put(destino, new HashSet<Integer>());
            }
            paradasPorProvinciaKhan.get(destino).add(idParada);
        } catch (IllegalArgumentException ignored) {
        }
        return reserva.adicionarAcesso(idParada);
    }

    /** Vizinhança específica do tabuleiro configurada no CSV, sem valores fixos na View. */
    public boolean configurarVizinhasKhan(Provincia origem, Provincia primeira, Provincia segunda) {
        if (origem == null || primeira == null || segunda == null ||
                origem == primeira || origem == segunda || primeira == segunda ||
                vizinhasDoKhan.containsKey(origem)) {
            return false;
        }
        vizinhasDoKhan.put(origem, new Provincia[] {primeira, segunda});
        return true;
    }

    public int consultarEstoqueProvincia(Provincia provincia) {
        ReservaProvincia reserva = reservas.get(provincia);
        if (reserva == null) {
            return -1;
        }
        return reserva.total();
    }

    public String consultarTipoProvincia(Provincia provincia) {
        ReservaProvincia reserva = reservas.get(provincia);
        if (reserva == null) {
            return "Não configurada";
        }
        return reserva.getTipo().name();
    }

    private void registrarParadaAtualNoTurno() {
        int parada = getParadaJogador(indiceAtual);
        if (parada >= 0) {
            paradasVisitadasTurno.add(parada);
        }
    }

    /** Apenas as paradas efetivamente cadastradas podem dar acesso ao estoque. */
    private boolean provinciaAcessivelNoTurno(ReservaProvincia reserva) {
        if (reserva == null) {
            return false;
        }
        return reserva.acessivelPelas(paradasVisitadasTurno);
    }

    /** Consulta as quatro posicoes do mercado, sem expor cartas internas. */
    public List<ResumoMelhoria> consultarMercadoMelhorias() {
        return mercadoMelhorias.consultarResumos();
    }

    /** Compra uma melhoria real do mercado por duas moedas. */
    public boolean comprarMelhoriaAtual(int posicao) {
        Melhoria melhoria = mercadoMelhorias.consultar(posicao);
        if (melhoria == null || partidaEncerrada) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
        if (!cartela.podeAdicionarMelhoria(melhoria.getColuna())) {
            return false;
        }
        int custo = melhoria.getCustoEmMoedas();
        int moedasVirtuais = cartela.getQuantidadeMoedasVirtuais();
        int moedasDisponiveis = jogador.quantidadeTributo(TipoTributo.MOEDA) + moedasVirtuais;
        if (moedasDisponiveis < custo) {
            return false;
        }
        int moedasReaisNecessarias = custo - moedasVirtuais;
        if (moedasReaisNecessarias < 0) {
            moedasReaisNecessarias = 0;
        }
        if (moedasReaisNecessarias > 0
                && !cartela.removerTributosPorTipo(TipoTributo.MOEDA, moedasReaisNecessarias)) {
            return false;
        }
        Melhoria retirada = mercadoMelhorias.retirar(posicao);
        cartela.adicionarMelhoria(retirada);
        verificarCartasConselheiroAberto(jogador);
        notificarObservadores();
        return true;
    }

    public int consultarMelhoriasBrancasPendentes() {
        return participantes.get(indiceAtual).getTabuleiroIndividual()
                .getLinhasBrancasPendentes();
    }

    /** Resgata uma carta revelada gratuitamente para a coluna branca. */
    public boolean escolherMelhoriaBrancaDoMercado(int posicao) {
        TabuleiroIndividual cartela = participantes.get(indiceAtual).getTabuleiroIndividual();
        if (!cartela.podeReceberMelhoriaBranca() || mercadoMelhorias.consultar(posicao) == null
                || partidaEncerrada) {
            return false;
        }
        Melhoria retirada = mercadoMelhorias.retirar(posicao);
        boolean recebida = cartela.receberMelhoriaBranca(retirada);
        if (recebida) {
            verificarCartasConselheiroAberto(participantes.get(indiceAtual));
            notificarObservadores();
        }
        return recebida;
    }

    /** Resgata a melhoria do topo da pilha; a cor nao limita a coluna branca. */
    public boolean escolherMelhoriaBrancaAleatoria() {
        TabuleiroIndividual cartela = participantes.get(indiceAtual).getTabuleiroIndividual();
        if (!cartela.podeReceberMelhoriaBranca() || !mercadoMelhorias.possuiPilha()
                || partidaEncerrada) {
            return false;
        }
        Melhoria retirada = mercadoMelhorias.retirarAleatoria();
        boolean recebida = cartela.receberMelhoriaBranca(retirada);
        if (recebida) {
            verificarCartasConselheiroAberto(participantes.get(indiceAtual));
            notificarObservadores();
        }
        return recebida;
    }

    /** Resolve a movimentação obrigatória do Khan nas províncias ou melhorias. */
    public boolean realizarAcaoKhan(DestinoKhan destino) {
        if (partidaEncerrada) {
            return false;
        }
        TabuleiroIndividual cartela = participantes.get(indiceAtual).getTabuleiroIndividual();
        if (!cartela.deveMoverKhan() || destino == null) {
            return false;
        }
        if (!jogo.getTabuleiro().deslocarKhan(destino)) {
            return false;
        }
        if (destino.ehProvincia()) {
            Provincia escolhida = Provincia.valueOf(destino.name());
            reporTributo(escolhida);
            Provincia[] adjacentes = vizinhasDoKhan.get(escolhida);
            if (adjacentes == null) {
                adjacentes = escolhida.vizinhas();
            }
            for (Provincia vizinha : adjacentes) {
                reporTributo(vizinha);
            }
        }
        if (destino == DestinoKhan.MELHORIAS) {
            mercadoMelhorias.reporVazios();
            participantes.get(indiceAtual).receberTributos(TipoTributo.MOEDA, 1);
            participantes.get(indiceAtual).receberTributos(TipoTributo.ESPADA, 1);
        }
        cartela.concluirAcaoKhan();
        verificarCartasConselheiroAberto(participantes.get(indiceAtual));
        notificarObservadores();
        return true;
    }

    private void reporTributo(Provincia provincia) {
        ReservaProvincia reserva = reservas.get(provincia);
        if (reserva != null) {
            reserva.reporUma();
        }
    }

    public int getOcupacao(int id) {
        return jogo.getTabuleiro().getOcupacao(id);
    }

    // =========================================================================
    // METODOS DE CONSELHEIROS, BONUS, OBJETIVOS E APURACAO FINAL (FACADE)
    // =========================================================================

    public boolean escolherConselheiroSecretoJogadorAtual(int indiceOpcao) {
        if (partidaEncerrada) {
            return false;
        }
        Jogador atual = participantes.get(indiceAtual);
        if (secretaPorJogador.containsKey(atual)) {
            return false;
        }
        List<CartaConselheiroSecreto> opcoes = opcoesSecretasPorJogador.get(atual);
        if (opcoes == null || indiceOpcao < 0 || indiceOpcao >= opcoes.size()) {
            return false;
        }
        secretaPorJogador.put(atual, opcoes.get(indiceOpcao));
        opcoes.clear();
        notificarObservadores();
        return true;
    }

    public boolean entregarTesourosConselheiro(int idParadaConselheiro) {
        if (partidaEncerrada) {
            return false;
        }
        Jogador atual = participantes.get(indiceAtual);
        Parada paradaAtual = obterPeaoAtivo(atual).getParadaAtual();
        if (paradaAtual == null || paradaAtual.getId() != idParadaConselheiro) {
            return false;
        }

        LocalConselheiro alvo = null;
        for (LocalConselheiro local : locaisConselheiros) {
            if (local.getIdParada() == idParadaConselheiro) {
                alvo = local;
                break;
            }
        }
        if (alvo == null) {
            return false;
        }

        List<TipoTesouro> mao = tesourosPorJogador.get(atual);
        if (mao == null || !alvo.podeEntregar(mao, null, false)) {
            return false;
        }

        alvo.consumirTesouros(mao, null, false, descarteTesouros);
        acaoRealizadaNoTurno = true;
        concederVotosAoJogador(atual, alvo.getVotosConcedidos());

        if (alvo.temPecaBonus()) {
            bonusPorJogador.get(atual).add(alvo.recolherPecaBonus());
        }

        verificarCartasConselheiroAberto(atual);
        notificarObservadores();
        return true;
    }

    public boolean usarPecaBonusJogadorAtual(int indiceBonus) {
        if (partidaEncerrada) {
            return false;
        }
        Jogador atual = participantes.get(indiceAtual);
        List<PecaBonus> lista = bonusPorJogador.get(atual);
        if (lista == null || indiceBonus < 0 || indiceBonus >= lista.size()) {
            return false;
        }

        PecaBonus escolhida = lista.remove(indiceBonus);
        switch (escolhida) {
            case VOTO_IMEDIATO:
                concederVotosAoJogador(atual, 1);
                break;
            case DUAS_ESPADAS:
                atual.receberTributos(TipoTributo.ESPADA, 2);
                break;
            case DUAS_MOEDAS:
                atual.receberTributos(TipoTributo.MOEDA, 2);
                break;
            case DOIS_YURTS:
                atual.receberTributos(TipoTributo.YURT, 2);
                break;
            case TRES_MOVIMENTOS_UM_PUNHO:
                bonusMovimentosTurno += 3;
                bonusPunhosTurno += 1;
                break;
            case DOIS_PUNHOS:
                bonusPunhosTurno += 2;
                break;
            case ESCOLHER_TESOURO: {
                TipoTesouro comprado = comprarTesouroDaPilha();
                if (comprado != null) {
                    tesourosPorJogador.get(atual).add(comprado);
                }
                break;
            }
            default:
                break;
        }

        verificarCartasConselheiroAberto(atual);
        notificarObservadores();
        return true;
    }

    public List<ResumoConselheiro> consultarConselheiros() {
        Jogador atual = participantes.get(indiceAtual);
        Parada paradaAtual = obterPeaoAtivo(atual).getParadaAtual();
        int idParadaAtual = (paradaAtual != null) ? paradaAtual.getId() : -1;
        List<TipoTesouro> mao = tesourosPorJogador.get(atual);

        List<ResumoConselheiro> resultado = new ArrayList<ResumoConselheiro>();
        for (LocalConselheiro local : locaisConselheiros) {
            boolean presente = (idParadaAtual == local.getIdParada());
            boolean podeEntregar = presente && local.podeEntregar(mao, null, false);
            String descBonus = local.temPecaBonus()
                    ? local.getPecaBonus().getDescricao()
                    : "Ja recolhido";

            resultado.add(new ResumoConselheiro(
                    local.getIdParada(),
                    local.getNome(),
                    local.getDescricaoDemanda(),
                    local.getVotosConcedidos(),
                    descBonus,
                    presente,
                    podeEntregar));
        }
        return Collections.unmodifiableList(resultado);
    }

    public ResumoCartaObjetivo consultarObjetivosJogadorAtual() {
        Jogador atual = participantes.get(indiceAtual);
        List<String> abertas = new ArrayList<String>();
        for (CartaConselheiroAberto c : cartasAbertasAtivas) {
            abertas.add(c.getDescricao());
        }

        CartaConselheiroSecreto escolhida = secretaPorJogador.get(atual);
        String descSecreta = escolhida != null ? escolhida.getDescricao() : "Ainda nao escolhida";

        List<String> opcoesDesc = new ArrayList<String>();
        List<CartaConselheiroSecreto> opcoes = opcoesSecretasPorJogador.get(atual);
        if (escolhida == null && opcoes != null) {
            for (CartaConselheiroSecreto op : opcoes) {
                opcoesDesc.add(op.getDescricao());
            }
        }

        List<String> bonusDesc = new ArrayList<String>();
        for (PecaBonus b : bonusPorJogador.get(atual)) {
            bonusDesc.add(b.getDescricao());
        }

        List<String> tesourosDesc = new ArrayList<String>();
        for (TipoTesouro t : tesourosPorJogador.get(atual)) {
            tesourosDesc.add(t.getDescricao());
        }

        return new ResumoCartaObjetivo(
                abertas,
                descSecreta,
                opcoesDesc,
                bonusDesc,
                tesourosDesc,
                cidadesConquistadasPorJogador.get(atual));
    }

    public List<ResumoResultadoFinal> consultarResultadoFinal() {
        Map<Jogador, List<ResumoMelhoria>> melhoriasPorJogador =
                new HashMap<Jogador, List<ResumoMelhoria>>();
        for (int i = 0; i < participantes.size(); i++) {
            melhoriasPorJogador.put(participantes.get(i), consultarCartela(i).getMelhoriasDetalhadas());
        }
        return AvaliadorPontuacaoFinal.apurar(
                participantes,
                jogo.getTabuleiro(),
                tesourosPorJogador,
                secretaPorJogador,
                melhoriasPorJogador,
                peaoExtraAltaniPorJogador,
                regiaoPorParada,
                regiaoPorCidade,
                paradasPorProvinciaKhan,
                paradasPorCidade);
    }

    private void verificarCartasConselheiroAberto(Jogador j) {
        Tabuleiro tab = jogo.getTabuleiro();
        int idx = participantes.indexOf(j);
        List<ResumoMelhoria> melhorias = (idx >= 0)
                ? consultarCartela(idx).getMelhoriasDetalhadas()
                : Collections.<ResumoMelhoria>emptyList();
        List<TipoTesouro> tesouros = tesourosPorJogador.get(j);
        List<PecaBonus> bonus = bonusPorJogador.get(j);
        int conquistas = cidadesConquistadasPorJogador.get(j);

        Iterator<CartaConselheiroAberto> it = cartasAbertasAtivas.iterator();
        while (it.hasNext()) {
            CartaConselheiroAberto carta = it.next();
            boolean cumpriu = false;
            switch (carta) {
                case QUATRO_ESPADAS:
                    cumpriu = j.quantidadeTributo(TipoTributo.ESPADA) >= 4;
                    break;
                case SEIS_YURTS:
                    cumpriu = tab.contarTotalYurtsNoMapa(j.getCor()) >= 6;
                    break;
                case DEZ_YURTS:
                    cumpriu = tab.contarTotalYurtsNoMapa(j.getCor()) >= 10;
                    break;
                case QUATRO_TESOUROS:
                    cumpriu = tesouros != null && tesouros.size() >= 4;
                    break;
                case CINCO_TESOUROS_DIFERENTES:
                    cumpriu = tesouros != null && new HashSet<TipoTesouro>(tesouros).size() >= 5;
                    break;
                case UM_YURT_CADA_REGIAO:
                    cumpriu = tab.contarYurtsJogadorNaRegiao(j.getCor(), Regiao.CHINA, regiaoPorParada, regiaoPorCidade) >= 1
                            && tab.contarYurtsJogadorNaRegiao(j.getCor(), Regiao.PERSIA, regiaoPorParada, regiaoPorCidade) >= 1
                            && tab.contarYurtsJogadorNaRegiao(j.getCor(), Regiao.RUSSIA, regiaoPorParada, regiaoPorCidade) >= 1;
                    break;
                case DOIS_YURTS_CADA_REGIAO:
                    cumpriu = tab.contarYurtsJogadorNaRegiao(j.getCor(), Regiao.CHINA, regiaoPorParada, regiaoPorCidade) >= 2
                            && tab.contarYurtsJogadorNaRegiao(j.getCor(), Regiao.PERSIA, regiaoPorParada, regiaoPorCidade) >= 2
                            && tab.contarYurtsJogadorNaRegiao(j.getCor(), Regiao.RUSSIA, regiaoPorParada, regiaoPorCidade) >= 2;
                    break;
                case DOIS_BONUS:
                    cumpriu = bonus != null && bonus.size() >= 2;
                    break;
                case TRES_BONUS:
                    cumpriu = bonus != null && bonus.size() >= 3;
                    break;
                case DUAS_CIDADES:
                    cumpriu = conquistas >= 2;
                    break;
                case TRES_CIDADES:
                    cumpriu = conquistas >= 3;
                    break;
                case DUAS_COLUNAS_DUAS_MELHORIAS: {
                    int[] porCol = new int[4];
                    int colunasComDuas = 0;
                    for (ResumoMelhoria m : melhorias) {
                        if (m.getColuna() >= 0 && m.getColuna() < 4) {
                            porCol[m.getColuna()]++;
                        }
                    }
                    for (int qtd : porCol) {
                        if (qtd >= 2) {
                            colunasComDuas++;
                        }
                    }
                    cumpriu = colunasComDuas >= 2;
                    break;
                }
                case QUATRO_MELHORIAS_COLUNA: {
                    int[] porCol = new int[4];
                    for (ResumoMelhoria m : melhorias) {
                        if (m.getColuna() >= 0 && m.getColuna() < 4) {
                            porCol[m.getColuna()]++;
                            if (porCol[m.getColuna()] >= 4) {
                                cumpriu = true;
                            }
                        }
                    }
                    break;
                }
                default:
                    break;
            }
            if (cumpriu) {
                it.remove();
                concederVotosAoJogador(j, 1);
            }
        }
    }
}