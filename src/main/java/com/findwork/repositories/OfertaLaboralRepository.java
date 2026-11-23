package com.findwork.repositories;

import com.findwork.entities.EstadoOferta;
import com.findwork.entities.EstadoVerificacion;
import com.findwork.entities.OfertaLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OfertaLaboralRepository extends JpaRepository<OfertaLaboral, Integer> {
    List<OfertaLaboral> findByEmpresaIdEmpresa(Integer empresaId);
    List<OfertaLaboral> findByCategoriaIdCategoria(Integer categoriaId);
    List<OfertaLaboral> findByEstado(EstadoOferta estado);
    List<OfertaLaboral> findByEstadoAndEmpresa_EstadoVerificacion(EstadoOferta estado, EstadoVerificacion estadoVerificacion);
}
