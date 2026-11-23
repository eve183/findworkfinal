package com.findwork.repositories;

import com.findwork.entities.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {
    List<Resena> findByEmpresaIdEmpresa(Integer empresaId);
    List<Resena> findByUsuarioIdUsuario(Integer usuarioId);
}
