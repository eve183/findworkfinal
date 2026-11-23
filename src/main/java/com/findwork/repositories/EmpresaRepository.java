package com.findwork.repositories;

import com.findwork.entities.Empresa;
import com.findwork.entities.EstadoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    List<Empresa> findByEmpleadorIdUsuario(Integer empleadorId);
    List<Empresa> findByEstadoVerificacion(EstadoVerificacion estadoVerificacion);
}
