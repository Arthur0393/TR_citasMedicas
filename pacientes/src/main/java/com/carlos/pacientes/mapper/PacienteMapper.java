package com.carlos.pacientes.mapper;

import com.carlos.commons.dto.pacientes.PacienteRequest;
import com.carlos.commons.dto.pacientes.PacienteResponse;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.mapper.CommonMapper;
import com.carlos.pacientes.entity.Paciente;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper
        implements CommonMapper<PacienteRequest, PacienteResponse, Paciente> {

    @Override
    public Paciente requestAEntidad(PacienteRequest request) {

        // Si el request es nulo, no se puede construir una entidad.
        if (request == null) {
            return null;
        }

        /*
         * El IMC es un dato calculado por el sistema.
         * No debe venir desde el Request porque el usuario no lo captura.
         *
         * Fórmula:
         * IMC = peso / estatura²
         */
        Double imc = calcularImc(
                request.peso(),
                request.estatura()
        );

        /*
         * El número de expediente también es generado automáticamente.
         *
         * Ejemplo:
         * telefono = 5512345678
         * expediente = 5X5X1X2X3X4X5X6X7X8X
         */
        String numExpediente =
                generarNumeroExpediente(request.telefono());

        /*
         * El Mapper transforma el PacienteRequest en una entidad Paciente.
         *
         * El estado inicial siempre será ACTIVO.
         */
        return Paciente.builder()
                .nombre(request.nombre().trim())
                .apellidoPaterno(request.apellidoPaterno().trim())
                .apellidoMaterno(request.apellidoMaterno().trim())
                .edad(request.edad())
                .peso(request.peso())
                .estatura(request.estatura())
                .imc(imc)
                .email(request.email().trim().toLowerCase())
                .numExpediente(numExpediente)
                .telefono(request.telefono().trim())
                .direccion(request.direccion().trim())
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();
    }

    @Override
    public PacienteResponse entidadAResponse(Paciente paciente) {

        // Evita intentar mapear una entidad inexistente.
        if (paciente == null) {
            return null;
        }

        /*
         * El Response solicita el nombre completo en un único campo.
         * Por eso concatenamos nombre + apellidos aquí y no en el Service.
         */
        String nombreCompleto = String.join(
                " ",
                paciente.getNombre(),
                paciente.getApellidoPaterno(),
                paciente.getApellidoMaterno()
        );

        // Transformamos la entidad al DTO que será enviado al cliente.
        return new PacienteResponse(
                paciente.getId(),
                nombreCompleto,
                paciente.getEdad(),
                paciente.getPeso(),
                paciente.getEstatura(),
                paciente.getImc(),
                paciente.getEmail(),
                paciente.getTelefono(),
                paciente.getDireccion(),
                paciente.getNumExpediente()
        );
    }

    public void actualizarEntidad(
            Paciente paciente,
            PacienteRequest request
    ) {

        /*
         * Si cambia el peso o la estatura durante un PUT,
         * el IMC debe volver a calcularse automáticamente.
         */
        Double imc = calcularImc(
                request.peso(),
                request.estatura()
        );

        /*
         * El expediente depende del teléfono.
         * Si el teléfono cambia durante el PUT,
         * también se genera nuevamente el expediente.
         */
        String numExpediente =
                generarNumeroExpediente(request.telefono());

        /*
         * El Mapper prepara los datos derivados y delega a la Entity
         * la modificación de su estado interno.
         */
        paciente.actualizar(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.peso(),
                request.estatura(),
                imc,
                request.email(),
                request.telefono(),
                request.direccion(),
                numExpediente
        );
    }

    private Double calcularImc(
            Double peso,
            Double estatura
    ) {

        /*
         * Fórmula establecida por los requerimientos:
         *
         * IMC = peso / estatura²
         */
        return peso / (estatura * estatura);
    }

    private String generarNumeroExpediente(String telefono) {

        /*
         * Se divide el teléfono dígito por dígito y se agrega "X"
         * después de cada número.
         *
         * 5512345678
         * ↓
         * 5X5X1X2X3X4X5X6X7X8X
         */
        return String.join("X", telefono.split("")) + "X";
    }
}