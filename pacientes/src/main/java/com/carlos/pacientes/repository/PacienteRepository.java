package com.carlos.pacientes.repository;

import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.pacientes.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findByEstadoRegistro(EstadoRegistro estadoRegistro);

    Optional<Paciente> findByIdAndEstadoRegistro(
            Long id,
            EstadoRegistro estadoRegistro
    );

    boolean existsByEmailIgnoreCaseAndEstadoRegistro(
            String email,
            EstadoRegistro estadoRegistro
    );

    boolean existsByTelefonoAndEstadoRegistro(
            String telefono,
            EstadoRegistro estadoRegistro
    );

    boolean existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(
            String email,
            EstadoRegistro estadoRegistro,
            Long id
    );

    boolean existsByTelefonoAndEstadoRegistroAndIdNot(
            String telefono,
            EstadoRegistro estadoRegistro,
            Long id
    );
}