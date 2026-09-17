package com.carlos.msv.citas.repository;

import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.msv.citas.entity.Cita;
import com.carlos.msv.citas.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository <Cita, Long>{

    List<Cita> findByEstadoRegistro(EstadoRegistro estadoRegistro);

    boolean existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
            Long idPaciente,
            EstadoRegistro estadoRegistro,
            List<EstadoCita> estados
    );


}
