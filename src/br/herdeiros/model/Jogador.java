package br.herdeiros.model;

import java.io.Serializable;

/** Representa um jogador, seus recursos e os votos acumulados. */
class Jogador implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nome;
    private final Cor cor;
    private final Personagem personagem;
    private final Peao meuPeao;
    private final int quantidadeDescendentes;
    private int votosKurultai;
    private final TabuleiroIndividual tabuleiroIndividual;

    public Jogador(String nome, Cor cor, int quantidadeDescendentes) {
        this(nome, cor, quantidadeDescendentes, null);
    }

    public Jogador(String nome, Cor cor, int quantidadeDescendentes, Personagem personagem) {
        this(nome, cor, quantidadeDescendentes, personagem, new TabuleiroIndividual(), 0);
    }

    Jogador(String nome, Cor cor, int quantidadeDescendentes, Personagem personagem,
            TabuleiroIndividual tabuleiroIndividual, int votosKurultai) {
        if (nome == null || nome.trim().isEmpty() || cor == null || cor == Cor.BRANCO
                || quantidadeDescendentes < 0 || tabuleiroIndividual == null
                || votosKurultai < 0) {
            throw new IllegalArgumentException("Dados inválidos do jogador");
        }

        this.nome = nome;
        this.cor = cor;
        this.personagem = personagem;
        this.quantidadeDescendentes = quantidadeDescendentes;
        this.meuPeao = new Peao(cor);
        this.tabuleiroIndividual = tabuleiroIndividual;
        this.votosKurultai = votosKurultai;
    }

    public void ganharVotos(int quantidade) {
        if (quantidade > 0) {
            votosKurultai += quantidade;
        }
    }

    public String getNome() {
        return nome;
    }

    public Personagem getPersonagem() {
        return personagem;
    }

    public Cor getCor() {
        return cor;
    }

    public int getQuantidadeDescendentes() {
        return quantidadeDescendentes;
    }

    public int getVotosKurultai() {
        return votosKurultai;
    }

    public Peao getMeuPeao() {
        return meuPeao;
    }

    public TabuleiroIndividual getTabuleiroIndividual() {
        return tabuleiroIndividual;
    }

    public int quantidadeTributo(TipoTributo tipo) {
        if (tipo == null) {
            return 0;
        }
        return tabuleiroIndividual.getQuantidadeTributoDoTipo(tipo);
    }

    public boolean receberTributos(TipoTributo tipo, int quantidade) {
        if (tipo == null || quantidade <= 0) {
            return false;
        }

        for (int i = 0; i < quantidade; i++) {
            tabuleiroIndividual.adicionarTributo(new Tributo(tipo));
        }
        return true;
    }

    /**
     * Realiza um saque quando há espadas suficientes.
     * O primeiro saque da cidade no turno custa uma espada; os demais custam duas.
     * O histórico de saques do turno é informado pelo chamador.
     * Em caso de falha, o estado da cidade e as espadas não são alterados.
     */
    public boolean saquearCidade(Cidade cidade, int saquesNaCidadeNesteTurno) {
        if (cidade == null || saquesNaCidadeNesteTurno < 0 || cidade.isConquistada()) {
            return false;
        }

        int custo;
        if (saquesNaCidadeNesteTurno == 0) {
            custo = 1;
        } else {
            custo = 2;
        }

        int espadasVirtuais = tabuleiroIndividual.getQuantidadeEspadasVirtuais();
        int espadasDisponiveis = quantidadeTributo(TipoTributo.ESPADA) + espadasVirtuais;

        if (espadasDisponiveis < custo) {
            return false;
        }

        boolean saqueRealizado = cidade.saquearTesouro();
        if (!saqueRealizado) {
            return false;
        }

        if (!tabuleiroIndividual.gastarTributoComVirtuais(TipoTributo.ESPADA, custo)) {
            throw new IllegalStateException("Pagamento de espadas mudou durante o saque");
        }
        return true;
    }
}
