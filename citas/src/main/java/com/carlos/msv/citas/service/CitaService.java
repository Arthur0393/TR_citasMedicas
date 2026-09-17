package com.carlos.msv.citas.service;

import com.carlos.commons.service.CrudService;
import com.carlos.msv.citas.dto.CitaRequest;
import com.carlos.msv.citas.dto.CitaResponse;

public interface CitaService extends CrudService<CitaRequest, CitaResponse> {

    void  actualizarEstadoCita(Long idCita, Long idEstadoCita);

    boolean pacienteTieneCitaBloqueante(Long idPaciente);
}
