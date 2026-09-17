package com.carlos.msv.citas.controller;

import com.carlos.commons.controller.CrudController;
import com.carlos.msv.citas.dto.CitaRequest;
import com.carlos.msv.citas.dto.CitaResponse;
import com.carlos.msv.citas.entity.Cita;
import com.carlos.msv.citas.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "API Citas", description = "Metodos para la gestion de citas")
public class CitaController extends CrudController<CitaRequest, CitaResponse, CitaService> {

    public CitaController(CitaService service) {
        super(service);
    }

    @Operation(
            summary = "Actualizar estado de la cita",
            description = "Actualizar el estado de una cita utilizado " +
                    "el identificador de la cita y el identificador del nuevo estado."
    )
    @PatchMapping("/{idCita}/estado/{idEstado}")
    public ResponseEntity<Void> actualizarEstadoCita(
            @PathVariable @Positive(message = "El idCita debe ser positivo") Long idCita,
            @PathVariable @Positive(message = "El idEstado debe ser positivo") Long idEstado){

        service.actualizarEstadoCita(idCita, idEstado);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Validar citas bloqueantes de un paciente",
            description = "Valida si un paciente tiene una cita en estado CONFIRMADA o EN_CURSO."
    )
    @GetMapping("/paciente/{idPaciente}/bloqueante")
    public ResponseEntity<Boolean> pacienteTieneCitaBloqueante(
            @PathVariable @Positive(message = "El Paciente debe ser estar en estado positivo") Long idPaciente
    ) {

        return ResponseEntity.ok(
                service.pacienteTieneCitaBloqueante(idPaciente)
        );
    }
}

