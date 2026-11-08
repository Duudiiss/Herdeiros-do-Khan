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
        movimentosRestantes = acoes.getCavalos();
        punhosDaColuna = acoes.getPunhos();
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

    void encerrarTurno() {
        colunaAtivaNoTurno = -1;
        movimentosRestantes = 0;
        punhosDaColuna = 0;
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
                acaoMelhoria, cavalos);
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
        this.melhoriasAdquiridas.add(melhoria);
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
}