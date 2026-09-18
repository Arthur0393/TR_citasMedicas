package com.carlos.msv.medicos.service;

import com.carlos.commons.clients.CitaClient;
import com.carlos.commons.dto.medicos.MedicoRequest;
import com.carlos.commons.dto.medicos.MedicoResponse;
import com.carlos.commons.enums.DisponibilidadMedico;
import com.carlos.commons.enums.EspecialidadMedico;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.exceptions.RecursoNoEncontradoException;
import com.carlos.commons.exceptions.EntidadRelacionadaException;
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

    /*
     * Cliente Feign utilizado para consultar al microservicio de Citas.
     *
     * Nos permite validar las reglas de negocio relacionadas
     * con las citas asignadas al médico.
     */
    private final CitaClient citaClient;

    @Transactional(readOnly = true)
    @Override
    public MedicoResponse obtenerMedicoPorIdSinEstado(Long id) {

        log.info("Buscando medico sin estado con id {}", id);

        /*
         * Este método permite recuperar al médico sin importar
         * si se encuentra ACTIVO o ELIMINADO.
         *
         * Es necesario para conservar la integridad histórica
         * de las citas.
         */
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

        /*
         * Primero comprobamos que el médico exista
         * y se encuentre ACTIVO.
         */
        Medico medico =
                obtenerMedicoActivoPorId(idMedico);

        log.info(
                "Actualizando disponibilidad del medico con id: {}",
                idMedico
        );

        /*
         * Convertimos el identificador recibido
         * al Enum correspondiente.
         */
        DisponibilidadMedico nuevaDisponibilidad =
                DisponibilidadMedico.obtenerDisponibilidadPorCodigo(
                        idDisponibilidad
                );

        /*
         * REGLA DE NEGOCIO:
         *
         * Un médico NO puede pasar a DISPONIBLE mientras
         * tenga una cita activa:
         *
         * - PENDIENTE
         * - CONFIRMADA
         * - EN_CURSO
         *
         * IMPORTANTE:
         * Esta validación solamente se ejecuta cuando se intenta
         * establecer DISPONIBLE.
         *
         * De esta manera Citas todavía puede cambiar automáticamente:
         *
         * PENDIENTE/CONFIRMADA -> NO_DISPONIBLE
         * EN_CURSO             -> EN_CONSULTA
         */
        if (nuevaDisponibilidad == DisponibilidadMedico.DISPONIBLE) {

            validarMedicoPuedeQuedarDisponible(
                    idMedico
            );
        }

        DisponibilidadMedico disponibilidadAnterior =
                medico.getDisponibilidad();

        /*
         * La Entity es responsable de modificar
         * su propio estado interno.
         */
        medico.actualizarDisponibilidadMedico(
                nuevaDisponibilidad
        );

        /*
         * Guardamos explícitamente el cambio.
         */
        medicoRepository.save(medico);

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

        /*
         * Los listados normales solamente muestran
         * registros ACTIVOS.
         */
        return medicoRepository
                .findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(medicoMapper::entidadAResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponse obtenerPorId(Long id) {

        /*
         * Este GET solamente devuelve médicos ACTIVOS.
         */
        return medicoMapper.entidadAResponse(
                obtenerMedicoActivoPorId(id)
        );
    }

    @Override
    public MedicoResponse registrar(MedicoRequest request) {

        log.info("Registrando nuevo medico");

        /*
         * Antes de registrar verificamos la unicidad
         * de email, teléfono y cédula entre médicos ACTIVOS.
         */
        validarDatosUnicos(request);

        /*
         * El Mapper transforma el Request en la Entity.
         *
         * El médico inicia:
         *
         * - ACTIVO
         * - DISPONIBLE
         */
        Medico medico =
                medicoMapper.requestAEntidad(request);

        /*
         * Convertimos el identificador de especialidad
         * al Enum correspondiente.
         */
        medico.actualizarEspecialidad(
                EspecialidadMedico.obtenerEspecialidadPorCodigo(
                        request.idEspecialidad()
                )
        );

        medicoRepository.save(medico);

        log.info(
                "Nuevo medico registrado con id {}",
                medico.getId()
        );

        return medicoMapper.entidadAResponse(medico);
    }

    private void validarDatosUnicos(
            MedicoRequest request
    ) {

        log.info("Validando email unico");

        /*
         * La unicidad solamente aplica entre médicos ACTIVOS.
         */
        if (medicoRepository
                .existsByEmailIgnoreCaseAndEstadoRegistro(
                        request.email(),
                        EstadoRegistro.ACTIVO
                )) {

            throw new EntidadRelacionadaException(
                    "Ya existe un medico activo registrado con este email: "
                            + request.email()
            );
        }

        log.info("Validando telefono unico");

        if (medicoRepository
                .existsByTelefonoAndEstadoRegistro(
                        request.telefono(),
                        EstadoRegistro.ACTIVO
                )) {

            throw new EntidadRelacionadaException(
                    "Ya existe un medico activo registrado con este telefono: "
                            + request.telefono()
            );
        }

        log.info("Validando cedula profesional unica");

        if (medicoRepository
                .existsByCedulaProfesionalIgnoreCaseAndEstadoRegistro(
                        request.cedulaProfesional(),
                        EstadoRegistro.ACTIVO
                )) {

            throw new EntidadRelacionadaException(
                    "Ya existe un medico activo registrado con la cedula profesional: "
                            + request.cedulaProfesional()
            );
        }
    }

    private void validarCambiosUnicos(
            MedicoRequest request,
            Long id
    ) {

        log.info("Validando email unico durante actualizacion");

        /*
         * IdNot permite conservar el mismo valor
         * perteneciente al médico que estamos modificando.
         */
        if (medicoRepository
                .existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(
                        request.email(),
                        EstadoRegistro.ACTIVO,
                        id
                )) {

            throw new EntidadRelacionadaException(
                    "Ya existe un medico activo registrado con este email: "
                            + request.email()
            );
        }

        log.info("Validando telefono unico durante actualizacion");

        if (medicoRepository
                .existsByTelefonoAndEstadoRegistroAndIdNot(
                        request.telefono(),
                        EstadoRegistro.ACTIVO,
                        id
                )) {

            throw new EntidadRelacionadaException(
                    "Ya existe un medico activo registrado con este telefono: "
                            + request.telefono()
            );
        }

        log.info("Validando cedula profesional unica durante actualizacion");

        if (medicoRepository
                .existsByCedulaProfesionalIgnoreCaseAndEstadoRegistroAndIdNot(
                        request.cedulaProfesional(),
                        EstadoRegistro.ACTIVO,
                        id
                )) {

            throw new EntidadRelacionadaException(
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

        /*
         * Primero comprobamos que el médico exista
         * y se encuentre ACTIVO.
         */
        Medico medico =
                obtenerMedicoActivoPorId(id);

        log.info(
                "Actualizando medico con id {}",
                id
        );

        /*
         * CORRECCIÓN DE LA OBSERVACIÓN DEL PROFESOR:
         *
         * Antes de realizar cualquier actualización,
         * comprobamos que el médico NO tenga citas:
         *
         * - CONFIRMADA
         * - EN_CURSO
         *
         * Esta validación debe ejecutarse ANTES
         * de comprobar los campos únicos.
         */
        validarCitasBloqueantesParaModificacion(
                id
        );

        /*
         * Una vez superada la regla de negocio,
         * comprobamos email, teléfono y cédula.
         */
        validarCambiosUnicos(
                request,
                id
        );

        /*
         * Delegamos a la Entity la actualización
         * de sus propios atributos.
         */
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

        medicoRepository.save(medico);

        log.info(
                "Medico con id {} actualizado correctamente",
                id
        );

        return medicoMapper.entidadAResponse(medico);
    }

    @Override
    public void eliminar(Long id) {

        /*
         * Solamente podemos eliminar lógicamente
         * médicos que actualmente estén ACTIVOS.
         */
        Medico medico =
                obtenerMedicoActivoPorId(id);

        log.info(
                "Eliminando medico con id {}",
                id
        );

        /*
         * CORRECCIÓN DE LA OBSERVACIÓN DEL PROFESOR:
         *
         * Un médico NO puede eliminarse mientras
         * tenga una cita:
         *
         * - CONFIRMADA
         * - EN_CURSO
         */
        validarCitasBloqueantesParaModificacion(
                id
        );

        /*
         * La eliminación es lógica:
         *
         * ACTIVO -> ELIMINADO
         */
        medico.eliminar();

        medicoRepository.save(medico);

        log.info(
                "Medico con id {} eliminado logicamente",
                id
        );
    }

    private Medico obtenerMedicoActivoPorId(Long id) {

        log.info(
                "Buscando medico activo con id {}",
                id
        );

        /*
         * Las operaciones normales solamente trabajan
         * con médicos ACTIVOS.
         */
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

    /*
     * Valida las reglas correspondientes al PUT y DELETE
     * del módulo de Médicos.
     *
     * Solamente bloquean:
     *
     * - CONFIRMADA
     * - EN_CURSO
     *
     * Una cita PENDIENTE no bloquea estas dos operaciones.
     */
    private void validarCitasBloqueantesParaModificacion(
            Long idMedico
    ) {

        log.info(
                "Validando citas CONFIRMADAS o EN_CURSO del medico {}",
                idMedico
        );

        if (citaClient
                .medicoTieneCitaBloqueanteParaModificacion(
                        idMedico
                )) {

            throw new IllegalStateException(
                    "El medico tiene una cita CONFIRMADA o EN_CURSO "
                            + "y no puede ser actualizado o eliminado"
            );
        }
    }

    /*
     * Valida específicamente si un médico puede
     * pasar al estado DISPONIBLE.
     *
     * En esta regla sí bloquean:
     *
     * - PENDIENTE
     * - CONFIRMADA
     * - EN_CURSO
     */
    private void validarMedicoPuedeQuedarDisponible(
            Long idMedico
    ) {

        log.info(
                "Validando si el medico {} puede quedar DISPONIBLE",
                idMedico
        );

        if (citaClient
                .medicoTieneCitaActivaBloqueante(
                        idMedico
                )) {

            throw new IllegalStateException(
                    "El medico tiene una cita PENDIENTE, CONFIRMADA "
                            + "o EN_CURSO y no puede quedar DISPONIBLE"
            );
        }
    }
}