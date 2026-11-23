package com.mycompany.cha_da_laura_java.dao;

import com.mycompany.cha_da_laura_java.conexao.ConexaoBanco;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Saídas de produtos com baixa de estoque e histórico na mesma transação. */
public class SaidaDAO {
    /** id, nome, categoria, unidade, estoque */
    public List<Object[]> listarProdutos() throws SQLException {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT id_produto,nome,categoria,unidade_medida,estoque_atual FROM produtos WHERE ativo=1 ORDER BY nome";
        try (Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while(r.next()) lista.add(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getBigDecimal(5)});
        }
        return lista;
    }
    /** id, nome */
    public List<Object[]> listarUsuarios() throws SQLException {
        List<Object[]> lista = new ArrayList<>();
        try(Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement("SELECT id,nome FROM usuarios WHERE situacao='Ativo' ORDER BY nome"); ResultSet r=p.executeQuery()) {
            while(r.next()) lista.add(new Object[]{r.getInt(1),r.getString(2)});
        }
        return lista;
    }
    /** Tipo, id, nome: entidades opcionais para a saída. */
    public List<Object[]> listarPessoas() throws SQLException {
        List<Object[]> lista=new ArrayList<>();
        // Não presumimos colunas além das chaves. Nomes de pessoas serão integrados após verificar o esquema.
        return lista;
    }
    /** id, data, produto, quantidade, destino, motivo, usuário, observações */
    public List<Object[]> listarSaidas() throws SQLException {
        List<Object[]> lista=new ArrayList<>();
        String sql="SELECT s.id,s.data_saida,p.nome,s.quantidade,s.destino,s.motivo,u.nome,s.observacoes " +
                "FROM saidas s JOIN produtos p ON p.id_produto=s.produto_id " +
                "JOIN usuarios u ON u.id=s.usuario_id ORDER BY s.data_saida DESC,s.id DESC";
        try(Connection c=ConexaoBanco.conectar();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()) {
            while(r.next()) lista.add(new Object[]{r.getInt(1),r.getTimestamp(2).toLocalDateTime().toLocalDate(),r.getString(3),r.getBigDecimal(4),r.getString(5),r.getString(6),r.getString(7),r.getString(8)});
        }
        return lista;
    }
    public BigDecimal registrarSaida(int produtoId, BigDecimal quantidade, String motivo, String destino,
                                    int usuarioId, String observacoes) throws SQLException {
        if(produtoId<=0 || usuarioId<=0 || quantidade==null || quantidade.signum()<=0 || quantidade.scale()>3)
            throw new SQLException("Produto, usuário ou quantidade inválidos.");
        if(motivo==null || motivo.isBlank() || motivo.length()>60) throw new SQLException("Motivo inválido.");
        if(!"Cliente".equals(destino)&&!"Parceiro".equals(destino)&&!"E-commerce".equals(destino))
            throw new SQLException("Destino inválido.");
        try(Connection c=ConexaoBanco.conectar()) {
            c.setAutoCommit(false);
            try {
                String nomeUsuario;
                try(PreparedStatement p=c.prepareStatement("SELECT nome FROM usuarios WHERE id=? AND situacao='Ativo'")) {
                    p.setInt(1,usuarioId);
                    try(ResultSet r=p.executeQuery()) {if(!r.next())throw new SQLException("Usuário não encontrado ou inativo.");nomeUsuario=r.getString(1);}
                }
                BigDecimal anterior;
                try(PreparedStatement p=c.prepareStatement("SELECT estoque_atual FROM produtos WHERE id_produto=? AND ativo=1 FOR UPDATE")) {
                    p.setInt(1,produtoId);
                    try(ResultSet r=p.executeQuery()) {if(!r.next())throw new SQLException("Produto não encontrado ou inativo.");anterior=r.getBigDecimal(1);}
                }
                if(anterior.compareTo(quantidade)<0)throw new SQLException("Estoque insuficiente. Disponível: "+anterior.toPlainString());
                BigDecimal posterior=anterior.subtract(quantidade);
                try(PreparedStatement p=c.prepareStatement("UPDATE produtos SET estoque_atual=? WHERE id_produto=?")) {
                    p.setBigDecimal(1,posterior);p.setInt(2,produtoId);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO saidas (produto_id,quantidade,motivo,destino,usuario_id,observacoes) VALUES (?,?,?,?,?,?)")) {
                    p.setInt(1,produtoId);p.setBigDecimal(2,quantidade);p.setString(3,motivo);p.setString(4,destino);p.setInt(5,usuarioId);p.setString(6,observacoes);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO movimentacoes_estoque (id_produto,tipo,quantidade,estoque_anterior,estoque_posterior,motivo,usuario,observacoes) VALUES (?,'Saída',?,?,?,?,?,?)")) {
                    p.setInt(1,produtoId);p.setBigDecimal(2,quantidade.negate());p.setBigDecimal(3,anterior);p.setBigDecimal(4,posterior);
                    p.setString(5,motivo);p.setString(6,nomeUsuario);p.setString(7,observacoes);p.executeUpdate();
                }
                c.commit();return posterior;
            } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
        }
    }
}
