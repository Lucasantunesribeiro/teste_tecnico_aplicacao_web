package com.solutionti.usuarios.entity;

import com.solutionti.usuarios.entity.enums.StatusUsuario;
import com.solutionti.usuarios.entity.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "usuarios",
    indexes = {
        @Index(name = "idx_usuario_cpf",    columnList = "cpf",    unique = true),
        @Index(name = "idx_usuario_tipo",   columnList = "tipo"),
        @Index(name = "idx_usuario_status", columnList = "status")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TipoUsuario tipo = TipoUsuario.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusUsuario status = StatusUsuario.ATIVO;

    @OneToMany(
        mappedBy = "usuario",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Endereco> enderecos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ---- métodos de negócio ----

    public void adicionarEndereco(Endereco endereco) {
        enderecos.add(endereco);
        endereco.setUsuario(this);
    }

    public void removerEndereco(Endereco endereco) {
        enderecos.remove(endereco);
        endereco.setUsuario(null);
    }

    public boolean isAdmin() {
        return TipoUsuario.ADMIN.equals(this.tipo);
    }

    public boolean isAtivo() {
        return StatusUsuario.ATIVO.equals(this.status);
    }
}
