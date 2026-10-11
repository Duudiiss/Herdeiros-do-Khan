package br.herdeiros.model;

public class Conselheiro {
    private String nome;
    private Integer votosConcedidos;
    private Integer tesourosEntregues;
    private Integer tesourosNecessariosParaSatisfazer;
    private Boolean satisfeito;

    public Conselheiro(String nome, Integer votosConcedidos, Integer tesourosNecessarios) {
        this.nome = nome;
        this.votosConcedidos = votosConcedidos;
        this.tesourosNecessariosParaSatisfazer = tesourosNecessarios;
        this.tesourosEntregues = 0;
        this.satisfeito = false;
    }

    // O jogador entrega um tesouro ao conselheiro
    public void receberTesouro() {
        if (!satisfeito) {
            this.tesourosEntregues++;
            
            if (this.tesourosEntregues >= this.tesourosNecessariosParaSatisfazer) {
                this.satisfeito = true;
            }
        }
    }

    public String getNome() {
        return nome;
    }

    public Integer getVotosConcedidos() {
        return votosConcedidos;
    }

    public Boolean isSatisfeito() {
        return satisfeito;
    }
}