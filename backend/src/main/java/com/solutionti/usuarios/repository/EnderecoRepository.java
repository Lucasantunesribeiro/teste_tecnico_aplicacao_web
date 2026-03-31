package com.solutionti.usuarios.repository;

import com.solutionti.usuarios.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, UUID> {
    List<Endereco> findByUsuarioIdOrderByPrincipalDesc(UUID usuarioId);
    Optional<Endereco> findFirstByUsuarioIdAndPrincipalTrue(UUID usuarioId);
    Optional<Endereco> findFirstByUsuarioIdOrderByCreatedAtAsc(UUID usuarioId);
    boolean existsByUsuarioId(UUID usuarioId);
    long countByUsuarioId(UUID usuarioId);

    @Modifying
    @Query("UPDATE Endereco e SET e.principal = false WHERE e.usuario.id = :usuarioId")
    void desmarcarPrincipalPorUsuario(@Param("usuarioId") UUID usuarioId);
}
