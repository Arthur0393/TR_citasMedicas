package com.carlos.commons.dto.medicos;

import io.swagger.v3.oas.annotations.media.Schema;

public record MedicoResponse(

        @Schema(
                description = "Identificador unico del medico",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Nombre completo del medico",
                example = "Carlos"
        )
        String nombre,

        @Schema(
                description = "Edad del medico",
                example = "30"
        )
        Short edad,

        @Schema(
                description = "Correo electrónico del medico",
                example = "carlos.romero@dominio.com"
        )
        String email,

        @Schema(
                description = "Numero telefónico del medico",
                example = "2461234567"
        )
        String telefono,

        @Schema(
                description = "Cedula profesional del medico",
                example = "123456789012"
        )
        String cedulaProfesional,

        @Schema(
                description = "Nombre de la especialidad del medico",
                example = "Cardiologia"
        )
        String especialidad,

        @Schema(
                description = "Disponibilidad del medico",
                example = "Lunes a Viernes de 08:00 a 16:00"
        )
        String disponibilidad,

        @Schema(
                description = "Identificador de la disponibilidad asociada al medico",
                example = "1"
        )
        Long idDisponibilidad
) {
}