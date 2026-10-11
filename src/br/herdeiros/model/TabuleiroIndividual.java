package br.herdeiros.model;

import java.util.ArrayList;
import java.util.List;

public class TabuleiroIndividual {
    private List<Tributo> tributosArmazenados;
    private List<Melhoria> melhoriasAdquiridas;
    private Integer quantidadeColunas;

    public TabuleiroIndividual() {
        this.tributosArmazenados = new ArrayList<>();
        this.melhoriasAdquiridas = new ArrayList<>();
        this.quantidadeColunas = 4; // Cada jogador começa com suas colunas padrão de ativação
    }

    public void adicionarTributo(Tributo tributo) {
        this.tributosArmazenados.add(tributo);
    }

    public boolean removerTributoPorTipo(TipoTributo tipo) {
        for (Tributo t : tributosArmazenados) {
        	Boolean jogadorTemEsseTributo = t.getTipo() == tipo;
        	
            if (jogadorTemEsseTributo) {
                tributosArmazenados.remove(t);
                return true;
            }
        }
        return false;
    }

    public void adicionarMelhoria(Melhoria melhoria) {
        this.melhoriasAdquiridas.add(melhoria);
    }

    public  List<Tributo> getTributosArmazenados() {
        return tributosArmazenados;
    }

    public List<Melhoria> getMelhoriasAdquiridas() {
        return melhoriasAdquiridas;
    }
}