package br.herdeiros.model;

import java.util.ArrayList;
import java.util.List;

public class TabuleiroIndividual {
    private final List<Tributo> tributosArmazenados;
    private final List<Melhoria> melhoriasAdquiridas;
    private final int quantidadeColunas;
    private final boolean[] colunasOcupadas;
    private int colunaAtivaNoTurno;
    private int movimentosRestantes;
    private int punhosDaColuna;
    private int moedasVirtuaisDaColuna;
    private int espadasVirtuaisDaColuna;
    private int yurtsVirtuaisDaColuna;
    private int cavalosVirtuaisDaColuna;
    private int punhosVirtuaisDaColuna;
    private int linhasBrancasPendentes;
    private boolean acaoKhan;
    private boolean acaoMelhoria;

    public TabuleiroIndividual() {
        this.tributosArmazenados = new ArrayList<>();
        this.melhoriasAdquiridas = new ArrayList<>();
        this.quantidadeColunas = 4; // Quatro colunas fixas das cartelas oficiais.
        this.colunasOcupadas = new boolean[4];
        this.colunaAtivaNoTurno = -1;
        this.movimentosRestantes = 0;
    }

    /**
     * A coluna ativada nao pode ser reutilizada ate a recuperacao branca.
     * A branca libera todas as pecas ao fim da propria ativacao.
     */
    boolean ativarColuna(int coluna, Personagem personagem) {
        if (coluna < 0 || coluna >= quantidadeColunas || colunaAtivaNoTurno >= 0) {
            return false;
        }
        if (colunasOcupadas[coluna]) {
            return false;
        }

        AcoesColuna acoes = AcoesColuna.obter(personagem, coluna);
        colunasOcupadas[coluna] = true;
        colunaAtivaNoTurno = coluna;
        // Os efeitos sao calculados ao ativar a coluna e nao mudam se
        // alguma melhoria for comprada durante este mesmo turno.
        cavalosVirtuaisDaColuna = contarMelhoriasDaColuna("CAV", coluna) * 2;
        punhosVirtuaisDaColuna = contarMelhoriasDaColuna("MAO", coluna);
        movimentosRestantes = acoes.getCavalos() + cavalosVirtuaisDaColuna;
        punhosDaColuna = acoes.getPunhos() + punhosVirtuaisDaColuna;
        moedasVirtuaisDaColuna = acoes.getMoedasVirtuais()
                + contarMelhoriasDaColuna("MOE", coluna);
        espadasVirtuaisDaColuna = contarMelhoriasDaColuna("ESP", coluna);
        yurtsVirtuaisDaColuna = contarMelhoriasDaColuna("CAS", coluna);
        acaoKhan = acoes.temKhan();
        acaoMelhoria = acoes.temMelhoria();

        if (acoes.recuperaPecas()) {
            for (int i = 0; i < colunasOcupadas.length; i++) {
                colunasOcupadas[i] = false;
            }
        }
        return true;
    }

    boolean consumirMovimentos(int quantidade) {
        if (colunaAtivaNoTurno < 0 || quantidade <= 0
                || quantidade > movimentosRestantes) {
            return false;
        }
        movimentosRestantes -= quantidade;
        return true;
    }

    boolean podeGastarPunho() {
        return colunaAtivaNoTurno >= 0 && punhosDaColuna > 0;
    }

    boolean gastarPunho() {
        if (!podeGastarPunho()) {
            return false;
        }
        punhosDaColuna--;
        return true;
    }

    boolean deveMoverKhan() {
        return colunaAtivaNoTurno >= 0 && acaoKhan;
    }

    boolean concluirAcaoKhan() {
        if (!deveMoverKhan()) {
            return false;
        }
        acaoKhan = false;
        return true;
    }

    void encerrarTurno() {
        colunaAtivaNoTurno = -1;
        movimentosRestantes = 0;
        punhosDaColuna = 0;
        moedasVirtuaisDaColuna = 0;
        espadasVirtuaisDaColuna = 0;
        yurtsVirtuaisDaColuna = 0;
        cavalosVirtuaisDaColuna = 0;
        punhosVirtuaisDaColuna = 0;
        acaoKhan = false;
        acaoMelhoria = false;
    }

    ResumoAtivacao consultarAtivacao(Personagem personagem) {
        int[] cavalos = new int[4];
        for (int i = 0; i < cavalos.length; i++) {
            cavalos[i] = AcoesColuna.obter(personagem, i).getCavalos();
        }
        return new ResumoAtivacao(colunasOcupadas, colunaAtivaNoTurno,
                movimentosRestantes, punhosDaColuna, acaoKhan,
                acaoMelhoria, cavalos, moedasVirtuaisDaColuna);
    }

    public void adicionarTributo(Tributo tributo) {
        this.tributosArmazenados.add(tributo);
    }

    public boolean removerTributoPorTipo(TipoTributo tipo) {
        for (Tributo tributo : tributosArmazenados) {
        	Boolean tributoEncontrado = tributo.getTipo() == tipo;
        	
            if (tributoEncontrado) {
                tributosArmazenados.remove(tributo);
                return true;
            }
        }
        return false;
    }

    public int getQuantidadeTributoDoTipo(TipoTributo tipo) {
        int quantidade = 0;

        for (Tributo tributo : tributosArmazenados) {
            if (tributo.getTipo() == tipo) {
                quantidade++;
            }
        }
        return quantidade;
    }

    public boolean removerTributosPorTipo(TipoTributo tipo, int quantidade) {
        boolean removidos = false;

        if (tipo != null && quantidade > 0 && getQuantidadeTributoDoTipo(tipo) >= quantidade) {
            for (int i = 0; i < quantidade; i++) {
                removerTributoPorTipo(tipo);
            }
            removidos = true;
        }

        return removidos;
    }

    public void adicionarMelhoria(Melhoria melhoria) {
        if (melhoria == null) {
            return;
        }
        int coluna = melhoria.getColuna();
        if (coluna == -1) {
            // Melhoria sem codigo/coluna criada pelos testes antigos do Model.
            melhoriasAdquiridas.add(melhoria);
            return;
        }
        if (coluna < 0 || coluna > 3) {
            return;
        }
        int linhasCompletasAntes = contarLinhasCompletas();
        this.melhoriasAdquiridas.add(melhoria);
        int novasLinhas = contarLinhasCompletas() - linhasCompletasAntes;
        if (coluna != 3 && novasLinhas > 0) {
            linhasBrancasPendentes += novasLinhas;
        }
    }

    private int contarLinhasCompletas() {
        int minimo = 4;
        for (int coluna = 0; coluna < 3; coluna++) {
            int quantidade = contarMelhoriasNaColuna(coluna);
            if (quantidade < minimo) {
                minimo = quantidade;
            }
        }
        return minimo;
    }

    public int getLinhasBrancasPendentes() {
        return linhasBrancasPendentes;
    }

    boolean podeReceberMelhoriaBranca() {
        return linhasBrancasPendentes > 0 && contarMelhoriasNaColuna(3) < 4;
    }

    boolean receberMelhoriaBranca(Melhoria melhoria) {
        if (melhoria == null || !podeReceberMelhoriaBranca()) {
            return false;
        }
        Melhoria branca = new Melhoria(melhoria.getDescdescricao(),
                melhoria.getCustoEmMoedas(), melhoria.getVotosBonus(),
                melhoria.getCodigo(), 3);
        melhoriasAdquiridas.add(branca);
        linhasBrancasPendentes--;
        return true;
    }

    boolean podeAdicionarMelhoria(int coluna) {
        if (coluna < 0 || coluna > 2) {
            return false;
        }
        int quantidade = 0;
        for (Melhoria melhoria : melhoriasAdquiridas) {
            if (melhoria.getColuna() == coluna) {
                quantidade++;
            }
        }
        return quantidade < 4;
    }

    public int getQuantidadeColunas() {
        return quantidadeColunas;
    }

    public List<Tributo> getTributosArmazenados() {
        return new ArrayList<>(tributosArmazenados);
    }
    
    public List<Melhoria> getMelhoriasAdquiridas() {
        return new ArrayList<>(melhoriasAdquiridas);
    }


    public int getQuantidadeMoedasVirtuais() {
        return moedasVirtuaisDaColuna;
    }

    public int getMoedasVirtuaisDaColuna() {
        return moedasVirtuaisDaColuna;
    }

    public int getQuantidadeEspadasVirtuais() {
        return espadasVirtuaisDaColuna;
    }

    public int getQuantidadeYurtsVirtuais() {
        return yurtsVirtuaisDaColuna;
    }

    public int getQuantidadeCavalosVirtuais() {
        return cavalosVirtuaisDaColuna;
    }

    public int getQuantidadePunhosVirtuais() {
        return punhosVirtuaisDaColuna;
    }

    public int contarMelhoriasNaColuna(int coluna) {
        int quantidade = 0;

        for (Melhoria melhoria : melhoriasAdquiridas) {
            if (melhoria != null && melhoria.getColuna() == coluna) {
                quantidade++;
            }
        }

        return quantidade;
    }

    private int contarMelhoriasDaColuna(String prefixo, int coluna) {
        int quantidade = 0;

        for (Melhoria melhoria : melhoriasAdquiridas) {
            if (melhoria != null && melhoria.getColuna() == coluna
                    && melhoria.getCodigo() != null
                    && melhoria.getCodigo().startsWith(prefixo)) {
                quantidade++;
            }
        }

        return quantidade;
    }

}
