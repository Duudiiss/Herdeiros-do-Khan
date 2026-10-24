package br.herdeiros.model;

import java.util.ArrayList;
import java.util.List;

public class TabuleiroIndividual {
    private final List<Tributo> tributosArmazenados;
    private final List<Melhoria> melhoriasAdquiridas;
    private final int quantidadeColunas;

    public TabuleiroIndividual() {
        this.tributosArmazenados = new ArrayList<>();
        this.melhoriasAdquiridas = new ArrayList<>();
        this.quantidadeColunas = 4; // Cada jogador começa com suas colunas padrão de ativação
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

    public List<Tributo> getTributosArmazenados() {
        return new ArrayList<>(tributosArmazenados);
    }

    public List<Melhoria> getMelhoriasAdquiridas() {
        return new ArrayList<>(melhoriasAdquiridas);
    }
}