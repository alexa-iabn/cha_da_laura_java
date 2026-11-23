
 
package com.mycompany.cha_da_laura_java;

import com.mycompany.cha_da_laura_java.dao.RelatoriosDAO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.DoubleFunction;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.text.MaskFormatter;


public class paginaRelatorios extends JPanel {

    // ===== Cores e fonte do layout =====
    private static final Color VERDE = new Color(22, 87, 40);
    private static final Color VERDE_CLARO = new Color(236, 243, 234);
    private static final Color VERDE_COLUNA = new Color(150, 196, 148);
    private static final Color VERDE_SUBIU = new Color(46, 125, 50);
    private static final Color AZUL_TITULO = new Color(16, 52, 128);
    private static final Color AZUL_TEXTO = new Color(84, 116, 196);
    private static final Color AZUL_GRAFICO = new Color(66, 165, 245);
    private static final Color VERMELHO = new Color(211, 47, 47);
    private static final Color BORDA = new Color(226, 230, 226);
    private static final Color CINZA_TRILHO = new Color(242, 244, 242);
    private static final Color BRANCO = Color.WHITE;
    private static final String FONTE = "Segoe UI";

    private static final Color[] CORES_ROSCA = {
        new Color(22, 87, 40), new Color(67, 140, 80), new Color(130, 185, 125),
        new Color(190, 220, 185), new Color(240, 226, 195), new Color(245, 185, 140)
    };

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] MESES = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

    private static final Set<String> STATUS_VERDE =
            new HashSet<>(Arrays.asList("Entrada", "Ativo", "Adequado", "Concluída"));
    private static final Set<String> STATUS_VERMELHO =
            new HashSet<>(Arrays.asList("Saída", "Inativo", "Baixo"));

    // ===== Abas =====
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel painelAbas = new PainelLarguraJanela(cardLayout);
    private final String[] nomesAbas = {"Clientes", "Fornecedores", "Insumos", "Produção", "Movimentação"};
    private final JButton[] botoesAbas = new JButton[nomesAbas.length];
    private final boolean[] abasCarregadas =
    new boolean[nomesAbas.length];
    
public paginaRelatorios() {

    setLayout(new BorderLayout());
    setBackground(BRANCO);
    setBorder(new EmptyBorder(20, 30, 20, 30));

    add(criarCabecalho(), BorderLayout.NORTH);

    painelAbas.setOpaque(false);

    // Criar espaços vazios para as cinco abas
    for (String nome : nomesAbas) {
        painelAbas.add(new JPanel(), nome);
    }

    JScrollPane scroll = new JScrollPane(painelAbas);
    scroll.setBorder(null);
    scroll.setHorizontalScrollBarPolicy(
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    );
    scroll.getViewport().setBackground(BRANCO);
    scroll.getVerticalScrollBar().setUnitIncrement(16);

    add(scroll, BorderLayout.CENTER);

    // Carregar apenas a primeira aba
    selecionarAba(0);
}


    // CABEÇALHO + BOTÕES DAS ABAS
    private JPanel criarCabecalho() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel titulo = new JLabel("Relatórios");
        titulo.setFont(new Font(FONTE, Font.BOLD, 32));
        titulo.setForeground(AZUL_TITULO);

        JLabel subtitulo = new JLabel("Acompanhe as informações da sua loja através de relatórios simples e objetivos.");
        subtitulo.setFont(new Font(FONTE, Font.PLAIN, 15));
        subtitulo.setForeground(AZUL_TEXTO);

        JPanel abas = new JPanel(new GridLayout(1, nomesAbas.length, 10, 0));
        abas.setOpaque(false);
        for (int i = 0; i < nomesAbas.length; i++) {
            final int indice = i;
            JButton b = new JButton(nomesAbas[i]);
            b.setFont(new Font(FONTE, Font.PLAIN, 15));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(160, 50));
            b.addActionListener(e -> selecionarAba(indice));
            botoesAbas[i] = b;
            abas.add(b);
        }

        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        abas.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(titulo);
        p.add(subtitulo);
        p.add(Box.createVerticalStrut(18));
        p.add(abas);
        p.add(Box.createVerticalStrut(18));
        return p;
    }

    
private final boolean[] abasCarregando = new boolean[nomesAbas.length];

private void selecionarAba(int indice) {
    if (indice < 0 || indice >= nomesAbas.length) return;

    // A tela responde imediatamente: as consultas não bloqueiam a interface.
    if (!abasCarregadas[indice] && !abasCarregando[indice]) {
        abasCarregando[indice] = true;
        JPanel carregando = new JPanel(new BorderLayout());
        carregando.setBackground(BRANCO);
        JLabel mensagem = new JLabel("Carregando dados do MySQL...", SwingConstants.CENTER);
        mensagem.setFont(new Font(FONTE, Font.PLAIN, 17));
        mensagem.setForeground(VERDE);
        carregando.add(mensagem, BorderLayout.CENTER);
        carregando.setPreferredSize(new Dimension(700, 340));
        substituirAba(indice, carregando);

        new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() {
                // Construção de componentes ainda desconectados da interface visível.
                // A instalação no CardLayout ocorre exclusivamente em done() (EDT).
                switch (indice) {
                    case 0: return criarRelatorioClientes();
                    case 1: return criarRelatorioFornecedores();
                    case 2: return criarRelatorioInsumos();
                    case 3: return criarRelatorioProducao();
                    case 4: return criarRelatorioMovimentacao();
                    default: return new JPanel();
                }
            }

            @Override
            protected void done() {
                abasCarregando[indice] = false;
                try {
                    JPanel conteudo = get();
                    substituirAba(indice, conteudo);
                    abasCarregadas[indice] = true;
                } catch (Exception ex) {
                    JPanel erro = new JPanel(new BorderLayout());
                    erro.setBackground(BRANCO);
                    JLabel msg = new JLabel("Erro ao carregar relatório. Clique na aba para tentar novamente.", SwingConstants.CENTER);
                    msg.setForeground(VERMELHO);
                    erro.add(msg, BorderLayout.CENTER);
                    substituirAba(indice, erro);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    cardLayout.show(painelAbas, nomesAbas[indice]);
    for (int i = 0; i < botoesAbas.length; i++) {
        boolean ativo = (i == indice);
        pintarBotao(botoesAbas[i], ativo ? VERDE : BRANCO,
                ativo ? BRANCO : VERDE, ativo ? VERDE : BORDA);
    }
    painelAbas.revalidate();
    painelAbas.repaint();
}

private void substituirAba(int indice, JPanel conteudo) {
    painelAbas.remove(indice);
    painelAbas.add(conteudo, nomesAbas[indice], indice);
    painelAbas.revalidate();
    painelAbas.repaint();
}


    // ABA 1: CLIENTES
    private JPanel criarRelatorioClientes() {
        


    RelatoriosDAO dao = new RelatoriosDAO();

    int totalClientes = 0;
    int clientesAtivos = 0;
    int clientesInativos = 0;
    int totalCompras = 0;

    List<Object[]> clientesBanco = new ArrayList<>();
    List<Object[]> topClientes = new ArrayList<>();
    double[] novosClientes = new double[12];

    int anoAtual = java.time.LocalDate.now().getYear();

    try {
        totalClientes = dao.totalClientes();
        clientesAtivos = dao.clientesPorSituacao("Ativo");
        clientesInativos = dao.clientesPorSituacao("Inativo");
        totalCompras = dao.totalCompras();

        clientesBanco = dao.listarClientes();
        topClientes = dao.top5Clientes();
        novosClientes = dao.clientesPorMes(anoAtual);

    } catch (java.sql.SQLException ex) {
        ex.printStackTrace();
    }

    // Indicadores reais
    JComponent[] kpis = {
        criarKpi("Total de clientes",
            String.valueOf(totalClientes), null, null,
            "clientes cadastrados"),

        criarKpi("Clientes ativos",
            String.valueOf(clientesAtivos), null, null,
            "clientes ativos"),

        criarKpi("Clientes inativos",
            String.valueOf(clientesInativos), null, null,
            "clientes inativos"),

        criarKpi("Total de compras",
            String.valueOf(totalCompras), null, null,
            "saídas registradas para clientes")
    };

    // Dados reais para o gráfico Top 5
    String[] nomes = new String[topClientes.size()];
    double[] valores = new double[topClientes.size()];

    for (int i = 0; i < topClientes.size(); i++) {
        Object[] cliente = topClientes.get(i);

        nomes[i] = (String) cliente[0];
        valores[i] = ((Number) cliente[1]).doubleValue();
    }

    // Gráficos com informações do MySQL
    JComponent[] graficos = {
        criarCardGrafico(
            "Clientes que mais compram",
            "Top 5 clientes por valor de compras registrado.",
            new GraficoBarrasH(
                nomes,
                valores,
                paginaRelatorios::moeda
            )
        ),

        criarCardGrafico(
            "Evolução de novos clientes",
            "Número de clientes cadastrados por mês em "
                + anoAtual + ".",
            new GraficoColunas(
                MESES,
                new double[][]{novosClientes},
                new Color[]{VERDE_COLUNA},
                null,
                true,
                paginaRelatorios::numero
            )
        )
    };

    // Tabela sem dados fictícios
    TabelaRelatorio tabela = new TabelaRelatorio(
        new String[]{
            "ID", "Nome", "E-mail", "CPF", "Telefone",
            "Cidade", "Total de compras", "Última compra",
            "Status"
        },
        new Object[][]{},
        7,
        "clientes"
    );

    tabela.carregarDados(clientesBanco);

    return montarRelatorio(
        "Relatório de clientes",
        "Visualize as informações dos clientes cadastrados "
            + "e o histórico de compras.",
        "Nome, e-mail ou CPF...",
        kpis,
        graficos,
        "Lista de clientes",
        tabela,
        "relatorio_clientes"
    );
}
    

    // ABA 2: FORNECEDORES
    private JPanel criarRelatorioFornecedores() {
        

    RelatoriosDAO dao = new RelatoriosDAO();

    int totalFornecedores = 0;
    int fornecedoresAtivos = 0;
    int fornecedoresInativos = 0;

    List<Object[]> fornecedoresBanco = new ArrayList<>();
    double[] novosFornecedores = new double[12];

    int anoAtual = java.time.LocalDate.now().getYear();

    try {
        totalFornecedores = dao.totalFornecedores();

        fornecedoresAtivos =
            dao.fornecedoresPorSituacao("Ativo");

        fornecedoresInativos =
            dao.fornecedoresPorSituacao("Inativo");

        fornecedoresBanco = dao.listarFornecedores();

        novosFornecedores =
            dao.fornecedoresPorMes(anoAtual);

    } catch (java.sql.SQLException ex) {

        ex.printStackTrace();
    }

    // Indicadores reais
    JComponent[] kpis = {

        criarKpi(
            "Total de fornecedores",
            String.valueOf(totalFornecedores),
            null, null,
            "fornecedores cadastrados"
        ),

        criarKpi(
            "Fornecedores ativos",
            String.valueOf(fornecedoresAtivos),
            null, null,
            "fornecedores ativos"
        ),

        criarKpi(
            "Fornecedores inativos",
            String.valueOf(fornecedoresInativos),
            null, null,
            "fornecedores inativos"
        ),

        criarKpi(
            "Total de compras",
            "—",
            null, null,
            "compras ainda não integradas"
        )
    };

    // Gráficos
    JComponent[] graficos = {

        criarCardGrafico(
            "Compras por fornecedor",
            "Aguardando integração dos registros de compras.",
            new GraficoBarrasH(
                new String[]{},
                new double[]{},
                paginaRelatorios::moeda
            )
        ),

        criarCardGrafico(
            "Novos fornecedores por mês",
            "Quantidade de fornecedores cadastrados por mês em "
                + anoAtual + ".",
            new GraficoColunas(
                MESES,
                new double[][]{novosFornecedores},
                new Color[]{VERDE_COLUNA},
                null,
                true,
                paginaRelatorios::numero
            )
        )
    };

    // Tabela com dados reais
    TabelaRelatorio tabela = new TabelaRelatorio(

        new String[]{
            "ID",
            "Nome",
            "CNPJ",
            "Cidade",
            "Telefone",
            "Total de compras",
            "Última compra",
            "Status"
        },

        new Object[][]{},
        6,
        "fornecedores"
    );

    tabela.carregarDados(fornecedoresBanco);

    return montarRelatorio(
        "Relatório de fornecedores",
        "Visualize as informações dos fornecedores cadastrados.",
        "Nome, CNPJ ou cidade...",
        kpis,
        graficos,
        "Lista de fornecedores",
        tabela,
        "relatorio_fornecedores"
    );
}


    // =====================================================================
    // ABA 3: INSUMOS
    // =====================================================================
   
private JPanel criarRelatorioInsumos() {

    RelatoriosDAO dao = new RelatoriosDAO();

    int totalInsumos = 0;
    int estoqueBaixo = 0;

    List<Object[]> insumosBanco = new ArrayList<>();
    List<Object[]> insumosBaixos = new ArrayList<>();
    List<Object[]> topInsumos = new ArrayList<>();
double[][] movimentos = new double[2][12];
int totalMovimentos = 0;
int anoAtual = java.time.LocalDate.now().getYear();

    try {
        totalInsumos = dao.totalInsumos();
        estoqueBaixo = dao.insumosEstoqueBaixo();
        insumosBanco = dao.listarInsumosComMovimentos();
topInsumos = dao.top5InsumosConsumidos();
movimentos = dao.movimentacaoInsumosPorMes(anoAtual);
totalMovimentos = dao.totalMovimentosConsumo();
        
        insumosBaixos = dao.listarInsumosBaixos();

    } catch (java.sql.SQLException ex) {

        ex.printStackTrace();
    }

    // Indicadores reais
    JComponent[] kpis = {

        criarKpi(
            "Total de insumos",
            String.valueOf(totalInsumos),
            null, null,
            "cadastrados no sistema"
        ),

        criarKpi(
            "Estoque total",
            "—",
            null, null,
            "unidades de medida diferentes"
        ),

        criarKpi(
            "Insumos com estoque baixo",
            String.valueOf(estoqueBaixo),
            null, null,
            "abaixo do estoque mínimo"
        ),

       criarKpi(
    "Movimentos de consumo",
    String.valueOf(totalMovimentos),
    null, null,
    "saídas registradas no histórico"
)
    };

        // Preparar dados reais dos insumos mais consumidos
        String[] nomesTop = new String[topInsumos.size()];
        double[] valoresTop = new double[topInsumos.size()];

        for (int i = 0; i < topInsumos.size(); i++) {

            Object[] insumo = topInsumos.get(i);

            nomesTop[i] = insumo[0] + " (" + insumo[1] + ")";
            valoresTop[i] = ((Number) insumo[2]).doubleValue();
        }

        // Preparar movimentações mensais
        double[][] movimentosGrafico = {
            java.util.Arrays.copyOf(movimentos[0], MESES.length),
            java.util.Arrays.copyOf(movimentos[1], MESES.length)
        };

    // Dados reais de estoque baixo
    Object[][] dadosBaixos =
        insumosBaixos.toArray(new Object[0][]);

    // Gráficos
   
        JComponent[] graficos = {

            // GRÁFICO 1 - Insumos mais consumidos
            criarCardGrafico(
                "Insumos mais consumidos",
                "Top 5 insumos por quantidade consumida no histórico.",
                new GraficoBarrasH(
                    nomesTop,
                    valoresTop,
                    paginaRelatorios::unidades
                )
            ),

            // GRÁFICO 2 - Movimentação mensal
            criarCardGrafico(
                "Movimentação de insumos",
                "Quantidade de registros de entrada e saída por mês.",
                new GraficoColunas(
                    MESES,
                    movimentosGrafico,
                    new Color[]{VERDE, AZUL_GRAFICO},
                    new String[]{"Entradas", "Saídas"},
                    false,
                    paginaRelatorios::numero
                )
            ),

            // GRÁFICO 3 - Estoque baixo
            criarCardGrafico(
                "Insumos com estoque baixo",
                "Insumos abaixo do estoque mínimo.",
                criarTabelaSimples(
                    new String[]{
                        "Insumo",
                        "Estoque atual",
                        "Estoque mínimo"
                    },
                    dadosBaixos,
                    1
                )
            )
        };


    // Tabela de insumos
    TabelaRelatorio tabela = new TabelaRelatorio(

        new String[]{
            "ID",
            "Nome",
            "Unidade de medida",
            "Estoque atual",
            "Estoque mínimo",
            "Total de entradas",
            "Total de consumo",
            "Status"
        },

        new Object[][]{},
        -1,
        "insumos"
    );

    tabela.carregarDados(insumosBanco);

    return montarRelatorio(
        "Relatório de insumos",
        "Visualize o estoque e a situação dos insumos cadastrados.",
        null,
        kpis,
        graficos,
        "Lista de insumos",
        tabela,
        "relatorio_insumos"
    );
}

    // =====================================================================
    // ABA 4: PRODUÇÃO
    // =====================================================================
    
private JPanel criarRelatorioProducao() {
    System.out.println("INICIANDO RELATORIO PRODUCAO");
    RelatoriosDAO dao = new RelatoriosDAO();

    int totalProducoes = 0;
    int totalUnidades = 0;
    int produtosDiferentes = 0;
    int registrosInsumos = 0;

    List<Object[]> producoesBanco = new ArrayList<>();
    List<Object[]> produtosBanco = new ArrayList<>();

    double[] producaoMensal = new double[12];

    int anoAtual = java.time.LocalDate.now().getYear();
    //System.out.println("INICIANDO CONSULTAS DE PRODUCAO");
    try {

        int[] indicadores = dao.indicadoresProducao();

        totalProducoes = indicadores[0];
        totalUnidades = indicadores[1];
        produtosDiferentes = indicadores[2];

        registrosInsumos =
            dao.totalRegistrosInsumosProducao();

        producoesBanco = dao.listarProducoes();
        produtosBanco = dao.producaoPorProduto();

        producaoMensal = dao.producaoPorMes(anoAtual);
        //System.out.println("CONSULTAS DE PRODUCAO FINALIZADAS");
    } catch (java.sql.SQLException ex) {

        ex.printStackTrace();
    }

    // Preparar dados dos gráficos
    String[] nomesProdutos = new String[produtosBanco.size()];
    double[] quantidades = new double[produtosBanco.size()];

    for (int i = 0; i < produtosBanco.size(); i++) {

        Object[] produto = produtosBanco.get(i);

        nomesProdutos[i] = (String) produto[0];
        quantidades[i] =
            ((Number) produto[1]).doubleValue();
    }

    double[] mesesGrafico =
        java.util.Arrays.copyOf(
            producaoMensal,
            MESES.length
        );

    // Indicadores reais
    JComponent[] kpis = {

        criarKpi(
            "Total de produções",
            String.valueOf(totalProducoes),
            null, null,
            "produções registradas"
        ),

        criarKpi(
            "Total de produtos produzidos",
            String.valueOf(totalUnidades),
            null, null,
            "unidades produzidas"
        ),

        criarKpi(
            "Produtos diferentes",
            String.valueOf(produtosDiferentes),
            null, null,
            "produtos produzidos"
        ),

        criarKpi(
            "Registros de insumos utilizados",
            String.valueOf(registrosInsumos),
            null, null,
            "itens registrados nas produções"
        )
    };

    // Gráficos com dados do MySQL
    JComponent[] graficos = {

        criarCardGrafico(
            "Produção por produto",
            "Quantidade produzida de cada produto.",
            new GraficoBarrasH(
                nomesProdutos,
                quantidades,
                paginaRelatorios::numero
            )
        ),

        criarCardGrafico(
            "Evolução da produção",
            "Quantidade produzida por mês em "
                + anoAtual + ".",
            new GraficoColunas(
                MESES,
                new double[][]{mesesGrafico},
                new Color[]{VERDE_COLUNA},
                null,
                true,
                paginaRelatorios::numero
            )
        ),

        criarCardGrafico(
            "Produção por produto (%)",
            "Participação dos produtos no total produzido.",
            new GraficoRosca(
                nomesProdutos,
                quantidades,
                CORES_ROSCA,
                String.valueOf(totalUnidades),
                "unidades",
                true
            )
        )
    };

    // Tabela com produções reais
    TabelaRelatorio tabela = new TabelaRelatorio(

        new String[]{
            "ID",
            "Data",
            "Produto",
            "Quantidade produzida",
            "Insumos utilizados (principais)",
            "Responsável",
            "Status"
        },

        new Object[][]{},
        1,
        "produções"
    );

    tabela.carregarDados(producoesBanco);

    return montarRelatorio(
        "Relatório de produção",
        "Visualize a produção dos produtos "
            + "e a utilização de insumos.",
        null,
        kpis,
        graficos,
        "Lista de produções",
        tabela,
        "relatorio_producao"
    );
}


    // =====================================================================
    // ABA 5: MOVIMENTAÇÃO
    // =====================================================================
    private JPanel criarRelatorioMovimentacao() {
        RelatoriosDAO dao = new RelatoriosDAO();
        int entradas = 0, saidas = 0, total = 0;
        double[][] mensal = new double[2][12];
        List<Object[]> produtos = new ArrayList<>();
        List<Object[]> motivos = new ArrayList<>();
        List<Object[]> registros = new ArrayList<>();
        int ano = LocalDate.now().getYear();
        try {
            int[] totais = dao.indicadoresMovimentacao();
            entradas = totais[0];
            saidas = totais[1];
            total = totais[2];
            mensal = dao.movimentacoesPorMes(ano);
            produtos = dao.topProdutosSaidas();
            motivos = dao.motivosSaidas();
            registros = dao.listarMovimentacoes();
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
        }
        String[] nomesProdutos = new String[produtos.size()];
        double[] quantidadesProdutos = new double[produtos.size()];
        for (int i = 0; i < produtos.size(); i++) {
            nomesProdutos[i] = String.valueOf(produtos.get(i)[0]);
            quantidadesProdutos[i] = ((Number) produtos.get(i)[1]).doubleValue();
        }
        String[] nomesMotivos = new String[motivos.size()];
        double[] quantidadesMotivos = new double[motivos.size()];
        for (int i = 0; i < motivos.size(); i++) {
            nomesMotivos[i] = String.valueOf(motivos.get(i)[0]);
            quantidadesMotivos[i] = ((Number) motivos.get(i)[1]).doubleValue();
        }
        JComponent[] kpis = {
            criarKpi("Registros de entrada", String.valueOf(entradas), null, null, "movimentações registradas"),
            criarKpi("Registros de saída", String.valueOf(saidas), null, null, "movimentações registradas"),
            criarKpi("Saldo de registros", String.valueOf(entradas - saidas), null, null, "entradas menos saídas"),
            criarKpi("Total de movimentações", String.valueOf(total), null, null, "registros no histórico")
        };
        JComponent[] graficos = {
            criarCardGrafico("Entradas x Saídas", "Número de movimentações por mês em " + ano + ".",
                new GraficoColunas(MESES, mensal,
                    new Color[]{VERDE, AZUL_GRAFICO}, new String[]{"Entradas", "Saídas"},
                    false, paginaRelatorios::numero)),
            criarCardGrafico("Produtos mais saídos", "Top 5 produtos por quantidade de saída.",
                new GraficoBarrasH(nomesProdutos, quantidadesProdutos, paginaRelatorios::unidades)),
            criarCardGrafico("Motivos de saída", "Distribuição das quantidades por motivo.",
                new GraficoRosca(nomesMotivos, quantidadesMotivos, CORES_ROSCA,
                    String.valueOf(saidas), "registros", false))
        };
        TabelaRelatorio tabela = new TabelaRelatorio(
            new String[]{"ID", "Data", "Tipo", "Produto/Insumo", "Quantidade", "Motivo", "Responsável", "Destino/Origem"},
            new Object[][]{}, 1, "movimentações");
        tabela.carregarDados(registros);
        return montarRelatorio("Relatório de movimentação",
            "Visualize as entradas e saídas de produtos e insumos no período.",
            null, kpis, graficos, "Últimas movimentações", tabela, "relatorio_movimentacao");
    }

    // =====================================================================
    // MONTAGEM DE UM RELATÓRIO (igual para todas as abas)
    // =====================================================================
    private JPanel montarRelatorio(String titulo, String subtitulo, String dicaBusca,
            JComponent[] kpis, JComponent[] graficos, String tituloLista,
            TabelaRelatorio tabela, String nomeArquivo) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BRANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(20, 24, 20, 24)));

        // --- Título + botão Exportar ---
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(criarTituloCard(titulo, subtitulo), BorderLayout.WEST);
        topo.add(criarBotaoExportar(tabela, titulo, nomeArquivo), BorderLayout.EAST);

        // --- Filtros: período e pesquisa ---
        JFormattedTextField campoDe = criarCampoData("01/01/" + LocalDate.now().getYear());
        JFormattedTextField campoAte = criarCampoData(LocalDate.now().format(FORMATO_DATA));
        CampoBusca busca = (dicaBusca != null) ? new CampoBusca(dicaBusca) : null;

        Runnable aplicarFiltros = () -> tabela.filtrar(
                lerData(campoDe.getText()),
                lerData(campoAte.getText()),
                busca == null ? "" : busca.getText());

        campoDe.addActionListener(e -> aplicarFiltros.run());   // ao apertar Enter
        campoAte.addActionListener(e -> aplicarFiltros.run());
        FocusAdapter aoSairDoCampo = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                aplicarFiltros.run();
            }
        };
        campoDe.addFocusListener(aoSairDoCampo);
        campoAte.addFocusListener(aoSairDoCampo);
        if (busca != null) {
            busca.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { aplicarFiltros.run(); }
                @Override public void removeUpdate(DocumentEvent e) { aplicarFiltros.run(); }
                @Override public void changedUpdate(DocumentEvent e) { aplicarFiltros.run(); }
            });
        }

        JLabel lblAte = new JLabel("até");
        lblAte.setFont(new Font(FONTE, Font.PLAIN, 14));
        lblAte.setForeground(AZUL_TITULO);

        JPanel periodo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        periodo.setOpaque(false);
        periodo.add(campoDe);
        periodo.add(Box.createHorizontalStrut(12));
        periodo.add(lblAte);
        periodo.add(Box.createHorizontalStrut(12));
        periodo.add(campoAte);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filtros.setOpaque(false);
        filtros.add(campo("Período", periodo));
        if (busca != null) {
            filtros.add(Box.createHorizontalStrut(24));
            filtros.add(campo("Pesquisar", busca));
        }

        // --- Cards de números (KPIs) ---
        JPanel linhaKpis = new JPanel(new GridLayout(1, kpis.length, 12, 0));
        linhaKpis.setOpaque(false);
        for (JComponent k : kpis) {
            linhaKpis.add(k);
        }

        // --- Gráficos ---
        JPanel linhaGraficos = new JPanel(new GridLayout(1, graficos.length, 12, 0));
        linhaGraficos.setOpaque(false);
        for (JComponent g : graficos) {
            linhaGraficos.add(g);
        }

        // --- Lista (tabela) ---
        JLabel lblLista = new JLabel(tituloLista);
        lblLista.setFont(new Font(FONTE, Font.BOLD, 17));
        lblLista.setForeground(AZUL_TITULO);

        JComponent[] partes = {topo, filtros, linhaKpis, linhaGraficos, lblLista, tabela};
        int[] espacos = {14, 16, 16, 20, 8, 0};
        for (int i = 0; i < partes.length; i++) {
            partes[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(partes[i]);
            card.add(Box.createVerticalStrut(espacos[i]));
        }

        return empilhar(card);
    }

    private JComponent criarBotaoExportar(TabelaRelatorio tabela, String titulo, String nomeArquivo) {
        JButton bt = botaoVerde("Exportar  ▼");
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemCsv = new JMenuItem("Exportar para Excel (.csv)");
        JMenuItem itemPdf = new JMenuItem("Imprimir / Salvar como PDF");
        itemCsv.setFont(new Font(FONTE, Font.PLAIN, 13));
        itemPdf.setFont(new Font(FONTE, Font.PLAIN, 13));
        itemCsv.addActionListener(e -> tabela.exportarCsv(nomeArquivo));
        itemPdf.addActionListener(e -> tabela.imprimir(titulo));
        menu.add(itemCsv);
        menu.add(itemPdf);
        bt.addActionListener(e -> menu.show(bt, 0, bt.getHeight()));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.add(bt);
        return p;
    }

    // =====================================================================
    // COMPONENTES VISUAIS
    // =====================================================================

    /** Card de número: título, valor grande, variação (↑/↓) e descrição. */
    private JPanel criarKpi(String titulo, String valor, String variacao, Boolean subiu, String descricao) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BRANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true),
                BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 4, 0, 0, VERDE), new EmptyBorder(12, 14, 12, 14))));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font(FONTE, Font.PLAIN, 13));
        lblTitulo.setForeground(AZUL_TITULO);

        JPanel linhaValor = new JPanel(new BorderLayout());
        linhaValor.setOpaque(false);
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font(FONTE, Font.BOLD, 26));
        lblValor.setForeground(AZUL_TITULO);
        linhaValor.add(lblValor, BorderLayout.WEST);
        if (variacao != null && subiu != null) {
            JLabel lblVariacao = new JLabel((subiu ? "↑ " : "↓ ") + variacao);
            lblVariacao.setFont(new Font(FONTE, Font.PLAIN, 14));
            lblVariacao.setForeground(subiu ? VERDE_SUBIU : VERMELHO);
            linhaValor.add(lblVariacao, BorderLayout.EAST);
        }

        JLabel lblDescricao = new JLabel(descricao);
        lblDescricao.setFont(new Font(FONTE, Font.PLAIN, 12));
        lblDescricao.setForeground(AZUL_TEXTO);

        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaValor.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDescricao.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblTitulo);
        card.add(linhaValor);
        card.add(lblDescricao);
        return card;
    }

    /** Card com título e subtítulo em cima e um gráfico embaixo. */
    private JPanel criarCardGrafico(String titulo, String subtitulo, JComponent grafico) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BRANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(14, 16, 14, 16)));

        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        cabecalho.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(new Font(FONTE, Font.BOLD, 15));
        t.setForeground(AZUL_TITULO);
        JLabel s = new JLabel(subtitulo);
        s.setFont(new Font(FONTE, Font.PLAIN, 12));
        s.setForeground(AZUL_TEXTO);
        cabecalho.add(t);
        cabecalho.add(s);

        card.add(cabecalho, BorderLayout.NORTH);
        card.add(grafico, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(300, 270));
        return card;
    }

    /** Tabela pequena, sem paginação (usada dentro de um card de gráfico). */
    private JComponent criarTabelaSimples(String[] colunas, Object[][] linhas, int colunaVermelha) {
        DefaultTableModel modelo = new DefaultTableModel(linhas, colunas) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        JTable t = new JTable(modelo);
        t.setRowHeight(26);
        t.setGridColor(BORDA);
        t.setFocusable(false);
        t.setRowSelectionAllowed(false);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabela, Object valor,
                    boolean selecionado, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(tabela, valor, false, false, linha, coluna);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                setBackground(BRANCO);
                boolean destaque = (coluna == colunaVermelha);
                setFont(new Font(FONTE, destaque ? Font.BOLD : Font.PLAIN, 12));
                setForeground(destaque ? VERMELHO : AZUL_TITULO);
                setHorizontalAlignment(coluna == 0 ? LEFT : CENTER);
                return this;
            }
        });
        estilizarCabecalho(t);

        JPanel caixa = new JPanel(new BorderLayout());
        caixa.setBorder(new LineBorder(BORDA));
        caixa.add(t.getTableHeader(), BorderLayout.NORTH);
        caixa.add(t, BorderLayout.CENTER);

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(caixa, BorderLayout.NORTH);
        return envoltorio;
    }

    private JFormattedTextField criarCampoData(String valor) {
        JFormattedTextField campo;
        try {
            MaskFormatter mascara = new MaskFormatter("##/##/####");
            mascara.setPlaceholderCharacter('_');
            campo = new JFormattedTextField(mascara);
        } catch (ParseException e) {
            campo = new JFormattedTextField();
        }
        campo.setValue(valor);
        campo.setFont(new Font(FONTE, Font.PLAIN, 14));
        campo.setForeground(AZUL_TITULO);
        campo.setPreferredSize(new Dimension(150, 38));
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(0, 12, 0, 12)));
        return campo;
    }

    private JPanel criarTituloCard(String titulo, String subtitulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel t = new JLabel(titulo);
        t.setFont(new Font(FONTE, Font.BOLD, 22));
        t.setForeground(AZUL_TITULO);

        JLabel s = new JLabel(subtitulo);
        s.setFont(new Font(FONTE, Font.PLAIN, 14));
        s.setForeground(AZUL_TEXTO);

        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(s);
        return p;
    }

    private JPanel empilhar(JComponent... itens) {
        JPanel coluna = new JPanel();
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        coluna.setOpaque(false);
        for (JComponent item : itens) {
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            coluna.add(item);
            coluna.add(Box.createVerticalStrut(16));
        }
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(coluna, BorderLayout.NORTH);
        return envoltorio;
    }

    private JPanel campo(String rotulo, JComponent componente) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(rotulo);
        l.setFont(new Font(FONTE, Font.PLAIN, 14));
        l.setForeground(AZUL_TITULO);
        p.add(l, BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    private JButton botaoVerde(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font(FONTE, Font.PLAIN, 15));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pintarBotao(b, VERDE, BRANCO, VERDE);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VERDE, 1, true), new EmptyBorder(10, 22, 10, 22)));
        return b;
    }

    /** No Windows o JButton ignora setBackground; desligar o contentArea resolve. */
    private static void pintarBotao(JButton b, Color fundo, Color texto, Color borda) {
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(fundo);
        b.setForeground(texto);
        b.setBorder(new LineBorder(borda, 1, true));
    }

    private static void estilizarCabecalho(JTable tabela) {
        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new Dimension(0, 32));
        cabecalho.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionado, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                setFont(new Font(FONTE, Font.BOLD, 12));
                setBackground(new Color(244, 246, 244));
                setForeground(AZUL_TITULO);
                setHorizontalAlignment(LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 1, BORDA), new EmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });
    }

    // =====================================================================
    // FORMATAÇÃO DE NÚMEROS E DATAS (padrão brasileiro)
    // =====================================================================
    private static String numero(double v) {
        return String.format(BR, "%,.0f", v);        // 1250 -> "1.250"
    }

    private static String moeda(double v) {
        return String.format(BR, "R$ %,.2f", v);     // 2450 -> "R$ 2.450,00"
    }

    private static String unidades(double v) {
        return numero(v) + " un";
    }

    private static LocalDate lerData(String texto) {
        try {
            return LocalDate.parse(texto, FORMATO_DATA);
        } catch (DateTimeParseException e) {
            return null; // data incompleta ou inválida = sem filtro
        }
    }

    /** Cor do degradê verde (escuro no primeiro item, claro no último). */
    private static Color corDegrade(int i, int total) {
        Color escuro = VERDE;
        Color claro = new Color(200, 228, 196);
        double t = (total <= 1) ? 0 : i / (double) (total - 1);
        return new Color(
                (int) (escuro.getRed() + t * (claro.getRed() - escuro.getRed())),
                (int) (escuro.getGreen() + t * (claro.getGreen() - escuro.getGreen())),
                (int) (escuro.getBlue() + t * (claro.getBlue() - escuro.getBlue())));
    }

    private static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    // =====================================================================
    // GRÁFICO DE BARRAS HORIZONTAIS (ranking "Top 5")
    // =====================================================================
    private static class GraficoBarrasH extends JComponent {
        private final String[] nomes;
        private final double[] valores;
        private final DoubleFunction<String> formato;

        GraficoBarrasH(String[] nomes, double[] valores, DoubleFunction<String> formato) {
            this.nomes = nomes;
            this.valores = valores;
            this.formato = formato;
            setPreferredSize(new Dimension(300, nomes.length * 29 + 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            g2.setFont(new Font(FONTE, Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();

            double max = 0;
            int larguraNome = 0;
            int larguraValor = 0;
            for (int i = 0; i < nomes.length; i++) {
                max = Math.max(max, valores[i]);
                larguraNome = Math.max(larguraNome, fm.stringWidth(nomes[i]));
                larguraValor = Math.max(larguraValor, fm.stringWidth(formato.apply(valores[i])));
            }

            int alturaLinha = 29;
            int alturaBarra = 18;
            int y0 = Math.max(0, (getHeight() - nomes.length * alturaLinha) / 2);
            int xBarra = larguraNome + 10;
            int larguraTrilho = Math.max(10, getWidth() - xBarra - larguraValor - 12);

            for (int i = 0; i < nomes.length; i++) {
                int y = y0 + i * alturaLinha + (alturaLinha - alturaBarra) / 2;
                int yTexto = y + (alturaBarra + fm.getAscent() - fm.getDescent()) / 2;

                g2.setColor(CINZA_TRILHO);
                g2.fillRoundRect(xBarra, y, larguraTrilho, alturaBarra, 4, 4);

                int largura = (max == 0) ? 0 : (int) (larguraTrilho * valores[i] / max);
                g2.setColor(corDegrade(i, nomes.length));
                g2.fillRoundRect(xBarra, y, largura, alturaBarra, 4, 4);

                g2.setColor(AZUL_TITULO);
                g2.drawString(nomes[i], larguraNome - fm.stringWidth(nomes[i]), yTexto);
                g2.drawString(formato.apply(valores[i]), xBarra + larguraTrilho + 8, yTexto);
            }
            g2.dispose();
        }
    }

    // =====================================================================
    // GRÁFICO DE COLUNAS (por mês) - aceita 1 ou mais séries
    // =====================================================================
    private static class GraficoColunas extends JComponent {
        private final String[] rotulos;
        private final double[][] series;
        private final Color[] cores;
        private final String[] nomesSeries;   // null = sem legenda
        private final boolean mostrarValores;
        private final DoubleFunction<String> formato;

        GraficoColunas(String[] rotulos, double[][] series, Color[] cores, String[] nomesSeries,
                boolean mostrarValores, DoubleFunction<String> formato) {
            this.rotulos = rotulos;
            this.series = series;
            this.cores = cores;
            this.nomesSeries = nomesSeries;
            this.mostrarValores = mostrarValores;
            this.formato = formato;
            setPreferredSize(new Dimension(360, 190));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            g2.setFont(new Font(FONTE, Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            int topo = 18;
            if (nomesSeries != null) {
                // legenda centralizada
                int larguraTotal = 0;
                for (String n : nomesSeries) {
                    larguraTotal += 16 + fm.stringWidth(n) + 20;
                }
                int x = (getWidth() - larguraTotal) / 2;
                for (int s = 0; s < nomesSeries.length; s++) {
                    g2.setColor(cores[s]);
                    g2.fillOval(x, 4, 10, 10);
                    g2.setColor(AZUL_TITULO);
                    g2.drawString(nomesSeries[s], x + 16, 13);
                    x += 16 + fm.stringWidth(nomesSeries[s]) + 20;
                }
                topo = 30;
            }

            // escala "redonda" do eixo Y (ex.: 0, 100, 200, 300, 400)
            double max = 1;
            for (double[] serie : series) {
                for (double v : serie) {
                    max = Math.max(max, v);
                }
            }
            double passoBruto = max / 4;
            double magnitude = Math.pow(10, Math.floor(Math.log10(passoBruto)));
            double normal = passoBruto / magnitude;
            double passo = (normal <= 1 ? 1 : normal <= 2 ? 2 : normal <= 2.5 ? 2.5 : normal <= 5 ? 5 : 10) * magnitude;
            double maxEixo = passo * 4;
            while (maxEixo < max) {
                maxEixo += passo;
            }
            int divisoes = (int) Math.round(maxEixo / passo);

            int esquerda = fm.stringWidth(formato.apply(maxEixo)) + 10;
            int base = getHeight() - 20;
            int altura = base - topo;
            int direita = getWidth() - 6;
            int larguraPlot = direita - esquerda;

            for (int k = 0; k <= divisoes; k++) {
                double v = passo * k;
                int y = base - (int) (altura * v / maxEixo);
                g2.setColor(new Color(235, 238, 235));
                g2.drawLine(esquerda, y, direita, y);
                String r = formato.apply(v);
                g2.setColor(AZUL_TEXTO);
                g2.drawString(r, esquerda - 6 - fm.stringWidth(r), y + 4);
            }

            int n = rotulos.length;
            double grupo = larguraPlot / (double) n;
            double larguraBarra = grupo * 0.62 / series.length;
            for (int i = 0; i < n; i++) {
                double xGrupo = esquerda + i * grupo + (grupo - larguraBarra * series.length) / 2;
                for (int s = 0; s < series.length; s++) {
                    double v = series[s][i];
                    int h = (int) (altura * v / maxEixo);
                    int x = (int) (xGrupo + s * larguraBarra);
                    int w = (int) Math.max(2, larguraBarra - 2);
                    g2.setColor(cores[s]);
                    g2.fillRoundRect(x, base - h, w, h, 4, 4);
                    if (mostrarValores) {
                        String txt = formato.apply(v);
                        g2.setColor(AZUL_TITULO);
                        g2.drawString(txt, x + (w - fm.stringWidth(txt)) / 2, base - h - 4);
                    }
                }
                g2.setColor(AZUL_TEXTO);
                g2.drawString(rotulos[i], (int) (esquerda + i * grupo + (grupo - fm.stringWidth(rotulos[i])) / 2), base + 15);
            }
            g2.dispose();
        }
    }

    // =====================================================================
    // GRÁFICO DE ROSCA (pizza com furo) + legenda
    // =====================================================================
    private static class GraficoRosca extends JComponent {
        private final String[] nomes;
        private final double[] valores;
        private final Color[] cores;
        private final String textoCentro;
        private final String subtextoCentro;
        private final boolean percentualNaLegenda;

        GraficoRosca(String[] nomes, double[] valores, Color[] cores,
                String textoCentro, String subtextoCentro, boolean percentualNaLegenda) {
            this.nomes = nomes;
            this.valores = valores;
            this.cores = cores;
            this.textoCentro = textoCentro;
            this.subtextoCentro = subtextoCentro;
            this.percentualNaLegenda = percentualNaLegenda;
            setPreferredSize(new Dimension(360, 190));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);

            double total = 0;
            for (double v : valores) {
                total += v;
            }
            int d = Math.min(getHeight() - 10, (int) (getWidth() * 0.42));
            int x = 4;
            int y = (getHeight() - d) / 2;

            g2.setFont(new Font(FONTE, Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            double inicio = 90; // começa no topo e vai no sentido horário
            for (int i = 0; i < valores.length; i++) {
                double angulo = 360 * valores[i] / total;
                g2.setColor(cores[i]);
                g2.fill(new Arc2D.Double(x, y, d, d, inicio, -angulo, Arc2D.PIE));

                if (angulo > 14) { // só escreve o % se a fatia for grande o suficiente
                    double meio = Math.toRadians(inicio - angulo / 2);
                    double raio = d * 0.40;
                    int cx = (int) (x + d / 2.0 + raio * Math.cos(meio));
                    int cy = (int) (y + d / 2.0 - raio * Math.sin(meio));
                    String p = Math.round(100 * valores[i] / total) + "%";
                    Color c = cores[i];
                    boolean escura = (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000 < 150;
                    g2.setColor(escura ? BRANCO : AZUL_TITULO);
                    g2.drawString(p, cx - fm.stringWidth(p) / 2, cy + fm.getAscent() / 2 - 1);
                }
                inicio -= angulo;
            }

            // furo central
            int dFuro = (int) (d * 0.58);
            g2.setColor(BRANCO);
            g2.fillOval(x + (d - dFuro) / 2, y + (d - dFuro) / 2, dFuro, dFuro);

            g2.setFont(new Font(FONTE, Font.BOLD, 20));
            FontMetrics fmGrande = g2.getFontMetrics();
            g2.setColor(AZUL_TITULO);
            g2.drawString(textoCentro, x + (d - fmGrande.stringWidth(textoCentro)) / 2, y + d / 2 + 4);
            g2.setFont(new Font(FONTE, Font.PLAIN, 11));
            g2.setColor(AZUL_TEXTO);
            g2.drawString(subtextoCentro, x + (d - fm.stringWidth(subtextoCentro)) / 2, y + d / 2 + 20);

            // legenda
            g2.setFont(new Font(FONTE, Font.PLAIN, 12));
            FontMetrics fmLegenda = g2.getFontMetrics();
            int lx = x + d + 18;
            int maiorNome = 0;
            for (String nome : nomes) {
                maiorNome = Math.max(maiorNome, fmLegenda.stringWidth(nome));
            }
            int ly = (getHeight() - nomes.length * 22) / 2 + 14;
            for (int i = 0; i < nomes.length; i++) {
                g2.setColor(cores[i]);
                g2.fillOval(lx, ly - 10, 10, 10);
                g2.setColor(AZUL_TITULO);
                g2.drawString(nomes[i], lx + 16, ly);
                if (percentualNaLegenda) {
                    String p = Math.round(100 * valores[i] / total) + "%";
                    g2.drawString(p, lx + 16 + maiorNome + 8, ly);
                }
                ly += 22;
            }
            g2.dispose();
        }
    }

    // =====================================================================
    // PAINEL QUE SEMPRE OCUPA A LARGURA DA JANELA (só rola na vertical)
    // =====================================================================
    private static class PainelLarguraJanela extends JPanel implements Scrollable {

        PainelLarguraJanela(LayoutManager layout) {
            super(layout);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientacao, int direcao) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientacao, int direcao) { return r.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // =====================================================================
    // CAMPO DE PESQUISA COM TEXTO DE DICA (placeholder)
    // =====================================================================
    private static class CampoBusca extends JTextField {
        private final String dica;

        CampoBusca(String dica) {
            this.dica = dica;
            setFont(new Font(FONTE, Font.PLAIN, 14));
            setForeground(AZUL_TITULO);
            setPreferredSize(new Dimension(380, 38));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDA, 1, true), new EmptyBorder(0, 12, 0, 12)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = suavizar(g);
                g2.setFont(getFont());
                g2.setColor(new Color(160, 170, 190));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(dica, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        }
    }

    // =====================================================================
    // "SELO" COLORIDO PARA STATUS (Ativo, Inativo, Entrada, Saída...)
    // =====================================================================
    private static class Selo extends JLabel {
        private Color corFundo = BRANCO;
        private Color corSelo = Color.LIGHT_GRAY;

        Selo() {
            setHorizontalAlignment(CENTER);
            setFont(new Font(FONTE, Font.PLAIN, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            g2.setColor(corFundo);
            g2.fillRect(0, 0, getWidth(), getHeight());
            FontMetrics fm = g2.getFontMetrics(getFont());
            int w = fm.stringWidth(getText()) + 22;
            int h = 20;
            g2.setColor(corSelo);
            g2.fillRoundRect((getWidth() - w) / 2, (getHeight() - h) / 2, w, h, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Desenha cada célula da tabela principal. */
    private static class Renderizador extends DefaultTableCellRenderer {
        private final Selo selo = new Selo();

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor,
                boolean selecionado, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(t, valor, selecionado, false, linha, coluna);
            Color fundo = selecionado ? VERDE_CLARO : (linha % 2 == 0 ? BRANCO : new Color(250, 251, 250));
            String nomeColuna = t.getColumnName(coluna);
            String texto = (valor == null) ? "" : valor.toString();

            if (nomeColuna.equals("Status") || nomeColuna.equals("Tipo")) {
                selo.setText(texto);
                selo.corFundo = fundo;
                if (STATUS_VERDE.contains(texto)) {
                    selo.corSelo = new Color(220, 240, 220);
                    selo.setForeground(new Color(30, 110, 50));
                } else if (STATUS_VERMELHO.contains(texto)) {
                    selo.corSelo = new Color(253, 224, 226);
                    selo.setForeground(new Color(198, 40, 40));
                } else {
                    selo.corSelo = new Color(235, 235, 235);
                    selo.setForeground(Color.DARK_GRAY);
                }
                return selo;
            }

            setBackground(fundo);
            setFont(new Font(FONTE, Font.PLAIN, 13));
            setForeground(AZUL_TITULO);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(LEFT);

            if (nomeColuna.equals("Ações")) {
                setForeground(VERDE);
                setFont(new Font(FONTE, Font.BOLD, 13));
                setHorizontalAlignment(CENTER);
            } else if (nomeColuna.equals("Estoque atual")) {
                // fica vermelho quando está abaixo do estoque mínimo
                int colunaMinimo = ((DefaultTableModel) t.getModel()).findColumn("Estoque mínimo");
                if (colunaMinimo >= 0) {
                    try {
                        double atual = Double.parseDouble(texto);
                        double minimo = Double.parseDouble(String.valueOf(t.getModel().getValueAt(linha, colunaMinimo)));
                        if (atual < minimo) {
                            setForeground(VERMELHO);
                            setFont(new Font(FONTE, Font.BOLD, 13));
                        }
                    } catch (NumberFormatException ignorado) {
                        // valor não numérico: deixa normal
                    }
                }
            }
            return this;
        }
    }

    // =====================================================================
    // TABELA COM FILTRO, PAGINAÇÃO, DETALHES E EXPORTAÇÃO
    // =====================================================================
    private static class TabelaRelatorio extends JPanel {
        private final String[] colunas;            // inclui a coluna "Ações"
        private final List<Object[]> todas = new ArrayList<>();
        private final List<Object[]> filtradas = new ArrayList<>();
        private final int colunaData;              // -1 = sem filtro de data
        private final String nomeItens;            // ex.: "clientes"
        private final DefaultTableModel modelo;
        private final JTable tabela;
        private final JLabel lblInfo = new JLabel();
        private final JPanel painelPaginas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        private int pagina = 0;
        private int porPagina = 5;

        TabelaRelatorio(String[] colunasDados, Object[][] linhas, int colunaData, String nomeItens) {
            this.colunas = Arrays.copyOf(colunasDados, colunasDados.length + 1);
            this.colunas[colunasDados.length] = "Ações";
            this.colunaData = colunaData;
            this.nomeItens = nomeItens;
            // TODO: trocar "linhas" pelos dados vindos do DAO
            todas.addAll(Arrays.asList(linhas));

            modelo = new DefaultTableModel(colunas, 0) {
                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }
            };
            tabela = new JTable(modelo);
            tabela.setRowHeight(32);
            tabela.setGridColor(BORDA);
            tabela.setSelectionBackground(VERDE_CLARO);
            tabela.setDefaultRenderer(Object.class, new Renderizador());
            estilizarCabecalho(tabela);

            TableColumn colunaId = tabela.getColumnModel().getColumn(0);
            colunaId.setPreferredWidth(40);
            colunaId.setMaxWidth(50);
            TableColumn colunaAcoes = tabela.getColumnModel().getColumn(colunas.length - 1);
            colunaAcoes.setPreferredWidth(60);
            colunaAcoes.setMaxWidth(70);

            // Clique em "Ver" mostra os detalhes da linha
            tabela.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int linha = tabela.rowAtPoint(e.getPoint());
                    int coluna = tabela.columnAtPoint(e.getPoint());
                    if (linha >= 0 && coluna == colunas.length - 1) {
                        mostrarDetalhes(linha);
                    }
                }
            });
            

            JPanel caixa = new JPanel(new BorderLayout());
            caixa.setBorder(new LineBorder(BORDA));
            caixa.add(tabela.getTableHeader(), BorderLayout.NORTH);
            caixa.add(tabela, BorderLayout.CENTER);

            lblInfo.setFont(new Font(FONTE, Font.PLAIN, 13));
            lblInfo.setForeground(AZUL_TEXTO);

            JComboBox<String> cbPorPagina = new JComboBox<>(new String[]{"5 por página", "10 por página", "20 por página"});
            cbPorPagina.setFont(new Font(FONTE, Font.PLAIN, 13));
            cbPorPagina.setBackground(BRANCO);
            cbPorPagina.addActionListener(e -> {
                porPagina = new int[]{5, 10, 20}[cbPorPagina.getSelectedIndex()];
                pagina = 0;
                atualizarPagina();
            });

            painelPaginas.setOpaque(false);
            JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
            direita.setOpaque(false);
            direita.add(painelPaginas);
            direita.add(cbPorPagina);

            JPanel rodape = new JPanel(new BorderLayout());
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(12, 0, 0, 0));
            rodape.add(lblInfo, BorderLayout.WEST);
            rodape.add(direita, BorderLayout.EAST);

            setLayout(new BorderLayout());
            setOpaque(false);
            add(caixa, BorderLayout.CENTER);
            add(rodape, BorderLayout.SOUTH);

            filtrar(null, null, "");
        }

        /** Filtra pelo período (se a tabela tiver data) e pelo texto pesquisado. */
        void filtrar(LocalDate de, LocalDate ate, String texto) {
            String busca = (texto == null) ? "" : texto.trim().toLowerCase();
            filtradas.clear();
            for (Object[] linha : todas) {
                if (colunaData >= 0) {
                    LocalDate data = lerData(String.valueOf(linha[colunaData]));
                    if (data != null && de != null && data.isBefore(de)) {
                        continue;
                    }
                    if (data != null && ate != null && data.isAfter(ate)) {
                        continue;
                    }
                }
                if (!busca.isEmpty()) {
                    boolean achou = false;
                    for (Object valor : linha) {
                        if (String.valueOf(valor).toLowerCase().contains(busca)) {
                            achou = true;
                            break;
                        }
                    }
                    if (!achou) {
                        continue;
                    }
                }
                filtradas.add(linha);
            }
            pagina = 0;
            atualizarPagina();
        }

        private void irPara(int novaPagina) {
            pagina = novaPagina;
            atualizarPagina();
        }

        private void atualizarPagina() {
            modelo.setRowCount(0);
            int total = filtradas.size();
            int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) porPagina));
            if (pagina >= totalPaginas) {
                pagina = totalPaginas - 1;
            }
            int inicio = pagina * porPagina;
            int fim = Math.min(inicio + porPagina, total);
            for (int i = inicio; i < fim; i++) {
                Object[] dados = filtradas.get(i);
                Object[] linha = Arrays.copyOf(dados, dados.length + 1);
                linha[dados.length] = "Ver";
                modelo.addRow(linha);
            }

            lblInfo.setText(total == 0
                    ? "Nenhum registro encontrado."
                    : "Mostrando " + (inicio + 1) + " a " + fim + " de " + total + " " + nomeItens);

            // botões de página: < 1 2 3 4 5 >
            painelPaginas.removeAll();
            painelPaginas.add(botaoPagina("<", false, pagina > 0, () -> irPara(pagina - 1)));
            int primeira = Math.max(0, pagina - 2);
            int ultima = Math.min(totalPaginas, primeira + 5);
            primeira = Math.max(0, ultima - 5);
            for (int p = primeira; p < ultima; p++) {
                final int destino = p;
                painelPaginas.add(botaoPagina(String.valueOf(p + 1), p == pagina, true, () -> irPara(destino)));
            }
            painelPaginas.add(botaoPagina(">", false, pagina < totalPaginas - 1, () -> irPara(pagina + 1)));

            revalidate();
            repaint();
        }

        private JButton botaoPagina(String texto, boolean ativo, boolean habilitado, Runnable acao) {
            JButton b = new JButton(texto);
            b.setFont(new Font(FONTE, Font.PLAIN, 13));
            b.setFocusPainted(false);
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setPreferredSize(new Dimension(36, 34));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pintarBotao(b, ativo ? VERDE : BRANCO, ativo ? BRANCO : AZUL_TITULO, ativo ? VERDE : BORDA);
            b.setEnabled(habilitado);
            b.addActionListener(e -> acao.run());
            return b;
        }

        private void mostrarDetalhes(int linha) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < colunas.length - 1; c++) {
                sb.append(colunas[c]).append(": ").append(modelo.getValueAt(linha, c)).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Detalhes", JOptionPane.INFORMATION_MESSAGE);
        }

        /** Salva as linhas filtradas num .csv (abre no Excel). */
        void exportarCsv(String nomeArquivo) {
            JFileChooser escolha = new JFileChooser();
            escolha.setSelectedFile(new File(nomeArquivo + ".csv"));
            escolha.setFileFilter(new FileNameExtensionFilter("Planilha CSV", "csv"));
            if (escolha.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File arquivo = escolha.getSelectedFile();
            if (!arquivo.getName().toLowerCase().endsWith(".csv")) {
                arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".csv");
            }
            try (Writer w = new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8)) {
                w.write('﻿'); // faz o Excel reconhecer os acentos
                w.write(linhaCsv(Arrays.copyOf(colunas, colunas.length - 1)));
                for (Object[] linha : filtradas) {
                    w.write(linhaCsv(linha));
                }
                JOptionPane.showMessageDialog(this, "Arquivo salvo em:\n" + arquivo.getAbsolutePath(),
                        "Exportado", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível salvar o arquivo:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        private static String linhaCsv(Object[] valores) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < valores.length; i++) {
                String s = String.valueOf(valores[i]);
                if (s.contains(";") || s.contains("\"")) {
                    s = "\"" + s.replace("\"", "\"\"") + "\"";
                }
                if (i > 0) {
                    sb.append(';'); // Excel em português usa ";" como separador
                }
                sb.append(s);
            }
            return sb.append("\r\n").toString();
        }

        /** Imprime todas as linhas filtradas (no Windows dá para escolher "Microsoft Print to PDF"). */
        void imprimir(String titulo) {
            int porPaginaAntes = porPagina;
            int paginaAntes = pagina;
            porPagina = Math.max(1, filtradas.size());
            pagina = 0;
            atualizarPagina();
            tabela.setSize(tabela.getWidth(), tabela.getPreferredSize().height);
            try {
                tabela.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat(titulo), new MessageFormat("Página {0}"));
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível imprimir:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
            porPagina = porPaginaAntes;
            pagina = paginaAntes;
            atualizarPagina();
        }
        
public void carregarDados(List<Object[]> dados) {
    todas.clear();
    todas.addAll(dados);
    filtrar(null, null, "");
}

    }
    

}
