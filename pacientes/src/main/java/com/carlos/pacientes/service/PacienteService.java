package com.carlos.pacientes.service;

import com.carlos.commons.dto.pacientes.PacienteRequest;
import com.carlos.commons.dto.pacientes.PacienteResponse;
import com.carlos.commons.service.CrudService;
import com.carlos.pacientes.entity.Paciente;

public interface PacienteService extends CrudService<PacienteRequest, PacienteResponse> {

    PacienteResponse obtenerPacientePorIdSinEstado(Long id);

}
