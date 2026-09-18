package com.carlos.pacientes.controller;

import com.carlos.commons.controller.CrudController;
import com.carlos.commons.dto.pacientes.PacienteRequest;
import com.carlos.commons.dto.pacientes.PacienteResponse;
import com.carlos.pacientes.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

// Ruta base para todos los endpoints relacionados con pacientes.
@RequestMapping("/api/pacientes")

@Tag(
        name = "API Pacientes",
        description = "Metodos para la gestion de pacientes"
)
public class PacienteController
        extends CrudController<PacienteRequest, PacienteResponse, PacienteService> {

    public PacienteController(PacienteService service) {
        super(service);
    }

    /*
     * Endpoint especial utilizado para obtener un paciente
     * sin importar si su registro está ACTIVO o ELIMINADO.
     *
     * Es utilizado principalmente por Citas para mantener
     * la integridad histórica.
     */
    @GetMapping("/id-paciente/{id}")
    @Operation(
            summary = "Obtener paciente por id sin importar el estado del registro"
    )
    public ResponseEntity<PacienteResponse> obtenerPacientePorId(
            @PathVariable
            @Positive(message = "El ID debe ser positivo")
            Long id
    ) {

        return ResponseEntity.ok(
                service.obtenerPacientePorIdSinEstado(id)
        );
    }
}