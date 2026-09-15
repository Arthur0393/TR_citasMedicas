package com.carlos.msv.medicos.service;

import com.carlos.commons.dto.medicos.MedicoRequest;
import com.carlos.commons.dto.medicos.MedicoResponse;
import com.carlos.commons.enums.DisponibilidadMedico;
import com.carlos.commons.enums.EspecialidadMedico;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.exceptions.RecursoNoEncontradoException;
import com.carlos.msv.medicos.entity.Medico;
import com.carlos.msv.medicos.mapper.MedicoMapper;
import com.carlos.msv.medicos.repository.MedicoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
@Transactional
@Slf4j
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;

    private final MedicoMapper medicoMapper;

    @Transactional(readOnly = true)
    @Override
    public MedicoResponse obtenerMedicoPorIdSinEstado(Long id) {

        log.info("Buscando medico sin estado con id {}", id);

        return medicoMapper.entidadAResponse(
                medicoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Medico sin estado no encontrado con id: " + id
                                )
                        )
        );
    }

    @Override
    public void actualizarDisponibilidadMedico(
            Long idMedico,
            Long idDisponibilidad
    ) {

        Medico medico = obtenerMedicoActivoPorId(idMedico);

        log.info(
                "Actualizando disponibilidad del medico con id: {}",
                idMedico
        );

        DisponibilidadMedico nuevaDisponibilidad =
                DisponibilidadMedico.obtenerDisponibilidadPorCodigo(
                        idDisponibilidad
                );

        DisponibilidadMedico disponibilidadAnterior =
                medico.getDisponibilidad();

        medico.actualizarDisponibilidadMedico(nuevaDisponibilidad);

        log.info(
                "Disponibilidad del medico con id {} cambio de {} a {}",
                idMedico,
                disponibilidadAnterior,
                nuevaDisponibilidad
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicoResponse> listar() {

        log.info("Listando todos los medicos activos");

        return medicoRepository
                .findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(medicoMapper::entidadAResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponse obtenerPorId(Long id) {

        return medicoMapper.entidadAResponse(
                obtenerMedicoActivoPorId(id)
        );
    }

    @Override
    public MedicoResponse registrar(MedicoRequest request) {

        log.info("Registrando nuevo medico");

        Medico medico = medicoMapper.requestAEntidad(request);

        medico.actualizarEspecialidad(
                EspecialidadMedico.obtenerEspecialidadPorCodigo(
                        request.idEspecialidad()
                )
        );

        validarDatosUnicos(request);

        medicoRepository.save(medico);

        log.info("Nuevo medico registrado: {}", medico);

        return medicoMapper.entidadAResponse(medico);
    }

    private void validarDatosUnicos(MedicoRequest request) {

        log.info("Validando email unico");

        if (medicoRepository.existsByEmailIgnoreCaseAndEstadoRegistro(
                request.email(),
                EstadoRegistro.ACTIVO
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un medico activo registrado con este email: "
                            + request.email()
            );
        }

        log.info("Validando telefono unico");

        if (medicoRepository.existsByTelefonoAndEstadoRegistro(
                request.telefono(),
                EstadoRegistro.ACTIVO
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un medico activo registrado con este telefono: "
                            + request.telefono()
            );
        }

        log.info("Validando cedula profesional unica");

        if (medicoRepository.existsByCedulaProfesionalIgnoreCaseAndEstadoRegistro(
                request.cedulaProfesional(),
                EstadoRegistro.ACTIVO
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un medico activo registrado con la cedula profesional: "
                            + request.cedulaProfesional()
            );
        }
    }

    private void validarCambiosUnicos(
            MedicoRequest request,
            Long id
    ) {

        log.info("Validando email unico");

        if (medicoRepository.existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(
                request.email(),
                EstadoRegistro.ACTIVO,
                id
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un medico activo registrado con este email: "
                            + request.email()
            );
        }

        log.info("Validando telefono unico");

        if (medicoRepository.existsByTelefonoAndEstadoRegistroAndIdNot(
                request.telefono(),
                EstadoRegistro.ACTIVO,
                id
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un medico activo registrado con este telefono: "
                            + request.telefono()
            );
        }

        log.info("Validando cedula profesional unica");

        if (medicoRepository.existsByCedulaProfesionalIgnoreCaseAndEstadoRegistroAndIdNot(
                request.cedulaProfesional(),
                EstadoRegistro.ACTIVO,
                id
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un medico activo registrado con la cedula profesional: "
                            + request.cedulaProfesional()
            );
        }
    }

    @Override
    public MedicoResponse actualizar(
            MedicoRequest request,
            Long id
    ) {

        Medico medico = obtenerMedicoActivoPorId(id);

        log.info("Actualizando medico con id {}", id);

        validarCambiosUnicos(request, id);

        medico.actualizar(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.email(),
                request.telefono(),
                request.cedulaProfesional(),
                EspecialidadMedico.obtenerEspecialidadPorCodigo(
                        request.idEspecialidad()
                )
        );

        log.info("Medico actualizado correctamente");

        return medicoMapper.entidadAResponse(medico);
    }

    @Override
    public void eliminar(Long id) {

        Medico medico = obtenerMedicoActivoPorId(id);

        log.info("Eliminando medico con id {}", id);

        medico.eliminar();

        log.info("Medico eliminado exitosamente");
    }

    private Medico obtenerMedicoActivoPorId(Long id) {

        log.info("Buscando medico con id {}", id);

        return medicoRepository
                .findByIdAndEstadoRegistro(
                        id,
                        EstadoRegistro.ACTIVO
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Medico activo no encontrado con id: " + id
                        )
                );
    }
}