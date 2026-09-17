package com.carlos.msv.citas.dto;

import com.carlos.commons.dto.medicos.DatosMedico;
import com.carlos.commons.dto.pacientes.DatosPaciente;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informacion de una cita medica registrada")
public record CitaResponse(

        @Schema(description = "Identificador unico de la cita", example = "1")
        Long id,

        @Schema(description = "Informacion del paciente asociada a la cita")
        DatosPaciente paciente,

        @Schema(description = "Informacion del medico asociada a la cita")
        DatosMedico medico,

        @Schema(description = "Fecha y hora programada para la cita", example = "25/09/2026 10:30",
        type = "String",
        format = "date-time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime fechaCita,

        @Schema(description = "Descripcion de los sintomas indicados por el paciente",
        example = "El paciente presenta dolor de cabeza intenso y fiebre desde hace dos dias")
        String sintomas,

        @Schema(description = "Estado actual de la cita", example = "Confirmada con el paciente")
        String estadoCita
) {
}
