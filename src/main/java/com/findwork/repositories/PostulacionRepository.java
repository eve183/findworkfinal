package com.findwork.repositories;

import com.findwork.entities.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Integer> {
    List<Postulacion> findByUsuarioIdUsuario(Integer usuarioId);
    List<Postulacion> findByOfertaIdOferta(Integer ofertaId);
    List<Postulacion> findByOferta_Empresa_Empleador_IdUsuario(Integer empleadorId);
}
