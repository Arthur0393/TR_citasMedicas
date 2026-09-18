package com.carlos.pacientes.service;

import com.carlos.commons.clients.CitaClient;
import com.carlos.commons.dto.pacientes.PacienteRequest;
import com.carlos.commons.dto.pacientes.PacienteResponse;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.exceptions.RecursoNoEncontradoException;
import com.carlos.commons.exceptions.EntidadRelacionadaException;
import com.carlos.pacientes.entity.Paciente;
import com.carlos.pacientes.mapper.PacienteMapper;
import com.carlos.pacientes.repository.PacienteRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
@Transactional
@Slf4j
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;

    // Mapper encargado de transformar Request ↔ Entity ↔ Response.
    private final PacienteMapper pacienteMapper;

    // Cliente Feign utilizado para consultar reglas del microservicio de citas.
    private final CitaClient citaClient;

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPacientePorIdSinEstado(Long id) {

        log.info(
                "Buscando paciente sin importar estado con id {}",
                id
        );

        /*
         * Este endpoint forma parte de la integridad histórica.
         * Puede devolver tanto pacientes ACTIVOS como ELIMINADOS.
         */
        Paciente paciente = pacienteRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Paciente no encontrado con id: " + id
                        )
                );

        return pacienteMapper.entidadAResponse(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponse> listar() {

        log.info("Listando todos los pacientes activos");

        /*
         * Los listados normales solamente muestran registros ACTIVOS.
         */
        return pacienteRepository
                .findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(pacienteMapper::entidadAResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorId(Long id) {

        /*
         * GET /{id} solamente permite consultar pacientes ACTIVOS.
         */
        Paciente paciente =
                obtenerPacienteActivoPorId(id);

        return pacienteMapper.entidadAResponse(paciente);
    }

    @Override
    public PacienteResponse registrar(PacienteRequest request) {

        log.info("Registrando nuevo paciente");

        /*
         * Email y teléfono solamente deben ser únicos
         * entre pacientes con estado ACTIVO.
         */
        validarDatosUnicos(request);

        /*
         * El Mapper se encarga de:
         *
         * - calcular el IMC;
         * - generar el número de expediente;
         * - normalizar los datos;
         * - establecer ESTADO_REGISTRO = ACTIVO.
         */
        Paciente paciente =
                pacienteMapper.requestAEntidad(request);

        pacienteRepository.save(paciente);

        log.info(
                "Nuevo paciente registrado con id {}",
                paciente.getId()
        );

        return pacienteMapper.entidadAResponse(paciente);
    }

    @Override
    public PacienteResponse actualizar(
            PacienteRequest request,
            Long id
    ) {

        // Primero comprobamos que el paciente exista y esté ACTIVO.
        Paciente paciente =
                obtenerPacienteActivoPorId(id);

        log.info(
                "Actualizando paciente con id {}",
                id
        );

        /*
         * CORRECCIÓN DEL PROFESOR:
         *
         * Antes de comprobar email/teléfono debemos verificar
         * que la regla de negocio permita modificar al paciente.
         *
         * Un paciente NO puede actualizarse si tiene una cita:
         *
         * - CONFIRMADA
         * - EN_CURSO
         */
        validarCitasBloqueantesParaPaciente(id);

        /*
         * Una vez comprobada la regla de citas,
         * verificamos la unicidad de email y teléfono.
         */
        validarCambiosUnicos(request, id);

        /*
         * El Mapper calcula nuevamente IMC y expediente
         * cuando corresponda y actualiza la Entity.
         */
        pacienteMapper.actualizarEntidad(
                paciente,
                request
        );

        /*
         * No es obligatorio llamar save() porque la Entity está
         * administrada por JPA dentro de la transacción.
         * Se deja explícito para que el flujo sea fácil de identificar.
         */
        pacienteRepository.save(paciente);

        log.info(
                "Paciente con id {} actualizado correctamente",
                id
        );

        return pacienteMapper.entidadAResponse(paciente);
    }

    @Override
    public void eliminar(Long id) {

        // Solamente se pueden eliminar lógicamente pacientes ACTIVOS.
        Paciente paciente =
                obtenerPacienteActivoPorId(id);

        log.info(
                "Eliminando paciente con id {}",
                id
        );

        /*
         * Un paciente NO puede eliminarse si tiene una cita:
         *
         * - CONFIRMADA
         * - EN_CURSO
         */
        validarCitasBloqueantesParaPaciente(id);

        /*
         * La Entity realiza borrado lógico:
         *
         * ACTIVO → ELIMINADO
         *
         * Nunca se elimina físicamente el registro.
         */
        paciente.eliminar();

        pacienteRepository.save(paciente);

        log.info(
                "Paciente con id {} eliminado logicamente",
                id
        );
    }

    private void validarDatosUnicos(
            PacienteRequest request
    ) {

        log.info(
                "Validando email unico entre pacientes activos"
        );

        if (pacienteRepository
                .existsByEmailIgnoreCaseAndEstadoRegistro(
                        request.email(),
                        EstadoRegistro.ACTIVO
                )) {

            throw new EntidadRelacionadaException(
                    "Ya existe un paciente activo registrado con este email: "
                            + request.email()
            );
        }

        log.info(
                "Validando telefono unico entre pacientes activos"
        );

        if (pacienteRepository
                .existsByTelefonoAndEstadoRegistro(
                        request.telefono(),
                        EstadoRegistro.ACTIVO
                )) {

            throw new EntidadRelacionadaException(
                    "Ya existe un paciente activo registrado con este telefono: "
                            + request.telefono()
            );
        }
    }

    private void validarCambiosUnicos(
            PacienteRequest request,
            Long id
    ) {

        log.info(
                "Validando email unico durante actualización"
        );

        /*
         * Se excluye al propio paciente mediante IdNot,
         * porque conservar su mismo email es perfectamente válido.
         */
        if (pacienteRepository
                .existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(
                        request.email(),
                        EstadoRegistro.ACTIVO,
                        id
                )) {

            throw new EntidadRelacionadaException(
                    "Ya existe un paciente activo registrado con este email: "
                            + request.email()
            );
        }

        log.info(
                "Validando telefono unico durante actualización"
        );

        /*
         * Igual que con el email, se excluye el registro actual.
         */
        if (pacienteRepository
                .existsByTelefonoAndEstadoRegistroAndIdNot(
                        request.telefono(),
                        EstadoRegistro.ACTIVO,
                        id
                )) {

            throw new EntidadRelacionadaException(
                    "Ya existe un paciente activo registrado con este telefono: "
                            + request.telefono()
            );
        }
    }

    private Paciente obtenerPacienteActivoPorId(Long id) {

        log.info(
                "Buscando paciente activo con id {}",
                id
        );

        /*
         * Los endpoints normales trabajan únicamente
         * con registros cuyo ESTADO_REGISTRO sea ACTIVO.
         */
        return pacienteRepository
                .findByIdAndEstadoRegistro(
                        id,
                        EstadoRegistro.ACTIVO
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Paciente activo no encontrado con id: " + id
                        )
                );
    }

    private void validarCitasBloqueantesParaPaciente(
            Long idPaciente
    ) {

        log.info(
                "Validando citas CONFIRMADAS o EN_CURSO del paciente {}",
                idPaciente
        );

        /*
         * Regla específica del módulo PACIENTES:
         *
         * El paciente no puede modificarse ni eliminarse
         * si tiene una cita CONFIRMADA o EN_CURSO.
         *
         * IMPORTANTE:
         * PENDIENTE no bloquea PUT/DELETE del paciente.
         */
        if (citaClient
                .pacienteTieneCitaBloqueanteParaModificacion(
                        idPaciente
                )) {

            throw new IllegalStateException(
                    "El paciente tiene una cita CONFIRMADA o EN_CURSO "
                            + "y no puede actualizarse ni eliminarse"
            );
        }
    }
}