package com.mycompany.cha_da_laura_java;

import java.awt.*;
import java.math.BigDecimal;
import com.mycompany.cha_da_laura_java.dao.SaidaDAO;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * Tela de Saídas: lista de saídas + formulário "Nova saída".
 * Feita 100% em código, sem o editor visual (Design).
 * Uso na tela principal: trocarPagina(new paginaSaidas());
 *
 * Ao salvar uma saída, a quantidade é descontada do estoque do produto.
 * Por enquanto os dados ficam em memória. Procure os "TODO" para ligar com o banco.
 */
public class paginaSaidas extends JPanel {

    // ===== Cores e fonte do layout =====
    private static final Color VERDE = new Color(22, 87, 40);
    private static final Color VERDE_CLARO = new Color(236, 243, 234);
    private static final Color AZUL_TITULO = new Color(16, 52, 128);
    private static final Color AZUL_TEXTO = new Color(84, 116, 196);
    private static final Color VERMELHO = new Color(211, 47, 47);
    private static final Color BORDA = new Color(226, 230, 226);
    private static final Color FUNDO_SECAO = new Color(241, 244, 239);
    private static final Color FUNDO_INFO = new Color(245, 247, 250);
    private static final Color BRANCO = Color.WHITE;
    private static final String FONTE = "Segoe UI";
    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Motivos e o destino que combina com cada um (o destino é sugerido automaticamente)
    private static final String[] MOTIVOS = {
        "Venda loja física", "Envio para parceiro", "Amostra grátis", "Consumo interno", "Perda / avaria"};
    private static final String[] DESTINO_DO_MOTIVO = {
        "Cliente", "Parceiro", "Cliente", "Cliente", "Cliente"};
    private static final String[] DESTINOS = {"Cliente", "E-commerce", "Parceiro"};

    // TODO: trocar pelo nome do usuário que fez login
    private final SaidaDAO dao = new SaidaDAO();

    // ===== Dados (em memória por enquanto) =====
    private final List<Produto> produtos = new ArrayList<>();
    private final List<Saida> saidas = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();

    // ===== Troca entre "lista" e "formulário" =====
    private final CardLayout telas = new CardLayout();
    private final JPanel painelTelas = new JPanel(telas);
    private final TelaLista telaLista;
    private final TelaFormulario telaFormulario;

    public paginaSaidas() {
        // Dados reais carregados em segundo plano.

        setLayout(new BorderLayout());
        setBackground(BRANCO);

        telaLista = new TelaLista();
        telaFormulario = new TelaFormulario();
        painelTelas.add(rolavel(telaLista), "lista");
        painelTelas.add(telaFormulario, "formulario");
        add(painelTelas, BorderLayout.CENTER);

        mostrarLista();
        carregarBanco();
    }

    private void mostrarLista() {
        telaLista.atualizar();
        telas.show(painelTelas, "lista");
    }

    private void mostrarFormulario() {
        telaFormulario.limpar();
        telas.show(painelTelas, "formulario");
    }

    // =====================================================================
    // MODELOS (no projeto real, use as classes do pacote "model")
    // =====================================================================
    private static class Produto {
        final int id;
        final String nome, categoria, unidade;
        BigDecimal estoque;
        Produto(int id,String nome,String categoria,String unidade,BigDecimal estoque) {
            this.id=id;this.nome=nome;this.categoria=categoria==null?"":categoria;
            this.unidade=unidade==null?"un":unidade;this.estoque=estoque;
        }
        @Override public String toString(){return nome;}
    }
    private static class Usuario {
        final int id; final String nome;
        Usuario(int id,String nome){this.id=id;this.nome=nome;}
        @Override public String toString(){return nome;}
    }
    private static class Saida {
        int id;LocalDate data;String produto;BigDecimal quantidade;
        String destino,motivo,usuario,observacoes;
        Saida(int id,LocalDate data,String produto,BigDecimal quantidade,String destino,String motivo,String usuario,String observacoes){
            this.id=id;this.data=data;this.produto=produto;this.quantidade=quantidade;
            this.destino=destino;this.motivo=motivo;this.usuario=usuario;this.observacoes=observacoes;
        }
    }
    private void carregarBanco() {
        new SwingWorker<Object[][][],Void>() {
            @Override protected Object[][][] doInBackground() throws Exception {
                return new Object[][][]{dao.listarProdutos().toArray(new Object[0][]),
                    dao.listarUsuarios().toArray(new Object[0][]),dao.listarSaidas().toArray(new Object[0][])};
            }
            @Override protected void done() {
                try {
                    Object[][][] dados=get();
                    produtos.clear();usuarios.clear();saidas.clear();
                    for(Object[] r:dados[0]) produtos.add(new Produto((Integer)r[0],(String)r[1],(String)r[2],(String)r[3],(BigDecimal)r[4]));
                    for(Object[] r:dados[1]) usuarios.add(new Usuario((Integer)r[0],(String)r[1]));
                    for(Object[] r:dados[2]) saidas.add(new Saida((Integer)r[0],(LocalDate)r[1],(String)r[2],(BigDecimal)r[3],(String)r[4],(String)r[5],(String)r[6],(String)r[7]));
                    telaLista.atualizar();
                } catch(Exception e) {
                    JOptionPane.showMessageDialog(paginaSaidas.this,"Erro ao carregar Saídas: "+causa(e),"Erro",JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    private static String causa(Throwable e) {
        while(e.getCause()!=null)e=e.getCause();return e.getMessage();
    }

    // =====================================================================
    // TELA 1: LISTA DE SAÍDAS
    // =====================================================================
    private class TelaLista extends JPanel {
        private final JComboBox<String> cbPeriodo = new JComboBox<>(new String[]{
            "Todo o período", "Hoje", "Últimos 7 dias", "Últimos 30 dias", "Este mês"});
        private final JComboBox<String> cbProduto = new JComboBox<>();
        private final JComboBox<String> cbMotivo = new JComboBox<>();
        private final JComboBox<String> cbDestino = new JComboBox<>();
        private final TabelaPaginada tabela;
        private boolean carregandoCombos = false;

        TelaLista() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BRANCO);
            setBorder(new EmptyBorder(20, 30, 20, 30));

            JComponent cabecalho = cabecalho("Saídas", "Registre e acompanhe as saídas de produtos do estoque.");
            cabecalho.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, BORDA), new EmptyBorder(0, 0, 14, 0)));
            cabecalho.setMaximumSize(new Dimension(Integer.MAX_VALUE, cabecalho.getPreferredSize().height));

            cbMotivo.addItem("Todos os motivos");
            for (String m : MOTIVOS) {
                cbMotivo.addItem(m);
            }
            cbDestino.addItem("Todos os destinos");
            for (String d : DESTINOS) {
                cbDestino.addItem(d);
            }

            JComboBox<?>[] combos = {cbPeriodo, cbProduto, cbMotivo, cbDestino};
            for (JComboBox<?> c : combos) {
                estilizarCombo(c, 160);
                c.addActionListener(e -> filtrosMudaram());
            }
            JButton btNova = botaoVerde("+  Nova saída");
            btNova.addActionListener(e -> mostrarFormulario());

            JPanel filtros = new JPanel(new GridBagLayout());
            filtros.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = 0;
            g.anchor = GridBagConstraints.SOUTH;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            g.insets = new Insets(0, 0, 0, 14);
            String[] rotulos = {"Período", "Produto", "Motivo", "Destino"};
            for (int i = 0; i < combos.length; i++) {
                g.gridx = i;
                filtros.add(campo(rotulos[i], combos[i], false), g);
            }
            g.gridx = combos.length;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            g.insets = new Insets(0, 0, 0, 0);
            filtros.add(btNova, g);

            tabela = new TabelaPaginada(
                new String[]{"ID", "Data", "Produto", "Quantidade", "Destino", "Motivo", "Usuário", "Ações"},
                "saídas",
                objeto -> mostrarDetalhes((Saida) objeto));
            tabela.larguras(40, 100, 190, 100, 130, 170, 100, 70);

            JComponent[] partes = {cabecalho, filtros, tabela};
            int[] espacos = {16, 20, 0};
            for (int i = 0; i < partes.length; i++) {
                partes[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                add(partes[i]);
                add(Box.createVerticalStrut(espacos[i]));
            }
        }

        private void filtrosMudaram() {
            if (!carregandoCombos) {
                tabela.voltarParaPrimeiraPagina();
                atualizar();
            }
        }

        void atualizar() {
            carregandoCombos = true;
            TreeSet<String> nomes = new TreeSet<>();
            for (Saida s : saidas) {
                nomes.add(s.produto);
            }
            recarregarCombo(cbProduto, "Todos os produtos", nomes);
            carregandoCombos = false;

            String periodo = (String) cbPeriodo.getSelectedItem();
            String produto = (String) cbProduto.getSelectedItem();
            String motivo = (String) cbMotivo.getSelectedItem();
            String destino = (String) cbDestino.getSelectedItem();
            LocalDate hoje = LocalDate.now();

            List<Object[]> linhas = new ArrayList<>();
            List<Saida> objetos = new ArrayList<>();
            for (Saida s : saidas) {
                boolean noPeriodo;
                switch (periodo) {
                    case "Hoje": noPeriodo = s.data.equals(hoje); break;
                    case "Últimos 7 dias": noPeriodo = !s.data.isBefore(hoje.minusDays(6)); break;
                    case "Últimos 30 dias": noPeriodo = !s.data.isBefore(hoje.minusDays(29)); break;
                    case "Este mês": noPeriodo = s.data.getYear() == hoje.getYear() && s.data.getMonth() == hoje.getMonth(); break;
                    default: noPeriodo = true;
                }
                if (!noPeriodo
                        || (!"Todos os produtos".equals(produto) && !s.produto.equals(produto))
                        || (!"Todos os motivos".equals(motivo) && !s.motivo.equals(motivo))
                        || (!"Todos os destinos".equals(destino) && !s.destino.equals(destino))) {
                    continue;
                }
                linhas.add(new Object[]{s.id, s.data.format(FORMATO_DATA), s.produto,
                    s.quantidade.stripTrailingZeros().toPlainString() + " un", s.destino, s.motivo, s.usuario, "Ver"});
                objetos.add(s);
            }
            tabela.setLinhas(linhas, objetos);
        }

        private void mostrarDetalhes(Saida s) {
            StringBuilder texto = new StringBuilder();
            texto.append("Saída nº ").append(s.id)
                 .append("\nData: ").append(s.data.format(FORMATO_DATA))
                 .append("\nProduto: ").append(s.produto)
                 .append("\nQuantidade: ").append(s.quantidade).append(" un")
                 .append("\nMotivo: ").append(s.motivo)
                 .append("\nDestino: ").append(s.destino);
            texto.append("\nUsuário: ").append(s.usuario);
            if (s.observacoes != null && !s.observacoes.isEmpty()) {
                texto.append("\nObservações: ").append(s.observacoes);
            }
            JOptionPane.showMessageDialog(this, texto.toString(), "Detalhes da saída",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =====================================================================
    // TELA 2: FORMULÁRIO "NOVA SAÍDA"
    // =====================================================================
    private class TelaFormulario extends JPanel {
        private final JComboBox<Object> cbProduto = new JComboBox<>();
        private final CampoTexto txtQuantidade = new CampoTexto("Ex.: 10");
        private final JLabel lblCategoria = valorInfo();
        private final JLabel lblUnidade = valorInfo();
        private final JLabel lblEstoqueAtual = valorInfo();
        private final JLabel lblEstoqueApos = valorInfo();
        private final JComboBox<String> cbMotivo = new JComboBox<>();
        private final JComboBox<String> cbDestino = new JComboBox<>();
        private final JComboBox<Usuario> cbUsuario = new JComboBox<>();
        private final JTextArea txtObservacoes = new JTextArea(3, 20);

        TelaFormulario() {
            setLayout(new BorderLayout());
            setBackground(BRANCO);

            // --- Botão voltar + título ---
            JButton btVoltar = new JButton("←  Voltar");
            btVoltar.setFont(new Font(FONTE, Font.PLAIN, 14));
            btVoltar.setFocusPainted(false);
            btVoltar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pintarBotao(btVoltar, FUNDO_SECAO, AZUL_TITULO, FUNDO_SECAO);
            btVoltar.setBorder(new EmptyBorder(8, 16, 8, 16));
            btVoltar.addActionListener(e -> mostrarLista());
            JPanel linhaVoltar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            linhaVoltar.setOpaque(false);
            linhaVoltar.add(btVoltar);

            JComponent cabecalho = cabecalho("Nova saída", "Registre uma saída de produtos do estoque.");

            // --- Campos ---
            estilizarCombo(cbProduto, 200);
            estilizarCombo(cbMotivo, 200);
            estilizarCombo(cbDestino, 200);
            estilizarCombo(cbUsuario, 200);
            txtObservacoes.setFont(new Font(FONTE, Font.PLAIN, 14));
            txtObservacoes.setLineWrap(true);
            txtObservacoes.setWrapStyleWord(true);

            cbProduto.addActionListener(e -> recalcular());
            txtQuantidade.getDocument().addDocumentListener(new AoDigitar(this::recalcular));
            cbMotivo.addActionListener(e -> motivoMudou());

            JLabel lblUn = new JLabel("un", SwingConstants.CENTER);
            lblUn.setOpaque(true);
            lblUn.setBackground(FUNDO_SECAO);
            lblUn.setForeground(AZUL_TITULO);
            lblUn.setPreferredSize(new Dimension(50, 38));
            lblUn.setBorder(new MatteBorder(1, 0, 1, 1, BORDA));
            JPanel quantidadeComUnidade = new JPanel(new BorderLayout());
            quantidadeComUnidade.add(txtQuantidade, BorderLayout.CENTER);
            quantidadeComUnidade.add(lblUn, BorderLayout.EAST);

            JPanel dados = gradeCampos(new double[]{1, 1},
                    campo("Produto", cbProduto, true),
                    campo("Quantidade", quantidadeComUnidade, true));

            // caixinhas cinzas com as informações do produto
            JPanel infos = new JPanel(new GridLayout(1, 4, 18, 0));
            infos.setOpaque(false);
            infos.setBorder(new EmptyBorder(0, 4, 0, 4));
            infos.add(caixaInfo("Categoria", lblCategoria));
            infos.add(caixaInfo("Unidade de medida", lblUnidade));
            infos.add(caixaInfo("Estoque atual", lblEstoqueAtual));
            infos.add(caixaInfo("Estoque após saída", lblEstoqueApos));

            JPanel informacoes = gradeCampos(new double[]{1, 1},
                    campo("Motivo da saída", cbMotivo, true),
                    campo("Destino", cbDestino, true));
            JPanel linhaPessoa = gradeCampos(new double[]{1}, campo("Usuário responsável", cbUsuario, true));

            JScrollPane rolagemObs = new JScrollPane(txtObservacoes);
            rolagemObs.setBorder(new LineBorder(BORDA, 1, true));
            rolagemObs.setPreferredSize(new Dimension(200, 80));

            // --- Botões ---
            JButton btCancelar = new JButton("Cancelar");
            btCancelar.setFont(new Font(FONTE, Font.PLAIN, 15));
            btCancelar.setFocusPainted(false);
            btCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pintarBotao(btCancelar, FUNDO_SECAO, AZUL_TITULO, BORDA);
            btCancelar.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDA, 1, true), new EmptyBorder(11, 40, 11, 40)));
            btCancelar.addActionListener(e -> mostrarLista());

            JButton btSalvar = botaoVerde("Salvar saída");
            btSalvar.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(VERDE, 1, true), new EmptyBorder(11, 50, 11, 50)));
            btSalvar.addActionListener(e -> salvar());

            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
            botoes.setOpaque(false);
            botoes.add(btCancelar);
            botoes.add(btSalvar);

            // --- Card com as seções ---
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(BRANCO);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDA, 1, true), new EmptyBorder(12, 12, 14, 12)));
            JComponent[] partesCard = {
                tituloSecao("Dados da saída"), dados, infos,
                tituloSecao("Informações da saída"), informacoes, linhaPessoa,
                tituloSecao("Observações (opcional)"), rolagemObs
            };
            int[] espacos = {12, 16, 22, 12, 14, 22, 10, 0};
            for (int i = 0; i < partesCard.length; i++) {
                partesCard[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(partesCard[i]);
                card.add(Box.createVerticalStrut(espacos[i]));
            }

            JPanel conteudo=new JPanel();
            conteudo.setLayout(new BoxLayout(conteudo,BoxLayout.Y_AXIS));
            conteudo.setBackground(BRANCO);
            conteudo.setBorder(new EmptyBorder(16,30,30,30));
            JComponent[] partes={linhaVoltar,cabecalho,card};
            int[] espacosTela={10,14,0};
            for(int i=0;i<partes.length;i++) {
                partes[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                conteudo.add(partes[i]);conteudo.add(Box.createVerticalStrut(espacosTela[i]));
            }
            add(rolavel(conteudo),BorderLayout.CENTER);
            JPanel rodape=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,10));
            rodape.setBackground(BRANCO);
            rodape.setBorder(new MatteBorder(1,0,0,0,BORDA));
            rodape.add(btCancelar);rodape.add(btSalvar);
            add(rodape,BorderLayout.SOUTH);
        }

        /** Deixa o formulário em branco toda vez que é aberto. */
        void limpar() {
            cbProduto.removeAllItems();
            cbProduto.addItem("Selecione o produto");
            for (Produto p : produtos) {
                cbProduto.addItem(p);
            }
            cbMotivo.removeAllItems();
            cbMotivo.addItem("Selecione o motivo");
            for (String m : MOTIVOS) {
                cbMotivo.addItem(m);
            }
            cbDestino.removeAllItems();
            cbDestino.addItem("Selecione o destino");
            for (String d : DESTINOS) {
                cbDestino.addItem(d);
            }
            cbUsuario.removeAllItems();
            for (Usuario u : usuarios) cbUsuario.addItem(u);
            cbUsuario.setSelectedIndex(-1);
            txtQuantidade.setText("");
            txtObservacoes.setText("");
            recalcular();
        }

        /** Ao escolher o motivo, sugere o destino que combina. */
        private void motivoMudou() {
            int indice = cbMotivo.getSelectedIndex() - 1; // -1 por causa do "Selecione"
            if (indice >= 0 && indice < DESTINO_DO_MOTIVO.length) {
                cbDestino.setSelectedItem(DESTINO_DO_MOTIVO[indice]);
            }
        }

        private int lerQuantidade() {
            try {
                return Integer.parseInt(txtQuantidade.getText().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        /** Atualiza as caixinhas de categoria, unidade e estoque. */
        private void recalcular() {
            Object item = cbProduto.getSelectedItem();
            if (!(item instanceof Produto)) {
                lblCategoria.setText("-");
                lblUnidade.setText("-");
                lblEstoqueAtual.setText("-");
                lblEstoqueApos.setText("-");
                lblEstoqueApos.setForeground(AZUL_TITULO);
                return;
            }
            Produto p = (Produto) item;
            BigDecimal depois = p.estoque.subtract(BigDecimal.valueOf(lerQuantidade()));
            lblCategoria.setText(p.categoria);
            lblUnidade.setText(p.unidade);
            lblEstoqueAtual.setText(p.estoque.stripTrailingZeros().toPlainString() + " " + p.unidade);
            lblEstoqueApos.setText(depois.stripTrailingZeros().toPlainString() + " " + p.unidade + (depois.signum() < 0 ? "  (insuficiente)" : ""));
            lblEstoqueApos.setForeground(depois.signum() < 0 ? VERMELHO : AZUL_TITULO);
        }

        private void salvar() {
            Object item = cbProduto.getSelectedItem();
            int quantidade = lerQuantidade();

            if (!(item instanceof Produto)) {
                aviso("Selecione o produto.");
                return;
            }
            Produto p = (Produto) item;
            if (quantidade <= 0) {
                aviso("Digite uma quantidade válida (número inteiro maior que zero).");
                return;
            }
            if (BigDecimal.valueOf(quantidade).compareTo(p.estoque)>0) {
                aviso("Estoque insuficiente. Estoque atual de " + p.nome + ": " + p.estoque.stripTrailingZeros().toPlainString() + " " + p.unidade);
                return;
            }
            if (cbMotivo.getSelectedIndex() <= 0 || cbDestino.getSelectedIndex() <= 0) {
                aviso("Selecione o motivo e o destino.");
                return;
            }
            Usuario usuario=(Usuario)cbUsuario.getSelectedItem();
            if(usuario==null){aviso("Selecione o usuário responsável.");return;}
            String motivo=(String)cbMotivo.getSelectedItem();
            String destino=(String)cbDestino.getSelectedItem();
            String observacoes=txtObservacoes.getText().trim();
            // O banco não possui destino 'Interno'; o motivo preserva a natureza da saída.
            new SwingWorker<BigDecimal,Void>() {
                @Override protected BigDecimal doInBackground() throws Exception {
                    return dao.registrarSaida(p.id,BigDecimal.valueOf(quantidade),motivo,destino,usuario.id,observacoes);
                }
                @Override protected void done() {
                    try {
                        BigDecimal novo=get();
                        JOptionPane.showMessageDialog(paginaSaidas.this,
                            "Saída registrada!\nNovo estoque: "+novo.stripTrailingZeros().toPlainString()+" "+p.unidade,
                            "Sucesso",JOptionPane.INFORMATION_MESSAGE);
                        mostrarLista();carregarBanco();
                    }catch(Exception e){JOptionPane.showMessageDialog(paginaSaidas.this,
                        "Erro ao salvar: "+causa(e),"Erro",JOptionPane.ERROR_MESSAGE);}
                }
            }.execute();
        }
    }

    /** Caixinha cinza com um rótulo e um valor (ex.: "Estoque atual / 25 un"). */
    private static JPanel caixaInfo(String rotulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(FUNDO_INFO);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel l = new JLabel(rotulo);
        l.setFont(new Font(FONTE, Font.PLAIN, 13));
        l.setForeground(AZUL_TITULO);
        p.add(l, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private static JLabel valorInfo() {
        JLabel l = new JLabel("-");
        l.setFont(new Font(FONTE, Font.BOLD, 14));
        l.setForeground(AZUL_TITULO);
        return l;
    }

    // =====================================================================
    // COMPONENTES VISUAIS
    // =====================================================================
    private JScrollPane rolavel(JComponent tela) {
        JPanel envoltorio = new PainelLarguraJanela();
        envoltorio.setLayout(new BorderLayout());
        envoltorio.setBackground(BRANCO);
        envoltorio.add(tela, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(envoltorio);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BRANCO);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private static JComponent cabecalho(String titulo, String subtitulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(new Font(FONTE, Font.BOLD, 32));
        t.setForeground(AZUL_TITULO);
        JLabel s = new JLabel(subtitulo);
        s.setFont(new Font(FONTE, Font.PLAIN, 15));
        s.setForeground(AZUL_TEXTO);
        p.add(t);
        p.add(s);
        return p;
    }

    /** Faixa cinza-esverdeada com o título da seção. */
    private static JComponent tituloSecao(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(FONTE, Font.BOLD, 14));
        l.setForeground(AZUL_TITULO);
        l.setOpaque(true);
        l.setBackground(FUNDO_SECAO);
        l.setBorder(new EmptyBorder(9, 10, 9, 10));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return l;
    }

    /** Rótulo em cima (com * vermelho se obrigatório) + componente embaixo. */
    private static JPanel campo(String rotulo, JComponent componente, boolean obrigatorio) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(obrigatorio
                ? "<html>" + rotulo + " <font color='#d32f2f'>*</font></html>"
                : rotulo);
        l.setFont(new Font(FONTE, Font.PLAIN, obrigatorio ? 14 : 12));
        l.setForeground(AZUL_TITULO);
        p.add(l, BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    /** Campos lado a lado, com larguras proporcionais aos pesos. */
    private static JPanel gradeCampos(double[] pesos, JComponent... campos) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        for (int i = 0; i < campos.length; i++) {
            g.gridx = i;
            g.weightx = pesos[i];
            g.insets = new Insets(0, i == 0 ? 4 : 0, 0, i < campos.length - 1 ? 20 : 4);
            p.add(campos[i], g);
        }
        return p;
    }

    private static void recarregarCombo(JComboBox<String> combo, String primeiro, TreeSet<String> opcoes) {
        Object escolhida = combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem(primeiro);
        for (String o : opcoes) {
            combo.addItem(o);
        }
        combo.setSelectedItem(escolhida == null ? primeiro : escolhida);
        if (combo.getSelectedIndex() < 0) {
            combo.setSelectedIndex(0);
        }
    }

    private static void estilizarCombo(JComboBox<?> combo, int largura) {
        combo.setFont(new Font(FONTE, Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(largura, 38));
        combo.setBackground(BRANCO);
    }

    private static JButton botaoVerde(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font(FONTE, Font.PLAIN, 15));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pintarBotao(b, VERDE, BRANCO, VERDE);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VERDE, 1, true), new EmptyBorder(11, 26, 11, 26)));
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

    private static void aviso(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private static String numero(double v) {
        return (v == Math.floor(v))
                ? String.format(BR, "%,.0f", v)      // 1150.0 -> "1.150"
                : String.format(BR, "%,.1f", v);     // 22.5 -> "22,5"
    }

    private static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    private static class AoDigitar implements DocumentListener {
        private final Runnable acao;

        AoDigitar(Runnable acao) {
            this.acao = acao;
        }

        @Override public void insertUpdate(DocumentEvent e) { acao.run(); }
        @Override public void removeUpdate(DocumentEvent e) { acao.run(); }
        @Override public void changedUpdate(DocumentEvent e) { acao.run(); }
    }

    /** Painel que sempre ocupa a largura da janela (só rola na vertical). */
    private static class PainelLarguraJanela extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientacao, int direcao) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientacao, int direcao) { return r.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    /** Campo de texto com dica cinza quando está vazio. */
    private static class CampoTexto extends JTextField {
        private final String dica;

        CampoTexto(String dica) {
            this.dica = dica;
            setFont(new Font(FONTE, Font.PLAIN, 14));
            setForeground(AZUL_TITULO);
            setPreferredSize(new Dimension(150, 38));
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
    // TABELA DA LISTA (selos, "Ver" e paginação)
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
            int w = Math.max(fm.stringWidth(getText()) + 30, Math.min(getWidth() - 20, 120));
            int h = 22;
            g2.setColor(corSelo);
            g2.fillRoundRect((getWidth() - w) / 2, (getHeight() - h) / 2, w, h, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class Renderizador extends DefaultTableCellRenderer {
        private final Selo selo = new Selo();

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor,
                boolean selecionado, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(t, valor, selecionado, false, linha, coluna);
            Color fundo = selecionado ? VERDE_CLARO : (linha % 2 == 0 ? BRANCO : new Color(250, 251, 250));
            String nomeColuna = t.getColumnName(coluna);
            String texto = (valor == null) ? "" : valor.toString();

            if (nomeColuna.equals("Destino")) {
                selo.setText(texto);
                selo.corFundo = fundo;
                switch (texto) {
                    case "Cliente":
                        selo.corSelo = new Color(219, 234, 254);
                        selo.setForeground(new Color(30, 100, 200));
                        break;
                    case "Parceiro":
                        selo.corSelo = new Color(237, 228, 255);
                        selo.setForeground(new Color(110, 60, 190));
                        break;
                    case "E-commerce":
                        selo.corSelo = new Color(220, 240, 220);
                        selo.setForeground(new Color(30, 110, 50));
                        break;
                    default: // Interno
                        selo.corSelo = new Color(235, 235, 235);
                        selo.setForeground(new Color(80, 80, 80));
                }
                return selo;
            }
            setBackground(fundo);
            setFont(new Font(FONTE, Font.PLAIN, 13));
            setForeground(AZUL_TITULO);
            setBorder(new EmptyBorder(0, 12, 0, 8));
            setHorizontalAlignment(LEFT);
            if (nomeColuna.equals("Ações")) {
                setForeground(VERDE);
                setFont(new Font(FONTE, Font.BOLD, 13));
                setHorizontalAlignment(CENTER);
            }
            return this;
        }
    }

    private interface AcaoTabela {
        void executar(Object objeto);
    }

    private static class TabelaPaginada extends JPanel {
        private final String[] colunas;
        private final String nomeItens;
        private final DefaultTableModel modelo;
        private final JTable tabela;
        private final JLabel lblInfo = new JLabel();
        private final JPanel painelPaginas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        private List<Object[]> linhas = new ArrayList<>();
        private List<?> objetos = new ArrayList<>();
        private int pagina = 0;
        private int porPagina = 10;

        TabelaPaginada(String[] colunas, String nomeItens, AcaoTabela acao) {
            this.colunas = colunas;
            this.nomeItens = nomeItens;

            modelo = new DefaultTableModel(colunas, 0) {
                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }
            };
            tabela = new JTable(modelo);
            tabela.setRowHeight(35);
            tabela.setGridColor(BORDA);
            tabela.setSelectionBackground(VERDE_CLARO);
            tabela.setDefaultRenderer(Object.class, new Renderizador());
            estilizarCabecalho(tabela);

            TableColumn colunaAcoes = tabela.getColumnModel().getColumn(colunas.length - 1);
            colunaAcoes.setMaxWidth(80);

            tabela.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int linha = tabela.rowAtPoint(e.getPoint());
                    int coluna = tabela.columnAtPoint(e.getPoint());
                    int indice = pagina * porPagina + linha;
                    if (linha >= 0 && coluna == colunas.length - 1 && indice < objetos.size()) {
                        acao.executar(objetos.get(indice));
                    }
                }
            });

            JPanel caixa = new JPanel(new BorderLayout());
            caixa.setBorder(new LineBorder(BORDA, 1, true));
            caixa.add(tabela.getTableHeader(), BorderLayout.NORTH);
            caixa.add(tabela, BorderLayout.CENTER);

            lblInfo.setFont(new Font(FONTE, Font.PLAIN, 13));
            lblInfo.setForeground(AZUL_TEXTO);

            JComboBox<String> cbPorPagina = new JComboBox<>(new String[]{"10 por página", "20 por página", "50 por página"});
            cbPorPagina.setFont(new Font(FONTE, Font.PLAIN, 13));
            cbPorPagina.setBackground(BRANCO);
            cbPorPagina.addActionListener(e -> {
                porPagina = new int[]{10, 20, 50}[cbPorPagina.getSelectedIndex()];
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
            rodape.setBorder(new EmptyBorder(16, 8, 0, 0));
            rodape.add(lblInfo, BorderLayout.WEST);
            rodape.add(direita, BorderLayout.EAST);

            setLayout(new BorderLayout());
            setOpaque(false);
            add(caixa, BorderLayout.CENTER);
            add(rodape, BorderLayout.SOUTH);
        }

        void larguras(int... valores) {
            for (int i = 0; i < valores.length && i < colunas.length; i++) {
                tabela.getColumnModel().getColumn(i).setPreferredWidth(valores[i]);
            }
        }

        void setLinhas(List<Object[]> novasLinhas, List<?> novosObjetos) {
            this.linhas = novasLinhas;
            this.objetos = novosObjetos;
            atualizarPagina();
        }

        void voltarParaPrimeiraPagina() {
            pagina = 0;
        }

        private void irPara(int novaPagina) {
            pagina = novaPagina;
            atualizarPagina();
        }

        private void atualizarPagina() {
            modelo.setRowCount(0);
            int total = linhas.size();
            int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) porPagina));
            if (pagina >= totalPaginas) {
                pagina = totalPaginas - 1;
            }
            int inicio = pagina * porPagina;
            int fim = Math.min(inicio + porPagina, total);
            for (int i = inicio; i < fim; i++) {
                modelo.addRow(linhas.get(i));
            }
            lblInfo.setText(total == 0
                    ? "Nenhuma saída encontrada."
                    : "Mostrando " + (inicio + 1) + " a " + fim + " de " + total + " " + nomeItens);

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

        private JButton botaoPagina(String texto, boolean ativo, boolean habilitado, Runnable aoClicar) {
            JButton b = new JButton(texto);
            b.setFont(new Font(FONTE, Font.PLAIN, 13));
            b.setFocusPainted(false);
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setPreferredSize(new Dimension(36, 34));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pintarBotao(b, ativo ? VERDE : BRANCO, ativo ? BRANCO : AZUL_TITULO, ativo ? VERDE : BORDA);
            b.setEnabled(habilitado);
            b.addActionListener(e -> aoClicar.run());
            return b;
        }
    }

    private static void estilizarCabecalho(JTable tabela) {
        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new Dimension(0, 36));
        cabecalho.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionado, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                setFont(new Font(FONTE, Font.BOLD, 12));
                setBackground(FUNDO_SECAO);
                setForeground(AZUL_TITULO);
                String texto = String.valueOf(valor);
                setHorizontalAlignment(texto.equals("Ações") || texto.equals("Destino") ? CENTER : LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, BORDA), new EmptyBorder(0, 12, 0, 8)));
                return this;
            }
        });
    }
    }
