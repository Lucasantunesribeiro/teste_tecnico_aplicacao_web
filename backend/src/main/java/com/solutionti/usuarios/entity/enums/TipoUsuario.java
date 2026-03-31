package com.solutionti.usuarios.entity.enums;

public enum TipoUsuario {
    ADMIN("Administrador"),
    USER("Usuário Comum");

    private final String descricao;

    TipoUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
