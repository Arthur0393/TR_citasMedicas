package com.carlos.commons.dto.pacientes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record PacienteRequest(

        @Schema(
                description = "Nombre del paciente",
                example = "Arturo")
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 1, max = 50, message = "El nombre tiene que tener entre 1 y 50")
        String nombre,

        @Schema(
                description = "Apellido paterno del medico",
                example = "Alcantara")
        @NotBlank(message = "El apellido paterno es requerido")
        @Size(min = 1, max = 50, message = "El apellido paterno debe tener entre 1 y 50 caracteres")
        String apellidoPaterno,

        @Schema(
                description = "Apellido materno del medico",
                example = "Alcantara"
        )
        @NotBlank(message = "El apellido materno es requerido")
        @Size(min = 1, max = 50, message = "El apellido materno debe tener entre 1 y 50 caracteres")
        String apellidoMaterno,

        @Schema(
                description = "La edad del paciente",
                example = "24",
                minimum = "1",
                maximum = "100"
        )
        @NotNull(message = "La edad es requerida")
        @Min(value = 1, message = "La edad minima es de 1 año")
        @Max(value = 100, message = "La edad maxima es de 100 años")
        Short edad,

        @Schema(
                description = "El peso del paciente",
                example = "70.5"
        )
        @NotNull(message = "El peso del paciente es requerido")
        @DecimalMin(value = "0.1", message = "El peso minimo es de 0.1 kilogramos")
        @DecimalMax(value = "200.0", message = "El peso maximo es de 200.0 kilogramos")
        Double peso,

        @Schema(
                description = "La estatura del paciente",
                example = "1.76"
        )
        @NotNull(message = "La estatura del paciente es requerida")
        @DecimalMin(value = "1.0", message = "La estatura minima es de 1.0 metros")
        @DecimalMax(value = "2.0", message = "La estatura maxima es de 2.0 metros")
        Double estatura,

        /*
        @Schema(
                description = "El Indice de Masa Corporal del paciente (IMC)",
                example = "22.2 Peso Normal"
        )
        Double IMC,
         */

        @NotBlank(message = "El email es requerido")
        @Size(
                max = 100,
                message = "El email debe tener maximo 100 caracteres")
        @Email(message = "El email debe tener el formato correcto (correo@dominio)")
        String email,

        /*
        @Schema(
                description = "Numero de expediente del paciente, debe contener exactamente 20 caracteres",
                example = "X1X2X3X4X5X6X7X8X9X0"
        )
        @NotBlank(message = "El Numero de Expediente es requerido")
        String numExpediente,
         */

        @NotBlank(message = "El telefono es requerido")
        @Schema(
                description = "Numero telefonico del paciente, debe contener exactamente 10 digitos",
                example = "2461234567",
                pattern = "^[0-9]{10}$"
        )
        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "El telefono debe contener exactamente 10 digitos"
        )
        String telefono,

        @Schema(
                description = "Direccion del paciente",
                example = "Av. Reforma 123, Col. Centro, C.P. 72000, Puebla, Puebla, México")
        @NotBlank(message = "La direccion es  requerida")
        @Size(min = 1, max = 150, message = "La direccion debe tener entre 1 y 150")
        String direccion
) {}