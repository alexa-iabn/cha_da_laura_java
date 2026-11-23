/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.cha_da_laura_java.dao;

import com.mycompany.cha_da_laura_java.conexao.ConexaoBanco;
import com.mycompany.cha_da_laura_java.model.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public List<Produto> listarAtivos() throws SQLException {

        List<Produto> produtos = new ArrayList<>();

        String sql = """
            SELECT id_produto, nome, categoria,
                   unidade_medida, preco, ativo
            FROM produtos
            WHERE ativo = TRUE
            ORDER BY nome
            """;

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando =
                     conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {

                Produto produto = new Produto();

                produto.setIdProduto(
                    resultado.getInt("id_produto")
                );

                produto.setNome(
                    resultado.getString("nome")
                );

                produto.setCategoria(
                    resultado.getString("categoria")
                );

                produto.setUnidadeMedida(
                    resultado.getString("unidade_medida")
                );

                produto.setPreco(
                    resultado.getBigDecimal("preco")
                );

                produto.setAtivo(
                    resultado.getBoolean("ativo")
                );

                produtos.add(produto);
            }
        }

        return produtos;
    }
    
public java.math.BigDecimal consultarEstoqueProduto(int idProduto)
        throws java.sql.SQLException {

    String sql = """
        SELECT
            COALESCE((
                SELECT SUM(quantidade)
                FROM producoes
                WHERE id_produto = ?
                  AND status = 'Concluída'
            ), 0)
            -
            COALESCE((
                SELECT SUM(quantidade)
                FROM saidas
                WHERE produto_id = ?
            ), 0) AS estoque_atual
        """;

    try (
        java.sql.Connection conexao =
            com.mycompany.cha_da_laura_java.conexao.ConexaoBanco.conectar();

        java.sql.PreparedStatement ps =
            conexao.prepareStatement(sql)
    ) {

        ps.setInt(1, idProduto);
        ps.setInt(2, idProduto);

        try (java.sql.ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("estoque_atual");
            }
        }
    }

    return java.math.BigDecimal.ZERO;
}

}

