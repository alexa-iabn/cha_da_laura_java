/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cha_da_laura_java.dao;

import com.mycompany.cha_da_laura_java.conexao.ConexaoBanco;
import java.sql.*;
import java.util.*;

public class ConfiguracoesDAO {
    public static class Usuario {
        public final int id;
        public final String nome, email, perfil;
        public Usuario(int id, String nome, String email, String perfil) {
            this.id=id; this.nome=nome; this.email=email; this.perfil=perfil;
        }
        @Override public String toString() { return nome + " (ID " + id + ")"; }
    }
    public List<Usuario> listarUsuarios() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql="SELECT id,nome,email,perfil FROM usuarios WHERE situacao='Ativo' ORDER BY nome";
        try(Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while(r.next()) lista.add(new Usuario(r.getInt("id"),r.getString("nome"),r.getString("email"),r.getString("perfil")));
        }
        return lista;
    }
    public void atualizarUsuario(int id, String nome, String email) throws SQLException {
        String sql="UPDATE usuarios SET nome=?,email=? WHERE id=? AND situacao='Ativo'";
        try(Connection c=ConexaoBanco.conectar(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,nome);p.setString(2,email);p.setInt(3,id);
            if(p.executeUpdate()!=1) throw new SQLException("Usuário não encontrado ou inativo.");
        }
    }
}
