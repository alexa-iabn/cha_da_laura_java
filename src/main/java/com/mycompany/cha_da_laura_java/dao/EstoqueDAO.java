package com.mycompany.cha_da_laura_java.dao;

import com.mycompany.cha_da_laura_java.conexao.ConexaoBanco;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Leitura do estoque existente sem alterar os registros do banco. */
public class EstoqueDAO {
    public List<Object[]> listarInsumos() throws SQLException {
        String sql = "SELECT id_insumo, nome, COALESCE(categoria, 'Sem categoria') categoria, "
                + "unidade_medida, estoque_atual, estoque_minimo FROM insumos WHERE ativo = 1 ORDER BY nome";
        List<Object[]> dados = new ArrayList<>();
        try (Connection c = ConexaoBanco.conectar();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                dados.add(new Object[]{r.getInt("id_insumo"), r.getString("nome"),
                    r.getString("categoria"), r.getString("unidade_medida"),
                    r.getDouble("estoque_atual"), r.getDouble("estoque_minimo")});
            }
        }
        return dados;
    }

    public List<Object[]> listarProdutos() throws SQLException {
        String sql = "SELECT id_produto, nome, COALESCE(categoria, 'Sem categoria') categoria, "
                + "COALESCE(unidade_medida, 'un') unidade_medida, estoque_atual, estoque_minimo "
                + "FROM produtos WHERE ativo = 1 ORDER BY nome";
        List<Object[]> dados = new ArrayList<>();
        try (Connection c = ConexaoBanco.conectar();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                dados.add(new Object[]{r.getInt("id_produto"), r.getString("nome"),
                    r.getString("categoria"), r.getString("unidade_medida"),
                    r.getDouble("estoque_atual"), r.getDouble("estoque_minimo")});
            }
        }
        return dados;
    }

    /** Cadastro e entrada inicial em uma única transação. */
    public void inserirInsumo(String nome, String categoria, String unidade,
                             double minimo, double inicial) throws SQLException {
        if (!Double.isFinite(inicial) || inicial < 0 || !Double.isFinite(minimo) || minimo < 0) {
            throw new SQLException("Quantidades inválidas.");
        }
        String sql = "INSERT INTO insumos (nome, categoria, unidade_medida, estoque_atual, estoque_minimo, ativo) VALUES (?, ?, ?, ?, ?, 1)";
        try (Connection c = ConexaoBanco.conectar()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1, nome);
                    p.setString(2, categoria);
                    p.setString(3, unidade);
                    p.setDouble(4, inicial);
                    p.setDouble(5, minimo);
                    p.executeUpdate();
                    try (ResultSet keys = p.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Não foi possível obter o ID do insumo.");
                        id = keys.getInt(1);
                    }
                }
                if (inicial > 0) {
                    String movimento = "INSERT INTO movimentacoes_estoque "
                            + "(id_insumo, id_produto, tipo, quantidade, estoque_anterior, "
                            + "estoque_posterior, motivo, usuario, observacoes) "
                            + "VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement p = c.prepareStatement(movimento)) {
                        p.setInt(1, id);
                        p.setString(2, "Entrada");
                        p.setDouble(3, inicial);
                        p.setDouble(4, 0);
                        p.setDouble(5, inicial);
                        p.setString(6, "Entrada inicial");
                        p.setString(7, "Sistema");
                        p.setString(8, "Estoque inicial no cadastro do insumo");
                        p.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                try { c.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            }
        }
    }

    public void atualizarInsumo(int id, String nome, String categoria, String unidade, double minimo) throws SQLException {
        String sql = "UPDATE insumos SET nome=?, categoria=?, unidade_medida=?, estoque_minimo=? WHERE id_insumo=? AND ativo=1";
        try (Connection c = ConexaoBanco.conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, nome); p.setString(2, categoria); p.setString(3, unidade);
            p.setDouble(4, minimo); p.setInt(5, id);
            if (p.executeUpdate() == 0) throw new SQLException("Insumo não encontrado ou inativo.");
        }
    }

    public void excluirInsumo(int id) throws SQLException {
        String sql = "UPDATE insumos SET ativo=0 WHERE id_insumo=? AND ativo=1";
        try (Connection c = ConexaoBanco.conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            if (p.executeUpdate() == 0) throw new SQLException("Insumo não encontrado ou inativo.");
        }
    }

    /** Produtos preservam preço e vínculos com receitas ao serem atualizados. */
    public void inserirProduto(String nome, String categoria, String unidade, double minimo, double inicial) throws SQLException {
        validarQuantidades(minimo, inicial);
        try (Connection c = ConexaoBanco.conectar()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO produtos (nome,categoria,unidade_medida,estoque_atual,estoque_minimo,ativo) VALUES (?,?,?,?,?,1)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1,nome); p.setString(2,categoria); p.setString(3,unidade);
                    p.setDouble(4,inicial); p.setDouble(5,minimo); p.executeUpdate();
                    try (ResultSet r=p.getGeneratedKeys()) {
                        if (!r.next()) throw new SQLException("ID do produto não encontrado.");
                        id=r.getInt(1);
                    }
                }
                if (inicial > 0) gravarMovimento(c, null, id, "Entrada", inicial, 0, inicial,
                        "Entrada inicial", "Sistema");
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    public void atualizarProduto(int id, String nome, String categoria, String unidade, double minimo) throws SQLException {
        validarQuantidades(minimo,0);
        try (Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement(
                "UPDATE produtos SET nome=?,categoria=?,unidade_medida=?,estoque_minimo=? WHERE id_produto=? AND ativo=1")) {
            p.setString(1,nome);p.setString(2,categoria);p.setString(3,unidade);
            p.setDouble(4,minimo);p.setInt(5,id);
            if(p.executeUpdate()==0) throw new SQLException("Produto não encontrado ou inativo.");
        }
    }

    public void excluirProduto(int id) throws SQLException {
        try (Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement(
                "UPDATE produtos SET ativo=0 WHERE id_produto=? AND ativo=1")) {
            p.setInt(1,id);
            if(p.executeUpdate()==0) throw new SQLException("Produto não encontrado ou inativo.");
        }
    }

    private static void validarQuantidades(double minimo,double inicial) throws SQLException {
        if(!Double.isFinite(minimo)||minimo<0||!Double.isFinite(inicial)||inicial<0)
            throw new SQLException("Quantidade inválida.");
    }

    private static void gravarMovimento(Connection c,Integer insumo,Integer produto,String tipo,
            double quantidade,double anterior,double posterior,String motivo,String usuario) throws SQLException {
        try (PreparedStatement p=c.prepareStatement(
                "INSERT INTO movimentacoes_estoque (id_insumo,id_produto,tipo,quantidade,estoque_anterior,estoque_posterior,motivo,usuario,observacoes) VALUES (?,?,?,?,?,?,?,?,?)")) {
            if(insumo==null)p.setNull(1,Types.INTEGER);else p.setInt(1,insumo);
            if(produto==null)p.setNull(2,Types.INTEGER);else p.setInt(2,produto);
            p.setString(3,tipo);p.setDouble(4,quantidade);p.setDouble(5,anterior);
            p.setDouble(6,posterior);p.setString(7,motivo);p.setString(8,usuario);
            p.setString(9,motivo);p.executeUpdate();
        }
    }

    /** Bloqueio da linha evita perda de atualização em movimentações simultâneas. */
    public double registrarMovimentacao(int id,boolean produto,String tipo,double quantidade,
            String motivo,String usuario) throws SQLException {
        if(!"Entrada".equals(tipo)&&!"Saída".equals(tipo)&&!"Ajuste".equals(tipo))
            throw new SQLException("Tipo inválido.");
        if(!Double.isFinite(quantidade)||quantidade==0||(!"Ajuste".equals(tipo)&&quantidade<0))
            throw new SQLException("Quantidade inválida.");
        String tabela=produto?"produtos":"insumos", chave=produto?"id_produto":"id_insumo";
        try(Connection c=ConexaoBanco.conectar()) {
            c.setAutoCommit(false);
            try {
                double anterior;
                try(PreparedStatement p=c.prepareStatement("SELECT estoque_atual FROM "+tabela+
                        " WHERE "+chave+"=? AND ativo=1 FOR UPDATE")) {
                    p.setInt(1,id);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next())throw new SQLException("Item não encontrado ou inativo.");
                        anterior=r.getDouble(1);
                    }
                }
                double variacao="Saída".equals(tipo)?-quantidade:quantidade;
                double posterior=anterior+variacao;
                if(posterior < -0.000001)throw new SQLException("Estoque insuficiente.");
                if(posterior<0)posterior=0;
                try(PreparedStatement p=c.prepareStatement("UPDATE "+tabela+" SET estoque_atual=? WHERE "+chave+"=?")) {
                    p.setDouble(1,posterior);p.setInt(2,id);p.executeUpdate();
                }
                gravarMovimento(c,produto?null:id,produto?id:null,tipo,variacao,anterior,posterior,motivo,usuario);
                c.commit();return posterior;
            } catch(SQLException | RuntimeException e) { c.rollback();throw e; }
        }
    }

    /** Linhas do histórico: ID, data, tipo, nome, categoria, variação, unidade, antes, depois, motivo, usuário. */
    public List<Object[]> listarMovimentacoes() throws SQLException {
        String sql="SELECT m.id_movimentacao,m.data_movimentacao,m.tipo, " +
            "COALESCE(CASE WHEN m.id_produto IS NOT NULL THEN p.nome ELSE i.nome END,'Item excluído') nome, " +
            "COALESCE(CASE WHEN m.id_produto IS NOT NULL THEN p.categoria ELSE i.categoria END,'Sem categoria') categoria, " +
            "m.quantidade,COALESCE(CASE WHEN m.id_produto IS NOT NULL THEN 'un' ELSE i.unidade_medida END,'un') unidade, " +
            "m.estoque_anterior,m.estoque_posterior,COALESCE(m.motivo,m.observacoes,'') motivo, " +
            "COALESCE(m.usuario,'Sistema') usuario FROM movimentacoes_estoque m " +
            "LEFT JOIN insumos i ON m.id_insumo=i.id_insumo " +
            "LEFT JOIN produtos p ON m.id_produto=p.id_produto ORDER BY m.data_movimentacao DESC,m.id_movimentacao DESC";
        List<Object[]> linhas=new ArrayList<>();
        try(Connection c=ConexaoBanco.conectar();PreparedStatement p=c.prepareStatement(sql);
                ResultSet r=p.executeQuery()) {
            while(r.next()) {
                Timestamp data=r.getTimestamp(2);
                double qtd=r.getDouble(6);
                String tipo=r.getString(3);
                if("Saída".equalsIgnoreCase(tipo)&&qtd>0)qtd=-qtd;
                linhas.add(new Object[]{r.getInt(1),data==null?java.time.LocalDateTime.now():data.toLocalDateTime(),
                    tipo,r.getString(4),r.getString(5),qtd,r.getString(7),r.getDouble(8),r.getDouble(9),
                    r.getString(10),r.getString(11)});
            }
        }
        return linhas;
    }

}


