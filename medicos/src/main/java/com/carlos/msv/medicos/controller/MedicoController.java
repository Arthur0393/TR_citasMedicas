package com.carlos.msv.medicos.controller;

import com.carlos.commons.controller.CrudController;
import com.carlos.commons.dto.medicos.MedicoRequest;
import com.carlos.commons.dto.medicos.MedicoResponse;
import com.carlos.msv.medicos.service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(
        name = "API Medicos",
        description = "Metodos para la gestion de medicos"
)
public class MedicoController
        extends CrudController<MedicoRequest, MedicoResponse, MedicoService> {

    public MedicoController(MedicoService service) {
        super(service);
    }

    /*
     * Obtiene únicamente los médicos cuyo estado de registro
     * sea ACTIVO.
     */
    @Override
    @Operation(
            summary = "Listar medicos",
            description = "Obtiene la lista de todos los medicos activos registrados."
    )
    public ResponseEntity<List<MedicoResponse>> listar() {
        return super.listar();
    }

    /*
     * Obtiene un médico ACTIVO mediante su identificador.
     */
    @Override
    @Operation(
            summary = "Obtener medico por ID",
            description = "Obtiene la información de un medico activo mediante su identificador."
    )
    public ResponseEntity<MedicoResponse> obtenerPorId(Long id) {
        return super.obtenerPorId(id);
    }

    /*
     * Registra un nuevo médico.
     *
     * El médico inicia automáticamente con:
     *
     * - ESTADO_REGISTRO = ACTIVO
     * - DISPONIBILIDAD = DISPONIBLE
     */
    @Override
    @Operation(
            summary = "Registrar medico",
            description = "Registra un nuevo medico. El email, telefono y cedula "
                    + "profesional deben ser unicos entre los medicos activos."
    )
    public ResponseEntity<MedicoResponse> registrar(
            MedicoRequest request
    ) {
        return super.registrar(request);
    }

    /*
     * Actualiza la información de un médico.
     *
     * Antes de realizar la modificación se debe comprobar
     * que no tenga citas CONFIRMADAS o EN_CURSO.
     */
    @Override
    @Operation(
            summary = "Actualizar medico",
            description = "Actualiza la información de un medico activo. "
                    + "No puede actualizarse si tiene una cita "
                    + "CONFIRMADA o EN_CURSO."
    )
    public ResponseEntity<MedicoResponse> actualizar(
            Long id,
            MedicoRequest request
    ) {
        return super.actualizar(id, request);
    }

    /*
     * Realiza eliminación lógica del médico.
     *
     * El registro cambia:
     *
     * ACTIVO → ELIMINADO
     *
     * No puede eliminarse si tiene una cita
     * CONFIRMADA o EN_CURSO.
     */
    @Override
    @Operation(
            summary = "Eliminar medico",
            description = "Realiza la eliminación lógica de un medico. "
                    + "No puede eliminarse si tiene una cita "
                    + "CONFIRMADA o EN_CURSO."
    )
    public ResponseEntity<Void> eliminar(Long id) {
        return super.eliminar(id);
    }

    /*
     * Endpoint utilizado para mantener la integridad histórica.
     *
     * A diferencia de GET /{id}, este método puede obtener
     * médicos ACTIVOS o ELIMINADOS.
     *
     * Es utilizado por Citas para seguir mostrando los datos
     * históricos del médico aunque posteriormente sea eliminado.
     */
    @GetMapping("/id-medico/{id}")
    @Operation(
            summary = "Obtener medico por ID sin importar el estado",
            description = "Obtiene un medico por su identificador sin importar "
                    + "si su estado de registro es ACTIVO o ELIMINADO."
    )
    public ResponseEntity<MedicoResponse> obtenerMedicoPorId(
            @PathVariable
            @Positive(message = "El ID debe ser positivo")
            Long id
    ) {

        /*
         * IMPORTANTE:
         * Aquí NO utilizamos service.obtenerPorId(),
         * porque ese método solamente consulta médicos ACTIVOS.
         */
        return ResponseEntity.ok(
                service.obtenerMedicoPorIdSinEstado(id)
        );
    }

    /*
     * Endpoint utilizado por el sistema de Citas para sincronizar
     * automáticamente la disponibilidad del médico.
     *
     * No debe considerarse un endpoint libre para modificar
     * arbitrariamente la disponibilidad.
     *
     * La capa Service será responsable de impedir que un médico
     * pase a DISPONIBLE mientras tenga una cita que lo bloquee.
     */
    @PutMapping("/{idMedico}/disponibilidad/{idDisponibilidad}")
    @Operation(
            summary = "Actualizar disponibilidad del medico",
            description = "Actualiza la disponibilidad del medico. "
                    + "Este endpoint debe ser gestionado por el sistema de Citas "
                    + "y debe respetar las reglas de integridad de las citas activas."
    )
    public ResponseEntity<Void> actualizarDisponibilidadMedico(
            @PathVariable
            @Positive(message = "El idMedico debe ser positivo")
            Long idMedico,

            @PathVariable
            @Positive(message = "El idDisponibilidad debe ser positivo")
            Long idDisponibilidad
    ) {

        service.actualizarDisponibilidadMedico(
                idMedico,
                idDisponibilidad
        );

        return ResponseEntity.noContent().build();
    }
}