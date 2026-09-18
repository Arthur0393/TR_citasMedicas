package com.carlos.msv.citas.service;

import com.carlos.commons.clients.MedicoClient;
import com.carlos.commons.clients.PacienteClient;
import com.carlos.commons.dto.medicos.MedicoResponse;
import com.carlos.commons.dto.pacientes.PacienteResponse;
import com.carlos.commons.enums.DisponibilidadMedico;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.exceptions.RecursoNoEncontradoException;
import com.carlos.msv.citas.dto.CitaRequest;
import com.carlos.msv.citas.dto.CitaResponse;
import com.carlos.msv.citas.entity.Cita;
import com.carlos.msv.citas.enums.EstadoCita;
import com.carlos.msv.citas.mapper.CitaMapper;
import com.carlos.msv.citas.repository.CitaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final CitaMapper citaMapper;
    private final MedicoClient medicoClient;
    private final PacienteClient pacienteClient;

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {

        log.info("Listando todas las citas activas");

        return citaRepository
                .findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(cita -> citaMapper.entidadAResponse(
                        cita,
                        obtenerPacienteSinEstado(cita.getIdPaciente()),
                        obtenerMedicoSinEstado(cita.getIdMedico())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long id) {

        Cita cita = obtenerCitaOException(id);

        return citaMapper.entidadAResponse(
                cita,
                obtenerPacienteSinEstado(cita.getIdPaciente()),
                obtenerMedicoSinEstado(cita.getIdMedico())
        );
    }

    @Override
    public CitaResponse registrar(CitaRequest request) {

        log.info("Registrando nueva cita");

        PacienteResponse paciente =
                obtenerPacienteActivo(request.idPaciente());

        validarPacienteSinCitaBloqueante(
                request.idPaciente()
        );

        MedicoResponse medico =
                obtenerMedicoActivo(request.idMedico());

        validarMedicoActivoDisponible(medico);

        Cita cita =
                citaMapper.requestAEntidad(request);

        citaRepository.save(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(
                medico.id(),
                cita.getEstadoCita()
        );

        log.info("Cita registrada exitosamente");

        return citaMapper.entidadAResponse(
                cita,
                paciente,
                medico
        );
    }

    @Override
    public CitaResponse actualizar(
            CitaRequest request,
            Long id
    ) {

        log.info("Actualizando cita con id: {}", id);

        Cita cita = obtenerCitaOException(id);

        Long idPacienteAnterior = cita.getIdPaciente();
        Long idMedicoAnterior = cita.getIdMedico();

        boolean cambioPaciente =
                !Objects.equals(
                        idPacienteAnterior,
                        request.idPaciente()
                );

        boolean cambioMedico =
                !Objects.equals(
                        idMedicoAnterior,
                        request.idMedico()
                );

        PacienteResponse paciente;

        if (cambioPaciente) {

            log.info(
                    "La cita {} cambiara de paciente",
                    id
            );

            paciente =
                    obtenerPacienteActivo(request.idPaciente());

            validarPacienteSinCitaBloqueante(
                    request.idPaciente()
            );

        } else {

            paciente =
                    obtenerPacienteSinEstado(
                            request.idPaciente()
                    );
        }

        MedicoResponse medico;

        if (cambioMedico) {

            log.info(
                    "La cita {} cambiara de medico",
                    id
            );

            medico =
                    obtenerMedicoActivo(
                            request.idMedico()
                    );

            validarMedicoActivoDisponible(medico);

        } else {

            medico =
                    obtenerMedicoSinEstado(
                            request.idMedico()
                    );
        }

        cita.actualizar(
                request.idPaciente(),
                request.idMedico(),
                request.fechaCita(),
                request.sintomas()
        );

        citaRepository.save(cita);

        if (cambioMedico) {

            actualizarDisponibilidadMedico(
                    request.idMedico(),
                    DisponibilidadMedico.NO_DISPONIBLE
                            .getCodigo()
            );

            liberarMedicoSiNoTieneCitasBloqueantes(
                    idMedicoAnterior
            );
        }

        log.info(
                "Cita con id {} actualizada correctamente",
                id
        );

        return citaMapper.entidadAResponse(
                cita,
                paciente,
                medico
        );
    }

    @Override
    public void actualizarEstadoCita(
            Long idCita,
            Long idEstadoCita
    ) {

        Cita cita =
                obtenerCitaOException(idCita);

        log.info(
                "Actualizando estado de la cita con id: {}",
                idCita
        );

        EstadoCita nuevoEstado =
                EstadoCita.obtenerEstadoCitaPorCodigo(
                        idEstadoCita
                );

        cita.actualizarEstadoCita(
                nuevoEstado
        );

        citaRepository.save(cita);

        if (nuevoEstado == EstadoCita.CANCELADA
                || nuevoEstado == EstadoCita.FINALIZADA) {

            liberarMedicoSiNoTieneCitasBloqueantes(
                    cita.getIdMedico()
            );

        } else {

            cambiarDisponibilidadMedicoSegunEstadoCita(
                    cita.getIdMedico(),
                    nuevoEstado
            );
        }

        log.info(
                "Estado de la cita {} actualizado correctamente",
                idCita
        );
    }

    @Override
    public void eliminar(Long id) {

        Cita cita =
                obtenerCitaOException(id);

        log.info(
                "Eliminando cita con id: {}",
                id
        );

        cita.eliminar();

        citaRepository.save(cita);

        liberarMedicoSiNoTieneCitasBloqueantes(
                cita.getIdMedico()
        );

        log.info(
                "Cita con id {} ha sido marcada como eliminada",
                id
        );
    }

    private Cita obtenerCitaOException(Long id) {

        log.info(
                "Buscando cita con id: {}",
                id
        );

        return citaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cita no encontrada con id: " + id
                        )
                );
    }

    private PacienteResponse obtenerPacienteActivo(
            Long id
    ) {

        log.info(
                "Buscando paciente activo con id {} en el servicio remoto",
                id
        );

        return pacienteClient
                .obtenerPacienteActivoPorId(id);
    }

    private PacienteResponse obtenerPacienteSinEstado(
            Long id
    ) {

        log.info(
                "Buscando paciente sin importar estado con id {}",
                id
        );

        return pacienteClient
                .obtenerPacienteSinEstadoPorId(id);
    }

    private MedicoResponse obtenerMedicoActivo(
            Long id
    ) {

        log.info(
                "Buscando medico activo con id {} en el servicio remoto",
                id
        );

        return medicoClient
                .obtenerMedicoActivoPorId(id);
    }

    private MedicoResponse obtenerMedicoSinEstado(
            Long id
    ) {

        log.info(
                "Buscando medico sin importar estado con id {}",
                id
        );

        return medicoClient
                .obtenerMedicoSinEstadoPorId(id);
    }

    private void validarMedicoActivoDisponible(
            MedicoResponse medico
    ) {

        log.info(
                "Validando disponibilidad del medico"
        );

        if (!DisponibilidadMedico.DISPONIBLE
                .getCodigo()
                .equals(medico.idDisponibilidad())) {

            throw new IllegalStateException(
                    "El medico activo no esta disponible para consultar"
            );
        }
    }

    private void validarPacienteSinCitaBloqueante(
            Long idPaciente
    ) {

        if (pacienteTieneCitaBloqueante(idPaciente)) {

            throw new IllegalStateException(
                    "El paciente ya tiene una cita PENDIENTE, CONFIRMADA o EN_CURSO"
            );
        }
    }

    private void actualizarDisponibilidadMedico(
            Long idMedico,
            Long idDisponibilidad
    ) {

        log.info(
                "Actualizando disponibilidad del medico {}",
                idMedico
        );

        medicoClient.actualizarDisponibilidadMedico(
                idMedico,
                idDisponibilidad
        );
    }

    private void cambiarDisponibilidadMedicoSegunEstadoCita(
            Long idMedico,
            EstadoCita estadoCita
    ) {

        switch (estadoCita) {

            case PENDIENTE, CONFIRMADA ->
                    actualizarDisponibilidadMedico(
                            idMedico,
                            DisponibilidadMedico
                                    .NO_DISPONIBLE
                                    .getCodigo()
                    );

            case EN_CURSO ->
                    actualizarDisponibilidadMedico(
                            idMedico,
                            DisponibilidadMedico
                                    .EN_CONSULTA
                                    .getCodigo()
                    );

            case FINALIZADA, CANCELADA ->
                    liberarMedicoSiNoTieneCitasBloqueantes(
                            idMedico
                    );
        }
    }

    private void liberarMedicoSiNoTieneCitasBloqueantes(
            Long idMedico
    ) {

        log.info(
                "Validando si el medico {} puede quedar disponible",
                idMedico
        );

        boolean tieneCitasBloqueantes =
                citaRepository
                        .existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(
                                idMedico,
                                EstadoRegistro.ACTIVO,
                                estadosBloqueantes()
                        );

        if (!tieneCitasBloqueantes) {

            actualizarDisponibilidadMedico(
                    idMedico,
                    DisponibilidadMedico
                            .DISPONIBLE
                            .getCodigo()
            );
        }
    }

    private List<EstadoCita> estadosBloqueantes() {

        return List.of(
                EstadoCita.PENDIENTE,
                EstadoCita.CONFIRMADA,
                EstadoCita.EN_CURSO
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean pacienteTieneCitaBloqueante(
            Long idPaciente
    ) {

        log.info(
                "Validando citas bloqueantes del paciente {}",
                idPaciente
        );

        return citaRepository
                .existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
                        idPaciente,
                        EstadoRegistro.ACTIVO,
                        estadosBloqueantes()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean pacienteTieneCitaBloqueanteParaModificacion(
            Long idPaciente
    ) {

        log.info(
                "Validando si el paciente {} tiene citas "
                        + "CONFIRMADAS o EN_CURSO",
                idPaciente
        );

        /*
         * Esta validación NO es la misma que utilizamos
         * para registrar una cita.
         *
         * Según las reglas del módulo PACIENTES,
         * un paciente solamente queda bloqueado para
         * PUT o DELETE cuando tiene una cita:
         *
         * - CONFIRMADA
         * - EN_CURSO
         *
         * Una cita PENDIENTE no bloquea estas operaciones.
         */
        return citaRepository
                .existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
                        idPaciente,
                        EstadoRegistro.ACTIVO,
                        List.of(
                                EstadoCita.CONFIRMADA,
                                EstadoCita.EN_CURSO
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean medicoTieneCitaBloqueanteParaModificacion(
            Long idMedico
    ) {

        log.info(
                "Validando si el medico {} tiene citas CONFIRMADAS o EN_CURSO",
                idMedico
        );

        /*
         * Regla utilizada por el microservicio MÉDICOS.
         *
         * Un médico no puede modificarse, eliminarse
         * o cambiar manualmente su disponibilidad cuando
         * tiene una cita:
         *
         * - CONFIRMADA
         * - EN_CURSO
         *
         * Una cita PENDIENTE no forma parte de esta
         * validación específica.
         */
        return citaRepository
                .existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(
                        idMedico,
                        EstadoRegistro.ACTIVO,
                        List.of(
                                EstadoCita.CONFIRMADA,
                                EstadoCita.EN_CURSO
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean medicoTieneCitaActivaBloqueante(
            Long idMedico
    ) {

        log.info(
                "Validando si el medico {} tiene citas "
                        + "PENDIENTES, CONFIRMADAS o EN_CURSO",
                idMedico
        );

        /*
         * Esta validación se utiliza específicamente
         * antes de permitir que un médico pase a DISPONIBLE.
         *
         * Mientras exista una cita activa:
         *
         * - PENDIENTE
         * - CONFIRMADA
         * - EN_CURSO
         *
         * el médico debe conservar la disponibilidad
         * correspondiente al estado de su cita.
         */
        return citaRepository
                .existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(
                        idMedico,
                        EstadoRegistro.ACTIVO,
                        estadosBloqueantes()
                );
    }
}