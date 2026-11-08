package br.herdeiros.model;

import java.util.ArrayList;
import java.util.List;
import br.herdeiros.mapa.MapaDados;

/** API mínima para a interface da segunda iteração; não expõe as classes internas. */
public class InterfacePartida implements ObservadoPartida {
    private final Jogo jogo;
    private final List<Jogador> participantes;
    private final List<ObservadorPartida> observadores = new ArrayList<>();

    /** Construtor padrao usado pelos testes de integracao do Model. */
    public InterfacePartida() {
        this(new String[] {"Azul", "Vermelho"},
                new Cor[] {Cor.AZUL, Cor.VERMELHO});
    }

    public InterfacePartida(String[] nomes, Cor[] cores) {
        if (nomes == null || cores == null || nomes.length == 0 || nomes.length != cores.length) {
            throw new IllegalArgumentException("Cada jogador deve ter nome e cor");
        }
        participantes = new ArrayList<>();
        for (int i = 0; i < nomes.length; i++) {
            participantes.add(new Jogador(nomes[i], cores[i], 0));
        }
        jogo = new Jogo(participantes);
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
        Peao peao = participantes.get(indiceAtual).getMeuPeao();
        boolean realizado = jogo.getTabuleiro().moverPeaoAte(peao, destino, movimentosDisponiveis);
        if (realizado) {
            notificarObservadores();
        }
        return realizado;
    }

    /** Passa para o próximo participante na ordem de cadastro. */
    public void avancarJogador() {
        indiceAtual++;
        if (indiceAtual >= participantes.size()) {
            indiceAtual = 0;
        }
        notificarObservadores();
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

        for (Melhoria melhoria : tabuleiro.getMelhoriasAdquiridas()) {
            if (melhoria != null) {
                descricoes.add(melhoria.getDescdescricao());
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
                descricoes
        );
    }

    public ResumoCartela consultarCartelaJogadorAtual() {
        return consultarCartela(indiceAtual);
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

    public int getOcupacao(int id) {
        return jogo.getTabuleiro().getOcupacao(id);
    }
}
