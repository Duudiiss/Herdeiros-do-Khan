package br.herdeiros.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import br.herdeiros.mapa.MapaDados;

/** API mínima para a interface da segunda iteração; não expõe as classes internas. */
public class InterfacePartida implements ObservadoPartida {
    private final Jogo jogo;
    private final MercadoMelhorias mercadoMelhorias;
    private final List<Jogador> participantes;
    private final List<ObservadorPartida> observadores = new ArrayList<>();
    private final Map<String, Set<Integer>> acessosCidades = new HashMap<String, Set<Integer>>();
    private final Set<Integer> paradasVisitadasTurno = new HashSet<Integer>();
    private final Map<String, Integer> saquesNaCidadeNoTurno = new HashMap<String, Integer>();
    private final Map<String, Cor> cidadesConquistadas = new HashMap<String, Cor>();
    private final Map<Provincia, ReservaProvincia> reservas = new HashMap<Provincia, ReservaProvincia>();
    private final Map<Provincia, Provincia[]> vizinhasDoKhan = new HashMap<Provincia, Provincia[]>();
    private int rodadasConcluidas;
    private int pontuacaoKhan;
    private int rodadasNoDisparoFim = -1;
    private boolean partidaEncerrada;

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
            if (ponto.getId() != 0 && !jogo.getTabuleiro().adicionarParada(
                    ponto.getId(), ponto.isDupla())) {
                return false;
            }
        }
        for (MapaDados.Ligacao ligacao : mapa.getLigacoes()) {
            if (!jogo.getTabuleiro().conectarParadas(ligacao.getOrigem(), ligacao.getDestino())) {
                return false;
            }
        }
        registrarParadaAtualNoTurno();
        notificarObservadores();
        return true;
    }

    public boolean cadastrarParada(int id, boolean dupla) {
        boolean adicionado = jogo.getTabuleiro().adicionarParada(id, dupla);
        if (adicionado) {
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

    public boolean movimentarJogador(int indice, int destino) {
        if (indice < 0 || indice >= participantes.size()) {
            return false;
        }
        boolean realizado = jogo.getTabuleiro().moverPeao(participantes.get(indice).getMeuPeao(), destino);
        if (realizado) {
            notificarObservadores();
        }
        return realizado;
    }

    /** Consulta o custo minimo do destino para o jogador da vez, sem movimentar. */
    public int consultarCustoMovimentoAtual(int destino) {
        Jogador jogador = participantes.get(indiceAtual);
        return jogo.getTabuleiro().distanciaMinima(
                jogador.getMeuPeao(), destino, jogador.getCor());
    }

    private int indiceAtual = 0;

    /** Jogador ativo da rodada visual. */
    public int getIndiceJogadorAtual() {
        return indiceAtual;
    }

    /**
     * Executa uma ação de movimento com o número de movimentos informado.
     * O seletor da interface é provisório, até implementar a ativação de cavalos.
     * A passagem da vez e responsabilidade do Controller, nao deste metodo.
     */
    public boolean movimentarJogadorAtual(int destino, int movimentosDisponiveis) {
        Jogador jogador = participantes.get(indiceAtual);
        Peao peao = jogador.getMeuPeao();
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

    /** Movimentacao cujo saldo e calculado e debitado no proprio Model. */
    public boolean movimentarJogadorComAtivacao(int destino) {
        if (partidaEncerrada) {
            return false;
        }
        Jogador jogador = participantes.get(indiceAtual);
        TabuleiroIndividual cartela = jogador.getTabuleiroIndividual();
        ResumoAtivacao ativacao = cartela.consultarAtivacao(jogador.getPersonagem());
        int custo = consultarCustoMovimentoAtual(destino);

        if (custo <= 0 || custo > ativacao.getMovimentosRestantes()) {
            return false;
        }
        List<Integer> percurso = jogo.getTabuleiro().consultarCaminho(
                jogador.getMeuPeao(), destino, jogador.getCor());
        boolean realizado = jogo.getTabuleiro().moverPeaoAte(
                jogador.getMeuPeao(), destino, ativacao.getMovimentosRestantes(), jogador.getCor());
        if (!realizado) {
            return false;
        }

        paradasVisitadasTurno.addAll(percurso);
        cartela.consumirMovimentos(custo);
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
        Parada parada = participantes.get(indice).getMeuPeao().getParadaAtual();
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
     *
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

    /** A ficha branca avanca sempre que qualquer jogador ganha votos. */
    public boolean registrarVotosConquistados(int quantidade) {
        if (quantidade <= 0 || partidaEncerrada) {
            return false;
        }
        participantes.get(indiceAtual).ganharVotos(quantidade);
        pontuacaoKhan += quantidade;
        if (rodadasNoDisparoFim < 0
                && pontuacaoKhan >= consultarCasaFinal()) {
            rodadasNoDisparoFim = rodadasConcluidas;
        }
        notificarObservadores();
        return true;
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
        if (!paradasVisitadasTurno.contains(idParada)) {
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
        int custo = 1;
        if (anteriores > 0) {
            custo = 2;
        }
        Jogador jogadorAtual = participantes.get(indiceAtual);
        return jogadorAtual.quantidadeTributo(TipoTributo.ESPADA)
                + jogadorAtual.getTabuleiroIndividual().getQuantidadeEspadasVirtuais() >= custo;
    }

    private Cidade encontrarCidadeDisponivel(NomeCidade nome) {
        for (Cidade cidade : jogo.getTabuleiro().getCidadesDisponiveis()) {
            if (cidade.getNomeCidade() == nome) {
                return cidade;
            }
        }
        return null;
    }

    /** Saque completo: valida visita, gasta espadas e revela nova cidade ao conquistar. */
    public boolean saquearCidadeAtual(String nomeCidade) {
        if (partidaEncerrada || !podeSaquearCidadeAtual(nomeCidade)) {
            return false;
        }
        NomeCidade nome = interpretarCidade(nomeCidade);
        Cidade cidade = encontrarCidadeDisponivel(nome);
        String chave = nome.name();
        int anteriores = 0;
        if (saquesNaCidadeNoTurno.containsKey(chave)) {
            anteriores = saquesNaCidadeNoTurno.get(chave);
        }
        if (!participantes.get(indiceAtual).saquearCidade(cidade, anteriores)) {
            return false;
        }
        saquesNaCidadeNoTurno.put(chave, anteriores + 1);
        if (cidade.isConquistada()) {
            cidadesConquistadas.put(chave, participantes.get(indiceAtual).getCor());
            Cidade proxima = jogo.revelarProximaCidade();
            jogo.getTabuleiro().substituirCidadeConquistada(cidade, proxima);
        }
        notificarObservadores();
        return true;
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

    /**
     * Recolhe um tributo de uma província acessada neste turno, gastando um punho.
     * Não permite coletar por paradas não visitadas ou quando a reserva está vazia.
     */
    /**
     * Permite escolher explicitamente qual peça de tributo recolher com um punho.
     * A reserva atual de cada província possui um único tipo, conforme o CSV.
     * Uma escolha diferente nunca consome estoque ou punhos.
     */
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

    /**
     * Coleta de uma pilha desenhada na divisa de provincias.
     * O jogador precisa ter acesso a pelo menos uma provincia do icone.
     * A pilha mostra a soma dos estoques das provincias indicadas;
     * por isso pode retirar uma peca de qualquer uma delas, inclusive
     * apos retirar outra da mesma pilha com um punho anterior.
     * A verificacao ocorre antes da alteracao, para preservar o estado
     * em tentativas recusadas.
     */
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
        if (!cartela.podeGastarPunho()) {
            return false;
        }

        if (!reservaComEstoque.retirar(tipo)) {
            return false;
        }

        jogador.receberTributos(tipo, 1);
        cartela.gastarPunho();
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
        if (!cartela.podeGastarPunho() || reserva.quantidade(tipo) <= 0) {
            return false;
        }
        if (!reserva.retirar(tipo)) {
            return false;
        }
        jogador.receberTributos(tipo, 1);
        cartela.gastarPunho();
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
            // A area impressa possui espacos de bonus (moeda/espada).
            // A ocupacao individual desses espacos ainda sera modelada.
            participantes.get(indiceAtual).receberTributos(TipoTributo.MOEDA, 1);
            participantes.get(indiceAtual).receberTributos(TipoTributo.ESPADA, 1);
        }
        cartela.concluirAcaoKhan();
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
}
