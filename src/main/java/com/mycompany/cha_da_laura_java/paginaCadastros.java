package com.mycompany.cha_da_laura_java;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
import com.mycompany.cha_da_laura_java.dao.ClientesDAO;
import java.sql.SQLException;
import com.mycompany.cha_da_laura_java.dao.FornecedoresDAO;
import com.mycompany.cha_da_laura_java.dao.ParceirosDAO;
import com.mycompany.cha_da_laura_java.dao.ReceitaDAO;

public class paginaCadastros extends JPanel {

    // ===== Cores e fonte do layout =====
    private static final Color VERDE = new Color(22, 87, 40);
    private static final Color VERDE_CLARO = new Color(236, 243, 234);
    private static final Color AZUL_TITULO = new Color(16, 52, 128);
    private static final Color AZUL_TEXTO = new Color(84, 116, 196);
    private static final Color VERMELHO = new Color(211, 47, 47);
    private static final Color BORDA = new Color(226, 230, 226);
    private static final Color BRANCO = Color.WHITE;
    private static final String FONTE = "Segoe UI";
    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    private static final String ACOES = "editar/excluir"; // marca a célula com as 2 ações
    private static final Set<String> SITUACAO_VERDE = new HashSet<>(Arrays.asList("Ativo", "Ativa"));
    private static final Set<String> SITUACAO_VERMELHA = new HashSet<>(Arrays.asList("Inativo", "Inativa"));

    // ===== Abas =====
    private final String[] nomesAbas = {"Clientes", "Fornecedores", "Parceiros", "Receitas"};
    private final String[] titulos = {"Cadastros", "Fornecedores", "Parceiros", "Receitas"};
    private final String[] subtitulos = {
        "Gerencie clientes, fornecedores, parceiros e receitas em um só lugar.",
        "Gerencie os fornecedores cadastrados no sistema.",
        "Gerencie os parceiros cadastrados no sistema.",
        "Gerencie as receitas cadastradas no sistema."
    };
    private final String[] textosBotaoNovo = {"Novo cliente", "Novo fornecedor", "Novo parceiro", "Nova receita"};

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel painelAbas = new PainelLarguraJanela(cardLayout);
    private final JButton[] botoesAbas = new JButton[nomesAbas.length];
    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblSubtitulo = new JLabel();
    private final JButton btNovo = botaoVerde("");
    private final Runnable[] acoesNovo = new Runnable[nomesAbas.length];
    private int abaAtual = 0;

    public paginaCadastros() {
        this(0);
    }

    public paginaCadastros(int abaInicial) {
        setLayout(new BorderLayout());
        setBackground(BRANCO);
        setBorder(new EmptyBorder(20, 30, 20, 30));
        add(criarCabecalho(), BorderLayout.NORTH);

        // TODO: trocar os dados de exemplo pelos dados do banco
        AbaPessoas clientes = new AbaPessoas("cliente", "clientes",
        new String[]{"Nome", "CPF/CNPJ", "Telefone", "Cidade", "Situação"},
        false, new ArrayList<>());
        AbaPessoas fornecedores = new AbaPessoas("fornecedor", "fornecedores",
                new String[]{"Nome da empresa", "CNPJ", "Telefone", "Cidade", "Situação"}, true, new ArrayList<>());
        AbaPessoas parceiros = new AbaPessoas("parceiro", "parceiros",
                new String[]{"Nome do parceiro", "CNPJ/CPF", "Telefone", "Cidade", "Situação"}, false, new ArrayList<>());
        AbaReceitas receitas = new AbaReceitas();

        acoesNovo[0] = () -> clientes.abrirFormulario(null);
        acoesNovo[1] = () -> fornecedores.abrirFormulario(null);
        acoesNovo[2] = () -> parceiros.abrirFormulario(null);
        acoesNovo[3] = () -> receitas.abrirFormulario(null);
        btNovo.addActionListener(e -> acoesNovo[abaAtual].run());

        painelAbas.setOpaque(false);
        JComponent[] abas = {clientes, fornecedores, parceiros, receitas};
        for (int i = 0; i < abas.length; i++) {
            painelAbas.add(dentroDeCard(abas[i]), nomesAbas[i]);
        }

        JScrollPane scroll = new JScrollPane(painelAbas);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(BRANCO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        selecionarAba(Math.max(0, Math.min(abaInicial, nomesAbas.length - 1)));

        // Carrega os dados sem bloquear a interface gráfica.
        carregarPessoasEmSegundoPlano(clientes, fornecedores, parceiros);

    }

    private void carregarPessoasEmSegundoPlano(
            AbaPessoas clientes, AbaPessoas fornecedores, AbaPessoas parceiros) {

        new SwingWorker<List<List<String[]>>, Void>() {
            @Override
            protected List<List<String[]>> doInBackground() {
                List<List<String[]>> dados = new ArrayList<>();
                dados.add(carregarClientesBanco());
                dados.add(carregarFornecedoresBanco());
                dados.add(carregarParceirosBanco());
                return dados;
            }

            @Override
            protected void done() {
                try {
                    List<List<String[]>> dados = get();
                    AbaPessoas[] abas = {clientes, fornecedores, parceiros};
                    for (int i = 0; i < abas.length; i++) {
                        abas[i].registros.clear();
                        abas[i].registros.addAll(dados.get(i));
                        abas[i].atualizar();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(paginaCadastros.this,
                        "Erro ao carregar cadastros: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // CABEÇALHO 
    
    private JPanel criarCabecalho() {
        lblTitulo.setFont(new Font(FONTE, Font.BOLD, 32));
        lblTitulo.setForeground(AZUL_TITULO);
        lblSubtitulo.setFont(new Font(FONTE, Font.PLAIN, 15));
        lblSubtitulo.setForeground(AZUL_TEXTO);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.add(lblTitulo);
        textos.add(lblSubtitulo);

        JPanel caixaBotao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        caixaBotao.setOpaque(false);
        caixaBotao.add(btNovo);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(textos, BorderLayout.WEST);
        topo.add(caixaBotao, BorderLayout.EAST);

        JPanel abas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        abas.setOpaque(false);
        for (int i = 0; i < nomesAbas.length; i++) {
            final int indice = i;
            JButton b = new JButton(nomesAbas[i]);
            b.setFont(new Font(FONTE, Font.PLAIN, 14));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(150, 44));
            b.addActionListener(e -> selecionarAba(indice));
            botoesAbas[i] = b;
            abas.add(b);
        }

        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 0, 0));
        p.add(topo, BorderLayout.NORTH);
        p.add(abas, BorderLayout.SOUTH);
        return p;
    }

    private void selecionarAba(int indice) {
        abaAtual = indice;
        cardLayout.show(painelAbas, nomesAbas[indice]);
        lblTitulo.setText(titulos[indice]);
        lblSubtitulo.setText(subtitulos[indice]);
        btNovo.setText("+  " + textosBotaoNovo[indice]);
        for (int i = 0; i < botoesAbas.length; i++) {
            boolean ativo = (i == indice);
            pintarBotao(botoesAbas[i],
                    ativo ? VERDE : new Color(244, 246, 244),
                    ativo ? BRANCO : AZUL_TITULO,
                    ativo ? VERDE : BORDA);
        }
    }

    /** Coloca a aba dentro de um card branco com borda. */
    private JPanel dentroDeCard(JComponent conteudo) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BRANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(18, 16, 16, 16)));
        card.add(conteudo, BorderLayout.CENTER);

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(new EmptyBorder(8, 0, 0, 0));
        envoltorio.add(card, BorderLayout.NORTH);
        return envoltorio;
    }

private static List<String[]> carregarFornecedoresBanco() {

    List<String[]> fornecedores = new ArrayList<>();

    try {
        FornecedoresDAO dao = new FornecedoresDAO();

        for (Object[] fornecedor : dao.listarFornecedores()) {

            String nome = (String) fornecedor[1];
            String cnpj = (String) fornecedor[2];
            String telefone = (String) fornecedor[4];
            String cidade = (String) fornecedor[5];
            String uf = (String) fornecedor[6];
            String situacao = (String) fornecedor[7];

            fornecedores.add(new String[]{
                nome,
                cnpj,
                telefone,
                cidade + " - " + uf,
                situacao
            });
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(
            null,
            "Erro ao carregar fornecedores: " + ex.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE
        );
    }

    return fornecedores;
}

    private static List<String[]> carregarParceirosBanco() {

        List<String[]> parceiros = new ArrayList<>();

        try {
            ParceirosDAO dao = new ParceirosDAO();

            for (Object[] parceiro : dao.listarParceiros()) {

                parceiros.add(new String[]{
                    (String) parceiro[1],
                    (String) parceiro[2],
                    (String) parceiro[4],
                    (String) parceiro[5] + " - " + (String) parceiro[6],
                    (String) parceiro[7]
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                null,
                "Erro ao carregar parceiros: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }

        return parceiros;
    }

        
        // ABAS CLIENTES / FORNECEDORES / PARCEIROS 
        
        private class AbaPessoas extends JPanel {
            private final String singular;
            private final String plural;
            private final String[] rotulos;
            private final boolean somenteCnpj;
            private final List<String[]> registros;
            private final CampoBusca busca;
            private final JComboBox<String> cbSituacao = new JComboBox<>(new String[]{"Todas", "Ativo", "Inativo"});
            private final JComboBox<String> cbCidade = new JComboBox<>();
            private final TabelaCadastro tabela;
            private boolean carregandoCombos = false;

            AbaPessoas(String singular, String plural, String[] rotulos, boolean somenteCnpj, List<String[]> registros) {
                this.singular = singular;
                this.plural = plural;
                this.rotulos = rotulos;
                this.somenteCnpj = somenteCnpj;
                this.registros = registros;

                busca = new CampoBusca("Buscar " + singular + " pelo nome...");
                busca.getDocument().addDocumentListener(new AoDigitar(this::filtrosMudaram));
                estilizarCombo(cbSituacao, 160);
                estilizarCombo(cbCidade, 160);
                cbSituacao.addActionListener(e -> filtrosMudaram());
                cbCidade.addActionListener(e -> filtrosMudaram());

                String[] colunas = Arrays.copyOf(rotulos, rotulos.length + 1);
                colunas[rotulos.length] = "Ações";
                tabela = new TabelaCadastro(colunas, plural + " cadastrados", 40,
                        (objeto, ladoDireito) -> {
                            String[] registro = (String[]) objeto;
                            if (ladoDireito) {
                                excluir(registro);
                            } else {
                                abrirFormulario(registro);
                            }
                        });
                tabela.larguras(200, 150, 140, 160, 100, 130);

                setLayout(new BorderLayout(0, 16));
                setOpaque(false);
                add(linhaFiltros(busca, new String[]{"Situação", "Cidade"}, cbSituacao, cbCidade), BorderLayout.NORTH);
                add(tabela, BorderLayout.CENTER);

                atualizar();
            }

            private void filtrosMudaram() {
                if (!carregandoCombos) {
                    tabela.voltarParaPrimeiraPagina();
                    atualizar();
                }
            }

            void atualizar() {
                // cidades disponíveis no filtro
                carregandoCombos = true;
                TreeSet<String> cidades = new TreeSet<>();
                for (String[] r : registros) {
                    cidades.add(r[3]);
                }
                recarregarCombo(cbCidade, "Todas", cidades);
                carregandoCombos = false;

                String texto = busca.getText().trim().toLowerCase();
                String situacao = (String) cbSituacao.getSelectedItem();
                String cidade = (String) cbCidade.getSelectedItem();

                List<Object[]> linhas = new ArrayList<>();
                List<String[]> objetos = new ArrayList<>();
                for (String[] r : registros) {
                    if (!texto.isEmpty() && !r[0].toLowerCase().contains(texto)) {
                        continue;
                    }
                    if (!"Todas".equals(situacao) && !r[4].equals(situacao)) {
                        continue;
                    }
                    if (!"Todas".equals(cidade) && !r[3].equals(cidade)) {
                        continue;
                    }
                    linhas.add(new Object[]{r[0], r[1], r[2], r[3], r[4], ACOES});
                    objetos.add(r);
                }
                tabela.setLinhas(linhas, objetos, registros.size());
            }

            /** Formulário de novo (registro == null) ou de edição. */

    void abrirFormulario(String[] registro) {

        boolean novo = (registro == null);

        JTextField txtNome = new JTextField(novo ? "" : registro[0], 24);
        JTextField txtDocumento = new JTextField(novo ? "" : registro[1]);
        JTextField txtTelefone = new JTextField(novo ? "" : registro[2]);
        JTextField txtCidade = new JTextField(novo ? "" : registro[3]);

        JTextField txtEmail = new JTextField(24);
        JTextField txtUF = new JTextField(2);

        Integer idRegistro = null;

        // Recupera os dados completos do banco para edição
        if (!novo && (singular.equals("cliente")
        || singular.equals("fornecedor")
        || singular.equals("parceiro"))) {

            try {
                Object[] pessoa;

                if (singular.equals("cliente")) {
                    ClientesDAO dao = new ClientesDAO();
                    pessoa = dao.buscarClientePorDocumento(registro[1]);
                } else if (singular.equals("fornecedor")) {
            FornecedoresDAO dao = new FornecedoresDAO();
            pessoa = dao.buscarFornecedorPorDocumento(registro[1]);
            } else {
            ParceirosDAO dao = new ParceirosDAO();
            pessoa = dao.buscarParceiroPorDocumento(registro[1]);
    }

                if (pessoa == null) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Registro não encontrado no banco de dados."
                    );
                    return;
                }

                idRegistro = (Integer) pessoa[0];

                txtNome.setText((String) pessoa[1]);
                txtDocumento.setText((String) pessoa[2]);
                txtEmail.setText((String) pessoa[3]);
                txtTelefone.setText((String) pessoa[4]);
                txtCidade.setText((String) pessoa[5]);
                txtUF.setText((String) pessoa[6]);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Erro ao consultar registro: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        JComboBox<String> cbSit = new JComboBox<>(
            new String[]{"Ativo", "Inativo"}
        );

        cbSit.setSelectedItem(novo ? "Ativo" : registro[4]);

        // Montagem do formulário
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));

        form.add(new JLabel(rotulos[0] + " *"));
        form.add(txtNome);

        form.add(new JLabel(
            rotulos[1] + (somenteCnpj
                ? " * (só números ou formatado)"
                : " (só números ou formatado)")
        ));
        form.add(txtDocumento);

        form.add(new JLabel("Telefone (com DDD)"));
        form.add(txtTelefone);

        form.add(new JLabel("Cidade"));
        form.add(txtCidade);

        // E-mail e UF para clientes e fornecedores
        if (singular.equals("cliente")
        || singular.equals("fornecedor")
        || singular.equals("parceiro")) {

            form.add(new JLabel("E-mail *"));
            form.add(txtEmail);

            form.add(new JLabel("UF * (ex.: PR)"));
            form.add(txtUF);
        }

        form.add(new JLabel("Situação"));
        form.add(cbSit);

        String tituloJanela = (novo ? "Novo " : "Editar ") + singular;

        while (true) {

            Object[] opcoes = {"Salvar", "Cancelar"};

            int resposta = JOptionPane.showOptionDialog(
                this,
                form,
                tituloJanela,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcoes,
                opcoes[0]
            );

            if (resposta != 0) {
                return;
            }

            String nome = txtNome.getText().trim();
            String documento = formatarDocumento(txtDocumento.getText());
            String telefone = formatarTelefone(txtTelefone.getText());
            String cidade = txtCidade.getText().trim();
            String situacao = (String) cbSit.getSelectedItem();

            // Validações
            if (nome.isEmpty()) {
                aviso("Preencha o nome.");
                continue;
            }

            if (documento == null) {
                aviso(somenteCnpj
                    ? "CNPJ inválido: deve ter 14 números."
                    : "CPF/CNPJ inválido: deve ter 11 ou 14 números.");
                continue;
            }

            if (somenteCnpj
                    && documento.replaceAll("\\D", "").length() != 14) {
                aviso("Para fornecedor, informe um CNPJ com 14 números.");
                continue;
            }

            if (telefone == null) {
                aviso("Telefone inválido: use DDD + número.");
                continue;
            }

            if (cidade.isEmpty()) {
                cidade = "-";
            }

            String email = txtEmail.getText().trim();
            String uf = txtUF.getText().trim().toUpperCase();

            if (singular.equals("cliente")
        || singular.equals("fornecedor")
        || singular.equals("parceiro")) {

                if (email.isEmpty() || !uf.matches("[A-Z]{2}")) {
                    aviso("Preencha o e-mail e a UF com 2 letras.");
                    continue;
                }
            }

            String[] dados = {
                nome,
                documento,
                telefone,
                cidade,
                situacao
            };

            // CADASTRAR NOVO REGISTRO
            if (novo) {

                if (singular.equals("cliente")) {

                    try {
                        ClientesDAO dao = new ClientesDAO();

                        dao.inserirCliente(
                            nome,
                            documento,
                            email,
                            telefone,
                            cidade,
                            uf,
                            situacao
                        );

                        registros.clear();
                        registros.addAll(carregarClientesBanco());

                        JOptionPane.showMessageDialog(
                            this,
                            "Cliente cadastrado com sucesso!"
                        );

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Erro ao cadastrar cliente: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                        );
                        continue;
                    }

                } else if (singular.equals("fornecedor")) {

                    try {
                        FornecedoresDAO dao = new FornecedoresDAO();

                        dao.inserirFornecedor(
                            nome,
                            documento,
                            email,
                            telefone,
                            cidade,
                            uf,
                            situacao
                        );

                        registros.clear();
                        registros.addAll(carregarFornecedoresBanco());

                        JOptionPane.showMessageDialog(
                            this,
                            "Fornecedor cadastrado com sucesso!"
                        );

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Erro ao cadastrar fornecedor: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                        );
                        continue;
                    }

                
} else if (singular.equals("parceiro")) {

    try {
        ParceirosDAO dao = new ParceirosDAO();

        dao.inserirParceiro(
            nome, documento, email, telefone,
            cidade, uf, situacao
        );

        registros.clear();
        registros.addAll(carregarParceirosBanco());

        JOptionPane.showMessageDialog(
            this, "Parceiro cadastrado com sucesso!"
        );

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(
            this,
            "Erro ao cadastrar parceiro: " + ex.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE
        );
        continue;
    }
}


            } else {

                // EDITAR REGISTRO EXISTENTE
                if (singular.equals("cliente")) {

                    try {
                        ClientesDAO dao = new ClientesDAO();

                        dao.atualizarCliente(
                            idRegistro,
                            nome,
                            documento,
                            email,
                            telefone,
                            cidade,
                            uf,
                            situacao
                        );

                        registros.clear();
                        registros.addAll(carregarClientesBanco());

                        JOptionPane.showMessageDialog(
                            this,
                            "Cliente atualizado com sucesso!"
                        );

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Erro ao atualizar cliente: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                        );
                        continue;
                    }

                } else if (singular.equals("fornecedor")) {

                    try {
                        FornecedoresDAO dao = new FornecedoresDAO();

                        dao.atualizarFornecedor(
                            idRegistro,
                            nome,
                            documento,
                        email,
                        telefone,
                        cidade,
                        uf,
                        situacao
                    );

                    registros.clear();
                    registros.addAll(carregarFornecedoresBanco());

                    JOptionPane.showMessageDialog(
                        this,
                        "Fornecedor atualizado com sucesso!"
                    );

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Erro ao atualizar fornecedor: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                    );
                    continue;
                }

            
} else if (singular.equals("parceiro")) {

    try {
        ParceirosDAO dao = new ParceirosDAO();

        dao.atualizarParceiro(
            idRegistro, nome, documento, email,
            telefone, cidade, uf, situacao
        );

        registros.clear();
        registros.addAll(carregarParceirosBanco());

        JOptionPane.showMessageDialog(
            this, "Parceiro atualizado com sucesso!"
        );

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(
            this,
            "Erro ao atualizar parceiro: " + ex.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE
        );
        continue;
    }
}

            
            }

        atualizar();
        return;
    }
}



       

private void excluir(String[] registro) {

    Object[] opcoes = {"Sim", "Não"};

    int resposta = JOptionPane.showOptionDialog(
        this,
        "Tem certeza de que deseja excluir \"" + registro[0] + "\"?",
        "Confirmar exclusão",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE,
        null,
        opcoes,
        opcoes[1]
    );

    if (resposta != JOptionPane.YES_OPTION) {
        return;
    }

    if (singular.equals("cliente")) {

        try {
            ClientesDAO dao = new ClientesDAO();

            Object[] cliente = dao.buscarClientePorDocumento(registro[1]);

            if (cliente == null) {
                aviso("Cliente não encontrado no banco de dados.");
                return;
            }

            int idCliente = (Integer) cliente[0];

            dao.excluirCliente(idCliente);

            registros.remove(registro);

            JOptionPane.showMessageDialog(
                this,
                "Cliente excluído com sucesso!"
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao excluir cliente: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

    } else if (singular.equals("fornecedor")) {

        try {
            FornecedoresDAO dao = new FornecedoresDAO();

            Object[] fornecedor =
                dao.buscarFornecedorPorDocumento(registro[1]);

            if (fornecedor == null) {
                aviso("Fornecedor não encontrado no banco de dados.");
                return;
            }

            int idFornecedor = (Integer) fornecedor[0];

            dao.excluirFornecedor(idFornecedor);

            registros.remove(registro);

            JOptionPane.showMessageDialog(
                this,
                "Fornecedor excluído com sucesso!"
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao excluir fornecedor: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

    } else {
        // Exclusão local de parceiros
        registros.remove(registro);
    }

    atualizar();
}
    }

    // ABA RECEITAS
    private static class Receita {
        int id;
        String nome;
        List<String> ingredientes;   // ex.: "Camomila - 15 g"
        int rendimentoUnidades;
        double rendimentoGramas;
        double custoTotal;
        int validadeDias;
        String situacao;             // "Ativa" ou "Inativa"

        Receita(int id, String nome, List<String> ingredientes, int rendimentoUnidades,
                double rendimentoGramas, double custoTotal, int validadeDias, String situacao) {
            this.id = id;
            this.nome = nome;
            this.ingredientes = ingredientes;
            this.rendimentoUnidades = rendimentoUnidades;
            this.rendimentoGramas = rendimentoGramas;
            this.custoTotal = custoTotal;
            this.validadeDias = validadeDias;
            this.situacao = situacao;
        }

        /** O custo por unidade é calculado, não digitado. */
        double custoPorUnidade() {
            return rendimentoUnidades == 0 ? 0 : custoTotal / rendimentoUnidades;
        }
    }

    
private class AbaReceitas extends JPanel {

    private final List<Receita> receitas = new ArrayList<>();
    private final CampoBusca busca =
        new CampoBusca("Buscar receita pelo nome...");
    private final JComboBox<String> cbSituacao =
        new JComboBox<>(new String[]{"Todas", "Ativa", "Inativa"});
    private final TabelaCadastro tabela;

    private class Opcao {
        final int id;
        final String nome;
        final String unidade;

        Opcao(Object[] dados) {
            id = ((Number) dados[0]).intValue();
            nome = (String) dados[1];
            unidade = dados.length > 2 ? (String) dados[2] : "";
        }

        @Override
        public String toString() {
            return nome + (unidade == null || unidade.isEmpty()
                    ? "" : " (" + unidade + ")");
        }
    }

    AbaReceitas() {
        busca.getDocument().addDocumentListener(
            new AoDigitar(this::filtrosMudaram)
        );
        estilizarCombo(cbSituacao, 180);
        cbSituacao.addActionListener(e -> filtrosMudaram());

        tabela = new TabelaCadastro(
            new String[]{
                "ID", "Nome da receita", "Ingredientes e quantidades",
                "<html><center>Rendimento<br>(unid.)</center></html>",
                "<html><center>Rendimento<br>(g)</center></html>",
                "<html><center>Custo total<br>(R$)</center></html>",
                "<html><center>Custo por<br>unid. (R$)</center></html>",
                "<html><center>Validade<br>(dias)</center></html>",
                "Situação", "Ações"
            },
            "receitas cadastradas", 58,
            (objeto, ladoDireito) -> {
                Receita r = (Receita) objeto;
                if (ladoDireito) excluir(r);
                else abrirFormulario(r);
            }
        );

        tabela.larguras(35, 150, 200, 85, 85, 85, 85, 70, 85, 130);

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        add(linhaFiltros(busca, new String[]{"Situação"}, cbSituacao),
                BorderLayout.NORTH);
        add(tabela, BorderLayout.CENTER);

        carregarBancoAssincrono();
    }

    private void filtrosMudaram() {
        tabela.voltarParaPrimeiraPagina();
        atualizar();
    }

    // A consulta ao banco acontece fora da thread da interface.
    private void carregarBancoAssincrono() {
        new SwingWorker<List<Receita>, Void>() {
            @Override
            protected List<Receita> doInBackground() throws Exception {
                return consultarReceitasBanco();
            }

            @Override
            protected void done() {
                try {
                    receitas.clear();
                    receitas.addAll(get());
                    atualizar();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AbaReceitas.this,
                        "Erro ao carregar receitas: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private List<Receita> consultarReceitasBanco() throws SQLException {
        ReceitaDAO dao = new ReceitaDAO();
        List<Receita> carregadas = new ArrayList<>();
        for (Object[] dados : dao.listarReceitas()) {
            int id = ((Number) dados[0]).intValue();
            List<String> ingredientes = new ArrayList<>();
            for (Object[] item : dao.listarIngredientes(id)) {
                ingredientes.add(item[1] + " - " + item[2] + " " + item[3]);
            }
            carregadas.add(new Receita(
                id, (String) dados[2], ingredientes,
                ((Number) dados[3]).intValue(),
                ((Number) dados[4]).doubleValue(),
                ((Number) dados[5]).doubleValue(),
                ((Number) dados[6]).intValue(), (String) dados[7]));
        }
        return carregadas;
    }

    void atualizar() {
        String texto = busca.getText().trim().toLowerCase();
        String situacao = (String) cbSituacao.getSelectedItem();

        List<Object[]> linhas = new ArrayList<>();
        List<Receita> objetos = new ArrayList<>();

        for (Receita r : receitas) {
            if (!texto.isEmpty()
                    && !r.nome.toLowerCase().contains(texto)) continue;

            if (!"Todas".equals(situacao)
                    && !r.situacao.equals(situacao)) continue;

            StringBuilder html = new StringBuilder("<html>");

            for (String ingrediente : r.ingredientes) {
                if (html.length() > 6) html.append("<br>");
                html.append("&bull; ")
                    .append(ingrediente.replace("&", "&amp;")
                        .replace("<", "&lt;").replace(">", "&gt;"));
            }

            html.append("</html>");

            linhas.add(new Object[]{
                r.id, r.nome, html.toString(),
                r.rendimentoUnidades,
                numero(r.rendimentoGramas, 0),
                numero(r.custoTotal, 2),
                numero(r.custoPorUnidade(), 2),
                r.validadeDias, r.situacao, ACOES
            });
            objetos.add(r);
        }

        tabela.setLinhas(linhas, objetos, receitas.size());
    }

    void abrirFormulario(Receita receita) {
        boolean nova = receita == null;
        ReceitaDAO dao = new ReceitaDAO();

        List<Object[]> produtos;
        List<Object[]> insumos;
        List<Object[]> ingredientes = new ArrayList<>();

        try {
            produtos = dao.listarProdutos();
            insumos = dao.listarInsumos();

            if (!nova) {
                ingredientes.addAll(
                    dao.listarIngredientes(receita.id)
                );
            }
        } catch (SQLException ex) {
            aviso("Erro ao consultar banco: " + ex.getMessage());
            return;
        }

        if (produtos.isEmpty() || insumos.isEmpty()) {
            aviso("Cadastre produtos e insumos ativos antes de criar receitas.");
            return;
        }

        JComboBox<Opcao> cbProduto = new JComboBox<>();
        JComboBox<Opcao> cbInsumo = new JComboBox<>();

        for (Object[] p : produtos) cbProduto.addItem(new Opcao(p));
        for (Object[] i : insumos) cbInsumo.addItem(new Opcao(i));

        if (!nova) {
            try {
                // Recupera o produto vinculado à receita.
                for (Object[] dados : dao.listarReceitas()) {
                    if (((Number) dados[0]).intValue() == receita.id) {
                        int idProduto = ((Number) dados[1]).intValue();

                        boolean encontrado = false;
                        for (int i = 0; i < cbProduto.getItemCount(); i++) {
                            if (cbProduto.getItemAt(i).id == idProduto) {
                                cbProduto.setSelectedIndex(i);
                                encontrado = true;
                                break;
                            }
                        }

                        if (!encontrado) {
                            aviso("O produto desta receita está inativo. "
                                + "Ative-o antes de editar.");
                            return;
                        }
                        break;
                    }
                }
            } catch (SQLException ex) {
                aviso("Erro ao consultar produto: " + ex.getMessage());
                return;
            }
        }

        JTextField txtNome = new JTextField(
            nova ? "" : receita.nome, 26
        );

        JSpinner spQuantidade = new JSpinner(
            new SpinnerNumberModel(1.0, 0.01, 1000000.0, 1.0)
        );

        DefaultListModel<String> modeloIngredientes =
            new DefaultListModel<>();
        JList<String> listaIngredientes =
            new JList<>(modeloIngredientes);

        Runnable atualizarIngredientes = () -> {
            modeloIngredientes.clear();

            for (Object[] item : ingredientes) {
                modeloIngredientes.addElement(
                    item[1] + " - " + item[2] + " " + item[3]
                );
            }
        };

        atualizarIngredientes.run();

        JButton btAdicionar = new JButton("Adicionar insumo");
        JButton btRemover = new JButton("Remover selecionado");

        btAdicionar.addActionListener(e -> {
            Opcao selecionado = (Opcao) cbInsumo.getSelectedItem();
            if (selecionado == null) return;

            for (Object[] item : ingredientes) {
                if (((Number) item[0]).intValue() == selecionado.id) {
                    aviso("Este insumo já foi adicionado.");
                    return;
                }
            }

            double quantidade =
                ((Number) spQuantidade.getValue()).doubleValue();

            ingredientes.add(new Object[]{
                selecionado.id,
                selecionado.nome,
                java.math.BigDecimal.valueOf(quantidade),
                selecionado.unidade
            });

            atualizarIngredientes.run();
        });

        btRemover.addActionListener(e -> {
            int indice = listaIngredientes.getSelectedIndex();

            if (indice >= 0) {
                ingredientes.remove(indice);
                atualizarIngredientes.run();
            }
        });

        JSpinner spUnidades = new JSpinner(
            new SpinnerNumberModel(
                nova ? 10 : receita.rendimentoUnidades,
                1, 100000, 1
            )
        );

        JSpinner spGramas = new JSpinner(
            new SpinnerNumberModel(
                nova ? 300.0 : receita.rendimentoGramas,
                0.0, 1000000.0, 10.0
            )
        );

        JSpinner spCusto = new JSpinner(
            new SpinnerNumberModel(
                nova ? 0.0 : receita.custoTotal,
                0.0, 1000000.0, 0.5
            )
        );

        JSpinner spValidade = new JSpinner(
            new SpinnerNumberModel(
                nova ? 180 : receita.validadeDias,
                1, 3650, 1
            )
        );

        JComboBox<String> cbSit = new JComboBox<>(
            new String[]{"Ativa", "Inativa"}
        );
        cbSit.setSelectedItem(
            nova ? "Ativa" : receita.situacao
        );

        JPanel numeros = new JPanel(
            new GridLayout(0, 2, 10, 4)
        );

        numeros.add(new JLabel("Rendimento (unidades)"));
        numeros.add(new JLabel("Rendimento (g)"));
        numeros.add(spUnidades);
        numeros.add(spGramas);
        numeros.add(new JLabel("Custo total (R$)"));
        numeros.add(new JLabel("Validade (dias)"));
        numeros.add(spCusto);
        numeros.add(spValidade);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JComponent[] campos = {
            new JLabel("Produto *"), cbProduto,
            new JLabel("Nome da receita *"), txtNome,
            new JLabel("Selecionar insumo"), cbInsumo,
            new JLabel("Quantidade"), spQuantidade,
            btAdicionar,
            new JScrollPane(listaIngredientes),
            btRemover,
            numeros,
            new JLabel("Situação"), cbSit
        };

        for (JComponent campo : campos) {
            campo.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(campo);
            form.add(Box.createVerticalStrut(5));
        }

        JScrollPane scroll = new JScrollPane(form);
        scroll.setPreferredSize(new Dimension(460, 500));

        while (true) {
            Object[] opcoes = {"Salvar", "Cancelar"};

            int resposta = JOptionPane.showOptionDialog(
                this, scroll,
                nova ? "Nova receita" : "Editar receita",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null, opcoes, opcoes[0]
            );

            if (resposta != 0) return;

            String nome = txtNome.getText().trim();

            if (nome.isEmpty() || ingredientes.isEmpty()) {
                aviso("Informe o nome e pelo menos um insumo.");
                continue;
            }

            Opcao produto = (Opcao) cbProduto.getSelectedItem();

            int unidades =
                ((Number) spUnidades.getValue()).intValue();
            double gramas =
                ((Number) spGramas.getValue()).doubleValue();
            double custo =
                ((Number) spCusto.getValue()).doubleValue();
            int validade =
                ((Number) spValidade.getValue()).intValue();
            String situacao =
                (String) cbSit.getSelectedItem();

            List<Object[]> dadosIngredientes = new ArrayList<>();

            for (Object[] item : ingredientes) {
                dadosIngredientes.add(new Object[]{
                    item[0], item[2]
                });
            }

            try {
                if (nova) {
                    dao.inserirReceita(
                        produto.id, nome, unidades,
                        gramas, custo, validade,
                        situacao, dadosIngredientes
                    );
                } else {
                    dao.atualizarReceita(
                        receita.id, produto.id, nome,
                        unidades, gramas, custo,
                        validade, situacao,
                        dadosIngredientes
                    );
                }

                carregarBancoAssincrono();

                JOptionPane.showMessageDialog(
                    this,
                    nova ? "Receita cadastrada com sucesso!"
                         : "Receita atualizada com sucesso!"
                );
                return;

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Erro ao salvar receita: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void excluir(Receita r) {
        Object[] opcoes = {"Sim", "Não"};

        int resposta = JOptionPane.showOptionDialog(
            this,
            "Tem certeza de que deseja excluir \"" + r.nome + "\"?",
            "Confirmar exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null, opcoes, opcoes[1]
        );

        if (resposta != 0) return;

        try {
            new ReceitaDAO().excluirReceita(r.id);
            carregarBancoAssincrono();

            JOptionPane.showMessageDialog(
                this, "Receita excluída com sucesso!"
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao excluir receita: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE
            );
        }
    }
}


    // VALIDAÇÃO E FORMATAÇÃO
    
   
    private static String formatarDocumento(String texto) {
        String n = texto.replaceAll("\\D", ""); 
        if (n.isEmpty()) {
            return "-";
        }
        if (n.length() == 11) {
            return n.substring(0, 3) + "." + n.substring(3, 6) + "." + n.substring(6, 9) + "-" + n.substring(9);
        }
        if (n.length() == 14) {
            return n.substring(0, 2) + "." + n.substring(2, 5) + "." + n.substring(5, 8) + "/"
                    + n.substring(8, 12) + "-" + n.substring(12);
        }
        return null;
    }

    
    private static String formatarTelefone(String texto) {
        String n = texto.replaceAll("\\D", "");
        if (n.isEmpty()) {
            return "-";
        }
        if (n.length() == 11) {
            return "(" + n.substring(0, 2) + ") " + n.substring(2, 7) + "-" + n.substring(7);
        }
        if (n.length() == 10) {
            return "(" + n.substring(0, 2) + ") " + n.substring(2, 6) + "-" + n.substring(6);
        }
        return null;
    }

    private static String numero(double v, int casas) {
        return String.format(BR, "%,." + casas + "f", v); // 1200 -> "1.200" | 12.5 -> "12,50"
    }

    
    private static List<String[]> lista(String[][] dados) {
        List<String[]> l = new ArrayList<>();
        for (String[] d : dados) {
            l.add(d.clone());
        }
        return l;
    }
    private static List<String[]> carregarClientesBanco() {

    List<String[]> clientes = new ArrayList<>();

    try {
        ClientesDAO dao = new ClientesDAO();

        for (Object[] c : dao.listarClientes()) {

            String nome = (String) c[1];
            String documento = (String) c[2];
            String telefone = (String) c[4];
            String cidade = (String) c[5];
            String uf = (String) c[6];
            String situacao = (String) c[7];

            clientes.add(new String[]{
                nome,
                documento,
                telefone,
                cidade + " - " + uf,
                situacao
            });
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(
            null,
            "Erro ao carregar clientes: " + ex.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE
        );
    }

    return clientes;
}
    private static List<String[]> dadosClientes() {
        return lista(new String[][]{
            {"Ana Beatriz Souza", "123.456.789-00", "(11) 98765-4321", "São Paulo - SP", "Ativo"},
            {"Carlos Mendes", "987.654.321-00", "(21) 91234-5678", "Rio de Janeiro - RJ", "Ativo"},
            {"Erika Ferreira", "456.789.123-00", "(31) 99876-5432", "Belo Horizonte - MG", "Ativo"},
            {"Juliana Lima", "321.654.987-00", "(11) 97654-3210", "Campinas - SP", "Inativo"},
            {"Lucas Almeida", "654.321.789-00", "(41) 98877-6655", "Curitiba - PR", "Ativo"},
            {"Mariana Costa", "789.123.456-00", "(51) 99988-7766", "Porto Alegre - RS", "Ativo"},
            {"Paula Nogueira", "147.258.369-00", "(61) 98123-4455", "Brasília - DF", "Ativo"},
            {"Ricardo Oliveira", "369.147.258-00", "(71) 91223-3344", "Salvador - BA", "Inativo"}
        });
    }

    private static List<String[]> dadosFornecedores() {
        return lista(new String[][]{
            {"Ervas do Brasil LTDA", "12.345.678/0001-90", "(11) 98765-4321", "São Paulo - SP", "Ativo"},
            {"Sabores Naturais", "98.765.432/0001-11", "(21) 91234-5678", "Rio de Janeiro - RJ", "Ativo"},
            {"Botânica Insumos", "45.678.912/0001-22", "(31) 99876-5432", "Belo Horizonte - MG", "Ativo"},
            {"Casa das Embalagens", "32.145.678/0001-33", "(41) 97654-3210", "Curitiba - PR", "Inativo"},
            {"Aroma e Flor Distribuidora", "78.912.345/0001-44", "(51) 99988-7766", "Porto Alegre - RS", "Ativo"},
            {"Chá & Cia Fornecimentos", "11.223.344/0001-55", "(61) 98123-4455", "Brasília - DF", "Ativo"},
            {"Vida Verde Produtos Naturais", "66.554.433/0001-66", "(71) 91223-3344", "Salvador - BA", "Ativo"},
            {"Insumos da Terra", "22.334.455/0001-77", "(19) 98876-6655", "Campinas - SP", "Inativo"}
        });
    }

    private static List<String[]> dadosParceiros() {
        return lista(new String[][]{
            {"Bem Estar Terapias", "12.345.678/0001-90", "(11) 98765-4321", "São Paulo - SP", "Ativo"},
            {"Espaço Harmonia", "98.765.432/0001-11", "(21) 91234-5678", "Rio de Janeiro - RJ", "Ativo"},
            {"Loja Vida Natural", "45.678.912/0001-22", "(31) 99876-5432", "Belo Horizonte - MG", "Ativo"},
            {"Empório do Grão", "32.145.678/0001-33", "(41) 97654-3210", "Curitiba - PR", "Inativo"},
            {"Clinic Mais Saúde", "78.912.344/0001-44", "(51) 99988-7766", "Porto Alegre - RS", "Ativo"},
            {"Viva Leve", "11.223.344/0001-55", "(61) 98123-4455", "Brasília - DF", "Ativo"},
            {"Raízes e Sabores", "66.554.433/0001-66", "(71) 91223-3344", "Salvador - BA", "Ativo"},
            {"Cantinho Natural", "22.334.455/0001-77", "(19) 98876-6655", "Campinas - SP", "Inativo"}
        });
    }

    private static List<Receita> dadosReceitas() {
        List<Receita> l = new ArrayList<>();
        l.add(new Receita(1, "Blend Calme", Arrays.asList("Camomila - 15 g", "Erva-doce - 10 g", "Melissa - 10 g"), 10, 300, 12.50, 180, "Ativa"));
        l.add(new Receita(2, "Blend Felicità", Arrays.asList("Hibisco - 20 g", "Maçã desidratada - 15 g", "Canela - 10 g"), 12, 960, 18.40, 180, "Ativa"));
        l.add(new Receita(3, "Blend Ormoni", Arrays.asList("Chá verde - 15 g", "Hortelã - 10 g", "Capim-limão - 10 g"), 10, 400, 14.20, 180, "Ativa"));
        l.add(new Receita(4, "Blend MaterniTea", Arrays.asList("Rooibos - 20 g", "Erva-doce - 10 g", "Camomila - 10 g"), 8, 720, 16.80, 180, "Ativa"));
        l.add(new Receita(5, "Blend Airmid", Arrays.asList("Lavanda - 15 g", "Melissa - 10 g", "Hortelã - 10 g"), 10, 400, 13.60, 180, "Ativa"));
        l.add(new Receita(6, "Chai Masala", Arrays.asList("Chá preto - 20 g", "Canela - 10 g", "Gengibre - 10 g"), 15, 1200, 22.50, 180, "Inativa"));
        l.add(new Receita(7, "Home Spray Relax", Arrays.asList("Álcool de cereais - 200 ml", "Essência de lavanda - 30 ml", "Água destilada - 70 ml"), 20, 300, 25.00, 365, "Ativa"));
        l.add(new Receita(8, "Home Spray Energia", Arrays.asList("Álcool de cereais - 200 ml", "Essência de alecrim - 30 ml", "Água destilada - 70 ml"), 20, 300, 24.00, 365, "Ativa"));
        l.add(new Receita(9, "Blend Detox", Arrays.asList("Chá verde - 15 g", "Gengibre - 10 g", "Hortelã - 10 g"), 10, 400, 13.90, 180, "Ativa"));
        l.add(new Receita(10, "Home Spray Capim-limão", Arrays.asList("Álcool de cereais - 200 ml", "Essência de capim-limão - 30 ml", "Água destilada - 70 ml"), 20, 300, 24.50, 365, "Ativa"));
        return l;
    }

  

    
    private static JPanel linhaFiltros(CampoBusca busca, String[] rotulos, JComboBox<?>... combos) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.anchor = GridBagConstraints.SOUTH;
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 40);
        p.add(busca, g);
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        for (int i = 0; i < combos.length; i++) {
            g.gridx = i + 1;
            g.insets = new Insets(0, 0, 0, i < combos.length - 1 ? 14 : 0);
            JPanel campo = new JPanel(new BorderLayout(0, 4));
            campo.setOpaque(false);
            JLabel l = new JLabel(rotulos[i]);
            l.setFont(new Font(FONTE, Font.PLAIN, 12));
            l.setForeground(AZUL_TITULO);
            campo.add(l, BorderLayout.NORTH);
            campo.add(combos[i], BorderLayout.CENTER);
            p.add(campo, g);
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
                new LineBorder(VERDE, 1, true), new EmptyBorder(11, 22, 11, 22)));
        return b;
    }

    
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

    /** Campo de busca com lupa desenhada e texto de dica quando vazio. */
    private static class CampoBusca extends JTextField {
        private final String dica;

        CampoBusca(String dica) {
            this.dica = dica;
            setFont(new Font(FONTE, Font.PLAIN, 14));
            setForeground(AZUL_TITULO);
            setPreferredSize(new Dimension(200, 40));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDA, 1, true), new EmptyBorder(0, 40, 0, 12)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = suavizar(g);
            // lupa
            g2.setColor(AZUL_TITULO);
            g2.setStroke(new BasicStroke(2f));
            int cy = getHeight() / 2;
            g2.drawOval(14, cy - 8, 12, 12);
            g2.drawLine(24, cy + 2, 29, cy + 7);
            // dica
            if (getText().isEmpty()) {
                g2.setFont(getFont());
                g2.setColor(new Color(160, 170, 190));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(dica, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    
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
            int w = fm.stringWidth(getText()) + 26;
            int h = 22;
            g2.setColor(corSelo);
            g2.fillRoundRect((getWidth() - w) / 2, (getHeight() - h) / 2, w, h, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class Renderizador extends DefaultTableCellRenderer {
        private static final String TEXTO_ACOES =
                "<html><b><font color='#165728'>Editar</font>&nbsp;&nbsp;&nbsp;"
                + "<font color='#d32f2f'>Excluir</font></b></html>";
        private final Selo selo = new Selo();

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor,
                boolean selecionado, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(t, valor, selecionado, false, linha, coluna);
            Color fundo = selecionado ? VERDE_CLARO : (linha % 2 == 0 ? BRANCO : new Color(250, 251, 250));
            String nomeColuna = t.getColumnName(coluna);
            String texto = (valor == null) ? "" : valor.toString();

            if (nomeColuna.equals("Situação")) {
                selo.setText(texto);
                selo.corFundo = fundo;
                if (SITUACAO_VERDE.contains(texto)) {
                    selo.corSelo = new Color(220, 240, 220);
                    selo.setForeground(new Color(30, 110, 50));
                } else if (SITUACAO_VERMELHA.contains(texto)) {
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
            setBorder(new EmptyBorder(0, 12, 0, 8));
            // números e colunas com título em 2 linhas ficam centralizados
            boolean centro = valor instanceof Number || nomeColuna.startsWith("<html>") || nomeColuna.equals("Ações");
            setHorizontalAlignment(centro ? CENTER : LEFT);
            if (ACOES.equals(texto)) {
                setText(TEXTO_ACOES);
                setBorder(new EmptyBorder(0, 2, 0, 2));
            } else if (texto.startsWith("<html>")) {
                setFont(new Font(FONTE, Font.PLAIN, 12)); // ingredientes
            }
            return this;
        }
    }

    private interface AcaoTabela {
        void executar(Object objeto, boolean ladoDireito);
    }

    private static class TabelaCadastro extends JPanel {
        private final String[] colunas;
        private final String textoTotal;
        private final AcaoTabela acao;
        private final DefaultTableModel modelo;
        private final JTable tabela;
        private final JLabel lblInfo = new JLabel();
        private final JPanel painelPaginas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        private List<Object[]> linhas = new ArrayList<>();
        private List<?> objetos = new ArrayList<>();
        private int totalCadastrados = 0;
        private int pagina = 0;
        private final int porPagina = 8;

        TabelaCadastro(String[] colunas, String textoTotal, int alturaLinha, AcaoTabela acao) {
            this.colunas = colunas;
            this.textoTotal = textoTotal;
            this.acao = acao;

            modelo = new DefaultTableModel(colunas, 0) {
                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }
            };
            tabela = new JTable(modelo);
            tabela.setRowHeight(alturaLinha);
            tabela.setGridColor(BORDA);
            tabela.setSelectionBackground(VERDE_CLARO);
            tabela.setDefaultRenderer(Object.class, new Renderizador());
            estilizarCabecalho(tabela);

            TableColumn colunaAcoes = tabela.getColumnModel().getColumn(colunas.length - 1);
            colunaAcoes.setMinWidth(130);
            colunaAcoes.setMaxWidth(150);

            tabela.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int linha = tabela.rowAtPoint(e.getPoint());
                    int coluna = tabela.columnAtPoint(e.getPoint());
                    if (linha < 0 || coluna != colunas.length - 1) {
                        return;
                    }
                    Rectangle celula = tabela.getCellRect(linha, coluna, false);
                    boolean ladoDireito = e.getX() > celula.x + celula.width / 2;
                    int indice = pagina * porPagina + linha;
                    if (indice < objetos.size()) {
                        acao.executar(objetos.get(indice), ladoDireito);
                    }
                }
            });

            JPanel caixa = new JPanel(new BorderLayout());
            caixa.setBorder(new LineBorder(BORDA, 1, true));
            caixa.add(tabela.getTableHeader(), BorderLayout.NORTH);
            caixa.add(tabela, BorderLayout.CENTER);

            lblInfo.setFont(new Font(FONTE, Font.PLAIN, 13));
            lblInfo.setForeground(AZUL_TEXTO);
            painelPaginas.setOpaque(false);

            JPanel rodape = new JPanel(new BorderLayout());
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(14, 4, 0, 0));
            rodape.add(lblInfo, BorderLayout.WEST);
            rodape.add(painelPaginas, BorderLayout.EAST);

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

        void setLinhas(List<Object[]> novasLinhas, List<?> novosObjetos, int total) {
            this.linhas = novasLinhas;
            this.objetos = novosObjetos;
            this.totalCadastrados = total;
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

            String info = totalCadastrados + " " + textoTotal;
            if (total != totalCadastrados) {
                info += " (" + total + " no filtro)";
            }
            lblInfo.setText(info);

            painelPaginas.removeAll();
            if (totalPaginas > 1) { // só mostra os botões se tiver mais de uma página
                painelPaginas.add(botaoPagina("<", false, pagina > 0, () -> irPara(pagina - 1)));
                for (int p = 0; p < totalPaginas; p++) {
                    final int destino = p;
                    painelPaginas.add(botaoPagina(String.valueOf(p + 1), p == pagina, true, () -> irPara(destino)));
                }
                painelPaginas.add(botaoPagina(">", false, pagina < totalPaginas - 1, () -> irPara(pagina + 1)));
            }
            revalidate();
            repaint();
        }

        private JButton botaoPagina(String texto, boolean ativo, boolean habilitado, Runnable aoClicar) {
            JButton b = new JButton(texto);
            b.setFont(new Font(FONTE, Font.PLAIN, 13));
            b.setFocusPainted(false);
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setPreferredSize(new Dimension(32, 30));
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
        cabecalho.setPreferredSize(new Dimension(0, 44));
        cabecalho.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionado, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                String texto = String.valueOf(valor);
                setFont(new Font(FONTE, Font.BOLD, 12));
                setBackground(new Color(240, 243, 240));
                setForeground(AZUL_TITULO);
                boolean centro = texto.startsWith("<html>") || texto.equals("Ações") || texto.equals("Situação") || texto.equals("ID");
                setHorizontalAlignment(centro ? CENTER : LEFT);
                int lado = (texto.startsWith("<html>") || texto.equals("ID")) ? 2 : 12; // títulos de 2 linhas: menos margem
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, BORDA), new EmptyBorder(0, lado, 0, lado)));
                return this;
            }
        });
    }
}