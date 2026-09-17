package com.carlos.pacientes.service;

import com.carlos.commons.clients.CitaClient;
import com.carlos.commons.dto.pacientes.PacienteRequest;
import com.carlos.commons.dto.pacientes.PacienteResponse;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.exceptions.RecursoNoEncontradoException;
import com.carlos.pacientes.entity.Paciente;
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
    private final CitaClient citaClient;

    @Transactional(readOnly = true)
    @Override
    public PacienteResponse obtenerPacientePorIdSinEstado(Long id) {

        log.info("Buscando paciente sin estado con id {}", id);

        return convertirAResponse(
                pacienteRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Paciente sin estado no encontrado con id: " + id
                                )
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponse> listar() {

        log.info("Listando todos los pacientes activos");

        return pacienteRepository
                .findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorId(Long id) {

        return convertirAResponse(
                obtenerPacienteActivoPorId(id)
        );
    }

    @Override
    public PacienteResponse registrar(PacienteRequest request) {

        log.info("Registrando nuevo paciente");

        validarDatosUnicos(request);

        Double imc =
                request.peso() / (request.estatura() * request.estatura());

        String numExpediente =
                String.join("X", request.telefono().split("")) + "X";

        Paciente paciente = Paciente.builder()
                .nombre(request.nombre())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .edad(request.edad())
                .peso(request.peso())
                .estatura(request.estatura())
                .imc(imc)
                .email(request.email())
                .numExpediente(numExpediente)
                .telefono(request.telefono())
                .direccion(request.direccion())
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();

        pacienteRepository.save(paciente);

        log.info("Nuevo paciente registrado: {}", paciente);

        return convertirAResponse(paciente);
    }

    private void validarDatosUnicos(PacienteRequest request) {

        log.info("Validando email unico");

        if (pacienteRepository.existsByEmailIgnoreCaseAndEstadoRegistro(
                request.email(),
                EstadoRegistro.ACTIVO
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un paciente activo registrado con este email: "
                            + request.email()
            );
        }

        log.info("Validando telefono unico");

        if (pacienteRepository.existsByTelefonoAndEstadoRegistro(
                request.telefono(),
                EstadoRegistro.ACTIVO
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un paciente activo registrado con este telefono: "
                            + request.telefono()
            );
        }
    }

    private void validarCambiosUnicos(
            PacienteRequest request,
            Long id
    ) {

        log.info("Validando email unico");

        if (pacienteRepository.existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(
                request.email(),
                EstadoRegistro.ACTIVO,
                id
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un paciente activo registrado con este email: "
                            + request.email()
            );
        }

        log.info("Validando telefono unico");

        if (pacienteRepository.existsByTelefonoAndEstadoRegistroAndIdNot(
                request.telefono(),
                EstadoRegistro.ACTIVO,
                id
        )) {
            throw new IllegalArgumentException(
                    "Ya existe un paciente activo registrado con este telefono: "
                            + request.telefono()
            );
        }
    }

    @Override
    public PacienteResponse actualizar(
            PacienteRequest request,
            Long id
    ) {

        Paciente paciente = obtenerPacienteActivoPorId(id);

        log.info("Actualizando paciente con id {}", id);

        validarCambiosUnicos(request, id);

        validarCitasBloqueantes(id);

        paciente.actualizar(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.peso(),
                request.estatura(),
                request.email(),
                request.telefono(),
                request.direccion()
        );

        log.info("Paciente actualizado correctamente");

        return convertirAResponse(paciente);
    }

    @Override
    public void eliminar(Long id) {

        Paciente paciente = obtenerPacienteActivoPorId(id);

        log.info("Eliminando paciente con id {}", id);

        validarCitasBloqueantes(id);

        paciente.eliminar();

        log.info("Paciente eliminado exitosamente");
    }

    private Paciente obtenerPacienteActivoPorId(Long id) {

        log.info("Buscando paciente con id {}", id);

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

    private PacienteResponse convertirAResponse(Paciente paciente) {

        return new PacienteResponse(
                paciente.getId(),
                paciente.getNombre(),
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
    private void validarCitasBloqueantes(Long idPaciente) {

        log.info("Validando citas bloqueantes del paciente con id {}", idPaciente);

        if (citaClient.pacienteTieneCitaBloqueante(idPaciente)) {
            throw new IllegalStateException(
                    "El paciente tiene una cita CONFIRMADA o EN_CURSO y no puede actualizarse ni eliminarse"
            );
        }
    }
}