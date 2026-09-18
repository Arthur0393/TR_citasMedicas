package com.carlos.msv.citas.service;

import com.carlos.commons.service.CrudService;
import com.carlos.msv.citas.dto.CitaRequest;
import com.carlos.msv.citas.dto.CitaResponse;

public interface CitaService
        extends CrudService<CitaRequest, CitaResponse> {

    /*
     * Actualiza únicamente el estado de una cita.
     *
     * Las transiciones permitidas son controladas
     * por EstadoCita y la Entity Cita.
     */
    void actualizarEstadoCita(
            Long idCita,
            Long idEstadoCita
    );

    /*
     * Regla utilizada al REGISTRAR o ASIGNAR una cita.
     *
     * Aquí una cita bloqueante puede estar:
     *
     * - PENDIENTE
     * - CONFIRMADA
     * - EN_CURSO
     *
     * Esto evita que un paciente tenga más de una
     * cita activa simultáneamente.
     */
    boolean pacienteTieneCitaBloqueante(
            Long idPaciente
    );

    /*
     * Regla utilizada por el microservicio PACIENTES
     * cuando quiere actualizar o eliminar un paciente.
     *
     * En este caso solamente bloquean:
     *
     * - CONFIRMADA
     * - EN_CURSO
     *
     * PENDIENTE no bloquea estas operaciones.
     */
    boolean pacienteTieneCitaBloqueanteParaModificacion(
            Long idPaciente
    );

    /*
     * Regla utilizada por el microservicio MÉDICOS
     * cuando se intenta actualizar o eliminar un médico.
     *
     * Los estados bloqueantes son:
     *
     * - CONFIRMADA
     * - EN_CURSO
     */
    boolean medicoTieneCitaBloqueanteParaModificacion(
            Long idMedico
    );

    /*
     * Regla utilizada para determinar si un médico
     * puede quedar DISPONIBLE.
     *
     * Mientras exista una cita activa en cualquiera
     * de estos estados:
     *
     * - PENDIENTE
     * - CONFIRMADA
     * - EN_CURSO
     *
     * el médico NO puede pasar a DISPONIBLE.
     */
    boolean medicoTieneCitaActivaBloqueante(
            Long idMedico
    );
}