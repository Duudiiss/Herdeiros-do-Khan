package br.herdeiros.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Objeto de consulta da Facade. Contem uma copia imutavel dos dados da cartela,
 * sem entregar as colecoes mutaveis do Model para a View.
 */
public final class ResumoCartela {

    private final String nomeJogador;
    private final Cor corJogador;
    private final int indiceJogador;
    private final int quantidadeColunas;
    private final int espadas;
    private final int moedas;
    private final int yurts;
    private final int espadasVirtuais;
    private final int moedasVirtuais;
    private final int yurtsVirtuais;
    private final List<String> melhorias;
    private final List<ResumoMelhoria> melhoriasDetalhadas;

    public ResumoCartela(String nomeJogador, Cor corJogador, int indiceJogador,
            int quantidadeColunas, int espadas, int moedas, int yurts,
            List<String> melhorias) {
        this(nomeJogador, corJogador, indiceJogador, quantidadeColunas,
                espadas, moedas, yurts, 0, 0, 0, melhorias,
                Collections.<ResumoMelhoria>emptyList());
    }

    public ResumoCartela(String nomeJogador, Cor corJogador, int indiceJogador,
            int quantidadeColunas, int espadas, int moedas, int yurts,
            int espadasVirtuais, int moedasVirtuais, int yurtsVirtuais,
            List<String> melhorias, List<ResumoMelhoria> melhoriasDetalhadas) {
        if (nomeJogador == null || corJogador == null || melhorias == null
                || melhoriasDetalhadas == null) {
            throw new IllegalArgumentException("Dados da cartela obrigatorios");
        }
        this.nomeJogador = nomeJogador;
        this.corJogador = corJogador;
        this.indiceJogador = indiceJogador;
        this.quantidadeColunas = quantidadeColunas;
        this.espadas = espadas;
        this.moedas = moedas;
        this.yurts = yurts;
        this.espadasVirtuais = espadasVirtuais;
        this.moedasVirtuais = moedasVirtuais;
        this.yurtsVirtuais = yurtsVirtuais;
        this.melhorias = Collections.unmodifiableList(new ArrayList<String>(melhorias));
        this.melhoriasDetalhadas = Collections.unmodifiableList(
                new ArrayList<ResumoMelhoria>(melhoriasDetalhadas));
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public Cor getCorJogador() {
        return corJogador;
    }

    public int getIndiceJogador() {
        return indiceJogador;
    }

    public int getQuantidadeColunas() {
        return quantidadeColunas;
    }

    public int getEspadas() {
        return espadas;
    }

    public int getMoedas() {
        return moedas;
    }

    public int getYurts() {
        return yurts;
    }

    public int getEspadasVirtuais() {
        return espadasVirtuais;
    }

    public int getMoedasVirtuais() {
        return moedasVirtuais;
    }

    public int getYurtsVirtuais() {
        return yurtsVirtuais;
    }

    public List<String> getMelhorias() {
        return melhorias;
    }

    public List<ResumoMelhoria> getMelhoriasDetalhadas() {
        return melhoriasDetalhadas;
    }
}
