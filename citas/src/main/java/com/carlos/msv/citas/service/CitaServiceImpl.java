package com.carlos.msv.citas.service;

import com.carlos.commons.clients.MedicoClient;
import com.carlos.commons.dto.medicos.MedicoResponse;
import com.carlos.commons.enums.DisponibilidadMedico;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.exceptions.RecursoNoEncontradoException;
import com.carlos.msv.citas.dto.CitaRequest;
import com.carlos.msv.citas.dto.CitaResponse;
import com.carlos.msv.citas.entity.Cita;
import com.carlos.msv.citas.enums.EstadoCita;
import com.carlos.msv.citas.mapper.CitaMapper;
import com.carlos.msv.citas.repository.CitaRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class CitaServiceImpl implements CitaService{

    private final CitaRepository citaRepository;
    private final CitaMapper citaMapper;
    private final MedicoClient medicoClient;

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {
        log.info("Listando todas las citas activas");
        return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(cita -> citaMapper.entidadAResponse(
                        cita,
                        null,
                        obtenerMedicoSinEstado(cita.getIdMedico())
                )).toList();
    }

    @Override
    public CitaResponse obtenerPorId(Long id) {
        Cita cita = obtenerCitaOException(id);

        return citaMapper.entidadAResponse(
                cita,
                null,
                obtenerMedicoSinEstado(cita.getIdMedico())
        );
    }

    @Override
    public CitaResponse registrar(CitaRequest request) {

        log.info("Registrando nueva cita...");

        MedicoResponse medico = obtenerMedicoActivo(request.idMedico());

        validarMedicoActivoDisponible(medico);

        Cita cita = citaMapper.requestAEntidad(request);

        citaRepository.save(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(
                medico.id(), cita.getEstadoCita());

        log.info("Cita registrada exitosamente");

        return citaMapper.entidadAResponse(
                cita,
                null,
                medico);
    }

    @Override
    public CitaResponse actualizar(CitaRequest request, Long id) {
        Cita cita = obtenerCitaOException(id);

        MedicoResponse medico = obtenerMedicoActivo(request.idMedico());
        log.info("Actualizando cita con id: {}", id);
        cita.actualizar(
                request.idPaciente(),
                request.idMedico(),
                request.fechaCita(),
                request.sintomas()
        );
        log.info("Cita actualizada con id: {}", id);
        return citaMapper.entidadAResponse(
                cita,
                null,
                medico
        );
    }


    @Override
    public void actualizarEstadoCita(Long idCita, Long idEstadoCita) {

        Cita cita = obtenerCitaOException(idCita);

        log.info("Actualizando estado de la cita con id: {}", idCita);

        cita.actualizarEstadoCita(EstadoCita.obtenerEstadoCitaPorCodigo(idEstadoCita));

        citaRepository.save(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(cita.getIdMedico(), cita.getEstadoCita());

        log.info("Estado de la cita {} actualizado correctamente", idCita);

    }

    @Override
    public void eliminar(Long id) {
        Cita cita = obtenerCitaOException(id);

        log.info("Eliminando cita con id: {}", id);

        cita.eliminar();

        if (cita.getEstadoCita() == EstadoCita.PENDIENTE)
            actualizarDisponibilidadMedico(cita.getIdMedico(),
                    DisponibilidadMedico.DISPONIBLE.getCodigo());

        log.info("Cita con id {} ha sido marcada como eliminada", id);


    }

    private Cita obtenerCitaOException(Long id) {
        log.info("Buscando cita con id: {}", id);

        return citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada con id: " + id));
    }

    private MedicoResponse obtenerMedicoActivo(Long id) {
        log.info("Buscando medico activo con id {} en el servicio remoto...", id);

        return medicoClient.obtenerMedicoActivoPorId(id);
    }

    private MedicoResponse obtenerMedicoSinEstado(Long id) {
        log.info("Buscando medico sin estado con id {} en el servicio remoto...", id);

        return medicoClient.obtenerMedicoSinEstadoPorId(id);
    }

    private void validarMedicoActivoDisponible(MedicoResponse medico){

        log.info("Validando medico activo esta disponible...");

        if(!DisponibilidadMedico.DISPONIBLE.getCodigo().equals(medico.idDisponibilidad()))
            throw new IllegalStateException("El medico activo no esta disponible para consultar");
    }

    private void actualizarDisponibilidadMedico(Long idMedico, Long idDisponibilidad) {
        log.info("Actualizando disponibilidad del medico en el servidor remoto...");
        medicoClient.actualizarDisponibilidadMedico(idMedico, idDisponibilidad);
        log.info("Disponibilidad del medico actualizada en el servidor remoto");
    }

    private void cambiarDisponibilidadMedicoSegunEstadoCita(Long idMedico, EstadoCita estadoCita) {

        switch(estadoCita){
            case PENDIENTE, CONFIRMADA ->
                    actualizarDisponibilidadMedico(idMedico,DisponibilidadMedico.NO_DISPONIBLE.getCodigo());

            case EN_CURSO ->
                    actualizarDisponibilidadMedico(idMedico,DisponibilidadMedico.EN_CONSULTA.getCodigo());

            case FINALIZADA,CANCELADA ->
                actualizarDisponibilidadMedico(idMedico,DisponibilidadMedico.DISPONIBLE.getCodigo());
        }
    }
    @Override
    @Transactional(readOnly = true)
    public boolean pacienteTieneCitaBloqueante(Long idPaciente) {

        log.info("Validando si el paciente con id {} tiene una cita bloqueante", idPaciente);

        return citaRepository.existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
                idPaciente,
                EstadoRegistro.ACTIVO,
                List.of(
                        EstadoCita.CONFIRMADA,
                        EstadoCita.EN_CURSO
                )
        );
    }
}