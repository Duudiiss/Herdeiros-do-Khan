package br.herdeiros.model;

import java.io.Serializable;

class Cidade implements Serializable {
    private static final long serialVersionUID = 1L;

	private NomeCidade nome;
    private Regiao regiao;
    private int quantidadeTesouros;
    private boolean conquistada;

    public Cidade(NomeCidade nome, Regiao regiao) {
        this(nome, regiao, 4, false);
    }

    Cidade(NomeCidade nome, Regiao regiao, int quantidadeTesouros, boolean conquistada) {
        if (nome == null || regiao == null || quantidadeTesouros < 0
                || quantidadeTesouros > 4 || conquistada != (quantidadeTesouros == 0)) {
            throw new IllegalArgumentException("Estado de cidade inválido");
        }
        this.nome = nome;
        this.regiao = regiao;
        this.quantidadeTesouros = quantidadeTesouros;
        this.conquistada = conquistada;
    }
    
    // Um Peão tentará saquear a cidade
    public boolean saquearTesouro() {
        if (conquistada || quantidadeTesouros <= 0) {
            return false; // Não há mais o que saquear
        }
        
        this.quantidadeTesouros--;
        
        // Se tomou o último tesouro, a cidade está conquistada
        if (this.quantidadeTesouros == 0) {
            this.conquistada = true;
        }
        
        return true;
    }

    public NomeCidade getNomeCidade() {
        return nome;
    }

    public String getNome() {
        return nome.getNomeFormatado();
    }

    public Regiao getRegiao() {
        return regiao;
    }

    public int getQuantidadeTesouros() {
        return quantidadeTesouros;
    }

    public boolean isConquistada() {
        return conquistada;
    }

}
