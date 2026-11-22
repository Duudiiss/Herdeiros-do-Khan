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
import br.herdeiros.mapa.IconesTabuleiro;
import br.herdeiros.layout.KhanRecursosLayout;
import java.nio.file.Paths;

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
    private final List<CartaConselheiroAberto> pilhaCartasAbertas = new ArrayList<CartaConselheiroAberto>();
    /** Jogadores elegiveis por carta; remove quem ja satisfazia a meta quando foi revelada. */
    private final Map<CartaConselheiroAberto, Set<Integer>> elegiveisAbertas =
            new EnumMap<CartaConselheiroAberto, Set<Integer>>(CartaConselheiroAberto.class);
    private final Map<Jogador, Integer> bonusRecebidosPorJogador = new HashMap<Jogador, Integer>();
    private final List<TipoTesouro> pilhaTesouros = new ArrayList<TipoTesouro>();
    /** Tesouros revelados em cada cidade: nao sao sorteados no saque. */
    private final Map<NomeCidade, List<TipoTesouro>> tesourosExpostosCidades =
            new EnumMap<NomeCidade, List<TipoTesouro>>(NomeCidade.class);
    private final List<TipoTesouro> descarteTesouros = new ArrayList<TipoTesouro>();
    private final List<PecaBonus> poolPecasBonus = new ArrayList<PecaBonus>();
    private final Map<Jogador, List<TipoTesouro>> tesourosPorJogador = new HashMap<Jogador, List<TipoTesouro>>();
    private final Map<Jogador, List<PecaBonus>> bonusPorJogador = new HashMap<Jogador, List<PecaBonus>>();
    private final Map<Jogador, Set<PecaBonus>> bonusUtilizados = new HashMap<Jogador, Set<PecaBonus>>();
    private final List<TipoTesouro> escolhaTesourosBonusPendente = new ArrayList<TipoTesouro>();
    private boolean karakorumEspecial;
    private final Map<Jogador, List<CartaConselheiroSecreto>> opcoesSecretasPorJogador =
            new HashMap<Jogador, List<CartaConselheiroSecreto>>();
    private final Map<Jogador, CartaConselheiroSecreto> secretaPorJogador =
            new HashMap<Jogador, CartaConselheiroSecreto>();
    private final Map<Jogador, Integer> cidadesConquistadasPorJogador = new HashMap<Jogador, Integer>();
    private final Map<Jogador, Peao> peaoExtraAltaniPorJogador = new HashMap<Jogador, Peao>();
    private final Map<Jogador, List<Melhoria>> opcoesIniciaisJochi =
            new HashMap<Jogador, List<Melhoria>>();

    /**  Mapeamentos espaciais para influencia regional e cartas secretas*/
    private final Map<Integer, Regiao> regiaoPorParada = new HashMap<Integer, Regiao>();
    private final Map<NomeCidade, Regiao> regiaoPorCidade = new HashMap<NomeCidade, Regiao>();
    private final Map<DestinoKhan, Set<Integer>> paradasPorProvinciaKhan =
            new HashMap<DestinoKhan, Set<Integer>>();
    private final Map<NomeCidade, Set<Integer>> paradasPorCidade = new HashMap<NomeCidade, Set<Integer>>();

    private final List<Integer> estoquesIconesTributos = new ArrayList<Integer>();

    private int indiceAtual = 0;
    private transient MapaDados mapaDados = new MapaDados();
    private transient SavePartidaData.Meta metaSave = new SavePartidaData.Meta();
    private int rodadasConcluidas;
    private int pontuacaoKhan;
    private int rodadasNoDisparoFim = -1;
    private boolean partidaEncerrada;
    private boolean usandoPeaoExtraAltani;
    private boolean escolhaAltaniFeitaTurno;
    private boolean acaoRealizadaNoTurno;
    private int bonusMovimentosTurno;
    private int bonusPunhosTurno;
    private boolean habilidadeChagataiUsada;
    private boolean habilidadeOgedeiUsada;

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

    public boolean podeSalvarEntreTurnos() {
        return estadoValidoParaSave()
                && escolhaTesourosBonusPendente.isEmpty()
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
        dados.partida.usandoPeaoExtraAltani = usandoPeaoExtraAltani;
        dados.partida.escolhaAltaniFeitaTurno = escolhaAltaniFeitaTurno;
        dados.partida.acaoRealizadaNoTurno = acaoRealizadaNoTurno;
        dados.partida.bonusMovimentosTurno = bonusMovimentosTurno;
        dados.partida.bonusPunhosTurno = bonusPunhosTurno;
        dados.partida.habilidadeChagataiUsada = habilidadeChagataiUsada;
        dados.partida.habilidadeOgedeiUsada = habilidadeOgedeiUsada;

        for (MapaDados.Ponto ponto : mapaDados.getPontos().values()) {
            SavePartidaData.Ponto salvo = new SavePartidaData.Ponto();
            salvo.id = ponto.getId();
            salvo.x = ponto.getX();
            salvo.y = ponto.getY();
            salvo.dupla = ponto.isDupla();
            salvo.raio = ponto.getRaio();
            salvo.raioVertical = ponto.getRaioVertical();
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
            salvo.recursosDesenvolvedor.putAll(cartela.getRecursosDesenvolvedor());
            for (TipoTributo tipo : TipoTributo.values()) {
                int quantidade = cartela.getQuantidadeTributoDoTipo(tipo);
                if (quantidade > 0) {
                    salvo.tributos.put(tipo.name(), quantidade);
                }
            }
            for (Melhoria melhoria : cartela.getMelhoriasAdquiridas()) {
                salvo.melhorias.add(criarDadosMelhoria(melhoria));
            }
            for (TipoTesouro tesouro : tesourosPorJogador.getOrDefault(jogador, Collections.emptyList())) {
                salvo.tesouros.add(tesouro.name());
            }
            salvo.bonusRecebidos = bonusRecebidosPorJogador.getOrDefault(jogador, 0);
            for (PecaBonus bonus : bonusPorJogador.getOrDefault(jogador, Collections.emptyList())) {
                salvo.bonus.add(bonus.name());
                if (bonusUtilizados.getOrDefault(jogador, Collections.emptySet()).contains(bonus)) {
                    salvo.bonusUtilizados.add(bonus.name());
                }
            }
            for (CartaConselheiroSecreto carta : opcoesSecretasPorJogador.getOrDefault(
                    jogador, Collections.emptyList())) {
                salvo.opcoesSecretas.add(carta.name());
            }
            CartaConselheiroSecreto secreta = secretaPorJogador.get(jogador);
            salvo.cartaSecreta = secreta == null ? null : secreta.name();
            salvo.cidadesConquistadas = cidadesConquistadasPorJogador.getOrDefault(jogador, 0);
            Peao extra = peaoExtraAltaniPorJogador.get(jogador);
            if (extra != null && extra.getParadaAtual() != null) {
                salvo.paradaPeaoExtraAltani = extra.getParadaAtual().getId();
            }
            for (Melhoria opcao : opcoesIniciaisJochi.getOrDefault(
                    jogador, Collections.emptyList())) {
                SavePartidaData.Melhoria item = new SavePartidaData.Melhoria();
                item.codigo = opcao.getCodigo();
                item.coluna = opcao.getColuna();
                salvo.opcoesIniciaisJochi.add(item);
            }
            dados.jogadores.add(salvo);
        }

        Tabuleiro tabuleiro = jogo.getTabuleiro();
        Parada paradaKhan = tabuleiro.getPeaoDoKhan().getParadaAtual();
        dados.tabuleiro.paradaKhan = paradaKhan == null ? -1 : paradaKhan.getId();
        Cor tolui = tabuleiro.getYurtToluiKarakorum();
        dados.tabuleiro.yurtToluiKarakorum = tolui == null ? null : tolui.name();
        for (Map.Entry<NomeCidade, Cor> entrada : tabuleiro.getYurtsConquistaCidade().entrySet()) {
            dados.tabuleiro.yurtsConquistaCidade.put(
                    entrada.getKey().name(), entrada.getValue().name());
        }
        dados.tabuleiro.destinoKhan = tabuleiro.getDestinoKhan() == null
                ? null : tabuleiro.getDestinoKhan().name();
        for (Parada parada : tabuleiro.getParadas()) {
            if (parada.getId() == 0) {
                // A yurt inicial de Tolui e armazenada separadamente.
                continue;
            }
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

        inicializarEstoquesIconesSeNecessario();
        dados.estoquesIconesTributos.addAll(estoquesIconesTributos);
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
        for (LocalConselheiro local : locaisConselheiros) {
            SavePartidaData.Local salvo = new SavePartidaData.Local();
            salvo.parada = local.getIdParada();
            salvo.nome = local.getNome();
            salvo.votos = local.getVotosConcedidos();
            TipoTesouro[] demanda = local.getTesourosExigidos();
            if (demanda != null) {
                salvo.demanda = new ArrayList<>();
                for (TipoTesouro tipo : demanda) {
                    salvo.demanda.add(tipo.name());
                }
            }
            salvo.bonus = local.getPecaBonus() == null ? null : local.getPecaBonus().name();
            dados.conselheiros.locais.add(salvo);
        }
        for (CartaConselheiroAberto carta : cartasAbertasAtivas) {
            dados.conselheiros.cartasAbertas.add(carta.name());
            dados.conselheiros.elegiveisAbertas.put(carta.name(),
                    new ArrayList<Integer>(elegiveisAbertas.getOrDefault(carta, Collections.<Integer>emptySet())));
        }
        for (CartaConselheiroAberto carta : pilhaCartasAbertas) {
            dados.conselheiros.pilhaCartasAbertas.add(carta.name());
        }
        for (Map.Entry<NomeCidade, List<TipoTesouro>> entrada : tesourosExpostosCidades.entrySet()) {
            List<String> tipos = new ArrayList<String>();
            for (TipoTesouro tipo : entrada.getValue()) {
                tipos.add(tipo.name());
            }
            dados.conselheiros.tesourosExpostosCidades.put(entrada.getKey().name(), tipos);
        }
        for (TipoTesouro tesouro : pilhaTesouros) {
            dados.conselheiros.pilhaTesouros.add(tesouro.name());
        }
        for (TipoTesouro tesouro : descarteTesouros) {
            dados.conselheiros.descarteTesouros.add(tesouro.name());
        }
        dados.conselheiros.karakorumEspecial = karakorumEspecial;
        for (PecaBonus bonus : poolPecasBonus) {
            dados.conselheiros.poolPecasBonus.add(bonus.name());
        }
        for (Map.Entry<Integer, Regiao> entry : regiaoPorParada.entrySet()) {
            dados.influencia.regiaoPorParada.put(entry.getKey(), entry.getValue().name());
        }
        for (Map.Entry<NomeCidade, Regiao> entry : regiaoPorCidade.entrySet()) {
            dados.influencia.regiaoPorCidade.put(entry.getKey().name(), entry.getValue().name());
        }
        for (Map.Entry<DestinoKhan, Set<Integer>> entry : paradasPorProvinciaKhan.entrySet()) {
            dados.influencia.paradasPorProvinciaKhan.put(entry.getKey().name(),
                    new ArrayList<>(entry.getValue()));
        }
        for (Map.Entry<NomeCidade, Set<Integer>> entry : paradasPorCidade.entrySet()) {
            dados.influencia.paradasPorCidade.put(entry.getKey().name(),
                    new ArrayList<>(entry.getValue()));
        }
        return dados;
    }

    static InterfacePartida restaurarDadosSave(SavePartidaData dados) {
        if (dados == null || dados.meta == null || !"herdeiros-save".equals(dados.meta.formato)
                || (dados.meta.versao != 1 && dados.meta.versao != 2)
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
            cartela.restaurarRecursosDesenvolvedor(salvo.recursosDesenvolvedor);
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
        this.usandoPeaoExtraAltani = dados.partida.usandoPeaoExtraAltani;
        this.escolhaAltaniFeitaTurno = dados.partida.escolhaAltaniFeitaTurno;
        this.acaoRealizadaNoTurno = dados.partida.acaoRealizadaNoTurno;
        this.bonusMovimentosTurno = dados.partida.bonusMovimentosTurno;
        this.bonusPunhosTurno = dados.partida.bonusPunhosTurno;
        this.karakorumEspecial = dados.conselheiros.karakorumEspecial;
        this.habilidadeChagataiUsada = dados.partida.habilidadeChagataiUsada;
        this.habilidadeOgedeiUsada = dados.partida.habilidadeOgedeiUsada;
        if (dados.estoquesIconesTributos != null && !dados.estoquesIconesTributos.isEmpty()) {
            for (Integer quantidade : dados.estoquesIconesTributos) {
                if (quantidade == null || quantidade < 0 || quantidade > 3) {
                    throw new IllegalArgumentException("Estoque de icone invalido no salvamento");
                }
                estoquesIconesTributos.add(quantidade);
            }
        }
        if (dados.meta.versao >= 2) {
            restaurarMecanicasAvancadas(dados);
        } else {
            // Compatibilidade com saves antigos: reconstroi as estruturas inexistentes.
            inicializarMecanicasRecuperadasLegado();
        }
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

    private void restaurarMecanicasAvancadas(SavePartidaData dados) {
        if (dados.conselheiros == null || dados.influencia == null
                || dados.conselheiros.locais == null || dados.conselheiros.cartasAbertas == null
                || dados.conselheiros.pilhaTesouros == null || dados.conselheiros.descarteTesouros == null
                || dados.conselheiros.poolPecasBonus == null) {
            throw new IllegalArgumentException("Save de conselheiros incompleto.");
        }
        for (SavePartidaData.Local salvo : dados.conselheiros.locais) {
            if (salvo == null || salvo.nome == null) {
                throw new IllegalArgumentException("Conselheiro invalido no save.");
            }
            TipoTesouro[] demanda = null;
            if (salvo.demanda != null) {
                demanda = new TipoTesouro[salvo.demanda.size()];
                for (int i = 0; i < demanda.length; i++) {
                    demanda[i] = TipoTesouro.valueOf(salvo.demanda.get(i));
                }
            }
            // Saves antigos trazem requisitos embaralhados. Corrige pelo ID oficial.
            switch (salvo.parada) {
                case 0:
                    demanda = new TipoTesouro[] {
                            TipoTesouro.FERRO, TipoTesouro.PORCELANA, TipoTesouro.GADO};
                    break;
                case 29:
                    demanda = new TipoTesouro[] {TipoTesouro.GADO, TipoTesouro.GADO};
                    break;
                case 23:
                    demanda = null;
                    break;
                case 30:
                    demanda = new TipoTesouro[] {TipoTesouro.PORCELANA, TipoTesouro.PORCELANA};
                    break;
                case 32:
                    demanda = new TipoTesouro[] {TipoTesouro.PELES, TipoTesouro.PELES};
                    break;
                case 31:
                    demanda = new TipoTesouro[] {TipoTesouro.SEDA, TipoTesouro.SEDA};
                    break;
                case 28:
                    demanda = new TipoTesouro[] {TipoTesouro.FERRO, TipoTesouro.FERRO};
                    break;
                default:
                    break;
            }
            PecaBonus bonus = salvo.bonus == null ? null : PecaBonus.valueOf(salvo.bonus);
            locaisConselheiros.add(new LocalConselheiro(
                    salvo.parada, salvo.nome, demanda, salvo.votos, bonus));
        }
        if (procurarParada(33) != null) {
            registrarParadaDuplaConselheiro(33);
        }
        for (String carta : dados.conselheiros.cartasAbertas) {
            cartasAbertasAtivas.add(CartaConselheiroAberto.valueOf(carta));
        }
        if (dados.conselheiros.pilhaCartasAbertas != null) {
            for (String carta : dados.conselheiros.pilhaCartasAbertas) {
                CartaConselheiroAberto tipo = CartaConselheiroAberto.valueOf(carta);
                if (!cartasAbertasAtivas.contains(tipo) && !pilhaCartasAbertas.contains(tipo)) {
                    pilhaCartasAbertas.add(tipo);
                }
            }
        }
        // Compatibilidade com saves antigos que nao guardavam a pilha.
        if (dados.conselheiros.pilhaCartasAbertas == null) {
            for (CartaConselheiroAberto tipo : CartaConselheiroAberto.values()) {
                if (!cartasAbertasAtivas.contains(tipo)) {
                    pilhaCartasAbertas.add(tipo);
                }
            }
        }
        for (CartaConselheiroAberto carta : cartasAbertasAtivas) {
            Set<Integer> indices = new HashSet<Integer>();
            if (dados.conselheiros.elegiveisAbertas != null
                    && dados.conselheiros.elegiveisAbertas.containsKey(carta.name())) {
                indices.addAll(dados.conselheiros.elegiveisAbertas.get(carta.name()));
            } else {
                for (int indice = 0; indice < participantes.size(); indice++) {
                    indices.add(indice);
                }
            }
            elegiveisAbertas.put(carta, indices);
        }
        for (String tesouro : dados.conselheiros.pilhaTesouros) {
            pilhaTesouros.add(TipoTesouro.valueOf(tesouro));
        }
        if (dados.conselheiros.tesourosExpostosCidades != null
                && !dados.conselheiros.tesourosExpostosCidades.isEmpty()) {
            for (Map.Entry<String, List<String>> entrada :
                    dados.conselheiros.tesourosExpostosCidades.entrySet()) {
                List<TipoTesouro> tipos = new ArrayList<TipoTesouro>();
                for (String tipo : entrada.getValue()) {
                    tipos.add(TipoTesouro.valueOf(tipo));
                }
                tesourosExpostosCidades.put(NomeCidade.valueOf(entrada.getKey()), tipos);
            }
        } else {
            // Saves antigos ainda nao registravam os tesouros visiveis das cidades.
            for (Cidade cidade : jogo.getTabuleiro().getCidadesDisponiveis()) {
                revelarTesourosCidade(cidade);
            }
        }
        for (String tesouro : dados.conselheiros.descarteTesouros) {
            descarteTesouros.add(TipoTesouro.valueOf(tesouro));
        }
        for (String bonus : dados.conselheiros.poolPecasBonus) {
            poolPecasBonus.add(PecaBonus.valueOf(bonus));
        }
        for (int i = 0; i < participantes.size(); i++) {
            Jogador jogador = participantes.get(i);
            SavePartidaData.Jogador salvo = dados.jogadores.get(i);
            if (salvo.tesouros == null || salvo.bonus == null || salvo.opcoesSecretas == null) {
                throw new IllegalArgumentException("Cartas do jogador incompletas no save.");
            }
            List<TipoTesouro> tesouros = new ArrayList<>();
            for (String tipo : salvo.tesouros) {
                tesouros.add(TipoTesouro.valueOf(tipo));
            }
            tesourosPorJogador.put(jogador, tesouros);
            List<PecaBonus> bonus = new ArrayList<>();
            for (String tipo : salvo.bonus) {
                bonus.add(PecaBonus.valueOf(tipo));
            }
            bonusPorJogador.put(jogador, bonus);
            Set<PecaBonus> usados = new HashSet<PecaBonus>();
            if (salvo.bonusUtilizados != null) {
                for (String tipo : salvo.bonusUtilizados) usados.add(PecaBonus.valueOf(tipo));
            }
            bonusUtilizados.put(jogador, usados);
            bonusRecebidosPorJogador.put(jogador, salvo.bonusRecebidos == null ? bonus.size() : salvo.bonusRecebidos);
            List<CartaConselheiroSecreto> opcoes = new ArrayList<>();
            for (String carta : salvo.opcoesSecretas) {
                opcoes.add(CartaConselheiroSecreto.valueOf(carta));
            }
            opcoesSecretasPorJogador.put(jogador, opcoes);
            if (salvo.cartaSecreta != null) {
                secretaPorJogador.put(jogador,
                        CartaConselheiroSecreto.valueOf(salvo.cartaSecreta));
            }
            cidadesConquistadasPorJogador.put(jogador, salvo.cidadesConquistadas);
            if (salvo.opcoesIniciaisJochi != null
                    && !salvo.opcoesIniciaisJochi.isEmpty()) {
                List<Melhoria> opcoesJochi = new ArrayList<Melhoria>();
                for (SavePartidaData.Melhoria carta : salvo.opcoesIniciaisJochi) {
                    opcoesJochi.add(MercadoMelhorias.criarPorCodigo(carta.codigo, carta.coluna));
                }
                opcoesIniciaisJochi.put(jogador, opcoesJochi);
            }
            if (salvo.paradaPeaoExtraAltani != null) {
                Parada destino = procurarParada(salvo.paradaPeaoExtraAltani);
                if (destino == null) {
                    throw new IllegalArgumentException("Posicao do peao extra invalida.");
                }
                Peao extra = new Peao(jogador.getCor());
                jogo.getTabuleiro().posicionarPeaoInicial(extra);
                extra.posicionarEm(destino);
                peaoExtraAltaniPorJogador.put(jogador, extra);
            }
        }
        if (dados.influencia.regiaoPorParada == null || dados.influencia.regiaoPorCidade == null
                || dados.influencia.paradasPorCidade == null
                || dados.influencia.paradasPorProvinciaKhan == null) {
            throw new IllegalArgumentException("Dados de influencia incompletos.");
        }
        for (Map.Entry<Integer, String> e : dados.influencia.regiaoPorParada.entrySet()) {
            regiaoPorParada.put(e.getKey(), Regiao.valueOf(e.getValue()));
        }
        for (Map.Entry<String, String> e : dados.influencia.regiaoPorCidade.entrySet()) {
            regiaoPorCidade.put(NomeCidade.valueOf(e.getKey()), Regiao.valueOf(e.getValue()));
        }
        for (Map.Entry<String, List<Integer>> e : dados.influencia.paradasPorCidade.entrySet()) {
            paradasPorCidade.put(NomeCidade.valueOf(e.getKey()), new HashSet<>(e.getValue()));
        }
        for (Map.Entry<String, List<Integer>> e : dados.influencia.paradasPorProvinciaKhan.entrySet()) {
            paradasPorProvinciaKhan.put(DestinoKhan.valueOf(e.getKey()), new HashSet<>(e.getValue()));
        }
    }

    private Parada procurarParada(int id) {
        for (Parada parada : jogo.getTabuleiro().getParadas()) {
            if (parada.getId() == id) {
                return parada;
            }
        }
        return null;
    }

    private void inicializarMecanicasRecuperadasLegado() {
        for (Jogador jogador : participantes) {
            tesourosPorJogador.put(jogador, new ArrayList<>());
            bonusPorJogador.put(jogador, new ArrayList<>());
            bonusUtilizados.put(jogador, new HashSet<PecaBonus>());
            opcoesSecretasPorJogador.put(jogador, new ArrayList<>());
            cidadesConquistadasPorJogador.put(jogador, 0);
            if (jogador.getPersonagem() == Personagem.ALTANI) {
                Peao extra = new Peao(jogador.getCor());
                jogo.getTabuleiro().posicionarPeaoInicial(extra);
                peaoExtraAltaniPorJogador.put(jogador, extra);
            }
        }
        for (Cidade cidade : jogo.getTabuleiro().getCidadesDisponiveis()) {
            regiaoPorCidade.put(cidade.getNomeCidade(), cidade.getRegiao());
        }
        // Arquivos antigos nao guardavam escolhas e ordem dos baralhos deste modulo.
        // Evita erros ao consultar, mas nao e possivel recuperar escolhas perdidas.
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
            double raioY = ponto.raioVertical == null ? raio : ponto.raioVertical;
            if (!mapa.alterarElipse(ponto.id, raio, raioY)) {
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
            copia.alterarElipse(ponto.getId(), ponto.getRaio(), ponto.getRaioVertical());
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
            bonusUtilizados.put(j, new HashSet<PecaBonus>());
            bonusRecebidosPorJogador.put(j, 0);
            cidadesConquistadasPorJogador.put(j, 0);

            if (j.getPersonagem() == Personagem.ALTANI) {
                Peao segundoPeao = new Peao(j.getCor());
                jogo.getTabuleiro().posicionarPeaoInicial(segundoPeao);
                peaoExtraAltaniPorJogador.put(j, segundoPeao);
            } else if (j.getPersonagem() == Personagem.TOLUI) {
                jogo.getTabuleiro().posicionarYurtInicialTolui(j.getCor());
            }
        }

        // Jochi escolhe uma entre duas melhorias sorteadas para a coluna branca.
        for (Jogador jogador : participantes) {
            if (jogador.getPersonagem() == Personagem.JOCHI) {
                List<Melhoria> opcoes = new ArrayList<Melhoria>();
                for (int i = 0; i < 2; i++) {
                    Melhoria sorteada = mercadoMelhorias.retirarAleatoria();
                    if (sorteada != null) {
                        opcoes.add(sorteada);
                    }
                }
                opcoesIniciaisJochi.put(jogador, opcoes);
            }
        }

        // 2. Baralho de 40 tesouros (8 de cada um dos 5 tipos)
        for (TipoTesouro tipo : TipoTesouro.values()) {
            for (int i = 0; i < 8; i++) {
                pilhaTesouros.add(tipo);
            }
        }
        Collections.shuffle(pilhaTesouros);
        for (Cidade cidade : jogo.getTabuleiro().getCidadesDisponiveis()) {
            revelarTesourosCidade(cidade);
        }


        // 3. Embaralha as 7 pecas de bonus e cria o local de Karakorum (id 0)
        poolPecasBonus.addAll(Arrays.asList(PecaBonus.values()));
        Collections.shuffle(poolPecasBonus);

        TipoTesouro[] demandaKarakorum = demandaKarakorum();
        PecaBonus bonusKarakorum = poolPecasBonus.isEmpty() ? null : poolPecasBonus.remove(0);
        locaisConselheiros.add(new LocalConselheiro(0, "Karakorum", demandaKarakorum, 2, bonusKarakorum));

        // 4. Sorteia 4 cartas de Conselheiro Aberto
        List<CartaConselheiroAberto> todasAbertas =
                new ArrayList<CartaConselheiroAberto>(Arrays.asList(CartaConselheiroAberto.values()));
        Collections.shuffle(todasAbertas);
        for (int i = 0; i < todasAbertas.size(); i++) {
            if (i < 4) {
                revelarCartaAberta(todasAbertas.get(i));
            } else {
                pilhaCartasAbertas.add(todasAbertas.get(i));
            }
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

    /** A peca especial altera somente a demanda; nao integra as sete pecas sorteadas. */
    private TipoTesouro[] demandaKarakorum() {
        return karakorumEspecial
                ? new TipoTesouro[] {TipoTesouro.FERRO, TipoTesouro.SEDA, TipoTesouro.PELES}
                : new TipoTesouro[] {TipoTesouro.FERRO, TipoTesouro.PORCELANA, TipoTesouro.GADO};
    }

    public boolean configurarKarakorumEspecial(boolean ativo) {
        if (rodadasConcluidas != 0 || acaoRealizadaNoTurno) return false;
        karakorumEspecial = ativo;
        for (LocalConselheiro local : locaisConselheiros) {
            if (local.getIdParada() == 0) local.alterarDemanda(demandaKarakorum());
        }
        notificarObservadores();
        return true;
    }

    public boolean isKarakorumEspecial() { return karakorumEspecial; }

    private void registrarParadaDuplaConselheiro(int idParada) {
        for (LocalConselheiro existente : locaisConselheiros) {
            if (existente.getIdParada() == idParada) {
                return;
            }
        }
        TipoTesouro[] demanda;
        switch (idParada) {
            case 29:
                demanda = new TipoTesouro[] {TipoTesouro.GADO, TipoTesouro.GADO};
                break;
            case 23:
            case 33:
                demanda = null; // Tres tesouros quaisquer.
                break;
            case 30:
                demanda = new TipoTesouro[] {TipoTesouro.PORCELANA, TipoTesouro.PORCELANA};
                break;
            case 32:
                demanda = new TipoTesouro[] {TipoTesouro.PELES, TipoTesouro.PELES};
                break;
            case 31:
                demanda = new TipoTesouro[] {TipoTesouro.SEDA, TipoTesouro.SEDA};
                break;
            case 28:
                demanda = new TipoTesouro[] {TipoTesouro.FERRO, TipoTesouro.FERRO};
                break;
            default:
                return; // Apenas as seis paradas oficiais tem conselheiros.
        }
        PecaBonus bonus = poolPecasBonus.isEmpty() ? null : poolPecasBonus.remove(0);
        locaisConselheiros.add(new LocalConselheiro(
                idParada, "Conselheiro (Parada " + idParada + ")", demanda, 1, bonus));
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
                if (ponto.isDupla() || ponto.getId() == 33) {
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
            if ((dupla || id == 33) && id != 0) {
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

    /** Chagatai troca um cavalo ainda disponivel por uma espada, uma vez por turno. */
    public boolean usarHabilidadeChagatai() {
        if (partidaEncerrada || habilidadeChagataiUsada) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        if (jogador.getPersonagem() != Personagem.CHAGATAI
                || !jogador.getTabuleiroIndividual().consumirMovimentos(1)) {
            return false;
        }
        jogador.receberTributos(TipoTributo.ESPADA, 1);
        habilidadeChagataiUsada = true;
        acaoRealizadaNoTurno = true;
        notificarObservadores();
        return true;
    }

    /** Ogedei compra um tesouro visivel de qualquer cidade por 1 moeda, uma vez por turno. */
    public boolean usarHabilidadeOgedei(String nomeCidade, TipoTesouro escolhido) {
        if (partidaEncerrada || habilidadeOgedeiUsada || escolhido == null) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        if (jogador.getPersonagem() != Personagem.OGEDEI) {
            return false;
        }
        NomeCidade nome = interpretarCidade(nomeCidade);
        Cidade cidade = nome == null ? null : encontrarCidadeDisponivel(nome);
        if (cidade == null || cidade.getQuantidadeTesouros() <= 1) {
            return false;
        }
        List<TipoTesouro> fichas = tesourosExpostosCidades.get(nome);
        if (fichas == null || !fichas.contains(escolhido)) {
            return false;
        }
        if (!jogador.getTabuleiroIndividual().removerTributosPorTipo(TipoTributo.MOEDA, 1)) {
            return false;
        }
        // Ha pelo menos dois tesouros: esta compra nunca conquista a cidade.
        cidade.saquearTesouro();
        fichas.remove(escolhido);
        tesourosPorJogador.get(jogador).add(escolhido);
        habilidadeOgedeiUsada = true;
        acaoRealizadaNoTurno = true;
        verificarCartasConselheiroAberto(jogador);
        notificarObservadores();
        return true;
    }

    public boolean habilidadeEspecialDisponivel() {
        Jogador jogador = participantes.get(indiceAtual);
        if (partidaEncerrada) {
            return false;
        }
        if (jogador.getPersonagem() == Personagem.CHAGATAI) {
            return !habilidadeChagataiUsada;
        }
        if (jogador.getPersonagem() == Personagem.OGEDEI) {
            return !habilidadeOgedeiUsada;
        }
        return jogador.getPersonagem() == Personagem.ALTANI && !acaoRealizadaNoTurno;
    }

    /** Altani escolhe exatamente um peao para executar as acoes do turno. */
    public boolean escolherPeaoAtivoAltani(boolean segundoPeao) {
        if (partidaEncerrada || escolhaAltaniFeitaTurno || acaoRealizadaNoTurno) {
            return false;
        }
        Jogador atual = participantes.get(indiceAtual);
        if (atual.getPersonagem() != Personagem.ALTANI
                || atual.getTabuleiroIndividual().consultarAtivacao(Personagem.ALTANI)
                        .getColunaAtiva() >= 0) {
            return false;
        }
        usandoPeaoExtraAltani = segundoPeao;
        escolhaAltaniFeitaTurno = true;
        paradasVisitadasTurno.clear();
        registrarParadaAtualNoTurno();
        notificarObservadores();
        return true;
    }

    public boolean escolhaPeaoAltaniRealizada() {
        return escolhaAltaniFeitaTurno;
    }

    /** Compatibilidade: alterna uma unica vez antes das acoes do turno. */
    public boolean alternarPeaoAtivoAltani() {
        return escolherPeaoAtivoAltani(!usandoPeaoExtraAltani);
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

    /** Custo de uma rota praticavel com os cavalos ainda disponiveis. */
    public int consultarCustoMovimentoAtual(int destino) {
        Jogador jogador = participantes.get(indiceAtual);
        Peao peao = obterPeaoAtivo(jogador);
        int cavalos = jogador.getTabuleiroIndividual()
                .consultarAtivacao(jogador.getPersonagem()).getMovimentosRestantes()
                + bonusMovimentosTurno;
        return jogo.getTabuleiro().distanciaMinima(
                peao, destino, jogador.getCor(), cavalos);
    }

    /** Jogador ativo da rodada visual. */
    public int getIndiceJogadorAtual() {
        return indiceAtual;
    }

    public boolean movimentarJogadorAtual(int destino, int movimentosDisponiveis) {
        Jogador jogador = participantes.get(indiceAtual);
        Peao peao = obterPeaoAtivo(jogador);
        List<Integer> percurso = jogo.getTabuleiro().consultarCaminho(
                peao, destino, jogador.getCor(), movimentosDisponiveis);
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
        if (jogador.getPersonagem() == Personagem.JOCHI
                && !consultarOpcoesIniciaisJochi().isEmpty()) {
            return false;
        }
        boolean ativada = jogador.getTabuleiroIndividual().ativarColuna(
                coluna, jogador.getPersonagem());
        if (ativada) {
            // Objetivos que contam tesouros podem ser satisfeitos pela melhoria virtual
            // assim que a coluna passa a oferecer o recurso neste turno.
            verificarCartasConselheiroAberto(jogador);
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

        if (custo < 0 || custo > movimentosTotais) {
            return false;
        }
        Peao peao = obterPeaoAtivo(jogador);
        List<Integer> percurso = jogo.getTabuleiro().consultarCaminho(
                peao, destino, jogador.getCor(), movimentosTotais);
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
        if (partidaEncerrada || !escolhaTesourosBonusPendente.isEmpty()) {
            return false;
        }
        Jogador jogadorDaVez = participantes.get(indiceAtual);
        if (!secretaPorJogador.containsKey(jogadorDaVez)) {
            return false;
        }
        TabuleiroIndividual cartela = jogadorDaVez.getTabuleiroIndividual();
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
        escolhaAltaniFeitaTurno = false;
        acaoRealizadaNoTurno = false;
        bonusMovimentosTurno = 0;
        bonusPunhosTurno = 0;
        habilidadeChagataiUsada = false;
        habilidadeOgedeiUsada = false;

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

    public int consultarRodadasConcluidas() {
        return rodadasConcluidas;
    }

    /** Posicoes independentes dos dois peoes de Altani, inclusive quando nao estao ativos. */
    public int consultarParadaPeaoPrincipal(int indice) {
        if (indice < 0 || indice >= participantes.size()) {
            return -1;
        }
        Parada parada = participantes.get(indice).getMeuPeao().getParadaAtual();
        return parada == null ? -1 : parada.getId();
    }

    public int consultarParadaSegundoPeaoAltani(int indice) {
        if (indice < 0 || indice >= participantes.size()) {
            return -1;
        }
        Peao extra = peaoExtraAltaniPorJogador.get(participantes.get(indice));
        return extra == null || extra.getParadaAtual() == null
                ? -1 : extra.getParadaAtual().getId();
    }

    public boolean estaUsandoSegundoPeaoAltani() {
        return usandoPeaoExtraAltani;
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

    public Map<TipoTesouro, Integer> consultarTesourosPorTipoJogadorAtual() {
        Map<TipoTesouro, Integer> quantidades = new EnumMap<TipoTesouro, Integer>(TipoTesouro.class);
        for (TipoTesouro tipo : TipoTesouro.values()) {
            quantidades.put(tipo, 0);
        }
        for (TipoTesouro tipo : tesourosPorJogador.getOrDefault(
                participantes.get(indiceAtual), Collections.emptyList())) {
            quantidades.put(tipo, quantidades.get(tipo) + 1);
        }
        return Collections.unmodifiableMap(quantidades);
    }

    /** Tesouros virtuais por tipo, validos somente na ativacao atual. */
    public Map<TipoTesouro, Integer> consultarTesourosVirtuaisJogadorAtual() {
        return participantes.get(indiceAtual).getTabuleiroIndividual().consultarTesourosVirtuais();
    }

    private boolean podePagarTesouros(LocalConselheiro local, List<TipoTesouro> fisicos,
                                      Map<TipoTesouro, Integer> virtuais) {
        TipoTesouro[] exigidos = local.getTesourosExigidos();
        if (exigidos == null) {
            int total = fisicos.size();
            for (int valor : virtuais.values()) total += valor;
            return total >= 3;
        }
        List<TipoTesouro> restantes = new ArrayList<TipoTesouro>(fisicos);
        Map<TipoTesouro, Integer> saldo = new EnumMap<TipoTesouro, Integer>(virtuais);
        for (TipoTesouro tipo : exigidos) {
            if (!restantes.remove(tipo)) {
                int quantidade = saldo.getOrDefault(tipo, 0);
                if (quantidade <= 0) return false;
                saldo.put(tipo, quantidade - 1);
            }
        }
        return true;
    }

    public int consultarQuantidadeTesourosJogadorAtual() {
        return tesourosPorJogador.getOrDefault(participantes.get(indiceAtual),
                Collections.emptyList()).size();
    }

    public int consultarCavalosRestantesJogadorAtual() {
        return consultarAtivacaoJogadorAtual().getMovimentosRestantes() + bonusMovimentosTurno;
    }

    public int consultarPunhosRestantesJogadorAtual() {
        return consultarAtivacaoJogadorAtual().getPunhosDaColuna() + bonusPunhosTurno;
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

    /** Acoes administrativas para testes manuais; nao sao movimentos normais. */
    public boolean desenvolvimentoAjustarTributo(int indiceJogador, String nomeTributo, int variacao) {
        if (partidaEncerrada || indiceJogador < 0 || indiceJogador >= participantes.size()
                || nomeTributo == null || variacao == 0 || Math.abs((long) variacao) > 999) {
            return false;
        }
        TipoTributo tipo;
        try {
            tipo = TipoTributo.valueOf(nomeTributo);
        } catch (IllegalArgumentException erro) {
            return false;
        }
        TabuleiroIndividual cartela = participantes.get(indiceJogador).getTabuleiroIndividual();
        if (variacao < 0 && cartela.getQuantidadeTributoDoTipo(tipo) < -variacao) {
            return false;
        }
        if (variacao > 0) {
            participantes.get(indiceJogador).receberTributos(tipo, variacao);
        } else {
            cartela.removerTributosPorTipo(tipo, -variacao);
        }
        verificarCartasConselheiroAberto(participantes.get(indiceJogador));
        notificarObservadores();
        return true;
    }

    /** Ajusta recursos virtuais temporarios e recursos da rodada sem criar tributos fisicos. */
    public boolean desenvolvimentoAjustarRecursoEspecial(int indiceJogador, String recurso, int variacao) {
        if (partidaEncerrada || indiceJogador < 0 || indiceJogador >= participantes.size()
                || recurso == null || variacao == 0 || Math.abs((long) variacao) > 999) {
            return false;
        }
        if (!participantes.get(indiceJogador).getTabuleiroIndividual()
                .desenvolvimentoAjustarVirtual(recurso, variacao)) {
            return false;
        }
        verificarCartasConselheiroAberto(participantes.get(indiceJogador));
        notificarObservadores();
        return true;
    }

    /** Permite montar maos de tesouros e fichas de bonus para testes de aceitacao. */
    public boolean desenvolvimentoAjustarItemEspecial(int indiceJogador, String item, int variacao) {
        if (partidaEncerrada || indiceJogador < 0 || indiceJogador >= participantes.size()
                || item == null || variacao == 0 || Math.abs((long) variacao) > 999) {
            return false;
        }
        Jogador jogador = participantes.get(indiceJogador);
        if (item.startsWith("TESOURO_")) {
            TipoTesouro tipo;
            try {
                tipo = TipoTesouro.valueOf(item.substring("TESOURO_".length()));
            } catch (IllegalArgumentException erro) {
                return false;
            }
            List<TipoTesouro> mao = tesourosPorJogador.get(jogador);
            if (variacao < 0 && Collections.frequency(mao, tipo) < -variacao) {
                return false;
            }
            for (int i = 0; i < Math.abs(variacao); i++) {
                if (variacao > 0) {
                    mao.add(tipo);
                } else {
                    mao.remove(tipo);
                }
            }
        } else if (item.startsWith("BONUS_")) {
            PecaBonus tipo;
            try {
                tipo = PecaBonus.valueOf(item.substring("BONUS_".length()));
            } catch (IllegalArgumentException erro) {
                return false;
            }
            List<PecaBonus> mao = bonusPorJogador.get(jogador);
            if (variacao < 0 && Collections.frequency(mao, tipo) < -variacao) {
                return false;
            }
            for (int i = 0; i < Math.abs(variacao); i++) {
                if (variacao > 0) {
                    mao.add(tipo);
                } else {
                    mao.remove(tipo);
                }
            }
        } else {
            return false;
        }
        verificarCartasConselheiroAberto(jogador);
        notificarObservadores();
        return true;
    }

    public boolean desenvolvimentoAlterarVotos(int indiceJogador, int quantidade) {
        if (partidaEncerrada || indiceJogador < 0 || indiceJogador >= participantes.size()
                || quantidade <= 0 || quantidade > 999) {
            return false;
        }
        concederVotosAoJogador(participantes.get(indiceJogador), quantidade);
        notificarObservadores();
        return true;
    }

    public boolean desenvolvimentoPosicionarKhan(DestinoKhan destino) {
        if (partidaEncerrada || destino == null) {
            return false;
        }
        if (!jogo.getTabuleiro().deslocarKhan(destino)) {
            return false;
        }
        aplicarEfeitoDestinoKhan(destino);
        notificarObservadores();
        return true;
    }

    public boolean desenvolvimentoAjustarPontuacaoKhan(int variacao) {
        if (partidaEncerrada || variacao == 0 || Math.abs((long) variacao) > 999
                || (long) pontuacaoKhan + variacao < 0) {
            return false;
        }
        pontuacaoKhan = Math.min(consultarCasaFinal(), pontuacaoKhan + variacao);
        verificarFimPorKhan();
        notificarObservadores();
        return true;
    }

    public boolean desenvolvimentoEncerrarPartida() {
        if (partidaEncerrada) {
            return false;
        }
        partidaEncerrada = true;
        notificarObservadores();
        return true;
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
        if (partidaEncerrada) {
            return;
        }
        int votosConcedidos = Math.min(quantidade, consultarCasaFinal() - pontuacaoKhan);
        if (votosConcedidos <= 0) {
            verificarFimPorKhan();
            return;
        }
        jogador.ganharVotos(votosConcedidos);
        pontuacaoKhan += votosConcedidos;
        verificarFimPorKhan();
    }

    private void verificarFimPorKhan() {
        if (pontuacaoKhan >= consultarCasaFinal()) {
            pontuacaoKhan = consultarCasaFinal();
            if (rodadasNoDisparoFim < 0) {
                rodadasNoDisparoFim = rodadasConcluidas;
            }
            // A ficha branca atingiu a casa FIM: encerra imediatamente a partida.
            partidaEncerrada = true;
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
            Parada posicaoAtual = obterPeaoAtivo(jogador).getParadaAtual();
            if (posicaoAtual != null) {
                alcanceValido = jogo.getTabuleiro().saoAdjacentes(
                        posicaoAtual.getId(), idParada);
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
        return false; // A habilidade propria de Ogedei nao exige acesso a cidade.
    }

    private Cidade encontrarCidadeDisponivel(NomeCidade nome) {
        for (Cidade cidade : jogo.getTabuleiro().getCidadesDisponiveis()) {
            if (cidade.getNomeCidade() == nome) {
                return cidade;
            }
        }
        return null;
    }

    /** Distribui as fichas no momento em que a cidade e revelada. */
    private void revelarTesourosCidade(Cidade cidade) {
        if (cidade == null || cidade.getNomeCidade() == null
                || tesourosExpostosCidades.containsKey(cidade.getNomeCidade())) {
            return;
        }
        List<TipoTesouro> fichas = new ArrayList<TipoTesouro>();
        for (int i = 0; i < cidade.getQuantidadeTesouros(); i++) {
            TipoTesouro ficha = comprarTesouroDaPilha();
            if (ficha != null) {
                fichas.add(ficha);
            }
        }
        tesourosExpostosCidades.put(cidade.getNomeCidade(), fichas);
    }

    /** Fichas efetivamente visiveis sobre a cidade, em ordem. */
    public List<TipoTesouro> consultarTesourosCidade(String nomeCidade) {
        NomeCidade nome = interpretarCidade(nomeCidade);
        if (nome == null || encontrarCidadeDisponivel(nome) == null) {
            return Collections.emptyList();
        }
        Cidade cidade = encontrarCidadeDisponivel(nome);
        revelarTesourosCidade(cidade);
        return Collections.unmodifiableList(new ArrayList<TipoTesouro>(
                tesourosExpostosCidades.get(nome)));
    }

    /** Saque completo: valida visita, gasta espadas/moedas (Ogedei), entrega tesouro e revela nova cidade. */
    public boolean saquearCidadeAtual(String nomeCidade) {
        List<TipoTesouro> opcoes = consultarTesourosCidade(nomeCidade);
        if (opcoes.isEmpty()) {
            return false;
        }
        return saquearCidadeAtual(nomeCidade, opcoes.get(0));
    }

    /** Saque de ficha escolhida previamente entre as fichas expostas. */
    public boolean saquearCidadeAtual(String nomeCidade, TipoTesouro tipoEscolhido) {
        if (tipoEscolhido == null || !consultarTesourosCidade(nomeCidade).contains(tipoEscolhido)) {
            return false;
        }
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
        // A conquista deposita uma yurt. Nao permita exceder as 12 fichas do jogador.
        if (cidade.getQuantidadeTesouros() == 1
                && jogo.getTabuleiro().contarTotalYurtsNoMapa(jogadorAtual.getCor()) >= 12) {
            return false;
        }
        boolean saqueOk = jogadorAtual.saquearCidade(cidade, anteriores);
        if (!saqueOk) {
            return false;
        }

        acaoRealizadaNoTurno = true;
        saquesNaCidadeNoTurno.put(chave, anteriores + 1);
        // A ficha foi sorteada ao revelar a cidade; o jogador escolhe qual retirar.
        tesourosExpostosCidades.get(nome).remove(tipoEscolhido);
        tesourosPorJogador.get(jogadorAtual).add(tipoEscolhido);

        if (cidade.isConquistada()) {
            tesourosExpostosCidades.remove(nome);
            cidadesConquistadas.put(chave, jogadorAtual.getCor());
            jogo.getTabuleiro().registrarYurtConquista(nome, jogadorAtual.getCor());
            cidadesConquistadasPorJogador.put(
                    jogadorAtual, cidadesConquistadasPorJogador.get(jogadorAtual) + 1);

            Cidade proxima = jogo.revelarProximaCidade();
            while (proxima != null && proxima.getNomeCidade() != null
                    && cidadesConquistadas.containsKey(proxima.getNomeCidade().name())) {
                proxima = jogo.revelarProximaCidade();
            }
            if (proxima != null && proxima.getNomeCidade() != null && proxima.getRegiao() != null) {
                regiaoPorCidade.put(proxima.getNomeCidade(), proxima.getRegiao());
            }
            jogo.getTabuleiro().substituirCidadeConquistada(cidade, proxima);
            revelarTesourosCidade(proxima);

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

    /** Os icones do mapa possuem estoques independentes, inicializados com uma ficha. */
    private void inicializarEstoquesIconesSeNecessario() {
        if (!estoquesIconesTributos.isEmpty()) {
            return;
        }
        try {
            IconesTabuleiro mapa = IconesTabuleiro.carregar(
                    Paths.get("assets", "icones_tabuleiro.csv"));
            for (int i = 0; i < mapa.getTributos().size(); i++) {
                estoquesIconesTributos.add(1);
            }
        } catch (IOException erro) {
            throw new IllegalStateException("Nao foi possivel carregar os icones de tributo", erro);
        }
    }

    public int consultarQuantidadeTributoIcone(int indice) {
        inicializarEstoquesIconesSeNecessario();
        if (indice < 0 || indice >= estoquesIconesTributos.size()) {
            return 0;
        }
        return estoquesIconesTributos.get(indice);
    }

    /** Coleta somente da ficha clicada, sem consumir a de outro icone da mesma provincia. */
    public boolean coletarTributoDoIcone(int indice, Provincia[] provincias, String tipoEscolhido) {
        if (partidaEncerrada || provincias == null || tipoEscolhido == null) {
            return false;
        }
        inicializarEstoquesIconesSeNecessario();
        if (indice < 0 || indice >= estoquesIconesTributos.size()
                || estoquesIconesTributos.get(indice) <= 0) {
            return false;
        }
        TipoTributo tipo;
        try {
            tipo = TipoTributo.valueOf(tipoEscolhido);
        } catch (IllegalArgumentException erro) {
            return false;
        }
        try {
            IconesTabuleiro mapa = IconesTabuleiro.carregar(
                    Paths.get("assets", "icones_tabuleiro.csv"));
            IconesTabuleiro.IconeTributo icone = mapa.getTributos().get(indice);
            if (!icone.getTipo().equals(tipo.name())
                    || !java.util.Arrays.equals(icone.getProvincias(), provincias)) {
                return false;
            }
        } catch (IOException | IndexOutOfBoundsException erro) {
            return false;
        }
        boolean acesso = false;
        // Paradas cadastradas no editor prevalecem sobre o acesso por provincia.
        // Sem configuracao especifica, mantem-se integralmente a regra antiga.
        try {
            IconesTabuleiro.IconeTributo configurado = IconesTabuleiro.carregar(
                    Paths.get("assets", "icones_tabuleiro.csv")).getTributos().get(indice);
            if (!configurado.getParadasPermitidas().isEmpty()) {
                for (Integer parada : configurado.getParadasPermitidas()) {
                    if (paradasVisitadasTurno.contains(parada)) {
                        acesso = true;
                        break;
                    }
                }
            } else {
                for (Provincia provincia : provincias) {
                    ReservaProvincia reserva = reservas.get(provincia);
                    if (reserva != null && reserva.acessivelPelas(paradasVisitadasTurno)) {
                        acesso = true;
                        break;
                    }
                }
            }
        } catch (IOException | IndexOutOfBoundsException erro) {
            return false;
        }
        if (!acesso) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
        if (!cartela.podeGastarPunho() && bonusPunhosTurno <= 0) {
            return false;
        }
        estoquesIconesTributos.set(indice, estoquesIconesTributos.get(indice) - 1);
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

    private void reporIconesKhan(DestinoKhan destino) {
        inicializarEstoquesIconesSeNecessario();
        try {
            IconesTabuleiro mapa = IconesTabuleiro.carregar(
                    Paths.get("assets", "icones_tabuleiro.csv"));
            KhanRecursosLayout configuracao = KhanRecursosLayout.carregar();
            java.util.Set<Integer> indices = new java.util.HashSet<Integer>();
            // Apenas os tres icones explicitamente associados no editor podem ser repostos.
            // Sem configuracao nao inventamos reposicoes por provincias vizinhas.
            indices.addAll(configuracao.consultar(destino));
            for (Integer indice : indices) {
                if (indice != null && indice >= 0 && indice < estoquesIconesTributos.size()) {
                    int quantidade = estoquesIconesTributos.get(indice);
                    estoquesIconesTributos.set(indice, Math.min(3, quantidade + 1));
                }
            }
        } catch (IOException erro) {
            throw new IllegalStateException("Nao foi possivel repor os tributos do Khan", erro);
        }
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

    /** Escolha inicial de Jochi: uma das duas melhorias sorteadas vai para a branca. */
    public List<ResumoMelhoria> consultarOpcoesIniciaisJochi() {
        Jogador jogadorAtual = participantes.get(indiceAtual);
        if (jogadorAtual.getPersonagem() != Personagem.JOCHI) {
            return Collections.emptyList();
        }
        for (Jogador jogador : Collections.singletonList(jogadorAtual)) {
            List<Melhoria> opcoes = opcoesIniciaisJochi.get(jogador);
            if (opcoes != null && !opcoes.isEmpty()) {
                List<ResumoMelhoria> resumos = new ArrayList<ResumoMelhoria>();
                for (Melhoria melhoria : opcoes) {
                    resumos.add(new ResumoMelhoria(melhoria.getCodigo(),
                            melhoria.getDescdescricao(), 3));
                }
                return Collections.unmodifiableList(resumos);
            }
        }
        return Collections.emptyList();
    }

    public boolean escolherMelhoriaInicialJochi(int indiceOpcao) {
        if (partidaEncerrada) {
            return false;
        }
        for (Jogador jogador : Collections.singletonList(participantes.get(indiceAtual))) {
            if (jogador.getPersonagem() != Personagem.JOCHI) {
                return false;
            }
            List<Melhoria> opcoes = opcoesIniciaisJochi.get(jogador);
            if (opcoes == null || opcoes.isEmpty()) {
                continue;
            }
            if (indiceOpcao < 0 || indiceOpcao >= opcoes.size()) {
                return false;
            }
            Melhoria escolhida = opcoes.get(indiceOpcao);
            jogador.getTabuleiroIndividual().adicionarMelhoriaInicial(new Melhoria(
                    escolhida.getDescdescricao(), escolhida.getCustoEmMoedas(),
                    escolhida.getVotosBonus(), escolhida.getCodigo(), 3));
            for (int i = 0; i < opcoes.size(); i++) {
                if (i != indiceOpcao) {
                    mercadoMelhorias.devolverAoFundo(opcoes.get(i));
                }
            }
            opcoesIniciaisJochi.remove(jogador);
            notificarObservadores();
            return true;
        }
        return false;
    }

    /** Compra uma melhoria real do mercado por duas moedas. */
    public boolean comprarMelhoriaAtual(int posicao) {
        return comprarMelhoriaAtual(posicao, -1);
    }

    /** Jochi pode escolher qualquer coluna; os demais respeitam a cor original. */
    public boolean comprarMelhoriaAtual(int posicao, int colunaEscolhida) {
        Melhoria melhoria = mercadoMelhorias.consultar(posicao);
        if (melhoria == null || partidaEncerrada) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
        int coluna = melhoria.getColuna();
        if (colunaEscolhida >= 0) {
            if (jogador.getPersonagem() != Personagem.JOCHI || colunaEscolhida > 3) {
                return false;
            }
            coluna = colunaEscolhida;
        }
        if (!cartela.podeAdicionarMelhoria(coluna)) {
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
        if (coluna != retirada.getColuna()) {
            retirada = new Melhoria(retirada.getDescdescricao(),
                    retirada.getCustoEmMoedas(), retirada.getVotosBonus(),
                    retirada.getCodigo(), coluna);
        }
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
        aplicarEfeitoDestinoKhan(destino);
        cartela.concluirAcaoKhan();
        verificarCartasConselheiroAberto(participantes.get(indiceAtual));
        notificarObservadores();
        return true;
    }

    private void aplicarEfeitoDestinoKhan(DestinoKhan destino) {
        if (destino.ehProvincia()) {
            reporIconesKhan(destino);
            // Mantem o estoque legado de provincia para compatibilidade com testes anteriores.
            // Quando ha mapeamento manual, ele substitui a reposicao por provincia.
            try {
                KhanRecursosLayout configuracao = KhanRecursosLayout.carregar();
                if (configuracao.configurado(destino)) {
                    IconesTabuleiro icones = IconesTabuleiro.carregar(
                            Paths.get("assets", "icones_tabuleiro.csv"));
                    for (Integer indice : configuracao.consultar(destino)) {
                        if (indice >= icones.getTributos().size()) {
                            continue;
                        }
                        IconesTabuleiro.IconeTributo icone = icones.getTributos().get(indice);
                        TipoTributo recurso = TipoTributo.valueOf(icone.getTipo());
                        for (Provincia provincia : icone.getProvincias()) {
                            ReservaProvincia reserva = reservas.get(provincia);
                            if (reserva != null) {
                                reserva.reporUma(recurso);
                            }
                        }
                    }
                    return;
                }
            } catch (IOException erro) {
                throw new IllegalStateException("Mapeamento dos recursos do Khan invalido", erro);
            }
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
        return entregarTesourosConselheiro(idParadaConselheiro, null);
    }

    /** Para conselheiros de demanda livre, consome precisamente as tres fichas escolhidas. */
    public boolean entregarTesourosConselheiro(int idParadaConselheiro,
                                                List<TipoTesouro> escolha) {
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
        TabuleiroIndividual cartela = atual.getTabuleiroIndividual();
        Map<TipoTesouro, Integer> virtuais = cartela.consultarTesourosVirtuais();
        if (mao == null || !podePagarTesouros(alvo, mao, virtuais)) {
            return false;
        }
        if (alvo.getTesourosExigidos() == null) {
            // Uma entrega livre precisa indicar quais fichas serao descartadas.
            // Nao se admite mais consumir automaticamente as primeiras da mao.
            if (escolha == null || escolha.size() != 3 || escolha.contains(null)) {
                return false;
            }
            List<TipoTesouro> restante = new ArrayList<TipoTesouro>(mao);
            Map<TipoTesouro, Integer> utilizados = new EnumMap<TipoTesouro, Integer>(TipoTesouro.class);
            for (TipoTesouro tipo : escolha) {
                if (!restante.remove(tipo)) {
                    int usado = utilizados.getOrDefault(tipo, 0);
                    if (usado >= virtuais.getOrDefault(tipo, 0)) return false;
                    utilizados.put(tipo, usado + 1);
                }
            }
            for (TipoTesouro tipo : escolha) {
                if (mao.remove(tipo)) descarteTesouros.add(tipo);
            }
            cartela.consumirTesourosVirtuais(utilizados);
        } else {
            List<TipoTesouro> restantes = new ArrayList<TipoTesouro>(mao);
            Map<TipoTesouro, Integer> utilizados = new EnumMap<TipoTesouro, Integer>(TipoTesouro.class);
            for (TipoTesouro tipo : alvo.getTesourosExigidos()) {
                if (!restantes.remove(tipo)) {
                    utilizados.put(tipo, utilizados.getOrDefault(tipo, 0) + 1);
                }
            }
            // Todos os requisitos ja foram validados antes de alterar inventarios.
            for (TipoTesouro tipo : alvo.getTesourosExigidos()) {
                if (mao.remove(tipo)) descarteTesouros.add(tipo);
            }
            cartela.consumirTesourosVirtuais(utilizados);
        }
        acaoRealizadaNoTurno = true;
        concederVotosAoJogador(atual, alvo.getVotosConcedidos());

        if (alvo.temPecaBonus()) {
            PecaBonus recebida = alvo.recolherPecaBonus();
            bonusPorJogador.get(atual).add(recebida);
            if (recebida == PecaBonus.VOTO_IMEDIATO) {
                concederVotosAoJogador(atual, 1);
                bonusUtilizados.get(atual).add(recebida);
            }
            bonusRecebidosPorJogador.put(atual,
                    bonusRecebidosPorJogador.getOrDefault(atual, 0) + 1);
        }

        verificarCartasConselheiroAberto(atual);
        notificarObservadores();
        return true;
    }

    /** Consulta somente de leitura: a View nao altera o estado dos bonus. */
    public List<PecaBonus> consultarBonusJogadorAtual() {
        return Collections.unmodifiableList(new ArrayList<PecaBonus>(
                bonusPorJogador.get(participantes.get(indiceAtual))));
    }

    public boolean isBonusUtilizadoJogadorAtual(int indice) {
        List<PecaBonus> lista = bonusPorJogador.get(participantes.get(indiceAtual));
        return indice >= 0 && indice < lista.size()
                && bonusUtilizados.get(participantes.get(indiceAtual)).contains(lista.get(indice));
    }

    public int consultarTotalBonusAdquiridosJogadorAtual() {
        return bonusRecebidosPorJogador.getOrDefault(participantes.get(indiceAtual), 0);
    }

    public List<TipoTesouro> consultarEscolhaTesourosBonusPendente() {
        return Collections.unmodifiableList(new ArrayList<TipoTesouro>(escolhaTesourosBonusPendente));
    }

    public boolean escolherTesouroBonus(int indice) {
        if (indice < 0 || indice >= escolhaTesourosBonusPendente.size() || partidaEncerrada) return false;
        Jogador atual = participantes.get(indiceAtual);
        tesourosPorJogador.get(atual).add(escolhaTesourosBonusPendente.get(indice));
        for (int i = 0; i < escolhaTesourosBonusPendente.size(); i++) {
            if (i != indice) descarteTesouros.add(escolhaTesourosBonusPendente.get(i));
        }
        escolhaTesourosBonusPendente.clear();
        bonusUtilizados.get(atual).add(PecaBonus.ESCOLHER_TESOURO);
        verificarCartasConselheiroAberto(atual);
        notificarObservadores();
        return true;
    }

    public boolean usarPecaBonusJogadorAtual(int indiceBonus) {
        if (partidaEncerrada || !escolhaTesourosBonusPendente.isEmpty()) return false;
        Jogador atual = participantes.get(indiceAtual);
        List<PecaBonus> lista = bonusPorJogador.get(atual);
        if (lista == null || indiceBonus < 0 || indiceBonus >= lista.size()) return false;
        PecaBonus escolhida = lista.get(indiceBonus);
        if (bonusUtilizados.get(atual).contains(escolhida)) return false;
        switch (escolhida) {
            case VOTO_IMEDIATO:
                return false; // Efeito automatico no recebimento.
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
                bonusPunhosTurno++;
                break;
            case DOIS_PUNHOS:
                bonusPunhosTurno += 2;
                break;
            case ESCOLHER_TESOURO:
                for (int i = 0; i < 3; i++) {
                    TipoTesouro comprado = comprarTesouroDaPilha();
                    if (comprado == null) break;
                    escolhaTesourosBonusPendente.add(comprado);
                }
                if (escolhaTesourosBonusPendente.isEmpty()) return false;
                notificarObservadores();
                return true; // Consumo so ocorre depois da escolha obrigatoria.
            default:
                return false;
        }
        bonusUtilizados.get(atual).add(escolhida);
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
            boolean podeEntregar = presente && podePagarTesouros(local, mao,
                    atual.getTabuleiroIndividual().consultarTesourosVirtuais());
            String descBonus = local.temPecaBonus()
                    ? local.getPecaBonus().getDescricao()
                    : "Ja recolhido";

            resultado.add(new ResumoConselheiro(
                    local.getIdParada(),
                    local.getNome(),
                    local.getDescricaoDemanda(),
                    local.getVotosConcedidos(),
                    descBonus,
                    local.temPecaBonus() ? local.getPecaBonus().getArquivoImagem() : null,
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
            bonusDesc.add(b.getDescricao() + (bonusUtilizados.get(atual).contains(b) ? " [UTILIZADA]" : " [DISPONIVEL]"));
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

    private List<Integer> consultarIdsPontosEntrega() {
        List<Integer> ids = new ArrayList<Integer>();
        for (LocalConselheiro local : locaisConselheiros) {
            ids.add(local.getIdParada());
        }
        return ids;
    }

    /** Consulta somente as opcoes do jogador atual. A carta escolhida continua secreta. */
    public List<CartaConselheiroSecreto> consultarOpcoesConselheiroSecretoJogadorAtual() {
        Jogador jogador = participantes.get(indiceAtual);
        if (secretaPorJogador.containsKey(jogador)) {
            return Collections.emptyList();
        }
        List<CartaConselheiroSecreto> opcoes = opcoesSecretasPorJogador.get(jogador);
        if (opcoes == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<CartaConselheiroSecreto>(opcoes));
    }

    public CartaConselheiroSecreto consultarConselheiroSecretoJogadorAtual() {
        return secretaPorJogador.get(participantes.get(indiceAtual));
    }

    /** Progresso atual do objetivo secreto do jogador da vez, sem conceder votos. */
    public int consultarProgressoSecretoJogadorAtual() {
        Jogador atual = participantes.get(indiceAtual);
        CartaConselheiroSecreto carta = secretaPorJogador.get(atual);
        if (carta == null) {
            return 0;
        }
        List<ResumoMelhoria> melhorias = consultarCartela(indiceAtual).getMelhoriasDetalhadas();
        return AvaliadorPontuacaoFinal.progressoConselheiroSecreto(
                atual, carta, melhorias, jogo.getTabuleiro(), regiaoPorParada,
                regiaoPorCidade, paradasPorProvinciaKhan, paradasPorCidade,
                consultarIdsPontosEntrega());
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
                paradasPorCidade,
                consultarIdsPontosEntrega());
    }

    /** Cartas reveladas sao verificadas automaticamente, inclusive requisitos ja ultrapassados. */
    private void revelarCartaAberta(CartaConselheiroAberto carta) {
        cartasAbertasAtivas.add(carta);
        Set<Integer> elegiveis = new HashSet<Integer>();
        for (int i = 0; i < participantes.size(); i++) elegiveis.add(i);
        elegiveisAbertas.put(carta, elegiveis);
    }

    /** Consulta publica para a View, mantendo a colecao interna protegida. */
    public List<CartaConselheiroAberto> consultarCartasAbertasAtivas() {
        return Collections.unmodifiableList(new ArrayList<CartaConselheiroAberto>(cartasAbertasAtivas));
    }

    public int consultarQuantidadeCartasAbertasNaPilha() {
        return pilhaCartasAbertas.size();
    }

    /** O reconhecimento ocorre logo apos cada acao que altera o requisito. */
    private void verificarCartasConselheiroAbertoParaTodos() {
        for (Jogador participante : participantes) {
            if (!cartasAbertasAtivas.isEmpty()) {
                verificarCartasConselheiroAberto(participante);
            }
        }
    }

    private void verificarCartasConselheiroAberto(Jogador jogador) {
        if (partidaEncerrada || jogador == null) {
            return;
        }
        int indice = participantes.indexOf(jogador);
        if (indice < 0) {
            return;
        }
        // A rodada de avaliacao trata somente as cartas ja reveladas no instante da acao.
        List<CartaConselheiroAberto> candidatas =
                new ArrayList<CartaConselheiroAberto>(cartasAbertasAtivas);
        List<CartaConselheiroAberto> concluidas = new ArrayList<CartaConselheiroAberto>();
        for (CartaConselheiroAberto carta : candidatas) {
            Set<Integer> elegiveis = elegiveisAbertas.get(carta);
            if (elegiveis != null && elegiveis.contains(indice)
                    && cumpreCartaAberta(carta, jogador)) {
                concluidas.add(carta);
            }
        }
        // Concede todas as recompensas da mesma acao antes de revelar substitutas.
        for (CartaConselheiroAberto carta : concluidas) {
            if (cartasAbertasAtivas.remove(carta)) {
                elegiveisAbertas.remove(carta);
                concederVotosAoJogador(jogador, 1);
            }
        }
        for (int i = 0; i < concluidas.size() && !pilhaCartasAbertas.isEmpty(); i++) {
            revelarCartaAberta(pilhaCartasAbertas.remove(0));
        }
        // Uma carta nova pode ter seu requisito ja satisfeito: premiar sem clique adicional.
        if (!concluidas.isEmpty()) {
            verificarCartasConselheiroAbertoParaTodos();
        }
    }

    private boolean cumpreCartaAberta(CartaConselheiroAberto carta, Jogador jogador) {
        int indice = participantes.indexOf(jogador);
        if (indice < 0) {
            return false;
        }
        List<ResumoMelhoria> melhorias = consultarCartela(indice).getMelhoriasDetalhadas();
        List<TipoTesouro> tesouros = tesourosPorJogador.get(jogador);
        Map<TipoTesouro, Integer> virtuaisAtivos =
                jogador.getTabuleiroIndividual().consultarTesourosVirtuais();
        int[] colunas = new int[4];
        for (ResumoMelhoria melhoria : melhorias) {
            if (melhoria.getColuna() >= 0 && melhoria.getColuna() < 4) {
                colunas[melhoria.getColuna()]++;
            }
        }
        Cor cor = jogador.getCor();
        Tabuleiro tabuleiro = jogo.getTabuleiro();
        switch (carta) {
            case QUATRO_ESPADAS:
                return jogador.quantidadeTributo(TipoTributo.ESPADA) >= 4;
            case SEIS_YURTS:
                return tabuleiro.contarTotalYurtsNoMapa(cor) >= 6;
            case DEZ_YURTS:
                return tabuleiro.contarTotalYurtsNoMapa(cor) >= 10;
            case QUATRO_TESOUROS:
                int total = tesouros == null ? 0 : tesouros.size();
                for (int quantidade : virtuaisAtivos.values()) total += quantidade;
                return total >= 4;
            case CINCO_TESOUROS_DIFERENTES:
                Set<TipoTesouro> tipos = new HashSet<TipoTesouro>();
                if (tesouros != null) tipos.addAll(tesouros);
                for (Map.Entry<TipoTesouro, Integer> entrada : virtuaisAtivos.entrySet()) {
                    if (entrada.getValue() > 0) tipos.add(entrada.getKey());
                }
                return tipos.size() >= 5;
            case UM_YURT_CADA_REGIAO:
                return yurtsNormaisNasTresRegioes(cor, 1);
            case DOIS_YURTS_CADA_REGIAO:
                return yurtsNormaisNasTresRegioes(cor, 2);
            case DOIS_BONUS:
                return bonusRecebidosPorJogador.getOrDefault(jogador, 0) >= 2;
            case TRES_BONUS:
                return bonusRecebidosPorJogador.getOrDefault(jogador, 0) >= 3;
            case DUAS_CIDADES:
                return cidadesConquistadasPorJogador.getOrDefault(jogador, 0) >= 2;
            case TRES_CIDADES:
                return cidadesConquistadasPorJogador.getOrDefault(jogador, 0) >= 3;
            case UMA_LINHA_MELHORIAS:
                return colunas[0] >= 1 && colunas[1] >= 1 && colunas[2] >= 1;
            case DUAS_COLUNAS_DUAS_MELHORIAS: {
                int quantidade = 0;
                for (int coluna : colunas) {
                    if (coluna >= 2) {
                        quantidade++;
                    }
                }
                return quantidade >= 2;
            }
            case QUATRO_MELHORIAS_COLUNA:
                for (int coluna : colunas) {
                    if (coluna >= 4) {
                        return true;
                    }
                }
                return false;
            default:
                return false;
        }
    }

    private boolean yurtsNormaisNasTresRegioes(Cor cor, int minimo) {
        Tabuleiro tabuleiro = jogo.getTabuleiro();
        return tabuleiro.contarYurtsNormaisNaRegiao(cor, Regiao.CHINA, regiaoPorParada) >= minimo
                && tabuleiro.contarYurtsNormaisNaRegiao(cor, Regiao.PERSIA, regiaoPorParada) >= minimo
                && tabuleiro.contarYurtsNormaisNaRegiao(cor, Regiao.RUSSIA, regiaoPorParada) >= minimo;
    }

}