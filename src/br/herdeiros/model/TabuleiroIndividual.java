package br.herdeiros.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

class TabuleiroIndividual implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Tributo> tributosArmazenados;
    private final List<Melhoria> melhoriasAdquiridas;
    private final int quantidadeColunas;
    private final boolean[] colunasOcupadas;
    private final boolean[] melhoriasNesteTurno = new boolean[4];
    private final int[] quantidadeMelhoriasNesteTurno = new int[4];
    private int colunaAtivaNoTurno;
    private int movimentosRestantes;
    private int punhosDaColuna;
    private int moedasVirtuaisDaColuna;
    private int espadasVirtuaisDaColuna;
    private int yurtsVirtuaisDaColuna;
    private int cavalosVirtuaisDaColuna;
    private int punhosVirtuaisDaColuna;
    /** Cada tipo de tesouro virtual pertence apenas ao turno ativado. */
    private final java.util.EnumMap<TipoTesouro, Integer> tesourosVirtuaisDaColuna =
            new java.util.EnumMap<TipoTesouro, Integer>(TipoTesouro.class);
    /** Recursos de teste aplicados antes da ativacao, mantidos no save entre turnos. */
    private final Map<String, Integer> recursosDesenvolvedor = new HashMap<String, Integer>();
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
        cavalosVirtuaisDaColuna = contarMelhoriasValidasDaColuna("CAV", coluna) * 2;
        punhosVirtuaisDaColuna = contarMelhoriasValidasDaColuna("MAO", coluna);
        movimentosRestantes = acoes.getCavalos() + cavalosVirtuaisDaColuna;
        punhosDaColuna = acoes.getPunhos() + punhosVirtuaisDaColuna;
        moedasVirtuaisDaColuna = acoes.getMoedasVirtuais()
                + contarMelhoriasValidasDaColuna("MOE", coluna);
        espadasVirtuaisDaColuna = contarMelhoriasValidasDaColuna("ESP", coluna);
        yurtsVirtuaisDaColuna = contarMelhoriasValidasDaColuna("CAS", coluna);
        tesourosVirtuaisDaColuna.clear();
        for (TipoTesouro tipo : TipoTesouro.values()) {
            int quantidade = contarMelhoriasValidasDaColuna(codigoTesouroVirtual(tipo), coluna);
            quantidade += recursosDesenvolvedor.getOrDefault("TESOURO_VIRTUAL_" + tipo.name(), 0);
            if (quantidade > 0) tesourosVirtuaisDaColuna.put(tipo, quantidade);
        }
        acaoKhan = acoes.temKhan();
        acaoMelhoria = acoes.temMelhoria();
        movimentosRestantes += recursosDesenvolvedor.getOrDefault("CAVALO", 0)
                + recursosDesenvolvedor.getOrDefault("CAVALO_VIRTUAL", 0);
        punhosDaColuna += recursosDesenvolvedor.getOrDefault("PUNHO", 0)
                + recursosDesenvolvedor.getOrDefault("PUNHO_VIRTUAL", 0);
        moedasVirtuaisDaColuna += recursosDesenvolvedor.getOrDefault("MOEDA_VIRTUAL", 0);
        espadasVirtuaisDaColuna += recursosDesenvolvedor.getOrDefault("ESPADA_VIRTUAL", 0);
        yurtsVirtuaisDaColuna += recursosDesenvolvedor.getOrDefault("YURT_VIRTUAL", 0);
        recursosDesenvolvedor.clear();

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
        java.util.Arrays.fill(melhoriasNesteTurno, false);
        java.util.Arrays.fill(quantidadeMelhoriasNesteTurno, 0);
        colunaAtivaNoTurno = -1;
        movimentosRestantes = 0;
        punhosDaColuna = 0;
        moedasVirtuaisDaColuna = 0;
        espadasVirtuaisDaColuna = 0;
        yurtsVirtuaisDaColuna = 0;
        cavalosVirtuaisDaColuna = 0;
        punhosVirtuaisDaColuna = 0;
        tesourosVirtuaisDaColuna.clear();
        acaoKhan = false;
        acaoMelhoria = false;
    }

    ResumoAtivacao consultarAtivacao(Personagem personagem) {
        int[] cavalos = new int[4];
        for (int i = 0; i < cavalos.length; i++) {
            cavalos[i] = AcoesColuna.obter(personagem, i).getCavalos();
        }
        boolean[] usadas = colunasOcupadas.clone();
        boolean[] bloqueadasPorMelhoria = new boolean[4];
        return new ResumoAtivacao(usadas, bloqueadasPorMelhoria,
                colunaAtivaNoTurno, movimentosRestantes, punhosDaColuna,
                acaoKhan, acaoMelhoria, cavalos, moedasVirtuaisDaColuna);
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

    /**
     * A melhoria inicial de Jochi e recebida no primeiro turno dele.
     * Pode ser colocada na coluna branca sem bloquea-la, mas seu beneficio
     * so passa a valer a partir do turno seguinte, como toda melhoria nova.
     */
    void adicionarMelhoriaInicial(Melhoria melhoria) {
        if (melhoria == null || melhoria.getColuna() != 3) {
            throw new IllegalArgumentException("Melhoria inicial de Jochi invalida");
        }
        melhoriasAdquiridas.add(melhoria);
        melhoriasNesteTurno[3] = true;
        quantidadeMelhoriasNesteTurno[3]++;
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
        melhoriasNesteTurno[coluna] = true;
        quantidadeMelhoriasNesteTurno[coluna]++;
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
        melhoriasNesteTurno[3] = true;
        quantidadeMelhoriasNesteTurno[3]++;
        linhasBrancasPendentes--;
        return true;
    }

    boolean podeAdicionarMelhoria(int coluna) {
        if (coluna < 0 || coluna > 3) {
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

    boolean[] getColunasOcupadas() {
        return colunasOcupadas.clone();
    }

    boolean podeSalvarEntreTurnos() {
        return colunaAtivaNoTurno < 0 && linhasBrancasPendentes == 0 && !acaoKhan;
    }

    void restaurarEstado(List<Tributo> tributos, List<Melhoria> melhorias,
                         boolean[] colunasOcupadasSalvas) {
        if (tributos == null || melhorias == null || colunasOcupadasSalvas == null
                || colunasOcupadasSalvas.length != quantidadeColunas) {
            throw new IllegalArgumentException("Estado da cartela inválido");
        }
        for (Tributo tributo : tributos) {
            if (tributo == null || tributo.getTipo() == null) {
                throw new IllegalArgumentException("Tributo inválido na cartela");
            }
        }
        for (Melhoria melhoria : melhorias) {
            if (melhoria == null || melhoria.getCodigo() == null
                    || melhoria.getColuna() < 0 || melhoria.getColuna() >= quantidadeColunas) {
                throw new IllegalArgumentException("Melhoria inválida na cartela");
            }
        }
        tributosArmazenados.clear();
        tributosArmazenados.addAll(tributos);
        melhoriasAdquiridas.clear();
        melhoriasAdquiridas.addAll(melhorias);
        System.arraycopy(colunasOcupadasSalvas, 0, colunasOcupadas, 0, quantidadeColunas);
        encerrarTurno();
    }

    public List<Tributo> getTributosArmazenados() {
        return new ArrayList<>(tributosArmazenados);
    }
    
    public List<Melhoria> getMelhoriasAdquiridas() {
        return new ArrayList<>(melhoriasAdquiridas);
    }


    Map<String, Integer> getRecursosDesenvolvedor() {
        return new HashMap<String, Integer>(recursosDesenvolvedor);
    }

    void restaurarRecursosDesenvolvedor(Map<String, Integer> recursos) {
        recursosDesenvolvedor.clear();
        if (recursos != null) {
            for (Map.Entry<String, Integer> entrada : recursos.entrySet()) {
                if (!desenvolvimentoAjustarVirtual(entrada.getKey(), entrada.getValue())) {
                    throw new IllegalArgumentException("Recurso de desenvolvedor invalido");
                }
            }
        }
    }

    boolean desenvolvimentoAjustarVirtual(String recurso, int variacao) {
        if (recurso == null || variacao == 0) {
            return false;
        }
        boolean conhecido = "MOEDA_VIRTUAL".equals(recurso)
                || "ESPADA_VIRTUAL".equals(recurso)
                || "YURT_VIRTUAL".equals(recurso)
                || "CAVALO_VIRTUAL".equals(recurso)
                || "PUNHO_VIRTUAL".equals(recurso)
                || "CAVALO".equals(recurso) || "PUNHO".equals(recurso)
                || tipoTesouroVirtual(recurso) != null;
        if (!conhecido) {
            return false;
        }
        if (colunaAtivaNoTurno < 0) {
            int atual = recursosDesenvolvedor.getOrDefault(recurso, 0);
            if ((long) atual + variacao < 0 || (long) atual + variacao > 9999) {
                return false;
            }
            recursosDesenvolvedor.put(recurso, atual + variacao);
            return true;
        }
        TipoTesouro tipoVirtual = tipoTesouroVirtual(recurso);
        if (tipoVirtual != null) {
            int atualVirtual = tesourosVirtuaisDaColuna.getOrDefault(tipoVirtual, 0);
            if ((long) atualVirtual + variacao < 0 || (long) atualVirtual + variacao > 9999)
                return false;
            tesourosVirtuaisDaColuna.put(tipoVirtual, atualVirtual + variacao);
            return true;
        }
        int atual;
        if ("MOEDA_VIRTUAL".equals(recurso)) {
            atual = moedasVirtuaisDaColuna;
        } else if ("ESPADA_VIRTUAL".equals(recurso)) {
            atual = espadasVirtuaisDaColuna;
        } else if ("YURT_VIRTUAL".equals(recurso)) {
            atual = yurtsVirtuaisDaColuna;
        } else if ("CAVALO_VIRTUAL".equals(recurso) || "CAVALO".equals(recurso)) {
            atual = movimentosRestantes;
        } else {
            atual = punhosDaColuna;
        }
        if ((long) atual + variacao < 0 || (long) atual + variacao > 9999) {
            return false;
        }
        if ("MOEDA_VIRTUAL".equals(recurso)) {
            moedasVirtuaisDaColuna += variacao;
        } else if ("ESPADA_VIRTUAL".equals(recurso)) {
            espadasVirtuaisDaColuna += variacao;
        } else if ("YURT_VIRTUAL".equals(recurso)) {
            yurtsVirtuaisDaColuna += variacao;
        } else if ("CAVALO_VIRTUAL".equals(recurso) || "CAVALO".equals(recurso)) {
            movimentosRestantes += variacao;
        } else {
            punhosDaColuna += variacao;
        }
        return true;
    }

    private static TipoTesouro tipoTesouroVirtual(String recurso) {
        if (recurso == null || !recurso.startsWith("TESOURO_VIRTUAL_")) return null;
        try {
            return TipoTesouro.valueOf(recurso.substring("TESOURO_VIRTUAL_".length()));
        } catch (IllegalArgumentException erro) {
            return null;
        }
    }

    /** Correspondencia com os desenhos originais das cinco melhorias. */
    private static String codigoTesouroVirtual(TipoTesouro tipo) {
        switch (tipo) {
            case GADO: return "BOI";
            case PELES: return "CUI";       // arte de especiarias
            case PORCELANA: return "MOP";   // arte de peles
            case SEDA: return "PER";
            case FERRO: return "MOB";
            default: throw new IllegalArgumentException("Tipo nao reconhecido");
        }
    }

    Map<TipoTesouro, Integer> consultarTesourosVirtuais() {
        java.util.EnumMap<TipoTesouro, Integer> copia =
                new java.util.EnumMap<TipoTesouro, Integer>(TipoTesouro.class);
        for (TipoTesouro tipo : TipoTesouro.values()) {
            int quantidade = tesourosVirtuaisDaColuna.getOrDefault(tipo, 0);
            copia.put(tipo, quantidade);
        }
        return java.util.Collections.unmodifiableMap(copia);
    }

    /** Consome apenas recursos virtuais realmente usados, nunca cria tesouro fisico. */
    void consumirTesourosVirtuais(Map<TipoTesouro, Integer> consumidos) {
        for (Map.Entry<TipoTesouro, Integer> entrada : consumidos.entrySet()) {
            int atual = tesourosVirtuaisDaColuna.getOrDefault(entrada.getKey(), 0);
            if (entrada.getValue() < 0 || atual < entrada.getValue())
                throw new IllegalStateException("Tesouro virtual indisponivel");
        }
        for (Map.Entry<TipoTesouro, Integer> entrada : consumidos.entrySet()) {
            tesourosVirtuaisDaColuna.put(entrada.getKey(),
                    tesourosVirtuaisDaColuna.getOrDefault(entrada.getKey(), 0) - entrada.getValue());
        }
    }

    public int getQuantidadeMoedasVirtuais() {
        return moedasVirtuaisDaColuna + recursosDesenvolvedor.getOrDefault("MOEDA_VIRTUAL", 0);
    }

    public int getMoedasVirtuaisDaColuna() {
        return moedasVirtuaisDaColuna + recursosDesenvolvedor.getOrDefault("MOEDA_VIRTUAL", 0);
    }

    public int getQuantidadeEspadasVirtuais() {
        return espadasVirtuaisDaColuna + recursosDesenvolvedor.getOrDefault("ESPADA_VIRTUAL", 0);
    }

    public int getQuantidadeYurtsVirtuais() {
        return yurtsVirtuaisDaColuna + recursosDesenvolvedor.getOrDefault("YURT_VIRTUAL", 0);
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

    /** Ignora as cartas adicionadas no turno corrente, inclusive na branca. */
    private int contarMelhoriasValidasDaColuna(String prefixo, int coluna) {
        int antigas = contarMelhoriasNaColuna(coluna) - quantidadeMelhoriasNesteTurno[coluna];
        int examinadas = 0;
        int total = 0;
        for (Melhoria melhoria : melhoriasAdquiridas) {
            if (melhoria == null || melhoria.getColuna() != coluna) {
                continue;
            }
            if (examinadas >= antigas) {
                break;
            }
            if (melhoria.getCodigo() != null && melhoria.getCodigo().startsWith(prefixo)) {
                total++;
            }
            examinadas++;
        }
        return total;
    }


}
