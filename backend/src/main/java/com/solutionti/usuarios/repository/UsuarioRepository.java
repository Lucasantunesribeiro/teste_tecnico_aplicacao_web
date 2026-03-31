package com.solutionti.usuarios.repository;

import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.entity.enums.StatusUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    Optional<Usuario> findByCpfAndStatus(String cpf, StatusUsuario status);
}
