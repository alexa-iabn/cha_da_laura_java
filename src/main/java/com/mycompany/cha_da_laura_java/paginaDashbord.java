/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.cha_da_laura_java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Dimension;
import java.util.Locale;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.*;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JProgressBar;
import javax.swing.*;
import javax.swing.table.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class paginaDashbord extends javax.swing.JPanel {

    /**
     * Creates new form paginaDashbord
     */
    public paginaDashbord() {
        initComponents();
        carregarGraficoProducaoProdutos();
        carregarGraficoEvolucaoSaida();
        carregarGraficoComposicaoEstoque();
        carregarGraficoInsumosMaisConsumidos();
        carregarTabelaEstoqueBaixo();
        carregarTabelaUltimasMovimentacoes();
        
    }

    private void carregarGraficoProducaoProdutos(){
        DefaultCategoryDataset dados = new DefaultCategoryDataset();
        
        dados.addValue(32, "Produção","Blend Calme");
        dados.addValue(25, "Produção","Blend Felicitá");
        dados.addValue(20, "Produção","Blend Ormoni");
        dados.addValue(15, "Produção","Blend MaterniTea");
        dados.addValue(10, "Produção","Blend Airmid");
        dados.addValue(8, "Produção","Blend Masala");
        
        JFreeChart grafico = ChartFactory.createBarChart(
                "Produção por produto",
                "",
                "",
                dados
        );
        
        grafico.removeLegend();
        
        CategoryPlot plot = grafico.getCategoryPlot();
        
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(230,230,230));
        
        BarRenderer barras = (BarRenderer) plot.getRenderer();
        
        barras.setSeriesPaint(0, new Color(126,190,160));
        barras.setShadowVisible(false);
        barras.setDrawBarOutline(false);
        barras.setMaximumBarWidth(0.12);
        
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI",Font.PLAIN,8));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI",Font.PLAIN,10));
        
        ChartPanel painel = new ChartPanel(grafico);
        
        painel.setPreferredSize(new Dimension(480,175));
        painel.setMinimumDrawWidth(0);
        painel.setMinimumDrawHeight(0);
        painel.setMaximumDrawWidth(480);
        painel.setMaximumDrawHeight(175);
        
        graficoProducaoPorProduto.removeAll();
        graficoProducaoPorProduto.setLayout(new BorderLayout());
        graficoProducaoPorProduto.add(painel,BorderLayout.CENTER);
        graficoProducaoPorProduto.revalidate();
        graficoProducaoPorProduto.repaint();
    }
    
    private void carregarGraficoEvolucaoSaida(){
        
        DefaultCategoryDataset dados = new DefaultCategoryDataset();
        
        dados.addValue(20,"Saídas","Abr");
        dados.addValue(35,"Saídas","Mai");
        dados.addValue(42,"Saídas","Jun");
        dados.addValue(58,"Saídas","Jul");
        dados.addValue(52,"Saídas","Ago");
        dados.addValue(70,"Saídas","Set");
        
        JFreeChart grafico = ChartFactory.createLineChart(
                "Evolução das saídas",
                "",
                "",
                dados,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        
        CategoryPlot plot = grafico.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(230,230,230));
        
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        
        renderer.setSeriesPaint(0, new Color(55,130, 190));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);
        grafico.setBackgroundPaint(Color.WHITE);
        grafico.getTitle().setFont(new Font("SansSerif", Font.BOLD,15));
        
        ChartPanel painel = new ChartPanel(grafico);
        
        painel.setMouseWheelEnabled(false);
        painel.setPreferredSize(new Dimension(480,175));
        painel.setMinimumDrawWidth(0);
        painel.setMinimumDrawHeight(0);
        painel.setMaximumDrawWidth(480);
        painel.setMaximumDrawHeight(175);
        
        graficoEvolucaoDasSaidas.removeAll();
        graficoEvolucaoDasSaidas.setLayout(new java.awt.BorderLayout());
        graficoEvolucaoDasSaidas.add(painel,java.awt.BorderLayout.CENTER);
        graficoEvolucaoDasSaidas.revalidate();
        graficoEvolucaoDasSaidas.repaint();
    }
    
    private void carregarGraficoComposicaoEstoque(){
        DefaultPieDataset<String> dados = new DefaultPieDataset<>();
        
        dados.setValue("Ervas", 35);
        dados.setValue("Especiarias", 20);
        dados.setValue("Frutas", 15);
        dados.setValue("Flores", 10);
        dados.setValue("Adoçantes", 8);
        dados.setValue("Outros", 12);
        
        JFreeChart grafico = ChartFactory.createRingChart(
                "Composição de estoque(insumos)",
                dados,
                true,
                true,
                false
        );
        
        RingPlot plot = (RingPlot)grafico.getPlot();
        
        plot.setSectionPaint("Ervas", new Color(43,110,72));
        plot.setSectionPaint("Especiarias", new Color(126,211,137));
        plot.setSectionPaint("Frutas", new Color(166,203,245));
        plot.setSectionPaint("Flores", new Color(228,207,166));
        plot.setSectionPaint("Adoçantes", new Color(248,183,197));
        plot.setSectionPaint("Outros", new Color(205,205,205));
        
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        
        plot.setSectionOutlinesVisible(false);
        plot.setSeparatorsVisible(false);
        
        plot.setSectionDepth(0.35);
        
        plot.setLabelGenerator(null);
        
        plot.setCenterText("20\ninsumos");
        plot.setCenterTextFont(new Font("SansSerif", Font.BOLD,20));
        plot.setCenterTextColor(new Color(30,45,55));
        
        grafico.setBackgroundPaint(Color.WHITE);
        grafico.getTitle().setFont(new Font("SansSerif", Font.BOLD,18));
        grafico.getTitle().setPaint(new Color(25,40,50));
        
        if(grafico.getLegend() != null){
            grafico.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN,13));
            grafico.getLegend().setBackgroundPaint(Color.WHITE);
        }
        
        ChartPanel painel = new ChartPanel(grafico);
        
        painel.setMouseWheelEnabled(false);
        painel.setPreferredSize(new Dimension(317,181));
        painel.setMinimumDrawWidth(0);
        painel.setMinimumDrawHeight(0);
        painel.setMaximumDrawWidth(317);
        painel.setMaximumDrawHeight(181);
        
        graficoComposicaoEstoque.removeAll();
        graficoComposicaoEstoque.setLayout(new BorderLayout());
        graficoComposicaoEstoque.add(painel,BorderLayout.CENTER);
        graficoComposicaoEstoque.revalidate();
        graficoComposicaoEstoque.repaint();
    }
    
    private void carregarGraficoInsumosMaisConsumidos(){
        String[] insumos = {
            "Camomila",
            "Hortelã",
            "Erva-doce",
            "Hibisco",
            "Chá verde"
        };
        
        int[] quantidades = {12, 9, 7, 6, 5};
        
        int maiorQuantidade = 12;
        
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel titulo = new JLabel("Insumos mais consumidos");
        
        titulo.setFont( new Font("SansSerif", Font.BOLD, 20));
        
        titulo.setForeground(new Color(30, 45, 55));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 20, 25, 20);
        
        painel.add(titulo,gbc);
        
        for(int i=0; i < insumos.length; i++){
            JLabel nome = new JLabel(insumos[i]);
            
            nome.setFont(new Font("SansSerif", Font.PLAIN, 15));
            
            nome.setForeground(new Color(40,55,65));
            
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(8,20,8,15);
            
            painel.add(nome,gbc);
            
            JProgressBar barra = new JProgressBar();
            
            barra.setMinimum(0);
            barra.setMaximum(maiorQuantidade);
            barra.setValue(quantidades[i]);
            
            barra.setForeground(new Color(104,163,125));
            barra.setBackground(new Color(235,235,235));
            
            barra.setBorderPainted(false);
            barra.setStringPainted(false);
            
            barra.setPreferredSize(new Dimension(200,30));
            
            gbc.gridx = 1;
            gbc.gridy = i +1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8,5,8,15);
            
            painel.add(barra,gbc);
            
            JLabel quantidade = new JLabel(quantidades[i] + "kg");
            
            quantidade.setFont(new Font("SansSerif", Font.PLAIN, 15));
            
            quantidade.setForeground(new Color(40,55,65));
            
            gbc.gridx = 2;
            gbc.gridy = i + 1;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(8,5,8,20);
            
            painel.add(quantidade,gbc);
        }
        
        gbc.gridx = 0;
        gbc.gridy = insumos.length + 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0,0,0,0);
        
        painel.add(new JPanel(){{
            setOpaque(false);
        }}, gbc);
        
        painel.setPreferredSize(new Dimension(317,181));
        painel.setMinimumSize(new Dimension(317,181));
        painel.setBorder(BorderFactory.createEmptyBorder(15,0,0,0));
        
        graficoInsumosMaisConsumidos.removeAll();
        graficoInsumosMaisConsumidos.setLayout(new BorderLayout());
        graficoInsumosMaisConsumidos.add(painel, BorderLayout.CENTER);
        graficoInsumosMaisConsumidos.revalidate();
        graficoInsumosMaisConsumidos.repaint();
    }
    
    private void carregarTabelaEstoqueBaixo() {

        tabelaEstoqueBaixo.removeAll();
        tabelaEstoqueBaixo.setLayout(new BorderLayout());

        // PAINEL PRINCIPAL
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(Color.WHITE);

        painel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        // CABEÇALHO
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);

        JLabel titulo = new JLabel("Estoque baixo");

        titulo.setFont(
            new Font("SansSerif", Font.BOLD, 14)
        );

        titulo.setForeground(new Color(35, 45, 45));

        cabecalho.add(titulo, BorderLayout.WEST);

        // COLUNAS
        String[] colunas = {
            "Nome",
            "Tipo",
            "Estoque atual",
            "Estoque mínimo"
        };

        // DADOS DE EXEMPLO
        Object[][] dados = {
            {"Camomila", "Insumo", 5, 10},
            {"Hortelã", "Insumo", 7, 10},
            {"Blend Ormoni (30g)", "Produto", 6, 10},
            {"Melissa", "Insumo", 8, 10},
            {"Chai Masala (80g)", "Produto", 5, 10}
        };

        DefaultTableModel modelo = new DefaultTableModel(
            dados, colunas
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabela = new JTable(modelo);

        tabela.setFont(
            new Font("SansSerif", Font.PLAIN, 12)
        );

        tabela.setRowHeight(27);

        tabela.setShowGrid(true);
        tabela.setGridColor(new Color(235, 235, 235));

        tabela.setBackground(Color.WHITE);
        tabela.setSelectionBackground(new Color(220, 240, 225));

        tabela.setFillsViewportHeight(true);

        // CABEÇALHO DA TABELA
        JTableHeader header = tabela.getTableHeader();

        header.setFont(
            new Font("SansSerif", Font.BOLD, 10)
        );

        header.setBackground(new Color(235, 242, 235));
        header.setForeground(new Color(35, 65, 45));
        header.setPreferredSize(new Dimension(0, 28));

        // CENTRALIZAR COLUNAS
        DefaultTableCellRenderer centralizado =
            new DefaultTableCellRenderer();

        centralizado.setHorizontalAlignment(
            SwingConstants.CENTER
        );

        tabela.getColumnModel().getColumn(1)
            .setCellRenderer(centralizado);

        tabela.getColumnModel().getColumn(3)
            .setCellRenderer(centralizado);

        // ESTOQUE ATUAL EM VERMELHO
        DefaultTableCellRenderer vermelho =
            new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
            ) {

                Component c = super.getTableCellRendererComponent(
                    table, value, isSelected,
                    hasFocus, row, column
                );

                setHorizontalAlignment(SwingConstants.CENTER);

                if (!isSelected) {
                    c.setForeground(new Color(200, 50, 60));
                }

                c.setFont(
                    new Font("SansSerif", Font.BOLD, 12)
                );

                return c;
            }
        };

        tabela.getColumnModel().getColumn(2)
            .setCellRenderer(vermelho);

        // AJUSTAR LARGURA DAS COLUNAS
        tabela.setAutoResizeMode(
            JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        tabela.getColumnModel().getColumn(0)
            .setPreferredWidth(140);

        tabela.getColumnModel().getColumn(1)
            .setPreferredWidth(65);

        tabela.getColumnModel().getColumn(2)
            .setPreferredWidth(90);

        tabela.getColumnModel().getColumn(3)
            .setPreferredWidth(90);

        // SCROLL
        JScrollPane scroll = new JScrollPane(tabela);

        scroll.setBorder(
            BorderFactory.createEmptyBorder()
        );

        scroll.getViewport().setBackground(Color.WHITE);

        // ADICIONAR COMPONENTES
        painel.add(cabecalho, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);

        tabelaEstoqueBaixo.add(
            painel, BorderLayout.CENTER
        );

        tabelaEstoqueBaixo.revalidate();
        tabelaEstoqueBaixo.repaint();
    }
    
    private void carregarTabelaUltimasMovimentacoes() {

        tabelaUltimasMovimentacoes.removeAll();
        tabelaUltimasMovimentacoes.setLayout(new BorderLayout());

        // PAINEL PRINCIPAL
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setBackground(Color.WHITE);

        painel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        // TÍTULO
        JLabel titulo = new JLabel("Últimas movimentações");

        titulo.setFont(
            new Font("SansSerif", Font.BOLD, 12)
        );

        titulo.setForeground(new Color(35, 45, 45));

        // COLUNAS
        String[] colunas = {
            "Data",
            "Tipo",
            "Descrição",
            "Produto/Insumo",
            "Quantidade",
            "Responsável"
        };

        // DADOS DE EXEMPLO
        Object[][] dados = {
            {"29/09/2026", "Saída", "Venda (cliente)",
             "Blend Calme", "5 un.", "Usuário"},

            {"28/09/2026", "Produção", "Produção realizada",
             "Blend Felicitá", "20 un.", "Usuário"},

            {"27/09/2026", "Entrada", "Compra (fornecedor)",
             "Camomila", "2 kg", "Usuário"},

            {"27/09/2026", "Saída", "E-commerce",
             "Blend Ormoni", "3 un.", "Usuário"},

            {"26/09/2026", "Produção", "Produção realizada",
             "Chai Masala", "15 un.", "Usuário"}
        };

        DefaultTableModel modelo = new DefaultTableModel(
            dados, colunas
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabela = new JTable(modelo);

        tabela.setFont(
            new Font("SansSerif", Font.PLAIN, 11)
        );

        tabela.setRowHeight(22);

        tabela.setShowGrid(true);
        tabela.setGridColor(new Color(235, 235, 235));

        tabela.setBackground(Color.WHITE);

        tabela.setSelectionBackground(
            new Color(225, 240, 230)
        );

        tabela.setFillsViewportHeight(true);

        // CABEÇALHO
        JTableHeader header = tabela.getTableHeader();

        header.setFont(
            new Font("SansSerif", Font.BOLD, 11)
        );

        header.setBackground(
            new Color(235, 242, 235)
        );

        header.setForeground(
            new Color(35, 65, 45)
        );

        header.setPreferredSize(
            new Dimension(0, 20)
        );

        // CENTRALIZAR TIPO E QUANTIDADE
        DefaultTableCellRenderer centralizado =
            new DefaultTableCellRenderer();

        centralizado.setHorizontalAlignment(
            SwingConstants.CENTER
        );

        tabela.getColumnModel().getColumn(4)
            .setCellRenderer(centralizado);

        // CORES DOS TIPOS DE MOVIMENTAÇÃO
        DefaultTableCellRenderer tipoRenderer =
            new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
            ) {

                super.getTableCellRendererComponent(
                    table, value, isSelected,
                    hasFocus, row, column
                );

                setHorizontalAlignment(SwingConstants.CENTER);
                setOpaque(true);

                if (!isSelected) {

                    String tipo = value.toString();

                    switch (tipo) {

                        case "Saída":
                            setBackground(new Color(255, 220, 225));
                            setForeground(new Color(170, 55, 65));
                            break;

                        case "Entrada":
                            setBackground(new Color(215, 235, 255));
                            setForeground(new Color(45, 100, 160));
                            break;

                        case "Produção":
                            setBackground(new Color(215, 245, 220));
                            setForeground(new Color(45, 120, 65));
                            break;

                        default:
                            setBackground(Color.WHITE);
                            setForeground(Color.BLACK);
                    }

                }

                return this;
            }
        };

        tabela.getColumnModel().getColumn(1)
            .setCellRenderer(tipoRenderer);

        // AJUSTAR LARGURA DAS COLUNAS
        tabela.setAutoResizeMode(
            JTable.AUTO_RESIZE_ALL_COLUMNS
        );
        
        tabela.setRowHeight(22);
        
        tabela.getTableHeader().setPreferredSize(new Dimension(0,20));

        tabela.getColumnModel().getColumn(0)
            .setPreferredWidth(95);

        tabela.getColumnModel().getColumn(1)
            .setPreferredWidth(85);

        tabela.getColumnModel().getColumn(2)
            .setPreferredWidth(160);

        tabela.getColumnModel().getColumn(3)
            .setPreferredWidth(140);

        tabela.getColumnModel().getColumn(4)
            .setPreferredWidth(90);

        tabela.getColumnModel().getColumn(5)
            .setPreferredWidth(100);

        // SCROLL
        JScrollPane scroll = new JScrollPane(tabela);

        scroll.setBorder(
            BorderFactory.createEmptyBorder()
        );

        scroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );
        
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        scroll.getViewport().setBackground(Color.WHITE);

        // ADICIONAR AO PAINEL
        painel.add(titulo, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);

        tabelaUltimasMovimentacoes.add(
            scroll, BorderLayout.CENTER
        );

        tabelaUltimasMovimentacoes.revalidate();
        tabelaUltimasMovimentacoes.repaint();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        graficoProducaoPorProduto = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        graficoEvolucaoDasSaidas = new javax.swing.JPanel();
        graficoComposicaoEstoque = new javax.swing.JPanel();
        tabelaEstoqueBaixo = new javax.swing.JPanel();
        graficoInsumosMaisConsumidos = new javax.swing.JPanel();
        tabelaUltimasMovimentacoes = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(1100, 600));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(600, 500));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel6.setBackground(new java.awt.Color(200, 249, 194));

        jLabel1.setBackground(new java.awt.Color(200, 249, 194));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/iconeCaixa.png"))); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
        );

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel8.setText("Produtos em estoque");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setText("42");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7))
                .addContainerGap(84, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel7.setBackground(new java.awt.Color(200, 249, 194));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/iconeFolha.png"))); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
        );

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel9.setText("Insumos em estoque");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel13.setText("20");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel13))
                .addContainerGap(86, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13))
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel8.setBackground(new java.awt.Color(200, 249, 194));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/iconeConfiguracao.png"))); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
        );

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel10.setText("Produções realizadas");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel12.setText("18");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel12))
                .addContainerGap(87, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12))
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel9.setBackground(new java.awt.Color(200, 249, 194));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/iconeSaida.png"))); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
        );

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel11.setText("Total de saídas");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel14.setText("56");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel14))
                .addContainerGap(115, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14))
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel5.setBackground(new java.awt.Color(0, 51, 0));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 51, 0));
        jLabel5.setText("Dashbord");
        jLabel5.setMinimumSize(new java.awt.Dimension(111, 20));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(153, 153, 153));
        jLabel6.setText("Visão geral do seu negócio");

        graficoProducaoPorProduto.setBackground(new java.awt.Color(255, 255, 255));
        graficoProducaoPorProduto.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        graficoProducaoPorProduto.setPreferredSize(new java.awt.Dimension(480, 175));

        javax.swing.GroupLayout graficoProducaoPorProdutoLayout = new javax.swing.GroupLayout(graficoProducaoPorProduto);
        graficoProducaoPorProduto.setLayout(graficoProducaoPorProdutoLayout);
        graficoProducaoPorProdutoLayout.setHorizontalGroup(
            graficoProducaoPorProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        graficoProducaoPorProdutoLayout.setVerticalGroup(
            graficoProducaoPorProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 173, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        graficoEvolucaoDasSaidas.setBackground(new java.awt.Color(255, 255, 255));
        graficoEvolucaoDasSaidas.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        graficoEvolucaoDasSaidas.setPreferredSize(new java.awt.Dimension(320, 163));

        javax.swing.GroupLayout graficoEvolucaoDasSaidasLayout = new javax.swing.GroupLayout(graficoEvolucaoDasSaidas);
        graficoEvolucaoDasSaidas.setLayout(graficoEvolucaoDasSaidasLayout);
        graficoEvolucaoDasSaidasLayout.setHorizontalGroup(
            graficoEvolucaoDasSaidasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        graficoEvolucaoDasSaidasLayout.setVerticalGroup(
            graficoEvolucaoDasSaidasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 173, Short.MAX_VALUE)
        );

        graficoComposicaoEstoque.setBackground(new java.awt.Color(255, 255, 255));
        graficoComposicaoEstoque.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        javax.swing.GroupLayout graficoComposicaoEstoqueLayout = new javax.swing.GroupLayout(graficoComposicaoEstoque);
        graficoComposicaoEstoque.setLayout(graficoComposicaoEstoqueLayout);
        graficoComposicaoEstoqueLayout.setHorizontalGroup(
            graficoComposicaoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 315, Short.MAX_VALUE)
        );
        graficoComposicaoEstoqueLayout.setVerticalGroup(
            graficoComposicaoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 179, Short.MAX_VALUE)
        );

        tabelaEstoqueBaixo.setBackground(new java.awt.Color(255, 255, 255));
        tabelaEstoqueBaixo.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        tabelaEstoqueBaixo.setPreferredSize(new java.awt.Dimension(315, 179));

        javax.swing.GroupLayout tabelaEstoqueBaixoLayout = new javax.swing.GroupLayout(tabelaEstoqueBaixo);
        tabelaEstoqueBaixo.setLayout(tabelaEstoqueBaixoLayout);
        tabelaEstoqueBaixoLayout.setHorizontalGroup(
            tabelaEstoqueBaixoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 313, Short.MAX_VALUE)
        );
        tabelaEstoqueBaixoLayout.setVerticalGroup(
            tabelaEstoqueBaixoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        graficoInsumosMaisConsumidos.setBackground(new java.awt.Color(255, 255, 255));
        graficoInsumosMaisConsumidos.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        graficoInsumosMaisConsumidos.setPreferredSize(new java.awt.Dimension(317, 181));

        javax.swing.GroupLayout graficoInsumosMaisConsumidosLayout = new javax.swing.GroupLayout(graficoInsumosMaisConsumidos);
        graficoInsumosMaisConsumidos.setLayout(graficoInsumosMaisConsumidosLayout);
        graficoInsumosMaisConsumidosLayout.setHorizontalGroup(
            graficoInsumosMaisConsumidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        graficoInsumosMaisConsumidosLayout.setVerticalGroup(
            graficoInsumosMaisConsumidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        tabelaUltimasMovimentacoes.setBackground(new java.awt.Color(255, 255, 255));
        tabelaUltimasMovimentacoes.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        tabelaUltimasMovimentacoes.setPreferredSize(new java.awt.Dimension(991, 102));

        javax.swing.GroupLayout tabelaUltimasMovimentacoesLayout = new javax.swing.GroupLayout(tabelaUltimasMovimentacoes);
        tabelaUltimasMovimentacoes.setLayout(tabelaUltimasMovimentacoesLayout);
        tabelaUltimasMovimentacoesLayout.setHorizontalGroup(
            tabelaUltimasMovimentacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        tabelaUltimasMovimentacoesLayout.setVerticalGroup(
            tabelaUltimasMovimentacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tabelaUltimasMovimentacoes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel6)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(26, 26, 26)
                                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(graficoProducaoPorProduto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(26, 26, 26)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(31, 31, 31)
                                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(graficoEvolucaoDasSaidas, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(graficoComposicaoEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(graficoInsumosMaisConsumidos, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                            .addGap(26, 26, 26)
                            .addComponent(tabelaEstoqueBaixo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(607, 607, 607))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(graficoEvolucaoDasSaidas, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(graficoProducaoPorProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tabelaEstoqueBaixo, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                            .addComponent(graficoInsumosMaisConsumidos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(graficoComposicaoEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tabelaUltimasMovimentacoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1100, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 756, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel graficoComposicaoEstoque;
    private javax.swing.JPanel graficoEvolucaoDasSaidas;
    private javax.swing.JPanel graficoInsumosMaisConsumidos;
    private javax.swing.JPanel graficoProducaoPorProduto;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel tabelaEstoqueBaixo;
    private javax.swing.JPanel tabelaUltimasMovimentacoes;
    // End of variables declaration//GEN-END:variables
}
