package com.findwork.repositories;

import com.findwork.entities.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolUsuarioRepository extends JpaRepository<RolUsuario, Integer> {
    Optional<RolUsuario> findByNombre(String nombre);
}
