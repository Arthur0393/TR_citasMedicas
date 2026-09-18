package com.carlos.msv.citas.controller;

import com.carlos.commons.controller.CrudController;
import com.carlos.msv.citas.dto.CitaRequest;
import com.carlos.msv.citas.dto.CitaResponse;
import com.carlos.msv.citas.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@Tag(
        name = "Citas",
        description = "Metodos para la gestión de citas médicas"
)
public class CitaController
        extends CrudController<CitaRequest, CitaResponse, CitaService> {

    public CitaController(CitaService service) {
        super(service);
    }

    /*
     * Endpoint heredado del CrudController.
     *
     * Obtiene todas las citas registradas y renderiza
     * la información histórica del paciente y médico asociados.
     */
    @Override
    @Operation(
            summary = "Listar citas",
            description = "Obtiene la lista de todas las citas registradas."
    )
    public ResponseEntity<List<CitaResponse>> listar() {
        return super.listar();
    }

    /*
     * Endpoint heredado del CrudController.
     *
     * Obtiene una cita mediante su identificador.
     */
    @Override
    @Operation(
            summary = "Obtener cita por ID",
            description = "Obtiene la información de una cita mediante su identificador."
    )
    public ResponseEntity<CitaResponse> obtenerPorId(Long id) {
        return super.obtenerPorId(id);
    }

    /*
     * Registra una nueva cita.
     *
     * Durante el registro se validan las reglas de negocio:
     *
     * - Paciente existente y ACTIVO.
     * - Paciente sin otra cita PENDIENTE, CONFIRMADA o EN_CURSO.
     * - Médico existente y ACTIVO.
     * - Médico DISPONIBLE.
     * - Fecha presente o futura.
     * - Síntomas válidos.
     */
    @Override
    @Operation(
            summary = "Registrar cita",
            description = "Registra una nueva cita médica. "
                    + "El paciente no puede tener otra cita PENDIENTE, "
                    + "CONFIRMADA o EN_CURSO y el médico debe estar DISPONIBLE."
    )
    public ResponseEntity<CitaResponse> registrar(
            CitaRequest request
    ) {
        return super.registrar(request);
    }

    /*
     * Actualiza los datos generales de una cita.
     *
     * Solamente las citas PENDIENTES o CONFIRMADAS
     * pueden modificarse mediante PUT.
     */
    @Override
    @Operation(
            summary = "Actualizar cita",
            description = "Actualiza la información de una cita existente. "
                    + "La actualización únicamente está permitida cuando la cita "
                    + "se encuentra en estado PENDIENTE o CONFIRMADA."
    )
    public ResponseEntity<CitaResponse> actualizar(
            Long id,
            CitaRequest request
    ) {
        return super.actualizar(id, request);
    }

    /*
     * Cambia exclusivamente el estado de una cita.
     *
     * Las transiciones permitidas son controladas
     * por la máquina de estados EstadoCita.
     *
     * Además, cualquier cambio de estado actualiza
     * automáticamente la disponibilidad del médico.
     */
    @Operation(
            summary = "Actualizar estado de la cita",
            description = "Actualiza el estado de una cita respetando las "
                    + "transiciones permitidas y actualiza automáticamente "
                    + "la disponibilidad del médico asociado."
    )
    @PatchMapping("/{idCita}/estado/{idEstado}")
    public ResponseEntity<Void> actualizarEstadoCita(
            @PathVariable Long idCita,
            @PathVariable Long idEstado
    ) {

        service.actualizarEstadoCita(
                idCita,
                idEstado
        );

        return ResponseEntity.noContent().build();
    }

    /*
     * Realiza únicamente eliminación lógica.
     *
     * Una cita solamente puede eliminarse si se encuentra:
     *
     * - PENDIENTE
     * - CANCELADA
     * - FINALIZADA
     */
    @Override
    @Operation(
            summary = "Eliminar cita",
            description = "Realiza la eliminación lógica de una cita. "
                    + "Solo puede eliminarse cuando se encuentra en estado "
                    + "PENDIENTE, CANCELADA o FINALIZADA."
    )
    public ResponseEntity<Void> eliminar(Long id) {
        return super.eliminar(id);
    }

    /*
     * VALIDACIÓN PARA CREAR/ASIGNAR CITAS.
     *
     * Este endpoint comprueba si el paciente ya tiene
     * una cita activa que impida registrar otra.
     *
     * Estados bloqueantes:
     *
     * - PENDIENTE
     * - CONFIRMADA
     * - EN_CURSO
     */
    @Operation(
            summary = "Validar citas bloqueantes para registrar una cita",
            description = "Valida si un paciente tiene una cita activa "
                    + "en estado PENDIENTE, CONFIRMADA o EN_CURSO."
    )
    @GetMapping("/paciente/{idPaciente}/bloqueante")
    public ResponseEntity<Boolean> pacienteTieneCitaBloqueante(
            @PathVariable Long idPaciente
    ) {

        return ResponseEntity.ok(
                service.pacienteTieneCitaBloqueante(
                        idPaciente
                )
        );
    }

    /*
     * VALIDACIÓN PARA MODIFICAR/ELIMINAR PACIENTES.
     *
     * Este endpoint es utilizado por Pacientes-MSV
     * mediante Feign.
     *
     * IMPORTANTE:
     * Para PUT/DELETE de un paciente solamente bloquean:
     *
     * - CONFIRMADA
     * - EN_CURSO
     *
     * Una cita PENDIENTE NO bloquea estas operaciones.
     */
    @Operation(
            summary = "Validar citas bloqueantes para modificar un paciente",
            description = "Valida si el paciente tiene una cita en estado "
                    + "CONFIRMADA o EN_CURSO que impida su actualización "
                    + "o eliminación lógica."
    )
    @GetMapping("/paciente/{idPaciente}/bloqueante-modificacion")
    public ResponseEntity<Boolean>
    pacienteTieneCitaBloqueanteParaModificacion(
            @PathVariable Long idPaciente
    ) {

        return ResponseEntity.ok(
                service.pacienteTieneCitaBloqueanteParaModificacion(
                        idPaciente
                )
        );
    }

    /*
     * VALIDACIÓN PARA MODIFICAR/ELIMINAR MÉDICOS.
     *
     * Este endpoint es utilizado por Medicos-MSV
     * mediante Feign.
     *
     * Para actualizar o eliminar un médico solamente
     * se consideran bloqueantes:
     *
     * - CONFIRMADA
     * - EN_CURSO
     */
    @Operation(
            summary = "Validar citas bloqueantes para modificar un medico",
            description = "Valida si el medico tiene una cita en estado "
                    + "CONFIRMADA o EN_CURSO que impida su actualización "
                    + "o eliminación lógica."
    )
    @GetMapping("/medico/{idMedico}/bloqueante-modificacion")
    public ResponseEntity<Boolean>
    medicoTieneCitaBloqueanteParaModificacion(
            @PathVariable Long idMedico
    ) {

        return ResponseEntity.ok(
                service.medicoTieneCitaBloqueanteParaModificacion(
                        idMedico
                )
        );
    }

    /*
     * VALIDACIÓN PARA DISPONIBILIDAD DEL MÉDICO.
     *
     * Este endpoint permite determinar si un médico
     * puede pasar al estado DISPONIBLE.
     *
     * Mientras exista una cita:
     *
     * - PENDIENTE
     * - CONFIRMADA
     * - EN_CURSO
     *
     * el médico NO puede quedar DISPONIBLE.
     */
    @Operation(
            summary = "Validar citas activas de un medico",
            description = "Valida si el medico tiene una cita activa en estado "
                    + "PENDIENTE, CONFIRMADA o EN_CURSO que impida cambiar "
                    + "su disponibilidad a DISPONIBLE."
    )
    @GetMapping("/medico/{idMedico}/bloqueante-disponibilidad")
    public ResponseEntity<Boolean>
    medicoTieneCitaActivaBloqueante(
            @PathVariable Long idMedico
    ) {

        return ResponseEntity.ok(
                service.medicoTieneCitaActivaBloqueante(
                        idMedico
                )
        );
    }
}