package com.carlos.pacientes.mapper;

import com.carlos.commons.dto.pacientes.PacienteRequest;
import com.carlos.commons.dto.pacientes.PacienteResponse;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.mapper.CommonMapper;
import com.carlos.pacientes.entity.Paciente;
import com.carlos.pacientes.repository.PacienteRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@AllArgsConstructor
@Service
@Transactional
@Slf4j

public class PacienteMapper implements CommonMapper <PacienteRequest, PacienteResponse, Paciente>{

    public Paciente requestAEntidad(PacienteRequest request){

        if (request == null) return null;

        return Paciente.builder()
                .nombre(request.nombre().trim())
                .apellidoPaterno(request.apellidoPaterno().trim())
                .apellidoMaterno(request.apellidoMaterno().trim())
                .edad(request.edad())
                .peso(request.peso())
                .estatura(request.estatura())
                .email(request.email().toLowerCase().trim())
                .telefono(request.telefono().trim())
                .direccion(request.direccion().trim())
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();
    }

    public PacienteResponse entidadAResponse(Paciente entidad){

        if (entidad == null) return null;

        return new PacienteResponse(
                entidad.getId(),
                String.join("",
                        entidad.getNombre(),
                        entidad.getApellidoPaterno(),
                        entidad.getApellidoMaterno()),
                entidad.getEdad(),
                entidad.getPeso(),
                entidad.getEstatura(),
                entidad.getImc(),
                entidad.getEmail(),
                entidad.getTelefono(),
                entidad.getDireccion(),
                entidad.getNumExpediente()
        );
    }



}
