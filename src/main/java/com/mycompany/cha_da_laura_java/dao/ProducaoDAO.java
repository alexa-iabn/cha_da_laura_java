/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.cha_da_laura_java.dao;

import com.mycompany.cha_da_laura_java.conexao.ConexaoBanco;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Produções e movimentos de estoque, com gravação atômica no MySQL. */
public class ProducaoDAO {
    /** id_receita, nome_receita, id_produto, nome_produto, validade_dias, rendimento_unidades */
    public List<Object[]> listarReceitasAtivas() throws SQLException {
        String sql = "SELECT r.id_receita,r.nome,r.id_produto,p.nome,r.validade_dias,r.rendimento_unidades "
                + "FROM receitas r JOIN produtos p ON p.id_produto=r.id_produto "
                + "WHERE r.ativo=1 AND p.ativo=1 ORDER BY p.nome,r.nome";
        List<Object[]> dados = new ArrayList<>();
        try (Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement(sql);
             ResultSet r=p.executeQuery()) {
            while(r.next()) dados.add(new Object[]{r.getInt(1),r.getString(2),r.getInt(3),r.getString(4),r.getInt(5),r.getInt(6)});
        }
        return dados;
    }

    /** id_insumo, nome, quantidade_lote, unidade, estoque_atual */
    public List<Object[]> listarInsumosReceita(int idReceita) throws SQLException {
        String sql="SELECT i.id_insumo,i.nome,ri.quantidade,i.unidade_medida,i.estoque_atual "
                + "FROM receita_insumos ri JOIN insumos i ON i.id_insumo=ri.id_insumo "
                + "WHERE ri.id_receita=? ORDER BY i.nome";
        List<Object[]> dados=new ArrayList<>();
        try(Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,idReceita);
            try(ResultSet r=p.executeQuery()) {
                while(r.next()) dados.add(new Object[]{r.getInt(1),r.getString(2),r.getBigDecimal(3),r.getString(4),r.getBigDecimal(5)});
            }
        }
        return dados;
    }

    /** id, data, produto, quantidade, receita, status, usuario, lote, validade, observacoes */
    public List<Object[]> listarProducoes() throws SQLException {
        String sql="SELECT pr.id_producao,pr.data_producao,p.nome,pr.quantidade,r.nome,pr.status,"
                + "COALESCE(pr.usuario,'Sistema'),pr.lote,pr.data_validade,pr.observacoes "
                + "FROM producoes pr JOIN produtos p ON p.id_produto=pr.id_produto "
                + "JOIN receitas r ON r.id_receita=pr.id_receita ORDER BY pr.id_producao DESC";
        List<Object[]> dados=new ArrayList<>();
        try(Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement(sql);
            ResultSet r=p.executeQuery()) {
            while(r.next()) dados.add(new Object[]{r.getInt(1),r.getDate(2).toLocalDate(),r.getString(3),r.getInt(4),r.getString(5),r.getString(6),r.getString(7),r.getString(8),r.getDate(9).toLocalDate(),r.getString(10)});
        }
        return dados;
    }

    /** Insumos cadastrados para o rendimento TOTAL da receita; consumo proporcional à quantidade produzida. */
    public int registrarProducao(int idReceita,int quantidade,String lote,LocalDate validade,
                                 String usuario,String observacoes) throws SQLException {
        if(quantidade<=0 || lote==null || lote.trim().isEmpty() || lote.length()>50 || validade==null || !validade.isAfter(LocalDate.now()))
            throw new SQLException("Quantidade, lote ou validade inválidos.");
        try(Connection c=ConexaoBanco.conectar()) {
            c.setAutoCommit(false);
            try {
                int idProduto, rendimento;
                try(PreparedStatement p=c.prepareStatement("SELECT id_produto,rendimento_unidades FROM receitas WHERE id_receita=? AND ativo=1")) {
                    p.setInt(1,idReceita);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next()) throw new SQLException("Receita não encontrada ou inativa.");
                        idProduto=r.getInt(1); rendimento=r.getInt(2);
                    }
                }
                if(rendimento<=0) throw new SQLException("O rendimento da receita deve ser maior que zero.");
                BigDecimal anteriorProduto;
                try(PreparedStatement p=c.prepareStatement("SELECT estoque_atual FROM produtos WHERE id_produto=? AND ativo=1 FOR UPDATE")) {
                    p.setInt(1,idProduto);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next()) throw new SQLException("Produto não encontrado ou inativo.");
                        anteriorProduto=r.getBigDecimal(1);
                    }
                }
                List<Object[]> itens=new ArrayList<>();
                String consulta="SELECT i.id_insumo,i.nome,ri.quantidade,i.estoque_atual,i.ativo "
                        + "FROM receita_insumos ri JOIN insumos i ON i.id_insumo=ri.id_insumo "
                        + "WHERE ri.id_receita=? ORDER BY i.id_insumo FOR UPDATE";
                try(PreparedStatement p=c.prepareStatement(consulta)) {
                    p.setInt(1,idReceita);
                    try(ResultSet r=p.executeQuery()) {
                        while(r.next()) {
                            BigDecimal necessario=r.getBigDecimal(3).multiply(BigDecimal.valueOf(quantidade))
                                    .divide(BigDecimal.valueOf(rendimento),2,RoundingMode.HALF_UP);
                            BigDecimal atual=r.getBigDecimal(4);
                            if(!r.getBoolean(5)) throw new SQLException("Insumo inativo: "+r.getString(2));
                            if(necessario.signum()<=0) throw new SQLException("Quantidade insuficiente na receita para: "+r.getString(2));
                            if(atual.compareTo(necessario)<0) throw new SQLException("Estoque insuficiente de "+r.getString(2)+". Necessário: "+necessario+"; disponível: "+atual);
                            itens.add(new Object[]{r.getInt(1),necessario,atual});
                        }
                    }
                }
                if(itens.isEmpty()) throw new SQLException("A receita não possui ingredientes cadastrados.");
                int idProducao;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO producoes (id_produto,id_receita,quantidade,lote,data_producao,data_validade,status,usuario,observacoes) VALUES (?,?,?,?,? ,?,'Concluída',?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    p.setInt(1,idProduto); p.setInt(2,idReceita); p.setInt(3,quantidade);
                    p.setString(4,lote.trim()); p.setDate(5,Date.valueOf(LocalDate.now()));
                    p.setDate(6,Date.valueOf(validade)); p.setString(7,usuario); p.setString(8,observacoes);
                    p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()) {
                        if(!r.next()) throw new SQLException("Não foi possível gerar o ID da produção.");
                        idProducao=r.getInt(1);
                    }
                }
                try(PreparedStatement consumo=c.prepareStatement("INSERT INTO producao_insumos (id_producao,id_insumo,quantidade_utilizada) VALUES (?,?,?)");
                    PreparedStatement baixa=c.prepareStatement("UPDATE insumos SET estoque_atual=? WHERE id_insumo=?");
                    PreparedStatement mov=c.prepareStatement("INSERT INTO movimentacoes_estoque (id_insumo,id_produto,id_producao,tipo,quantidade,estoque_anterior,estoque_posterior,motivo,usuario,observacoes) VALUES (?,?,? ,?,?,?,?,?,?,?)")) {
                    for(Object[] item:itens) {
                        int id=(Integer)item[0]; BigDecimal usado=(BigDecimal)item[1], antes=(BigDecimal)item[2], depois=antes.subtract(usado);
                        consumo.setInt(1,idProducao); consumo.setInt(2,id); consumo.setBigDecimal(3,usado); consumo.executeUpdate();
                        baixa.setBigDecimal(1,depois); baixa.setInt(2,id); baixa.executeUpdate();
                        mov.setInt(1,id); mov.setNull(2,Types.INTEGER); mov.setInt(3,idProducao);
                        mov.setString(4,"Saída"); mov.setBigDecimal(5,usado); mov.setBigDecimal(6,antes);
                        mov.setBigDecimal(7,depois); mov.setString(8,"Produção"); mov.setString(9,usuario);
                        mov.setString(10,"Consumo de insumo na produção "+idProducao); mov.executeUpdate();
                    }
                    BigDecimal depoisProduto=anteriorProduto.add(BigDecimal.valueOf(quantidade));
                    try(PreparedStatement p=c.prepareStatement("UPDATE produtos SET estoque_atual=? WHERE id_produto=?")) {
                        p.setBigDecimal(1,depoisProduto); p.setInt(2,idProduto); p.executeUpdate();
                    }
                    mov.setNull(1,Types.INTEGER); mov.setInt(2,idProduto); mov.setInt(3,idProducao);
                    mov.setString(4,"Entrada"); mov.setBigDecimal(5,BigDecimal.valueOf(quantidade));
                    mov.setBigDecimal(6,anteriorProduto); mov.setBigDecimal(7,depoisProduto);
                    mov.setString(8,"Produção"); mov.setString(9,usuario);
                    mov.setString(10,"Entrada de produto acabado da produção "+idProducao); mov.executeUpdate();
                }
                c.commit();
                return idProducao;
            } catch(SQLException | RuntimeException e) {
                try { c.rollback(); } catch(SQLException ex) { e.addSuppressed(ex); }
                throw e;
            }
        }
    }

    /** Para registros antigos 'Em produção': muda só o status, sem duplicar movimentos de estoque. */
    public void concluirRegistroAntigo(int idProducao) throws SQLException {
        try(Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement(
                "UPDATE producoes SET status='Concluída' WHERE id_producao=? AND status='Em produção'")) {
            p.setInt(1,idProducao);
            if(p.executeUpdate()!=1) throw new SQLException("Produção não encontrada ou já concluída.");
        }
    }
}
