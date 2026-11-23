/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.cha_da_laura_java.dao;

import com.mycompany.cha_da_laura_java.conexao.ConexaoBanco;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InsumoDAO {

    public static class InsumoReceita {

        private final int idInsumo;
        private final String nome;
        private final String unidadeMedida;
        private final BigDecimal quantidadePorReceita;
        private final BigDecimal estoqueAtual;

        public InsumoReceita(
                int idInsumo,
                String nome,
                String unidadeMedida,
                BigDecimal quantidadePorReceita,
                BigDecimal estoqueAtual) {

            this.idInsumo = idInsumo;
            this.nome = nome;
            this.unidadeMedida = unidadeMedida;
            this.quantidadePorReceita = quantidadePorReceita;
            this.estoqueAtual = estoqueAtual;
        }

        public int getIdInsumo() {
            return idInsumo;
        }

        public String getNome() {
            return nome;
        }

        public String getUnidadeMedida() {
            return unidadeMedida;
        }

        public BigDecimal getQuantidadePorReceita() {
            return quantidadePorReceita;
        }

        public BigDecimal getEstoqueAtual() {
            return estoqueAtual;
        }
    }

    public List<InsumoReceita> listarPorReceita(int idReceita)
            throws SQLException {

        List<InsumoReceita> insumos = new ArrayList<>();

        String sql = """
            SELECT i.id_insumo, i.nome, i.unidade_medida,
                   i.estoque_atual, ri.quantidade
            FROM receita_insumos ri
            INNER JOIN insumos i
                ON i.id_insumo = ri.id_insumo
            WHERE ri.id_receita = ?
              AND i.ativo = TRUE
            ORDER BY i.nome
            """;

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando =
                     conexao.prepareStatement(sql)) {

            comando.setInt(1, idReceita);

            try (ResultSet resultado = comando.executeQuery()) {

                while (resultado.next()) {

                    InsumoReceita insumo = new InsumoReceita(
                        resultado.getInt("id_insumo"),
                        resultado.getString("nome"),
                        resultado.getString("unidade_medida"),
                        resultado.getBigDecimal("quantidade"),
                        resultado.getBigDecimal("estoque_atual")
                    );

                    insumos.add(insumo);
                }
            }
        }

        return insumos;
    }
}
