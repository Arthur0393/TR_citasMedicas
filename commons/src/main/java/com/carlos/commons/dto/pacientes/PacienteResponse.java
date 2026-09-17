package com.carlos.commons.dto.pacientes;

import io.swagger.v3.oas.annotations.media.Schema;

public record PacienteResponse(

        @Schema(
                description = "Identificador único del paciente",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Nombre completo del paciente",
                example = "Charly Habara"
        )
        String nombre,

        @Schema(
                description = "Edad del paciente",
                example = "24"
        )
        Short edad,

        @Schema(
                description = "Peso del paciente",
                example = "70.28"
        )
        Double peso,

        @Schema(
                description = "Estatura del paciente",
                example = "1.73"
        )
        Double estatura,

        @Schema(
                description = "Indice de Masa Corporal del paciente (IMC)",
                example=  "24.653061224489797"
        )
        Double IMC,

        @Schema(
                description = "Correo electrónico del paciente",
                example = "charly.habara@dominio.com"
        )
        String email,

        @Schema(
                description = "Numero telefónico del paciente",
                example = "1234567890"
        )
        String telefono,

        @Schema(
                description = "Direccion del paciente",
                example = "Av. Reforma 123, Colonia Centro, Ciudad de México"
        )
        String direccion,

        @Schema(
                description = "Numero de expediente del paciente",
                example = "1X2X3X4X5X6X7X8X9X0"
        )
        String numExpediente
) {}