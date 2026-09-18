package com.carlos.msv.citas.repository;

import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.msv.citas.entity.Cita;
import com.carlos.msv.citas.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository
        extends JpaRepository<Cita, Long> {

    /*
     * Obtiene citas según su estado de registro.
     * Se utiliza principalmente para excluir registros
     * eliminados de los listados normales.
     */
    List<Cita> findByEstadoRegistro(
            EstadoRegistro estadoRegistro
    );

    /*
     * Permite comprobar si un paciente tiene una cita
     * dentro de determinados estados.
     *
     * La lista de estados cambia dependiendo
     * de la regla de negocio que se esté validando.
     */
    boolean existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
            Long idPaciente,
            EstadoRegistro estadoRegistro,
            List<EstadoCita> estados
    );

    /*
     * Permite comprobar si un médico tiene una cita
     * dentro de determinados estados.
     *
     * Se utiliza para impedir modificaciones que rompan
     * la integridad entre Médicos y Citas.
     */
    boolean existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(
            Long idMedico,
            EstadoRegistro estadoRegistro,
            List<EstadoCita> estados
    );
}