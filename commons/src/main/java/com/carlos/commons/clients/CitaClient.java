package com.carlos.commons.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "citas")
public interface CitaClient {

    /*
     * Esta consulta es utilizada por Pacientes-MSV
     * antes de actualizar o eliminar un paciente.
     *
     * Estados bloqueantes:
     *
     * - CONFIRMADA
     * - EN_CURSO
     */
    @GetMapping("/paciente/{idPaciente}/bloqueante-modificacion")
    boolean pacienteTieneCitaBloqueanteParaModificacion(
            @PathVariable Long idPaciente
    );

    /*
     * Esta consulta es utilizada por Medicos-MSV
     * antes de actualizar o eliminar un médico.
     *
     * Estados bloqueantes:
     *
     * - CONFIRMADA
     * - EN_CURSO
     */
    @GetMapping("/medico/{idMedico}/bloqueante-modificacion")
    boolean medicoTieneCitaBloqueanteParaModificacion(
            @PathVariable Long idMedico
    );

    /*
     * Esta consulta es utilizada antes de intentar
     * colocar a un médico como DISPONIBLE.
     *
     * Estados bloqueantes:
     *
     * - PENDIENTE
     * - CONFIRMADA
     * - EN_CURSO
     */
    @GetMapping("/medico/{idMedico}/bloqueante-disponibilidad")
    boolean medicoTieneCitaActivaBloqueante(
            @PathVariable Long idMedico
    );
}