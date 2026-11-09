
package br.herdeiros.view;

import br.herdeiros.controller.ControladorTabuleiro;
import br.herdeiros.mapa.ArquivoMapa;
import br.herdeiros.mapa.MapaDados;
import br.herdeiros.model.InterfacePartida;
import br.herdeiros.model.GerenciadorPartida;
import br.herdeiros.model.ObservadorPartida;
import br.herdeiros.model.ObservadoPartida;
import br.herdeiros.model.Cor;
import br.herdeiros.model.Personagem;
import br.herdeiros.model.DestinoKhan;
import br.herdeiros.model.Provincia;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class JanelaJogo extends JPanel implements ObservadorPartida {

    private static final long serialVersionUID = 1L;

    // Componentes da partida
    private InterfacePartida partida;
    private MapaDados mapa;
    private ControladorTabuleiro controlador;
    private PainelTabuleiro painel;

    // Componentes da interface
    private JLabel indicadorVez;
    private JTextArea estado;


    private JButton passar;
    private JButton abrirCartela;
    private JButton abrirCidades;
    private JButton abrirMelhorias;
    private JButton construirYurt;
    private JanelaMelhorias janelaMelhorias;
    private JButton moverKhan;
    private JComboBox<DestinoKhan> destinosKhan;
    private JLabel posicaoKhan;
    private JanelaCartela janelaCartela;
    private JanelaCidades janelaCidades;

    public JanelaJogo() {

        configurarInterface();
    }

    /**
     * Prepara a estrutura visual da janela.
     */
    private void configurarInterface() {

        setLayout(new BorderLayout(8, 8));

        indicadorVez = new JLabel();
        indicadorVez.setHorizontalAlignment(JLabel.CENTER);

        estado = new JTextArea(
            "Configure uma partida para começar."
        );

        estado.setEditable(false);
        estado.setLineWrap(true);
        estado.setWrapStyleWord(true);
        estado.setOpaque(false);
        estado.setRows(3);

        JPanel lateral = new JPanel(
            new BorderLayout(8, 8)
        );

        lateral.setPreferredSize(new Dimension(240, 0));

        JPanel ferramentas = new JPanel(
            new GridLayout(0, 1, 6, 6)
        );

        ferramentas.add(new JLabel(

            "<html>Movimentacao: ative uma<br>coluna na cartela individual.</html>"
        ));

        passar = new JButton("Passar turno");

        passar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {

                if (controlador != null) {
                    controlador.passarVez();
                }
            }
        });

        ferramentas.add(passar);

        abrirCartela = new JButton("Abrir cartela do jogador");
        abrirCartela.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                abrirCartelaDoJogador();
            }
        });
        ferramentas.add(abrirCartela);

        abrirCidades = new JButton("Consultar cidades abertas");
        abrirCidades.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                abrirJanelaCidades();
            }
        });
        // O saque de cidades e feito clicando diretamente nas cidades do tabuleiro.
        abrirMelhorias = new JButton("Comprar melhorias");
        abrirMelhorias.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                abrirJanelaMelhorias();
            }
        });
        ferramentas.add(abrirMelhorias);

        construirYurt = new JButton("Construir yurt");
        construirYurt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                if (controlador != null) {
                    controlador.alternarModoConstrucaoYurt();
                    construirYurt.setText(controlador.isModoConstrucaoYurt()
                            ? "Cancelar yurt" : "Construir yurt");
                }
            }
        });
        ferramentas.add(construirYurt);

        ferramentas.add(new JLabel("Ação obrigatória do Khan:"));
        destinosKhan = new JComboBox<DestinoKhan>(DestinoKhan.values());
        ferramentas.add(destinosKhan);
        moverKhan = new JButton("Mover Khan");
        moverKhan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                if (partida != null) {
                    DestinoKhan escolhido = (DestinoKhan) destinosKhan.getSelectedItem();
                    if (!partida.realizarAcaoKhan(escolhido)) {
                        estado.setText("Ação recusada: ative uma coluna com Khan e escolha outro destino.");
                    }
                }
            }
        });
        ferramentas.add(moverKhan);
        posicaoKhan = new JLabel("Khan: fora do tabuleiro");
        ferramentas.add(posicaoKhan);

        ferramentas.add(new JLabel(
            "<html>Clique nos icones com contadores para coletar tributos (1 punho).<br>Clique numa cidade aberta para saquear com espadas.</html>"
        ));

        ferramentas.add(new JLabel(
            "<html>Mapa carregado do<br>arquivo CSV.</html>"
        ));

        lateral.add(indicadorVez, BorderLayout.CENTER);
        lateral.add(ferramentas, BorderLayout.NORTH);
        lateral.add(estado, BorderLayout.SOUTH);

        add(lateral, BorderLayout.EAST);
    }

    private void abrirJanelaMelhorias() {
        if (partida == null) {
            return;
        }
        if (janelaMelhorias != null && janelaMelhorias.isDisplayable()) {
            janelaMelhorias.toFront();
            return;
        }
        janelaMelhorias = new JanelaMelhorias(partida);
        janelaMelhorias.setVisible(true);
    }

    /** Janela informativa das cidades disponiveis, atualizada via Observer. */
    private void abrirJanelaCidades() {
        if (partida == null) {
            return;
        }
        if (janelaCidades != null && janelaCidades.isDisplayable()) {
            janelaCidades.toFront();
            return;
        }
        janelaCidades = new JanelaCidades(partida);
        janelaCidades.setVisible(true);
    }

    /** Abre uma cartela que acompanha automaticamente o jogador da vez. */
    private void abrirCartelaDoJogador() {
        if (partida == null) {
            return;
        }
        if (janelaCartela != null && janelaCartela.isDisplayable()) {
            janelaCartela.toFront();
            return;
        }
        try {
            janelaCartela = new JanelaCartela(partida);
            janelaCartela.setVisible(true);
        } catch (IOException erro) {
            JOptionPane.showMessageDialog(this,
                    "Nao foi possivel abrir as imagens das cartelas: " + erro.getMessage(),
                    "Erro ao abrir cartela", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicializa uma nova partida.
     */
    public void iniciarPartida(String[] nomes, Cor[] cores) throws IOException {
        iniciarPartida(nomes, cores, null, false);
    }

    public void iniciarPartida(String[] nomes, Cor[] cores,
                              Personagem[] personagens) throws IOException {
        iniciarPartida(nomes, cores, personagens, true);
    }

    private void iniciarPartida(String[] nomes, Cor[] cores,
                               Personagem[] personagens, boolean ordemAleatoria) throws IOException {

        if (janelaMelhorias != null) {
            janelaMelhorias.dispose();
            janelaMelhorias = null;
        }
        if (janelaCidades != null) {
            janelaCidades.dispose();
            janelaCidades = null;
        }

        if (janelaCartela != null) {
            janelaCartela.dispose();
            janelaCartela = null;
        }

        // Criação do Model
        if (partida != null) {
            partida.removerObservador(this);
            if (painel != null) {
                partida.removerObservador(painel);
                remove(painel);
            }
        }
        partida = GerenciadorPartida.getInstancia()
                .iniciarPartida(nomes, cores, personagens, ordemAleatoria);

        // Carregamento do mapa
        Path caminho = Paths.get(
            "assets",
            "mapa_paradas.csv"
        );

        if (Files.exists(caminho)) {
            mapa = ArquivoMapa.carregar(caminho);
        } else {
            mapa = new MapaDados();
        }

        if (!partida.carregarMapa(mapa)) {
            throw new IOException(
                "O mapa não pôde ser carregado no Model."
            );
        }
        carregarAcessosCidades();
        carregarProvincias();
        carregarEstoquesAdicionais();

        // Criação do Controller
        controlador = new ControladorTabuleiro(
            partida,
            mapa
        );

        // Criação da View do tabuleiro
        painel = new PainelTabuleiro(partida, mapa, controlador);

        controlador.configurar(
            painel,
            estado,
            indicadorVez
        );
        controlador.habilitarAtivacaoReal();

        partida.registrarObservador(this);
        partida.registrarObservador(painel);
        atualizar(partida);

        // Adiciona o painel à interface
        add(painel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    /** Cadastro dos acessos às cidades: o mapa e a imagem oficial não ficam no Model. */
    private void carregarAcessosCidades() throws IOException {
        Path arquivo = Paths.get("assets", "cidades_paradas.csv");
        if (!Files.exists(arquivo)) {
            throw new IOException("Arquivo de acessos das cidades ausente: " + arquivo);
        }
        java.util.List<String> linhas = Files.readAllLines(arquivo);
        for (String linha : linhas) {
            String limpa = linha.trim();
            if (limpa.length() == 0 || limpa.startsWith("#")) {
                continue;
            }
            String[] partes = limpa.split(";");
            if (partes.length < 2) {
                throw new IOException("Cadastro inválido de cidade: " + linha);
            }
            for (int indice = 1; indice < partes.length; indice++) {
                int parada;
                try {
                    parada = Integer.parseInt(partes[indice].trim());
                } catch (NumberFormatException erro) {
                    throw new IOException("Identificador de parada inválido: " + linha, erro);
                }
                if (!partida.configurarAcessoCidade(partes[0], parada)) {
                    throw new IOException("Cidade ou parada inválida no cadastro: " + linha);
                }
            }
        }
    }

    /** Estoques adicionais por tipo, configurados sem duplicar dados na View. */
    private void carregarEstoquesAdicionais() throws IOException {
        Path arquivo = Paths.get("assets", "estoques_adicionais.csv");
        if (!Files.exists(arquivo)) {
            return;
        }
        for (String linha : Files.readAllLines(arquivo)) {
            String limpa = linha.trim();
            if (limpa.isEmpty() || limpa.startsWith("#")) {
                continue;
            }
            String[] partes = limpa.split(";");
            if (partes.length != 3) {
                throw new IOException("Estoque adicional invalido: " + linha);
            }
            try {
                Provincia provincia = Provincia.valueOf(partes[0].trim());
                int quantidade = Integer.parseInt(partes[2].trim());
                if (!partida.configurarTributoAdicional(provincia, partes[1], quantidade)) {
                    throw new IOException("Estoque adicional recusado: " + linha);
                }
            } catch (IllegalArgumentException erro) {
                throw new IOException("Estoque adicional invalido: " + linha, erro);
            }
        }
    }

    /** As relações visuais estão em CSV, fora do Model e sem números fixos no Java. */
    private void carregarProvincias() throws IOException {
        Path arquivo = Paths.get("assets", "provincias_paradas.csv");
        if (!Files.exists(arquivo)) {
            throw new IOException("Falta o mapa de províncias: " + arquivo);
        }
        java.util.List<String> linhas = Files.readAllLines(arquivo);
        for (String linha : linhas) {
            String limpa = linha.trim();
            if (limpa.length() == 0 || limpa.startsWith("#")) {
                continue;
            }
            String[] campos = limpa.split(";");
            if (campos.length == 4 && campos[0].equals("V")) {
                try {
                    Provincia origem = Provincia.valueOf(campos[1].trim());
                    Provincia primeira = Provincia.valueOf(campos[2].trim());
                    Provincia segunda = Provincia.valueOf(campos[3].trim());
                    if (!partida.configurarVizinhasKhan(origem, primeira, segunda)) {
                        throw new IOException("Vizinhança de Khan inválida: " + linha);
                    }
                    continue;
                } catch (IllegalArgumentException erro) {
                    throw new IOException("Província de Khan inválida: " + linha, erro);
                }
            }
            if (campos.length < 4) {
                throw new IOException("Linha de província incompleta: " + linha);
            }
            try {
                Provincia provincia = Provincia.valueOf(campos[0].trim());
                int estoque = Integer.parseInt(campos[2].trim());
                if (!partida.configurarProvincia(provincia, campos[1], estoque)) {
                    throw new IOException("Província inválida: " + linha);
                }
                for (int i = 3; i < campos.length; i++) {
                    int parada = Integer.parseInt(campos[i].trim());
                    if (!partida.configurarAcessoProvincia(provincia, parada)) {
                        throw new IOException("Parada inválida na província: " + linha);
                    }
                }
            } catch (IllegalArgumentException erro) {
                throw new IOException("Erro no cadastro de província: " + linha, erro);
            }
        }
        for (Provincia provincia : Provincia.values()) {
            if (partida.consultarEstoqueProvincia(provincia) < 0) {
                throw new IOException("Província ausente no arquivo: " + provincia.name());
            }
        }
    }

    @Override
    public void atualizar(ObservadoPartida observado) {
        if (controlador != null) {
            controlador.atualizarEstado();
        }
        if (partida != null) {
            moverKhan.setEnabled(partida.deveMoverKhanJogadorAtual());
            if (controlador != null && construirYurt != null) {
                construirYurt.setText(controlador.isModoConstrucaoYurt()
                        ? "Cancelar yurt" : "Construir yurt");
            }
            DestinoKhan destino = partida.consultarDestinoKhan();
            if (destino == null) {
                posicaoKhan.setText("Khan: fora do tabuleiro");
            } else {
                posicaoKhan.setText("Khan: " + destino.getDescricao());
            }
        }
    }
}
